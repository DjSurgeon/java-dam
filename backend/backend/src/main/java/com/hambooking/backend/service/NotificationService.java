package com.hambooking.backend.service;

import com.hambooking.backend.dto.notification.NotificationResponseDTO;
import com.hambooking.backend.model.entity.Notification;
import com.hambooking.backend.model.entity.Reservation;
import com.hambooking.backend.model.enums.NotificationType;
import com.hambooking.backend.model.enums.RecipientType;
import com.hambooking.backend.model.enums.Role;
import com.hambooking.backend.repository.NotificationRepository;
import com.hambooking.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository,
                               UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    // ─────────────────────────────────────────
    // Método principal — genera 3 notificaciones por evento
    // ─────────────────────────────────────────
    @Transactional
    public void sendReservationNotification(Reservation reservation, NotificationType type) {

        String subject = buildSubject(type, reservation);

        sendSingle(reservation, type, RecipientType.CLIENT,
                reservation.getClient().getEmail(), subject,
                buildClientMessage(type, reservation));

        sendSingle(reservation, type, RecipientType.CARVER,
                reservation.getCarver().getUser().getEmail(), subject,
                buildCarverMessage(type, reservation));

        String adminEmail = userRepository.findByRole(Role.ADMIN)
                .stream()
                .findFirst()
                .map(u -> u.getEmail())
                .orElse("admin@hambooking.com");

        sendSingle(reservation, type, RecipientType.ADMIN,
                adminEmail, subject,
                buildAdminMessage(type, reservation));
    }

    // ─────────────────────────────────────────
    // LISTAR TODAS LAS NOTIFICACIONES  ← NUEVO
    // ─────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<NotificationResponseDTO> listAllNotifications() {
        return notificationRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────
    // Consulta — historial por reserva
    // ─────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<NotificationResponseDTO> getNotificationsByReservation(Reservation reservation) {
        return notificationRepository.findByReservation(reservation)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────
    // Privados — construcción y persistencia
    // ─────────────────────────────────────────
    private void sendSingle(Reservation reservation, NotificationType type,
                            RecipientType recipientType, String email,
                            String subject, String message) {

        Notification notification = Notification.builder()
                .reservation(reservation)
                .notificationType(type)
                .recipientType(recipientType)
                .recipientEmail(email)
                .subject(subject)
                .message(message)
                .isSent(true)
                .build();

        notificationRepository.save(notification);

        logger.info("[NOTIFICATION] {} → {} | {} | {}",
                type, recipientType, email, subject);
    }

    private String buildSubject(NotificationType type, Reservation reservation) {
        return switch (type) {
            case CREATED   -> "Nueva reserva confirmada - " + reservation.getService().getName();
            case MODIFIED  -> "Reserva modificada - " + reservation.getService().getName();
            case CANCELLED -> "Reserva cancelada - " + reservation.getService().getName();
            case REMINDER  -> "Recordatorio de reserva - " + reservation.getService().getName();
        };
    }

    private String buildClientMessage(NotificationType type, Reservation reservation) {
        String base = "Hola " + reservation.getClient().getFirstName() + ",\n\n";
        return base + switch (type) {
            case CREATED   -> "Tu reserva de " + reservation.getService().getName()
                    + " ha sido creada para el " + reservation.getReservationDate()
                    + " a las " + reservation.getStartTime() + ".";
            case MODIFIED  -> "Tu reserva de " + reservation.getService().getName()
                    + " ha sido modificada. Nueva fecha: " + reservation.getReservationDate()
                    + " a las " + reservation.getStartTime() + ".";
            case CANCELLED -> "Tu reserva de " + reservation.getService().getName()
                    + " del " + reservation.getReservationDate() + " ha sido cancelada.";
            case REMINDER  -> "Recuerda que tienes una reserva de " + reservation.getService().getName()
                    + " mañana " + reservation.getReservationDate()
                    + " a las " + reservation.getStartTime() + ".";
        };
    }

    private String buildCarverMessage(NotificationType type, Reservation reservation) {
        String base = "Hola " + reservation.getCarver().getUser().getFirstName() + ",\n\n";
        return base + switch (type) {
            case CREATED   -> "Tienes una nueva reserva de " + reservation.getService().getName()
                    + " el " + reservation.getReservationDate()
                    + " a las " + reservation.getStartTime() + ".";
            case MODIFIED  -> "Una reserva de " + reservation.getService().getName()
                    + " ha sido modificada. Nueva fecha: " + reservation.getReservationDate()
                    + " a las " + reservation.getStartTime() + ".";
            case CANCELLED -> "La reserva de " + reservation.getService().getName()
                    + " del " + reservation.getReservationDate() + " ha sido cancelada.";
            case REMINDER  -> "Recuerda que tienes una reserva de " + reservation.getService().getName()
                    + " mañana " + reservation.getReservationDate()
                    + " a las " + reservation.getStartTime() + ".";
        };
    }

    private String buildAdminMessage(NotificationType type, Reservation reservation) {
        String clientName = reservation.getClient().getFirstName()
                + " " + reservation.getClient().getLastName();
        String carverName = reservation.getCarver().getUser().getFirstName()
                + " " + reservation.getCarver().getUser().getLastName();
        return switch (type) {
            case CREATED   -> "Nueva reserva creada. Cliente: " + clientName
                    + " | Cortador: " + carverName
                    + " | Servicio: " + reservation.getService().getName()
                    + " | Fecha: " + reservation.getReservationDate()
                    + " | Hora: " + reservation.getStartTime();
            case MODIFIED  -> "Reserva modificada. Cliente: " + clientName
                    + " | Cortador: " + carverName
                    + " | Nueva fecha: " + reservation.getReservationDate()
                    + " | Nueva hora: " + reservation.getStartTime();
            case CANCELLED -> "Reserva cancelada. Cliente: " + clientName
                    + " | Cortador: " + carverName
                    + " | Servicio: " + reservation.getService().getName()
                    + " | Fecha: " + reservation.getReservationDate();
            case REMINDER  -> "Recordatorio enviado. Cliente: " + clientName
                    + " | Servicio: " + reservation.getService().getName()
                    + " | Fecha: " + reservation.getReservationDate();
        };
    }

    private NotificationResponseDTO toDTO(Notification n) {
        Long reservationId = n.getReservation() != null ? n.getReservation().getId() : null;
        return new NotificationResponseDTO(
                n.getId(),
                reservationId,
                n.getRecipientType(),
                n.getRecipientEmail(),
                n.getNotificationType(),
                n.getSubject(),
                n.getMessage(),
                n.getIsSent(),
                n.getSentAt()
        );
    }
}