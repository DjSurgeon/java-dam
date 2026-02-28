package com.hambooking.backend.repository;

import com.hambooking.backend.model.entity.Notification;
import com.hambooking.backend.model.enums.NotificationType;
import com.hambooking.backend.model.enums.RecipientType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByReservation_Id(Long reservationId);

    List<Notification> findByRecipientEmail(String recipientEmail);

    List<Notification> findByRecipientType(RecipientType recipientType);

    List<Notification> findByNotificationType(NotificationType notificationType);

    List<Notification> findByIsSentFalse();
}