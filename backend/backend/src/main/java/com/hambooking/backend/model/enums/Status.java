package com.hambooking.backend.model.enums;

public enum Status {
    PENDING("Pendiente"),
    CONFIRMED("Confirmada"),
    COMPLETED("Completada"),
    CANCELLED("Cancelada");

    private final String displayName;

    Status(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}