package io.logchain.bundler.config;

import io.grpc.ManagedChannel;
import io.grpc.TlsChannelCredentials;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hyperledger.fabric.client.Contract;
import org.hyperledger.fabric.client.Gateway;
import org.hyperledger.fabric.client.Hash;
import org.hyperledger.fabric.client.Network;
import org.hyperledger.fabric.client.identity.*;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.cert.CertificateException;

@Slf4j
@Configuration
@EnableConfigurationProperties(
        value = {
                LogChainConfig.class,
                FabricConfig.class,
                ElasticConfig.class
        }
)
@RequiredArgsConstructor
public class AppConfig {
    final FabricConfig fabricConfig;

    @Bean
    public Contract initFabric() throws IOException, CertificateException, InvalidKeyException {
        String channelName = fabricConfig.getNetworkName();
        String chaincodeName = fabricConfig.getContractName();

        ManagedChannel channel = newGrpcConnection();
        var builder = Gateway.newInstance()
                .identity(newIdentity())
                .signer(newSigner())
                .hash(Hash.SHA256)
                .connection(channel)
                .evaluateOptions(options -> options.withDeadlineAfter(5, java.util.concurrent.TimeUnit.SECONDS))
                .endorseOptions(options -> options.withDeadlineAfter(15, java.util.concurrent.TimeUnit.SECONDS))
                .submitOptions(options -> options.withDeadlineAfter(5, java.util.concurrent.TimeUnit.SECONDS))
                .commitStatusOptions(options -> options.withDeadlineAfter(1, java.util.concurrent.TimeUnit.MINUTES));
        try (var gateway = builder.connect()) {
            Network network = gateway.getNetwork(channelName);
            var contract = network.getContract(chaincodeName);
            log.info("Connected to Fabric channel '{}' and contract '{}'", channelName, chaincodeName);
            return contract;
        }
    }

    private Path getFirstFilePath(Path dirPath) throws IOException {
        try (var keyFiles = Files.list(dirPath)) {
            return keyFiles.findFirst().orElseThrow();
        }
    }

    private Identity newIdentity() throws IOException, CertificateException {
        try (var certReader = Files.newBufferedReader(getFirstFilePath(Path.of(fabricConfig.certPath)))) {
            var certificate = Identities.readX509Certificate(certReader);
            return new X509Identity(fabricConfig.mspId, certificate);
        }
    }

    private Signer newSigner() throws IOException, InvalidKeyException {
        try (var keyReader = Files.newBufferedReader(getFirstFilePath(Path.of(fabricConfig.keyPath)))) {
            var privateKey = Identities.readPrivateKey(keyReader);
            return Signers.newPrivateKeySigner(privateKey);
        }
    }

    private ManagedChannel newGrpcConnection() throws IOException {
        var credentials = TlsChannelCredentials.newBuilder()
                .trustManager(new File(fabricConfig.tlsCertPath))
                .build();
        return io.grpc.Grpc.newChannelBuilder(fabricConfig.peerEndpoint, credentials)
                .overrideAuthority(fabricConfig.overrideAuth)
                .maxInboundMessageSize(20 * 1024 * 1024) // 20 MB
                .build();
    }

}
