package dev.pawan.rupixo.common.config;

import dev.pawan.rupixo.common.enums.EventAggregateType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "app.webhook")
@Getter
@Setter
public class
KafkaProperties {
    private Map<String, String> topics = new HashMap<>();

    public String topicFor(EventAggregateType eventAggregateType) {
        String topic = topics.get(eventAggregateType.name().toLowerCase());
        if (topic == null) {
            throw new IllegalArgumentException("No topic configured for event aggregate type: " + eventAggregateType.name());
        }
        return topic;
    }
}
