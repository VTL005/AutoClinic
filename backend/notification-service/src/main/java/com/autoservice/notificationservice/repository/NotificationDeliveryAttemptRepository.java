package com.autoservice.notificationservice.repository;

import com.autoservice.notificationservice.domain.entity.NotificationDeliveryAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationDeliveryAttemptRepository
        extends JpaRepository<
        NotificationDeliveryAttempt,
        Long
        > {

    List<NotificationDeliveryAttempt>
    findAllByNotificationIdOrderByAttemptNumberAsc(
            Long notificationId
    );

    long countByNotificationId(
            Long notificationId
    );
}