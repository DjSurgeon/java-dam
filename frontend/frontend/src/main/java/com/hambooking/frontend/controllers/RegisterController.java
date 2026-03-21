package com.hambooking.frontend.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controlador de la pantalla de Registro.
 *
 * fx:id declarados en register.fxml:
 *   - firstNameField       → TextField
 *   - lastNameField        → TextField
 *   - dniField             → TextField
 *   - phoneField           → TextField
 *   - emailField           → TextField
 *   - passwordField        → PasswordField
 *   - confirmPasswordField → PasswordField
 *   - errorLabel           → Label
 *   - registerBtn          → Button
 *
 * onAction declarados en register.fxml:
 *   - #handleRegister  → boton "Crear cuenta"
 *   - #goToLogin       → hyperlink "Inicia sesion"
 */
public class RegisterController implements Initializable {

    // ── fx:id ────────────────────────────────────────────────────
    @FXML private TextField     firstNameField;
    @FXML private TextField     lastNameField;
    @FXML private TextField     dniField;
    @FXML private TextField     phoneField;
    @FXML private TextField     emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label         errorLabel;
    @FXML private Button        registerBtn;

    // Patrones de validacion
    private static final String REGEX_DNI   = "^[0-9]{8}[A-Za-z]$";
    private static final String REGEX_PHONE = "^[0-9]{9}$";
    private static final String REGEX_EMAIL = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$";

    // ── Inicializacion ───────────────────────────────────────────
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        // Limpiar error al escribir en cualquier campo
        firstNameField.textProperty().addListener((o, old, nv)       -> clearError());
        lastNameField.textProperty().addListener((o, old, nv)        -> clearError());
        dniField.textProperty().addListener((o, old, nv)             -> clearError());
        phoneField.textProperty().addListener((o, old, nv)           -> clearError());
        emailField.textProperty().addListener((o, old, nv)           -> clearError());
        passwordField.textProperty().addListener((o, old, nv)        -> clearError());
        confirmPasswordField.textProperty().addListener((o, old, nv) -> clearError());
    }

    // ── Handlers ─────────────────────────────────────────────────

    /**
     * Se ejecuta al pulsar "Crear cuenta".
     * Valida todos los campos y, si son correctos, llama a la API.
     * La llamada real a POST /api/auth/register se implementa en Issue #35.
     */
    @FXML
    private void handleRegister() {
        String firstName       = firstNameField.getText().trim();
        String lastName        = lastNameField.getText().trim();
        String dni             = dniField.getText().trim();
        String phone           = phoneField.getText().trim();
        String email           = emailField.getText().trim();
        String password        = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Validacion: campos vacios
        if (firstName.isEmpty() || lastName.isEmpty() || dni.isEmpty()
                || phone.isEmpty() || email.isEmpty()
                || password.isEmpty() || confirmPassword.isEmpty()) {
            showError("Todos los campos son obligatorios.");
            return;
        }

        // Validacion: formato DNI (8 digitos + 1 letra)
        if (!dni.matches(REGEX_DNI)) {
            showError("El DNI debe tener el formato: 12345678A");
            return;
        }

        // Validacion: formato telefono (9 digitos)
        if (!phone.matches(REGEX_PHONE)) {
            showError("El telefono debe tener exactamente 9 digitos.");
            return;
        }

        // Validacion: formato email
        if (!email.matches(REGEX_EMAIL)) {
            showError("El formato del email no es valido.");
            return;
        }

        // Validacion: longitud minima de contrasena
        if (password.length() < 8) {
            showError("La contrasena debe tener al menos 8 caracteres.");
            return;
        }

        // Validacion: contrasena tiene mayuscula y numero
        boolean tieneMayuscula = password.chars().anyMatch(Character::isUpperCase);
        boolean tieneNumero    = password.chars().anyMatch(Character::isDigit);
        if (!tieneMayuscula || !tieneNumero) {
            showError("La contrasena debe tener al menos una mayuscula y un numero.");
            return;
        }

        // Validacion: contrasenas coinciden
        if (!password.equals(confirmPassword)) {
            showError("Las contrasenas no coinciden.");
            return;
        }

        // TODO Issue #35 — ApiClient: POST /api/auth/register
        // RegisterRequestDTO dto = new RegisterRequestDTO(firstName, lastName, email, password, phone);
        // LoginResponseDTO response = authService.register(dto);
        // navigateTo("/com/hambooking/frontend/fxml/login.fxml", "HamBooking");
        showError("Registro pendiente de conexion con API (Issue #35)");
    }

    /**
     * Se ejecuta al pulsar el Hyperlink "Inicia sesion".
     * Vuelve a la pantalla de login.
     */
    @FXML
    private void goToLogin() {
        navigateTo("/com/hambooking/frontend/fxml/login.fxml", "HamBooking - Iniciar sesion");
    }

    // ── Utilidades privadas ───────────────────────────────────────

    private void showError(String mensaje) {
        errorLabel.setText(mensaje);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void clearError() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        errorLabel.setText("");
    }

    private void navigateTo(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
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