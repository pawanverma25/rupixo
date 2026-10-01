package dev.pawan.rupixo.merchant.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateWebhookConfigRequest(
        @NotBlank(message = "Webhook URL is required")
        @Size(max = 500)
        @Pattern(regexp = "http?://.+", message = "Webhook URL must bea valid http(s) URL")
        String targetUrl,

        // comma separated event names i.e. "PAYMENT_STATUS_UPDATED,REFUND_CREATED"
        // NULL/BLANK/"ALL" subscribe to everything
        @Size(max = 1000)
        String eventTypes
) {
}
