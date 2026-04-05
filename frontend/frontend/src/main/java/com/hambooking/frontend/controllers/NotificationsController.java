package com.hambooking.frontend.controllers;

import com.hambooking.frontend.SessionManager;
import com.hambooking.frontend.dto.AppDTO;
import com.hambooking.frontend.service.ApiClient;
import com.hambooking.frontend.util.ViewManager;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.text.Text;

import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class NotificationsController implements Initializable {

    @FXML private Label sidebarUserName;

    @FXML private TableView<AppDTO.NotificationResponse>           notifTable;
    @FXML private TableColumn<AppDTO.NotificationResponse, String> nColFecha;
    @FXML private TableColumn<AppDTO.NotificationResponse, String> nColTipo;
    @FXML private TableColumn<AppDTO.NotificationResponse, String> nColAsunto;
    @FXML private TableColumn<AppDTO.NotificationResponse, String> nColMensaje;

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm", new Locale("es", "ES"));

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        sidebarUserName.setText(SessionManager.getInstance().getFullName());
        configurarTabla();
        cargarNotificaciones();
    }

    private void configurarTabla() {
        nColFecha.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().sentAt != null ? d.getValue().sentAt.format(FMT) : ""));
        nColTipo.setCellValueFactory(d -> new SimpleStringProperty(
                traducirTipo(d.getValue().notificationType)));
        nColAsunto.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().subject != null ? d.getValue().subject : ""));
        // Mensaje con wrap de texto para que se lea completo
        nColMensaje.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().message != null ? d.getValue().message : ""));
        nColMensaje.setCellFactory(col -> new TableCell<AppDTO.NotificationResponse, String>() {
            private final Text text = new Text();
            {
                text.wrappingWidthProperty().bind(nColMensaje.widthProperty().subtract(10));
                text.setStyle("-fx-font-size: 11px;");
                setGraphic(text);
                setPrefHeight(Control.USE_COMPUTED_SIZE);
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                text.setText(empty || item == null ? "" : item);
            }
        });
    }

    private void cargarNotificaciones() {
        Long userId = SessionManager.getInstance().getUserId();
        Thread t = new Thread(() -> {
            try {
                List<AppDTO.NotificationResponse> notifs = ApiClient.getInstance()
                        .getList("/notifications/user/" + userId,
                                AppDTO.NotificationResponse.class);
                Platform.runLater(() -> notifTable.getItems().setAll(notifs));
            } catch (ApiClient.ApiException ex) {
                Platform.runLater(() ->
                        sidebarUserName.setText("Error: " + ex.getMessage()));
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

    @FXML private void goToProfile() {
        navigateTo("/com/hambooking/frontend/fxml/profile.fxml",
                "HamBooking - Mi Perfil");
    }

    @FXML private void handleLogout() {
        SessionManager.getInstance().clear();
        navigateTo("/com/hambooking/frontend/fxml/login.fxml",
                "HamBooking - Iniciar sesi\u00f3n");
    }

    // ── Utilidades ───────────────────────────────────────────────

    private String traducirTipo(String tipo) {
        if (tipo == null) return "";
        return switch (tipo) {
            case "CREATED"   -> "Creaci\u00f3n";
            case "MODIFIED"  -> "Modificaci\u00f3n";
            case "CANCELLED" -> "Cancelaci\u00f3n";
            case "REMINDER"  -> "Recordatorio";
            default          -> tipo;
        };
    }

    private void navigateTo(String fxmlPath, String title) {
        try {
            ViewManager.getInstance().navigateTo(fxmlPath, title);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
