package com.booknest.notification.service;

import com.booknest.notification.dto.OrderEvent;
import com.booknest.notification.entity.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/*
@Service
@Slf4j
public class OrderEventListener {

    private final NotificationService notificationService;

    public OrderEventListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @RabbitListener(queues = "${notification.rabbitmq.queue}")
    public void consumeOrderEvent(OrderEvent event) {
        log.info("Received order event for order ID: {} with status: {}", event.getOrderId(), event.getOrderStatus());

        Notification notification = new Notification();
        notification.setUserId(event.getUserId());
        notification.setCreatedAt(LocalDateTime.now());
        notification.setRead(false);
        notification.setType("ORDER");
        notification.setChannel("IN_APP");

        String message;
        if ("PLACED".equalsIgnoreCase(event.getOrderStatus())) {
            message = String.format("Success! Your order for '%s' has been placed. Order ID: #%d", 
                    event.getBookTitle(), event.getOrderId());
        } else if ("SHIPPED".equalsIgnoreCase(event.getOrderStatus())) {
            message = String.format("Good news! Your order #%d for '%s' has been shipped.", 
                    event.getOrderId(), event.getBookTitle());
        } else if ("DELIVERED".equalsIgnoreCase(event.getOrderStatus())) {
            message = String.format("Delivered! Your order #%d for '%s' has been successfully delivered.", 
                    event.getOrderId(), event.getBookTitle());
        } else if ("CANCELLED".equalsIgnoreCase(event.getOrderStatus())) {
            message = String.format("Order Cancelled. Your order #%d for '%s' has been cancelled.", 
                    event.getOrderId(), event.getBookTitle());
        } else {
            message = String.format("Order Update: Your order #%d is now %s.", 
                    event.getOrderId(), event.getOrderStatus());
        }

        notification.setMessage(message);
        
        // Send In-App Notification
        notification.setChannel("IN_APP");
        notificationService.sendNotification(notification);
        
        // Send Email Notification
        Notification emailNotif = new Notification();
        emailNotif.setUserId(event.getUserId());
        emailNotif.setType("ORDER_EMAIL");
        emailNotif.setMessage(message);
        emailNotif.setChannel("EMAIL");
        notificationService.sendNotification(emailNotif);
        
        log.info("Notifications (In-App & Email) sent for user ID: {}", event.getUserId());
    }
}
*/
