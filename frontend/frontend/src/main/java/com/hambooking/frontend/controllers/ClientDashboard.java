package com.hambooking.frontend.controllers;

import com.hambooking.frontend.SessionManager;
import com.hambooking.frontend.dto.AppDTO;
import com.hambooking.frontend.service.ApiClient;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ClientDashboard implements Initializable {

    // ── Sidebar ──────────────────────────────────────────────────
    @FXML private Label sidebarUserName;
    @FXML private Label sidebarUserRole;

    // ── Cabecera ─────────────────────────────────────────────────
    @FXML private Label fechaHoyLabel;

    // ── KPIs ─────────────────────────────────────────────────────
    @FXML private Label kpiSemana;
    @FXML private Label kpiCupoHoy;
    @FXML private Label kpiRealizadas;

    // ── Tabla proximas ───────────────────────────────────────────
    @FXML private TableView<AppDTO.ReservationResponse>           proximasTable;
    @FXML private TableColumn<AppDTO.ReservationResponse, String> colFecha;
    @FXML private TableColumn<AppDTO.ReservationResponse, String> colServicio;
    @FXML private TableColumn<AppDTO.ReservationResponse, String> colCortador;
    @FXML private TableColumn<AppDTO.ReservationResponse, String> colHora;
    @FXML private TableColumn<AppDTO.ReservationResponse, String> colEstado;
    @FXML private TableColumn<AppDTO.ReservationResponse, String> colAcciones;

    // ── Tabla historial ──────────────────────────────────────────
    @FXML private TableView<AppDTO.ReservationResponse>           historialTable;
    @FXML private TableColumn<AppDTO.ReservationResponse, String> hColFecha;
    @FXML private TableColumn<AppDTO.ReservationResponse, String> hColServicio;
    @FXML private TableColumn<AppDTO.ReservationResponse, String> hColCortador;
    @FXML private TableColumn<AppDTO.ReservationResponse, String> hColHora;
    @FXML private TableColumn<AppDTO.ReservationResponse, String> hColEstado;

    private static final DateTimeFormatter FMT_FECHA =
            DateTimeFormatter.ofPattern("dd MMM yyyy", new Locale("es", "ES"));

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        SessionManager session = SessionManager.getInstance();
        sidebarUserName.setText(session.getFullName());
        sidebarUserRole.setText("Cliente");

        String fechaHoy = LocalDate.now().format(
                DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy", new Locale("es", "ES")));
        fechaHoyLabel.setText("Hoy es " + fechaHoy);

        configurarTablaProximas();
        configurarTablaHistorial();
        cargarReservas();
    }

    // ── Configuracion de tablas ──────────────────────────────────

    private void configurarTablaProximas() {
        colFecha.setCellValueFactory(d -> new SimpleStringProperty(
                formatFecha(d.getValue().reservationDate)));
        colServicio.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().serviceName != null ? d.getValue().serviceName : ""));
        colCortador.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getCarverFullName()));
        colHora.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getHoraStr()));
        colEstado.setCellValueFactory(d -> new SimpleStringProperty(
                traducirEstado(d.getValue().status)));
        colAcciones.setCellFactory(accionesFactory());
    }

    private void configurarTablaHistorial() {
        hColFecha.setCellValueFactory(d -> new SimpleStringProperty(
                formatFecha(d.getValue().reservationDate)));
        hColServicio.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().serviceName != null ? d.getValue().serviceName : ""));
        hColCortador.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getCarverFullName()));
        hColHora.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getHoraStr()));
        hColEstado.setCellValueFactory(d -> new SimpleStringProperty(
                traducirEstado(d.getValue().status)));
    }

    // ── Factory de botones para proximas ─────────────────────────

    private Callback<TableColumn<AppDTO.ReservationResponse, String>,
            TableCell<AppDTO.ReservationResponse, String>> accionesFactory() {

        return col -> new TableCell<>() {
            private final Button btnCancelar = new Button("Cancelar");
            private final HBox   box         = new HBox(6, btnCancelar);

            {
                box.setPadding(new Insets(2, 0, 2, 0));
                btnCancelar.setStyle(
                        "-fx-font-size:11px; -fx-padding:3 8 3 8;" +
                                "-fx-background-color:#e74c3c; -fx-text-fill:white;");

                btnCancelar.setOnAction(e -> {
                    AppDTO.ReservationResponse r = getTableView().getItems().get(getIndex());
                    cancelarReserva(r);
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                    return;
                }
                AppDTO.ReservationResponse r = getTableView().getItems().get(getIndex());
                // Solo mostrar cancelar si es PENDING o CONFIRMED y fecha futura
                boolean cancelable = ("PENDING".equals(r.status) || "CONFIRMED".equals(r.status))
                        && r.reservationDate != null
                        && !r.reservationDate.isBefore(LocalDate.now());
                if (cancelable) {
                    setGraphic(box);
                } else {
                    setGraphic(null);
                }
            }
        };
    }

    // ── Logica de cancelacion ─────────────────────────────────────

    private void cancelarReserva(AppDTO.ReservationResponse reserva) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Cancelar reserva");
        confirm.setHeaderText("\u00bfCancelar tu reserva de " + reserva.serviceName + "?");
        confirm.setContentText("Fecha: " + formatFecha(reserva.reservationDate)
                + "\nHora: " + reserva.getHoraStr()
                + "\n\nEsta acci\u00f3n no se puede deshacer.");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                Thread t = new Thread(() -> {
                    try {
                        ApiClient.getInstance().patch("/reservations/" + reserva.id + "/cancel");
                        Platform.runLater(() -> {
                            mostrarAlerta(Alert.AlertType.INFORMATION,
                                    "\u00c9xito", "Reserva cancelada correctamente.");
                            cargarReservas();
                        });
                    } catch (ApiClient.ApiException ex) {
                        Platform.runLater(() ->
                                mostrarAlerta(Alert.AlertType.ERROR, "Error", ex.getMessage()));
                    }
                });
                t.setDaemon(true);
                t.start();
            }
        });
    }

    // ── Carga de datos ───────────────────────────────────────────

    private void cargarReservas() {
        Long clientId = SessionManager.getInstance().getUserId();

        Thread thread = new Thread(() -> {
            try {
                List<AppDTO.ReservationResponse> todas = ApiClient.getInstance()
                        .getList("/reservations/client/" + clientId,
                                AppDTO.ReservationResponse.class);

                LocalDate hoy = LocalDate.now();

                List<AppDTO.ReservationResponse> proximas = todas.stream()
                        .filter(r -> r.reservationDate != null
                                && !r.reservationDate.isBefore(hoy)
                                && !"CANCELLED".equals(r.status)
                                && !"COMPLETED".equals(r.status))
                        .collect(Collectors.toList());

                List<AppDTO.ReservationResponse> historial = todas.stream()
                        .filter(r -> r.reservationDate != null
                                && (r.reservationDate.isBefore(hoy)
                                || "CANCELLED".equals(r.status)
                                || "COMPLETED".equals(r.status)))
                        .collect(Collectors.toList());

                long semana = proximas.stream()
                        .filter(r -> !r.reservationDate.isBefore(hoy)
                                && !r.reservationDate.isAfter(hoy.plusDays(6)))
                        .count();

                long hoyCount = proximas.stream()
                        .filter(r -> r.reservationDate.equals(hoy))
                        .count();

                long realizadas = todas.stream()
                        .filter(r -> "COMPLETED".equals(r.status))
                        .count();

                Platform.runLater(() -> {
                    proximasTable.getItems().setAll(proximas);
                    historialTable.getItems().setAll(historial);
                    kpiSemana.setText(String.valueOf(semana));
                    kpiCupoHoy.setText(hoyCount + " / 2");
                    kpiRealizadas.setText(String.valueOf(realizadas));
                });

            } catch (ApiClient.ApiException e) {
                Platform.runLater(() ->
                        fechaHoyLabel.setText("Error al cargar: " + e.getMessage()));
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    // ── Navegacion ───────────────────────────────────────────────

    @FXML private void goToCalendar() {
        navigateTo("/com/hambooking/frontend/fxml/calendar.fxml",
                "HamBooking - Nueva Reserva");
    }

    @FXML private void goToReservations() {
        // Historial ya visible en la misma pantalla — scroll a la tabla
        historialTable.requestFocus();
    }

    @FXML private void goToProfile() {
        mostrarAlerta(Alert.AlertType.INFORMATION,
                "Perfil", "Funcionalidad disponible en una pr\u00f3xima versi\u00f3n.");
    }

    @FXML private void goToNotifications() {
        mostrarAlerta(Alert.AlertType.INFORMATION,
                "Notificaciones", "Funcionalidad disponible en una pr\u00f3xima versi\u00f3n.");
    }

    @FXML private void handleLogout() {
        SessionManager.getInstance().clear();
        navigateTo("/com/hambooking/frontend/fxml/login.fxml",
                "HamBooking - Iniciar sesion");
    }

    // ── Utilidades ───────────────────────────────────────────────

    private String traducirEstado(String status) {
        if (status == null) return "";
        return switch (status) {
            case "PENDING"   -> "Pendiente";
            case "CONFIRMED" -> "Confirmada";
            case "COMPLETED" -> "Realizada";
            case "CANCELLED" -> "Cancelada";
            default          -> status;
        };
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private String formatFecha(LocalDate date) {
        return date != null ? date.format(FMT_FECHA) : "";
    }

    private void navigateTo(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) proximasTable.getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.setTitle(title);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}