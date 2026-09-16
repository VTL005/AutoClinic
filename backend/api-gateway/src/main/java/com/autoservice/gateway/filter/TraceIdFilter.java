package com.autoservice.gateway.filter;

import java.util.UUID;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@Component
public class TraceIdFilter implements GlobalFilter, Ordered {

    private static final String TRACE_ID_HEADER = "X-Trace-Id";

    @Override
    public Mono<Void> filter(
            ServerWebExchange exchange,
            GatewayFilterChain chain
    ) {
        String incomingTraceId = exchange
                .getRequest()
                .getHeaders()
                .getFirst(TRACE_ID_HEADER);

        final String traceId =
                incomingTraceId == null || incomingTraceId.isBlank()
                        ? UUID.randomUUID().toString()
                        : incomingTraceId;

        ServerHttpRequest updatedRequest = exchange
                .getRequest()
                .mutate()
                .headers(headers ->
                        headers.set(TRACE_ID_HEADER, traceId)
                )
                .build();

        exchange
                .getResponse()
                .getHeaders()
                .set(TRACE_ID_HEADER, traceId);

        ServerWebExchange updatedExchange = exchange
                .mutate()
                .request(updatedRequest)
                .build();

        return chain.filter(updatedExchange);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}