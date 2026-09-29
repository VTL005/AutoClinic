package com.autoservice.gateway.config;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Base64;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private static final String LOCAL_FRONTEND =
            "http://localhost:5173";

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(
                List.of(LOCAL_FRONTEND)
        );

        configuration.setAllowedMethods(
                List.of(
                        "GET",
                        "POST",
                        "PUT",
                        "PATCH",
                        "DELETE",
                        "OPTIONS"
                )
        );

        configuration.setAllowedHeaders(
                List.of(
                        HttpHeaders.AUTHORIZATION,
                        HttpHeaders.CONTENT_TYPE,
                        HttpHeaders.ACCEPT,
                        "Idempotency-Key",
                        "X-Trace-Id"
                )
        );

        configuration.setExposedHeaders(
                List.of("X-Trace-Id")
        );

        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/**",
                configuration
        );

        return source;
    }

    /*
     * Chế độ phát triển:
     * Identity Service chưa phát JWT nên Gateway cho phép request đi qua.
     */
    @Bean
    @ConditionalOnProperty(
            name = "security.jwt.enabled",
            havingValue = "false",
            matchIfMissing = true
    )
    public SecurityWebFilterChain developmentSecurityFilterChain(
            ServerHttpSecurity http,
            CorsConfigurationSource corsConfigurationSource
    ) {
        return commonSecurityConfiguration(
                http,
                corsConfigurationSource
        )
                .authorizeExchange(exchange -> exchange
                        .anyExchange()
                        .permitAll()
                )
                .build();
    }

    /*
     * Chế độ bảo mật:
     * Được kích hoạt khi security.jwt.enabled=true.
     */
    @Bean
    @ConditionalOnProperty(
            name = "security.jwt.enabled",
            havingValue = "true"
    )
    public SecurityWebFilterChain jwtSecurityFilterChain(
            ServerHttpSecurity http,
            CorsConfigurationSource corsConfigurationSource
    ) {
        ReactiveJwtAuthenticationConverter authenticationConverter =
                jwtAuthenticationConverter();

        return commonSecurityConfiguration(
                http,
                corsConfigurationSource
        )
                .authorizeExchange(exchange -> exchange
                        .pathMatchers(HttpMethod.OPTIONS)
                        .permitAll()

                        .pathMatchers(
                                "/actuator/health",
                                "/actuator/info",
                                "/fallback/**",
                                "/api/v1/auth/login",
                                "/api/v1/auth/register",
                                "/api/v1/auth/refresh",
                                "/api/v1/payments/payos/webhook",
                                "/api/v1/payments/payos/return",
                                "/api/v1/payments/payos/cancel",
                                "/api/v1/payments/onepay/return",
                                "/api/v1/payments/onepay/ipn"
                        )
                        .permitAll()

                        .anyExchange()
                        .authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(
                                        authenticationConverter
                                )
                        )
                        .authenticationEntryPoint(
                                (exchange, exception) ->
                                        writeErrorResponse(
                                                exchange,
                                                HttpStatus.UNAUTHORIZED,
                                                "UNAUTHORIZED",
                                                "Token không hợp lệ hoặc đã hết hạn."
                                        )
                        )
                        .accessDeniedHandler(
                                (exchange, exception) ->
                                        writeErrorResponse(
                                                exchange,
                                                HttpStatus.FORBIDDEN,
                                                "FORBIDDEN",
                                                "Bạn không có quyền truy cập tài nguyên này."
                                        )
                        )
                )
                .build();
    }

    @Bean
    @ConditionalOnProperty(
            name = "security.jwt.enabled",
            havingValue = "true"
    )
    public ReactiveJwtDecoder jwtDecoder(
            @Value("${security.jwt.secret}") String secret
    ) {
        byte[] secretBytes;

        try {
            secretBytes = Base64
                    .getDecoder()
                    .decode(secret);
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException(
                    "JWT_SECRET không phải chuỗi Base64 hợp lệ.",
                    exception
            );
        }

        if (secretBytes.length < 32) {
            throw new IllegalStateException(
                    "JWT_SECRET sau khi giải mã phải có ít nhất 32 byte."
            );
        }

        SecretKey secretKey = new SecretKeySpec(
                secretBytes,
                "HmacSHA256"
        );

        return NimbusReactiveJwtDecoder
                .withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }

    private ServerHttpSecurity commonSecurityConfiguration(
            ServerHttpSecurity http,
            CorsConfigurationSource corsConfigurationSource
    ) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(cors -> cors
                        .configurationSource(corsConfigurationSource)
                )
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .logout(ServerHttpSecurity.LogoutSpec::disable);
    }

    private ReactiveJwtAuthenticationConverter
    jwtAuthenticationConverter() {
        ReactiveJwtAuthenticationConverter converter =
                new ReactiveJwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(
                this::extractAuthorities
        );

        return converter;
    }

    private Flux<GrantedAuthority> extractAuthorities(Jwt jwt) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        String role = jwt.getClaimAsString("role");

        if (role != null && !role.isBlank()) {
            authorities.add(
                    new SimpleGrantedAuthority(
                            normalizeRole(role)
                    )
            );
        }

        Object rolesClaim = jwt.getClaims().get("roles");

        if (rolesClaim instanceof Iterable<?> roles) {
            for (Object currentRole : roles) {
                if (currentRole != null) {
                    authorities.add(
                            new SimpleGrantedAuthority(
                                    normalizeRole(
                                            currentRole.toString()
                                    )
                            )
                    );
                }
            }
        }

        return Flux.fromIterable(authorities);
    }

    private String normalizeRole(String role) {
        String normalizedRole = role
                .trim()
                .toUpperCase();

        if (normalizedRole.startsWith("ROLE_")) {
            return normalizedRole;
        }

        return "ROLE_" + normalizedRole;
    }

    private Mono<Void> writeErrorResponse(
            ServerWebExchange exchange,
            HttpStatus status,
            String errorCode,
            String message
    ) {
        String traceId = exchange
                .getRequest()
                .getHeaders()
                .getFirst("X-Trace-Id");

        String responseBody = """
                {
                  "success": false,
                  "errorCode": "%s",
                  "message": "%s",
                  "traceId": "%s"
                }
                """.formatted(
                errorCode,
                message,
                traceId == null ? "" : traceId
        );

        byte[] bytes = responseBody.getBytes(
                StandardCharsets.UTF_8
        );

        exchange.getResponse().setStatusCode(status);
        exchange.getResponse()
                .getHeaders()
                .setContentType(MediaType.APPLICATION_JSON);

        return exchange
                .getResponse()
                .writeWith(
                        Mono.just(
                                exchange
                                        .getResponse()
                                        .bufferFactory()
                                        .wrap(bytes)
                        )
                );
    }
}