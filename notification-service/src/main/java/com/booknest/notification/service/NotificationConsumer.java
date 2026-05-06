package com.booknest.notification.service;

import com.booknest.notification.entity.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationConsumer {

    private final NotificationServiceImpl notificationService;

    public NotificationConsumer(NotificationServiceImpl notificationService) {
        this.notificationService = notificationService;
    }

    @RabbitListener(queues = "${notification.rabbitmq.queue}")
    public void handleNotification(Notification notification) {
        log.info("[ASYNC-CONSUME] Received notification for processing: user={}, channel={}", 
                notification.getUserId(), notification.getChannel());
        
        try {
            notificationService.processNotificationSync(notification);
            log.info("[ASYNC-CONSUME] Successfully processed notification for user: {}", notification.getUserId());
        } catch (Exception e) {
            log.error("[ASYNC-CONSUME] Failed to process notification: {}", e.getMessage());
        }
    }
}
