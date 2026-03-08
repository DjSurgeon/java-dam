package com.hambooking.backend.dto.service;

import java.math.BigDecimal;

public class ServiceResponseDTO {

    private Long id;
    private String name;
    private String description;
    private Integer durationMinutes;
    private BigDecimal basePrice;
    private Boolean isActive;

    public ServiceResponseDTO() {}

    public ServiceResponseDTO(Long id, String name, String description,
                              Integer durationMinutes, BigDecimal basePrice, Boolean isActive) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.durationMinutes = durationMinutes;
        this.basePrice = basePrice;
        this.isActive = isActive;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }

    public BigDecimal getBasePrice() { return basePrice; }
    public void setBasePrice(BigDecimal basePrice) { this.basePrice = basePrice; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}