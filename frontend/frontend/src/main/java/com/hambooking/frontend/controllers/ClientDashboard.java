package com.hambooking.frontend.controllers;

import com.hambooking.frontend.SessionManager;
import com.hambooking.frontend.dto.AppDTO;
import com.hambooking.frontend.service.ApiClient;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

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
        // Sidebar
        SessionManager session = SessionManager.getInstance();
        sidebarUserName.setText(session.getFullName());
        sidebarUserRole.setText("Cliente");

        // Fecha de hoy
        String fechaHoy = LocalDate.now().format(
                DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy", new Locale("es", "ES"))
        );
        fechaHoyLabel.setText("Hoy es " + fechaHoy);

        // Configurar columnas proximas
        colFecha.setCellValueFactory(   d -> new SimpleStringProperty(formatFecha(d.getValue().reservationDate)));
        colServicio.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().serviceName));
        colCortador.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCarverFullName()));
        colHora.setCellValueFactory(    d -> new SimpleStringProperty(d.getValue().getHoraStr()));
        colEstado.setCellValueFactory(  d -> new SimpleStringProperty(d.getValue().status));
        colAcciones.setCellValueFactory(d -> new SimpleStringProperty("Modificar / Cancelar"));

        // Configurar columnas historial
        hColFecha.setCellValueFactory(   d -> new SimpleStringProperty(formatFecha(d.getValue().reservationDate)));
        hColServicio.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().serviceName));
        hColCortador.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCarverFullName()));
        hColHora.setCellValueFactory(    d -> new SimpleStringProperty(d.getValue().getHoraStr()));
        hColEstado.setCellValueFactory(  d -> new SimpleStringProperty(d.getValue().status));

        // Cargar reservas desde la API
        cargarReservas();
    }

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

                // KPIs
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
                        fechaHoyLabel.setText("Error al cargar reservas: " + e.getMessage())
                );
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    // ── Navegacion ───────────────────────────────────────────────

    @FXML private void goToCalendar() {
        navigateTo("/com/hambooking/frontend/fxml/calendar.fxml", "HamBooking - Nueva Reserva");
    }

    @FXML private void goToReservations()  { /* TODO */ }
    @FXML private void goToProfile()       { /* TODO */ }
    @FXML private void goToNotifications() { /* TODO */ }

    @FXML
    private void handleLogout() {
        SessionManager.getInstance().clear();
        navigateTo("/com/hambooking/frontend/fxml/login.fxml", "HamBooking - Iniciar sesion");
    }

    // ── Utilidades ───────────────────────────────────────────────

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