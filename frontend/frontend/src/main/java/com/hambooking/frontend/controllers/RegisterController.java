package com.hambooking.frontend.controllers;

import com.hambooking.frontend.SessionManager;
import com.hambooking.frontend.dto.AuthDTO;
import com.hambooking.frontend.service.ApiClient;
import javafx.application.Platform;
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

public class RegisterController implements Initializable {

    @FXML private TextField     firstNameField;
    @FXML private TextField     lastNameField;
    @FXML private TextField     dniField;
    @FXML private TextField     phoneField;
    @FXML private TextField     emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label         errorLabel;
    @FXML private Button        registerBtn;

    private static final String REGEX_DNI   = "^[0-9]{8}[A-Za-z]$";
    private static final String REGEX_PHONE = "^[0-9]{9}$";
    private static final String REGEX_EMAIL = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        firstNameField.textProperty().addListener((o, old, nv)       -> clearError());
        lastNameField.textProperty().addListener((o, old, nv)        -> clearError());
        dniField.textProperty().addListener((o, old, nv)             -> clearError());
        phoneField.textProperty().addListener((o, old, nv)           -> clearError());
        emailField.textProperty().addListener((o, old, nv)           -> clearError());
        passwordField.textProperty().addListener((o, old, nv)        -> clearError());
        confirmPasswordField.textProperty().addListener((o, old, nv) -> clearError());
    }

    @FXML
    private void handleRegister() {
        String firstName       = firstNameField.getText().trim();
        String lastName        = lastNameField.getText().trim();
        String dni             = dniField.getText().trim();
        String phone           = phoneField.getText().trim();
        String email           = emailField.getText().trim();
        String password        = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Validaciones locales
        if (firstName.isEmpty() || lastName.isEmpty() || dni.isEmpty()
                || phone.isEmpty() || email.isEmpty()
                || password.isEmpty() || confirmPassword.isEmpty()) {
            showError("Todos los campos son obligatorios.");
            return;
        }
        if (!dni.matches(REGEX_DNI)) {
            showError("El DNI debe tener el formato: 12345678A");
            return;
        }
        if (!phone.matches(REGEX_PHONE)) {
            showError("El telefono debe tener exactamente 9 digitos.");
            return;
        }
        if (!email.matches(REGEX_EMAIL)) {
            showError("El formato del email no es valido.");
            return;
        }
        if (password.length() < 8) {
            showError("La contrasena debe tener al menos 8 caracteres.");
            return;
        }
        boolean tieneMayuscula = password.chars().anyMatch(Character::isUpperCase);
        boolean tieneNumero    = password.chars().anyMatch(Character::isDigit);
        if (!tieneMayuscula || !tieneNumero) {
            showError("La contrasena debe tener al menos una mayuscula y un numero.");
            return;
        }
        if (!password.equals(confirmPassword)) {
            showError("Las contrasenas no coinciden.");
            return;
        }

        // Deshabilitar boton mientras se hace la peticion
        registerBtn.setDisable(true);
        registerBtn.setText("Creando cuenta...");

        // Llamada a la API en hilo secundario
        Thread thread = new Thread(() -> {
            try {
                AuthDTO.RegisterRequest request = new AuthDTO.RegisterRequest(
                        dni, firstName, lastName, email, password, phone
                );
                AuthDTO.LoginResponse response = ApiClient.getInstance()
                        .post("/auth/register", request, AuthDTO.LoginResponse.class);

                // Guardar sesion (el backend devuelve LoginResponse tras registrar)
                SessionManager.getInstance().setSession(
                        response.id,
                        response.firstName,
                        response.lastName,
                        response.email,
                        response.role
                );

                // Navegar al dashboard del cliente
                Platform.runLater(() ->
                        navigateTo("/com/hambooking/frontend/fxml/client-dashboard.fxml",
                                "HamBooking - Mi Panel")
                );

            } catch (ApiClient.ApiException e) {
                Platform.runLater(() -> {
                    showError(e.getMessage());
                    registerBtn.setDisable(false);
                    registerBtn.setText("Crear cuenta");
                });
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    private void goToLogin() {
        navigateTo("/com/hambooking/frontend/fxml/login.fxml",
                "HamBooking - Iniciar sesion");
    }

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
            registerBtn.setDisable(false);
            registerBtn.setText("Crear cuenta");
        }
    }
}