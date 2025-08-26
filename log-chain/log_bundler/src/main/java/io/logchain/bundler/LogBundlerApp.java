package io.logchain.bundler;

import io.logchain.bundler.config.FabricConfig;
import io.logchain.bundler.config.LogChainConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hyperledger.fabric.client.Contract;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@SpringBootApplication
@RequiredArgsConstructor
public class LogBundlerApp implements ApplicationRunner {
    final LogChainConfig bundlerConfig;
    final FabricConfig fabricConfig;
    final Contract contract;

    public static void main(String[] args) {
        SpringApplication.run(LogBundlerApp.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        ExecutorService executor = Executors.newWorkStealingPool();
        int workerCount = bundlerConfig.getNormalizer().getWorkerCount();
        for (int i = 0; i < workerCount; i++) {
            executor.submit(new NormalizerWorker(bundlerConfig.getNormalizer()));
        }

        int bundlerWorkerCount = bundlerConfig.getBundler().getWorkerCount();
        for (int i = 0; i < bundlerWorkerCount; i++) {
            executor.submit(new BundlerWorker(contract, bundlerConfig.getBundler()));
        }
        log.info("LogBundlerApp started successfully with {} normalizer workers and {} bundler workers",
                bundlerConfig.getNormalizer().getWorkerCount(), bundlerConfig.getBundler().getWorkerCount());
    }

}
