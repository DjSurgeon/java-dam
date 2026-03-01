package com.hambooking.backend.repository;

import com.hambooking.backend.model.entity.Carver;
import com.hambooking.backend.model.entity.Reservation;
import com.hambooking.backend.model.entity.User;
import com.hambooking.backend.model.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository para la entidad {@link Reservation}.
 *
 * Es el repository más complejo del dominio porque Reservation conecta
 * tres entidades (User, Carver, Service) y contiene la lógica de negocio
 * más crítica: evitar solapamientos de horario y respetar los límites
 * de reservas por cliente y por cortador.
 *
 * Tipos de métodos usados aquí:
 *   1. Query methods derivados → Spring genera la SQL por el nombre del método
 *   2. @Query con JPQL        → para lógica que no se puede expresar con nombres
 *
 * Métodos heredados de JpaRepository (gratis):
 *   save(), findById(), findAll(), deleteById(), count(), existsById()
 */
@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    // =========================================================================
    // BÚSQUEDAS POR CLIENTE
    // =========================================================================

    /**
     * Devuelve todas las reservas de un cliente.
     *
     * SQL generada: SELECT * FROM reservations WHERE client_id = ?
     *
     * Uso: Historial completo de reservas en el dashboard del cliente.
     *
     * @param client El usuario cliente
     * @return Lista de todas sus reservas
     */
    List<Reservation> findByClient(User client);

    /**
     * Devuelve las reservas de un cliente filtradas por estado.
     *
     * SQL generada: SELECT * FROM reservations WHERE client_id = ? AND status = ?
     *
     * Uso: Mostrar solo las reservas PENDING o CONFIRMED en el dashboard
     * (las próximas), sin incluir historial de COMPLETED o CANCELLED.
     *
     * @param client El usuario cliente
     * @param status Estado a filtrar
     * @return Lista de reservas del cliente con ese estado
     */
    List<Reservation> findByClientAndStatus(User client, Status status);

    // =========================================================================
    // BÚSQUEDAS POR CORTADOR
    // =========================================================================

    /**
     * Devuelve todas las reservas asignadas a un cortador.
     *
     * SQL generada: SELECT * FROM reservations WHERE carver_id = ?
     *
     * Uso: Agenda completa de un cortador en el panel de administración.
     *
     * @param carver El cortador
     * @return Lista de todas las reservas del cortador
     */
    List<Reservation> findByCarver(Carver carver);

    /**
     * Busca reservas de un cortador en una fecha concreta con estados activos.
     *
     * SQL generada:
     *   SELECT * FROM reservations
     *   WHERE carver_id = ?
     *     AND reservation_date = ?
     *     AND status IN (?, ?, ...)
     *
     * Uso: CRÍTICO para detectar solapamientos de horario.
     * Antes de crear una reserva, el ReservationService llama a este método
     * para obtener los bloques ya ocupados del cortador ese día, y verifica
     * que el nuevo slot no se solape con ninguno de ellos.
     *
     * Solo se pasan los estados activos (PENDING, CONFIRMED) porque
     * las reservas COMPLETED o CANCELLED ya no bloquean el horario.
     *
     * @param carver          El cortador cuya agenda se consulta
     * @param reservationDate Fecha del día a comprobar
     * @param statuses        Lista de estados que se consideran "ocupado"
     * @return Lista de reservas activas del cortador en esa fecha
     */
    List<Reservation> findByCarverAndReservationDateAndStatusIn(
            Carver carver,
            LocalDate reservationDate,
            List<Status> statuses
    );

    // =========================================================================
    // BÚSQUEDAS POR FECHA Y ESTADO
    // =========================================================================

    /**
     * Devuelve todas las reservas de una fecha concreta con un estado dado.
     *
     * SQL generada:
     *   SELECT * FROM reservations WHERE reservation_date = ? AND status = ?
     *
     * Uso: Vista diaria del admin — ver todas las reservas de hoy confirmadas,
     * o las pendientes de confirmar.
     *
     * @param reservationDate Fecha a consultar
     * @param status          Estado a filtrar
     * @return Lista de reservas en esa fecha con ese estado
     */
    List<Reservation> findByReservationDateAndStatus(LocalDate reservationDate, Status status);

    /**
     * Devuelve todas las reservas entre dos fechas (inclusive) con un estado.
     *
     * SQL generada:
     *   SELECT * FROM reservations
     *   WHERE reservation_date BETWEEN ? AND ?
     *     AND status = ?
     *
     * Uso: Vista semanal del admin, informes de periodo.
     *
     * @param startDate Fecha de inicio del rango (inclusive)
     * @param endDate   Fecha de fin del rango (inclusive)
     * @param status    Estado a filtrar
     * @return Lista de reservas en el rango con ese estado
     */
    List<Reservation> findByReservationDateBetweenAndStatus(
            LocalDate startDate,
            LocalDate endDate,
            Status status
    );

    // =========================================================================
    // QUERIES PERSONALIZADAS CON @Query (JPQL)
    // Usamos @Query cuando la lógica es demasiado compleja para expresarla
    // solo con el nombre del método.
    // JPQL usa nombres de clases y campos Java, no nombres de tabla SQL.
    // =========================================================================

    /**
     * Cuenta las reservas activas de un cliente en una fecha concreta.
     *
     * JPQL:
     *   SELECT COUNT(r) FROM Reservation r
     *   WHERE r.client = :client
     *     AND r.reservationDate = :date
     *     AND r.status IN ('PENDING', 'CONFIRMED')
     *
     * Uso: CRÍTICO para respetar el límite de negocio de 2 reservas por
     * cliente por día. El ReservationService llama a este método antes
     * de crear una nueva reserva y lanza excepción si el resultado >= 2.
     *
     * ¿Por qué @Query y no query method derivado?
     * Porque el filtro IN con lista de enums fija es más legible y
     * mantenible escrito en JPQL que como nombre de método.
     *
     * @param client El cliente a consultar
     * @param date   Fecha del día
     * @return Número de reservas activas del cliente ese día (0, 1 o 2)
     */
    @Query("SELECT COUNT(r) FROM Reservation r " +
            "WHERE r.client = :client " +
            "AND r.reservationDate = :date " +
            "AND r.status IN ('PENDING', 'CONFIRMED')")
    int countActiveReservationsByClientAndDate(
            @Param("client") User client,
            @Param("date") LocalDate date
    );

    /**
     * Suma los minutos de trabajo activos de un cortador en un día concreto.
     *
     * JPQL:
     *   SELECT COALESCE(SUM(s.durationMinutes), 0) FROM Reservation r
     *   JOIN r.service s
     *   WHERE r.carver = :carver
     *     AND r.reservationDate = :date
     *     AND r.status IN ('PENDING', 'CONFIRMED')
     *
     * Uso: Validar que el cortador no supere las 6 horas (360 min)
     * de trabajo efectivo en un día. Antes de aceptar una nueva reserva,
     * el ReservationService suma los minutos ya comprometidos y verifica
     * que la nueva reserva no haga superar el límite.
     *
     * COALESCE(..., 0) garantiza que devuelve 0 si no hay reservas
     * en lugar de null, evitando NullPointerExceptions en el Service.
     *
     * @param carver El cortador
     * @param date   Fecha del día
     * @return Total de minutos de trabajo activos ese día (mínimo 0)
     */
    @Query("SELECT COALESCE(SUM(s.durationMinutes), 0) FROM Reservation r " +
            "JOIN r.service s " +
            "WHERE r.carver = :carver " +
            "AND r.reservationDate = :date " +
            "AND r.status IN ('PENDING', 'CONFIRMED')")
    int sumActiveMinutesByCarverAndDate(
            @Param("carver") Carver carver,
            @Param("date") LocalDate date
    );
}