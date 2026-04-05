package com.hambooking.frontend.controllers;

import com.hambooking.frontend.SessionManager;
import com.hambooking.frontend.dto.AuthDTO;
import com.hambooking.frontend.service.ApiClient;
import com.hambooking.frontend.util.ViewManager;
import javafx.application.Platform;
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
 * Controlador para la vista de inicio de sesión (Login).
 * Gestiona la autenticación de usuarios, la validación de credenciales y la navegación inicial.
 */
public class LoginController implements Initializable {

    /** Campo de texto para el correo electrónico del usuario. */
    @FXML private TextField emailField;

    /** Campo de texto para la contraseña. */
    @FXML private PasswordField passwordField;

    /** Etiqueta para mostrar mensajes de error de validación o red. */
    @FXML private Label errorLabel;

    /** Botón para disparar la acción de inicio de sesión. */
    @FXML private Button loginBtn;

    /**
     * Inicializa el controlador configurando los estados iniciales de la UI.
     * Añade listeners para limpiar errores cuando el usuario vuelve a escribir.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        emailField.textProperty().addListener((o, old, nv) -> clearError());
        passwordField.textProperty().addListener((o, old, nv) -> clearError());
    }

    /**
     * Gestiona el evento de clic en el botón de login.
     * Valida los campos localmente y lanza una tarea asíncrona para la autenticación.
     */
    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (!validarCampos(email, password)) return;

        configurarEstadoCargando(true);

        Task<AuthDTO.LoginResponse> loginTask = crearTareaLogin(email, password);

        loginTask.setOnSucceeded(event -> {
            procesarExitoLogin(loginTask.getValue());
        });

        loginTask.setOnFailed(event -> {
            procesarFalloLogin(loginTask.getException());
        });

        Thread thread = new Thread(loginTask);
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Valida que el email y la contraseña cumplan con el formato básico.
     * 
     * @param email    Email a validar.
     * @param password Contraseña a validar.
     * @return true si los campos son válidos.
     */
    private boolean validarCampos(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            showError("Por favor, introduce tu email y contraseña.");
            return false;
        }
        if (!email.contains("@")) {
            showError("El formato del email no es válido.");
            return false;
        }
        return true;
    }

    /**
     * Crea la tarea asíncrona para realizar la petición HTTP de login.
     */
    private Task<AuthDTO.LoginResponse> crearTareaLogin(String email, String password) {
        return new Task<>() {
            @Override
            protected AuthDTO.LoginResponse call() throws Exception {
                AuthDTO.LoginRequest request = new AuthDTO.LoginRequest(email, password);
                return ApiClient.getInstance().post("/auth/login", request, AuthDTO.LoginResponse.class);
            }
        };
    }

    /**
     * Procesa la respuesta exitosa del servidor guardando la sesión y navegando al dashboard.
     */
    private void procesarExitoLogin(AuthDTO.LoginResponse response) {
        SessionManager.getInstance().setSession(
                response.id, response.firstName, response.lastName,
                response.email, response.role
        );

        String destino = "ADMIN".equals(response.role)
                ? "/com/hambooking/frontend/fxml/admin-dashboard.fxml"
                : "/com/hambooking/frontend/fxml/client-dashboard.fxml";
        
        String titulo = "ADMIN".equals(response.role)
                ? "HamBooking - Panel de Administración"
                : "HamBooking - Mi Panel";

        navigateTo(destino, titulo);
    }

    /**
     * Procesa los fallos en la tarea de login (errores de API o de red).
     */
    private void procesarFalloLogin(Throwable exception) {
        showError(exception.getMessage());
        configurarEstadoCargando(false);
    }

    /**
     * Configura la visualización de los controles durante el proceso de carga.
     */
    private void configurarEstadoCargando(boolean cargando) {
        loginBtn.setDisable(cargando);
        loginBtn.setText(cargando ? "Conectando..." : "Iniciar sesión");
    }

    /**
     * Navega hacia la vista de registro de nuevos clientes.
     */
    @FXML
    private void goToRegister() {
        navigateTo("/com/hambooking/frontend/fxml/register.fxml", "HamBooking - Crear cuenta");
    }

    /**
     * Muestra un mensaje de error en la interfaz.
     */
    private void showError(String mensaje) {
        errorLabel.setText(mensaje);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    /**
     * Limpia los mensajes de error activos.
     */
    private void clearError() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        errorLabel.setText("");
    }

    /**
     * Utiliza el ViewManager para cambiar de pantalla.
     */
    private void navigateTo(String fxmlPath, String title) {
        try {
            ViewManager.getInstance().navigateTo(fxmlPath, title);
        } catch (IOException e) {
            showError("Error al cargar la pantalla: " + e.getMessage());
            configurarEstadoCargando(false);
        }
    }
}
