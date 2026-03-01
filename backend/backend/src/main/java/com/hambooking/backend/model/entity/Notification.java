package com.hambooking.backend.model.entity;

import com.hambooking.backend.model.enums.NotificationType;
import com.hambooking.backend.model.enums.RecipientType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entidad JPA que representa el historial y envío de notificaciones.
 * Mapea a la tabla 'notifications' en MySQL.
 */
@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // =================================================================================
    // RELACIÓN (OPCIONAL) CON RESERVA
    // =================================================================================
    // reservation_id BIGINT UNSIGNED (No lleva NOT NULL porque una notificación podría ser genérica)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id")
    private Reservation reservation;

    // =================================================================================
    // DATOS DE LA NOTIFICACIÓN
    // =================================================================================

    // recipient_type ENUM NOT NULL
    @NotNull(message = "El tipo de destinatario es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "recipient_type", nullable = false)
    private RecipientType recipientType;

    // recipient_email VARCHAR(150) NOT NULL
    @NotBlank(message = "El email del destinatario es obligatorio")
    @Email(message = "El email debe tener un formato válido")
    @Size(max = 150, message = "El email no puede exceder los 150 caracteres")
    @Column(name = "recipient_email", nullable = false, length = 150)
    private String recipientEmail;

    // notification_type ENUM NOT NULL
    @NotNull(message = "El tipo de notificación es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false)
    private NotificationType notificationType;

    // subject VARCHAR(255) NOT NULL
    @NotBlank(message = "El asunto es obligatorio")
    @Size(max = 255, message = "El asunto no puede exceder los 255 caracteres")
    @Column(nullable = false)
    private String subject;

    // message TEXT NOT NULL
    @NotBlank(message = "El mensaje es obligatorio")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    // is_sent BOOLEAN DEFAULT TRUE
    @Column(name = "is_sent", nullable = false)
    @Builder.Default
    private Boolean isSent = true;

    // sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    @CreationTimestamp
    @Column(name = "sent_at", updatable = false)
    private LocalDateTime sentAt;

    // =================================================================================
    // EQUALS, HASHCODE Y TOSTRING SEGUROS
    // =================================================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Notification)) return false;
        Notification that = (Notification) o;
        return id != null && id.equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        // Obtenemos el ID de la reserva de forma segura para no desencadenar peticiones a BD
        Long resId = (reservation != null) ? reservation.getId() : null;

        return "Notification{" +
                "id=" + id +
                ", reservationId=" + resId +
                ", recipientType=" + recipientType +
                ", recipientEmail='" + recipientEmail + '\'' +
                ", notificationType=" + notificationType +
                ", subject='" + subject + '\'' +
                ", isSent=" + isSent +
                ", sentAt=" + sentAt +
                '}';
    }
}