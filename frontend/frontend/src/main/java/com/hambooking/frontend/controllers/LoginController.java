package com.hambooking.frontend.controllers;

import com.hambooking.frontend.SessionManager;
import com.hambooking.frontend.dto.AuthDTO;
import com.hambooking.frontend.service.ApiClient;
import com.hambooking.frontend.service.ApiException;
import com.hambooking.frontend.util.AlertHelper;
import com.hambooking.frontend.util.ViewManager;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controlador de la vista de inicio de sesión.
 * Gestiona la autenticación de usuarios y la transición a los paneles principales.
 */
public final class LoginController implements Initializable {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private Button loginBtn;

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        ocultarError();
        // Limpiar errores mientras el usuario escribe para mejorar la UX
        emailField.textProperty().addListener((obs, old, val) -> ocultarError());
        passwordField.textProperty().addListener((obs, old, val) -> ocultarError());
    }

    /**
     * Procesa la solicitud de inicio de sesión.
     */
    @FXML
    private void handleLogin() {
        final String email = emailField.getText().trim();
        final String password = passwordField.getText();

        if (!validarEntradas(email, password)) {
            return;
        }

        setLoadingState(true);
        final Task<AuthDTO.LoginResponse> loginTask = createLoginTask(email, password);

        loginTask.setOnSucceeded(event -> {
            final AuthDTO.LoginResponse user = loginTask.getValue();
            SessionManager.getInstance().setSession(user);
            redigirAlDashboard(user);
        });

        loginTask.setOnFailed(event -> {
            setLoadingState(false);
            final Throwable ex = loginTask.getException();
            mostrarError(ex.getMessage());
        });

        final Thread thread = new Thread(loginTask);
        thread.setDaemon(true);
        thread.start();
    }

    private boolean validarEntradas(final String email, final String password) {
        if (email.isEmpty() || password.isEmpty()) {
            mostrarError("Email y contraseña son obligatorios.");
            return false;
        }
        if (!email.contains("@")) {
            mostrarError("Introduce un formato de email válido.");
            return false;
        }
        return true;
    }

    private Task<AuthDTO.LoginResponse> createLoginTask(final String email, final String password) {
        return new Task<>() {
            @Override
            protected AuthDTO.LoginResponse call() throws ApiException {
                AuthDTO.LoginRequest request = new AuthDTO.LoginRequest(email, password);
                return ApiClient.getInstance().post("/auth/login", request, AuthDTO.LoginResponse.class);
            }
        };
    }

    private void redigirAlDashboard(final AuthDTO.LoginResponse user) {
        final boolean isAdmin = "ADMIN".equals(user.role);
        final String fxml = isAdmin 
                ? "/com/hambooking/frontend/fxml/admin-dashboard.fxml" 
                : "/com/hambooking/frontend/fxml/client-dashboard.fxml";
        final String title = isAdmin ? "HamBooking - Panel de Administración" : "HamBooking - Mi Panel";

        try {
            ViewManager.getInstance().navigateTo(fxml, title);
        } catch (IOException e) {
            AlertHelper.showError("Error de Navegación", "No se pudo cargar la vista del dashboard principal.");
        }
    }

    @FXML
    private void goToRegister() {
        try {
            ViewManager.getInstance().navigateTo("/com/hambooking/frontend/fxml/register.fxml", "HamBooking - Registro");
        } catch (IOException e) {
            AlertHelper.showError("Error", "No se pudo cargar la vista de registro.");
        }
    }

    private void setLoadingState(final boolean loading) {
        loginBtn.setDisable(loading);
        loginBtn.setText(loading ? "Conectando..." : "Iniciar sesión");
    }

    private void mostrarError(final String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void ocultarError() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }
}
