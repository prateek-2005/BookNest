package com.booknest.gateway.filter;

import com.booknest.gateway.util.JwtUtil;
import org.apache.hc.core5.http.HttpStatus;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.http.HttpMethod;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        HttpMethod method = exchange.getRequest().getMethod();

        // Always allow CORS preflight
        if (HttpMethod.OPTIONS.equals(method)) {
            return chain.filter(exchange);
        }

        // Allow public endpoints without token
        if (path.contains("v3/api-docs") || path.contains("swagger-ui") || path.endsWith("swagger-ui.html")) {
            return chain.filter(exchange);
        }

        if (path.startsWith("/auth/customer/register") ||
            path.startsWith("/auth/customer/login") ||
            path.startsWith("/auth/admin/register") ||
            path.startsWith("/auth/admin/login") ||
            path.startsWith("/auth/refresh") ||
            path.startsWith("/auth/me") ||
            path.startsWith("/auth/user") ||
            path.startsWith("/notifications/admin")) {
            return chain.filter(exchange);
        }

        // Public browsing: allow GET catalog + GET reviews
        if (HttpMethod.GET.equals(method) &&
                (path.startsWith("/books") || path.startsWith("/reviews"))) {
            return chain.filter(exchange);
        }

        // Validate JWT for protected endpoints
        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (!jwtUtil.validateToken(token)) {
                exchange.getResponse().setRawStatusCode(HttpStatus.SC_UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
        } else {
            exchange.getResponse().setRawStatusCode(HttpStatus.SC_UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        return chain.filter(exchange);
    }


    @Override
    public int getOrder() {
        return -1;
    }
}
