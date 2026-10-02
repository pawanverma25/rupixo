package dev.pawan.rupixo.operations.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class WebhookClientConfig {

    @Value("${app.webhook.delivery.client.connect-timeout-ms:5000}")
    private int connectTimeout;

    @Value("${app.webhook.delivery.client.read-timeout-ms:5000}")
    private int readTimeout;

    @Bean
    public RestClient webhookRestClient() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeout);
        requestFactory.setReadTimeout(readTimeout);

        return RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }

}
