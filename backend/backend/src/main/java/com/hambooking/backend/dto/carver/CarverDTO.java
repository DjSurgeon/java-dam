package com.hambooking.backend.dto.carver;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CarverDTO {

    // Campos de solo lectura (respuesta) — datos del usuario asociado
    private Long   id;
    private Long   userId;
    private String firstName;
    private String lastName;
    private String dni;
    private String email;
    private String phone;
    private Boolean isActive;

    // Campos editables
    @Size(max = 100, message = "La especialidad no puede exceder 100 caracteres")
    private String specialty;

    @Min(value = 0, message = "Los años de experiencia no pueden ser negativos")
    private Integer experienceYears;

    @NotNull(message = "El límite diario de servicios es obligatorio")
    @Min(value = 1, message = "Debe permitir al menos 1 servicio por día")
    @Max(value = 10, message = "El límite máximo de servicios por día es 10")
    private Integer maxHamsPerDay;

    public CarverDTO() {}

    // Constructor completo para respuestas (toDTO)
    public CarverDTO(Long id, Long userId,
                     String firstName, String lastName, String dni, String email, String phone,
                     String specialty, Integer experienceYears, Integer maxHamsPerDay,
                     Boolean isActive) {
        this.id            = id;
        this.userId        = userId;
        this.firstName     = firstName;
        this.lastName      = lastName;
        this.dni           = dni;
        this.email         = email;
        this.phone         = phone;
        this.specialty     = specialty;
        this.experienceYears = experienceYears;
        this.maxHamsPerDay = maxHamsPerDay;
        this.isActive      = isActive;
    }

    public Long    getId()             { return id; }
    public void    setId(Long id)      { this.id = id; }

    public Long    getUserId()         { return userId; }
    public void    setUserId(Long v)   { this.userId = v; }

    public String  getFirstName()      { return firstName; }
    public void    setFirstName(String v) { this.firstName = v; }

    public String  getLastName()       { return lastName; }
    public void    setLastName(String v)  { this.lastName = v; }

    public String  getDni()            { return dni; }
    public void    setDni(String v)    { this.dni = v; }

    public String  getEmail()          { return email; }
    public void    setEmail(String v)  { this.email = v; }

    public String  getPhone()          { return phone; }
    public void    setPhone(String v)  { this.phone = v; }

    public String  getSpecialty()      { return specialty; }
    public void    setSpecialty(String v) { this.specialty = v; }

    public Integer getExperienceYears()       { return experienceYears; }
    public void    setExperienceYears(Integer v) { this.experienceYears = v; }

    public Integer getMaxHamsPerDay()         { return maxHamsPerDay; }
    public void    setMaxHamsPerDay(Integer v)   { this.maxHamsPerDay = v; }

    public Boolean getIsActive()       { return isActive; }
    public void    setIsActive(Boolean v) { this.isActive = v; }
}