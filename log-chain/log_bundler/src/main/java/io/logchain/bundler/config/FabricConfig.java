package io.logchain.bundler.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@NoArgsConstructor
@ConfigurationProperties(prefix = "fabric")
public class FabricConfig {
    String peerEndpoint;
    String overrideAuth;

    String mspId;
    String certPath;
    String keyPath;
    String tlsCertPath;
    String networkName;
    String contractName;
}
