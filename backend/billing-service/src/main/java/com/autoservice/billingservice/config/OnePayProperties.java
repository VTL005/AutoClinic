package com.autoservice.billingservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(
        prefix = "app.payment.onepay"
)
@Getter
@Setter
public class OnePayProperties {

    private String gatewayUrl;

    private String merchantId;

    private String accessCode;

    private String hashKey;

    private String returnUrl;

    private String ipnUrl;

    public void validate() {
        requireValue(
                gatewayUrl,
                "ONEPAY_GATEWAY_URL"
        );

        requireValue(
                merchantId,
                "ONEPAY_MERCHANT_ID"
        );

        requireValue(
                accessCode,
                "ONEPAY_ACCESS_CODE"
        );

        requireValue(
                hashKey,
                "ONEPAY_HASH_KEY"
        );

        requireValue(
                returnUrl,
                "OnePay return URL"
        );

        requireValue(
                ipnUrl,
                "OnePay IPN URL"
        );
    }

    private void requireValue(
            String value,
            String propertyName
    ) {
        if (value == null
                || value.isBlank()) {
            throw new IllegalStateException(
                    propertyName
                            + " chưa được cấu hình."
            );
        }
    }
}