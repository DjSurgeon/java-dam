package com.hambooking.backend.dto.notification;

import com.hambooking.backend.model.enums.NotificationType;
import com.hambooking.backend.model.enums.RecipientType;

import java.time.LocalDateTime;

public class NotificationResponseDTO {

    private Long id;
    private Long reservationId;
    private RecipientType recipientType;
    private String recipientEmail;
    private NotificationType notificationType;
    private String subject;
    private String message;
    private Boolean isSent;
    private LocalDateTime sentAt;

    public NotificationResponseDTO() {}

    public NotificationResponseDTO(Long id, Long reservationId,
                                   RecipientType recipientType, String recipientEmail,
                                   NotificationType notificationType, String subject,
                                   String message, Boolean isSent, LocalDateTime sentAt) {
        this.id = id;
        this.reservationId = reservationId;
        this.recipientType = recipientType;
        this.recipientEmail = recipientEmail;
        this.notificationType = notificationType;
        this.subject = subject;
        this.message = message;
        this.isSent = isSent;
        this.sentAt = sentAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getReservationId() { return reservationId; }
    public void setReservationId(Long reservationId) { this.reservationId = reservationId; }

    public RecipientType getRecipientType() { return recipientType; }
    public void setRecipientType(RecipientType recipientType) { this.recipientType = recipientType; }

    public String getRecipientEmail() { return recipientEmail; }
    public void setRecipientEmail(String recipientEmail) { this.recipientEmail = recipientEmail; }

    public NotificationType getNotificationType() { return notificationType; }
    public void setNotificationType(NotificationType notificationType) { this.notificationType = notificationType; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Boolean getIsSent() { return isSent; }
    public void setIsSent(Boolean isSent) { this.isSent = isSent; }

    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }
}
