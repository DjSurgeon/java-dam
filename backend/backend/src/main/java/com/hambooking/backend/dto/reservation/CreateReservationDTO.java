package com.hambooking.backend.dto.reservation;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public class CreateReservationDTO {

        @NotNull(message = "El cliente es obligatorio")
        private Long clientId;

        @NotNull(message = "El cortador es obligatorio")
        private Long carverId;

        @NotNull(message = "El servicio es obligatorio")
        private Long serviceId;

        @NotNull(message = "La fecha de reserva es obligatoria")
        @Future(message = "La fecha de reserva debe ser en el futuro")
        private LocalDate reservationDate;

        @NotNull(message = "La hora de inicio es obligatoria")
        private LocalTime startTime;

        // Opcional — el cliente puede añadir notas a su reserva
        private String notes;

        public CreateReservationDTO() {}

        public CreateReservationDTO(Long clientId, Long carverId, Long serviceId,
                                    LocalDate reservationDate, LocalTime startTime, String notes) {
            this.clientId = clientId;
            this.carverId = carverId;
            this.serviceId = serviceId;
            this.reservationDate = reservationDate;
            this.startTime = startTime;
            this.notes = notes;
        }

        public Long getClientId() { return clientId; }
        public void setClientId(Long clientId) { this.clientId = clientId; }

        public Long getCarverId() { return carverId; }
        public void setCarverId(Long carverId) { this.carverId = carverId; }

        public Long getServiceId() { return serviceId; }
        public void setServiceId(Long serviceId) { this.serviceId = serviceId; }

        public LocalDate getReservationDate() { return reservationDate; }
        public void setReservationDate(LocalDate reservationDate) { this.reservationDate = reservationDate; }

        public LocalTime getStartTime() { return startTime; }
        public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }
