package com.hambooking.backend.service;

import com.hambooking.backend.model.entity.Carver;
import com.hambooking.backend.model.entity.Reservation;
import com.hambooking.backend.model.enums.Status;
import com.hambooking.backend.repository.CarverRepository;
import com.hambooking.backend.repository.ReservationRepository;
import com.hambooking.backend.repository.ServiceRepository;
import com.hambooking.backend.exception.BusinessRuleException;
import com.hambooking.backend.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;

@Service
public class AvailabilityService {

    private static final LocalTime OPENING_TIME = LocalTime.of(10, 0);
    private static final LocalTime CLOSING_TIME = LocalTime.of(18, 0);
    private static final int SLOT_DURATION_MINUTES = 30;

    private final ReservationRepository reservationRepository;
    private final CarverRepository carverRepository;
    private final ServiceRepository serviceRepository;

    public AvailabilityService(ReservationRepository reservationRepository,
                               CarverRepository carverRepository,
                               ServiceRepository serviceRepository) {
        this.reservationRepository = reservationRepository;
        this.carverRepository = carverRepository;
        this.serviceRepository = serviceRepository;
    }

    // ─────────────────────────────────────────
    // PASO 1 — Generar todos los slots del día
    // ─────────────────────────────────────────
    private List<LocalTime> generateAllSlots() {
        List<LocalTime> slots = new ArrayList<>();
        LocalTime current = OPENING_TIME;

        while (current.isBefore(CLOSING_TIME)) {
            slots.add(current);
            current = current.plusMinutes(SLOT_DURATION_MINUTES);
        }

        return slots;
    }

    // ─────────────────────────────────────────
    // PASO 2 — Filtrar slots válidos por duración del servicio
    // ─────────────────────────────────────────
    private List<LocalTime> filterSlotsByDuration(List<LocalTime> allSlots, int durationMinutes) {
        List<LocalTime> validSlots = new ArrayList<>();

        for (LocalTime slot : allSlots) {
            LocalTime slotEnd = slot.plusMinutes(durationMinutes);
            if (!slotEnd.isAfter(CLOSING_TIME)) {
                validSlots.add(slot);
            }
        }

        return validSlots;
    }

    // ─────────────────────────────────────────
    // PASO 3 — Consultar reservas activas del cortador ese día
    // ─────────────────────────────────────────
    private List<Reservation> getActiveReservations(Carver carver, LocalDate date) {
        return reservationRepository
                .findByCarverAndReservationDateAndStatusIn(
                        carver,
                        date,
                        List.of(Status.PENDING, Status.CONFIRMED)
                );
    }
    // ─────────────────────────────────────────
    // PASO 4 — Eliminar slots que se solapen con reservas existentes
    // ─────────────────────────────────────────
    private List<LocalTime> removeOccupiedSlots(List<LocalTime> validSlots,
                                                List<Reservation> activeReservations,
                                                int durationMinutes) {
        List<LocalTime> availableSlots = new ArrayList<>();
        for (LocalTime slot : validSlots) {
            LocalTime slotEnd = slot.plusMinutes(durationMinutes);
            boolean isOccupied = false;
            for (Reservation reservation : activeReservations) {
                boolean overlaps = slot.isBefore(reservation.getEndTime())
                        && slotEnd.isAfter(reservation.getStartTime());
                if (overlaps) {
                    isOccupied = true;
                    break;
                }
            }
            if (!isOccupied) {
                availableSlots.add(slot);
            }
        }
        return availableSlots;
    }
    // ─────────────────────────────────────────
    // PASO 5 — Método principal que une todo
    // ─────────────────────────────────────────
    public List<LocalTime> getAvailableSlots(Long carverId, LocalDate date, Long serviceId) {
        // 1. Verificar que existen cortador y servicio
        Carver carver = carverRepository.findById(carverId)
                .orElseThrow(() -> new ResourceNotFoundException("Cortador no encontrado"));

        com.hambooking.backend.model.entity.Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado"));
        // 2. Verificar que el cortador está activo
        if (!carver.getIsActive()) {
            throw new BusinessRuleException("El cortador seleccionado no está activo");
        }
        // 3. Verificar que es día laborable
        DayOfWeek day = date.getDayOfWeek();
        if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
            throw new BusinessRuleException("Solo se puede consultar disponibilidad de lunes a viernes");
        }
        // 4. Ejecutar los 4 pasos en orden
        List<LocalTime> allSlots = generateAllSlots();
        List<LocalTime> validSlots = filterSlotsByDuration(allSlots, service.getDurationMinutes());
        List<Reservation> activeReservations = getActiveReservations(carver, date);
        List<LocalTime> availableSlots = removeOccupiedSlots(validSlots, activeReservations, service.getDurationMinutes());
        return availableSlots;
    }
}