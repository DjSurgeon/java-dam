package com.hambooking.backend.repository;

import com.hambooking.backend.model.entity.Reservation;
import com.hambooking.backend.model.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByClient_Id(Long clientId);

    List<Reservation> findByCarver_Id(Long carverId);

    List<Reservation> findByService_Id(Long serviceId);

    List<Reservation> findByReservationDate(LocalDate date);

    List<Reservation> findByStatus(Status status);

    List<Reservation> findByCarver_IdAndReservationDate(Long carverId, LocalDate date);
}