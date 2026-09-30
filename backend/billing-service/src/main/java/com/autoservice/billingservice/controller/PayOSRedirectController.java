package com.autoservice.billingservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments/payos")
public class PayOSRedirectController {

    @GetMapping("/return")
    public ResponseEntity<Map<String, Object>>
    handleReturn(
            @RequestParam
            Map<String, String> parameters
    ) {
        Map<String, Object> response =
                new LinkedHashMap<>();

        response.put("success", true);
        response.put(
                "message",
                "Đã nhận kết quả trả về từ payOS. "
                        + "Hệ thống đang xác minh thanh toán."
        );
        response.put(
                "paymentResult",
                "PROCESSING"
        );
        response.put(
                "orderCode",
                parameters.get("orderCode")
        );
        response.put(
                "payOSStatus",
                parameters.get("status")
        );
        response.put(
                "timestamp",
                Instant.now().toString()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/cancel")
    public ResponseEntity<Map<String, Object>>
    handleCancel(
            @RequestParam
            Map<String, String> parameters
    ) {
        Map<String, Object> response =
                new LinkedHashMap<>();

        response.put("success", false);
        response.put(
                "message",
                "Khách hàng đã hủy quá trình "
                        + "thanh toán payOS."
        );
        response.put(
                "paymentResult",
                "CANCELLED_BY_CUSTOMER"
        );
        response.put(
                "orderCode",
                parameters.get("orderCode")
        );
        response.put(
                "payOSStatus",
                parameters.get("status")
        );
        response.put(
                "timestamp",
                Instant.now().toString()
        );

        return ResponseEntity.ok(response);
    }
}