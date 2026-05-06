package com.booknest.order.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class AuthClient {
    private static final Logger log = LoggerFactory.getLogger(AuthClient.class);

    private final RestTemplate restTemplate;
    private final String authBaseUrl;

    public AuthClient(
            RestTemplate restTemplate,
            @Value("${booknest.auth.base-url:http://AUTH-SERVICE}") String authBaseUrl
    ) {
        this.restTemplate = restTemplate;
        this.authBaseUrl = authBaseUrl;
    }

    public List<Map<String, Object>> getAdmins() {
        String url = authBaseUrl.replace("lb://", "http://") + "/auth/user/role/ADMIN";
        try {
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {}
            );
            return response.getBody() != null ? response.getBody() : Collections.emptyList();
        } catch (Exception e) {
            log.error("Failed to fetch admins from auth-service: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}
