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
import java.util.HashMap;
import java.util.Map;

@Component
public class NotificationClient {
    private static final Logger log = LoggerFactory.getLogger(NotificationClient.class);

    private final RestTemplate restTemplate;
    private final String notificationBaseUrl;

    public NotificationClient(
            RestTemplate restTemplate,
            @Value("${booknest.notification.base-url:http://NOTIFICATION-SERVICE}") String notificationBaseUrl
    ) {
        this.restTemplate = restTemplate;
        this.notificationBaseUrl = notificationBaseUrl;
    }

    public void sendInAppNotification(Long userId, String type, String message) {
        sendNotification(userId, type, message, "IN_APP");
    }

    public void sendEmailNotification(Long userId, String type, String message) {
        sendNotification(userId, type, message, "EMAIL");
    }

    public void notifyAdmins(String type, String message, boolean email) {
        String url = UriComponentsBuilder
                .fromUriString(notificationBaseUrl.replace("lb://", "http://"))
                .path("/notifications/admin")
                .queryParam("type", type)
                .queryParam("message", message)
                .queryParam("email", email)
                .toUriString();

        try {
            restTemplate.postForEntity(url, null, Void.class);
        } catch (Exception e) {
            log.warn("Failed to broadcast admin notification: {}", e.getMessage());
        }
    }

    private void sendNotification(Long userId, String type, String message, String channel) {
        if (userId == null) return;

        String url = notificationBaseUrl + "/notifications";
        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", userId);
        payload.put("type", type);
        payload.put("message", message);
        payload.put("channel", channel);

        try {
            ResponseEntity<String> res = restTemplate.postForEntity(url, payload, String.class);
            if (!res.getStatusCode().is2xxSuccessful()) {
                log.warn("Notification service returned status {} for user {} channel {}", res.getStatusCode(), userId, channel);
            }
        } catch (Exception e) {
            log.warn("Failed to send notification for user {} channel {}: {}", userId, channel, e.getMessage());
        }
    }
}

