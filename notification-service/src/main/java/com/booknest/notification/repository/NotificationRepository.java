package com.booknest.notification.repository;

import com.booknest.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserId(Long userId);

    @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND n.channel = :channel")
    List<Notification> findByUserIdAndChannel(@Param("userId") Long userId, @Param("channel") String channel);

    @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND n.isRead = :isRead AND n.channel = :channel")
    List<Notification> findByUserIdAndIsReadAndChannel(@Param("userId") Long userId, @Param("isRead") boolean isRead, @Param("channel") String channel);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.userId = :userId AND n.isRead = :isRead AND n.channel = :channel")
    int countByUserIdAndIsReadAndChannel(@Param("userId") Long userId, @Param("isRead") boolean isRead, @Param("channel") String channel);

    List<Notification> findByType(String type);

    void deleteByNotificationId(Long notificationId);

    /**
     * Hard-deletes all notifications with the given channel from the database.
     * Used at startup to remove any stray EMAIL-channel records.
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM Notification n WHERE n.channel = :channel")
    int deleteAllByChannel(@Param("channel") String channel);
}
