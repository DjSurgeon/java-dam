package com.hambooking.backend.dto.reservation;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public class UpdateReservationDTO {

    @NotNull(message = "La fecha de reserva es obligatoria")
    @Future(message = "La fecha de reserva debe ser en el futuro")
    private LocalDate reservationDate;

    @NotNull(message = "La hora de inicio es obligatoria")
    private LocalTime startTime;

    // Opcional — el cliente puede modificar o borrar sus notas
    private String notes;

    public UpdateReservationDTO() {}

    public UpdateReservationDTO(LocalDate reservationDate, LocalTime startTime, String notes) {
        this.reservationDate = reservationDate;
        this.startTime = startTime;
        this.notes = notes;
    }

    public LocalDate getReservationDate() { return reservationDate; }
    public void setReservationDate(LocalDate reservationDate) { this.reservationDate = reservationDate; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}