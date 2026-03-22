package com.hambooking.backend.service;

import com.hambooking.backend.model.enums.Status;
import com.hambooking.backend.repository.ReservationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Actualiza automáticamente el estado de reservas con fecha pasada.
 *
 * Usa @Modifying + @Query para hacer UPDATE directo en BD,
 * evitando que Hibernate valide la constraint @Future de la entidad.
 *
 * - PENDING  con fecha < hoy → CANCELLED
 * - CONFIRMED con fecha < hoy → COMPLETED
 *
 * Se ejecuta al arrancar (llamado desde BackendApplication) y cada día a las 01:00 AM.
 */
@Service
@EnableScheduling
public class ReservationStatusService {

    private static final Logger logger =
            LoggerFactory.getLogger(ReservationStatusService.class);

    private final ReservationRepository reservationRepository;

    public ReservationStatusService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    // Ejecutar cada día a las 01:00 AM
    @Scheduled(cron = "0 0 1 * * *")
    public void scheduledUpdate() {
        logger.info("[ReservationStatusService] Tarea diaria — actualizando estados...");
        actualizarEstadosPasados();
    }

    @Transactional
    public void actualizarEstadosPasados() {
        LocalDate hoy = LocalDate.now();

        int canceladas = reservationRepository.updateStatusForPastReservations(
                Status.CANCELLED, Status.PENDING, hoy);

        int completadas = reservationRepository.updateStatusForPastReservations(
                Status.COMPLETED, Status.CONFIRMED, hoy);

        logger.info("[ReservationStatusService] {} canceladas, {} completadas.",
                canceladas, completadas);
    }
}