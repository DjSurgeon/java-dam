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
 * Controlador para la vista de registro de usuarios.
 * Gestiona la creación de nuevas cuentas de cliente, validando los datos personales,
 * de contacto y credenciales de seguridad antes de la comunicación con la API.
 */
public class RegisterController implements Initializable {

    /** Campo de entrada para el nombre del usuario. */
    @FXML private TextField firstNameField;

    /** Campo de entrada para los apellidos del usuario. */
    @FXML private TextField lastNameField;

    /** Campo de entrada para el DNI (Documento Nacional de Identidad). */
    @FXML private TextField dniField;

    /** Campo de entrada para el número de teléfono. */
    @FXML private TextField phoneField;

    /** Campo de entrada para el correo electrónico. */
    @FXML private TextField emailField;

    /** Campo para introducir la contraseña deseada. */
    @FXML private PasswordField passwordField;

    /** Campo para confirmar la contraseña introducida. */
    @FXML private PasswordField confirmPasswordField;

    /** Etiqueta para la visualización de mensajes de error de validación o del servidor. */
    @FXML private Label errorLabel;

    /** Botón para ejecutar la acción de registro. */
    @FXML private Button registerBtn;

    /** Expresión regular para validar el formato del DNI (8 dígitos y una letra). */
    private static final String REGEX_DNI   = "^[0-9]{8}[A-Za-z]$";
    /** Expresión regular para validar el número de teléfono (9 dígitos). */
    private static final String REGEX_PHONE = "^[0-9]{9}$";
    /** Expresión regular estándar para la validación básica de correos electrónicos. */
    private static final String REGEX_EMAIL = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$";

    /**
     * Inicializa el controlador ocultando los errores y configurando los listeners
     * de limpieza automática de mensajes de error al modificar los campos.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        // Añadir listeners para limpiar el error en cuanto el usuario empiece a corregir
        firstNameField.textProperty().addListener((o, old, nv)       -> clearError());
        lastNameField.textProperty().addListener((o, old, nv)        -> clearError());
        dniField.textProperty().addListener((o, old, nv)             -> clearError());
        phoneField.textProperty().addListener((o, old, nv)           -> clearError());
        emailField.textProperty().addListener((o, old, nv)           -> clearError());
        passwordField.textProperty().addListener((o, old, nv)        -> clearError());
        confirmPasswordField.textProperty().addListener((o, old, nv) -> clearError());
    }

    /**
     * Gestiona la lógica de registro al pulsar el botón correspondiente.
     * Realiza las validaciones de negocio y lanza la tarea asíncrona de creación de cuenta.
     */
    @FXML
    private void handleRegister() {
        if (!validarFormulario()) return;

        configurarEstadoCargando(true);

        Task<AuthDTO.LoginResponse> registerTask = crearTareaRegistro();

        registerTask.setOnSucceeded(event -> {
            procesarExitoRegistro(registerTask.getValue());
        });

        registerTask.setOnFailed(event -> {
            procesarFalloRegistro(registerTask.getException());
        });

        Thread thread = new Thread(registerTask);
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Valida de forma integral todos los campos del formulario de registro.
     * 
     * @return true si todos los campos cumplen con los criterios de validación.
     */
    private boolean validarFormulario() {
        String firstName = firstNameField.getText().trim();
        String lastName  = lastNameField.getText().trim();
        String dni       = dniField.getText().trim();
        String phone     = phoneField.getText().trim();
        String email     = emailField.getText().trim();
        String pass      = passwordField.getText();
        String confirm   = confirmPasswordField.getText();

        if (firstName.isEmpty() || lastName.isEmpty() || dni.isEmpty()
                || phone.isEmpty() || email.isEmpty() || pass.isEmpty() || confirm.isEmpty()) {
            showError("Todos los campos son obligatorios.");
            return false;
        }
        if (!dni.matches(REGEX_DNI)) {
            showError("El DNI debe tener el formato: 12345678A.");
            return false;
        }
        if (!phone.matches(REGEX_PHONE)) {
            showError("El teléfono debe tener exactamente 9 dígitos.");
            return false;
        }
        if (!email.matches(REGEX_EMAIL)) {
            showError("El formato del email no es válido.");
            return false;
        }
        if (pass.length() < 8) {
            showError("La contraseña debe tener al menos 8 caracteres.");
            return false;
        }
        
        boolean tieneMayuscula = pass.chars().anyMatch(Character::isUpperCase);
        boolean tieneNumero    = pass.chars().anyMatch(Character::isDigit);
        if (!tieneMayuscula || !tieneNumero) {
            showError("La contraseña debe incluir al menos una mayúscula y un número.");
            return false;
        }
        if (!pass.equals(confirm)) {
            showError("Las contraseñas no coinciden.");
            return false;
        }
        
        return true;
    }

    /**
     * Crea la tarea asíncrona para enviar los datos de registro a la API.
     */
    private Task<AuthDTO.LoginResponse> crearTareaRegistro() {
        AuthDTO.RegisterRequest request = new AuthDTO.RegisterRequest(
                dniField.getText().trim(),
                firstNameField.getText().trim(),
                lastNameField.getText().trim(),
                emailField.getText().trim(),
                passwordField.getText(),
                phoneField.getText().trim()
        );

        return new Task<>() {
            @Override
            protected AuthDTO.LoginResponse call() throws Exception {
                return ApiClient.getInstance().post("/auth/register", request, AuthDTO.LoginResponse.class);
            }
        };
    }

    /**
     * Gestiona la respuesta satisfactoria del registro, guardando la sesión automática.
     */
    private void procesarExitoRegistro(AuthDTO.LoginResponse response) {
        SessionManager.getInstance().setSession(response);

        navigateTo("/com/hambooking/frontend/fxml/client-dashboard.fxml", "HamBooking - Mi Panel");
    }

    /**
     * Gestiona los fallos de comunicación o errores de negocio devueltos por la API.
     */
    private void procesarFalloRegistro(Throwable exception) {
        showError(exception.getMessage());
        configurarEstadoCargando(false);
    }

    /**
     * Configura el estado visual de los controles durante la espera de respuesta.
     */
    private void configurarEstadoCargando(boolean cargando) {
        registerBtn.setDisable(cargando);
        registerBtn.setText(cargando ? "Creando cuenta..." : "Crear cuenta");
    }

    /**
     * Navega de vuelta a la pantalla de inicio de sesión.
     */
    @FXML
    private void goToLogin() {
        navigateTo("/com/hambooking/frontend/fxml/login.fxml", "HamBooking - Iniciar sesión");
    }

    /**
     * Muestra un mensaje informativo de error en la vista.
     */
    private void showError(String mensaje) {
        errorLabel.setText(mensaje);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    /**
     * Elimina cualquier mensaje de error visible.
     */
    private void clearError() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        errorLabel.setText("");
    }

    /**
     * Método centralizado para la navegación a través del ViewManager.
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
