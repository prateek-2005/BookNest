package com.booknest.catalog.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

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

    public void notifyAdmins(String type, String message) {
        String url = UriComponentsBuilder
                .fromUriString(notificationBaseUrl.replace("lb://", "http://"))
                .path("/notifications/admin")
                .queryParam("type", type)
                .queryParam("message", message)
                .queryParam("email", true)
                .toUriString();

        try {
            restTemplate.postForEntity(url, null, Void.class);
        } catch (Exception e) {
            log.warn("Failed to broadcast low-stock alert to admins: {}", e.getMessage());
        }
    }
}
