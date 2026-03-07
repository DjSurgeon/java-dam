package com.hambooking.backend.dto.carver;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CarverDTO {

    // El ID del usuario al que se le asigna el perfil de cortador
    // Solo se usa en la creación, en actualizaciones se ignora
    private Long userId;

    @Size(max = 100, message = "La especialidad no puede exceder 100 caracteres")
    private String specialty;

    @Min(value = 0, message = "Los años de experiencia no pueden ser negativos")
    private Integer experienceYears;

    @NotNull(message = "El límite diario de servicios es obligatorio")
    @Min(value = 1, message = "Debe permitir al menos 1 servicio por día")
    @Max(value = 10, message = "El límite máximo de servicios por día es 10")
    private Integer maxHamsPerDay;

    // Solo viene relleno en respuestas, nunca en peticiones de entrada
    private Long id;
    private Boolean isActive;

    public CarverDTO() {}

    public CarverDTO(Long id, Long userId, String specialty,
                     Integer experienceYears, Integer maxHamsPerDay, Boolean isActive) {
        this.id = id;
        this.userId = userId;
        this.specialty = specialty;
        this.experienceYears = experienceYears;
        this.maxHamsPerDay = maxHamsPerDay;
        this.isActive = isActive;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getSpecialty() { return specialty; }
    public void setSpecialty(String specialty) { this.specialty = specialty; }

    public Integer getExperienceYears() { return experienceYears; }
    public void setExperienceYears(Integer experienceYears) { this.experienceYears = experienceYears; }

    public Integer getMaxHamsPerDay() { return maxHamsPerDay; }
    public void setMaxHamsPerDay(Integer maxHamsPerDay) { this.maxHamsPerDay = maxHamsPerDay; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}