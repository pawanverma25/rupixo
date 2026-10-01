package dev.pawan.rupixo.merchant.entity;

import dev.pawan.rupixo.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "merchant_webhook_config",
indexes = {
        @Index(name="idx_merchant_webhook_merchant_id", columnList="merchant_id, enabled")
})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MerchantWebhookConfig  extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "merchant_id", nullable = false)
    private Merchant merchant;

    @Column(nullable = false, length = 500)
    private String targetUrl; //www.zara.com/webhook/success

    // encrypted not hashed
    @Column(length = 255)
    private String webhookSecret;

    @Column(nullable = false)
    private Boolean enabled = true;

    @Column(length = 255)
    private String eventTypes;
    // Comma-separated list of event types to subscribe to

    public boolean isSubscribedToEventType(@NonNull String eventType){
        if(eventTypes == null || eventTypes.isBlank()){
            return true;
        }
        for(String configEvenType : eventTypes.split(",")){
            String trimmed = configEvenType.trim();
            if("ALL".equalsIgnoreCase(trimmed) || eventType.equalsIgnoreCase(trimmed)){
                return true;
            }
        }
        return false;
    }
}
