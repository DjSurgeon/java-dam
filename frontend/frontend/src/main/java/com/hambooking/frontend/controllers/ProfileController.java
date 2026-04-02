package com.hambooking.frontend.controllers;

import com.hambooking.frontend.SessionManager;
import com.hambooking.frontend.dto.AppDTO;
import com.hambooking.frontend.service.ApiClient;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class ProfileController implements Initializable {

    @FXML private Label         sidebarUserName;
    @FXML private Label         lblNombre;
    @FXML private Label         lblApellidos;
    @FXML private Label         lblDni;
    @FXML private Label         lblEmail;
    @FXML private Label         lblTelefono;
    @FXML private PasswordField pfActual;
    @FXML private PasswordField pfNueva;
    @FXML private PasswordField pfConfirmar;
    @FXML private Label         lblError;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        SessionManager session = SessionManager.getInstance();
        sidebarUserName.setText(session.getFullName());
        lblError.setVisible(false);
        lblError.setManaged(false);
        cargarDatosUsuario();
    }

    private void cargarDatosUsuario() {
        Long userId = SessionManager.getInstance().getUserId();
        Thread t = new Thread(() -> {
            try {
                AppDTO.UserResponse user = ApiClient.getInstance()
                        .get("/users/" + userId, AppDTO.UserResponse.class);
                Platform.runLater(() -> {
                    lblNombre.setText(user.firstName != null ? user.firstName : "");
                    lblApellidos.setText(user.lastName != null ? user.lastName : "");
                    lblDni.setText(user.dni != null ? user.dni : "");
                    lblEmail.setText(user.email != null ? user.email : "");
                    lblTelefono.setText(user.phone != null ? user.phone : "");
                });
            } catch (ApiClient.ApiException ex) {
                Platform.runLater(() -> mostrarError("Error al cargar perfil: " + ex.getMessage()));
            }
        });
        t.setDaemon(true);
        t.start();
    }

    @FXML
    private void handleCambiarPassword() {
        String actual    = pfActual.getText();
        String nueva     = pfNueva.getText();
        String confirmar = pfConfirmar.getText();

        if (actual.isEmpty() || nueva.isEmpty() || confirmar.isEmpty()) {
            mostrarError("Todos los campos de contrase\u00f1a son obligatorios.");
            return;
        }
        if (nueva.length() < 8) {
            mostrarError("La nueva contrase\u00f1a debe tener al menos 8 caracteres.");
            return;
        }
        boolean tieneMayuscula = nueva.chars().anyMatch(Character::isUpperCase);
        boolean tieneNumero    = nueva.chars().anyMatch(Character::isDigit);
        if (!tieneMayuscula || !tieneNumero) {
            mostrarError("La contrase\u00f1a debe tener al menos una may\u00fascula y un n\u00famero.");
            return;
        }
        if (!nueva.equals(confirmar)) {
            mostrarError("Las contrase\u00f1as no coinciden.");
            return;
        }

        Long userId = SessionManager.getInstance().getUserId();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("currentPassword", actual);
        body.put("newPassword", nueva);

        Thread t = new Thread(() -> {
            try {
                ApiClient.getInstance().put("/users/" + userId + "/password", body);
                Platform.runLater(() -> {
                    pfActual.clear();
                    pfNueva.clear();
                    pfConfirmar.clear();
                    ocultarError();
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("\u00c9xito");
                    alert.setHeaderText(null);
                    alert.setContentText("Contrase\u00f1a actualizada correctamente.");
                    alert.showAndWait();
                });
            } catch (ApiClient.ApiException ex) {
                Platform.runLater(() -> mostrarError(ex.getMessage()));
            }
        });
        t.setDaemon(true);
        t.start();
    }

    // ── Navegacion ───────────────────────────────────────────────

    @FXML private void goToCalendar() {
        navigateTo("/com/hambooking/frontend/fxml/calendar.fxml",
                "HamBooking - Nueva Reserva");
    }

    @FXML private void goToDashboard() {
        navigateTo("/com/hambooking/frontend/fxml/client-dashboard.fxml",
                "HamBooking - Mi Panel");
    }

    @FXML private void goToNotifications() {
        navigateTo("/com/hambooking/frontend/fxml/notifications.fxml",
                "HamBooking - Notificaciones");
    }

    @FXML private void handleLogout() {
        SessionManager.getInstance().clear();
        navigateTo("/com/hambooking/frontend/fxml/login.fxml",
                "HamBooking - Iniciar sesi\u00f3n");
    }

    // ── Utilidades ───────────────────────────────────────────────

    private void mostrarError(String msg) {
        lblError.setText(msg);
        lblError.setVisible(true);
        lblError.setManaged(true);
    }

    private void ocultarError() {
        lblError.setVisible(false);
        lblError.setManaged(false);
    }

    private void navigateTo(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) lblNombre.getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.setTitle(title);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}