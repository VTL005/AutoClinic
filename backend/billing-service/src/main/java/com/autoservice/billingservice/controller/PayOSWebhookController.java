package com.autoservice.billingservice.controller;

import com.autoservice.billingservice.dto.response.PaymentResponse;
import com.autoservice.billingservice.exception.InvalidInvoiceStateException;
import com.autoservice.billingservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.payos.PayOS;
import vn.payos.model.webhooks.Webhook;
import vn.payos.model.webhooks.WebhookData;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments/payos")
@RequiredArgsConstructor
public class PayOSWebhookController {

    private final PayOS payOS;

    private final PaymentService paymentService;

    @PostMapping("/webhook")
    public ResponseEntity<Map<String, Object>>
    handleWebhook(
            @RequestBody Webhook webhook
    ) {
        /*
         * verify() dùng CHECKSUM_KEY để xác nhận:
         * - webhook thực sự đến từ payOS;
         * - dữ liệu không bị chỉnh sửa.
         */
        WebhookData verifiedData =
                payOS.webhooks()
                        .verify(webhook);

        validateRequiredData(
                verifiedData
        );

        /*
         * payOS có thể gửi giao dịch thử khi xác nhận
         * địa chỉ webhook. Chỉ phản hồi OK, không cập
         * nhật hóa đơn thật.
         */
        if (isWebhookVerificationRequest(
                verifiedData
        )) {
            return successfulResponse(
                    "Webhook verification accepted."
            );
        }

        String providerOrderCode =
                String.valueOf(
                        verifiedData.getOrderCode()
                );

        PaymentResponse payment =
                paymentService
                        .getPaymentByProviderOrderCode(
                                providerOrderCode
                        );

        validateAmount(
                payment,
                verifiedData.getAmount()
        );

        validateSuccessfulWebhook(
                webhook,
                verifiedData
        );

        paymentService.completeOnlinePayment(
                providerOrderCode,
                verifiedData.getReference()
        );

        return successfulResponse(
                "Webhook processed successfully."
        );
    }

    private void validateRequiredData(
            WebhookData data
    ) {
        if (data == null
                || data.getOrderCode() == null
                || data.getAmount() == null) {
            throw new InvalidInvoiceStateException(
                    "Dữ liệu webhook payOS "
                            + "không đầy đủ."
            );
        }
    }

    private boolean isWebhookVerificationRequest(
            WebhookData data
    ) {
        return Long.valueOf(123L)
                .equals(data.getOrderCode());
    }

    private void validateAmount(
            PaymentResponse payment,
            Long receivedAmount
    ) {
        BigDecimal webhookAmount =
                BigDecimal.valueOf(
                        receivedAmount
                );

        if (payment.amount()
                .compareTo(webhookAmount) != 0) {
            throw new InvalidInvoiceStateException(
                    "Số tiền từ webhook không khớp "
                            + "với giao dịch."
            );
        }
    }

    private void validateSuccessfulWebhook(
            Webhook webhook,
            WebhookData data
    ) {
        boolean successful =
                Boolean.TRUE.equals(
                        webhook.getSuccess()
                )
                        && "00".equals(
                        webhook.getCode()
                )
                        && "00".equals(
                        data.getCode()
                );

        if (!successful) {
            throw new InvalidInvoiceStateException(
                    "Webhook không phải thông báo "
                            + "thanh toán thành công."
            );
        }
    }

    private ResponseEntity<Map<String, Object>>
    successfulResponse(
            String message
    ) {
        Map<String, Object> response =
                new LinkedHashMap<>();

        response.put("success", true);
        response.put("message", message);

        return ResponseEntity.ok(response);
    }
}