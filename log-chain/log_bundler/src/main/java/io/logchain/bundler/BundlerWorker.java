package io.logchain.bundler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.logchain.bundler.config.BundlerConfig;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.hyperledger.fabric.client.*;
import org.HdrHistogram.Histogram;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.RandomStringUtils.insecure;

@Slf4j
@RequiredArgsConstructor
public class BundlerWorker implements Runnable {
    private final ObjectMapper mapper = new ObjectMapper();
    private final KafkaConsumer<String, String> consumer;
    private final KafkaProducer<String, String> producer;

    private final Histogram freshnessHistogram =
        new Histogram(1, 3_600_000, 3);

    private final Histogram anchorLatencyHistogram =
        new Histogram(1, 3_600_000, 3);
    BundlerConfig bundlerConfig;
    Contract contract;

    public BundlerWorker(Contract contract, BundlerConfig bundlerConfig) throws IOException {
        this.bundlerConfig = bundlerConfig;
        this.contract = contract;
        consumer = new KafkaConsumer<>(bundlerConfig.toConsumerProps());
        producer = new KafkaProducer<>(bundlerConfig.toProducerProps());
        consumer.subscribe(List.of(bundlerConfig.getConsumeTopic()));
    }


    @Override
    public void run() {
        freshnessHistogram.reset();
        anchorLatencyHistogram.reset();
        List<String> batch = new ArrayList<>(bundlerConfig.getBatchSize());
        try {
            log.debug("Starting BundlerWorker for topic: {}", bundlerConfig.getConsumeTopic());
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1));
                if (records.isEmpty()) {
                    continue;
                }
                log.info("Polled {} records", records.count());
                for (ConsumerRecord<String, String> record : records) {
                    batch.add(record.value());
                    if (batch.size() >= bundlerConfig.getBatchSize()) {
                        anchorBatch(batch);
                        batch.clear();
                        consumer.commitAsync();
                    }
                }
            }
        } finally {
            consumer.close();
            producer.close();
            log.info("BundlerWorker stopped");
        }
    }

    @SneakyThrows
    private void anchorBatch(List<String> batch) {
        log.info("Anchoring batch of {} logs", batch.size());
        String batchId = System.currentTimeMillis() + "-" + insecure().nextAlphabetic(3);
        List<JsonNode> nodes = new ArrayList<>(batch.size());
        List<String> leaves = new ArrayList<>(batch.size());

        long startTs = Long.MAX_VALUE;
        long endTs = 0L;
        List<JsonNode> filteredTrans = new ArrayList<>();
        long commitAt = 0;
        for (String logMessage : batch) {
            JsonNode node = mapper.readTree(logMessage);
            nodes.add(node);

            String hash = node.has("hash") ? node.get("hash").asText() : MerkleAlgorithm.computeHash(logMessage);
            leaves.add(hash);

            // extract timestamp
            long ts = Instant.parse(node.get("timestamp").asText()).toEpochMilli();
            commitAt = Instant.now().toEpochMilli();
            long freshness = commitAt - ts; // ts = emitAt
            try{
                freshnessHistogram.recordValue(freshness);
            } catch (Exception e) {
                log.error("Error recording freshness value: {}", e.getMessage());
            }

            if (ts < startTs) startTs = ts;
            if (ts > endTs) endTs = ts;

            // Filter
            for (String f : bundlerConfig.getFilter()) {
                String message = node.get("message").textValue();
                if (message.contains(f)) {
                    log.debug("Filtered log: {}", message);
                    filteredTrans.add(node);
                }
            }
        }
        log.debug("Start merkel root computation");
        String merkleRoot = MerkleAlgorithm.computeMerkleRoot(leaves);


        // Anchor to Fabric
        String fabricTxId = anchorMerkleRoot(batchId,
                merkleRoot,
                batch.size(),
                startTs,
                endTs,
                mapper.writeValueAsString(filteredTrans)
        );
        log.debug("End merkel root computation");
        long anchorLatency = Instant.now().toEpochMilli() - commitAt;
        try{
            anchorLatencyHistogram.recordValue(anchorLatency);
        } catch (Exception e) {
            log.error("Error recording freshness value: {}", e.getMessage());
        }

        log.info("FabricTxId {}", fabricTxId);
        for (JsonNode node : nodes) {
            Map<String, Object> enriched = new HashMap<>();
            enriched.put("log", node);
            enriched.put("batchId", batchId);
            enriched.put("merkleRoot", merkleRoot);
            enriched.put("fabricTxId", fabricTxId);
            enriched.put("anchoredAt", Instant.now().toString());

            String anchored = mapper.writeValueAsString(enriched);
            producer.send(new ProducerRecord<>(bundlerConfig.getProduceTopic(), null, anchored));
        }
        // In các percentile
        log.info("Freshness p50={} ms, p95={} ms, p99={} ms",
            freshnessHistogram.getValueAtPercentile(50.0),
            freshnessHistogram.getValueAtPercentile(95.0),
            freshnessHistogram.getValueAtPercentile(99.0));

        log.info("Anchor Latency p50={} ms, p95={} ms, p99={} ms",
            anchorLatencyHistogram.getValueAtPercentile(50.0),
            anchorLatencyHistogram.getValueAtPercentile(95.0),
            anchorLatencyHistogram.getValueAtPercentile(99.0));


    }


    /**
     * Anchors the Merkle root to the Fabric contract with retry logic.
     * Submits the "PutAnchor" transaction, retrying up to maxRetries if the contract is unavailable.
     * Throws RuntimeException if all attempts fail.
     */
    private String anchorMerkleRoot(
            String batchId, String merkleRoot,
            int count, long startTsMillis, long endTsMillis,
            String note
    ) {
        int maxRetries = bundlerConfig.getMaxRetries();
        long delayMillis = bundlerConfig.getDelayMillis();
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                byte[] result = contract.submitTransaction(
                        "PutAnchor",
                        batchId,
                        merkleRoot,
                        String.valueOf(count),
                        String.valueOf(startTsMillis),
                        String.valueOf(endTsMillis),
                        String.valueOf(System.currentTimeMillis()),
                        "",
                        note
                );
                return new String(result, StandardCharsets.UTF_8);
            } catch (EndorseException | SubmitException | CommitStatusException | CommitException e) {
                log.warn("anchorMerkleRoot attempt {} failed: {}", attempt, e.getMessage());
                if (attempt == maxRetries) throw new RuntimeException("Contract unavailable after retries", e);
                try {
                    Thread.sleep(delayMillis);
                } catch (InterruptedException ignored) {
                }
            } catch (Exception e) {
                throw new RuntimeException("Unexpected error during anchorMerkleRoot", e);
            }
        }
        throw new RuntimeException("Failed to anchor Merkle root after retries");
    }
}
