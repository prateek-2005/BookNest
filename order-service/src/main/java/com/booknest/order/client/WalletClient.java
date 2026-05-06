package com.booknest.order.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;

@Component
public class WalletClient {
    private static final Logger log = LoggerFactory.getLogger(WalletClient.class);

    private final RestTemplate restTemplate;
    private final String walletBaseUrl;

    public WalletClient(
            RestTemplateBuilder restTemplateBuilder,
            @Value("${booknest.wallet.base-url:http://localhost:8085}") String walletBaseUrl
    ) {
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(3))
                .setReadTimeout(Duration.ofSeconds(8))
                .build();
        this.walletBaseUrl = walletBaseUrl;
    }

    public void debitForOrder(Long userId, Double amount, Long orderId) {
        if (userId == null) throw new RuntimeException("UserId required for wallet payment");
        if (amount == null || amount <= 0) throw new RuntimeException("Amount must be greater than 0");

        String url = UriComponentsBuilder
                .fromUriString(walletBaseUrl.replace("lb://", "http://"))
                .path("/wallet/user/{userId}/payMoney")
                .queryParam("amount", amount)
                .queryParam("orderId", orderId)
                .buildAndExpand(userId)
                .toUriString();

        try {
            ResponseEntity<String> res = restTemplate.exchange(url, org.springframework.http.HttpMethod.PUT, null, String.class);
            if (!res.getStatusCode().is2xxSuccessful()) {
                throw new org.springframework.web.server.ResponseStatusException(res.getStatusCode(), "Wallet payment failed with status " + res.getStatusCode());
            }
        } catch (org.springframework.web.client.HttpStatusCodeException e) {
            String msg = "Wallet payment failed";
            try {
                com.fasterxml.jackson.databind.JsonNode node = new com.fasterxml.jackson.databind.ObjectMapper().readTree(e.getResponseBodyAsString());
                if (node.has("message")) msg = node.get("message").asText();
            } catch (Exception ex) {
                if (e.getStatusCode() == org.springframework.http.HttpStatus.BAD_REQUEST) {
                    msg = "Insufficient balance or invalid amount.";
                }
            }
            throw new org.springframework.web.server.ResponseStatusException(e.getStatusCode(), msg);
        } catch (org.springframework.web.server.ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Wallet debit failed for user {}: {}", userId, e.getMessage());
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, "Wallet payment failed");
        }
    }
}

