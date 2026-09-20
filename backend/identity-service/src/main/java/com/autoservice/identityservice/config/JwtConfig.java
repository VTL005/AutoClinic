package com.autoservice.identityservice.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtConfig {

    private static final int MINIMUM_KEY_LENGTH_BYTES = 32;

    @Bean
    public SecretKey jwtSecretKey(JwtProperties jwtProperties) {
        byte[] keyBytes;

        try {
            keyBytes = Base64.getDecoder()
                    .decode(jwtProperties.secret());
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException(
                    "AUTOSERVICE_JWT_SECRET phải là chuỗi Base64 hợp lệ.",
                    exception
            );
        }

        if (keyBytes.length < MINIMUM_KEY_LENGTH_BYTES) {
            throw new IllegalStateException(
                    "AUTOSERVICE_JWT_SECRET phải có ít nhất 32 byte."
            );
        }

        return new SecretKeySpec(
                keyBytes,
                "HmacSHA256"
        );
    }

    @Bean
    public JwtEncoder jwtEncoder(SecretKey jwtSecretKey) {
        return NimbusJwtEncoder
                .withSecretKey(jwtSecretKey)
                .algorithm(MacAlgorithm.HS256)
                .build();
    }

    @Bean
    public JwtDecoder jwtDecoder(
            SecretKey jwtSecretKey,
            JwtProperties jwtProperties
    ) {
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder
                .withSecretKey(jwtSecretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();

        jwtDecoder.setJwtValidator(
                JwtValidators.createDefaultWithIssuer(
                        jwtProperties.issuer()
                )
        );

        return jwtDecoder;
    }
}