package com.hambooking.backend.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad JPA que representa el catálogo de servicios de corte disponibles.
 * Mapea a la tabla 'services' en MySQL.
 */
@Entity
@Table(name = "services")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Service {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // name VARCHAR(100) NOT NULL + UNIQUE
    @NotBlank(message = "El nombre del servicio es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder los 100 caracteres")
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    // description TEXT
    @Size(max = 1000, message = "La descripción es demasiado larga")
    @Column(columnDefinition = "TEXT")
    private String description;

    // duration_minutes INT UNSIGNED NOT NULL + CHECK > 0
    @NotNull(message = "La duración es obligatoria")
    @Positive(message = "La duración en minutos debe ser mayor a 0")
    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    // base_price DECIMAL(10,2) NOT NULL + CHECK >= 0
    @NotNull(message = "El precio base es obligatorio")
    @DecimalMin(value = "0.0", inclusive = true, message = "El precio base no puede ser negativo")
    @Column(name = "base_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice;

    // is_active BOOLEAN NOT NULL DEFAULT TRUE
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    // =================================================================================
    // RELACIONES (1:N)
    // =================================================================================
    // Un servicio puede estar incluido en muchas reservas.
    @OneToMany(mappedBy = "service", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private List<Reservation> reservations = new ArrayList<>();

    // =================================================================================
    // MÉTODOS DE UTILIDAD
    // =================================================================================
    public void addReservation(Reservation reservation) {
        reservations.add(reservation);
        reservation.setService(this);
    }

    public void removeReservation(Reservation reservation) {
        reservations.remove(reservation);
        reservation.setService(null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Service)) return false;
        Service service = (Service) o;
        return id != null && id.equals(service.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Service{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", durationMinutes=" + durationMinutes +
                ", basePrice=" + basePrice +
                ", isActive=" + isActive +
                '}';
    }
}