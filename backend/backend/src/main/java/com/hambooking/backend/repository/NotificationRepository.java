package com.hambooking.backend.repository;

import com.hambooking.backend.model.entity.Notification;
import com.hambooking.backend.model.entity.Reservation;
import com.hambooking.backend.model.enums.NotificationType;
import com.hambooking.backend.model.enums.RecipientType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository para la entidad {@link Notification}.
 *
 * Notification es la entidad de auditoría del sistema: registra cada
 * email enviado (o simulado) con su destinatario, tipo y contenido.
 * Su relación con Reservation es nullable — puede haber notificaciones
 * genéricas del sistema sin reserva asociada.
 *
 * El rol de este repository es principalmente de CONSULTA (lectura),
 * ya que las notificaciones se crean una vez y no se modifican.
 * Las queries están orientadas a dos casos de uso:
 *   1. Historial por reserva (¿qué emails se enviaron para esta reserva?)
 *   2. Historial por destinatario (¿qué emails recibió este email/tipo?)
 *
 * Métodos heredados de JpaRepository (gratis):
 *   save(), findById(), findAll(), deleteById(), count(), existsById()
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // =========================================================================
    // BÚSQUEDAS POR RESERVA
    // =========================================================================

    /**
     * Devuelve todas las notificaciones asociadas a una reserva concreta.
     *
     * SQL generada: SELECT * FROM notifications WHERE reservation_id = ?
     *
     * Uso: Auditoría — ver el historial completo de emails enviados
     * para una reserva (creación, confirmación, cancelación, recordatorio).
     * Una reserva puede generar hasta 3 notificaciones por evento
     * (una para CLIENT, otra para CARVER, otra para ADMIN).
     *
     * @param reservation La reserva cuyo historial se consulta
     * @return Lista de notificaciones de esa reserva (puede estar vacía)
     */
    List<Notification> findByReservation(Reservation reservation);

    /**
     * Devuelve las notificaciones de una reserva filtradas por tipo.
     *
     * SQL generada:
     *   SELECT * FROM notifications
     *   WHERE reservation_id = ? AND notification_type = ?
     *
     * Uso: Ver solo los emails de un tipo concreto para una reserva
     * (ej: todos los recordatorios enviados, o solo las cancelaciones).
     *
     * @param reservation      La reserva
     * @param notificationType Tipo de notificación a filtrar
     * @return Lista de notificaciones de ese tipo para esa reserva
     */
    List<Notification> findByReservationAndNotificationType(
            Reservation reservation,
            NotificationType notificationType
    );

    // =========================================================================
    // BÚSQUEDAS POR DESTINATARIO
    // =========================================================================

    /**
     * Devuelve todas las notificaciones enviadas a un email concreto.
     *
     * SQL generada: SELECT * FROM notifications WHERE recipient_email = ?
     *
     * Uso: Historial de emails recibidos por un usuario específico,
     * útil para depuración o soporte al cliente.
     *
     * @param recipientEmail Email del destinatario
     * @return Lista de notificaciones enviadas a ese email
     */
    List<Notification> findByRecipientEmail(String recipientEmail);

    /**
     * Devuelve las notificaciones enviadas a un tipo de destinatario concreto.
     *
     * SQL generada: SELECT * FROM notifications WHERE recipient_type = ?
     *
     * Uso: Ver todas las notificaciones enviadas a clientes, o a cortadores,
     * o al administrador — útil para informes de comunicación.
     *
     * @param recipientType Tipo de destinatario (CLIENT, CARVER, ADMIN)
     * @return Lista de notificaciones para ese tipo de destinatario
     */
    List<Notification> findByRecipientType(RecipientType recipientType);

    // =========================================================================
    // BÚSQUEDAS POR TIPO DE NOTIFICACIÓN
    // =========================================================================

    /**
     * Devuelve todas las notificaciones de un tipo concreto.
     *
     * SQL generada: SELECT * FROM notifications WHERE notification_type = ?
     *
     * Uso: Informes globales — cuántas notificaciones de cancelación
     * se han enviado, cuántos recordatorios, etc.
     *
     * @param notificationType Tipo de notificación
     * @return Lista de notificaciones de ese tipo
     */
    List<Notification> findByNotificationType(NotificationType notificationType);

    // =========================================================================
    // QUERY PERSONALIZADA CON @Query
    // =========================================================================

    /**
     * Cuenta las notificaciones enviadas para una reserva concreta.
     *
     * JPQL:
     *   SELECT COUNT(n) FROM Notification n
     *   WHERE n.reservation = :reservation
     *
     * Uso: Verificar que tras crear/modificar/cancelar una reserva
     * se han generado exactamente las notificaciones esperadas
     * (normalmente 3: CLIENT + CARVER + ADMIN).
     * Útil en tests de integración del NotificationService.
     *
     * ¿Por qué @Query y no query method derivado?
     * Podría escribirse como countByReservation(Reservation) y
     * Spring lo resolvería igual. Usamos @Query aquí para ilustrar
     * que ambas formas son válidas y dejar el JPQL explícito y
     * documentado para la memoria del TFG.
     *
     * @param reservation La reserva a consultar
     * @return Número de notificaciones generadas para esa reserva
     */
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.reservation = :reservation")
    long countByReservation(@Param("reservation") Reservation reservation);
}