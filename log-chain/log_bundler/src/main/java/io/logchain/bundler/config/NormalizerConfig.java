package io.logchain.bundler.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Properties;


@Slf4j
@Data
@NoArgsConstructor
public class NormalizerConfig {
    String bootstrapServers;
    String groupId;
    String keySerializer;
    String valueSerializer;
    String keyDeserializer;
    String valueDeserializer;
    String autoOffsetReset;
    String consumeTopic;
    String produceTopic;
    int workerCount;

    public Properties toProducerProps() {
        Properties props = new Properties();
        props.put("bootstrap.servers", bootstrapServers);
        props.put("key.serializer", keySerializer);
        props.put("value.serializer", valueSerializer);
        return props;
    }

    public Properties toConsumerProps() {
        Properties props = new Properties();
        props.put("bootstrap.servers", bootstrapServers);
        props.put("group.id", groupId);
        props.put("key.deserializer", keyDeserializer);
        props.put("value.deserializer", valueDeserializer);
        props.put("auto.offset.reset", autoOffsetReset);
        return props;
    }
}
