package com.autoservice.inventoryservice.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtConfig {

    @Bean
    public SecretKey jwtSecretKey(
            JwtProperties properties
    ) {
        byte[] keyBytes =
                Base64.getDecoder()
                        .decode(properties.secret());

        return new SecretKeySpec(
                keyBytes,
                "HmacSHA256"
        );
    }

    @Bean
    public JwtDecoder jwtDecoder(
            SecretKey secretKey,
            JwtProperties properties
    ) {
        NimbusJwtDecoder decoder =
                NimbusJwtDecoder
                        .withSecretKey(secretKey)
                        .macAlgorithm(
                                MacAlgorithm.HS256
                        )
                        .build();

        decoder.setJwtValidator(
                JwtValidators.createDefaultWithIssuer(
                        properties.issuer()
                )
        );

        return decoder;
    }
}