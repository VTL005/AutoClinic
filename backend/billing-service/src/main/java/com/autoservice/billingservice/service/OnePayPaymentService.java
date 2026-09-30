package com.autoservice.billingservice.service;

import com.autoservice.billingservice.config.OnePayProperties;
import com.autoservice.billingservice.domain.enums.PaymentMethod;
import com.autoservice.billingservice.domain.enums.PaymentStatus;
import com.autoservice.billingservice.dto.request.InitiateOnlinePaymentRequest;
import com.autoservice.billingservice.dto.response.PaymentResponse;
import com.autoservice.billingservice.exception.InvalidInvoiceStateException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import java.time.ZoneOffset;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OnePayPaymentService {

    private static final ZoneId VIETNAM_ZONE =
            ZoneId.of("Asia/Ho_Chi_Minh");

    private static final DateTimeFormatter EXPIRY_FORMATTER =
            DateTimeFormatter.ofPattern(
                    "yyyyMMdd'T'HHmmss'Z'"
            );

    private final OnePayProperties onePayProperties;
    private final OnePaySignatureService signatureService;
    private final PaymentService paymentService;

    public PaymentResponse createPaymentUrl(
            Long invoiceId,
            Long customerUserId,
            String clientIp,
            InitiateOnlinePaymentRequest request
    ) {
        if (request.paymentMethod() != PaymentMethod.ONEPAY) {
            throw new InvalidInvoiceStateException(
                    "Phương thức thanh toán phải là ONEPAY."
            );
        }

        onePayProperties.validate();

        PaymentResponse payment =
                paymentService.createOnlinePaymentAttempt(
                        invoiceId,
                        customerUserId,
                        request
                );

        try {
            Map<String, String> parameters =
                    createParameters(
                            payment,
                            normalizeClientIp(clientIp)
                    );

            String secureHash =
                    signatureService.createSignature(parameters);

            parameters.put(
                    "vpc_SecureHashType",
                    "SHA256"
            );

            parameters.put(
                    "vpc_SecureHash",
                    secureHash
            );

            String checkoutUrl =
                    buildCheckoutUrl(parameters);

            return paymentService.updateGatewayDetails(
                    payment.id(),
                    payment.providerOrderCode(),
                    checkoutUrl,
                    null,
                    payment.expiresAt()
            );
        }
        catch (Exception exception) {
            paymentService.failOnlinePayment(
                    payment.providerOrderCode(),
                    PaymentStatus.FAILED,
                    "Không thể tạo liên kết thanh toán OnePay: "
                            + exception.getMessage()
            );

            if (exception
                    instanceof InvalidInvoiceStateException stateException) {
                throw stateException;
            }

            throw new InvalidInvoiceStateException(
                    "Không thể tạo liên kết thanh toán OnePay."
            );
        }
    }

    private Map<String, String> createParameters(
            PaymentResponse payment,
            String clientIp
    ) {
        Map<String, String> parameters =
                new LinkedHashMap<>();

        parameters.put(
                "vpc_Version",
                "2"
        );

        parameters.put(
                "vpc_Command",
                "pay"
        );

        parameters.put(
                "vpc_AccessCode",
                onePayProperties.getAccessCode()
        );

        parameters.put(
                "vpc_Merchant",
                onePayProperties.getMerchantId()
        );

        parameters.put(
                "vpc_MerchTxnRef",
                payment.providerOrderCode()
        );

        parameters.put(
                "vpc_OrderInfo",
                "Thanh toan " + payment.paymentCode()
        );

        parameters.put(
                "vpc_Amount",
                convertAmount(payment.amount())
        );

        parameters.put(
                "vpc_Currency",
                "VND"
        );

        parameters.put(
                "vpc_Locale",
                "vn"
        );

        parameters.put(
                "vpc_ReturnURL",
                onePayProperties.getReturnUrl()
        );

        parameters.put(
                "vpc_IpnURL",
                onePayProperties.getIpnUrl()
        );

        parameters.put(
                "vpc_TicketNo",
                clientIp
        );

        parameters.put(
                "vpc_ExpiryTime",
                payment.expiresAt()
                        .atZone(VIETNAM_ZONE)
                        .withZoneSameInstant(ZoneOffset.UTC)
                        .format(EXPIRY_FORMATTER)
        );

        return parameters;
    }

    private String buildCheckoutUrl(
            Map<String, String> parameters
    ) {
        UriComponentsBuilder builder =
                UriComponentsBuilder.fromUriString(
                        onePayProperties.getGatewayUrl()
                );

        parameters.forEach(
                builder::queryParam
        );

        return builder
                .build()
                .encode(StandardCharsets.UTF_8)
                .toUriString();
    }

    private String convertAmount(
            BigDecimal amount
    ) {
        return amount
                .movePointRight(2)
                .longValueExact()
                + "";
    }

    private String normalizeClientIp(
            String clientIp
    ) {
        if (clientIp == null
                || clientIp.isBlank()
                || "0:0:0:0:0:0:0:1".equals(clientIp)
                || "::1".equals(clientIp)) {
            return "127.0.0.1";
        }

        return clientIp;
    }
}