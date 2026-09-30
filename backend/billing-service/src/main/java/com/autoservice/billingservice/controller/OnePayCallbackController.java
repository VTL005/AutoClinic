package com.autoservice.billingservice.controller;

import com.autoservice.billingservice.dto.response.PaymentResponse;
import com.autoservice.billingservice.service.OnePayCallbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments/onepay")
@RequiredArgsConstructor
public class OnePayCallbackController {

    private final OnePayCallbackService callbackService;

    @GetMapping("/return")
    public ResponseEntity<Map<String, Object>>
    handleReturn(
            @RequestParam
            Map<String, String> parameters
    ) {
        PaymentResponse payment =
                callbackService.processCallback(
                        parameters
                );

        Map<String, Object> response =
                new LinkedHashMap<>();

        response.put(
                "success",
                payment.status().name().equals(
                        "COMPLETED"
                )
        );

        response.put(
                "message",
                createMessage(payment.status().name())
        );

        response.put(
                "paymentId",
                payment.id()
        );

        response.put(
                "paymentCode",
                payment.paymentCode()
        );

        response.put(
                "paymentStatus",
                payment.status()
        );

        response.put(
                "timestamp",
                Instant.now().toString()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping(
            value = "/ipn",
            produces = MediaType.TEXT_PLAIN_VALUE
    )
    public ResponseEntity<String>
    handleIpn(
            @RequestParam
            Map<String, String> parameters
    ) {
        callbackService.processCallback(parameters);

        /*
         * Đây là nội dung OnePay yêu cầu hệ thống
         * trả lại để xác nhận đã nhận IPN.
         */
        return ResponseEntity.ok(
                "responsecode=1&desc=confirm-success"
        );
    }

    private String createMessage(
            String status
    ) {
        return switch (status) {
            case "COMPLETED" ->
                    "Thanh toán OnePay thành công.";

            case "CANCELLED" ->
                    "Khách hàng đã hủy thanh toán OnePay.";

            case "EXPIRED" ->
                    "Giao dịch OnePay đã hết hạn.";

            default ->
                    "Thanh toán OnePay không thành công.";
        };
    }
}