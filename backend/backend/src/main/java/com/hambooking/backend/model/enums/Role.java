package com.hambooking.backend.model.enums;

/**
 * Enum que representa los roles de usuario en el sistema.
 *
 * DECISIÓN DE DISEÑO:
 * - Solo 2 roles en v1.0: ADMIN (administrador único) y CLIENT (clientes)
 */
public enum Role {
    ADMIN("Administrador"),
    CLIENT("Cliente");

    private final String displayName;

    /**
     * @param displayName Nombre legible para humanos
     */
    Role(String displayName) {
        this.displayName = displayName;
    }

    /**
     * @return Nombre amigable del rol
     */
    public String getDisplayName() {
        return displayName;
    }
}