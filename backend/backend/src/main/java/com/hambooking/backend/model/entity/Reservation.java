package com.hambooking.backend.model.entity;

import com.hambooking.backend.model.enums.Status;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad JPA que representa una reserva en el sistema.
 * Mapea a la tabla 'reservations' en MySQL.
 * Actúa como tabla pivote con información extra conectando User, Carver y Service.
 */
@Entity
@Table(name = "reservations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // =================================================================================
    // RELACIONES (MANY-TO-ONE) - EL CORAZÓN DE LA RESERVA
    // =================================================================================

    // 1. ¿QUIÉN RESERVA? (client_id)
    @NotNull(message = "El cliente es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    // 2. ¿QUIÉN CORTA? (carver_id)
    @NotNull(message = "El cortador es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "carver_id", nullable = false)
    private Carver carver;

    // 3. ¿QUÉ SE CORTA? (service_id)
    @NotNull(message = "El servicio es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    private Service service;

    // =================================================================================
    // DATOS DE LA RESERVA (FECHAS Y HORAS)
    // =================================================================================

    // reservation_date DATE NOT NULL
    @NotNull(message = "La fecha de reserva es obligatoria")
    @Future(message = "La fecha de reserva debe ser en el futuro")
    @Column(name = "reservation_date", nullable = false)
    private LocalDate reservationDate;

    // start_time TIME NOT NULL
    @NotNull(message = "La hora de inicio es obligatoria")
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    // end_time TIME NOT NULL
    @NotNull(message = "La hora de fin es obligatoria")
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    // =================================================================================
    // ESTADO Y METADATOS
    // =================================================================================

    // status ENUM NOT NULL DEFAULT 'PENDING'
    @NotNull(message = "El estado es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Status status = Status.PENDING;

    // notes TEXT
    @Column(columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // =================================================================================
    // RELACIONES INVERSAS (NOTIFICACIONES)
    // =================================================================================
    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Notification> notifications = new ArrayList<>();

    // =================================================================================
    // LÓGICA DE NEGOCIO (MÉTODOS HELPER)
    // =================================================================================

    /**
     * Calcula automáticamente la hora de fin de la reserva sumando
     * la duración del servicio a la hora de inicio.
     */
    public void calculateEndTime() {
        if (this.startTime != null && this.service != null && this.service.getDurationMinutes() != null) {
            this.endTime = this.startTime.plusMinutes(this.service.getDurationMinutes());
        }
    }

    public void addNotification(Notification notification) {
        notifications.add(notification);
        notification.setReservation(this);
    }

    public void removeNotification(Notification notification) {
        notifications.remove(notification);
        notification.setReservation(null);
    }

    // =================================================================================
    // EQUALS, HASHCODE Y TOSTRING SEGUROS
    // =================================================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Reservation)) return false;
        Reservation that = (Reservation) o;
        return id != null && id.equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        // Sacamos los IDs de forma segura para no cargar los objetos enteros de BD (LazyInitializationException)
        Long clientId = (client != null) ? client.getId() : null;
        Long carverId = (carver != null) ? carver.getId() : null;
        Long serviceId = (service != null) ? service.getId() : null;

        return "Reservation{" +
                "id=" + id +
                ", clientId=" + clientId +
                ", carverId=" + carverId +
                ", serviceId=" + serviceId +
                ", reservationDate=" + reservationDate +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", status=" + status +
                '}';
    }
}