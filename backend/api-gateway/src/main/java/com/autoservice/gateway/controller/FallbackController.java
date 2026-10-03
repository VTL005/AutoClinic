package com.autoservice.gateway.controller;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    // Không giới hạn HTTP method để nhận cả POST, PATCH, PUT...
    @RequestMapping(
            value = "/{serviceName}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Map<String, Object>> serviceFallback(
            @PathVariable("serviceName") String serviceName
    ) {
        Map<String, Object> response = new LinkedHashMap<>();

        response.put("success", false);
        response.put("errorCode", "SERVICE_UNAVAILABLE");
        response.put(
                "message",
                "Gateway chưa nhận được phản hồi thành công từ dịch vụ "
                        + serviceName
                        + ". Vui lòng kiểm tra trạng thái thao tác "
                        + "trước khi thực hiện lại."
        );
        response.put("service", serviceName);
        response.put("timestamp", Instant.now().toString());

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(response);
    }
}