package com.autoservice.identityservice.dto.response;

public record AuthTokenResponse(

        String accessToken,

        String refreshToken,

        String tokenType,

        long expiresInSeconds,

        UserResponse user

) {
}