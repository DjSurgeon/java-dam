package com.hambooking.backend.model.enums;

public enum RecipientType {
    CLIENT("Cliente"),
    CARVER("Cortador"),
    ADMIN("Administrador");

    private final String displayName;

    RecipientType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}