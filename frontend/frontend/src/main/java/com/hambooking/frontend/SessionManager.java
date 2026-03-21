package com.hambooking.frontend;

/**
 * Singleton que guarda la sesion del usuario durante la ejecucion.
 * Se rellena al hacer login y se limpia al cerrar sesion.
 */
public class SessionManager {

    private static SessionManager instance;

    private Long   userId;
    private String firstName;
    private String lastName;
    private String email;
    private String role; // "ADMIN" o "CLIENT"

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    // ── Getters ──────────────────────────────────────────────────

    public Long   getUserId()    { return userId; }
    public String getFirstName() { return firstName; }
    public String getLastName()  { return lastName; }
    public String getEmail()     { return email; }
    public String getRole()      { return role; }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }

    public boolean isLoggedIn() {
        return userId != null;
    }

    // ── Setter unico: se llama solo al hacer login ────────────────

    public void setSession(Long userId, String firstName, String lastName,
                           String email, String role) {
        this.userId    = userId;
        this.firstName = firstName;
        this.lastName  = lastName;
        this.email     = email;
        this.role      = role;
    }

    // ── Limpiar sesion al logout ──────────────────────────────────

    public void clear() {
        this.userId    = null;
        this.firstName = null;
        this.lastName  = null;
        this.email     = null;
        this.role      = null;
    }
}