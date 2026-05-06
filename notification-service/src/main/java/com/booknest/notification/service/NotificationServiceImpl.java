package com.booknest.notification.service;

import com.booknest.notification.entity.Notification;
import com.booknest.notification.repository.NotificationRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {
    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;
    private final org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate;

    @org.springframework.beans.factory.annotation.Value("${notification.rabbitmq.exchange}")
    private String exchange;

    @org.springframework.beans.factory.annotation.Value("${notification.rabbitmq.routingkey}")
    private String routingKey;

    public NotificationServiceImpl(NotificationRepository notificationRepository, 
                                 EmailService emailService,
                                 org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate) {
        this.notificationRepository = notificationRepository;
        this.emailService = emailService;
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * On startup, purge any EMAIL-channel notifications that were accidentally
     * saved to the DB by previous versions of the code. This ensures the app
     * notification panel never shows email notifications.
     */
    @PostConstruct
    public void purgeEmailNotificationsOnStartup() {
        try {
            int deleted = notificationRepository.deleteAllByChannel("EMAIL");
            if (deleted > 0) {
                log.info("[STARTUP-CLEANUP] Purged {} stray EMAIL notifications from database.", deleted);
            }
        } catch (Exception e) {
            log.warn("[STARTUP-CLEANUP] Could not purge EMAIL notifications: {}", e.getMessage());
        }
    }

    @Override
    public Notification markAsRead(Long notificationId) {
        Notification notif = notificationRepository.findById(notificationId).orElseThrow();
        notif.setRead(true);
        return notificationRepository.save(notif);
    }

    @Override
    public void markAllRead(Long userId) {
        List<Notification> notifs = notificationRepository.findAll().stream()
                .filter(n -> n.getUserId().equals(userId) && !n.isRead() && "IN_APP".equalsIgnoreCase(n.getChannel()))
                .toList();
        notifs.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(notifs);
    }

    @Override
    public List<Notification> getByUser(Long userId) {
        // Clean up any stray EMAIL notifications that might have been saved previously
        try {
            List<Notification> strayEmails = notificationRepository.findAll().stream()
                    .filter(n -> n.getUserId() != null && n.getUserId().equals(userId) && "EMAIL".equalsIgnoreCase(n.getChannel()))
                    .toList();
            if (!strayEmails.isEmpty()) {
                notificationRepository.deleteAll(strayEmails);
            }
        } catch (Exception ignored) {}

        return notificationRepository.findAll().stream()
                .filter(n -> n.getUserId() != null && n.getUserId().equals(userId) && "IN_APP".equalsIgnoreCase(n.getChannel()))
                .toList();
    }

    @Override
    public int getUnreadCount(Long userId) {
        return (int) notificationRepository.findAll().stream()
                .filter(n -> n.getUserId() != null && n.getUserId().equals(userId) && !n.isRead() && "IN_APP".equalsIgnoreCase(n.getChannel()))
                .count();
    }

    @Override
    public List<Notification> getAll() {
        return notificationRepository.findAll().stream()
                .filter(n -> "IN_APP".equalsIgnoreCase(n.getChannel()))
                .toList();
    }
    
    @Override
    public void deleteNotification(Long notificationId) {
        notificationRepository.deleteById(notificationId); // use standard JPA method
    }

    @Override
    public Notification sendNotification(Notification notification) {
        notification.setCreatedAt(LocalDateTime.now());
        notification.setRead(false);

        log.info("[ASYNC-DISPATCH] Dispatching notification to RabbitMQ for user: {} channel: {}", 
                notification.getUserId(), notification.getChannel());
        
        // Push to RabbitMQ for asynchronous processing
        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, notification);
        } catch (Exception e) {
            log.error("[RABBITMQ-ERROR] Failed to dispatch notification: {}. Falling back to sync processing.", e.getMessage());
            // Fallback to synchronous processing if RabbitMQ is down
            return processNotificationSync(notification);
        }

        return notification;
    }

    /**
     * Internal method for synchronous processing (used by consumer or as fallback)
     */
    public Notification processNotificationSync(Notification notification) {
        if ("EMAIL".equalsIgnoreCase(notification.getChannel())) {
            if (emailService != null) {
                try {
                    emailService.sendEmail(notification.getUserId(), notification.getMessage());
                } catch (Exception ignored) { }
            }
            return notification; 
        }
        return notificationRepository.save(notification);
    }

    @Override
    public void notifyAdmins(String type, String message, boolean sendEmail) {
        log.info("BROADCAST: Received request to notify all administrators. Type: {}", type);
        try {
            // Fetch admins from AUTH-SERVICE
            java.util.List<java.util.Map> admins = emailService.getRestTemplate().getForObject(
                "http://AUTH-SERVICE/auth/user/role/ADMIN", 
                java.util.List.class
            );

            if (admins == null || admins.isEmpty()) {
                log.warn("BROADCAST: No administrators found in AUTH-SERVICE database for role 'ADMIN'");
                return;
            }

            log.info("BROADCAST: Found {} administrators to notify.", admins.size());

            for (java.util.Map admin : admins) {
                Number adminId = (Number) admin.get("userId");
                String adminEmail = (String) admin.get("email");
                
                if (adminId != null) {
                    log.info("BROADCAST: Notifying Admin [ID: {}, Email: {}]", adminId, adminEmail);
                    
                    // In-App
                    Notification n = new Notification();
                    n.setUserId(adminId.longValue());
                    n.setType(type);
                    n.setMessage(message);
                    n.setChannel("IN_APP");
                    sendNotification(n);

                    // Email if requested
                    if (sendEmail) {
                        Notification emailNotif = new Notification();
                        emailNotif.setUserId(adminId.longValue());
                        emailNotif.setType(type);
                        emailNotif.setMessage(message);
                        emailNotif.setChannel("EMAIL");
                        sendNotification(emailNotif);
                    }
                }
            }
        } catch (Exception e) {
            log.error("BROADCAST ERROR: Failed to process admin broadcast: {}", e.getMessage());
        }
    }
}
