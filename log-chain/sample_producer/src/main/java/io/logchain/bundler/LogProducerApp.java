package io.logchain.bundler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.concurrent.TimeUnit;

@Slf4j
@SpringBootApplication
public class LogProducerApp implements ApplicationRunner {

    public static void main(String[] args) {
        SpringApplication.run(LogProducerApp.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Runnable runnable = this::runGenerateLog;
        runnable.run();
    }


    public void runGenerateLog() {
        while (true) {
            int a = 1;
            log.info("SMART-CONTRACT-BOOKING-CREATE - Log written: {}", a);
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
