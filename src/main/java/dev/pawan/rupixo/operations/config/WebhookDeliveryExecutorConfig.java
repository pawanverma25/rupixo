package dev.pawan.rupixo.operations.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class WebhookDeliveryExecutorConfig {

    @Bean(name = "virtualThreadExecutorService", destroyMethod = "shutdown")
    public ExecutorService virtualThreadExecutorService() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}
