package com.hambooking.backend.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad JPA que representa el perfil profesional de un cortador.
 * Mapea a la tabla 'carvers' en MySQL.
 * Es dueña de la relación OneToOne con User.
 */
@Entity
@Table(name = "carvers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Carver {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // =================================================================================
    // RELACIÓN PRINCIPAL (OWNER)
    // =================================================================================
    // El cortador es el dueño de la relación (tiene la FK user_id en BD)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    // =================================================================================
    // DATOS PROFESIONALES
    // =================================================================================
    @Size(max = 100, message = "La especialidad no puede exceder de los 100 caracteres")
    @Column(length = 100)
    private String specialty;

    @Min(value = 0, message = "Los años de experiencia no pueden ser negativos")
    @Column(name = "experience_years")
    @Builder.Default
    private Integer experienceYears = 0;

    @Min(value = 1, message = "Debe permitir al menos 1 servicio por día")
    @Max(value = 10, message = "El límite máximo de servicios por día es 10")
    @Column(name = "max_hams_per_day")
    @Builder.Default
    private Integer maxHamsPerDay = 3;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // =================================================================================
    // RELACIONES (1:N)
    // =================================================================================
    // Un cortador puede atender muchas reservas.
    @OneToMany(mappedBy = "carver", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private List<Reservation> reservations = new ArrayList<>();

    // =================================================================================
    // MÉTODOS DE UTILIDAD
    // =================================================================================
    public void addReservation(Reservation reservation) {
        reservations.add(reservation);
        reservation.setCarver(this);
    }

    public void removeReservation(Reservation reservation) {
        reservations.remove(reservation);
        reservation.setCarver(null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Carver)) return false;
        Carver carver = (Carver) o;
        return id != null && id.equals(carver.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        // Obtenemos el ID del usuario de forma segura para no provocar un volcado de toda la entidad User
        Long userId = (user != null) ? user.getId() : null;

        return "Carver{" +
                "id=" + id +
                ", userId=" + userId +
                ", specialty='" + specialty + '\'' +
                ", experienceYears=" + experienceYears +
                ", maxHamsPerDay=" + maxHamsPerDay +
                ", isActive=" + isActive +
                '}';
    }
}