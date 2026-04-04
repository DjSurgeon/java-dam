package com.hambooking.frontend.dto;

/**
 * DTOs de autenticacion para el frontend.
 * Espejo de los DTOs del backend, sin anotaciones de validacion
 * (la validacion ya la hace el controlador JavaFX).
 *
 * Se usan como clases internas estaticas para mantener todo junto.
 * Jackson los serializa/deserializa automaticamente.
 */
public class AuthDTO {

    // ── Request: Login ────────────────────────────────────────────
    public static class LoginRequest {
        public String email;
        public String password;

        public LoginRequest() {}

        public LoginRequest(String email, String password) {
            this.email    = email;
            this.password = password;
        }
    }

    // ── Response: Login ───────────────────────────────────────────
    public static class LoginResponse {
        public Long   id;
        public String firstName;
        public String lastName;
        public String email;
        public String role; // "ADMIN" o "CLIENT"

        public LoginResponse() {}
    }

    // ── Request: Register ─────────────────────────────────────────
    public static class RegisterRequest {
        public String dni;
        public String firstName;
        public String lastName;
        public String email;
        public String password;
        public String phone;

        public RegisterRequest() {}

        public RegisterRequest(String dni, String firstName, String lastName,
                               String email, String password, String phone) {
            this.dni       = dni;
            this.firstName = firstName;
            this.lastName  = lastName;
            this.email     = email;
            this.password  = password;
            this.phone     = phone;
        }
    }

    // ── Response: Error del backend ───────────────────────────────
    public static class ErrorResponse {
        public int    status;
        public String message;
        public String timestamp;

        public ErrorResponse() {}
    }
}