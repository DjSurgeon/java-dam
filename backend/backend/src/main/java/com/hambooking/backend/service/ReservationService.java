package com.hambooking.backend.service;

import com.hambooking.backend.dto.reservation.CreateReservationDTO;
import com.hambooking.backend.dto.reservation.ReservationResponseDTO;
import com.hambooking.backend.dto.reservation.UpdateReservationDTO;
import com.hambooking.backend.exception.BusinessRuleException;
import com.hambooking.backend.exception.ReservationLimitExceededException;
import com.hambooking.backend.exception.ResourceNotFoundException;
import com.hambooking.backend.exception.TimeSlotNotAvailableException;
import com.hambooking.backend.model.entity.Carver;
import com.hambooking.backend.model.entity.Reservation;
import com.hambooking.backend.model.entity.Service;
import com.hambooking.backend.model.entity.User;
import com.hambooking.backend.model.enums.Status;
import com.hambooking.backend.model.enums.NotificationType;
import com.hambooking.backend.repository.CarverRepository;
import com.hambooking.backend.repository.ReservationRepository;
import com.hambooking.backend.repository.ServiceRepository;
import com.hambooking.backend.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
public class ReservationService {

    private static final LocalTime OPENING_TIME = LocalTime.of(10, 0);
    private static final LocalTime CLOSING_TIME = LocalTime.of(18, 0);
    private static final int MAX_DAILY_RESERVATIONS_PER_CLIENT = 2;
    private static final int MAX_DAILY_MINUTES_PER_CARVER = 360; // 6 horas

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final CarverRepository carverRepository;
    private final ServiceRepository serviceRepository;
    private final NotificationService notificationService;

    public ReservationService(ReservationRepository reservationRepository,
                              UserRepository userRepository,
                              CarverRepository carverRepository,
                              ServiceRepository serviceRepository,
                              NotificationService notificationService) {
        this.reservationRepository = reservationRepository;
        this.userRepository = userRepository;
        this.carverRepository = carverRepository;
        this.serviceRepository = serviceRepository;
        this.notificationService = notificationService;
    }

    // ─────────────────────────────────────────
    // CREAR RESERVA
    // ─────────────────────────────────────────
    @Transactional
    public ReservationResponseDTO createReservation(CreateReservationDTO request) {

        // 1. Verificar que existen cliente, cortador y servicio
        User client = userRepository.findById(request.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));

        Carver carver = carverRepository.findById(request.getCarverId())
                .orElseThrow(() -> new ResourceNotFoundException("Cortador no encontrado"));

        Service service = serviceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado"));

        // 2. Verificar que el cortador está activo
        if (!carver.getIsActive()) {
            throw new BusinessRuleException("El cortador seleccionado no está activo");
        }

        // 3. Verificar que es día laborable (lunes a viernes)
        DayOfWeek day = request.getReservationDate().getDayOfWeek();
        if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
            throw new BusinessRuleException("Solo se pueden hacer reservas de lunes a viernes");
        }

        // 4. Verificar horario laboral (10:00 a 18:00)
        LocalTime endTime = request.getStartTime().plusMinutes(service.getDurationMinutes());
        if (request.getStartTime().isBefore(OPENING_TIME) || endTime.isAfter(CLOSING_TIME)) {
            throw new BusinessRuleException(
                    "La reserva debe estar dentro del horario laboral (10:00 - 18:00)"
            );
        }

        // 5. Verificar límite diario del cliente (máx 2 reservas por día)
        int clientDailyReservations = reservationRepository
                .countActiveReservationsByClientAndDate(client, request.getReservationDate());
        if (clientDailyReservations >= MAX_DAILY_RESERVATIONS_PER_CLIENT) {
            throw new ReservationLimitExceededException(
                    "Has alcanzado el límite de " + MAX_DAILY_RESERVATIONS_PER_CLIENT + " reservas para ese día"
            );
        }

        // 6. Verificar límite de horas del cortador (máx 6 horas por día)
        int carverDailyMinutes = reservationRepository
                .sumActiveMinutesByCarverAndDate(carver, request.getReservationDate());
        if (carverDailyMinutes + service.getDurationMinutes() > MAX_DAILY_MINUTES_PER_CARVER) {
            throw new ReservationLimitExceededException(
                    "El cortador ha alcanzado su límite de horas para ese día"
            );
        }

        // 7. Verificar solapamiento de horario del cortador
        List<Reservation> existingReservations = reservationRepository
                .findByCarverAndReservationDateAndStatusIn(
                        carver,
                        request.getReservationDate(),
                        List.of(Status.PENDING, Status.CONFIRMED)
                );

        for (Reservation existing : existingReservations) {
            boolean overlaps = request.getStartTime().isBefore(existing.getEndTime())
                    && endTime.isAfter(existing.getStartTime());
            if (overlaps) {
                throw new TimeSlotNotAvailableException(
                        "El cortador ya tiene una reserva en ese horario"
                );
            }
        }

        // 8. Construir y guardar la reserva
        Reservation reservation = new Reservation();
        reservation.setClient(client);
        reservation.setCarver(carver);
        reservation.setService(service);
        reservation.setReservationDate(request.getReservationDate());
        reservation.setStartTime(request.getStartTime());
        reservation.setNotes(request.getNotes());
        reservation.setStatus(Status.PENDING);
        reservation.calculateEndTime();

        Reservation saved = reservationRepository.save(reservation);
        notificationService.sendReservationNotification(saved, NotificationType.CREATED);
        return toDTO(saved);
    }

    // ─────────────────────────────────────────
    // ACTUALIZAR RESERVA
    // ─────────────────────────────────────────
    @Transactional
    public ReservationResponseDTO updateReservation(Long id, UpdateReservationDTO request) {

        // 1. Verificar que la reserva existe
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada"));

        // 2. Solo se pueden modificar reservas en estado PENDING
        if (reservation.getStatus() != Status.PENDING) {
            throw new BusinessRuleException(
                    "Solo se pueden modificar reservas en estado PENDIENTE"
            );
        }

        // 3. Verificar día laborable
        DayOfWeek day = request.getReservationDate().getDayOfWeek();
        if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
            throw new BusinessRuleException("Solo se pueden hacer reservas de lunes a viernes");
        }

        // 4. Verificar horario laboral
        LocalTime endTime = request.getStartTime()
                .plusMinutes(reservation.getService().getDurationMinutes());
        if (request.getStartTime().isBefore(OPENING_TIME) || endTime.isAfter(CLOSING_TIME)) {
            throw new BusinessRuleException(
                    "La reserva debe estar dentro del horario laboral (10:00 - 18:00)"
            );
        }

        // 5. Verificar solapamiento — ignorando la propia reserva
        List<Reservation> existingReservations = reservationRepository
                .findByCarverAndReservationDateAndStatusIn(
                        reservation.getCarver(),
                        request.getReservationDate(),
                        List.of(Status.PENDING, Status.CONFIRMED)
                );

        for (Reservation existing : existingReservations) {
            if (existing.getId().equals(id)) continue; // ignorar la propia reserva
            boolean overlaps = request.getStartTime().isBefore(existing.getEndTime())
                    && endTime.isAfter(existing.getStartTime());
            if (overlaps) {
                throw new TimeSlotNotAvailableException(
                        "El cortador ya tiene una reserva en ese horario"
                );
            }
        }

        // 6. Aplicar cambios
        reservation.setReservationDate(request.getReservationDate());
        reservation.setStartTime(request.getStartTime());
        reservation.setNotes(request.getNotes());
        reservation.calculateEndTime();

        Reservation updated = reservationRepository.save(reservation);
        notificationService.sendReservationNotification(updated, NotificationType.MODIFIED);
        return toDTO(updated);
    }

    // ─────────────────────────────────────────
    // CANCELAR RESERVA
    // ─────────────────────────────────────────
    @Transactional
    public void cancelReservation(Long id) {

        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada"));

        if (reservation.getStatus() == Status.CANCELLED) {
            throw new BusinessRuleException("La reserva ya está cancelada");
        }

        if (reservation.getStatus() == Status.CONFIRMED) {
            throw new BusinessRuleException(
                    "No se puede cancelar una reserva ya confirmada. Contacta con el administrador"
            );
        }

        reservation.setStatus(Status.CANCELLED);
        Reservation cancelled = reservationRepository.save(reservation);
        notificationService.sendReservationNotification(cancelled, NotificationType.CANCELLED);
    }

    // ─────────────────────────────────────────
    // LISTAR RESERVAS POR CLIENTE
    // ─────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<ReservationResponseDTO> listReservationsByClient(Long clientId) {

        User client = userRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));

        return reservationRepository.findByClient(client)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────
    // MÉTODO PRIVADO — Convertir entidad a DTO
    // ─────────────────────────────────────────
    private ReservationResponseDTO toDTO(Reservation r) {
        return new ReservationResponseDTO(
                r.getId(),
                r.getClient().getId(),
                r.getClient().getFirstName(),
                r.getClient().getLastName(),
                r.getCarver().getId(),
                r.getCarver().getUser().getFirstName(),
                r.getCarver().getUser().getLastName(),
                r.getService().getId(),
                r.getService().getName(),
                r.getService().getDurationMinutes(),
                r.getReservationDate(),
                r.getStartTime(),
                r.getEndTime(),
                r.getStatus(),
                r.getNotes(),
                r.getCreatedAt()
        );
    }
}