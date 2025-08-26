package io.logchain.bundler.config;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Properties;

@Data
@NoArgsConstructor
public class BundlerConfig {
    String bootstrapServers;
    String groupId;
    String keySerializer;
    String valueSerializer;
    String keyDeserializer;
    String valueDeserializer;
    String autoOffsetReset;
    String produceTopic;
    String consumeTopic;
    List<String> filter;
    int maxRetries;
    int delayMillis;
    int workerCount;
    int batchSize;

    public Properties toConsumerProps() {
        Properties props = new Properties();
        props.put("group.id", groupId);
        props.put("bootstrap.servers", bootstrapServers);
        props.put("key.deserializer", keyDeserializer);
        props.put("value.deserializer", valueDeserializer);
        return props;
    }

    public Properties toProducerProps() {
        Properties props = new Properties();
        props.put("bootstrap.servers", bootstrapServers);
        props.put("group.id", groupId);
        props.put("key.serializer", keySerializer);
        props.put("value.serializer", valueSerializer);
        props.put("auto.offset.reset", autoOffsetReset);
        return props;
    }

}
