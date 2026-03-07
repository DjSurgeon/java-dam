package com.hambooking.backend.dto.reservation;

import com.hambooking.backend.model.enums.Status;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

public class ReservationResponseDTO {

    private Long id;

    // Datos del cliente
    private Long clientId;
    private String clientFirstName;
    private String clientLastName;

    // Datos del cortador
    private Long carverId;
    private String carverFirstName;
    private String carverLastName;

    // Datos del servicio
    private Long serviceId;
    private String serviceName;
    private Integer serviceDurationMinutes;

    // Datos de la reserva
    private LocalDate reservationDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Status status;
    private String notes;
    private LocalDateTime createdAt;

    public ReservationResponseDTO() {}

    public ReservationResponseDTO(Long id,
                                  Long clientId, String clientFirstName, String clientLastName,
                                  Long carverId, String carverFirstName, String carverLastName,
                                  Long serviceId, String serviceName, Integer serviceDurationMinutes,
                                  LocalDate reservationDate, LocalTime startTime, LocalTime endTime,
                                  Status status, String notes, LocalDateTime createdAt) {
        this.id = id;
        this.clientId = clientId;
        this.clientFirstName = clientFirstName;
        this.clientLastName = clientLastName;
        this.carverId = carverId;
        this.carverFirstName = carverFirstName;
        this.carverLastName = carverLastName;
        this.serviceId = serviceId;
        this.serviceName = serviceName;
        this.serviceDurationMinutes = serviceDurationMinutes;
        this.reservationDate = reservationDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.notes = notes;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }

    public String getClientFirstName() { return clientFirstName; }
    public void setClientFirstName(String clientFirstName) { this.clientFirstName = clientFirstName; }

    public String getClientLastName() { return clientLastName; }
    public void setClientLastName(String clientLastName) { this.clientLastName = clientLastName; }

    public Long getCarverId() { return carverId; }
    public void setCarverId(Long carverId) { this.carverId = carverId; }

    public String getCarverFirstName() { return carverFirstName; }
    public void setCarverFirstName(String carverFirstName) { this.carverFirstName = carverFirstName; }

    public String getCarverLastName() { return carverLastName; }
    public void setCarverLastName(String carverLastName) { this.carverLastName = carverLastName; }

    public Long getServiceId() { return serviceId; }
    public void setServiceId(Long serviceId) { this.serviceId = serviceId; }

    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    public Integer getServiceDurationMinutes() { return serviceDurationMinutes; }
    public void setServiceDurationMinutes(Integer serviceDurationMinutes) { this.serviceDurationMinutes = serviceDurationMinutes; }

    public LocalDate getReservationDate() { return reservationDate; }
    public void setReservationDate(LocalDate reservationDate) { this.reservationDate = reservationDate; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}