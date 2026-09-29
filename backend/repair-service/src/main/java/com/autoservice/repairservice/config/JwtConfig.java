package com.autoservice.repairservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Configuration
public class JwtConfig {

    @Bean
    public SecretKey jwtSecretKey(
            @Value("${app.jwt.secret}")
            String encodedSecret
    ) {
        byte[] decodedSecret = Base64.getDecoder()
                .decode(encodedSecret);

        return new SecretKeySpec(
                decodedSecret,
                "HmacSHA256"
        );
    }

    @Bean
    public JwtDecoder jwtDecoder(
            SecretKey jwtSecretKey,
            @Value("${app.jwt.issuer}")
            String issuer
    ) {
        NimbusJwtDecoder decoder =
                NimbusJwtDecoder
                        .withSecretKey(jwtSecretKey)
                        .build();

        OAuth2TokenValidator<Jwt> validator =
                new DelegatingOAuth2TokenValidator<>(
                        new JwtTimestampValidator(),
                        new JwtIssuerValidator(issuer)
                );

        decoder.setJwtValidator(validator);

        return decoder;
    }
}