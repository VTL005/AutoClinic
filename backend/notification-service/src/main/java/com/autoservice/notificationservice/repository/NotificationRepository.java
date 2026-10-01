package com.autoservice.notificationservice.repository;

import com.autoservice.notificationservice.domain.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface NotificationRepository
        extends JpaRepository<Notification, Long> {

    Page<Notification> findAllByRecipientUserId(
            Long recipientUserId,
            Pageable pageable
    );

    Optional<Notification> findByIdAndRecipientUserId(
            Long id,
            Long recipientUserId
    );

    Optional<Notification> findByIdempotencyKey(
            String idempotencyKey
    );

    boolean existsByIdempotencyKey(
            String idempotencyKey
    );

    long countByRecipientUserIdAndReadAtIsNull(
            Long recipientUserId
    );

    @Modifying
    @Query("""
            UPDATE Notification notification
            SET notification.readAt = :readAt
            WHERE notification.recipientUserId = :recipientUserId
              AND notification.readAt IS NULL
            """)
    int markAllAsRead(
            @Param("recipientUserId")
            Long recipientUserId,

            @Param("readAt")
            LocalDateTime readAt
    );
}