package com.hambooking.backend.repository;

import com.hambooking.backend.model.entity.Carver;
import com.hambooking.backend.model.entity.Reservation;
import com.hambooking.backend.model.entity.User;
import com.hambooking.backend.model.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByClient(User client);

    List<Reservation> findByClientAndStatus(User client, Status status);

    List<Reservation> findByCarver(Carver carver);

    List<Reservation> findByCarverAndReservationDateAndStatusIn(
            Carver carver, LocalDate reservationDate, List<Status> statuses);

    List<Reservation> findByReservationDateAndStatus(LocalDate reservationDate, Status status);

    List<Reservation> findByReservationDateBetweenAndStatus(
            LocalDate startDate, LocalDate endDate, Status status);

    @Query("SELECT COUNT(r) FROM Reservation r " +
            "WHERE r.client = :client " +
            "AND r.reservationDate = :date " +
            "AND r.status IN ('PENDING', 'CONFIRMED')")
    int countActiveReservationsByClientAndDate(
            @Param("client") User client,
            @Param("date") LocalDate date);

    @Query("SELECT COALESCE(SUM(s.durationMinutes), 0) FROM Reservation r " +
            "JOIN r.service s " +
            "WHERE r.carver = :carver " +
            "AND r.reservationDate = :date " +
            "AND r.status IN ('PENDING', 'CONFIRMED')")
    int sumActiveMinutesByCarverAndDate(
            @Param("carver") Carver carver,
            @Param("date") LocalDate date);

    // ── UPDATE DIRECTO — evita validación @Future de la entidad ──────────
    // Usado por ReservationStatusService para actualizar reservas pasadas
    // sin que Hibernate valide la constraint @Future en reservationDate.
    @Modifying
    @Query("UPDATE Reservation r SET r.status = :newStatus " +
            "WHERE r.reservationDate < :fecha AND r.status = :currentStatus")
    int updateStatusForPastReservations(
            @Param("newStatus") Status newStatus,
            @Param("currentStatus") Status currentStatus,
            @Param("fecha") LocalDate fecha);
}