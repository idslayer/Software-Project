package io.logchain.bundler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.logchain.bundler.config.NormalizerConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.List;

@Slf4j
public class NormalizerWorker implements Runnable {
    private final ObjectMapper mapper = new ObjectMapper();
    private final KafkaConsumer<String, String> consumer;
    private final KafkaProducer<String, String> producer;
    NormalizerConfig normalizerConfig;

    public NormalizerWorker(NormalizerConfig normalizerConfig) {
        this.normalizerConfig = normalizerConfig;
        consumer = new KafkaConsumer<>(normalizerConfig.toConsumerProps());
        producer = new KafkaProducer<>(normalizerConfig.toProducerProps());
        consumer.subscribe(List.of(normalizerConfig.getConsumeTopic()));
    }

    @Override
    public void run() {
        try {
            log.info("Starting NormalizerWorker for topic: {}", normalizerConfig.getConsumeTopic());
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1));
                if (records.isEmpty()) {
                    continue;
                }
                for (ConsumerRecord<String, String> record : records) {
                    try {
                        String normalized = consume(record);
                        producer.send(new ProducerRecord<>(normalizerConfig.getProduceTopic(), record.key(), normalized));
                        consumer.commitAsync();
                    } catch (Exception e) {
                        log.error("Failed to normalize record: {}", record.value(), e);
                    }
                }
                consumer.commitAsync();
            }
        } finally {
            consumer.close();
            producer.close();
        }
    }

    public String consume(ConsumerRecord<String, String> record) throws Exception {
        JsonNode raw = mapper.readTree(record.value());
        NormalizedLog normalized = normalize(raw);
        return mapper.writeValueAsString(normalized);
    }

    private NormalizedLog normalize(JsonNode raw) throws Exception {
        String level = raw.has("level") ? raw.get("level").asText().toUpperCase() : "INFO";
        String message = raw.has("message") ? raw.get("message").asText() : "";
        String source = raw.has("source") ? raw.get("source").asText() : "unknown";

        if (!raw.has("@timestamp")) {
            log.warn("Record missing @timestamp, using current time for normalization: {}, try with timestamp", raw);
        }
        String isoTs = raw.has("@timestamp") ? raw.get("@timestamp").asText() : raw.get("timestamp").asText();
        long ts = Instant.parse(isoTs).toEpochMilli();

        // Add salt + hash
        String salt = generateSalt();
        String hash = computeHash(source, ts, message, salt);

        return new NormalizedLog(level, isoTs, message, source, salt, hash, "my-app", "prod");
    }

    private String generateSalt() {
        byte[] salt = new byte[32];
        new SecureRandom().nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    private String computeHash(String source, long ts, String message, String salt) throws Exception {
        String data = source + "|" + ts + "|" + message + "|" + salt;
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hashBytes);
    }

    public record NormalizedLog(
            String level,
            String timestamp,
            String message,
            String source,
            String salt,
            String hash,
            String app,
            String env
    ) {
    }
}
