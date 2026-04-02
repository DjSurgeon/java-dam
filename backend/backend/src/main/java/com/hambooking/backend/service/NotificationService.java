package com.hambooking.backend.service;

import com.hambooking.backend.dto.notification.NotificationResponseDTO;
import com.hambooking.backend.exception.ResourceNotFoundException;
import com.hambooking.backend.model.entity.Notification;
import com.hambooking.backend.model.entity.Reservation;
import com.hambooking.backend.model.entity.User;
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
    // Genera 3 notificaciones por evento
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

        String adminEmail = userRepository.findByRole(Role.ADMIN).stream()
                .findFirst().map(User::getEmail).orElse("admin@hambooking.com");

        sendSingle(reservation, type, RecipientType.ADMIN,
                adminEmail, subject, buildAdminMessage(type, reservation));
    }

    // ─────────────────────────────────────────
    // LISTAR TODAS (admin)
    // ─────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<NotificationResponseDTO> listAllNotifications() {
        return notificationRepository.findAll().stream()
                .map(this::toDTO).collect(Collectors.toList());
    }

    // ─────────────────────────────────────────
    // LISTAR POR USUARIO (cliente)  ← NUEVO
    // ─────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<NotificationResponseDTO> listByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        return notificationRepository.findByRecipientEmail(user.getEmail())
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    // ─────────────────────────────────────────
    // Historial por reserva
    // ─────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<NotificationResponseDTO> getNotificationsByReservation(Reservation reservation) {
        return notificationRepository.findByReservation(reservation)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    // ── Privados ─────────────────────────────
    private void sendSingle(Reservation reservation, NotificationType type,
                            RecipientType recipientType, String email,
                            String subject, String message) {
        Notification n = Notification.builder()
                .reservation(reservation)
                .notificationType(type)
                .recipientType(recipientType)
                .recipientEmail(email)
                .subject(subject)
                .message(message)
                .isSent(true)
                .build();
        notificationRepository.save(n);
        logger.info("[NOTIFICATION] {} → {} | {} | {}", type, recipientType, email, subject);
    }

    private String buildSubject(NotificationType type, Reservation r) {
        return switch (type) {
            case CREATED   -> "Nueva reserva confirmada - " + r.getService().getName();
            case MODIFIED  -> "Reserva modificada - " + r.getService().getName();
            case CANCELLED -> "Reserva cancelada - " + r.getService().getName();
            case REMINDER  -> "Recordatorio de reserva - " + r.getService().getName();
        };
    }

    private String buildClientMessage(NotificationType type, Reservation r) {
        String base = "Hola " + r.getClient().getFirstName() + ",\n\n";
        return base + switch (type) {
            case CREATED   -> "Tu reserva de " + r.getService().getName()
                    + " ha sido creada para el " + r.getReservationDate()
                    + " a las " + r.getStartTime() + ".";
            case MODIFIED  -> "Tu reserva de " + r.getService().getName()
                    + " ha sido modificada. Nueva fecha: " + r.getReservationDate()
                    + " a las " + r.getStartTime() + ".";
            case CANCELLED -> "Tu reserva de " + r.getService().getName()
                    + " del " + r.getReservationDate() + " ha sido cancelada.";
            case REMINDER  -> "Recuerda tu reserva de " + r.getService().getName()
                    + " mañana " + r.getReservationDate() + " a las " + r.getStartTime() + ".";
        };
    }

    private String buildCarverMessage(NotificationType type, Reservation r) {
        String base = "Hola " + r.getCarver().getUser().getFirstName() + ",\n\n";
        return base + switch (type) {
            case CREATED   -> "Nueva reserva de " + r.getService().getName()
                    + " el " + r.getReservationDate() + " a las " + r.getStartTime() + ".";
            case MODIFIED  -> "Reserva modificada. Nueva fecha: " + r.getReservationDate()
                    + " a las " + r.getStartTime() + ".";
            case CANCELLED -> "Reserva de " + r.getService().getName()
                    + " del " + r.getReservationDate() + " cancelada.";
            case REMINDER  -> "Recuerda tu reserva de " + r.getService().getName()
                    + " mañana " + r.getReservationDate() + " a las " + r.getStartTime() + ".";
        };
    }

    private String buildAdminMessage(NotificationType type, Reservation r) {
        String client = r.getClient().getFirstName() + " " + r.getClient().getLastName();
        String carver = r.getCarver().getUser().getFirstName() + " " + r.getCarver().getUser().getLastName();
        return switch (type) {
            case CREATED   -> "Nueva reserva. Cliente: " + client + " | Cortador: " + carver
                    + " | " + r.getService().getName() + " | " + r.getReservationDate() + " " + r.getStartTime();
            case MODIFIED  -> "Reserva modificada. Cliente: " + client
                    + " | Nueva fecha: " + r.getReservationDate() + " " + r.getStartTime();
            case CANCELLED -> "Reserva cancelada. Cliente: " + client + " | Cortador: " + carver
                    + " | " + r.getService().getName() + " | " + r.getReservationDate();
            case REMINDER  -> "Recordatorio. Cliente: " + client
                    + " | " + r.getService().getName() + " | " + r.getReservationDate();
        };
    }

    private NotificationResponseDTO toDTO(Notification n) {
        return new NotificationResponseDTO(
                n.getId(),
                n.getReservation() != null ? n.getReservation().getId() : null,
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