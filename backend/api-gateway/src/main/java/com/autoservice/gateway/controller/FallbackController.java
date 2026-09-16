package com.autoservice.gateway.controller;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping(
            value = "/{serviceName}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Map<String, Object>> serviceFallback(
            @PathVariable String serviceName
    ) {
        Map<String, Object> response = new LinkedHashMap<>();

        response.put("success", false);
        response.put("errorCode", "SERVICE_UNAVAILABLE");
        response.put(
                "message",
                "Dịch vụ " + serviceName
                        + " hiện không khả dụng. Vui lòng thử lại sau."
        );
        response.put("service", serviceName);
        response.put("timestamp", Instant.now().toString());

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(response);
    }
}