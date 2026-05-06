package com.booknest.notification.service;

import com.booknest.notification.entity.Notification;
import java.util.List;

public interface NotificationService {
    Notification sendNotification(Notification notification);
    Notification markAsRead(Long notificationId);
    void markAllRead(Long userId);
    List<Notification> getByUser(Long userId);
    int getUnreadCount(Long userId);
    void deleteNotification(Long notificationId);
    List<Notification> getAll();
    void notifyAdmins(String type, String message, boolean sendEmail);
}
