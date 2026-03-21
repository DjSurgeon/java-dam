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

public class LoginController implements Initializable {

    @FXML private TextField     emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label         errorLabel;
    @FXML private Button        loginBtn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        emailField.textProperty().addListener((o, old, nv)    -> clearError());
        passwordField.textProperty().addListener((o, old, nv) -> clearError());
    }

    @FXML
    private void handleLogin() {
        String email    = emailField.getText().trim();
        String password = passwordField.getText();

        // Validaciones locales
        if (email.isEmpty() || password.isEmpty()) {
            showError("Por favor, introduce tu email y contrasena.");
            return;
        }
        if (!email.contains("@")) {
            showError("El formato del email no es valido.");
            return;
        }

        // Deshabilitar boton mientras se hace la peticion
        loginBtn.setDisable(true);
        loginBtn.setText("Conectando...");

        // Llamada a la API en hilo secundario para no bloquear la UI
        Thread thread = new Thread(() -> {
            try {
                AuthDTO.LoginRequest request = new AuthDTO.LoginRequest(email, password);
                AuthDTO.LoginResponse response = ApiClient.getInstance()
                        .post("/auth/login", request, AuthDTO.LoginResponse.class);

                // Guardar sesion
                SessionManager.getInstance().setSession(
                        response.id,
                        response.firstName,
                        response.lastName,
                        response.email,
                        response.role
                );

                // Navegar al dashboard segun rol (en hilo JavaFX)
                Platform.runLater(() -> {
                    String destino = "ADMIN".equals(response.role)
                            ? "/com/hambooking/frontend/fxml/admin-dashboard.fxml"
                            : "/com/hambooking/frontend/fxml/client-dashboard.fxml";
                    String titulo = "ADMIN".equals(response.role)
                            ? "HamBooking - Panel de Administracion"
                            : "HamBooking - Mi Panel";
                    navigateTo(destino, titulo);
                });

            } catch (ApiClient.ApiException e) {
                Platform.runLater(() -> {
                    showError(e.getMessage());
                    loginBtn.setDisable(false);
                    loginBtn.setText("Iniciar sesion");
                });
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    private void goToRegister() {
        navigateTo("/com/hambooking/frontend/fxml/register.fxml",
                "HamBooking - Crear cuenta");
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
            loginBtn.setDisable(false);
            loginBtn.setText("Iniciar sesion");
        }
    }
}