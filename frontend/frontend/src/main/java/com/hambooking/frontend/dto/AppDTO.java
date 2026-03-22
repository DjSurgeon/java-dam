package com.hambooking.frontend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class AppDTO {

    // ── Carver ───────────────────────────────────────────────────────────
    public static class CarverResponse {
        public Long    id;
        public Long    userId;
        public String  firstName;
        public String  lastName;
        public String  dni;
        public String  email;
        public String  phone;
        public String  specialty;
        public Integer experienceYears;
        public Integer maxHamsPerDay;
        public Boolean isActive;

        public CarverResponse() {}

        public String getDisplayName() {
            if (firstName != null && lastName != null) {
                return firstName + " " + lastName;
            }
            return specialty != null ? specialty : "Cortador #" + id;
        }
    }

    // ── User ─────────────────────────────────────────────────────────────
    public static class UserResponse {
        public Long    id;
        public String  dni;
        public String  firstName;
        public String  lastName;
        public String  email;
        public String  phone;
        public String  role;
        public Boolean isActive;

        public UserResponse() {}

        public String getFullName() {
            return firstName + " " + lastName;
        }
    }

    // ── Service ──────────────────────────────────────────────────────────
    public static class ServiceResponse {
        public Long       id;
        public String     name;
        public String     description;
        public Integer    durationMinutes;
        public BigDecimal basePrice;
        public Boolean    isActive;

        public ServiceResponse() {}

        public String getDisplayName() {
            int h   = durationMinutes / 60;
            int min = durationMinutes % 60;
            String durStr = h > 0
                    ? (h + "h" + (min > 0 ? min + "min" : ""))
                    : (min + "min");
            return name + " (" + durStr + ") - " + basePrice + " EUR";
        }

        public String getPrecioStr() {
            return basePrice != null ? basePrice.toPlainString() + " EUR" : "";
        }
    }

    // ── Reservation: crear ───────────────────────────────────────────────
    public static class CreateReservationRequest {
        public Long      clientId;
        public Long      carverId;
        public Long      serviceId;
        public LocalDate reservationDate;
        public LocalTime startTime;
        public String    notes;

        public CreateReservationRequest() {}

        public CreateReservationRequest(Long clientId, Long carverId, Long serviceId,
                                        LocalDate reservationDate, LocalTime startTime,
                                        String notes) {
            this.clientId        = clientId;
            this.carverId        = carverId;
            this.serviceId       = serviceId;
            this.reservationDate = reservationDate;
            this.startTime       = startTime;
            this.notes           = notes;
        }
    }

    // ── Reservation: respuesta ───────────────────────────────────────────
    public static class ReservationResponse {
        public Long          id;
        public Long          clientId;
        public String        clientFirstName;
        public String        clientLastName;
        public Long          carverId;
        public String        carverFirstName;
        public String        carverLastName;
        public Long          serviceId;
        public String        serviceName;
        public Integer       serviceDurationMinutes;
        public LocalDate     reservationDate;
        public LocalTime     startTime;
        public LocalTime     endTime;
        public String        status;
        public String        notes;
        public LocalDateTime createdAt;

        public ReservationResponse() {}

        public String getCarverFullName() {
            return carverFirstName + " " + carverLastName;
        }

        public String getClientFullName() {
            return clientFirstName + " " + clientLastName;
        }

        public String getHoraStr() {
            return startTime + " - " + endTime;
        }

        public String getFechaStr() {
            return reservationDate != null ? reservationDate.toString() : "";
        }
    }

    // ── Reservation: actualizar ──────────────────────────────────────────
    public static class UpdateReservationRequest {
        public LocalDate reservationDate;
        public LocalTime startTime;
        public String    notes;

        public UpdateReservationRequest() {}

        public UpdateReservationRequest(LocalDate reservationDate,
                                        LocalTime startTime, String notes) {
            this.reservationDate = reservationDate;
            this.startTime       = startTime;
            this.notes           = notes;
        }
    }

    // ── Notification ─────────────────────────────────────────────────────
    public static class NotificationResponse {
        public Long          id;
        public Long          reservationId;
        public String        recipientType;
        public String        recipientEmail;
        public String        notificationType;
        public String        subject;
        public String        message;
        public Boolean       isSent;
        public LocalDateTime sentAt;

        public NotificationResponse() {}
    }
}