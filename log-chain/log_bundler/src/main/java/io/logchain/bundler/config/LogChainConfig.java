package io.logchain.bundler.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "log-chain")
public class LogChainConfig {
    NormalizerConfig normalizer;
    BundlerConfig bundler;
}
