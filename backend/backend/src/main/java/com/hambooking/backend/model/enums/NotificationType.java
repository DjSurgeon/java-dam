package com.hambooking.backend.model.enums;

public enum NotificationType {
    CREATED("Reserva Creada"),
    MODIFIED("Reserva Modificada"),
    CANCELLED("Reserva Cancelada"),
    REMINDER("Recordatorio");

    private final String displayName;

    NotificationType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}