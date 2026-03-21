package com.hambooking.frontend.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controlador de la pantalla de Login.
 *
 * fx:id declarados en login.fxml:
 *   - emailField    → TextField
 *   - passwordField → PasswordField
 *   - errorLabel    → Label (oculto por defecto)
 *   - loginBtn      → Button
 *
 * onAction declarados en login.fxml:
 *   - #handleLogin   → botón "Iniciar sesión"
 *   - #goToRegister  → hyperlink "Regístrate aquí"
 */
public class LoginController implements Initializable {

    // ── Campos inyectados desde el FXML ──────────────────────────
    @FXML private TextField     emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label         errorLabel;
    @FXML private Button        loginBtn;

    // ── Inicialización ───────────────────────────────────────────
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // El errorLabel empieza oculto y sin ocupar espacio en el layout
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        // Limpiar el error en cuanto el usuario empiece a escribir
        emailField.textProperty().addListener((obs, oldVal, newVal) -> clearError());
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> clearError());
    }

    // ── Handlers ─────────────────────────────────────────────────

    /**
     * Se ejecuta al pulsar "Iniciar sesión".
     * Valida campos y navega al dashboard según rol.
     * La llamada real a la API se implementará en Issue #35 (ApiClient).
     */
    @FXML
    private void handleLogin() {
        String email    = emailField.getText().trim();
        String password = passwordField.getText();

        // Validación: campos vacíos
        if (email.isEmpty() || password.isEmpty()) {
            showError("Por favor, introduce tu email y contraseña.");
            return;
        }

        // Validación: formato mínimo de email
        if (!email.contains("@") || !email.contains(".")) {
            showError("El formato del email no es válido.");
            return;
        }

        // TODO Issue #35 — ApiClient: POST /api/auth/login
        // LoginResponseDTO response = authService.login(email, password);
        // if (response.getRole() == Role.ADMIN) navigateTo(admin-dashboard.fxml)
        // else navigateTo(client-dashboard.fxml)
        showError("Conectando con el servidor… (pendiente Issue #35)");
    }

    /**
     * Se ejecuta al pulsar el Hyperlink "Regístrate aquí".
     * Navega a la pantalla de registro.
     */
    @FXML
    private void goToRegister() {
        navigateTo(
                "/com/hambooking/frontend/fxml/register.fxml",
                "HamBooking – Crear cuenta"
        );
    }

    // ── Utilidades privadas ───────────────────────────────────────

    /** Muestra el label de error con el mensaje indicado. */
    private void showError(String mensaje) {
        errorLabel.setText(mensaje);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    /** Oculta el label de error y libera su espacio en el layout. */
    private void clearError() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        errorLabel.setText("");
    }

    /**
     * Cambia la pantalla actual cargando un nuevo FXML.
     * Reutiliza el Stage y la Scene existentes.
     *
     * @param fxmlPath ruta al recurso FXML (desde resources/)
     * @param title    título de la ventana
     */
    private void navigateTo(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(fxmlPath)
            );
            Parent root = loader.load();
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.setTitle(title);
        } catch (IOException e) {
            showError("Error al cargar la pantalla: " + e.getMessage());
            e.printStackTrace();
        }
    }
}