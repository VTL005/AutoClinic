package com.autoservice.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RouteConfig {

    @Bean
    public RouteLocator gatewayRoutes(RouteLocatorBuilder builder) {
        return builder.routes()

                // Identity Service - port 8081
                .route("identity-service", route -> route
                        .path(
                                "/api/v1/auth/**",
                                "/api/v1/users/**",
                                "/api/v1/mechanics/**"
                        )
                        .filters(filter -> filter
                                .circuitBreaker(config -> config
                                        .setName("identityServiceCircuitBreaker")
                                        .setFallbackUri(
                                                "forward:/fallback/identity-service"
                                        )
                                )
                        )
                        .uri("http://localhost:8081")
                )

                // Vehicle Service - port 8082
                .route("vehicle-service", route -> route
                        .path("/api/v1/vehicles/**")
                        .filters(filter -> filter
                                .circuitBreaker(config -> config
                                        .setName("vehicleServiceCircuitBreaker")
                                        .setFallbackUri(
                                                "forward:/fallback/vehicle-service"
                                        )
                                )
                        )
                        .uri("http://localhost:8082")
                )

                // Booking Service - port 8083
                .route("booking-service", route -> route
                        .path(
                                "/api/v1/appointments/**",
                                "/api/v1/service-types/**",
                                "/api/v1/availability/**"
                        )
                        .filters(filter -> filter
                                .circuitBreaker(config -> config
                                        .setName("bookingServiceCircuitBreaker")
                                        .setFallbackUri(
                                                "forward:/fallback/booking-service"
                                        )
                                )
                        )
                        .uri("http://localhost:8083")
                )

                // Repair Service - port 8084
                .route("repair-service", route -> route
                        .path("/api/v1/repair-orders/**")
                        .filters(filter -> filter
                                .circuitBreaker(config -> config
                                        .setName("repairServiceCircuitBreaker")
                                        .setFallbackUri(
                                                "forward:/fallback/repair-service"
                                        )
                                )
                        )
                        .uri("http://localhost:8084")
                )

                // Inventory Service - port 8085
                .route("inventory-service", route -> route
                        .path(
                                "/api/v1/parts/**",
                                "/api/v1/inventory/**"
                        )
                        .filters(filter -> filter
                                .circuitBreaker(config -> config
                                        .setName("inventoryServiceCircuitBreaker")
                                        .setFallbackUri(
                                                "forward:/fallback/inventory-service"
                                        )
                                )
                        )
                        .uri("http://localhost:8085")
                )

                // Billing Service - port 8086
                .route("billing-service", route -> route
                        .path(
                                "/api/v1/invoices/**",
                                "/api/v1/payments/**"
                        )
                        .filters(filter -> filter
                                .circuitBreaker(config -> config
                                        .setName("billingServiceCircuitBreaker")
                                        .setFallbackUri(
                                                "forward:/fallback/billing-service"
                                        )
                                )
                        )
                        .uri("http://localhost:8086")
                )

                // Notification Service - port 8087
                .route("notification-service", route -> route
                        .path("/api/v1/notifications/**")
                        .filters(filter -> filter
                                .circuitBreaker(config -> config
                                        .setName("notificationServiceCircuitBreaker")
                                        .setFallbackUri(
                                                "forward:/fallback/notification-service"
                                        )
                                )
                        )
                        .uri("http://localhost:8087")
                )

                .build();
    }
}