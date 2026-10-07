package com.autoservice.identityservice.service;

import java.time.Instant;

// Internal transient event; never return the code in an API response or write it to logs.
public record ActivationCodeIssuedEvent(Long userId, String email, String fullName,
        String code, Instant expiresAt) {}
