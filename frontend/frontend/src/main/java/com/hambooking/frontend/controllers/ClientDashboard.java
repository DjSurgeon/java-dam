package com.hambooking.frontend.controllers;

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
import java.util.ResourceBundle;

/**
 * Controlador del dashboard del cliente.
 *
 * fx:id declarados en client-dasboard.fxml:
 *   Sidebar:
 *     - sidebarUserName, sidebarUserRole
 *   Cabecera:
 *     - fechaHoyLabel
 *   KPIs:
 *     - kpiSemana, kpiCupoHoy, kpiRealizadas
 *   Tabla proximas reservas:
 *     - proximasTable
 *     - colFecha, colServicio, colCortador, colHora, colEstado, colAcciones
 *   Tabla historial:
 *     - historialTable
 *     - hColFecha, hColServicio, hColCortador, hColHora, hColEstado
 *
 * onAction declarados:
 *   - #goToCalendar, #goToReservations, #goToProfile
 *   - #goToNotifications, #handleLogout
 */
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

    // ── Tabla: proximas reservas ──────────────────────────────────
    @FXML private TableView<String[]>         proximasTable;
    @FXML private TableColumn<String[], String> colFecha;
    @FXML private TableColumn<String[], String> colServicio;
    @FXML private TableColumn<String[], String> colCortador;
    @FXML private TableColumn<String[], String> colHora;
    @FXML private TableColumn<String[], String> colEstado;
    @FXML private TableColumn<String[], String> colAcciones;

    // ── Tabla: historial ─────────────────────────────────────────
    @FXML private TableView<String[]>         historialTable;
    @FXML private TableColumn<String[], String> hColFecha;
    @FXML private TableColumn<String[], String> hColServicio;
    @FXML private TableColumn<String[], String> hColCortador;
    @FXML private TableColumn<String[], String> hColHora;
    @FXML private TableColumn<String[], String> hColEstado;

    // ── Inicializacion ───────────────────────────────────────────
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Fecha de hoy en la cabecera
        String fechaHoy = LocalDate.now().format(
                DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy",
                        new java.util.Locale("es", "ES"))
        );
        fechaHoyLabel.setText("Hoy es " + fechaHoy);

        // Configurar columnas de proximas reservas
        colFecha.setCellValueFactory(   data -> new SimpleStringProperty(data.getValue()[0]));
        colServicio.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[1]));
        colCortador.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[2]));
        colHora.setCellValueFactory(    data -> new SimpleStringProperty(data.getValue()[3]));
        colEstado.setCellValueFactory(  data -> new SimpleStringProperty(data.getValue()[4]));
        colAcciones.setCellValueFactory(data -> new SimpleStringProperty("Modificar / Cancelar"));

        // Configurar columnas de historial
        hColFecha.setCellValueFactory(   data -> new SimpleStringProperty(data.getValue()[0]));
        hColServicio.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[1]));
        hColCortador.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[2]));
        hColHora.setCellValueFactory(    data -> new SimpleStringProperty(data.getValue()[3]));
        hColEstado.setCellValueFactory(  data -> new SimpleStringProperty(data.getValue()[4]));

        // Cargar datos de ejemplo
        // TODO Issue #36: sustituir por llamada a GET /api/reservations/my
        cargarDatosEjemplo();
    }

    // ── Carga de datos ───────────────────────────────────────────

    private void cargarDatosEjemplo() {
        // Proximas reservas
        proximasTable.getItems().addAll(
                new String[]{"22 Ene 2026", "Corte de Jamon",   "Carlos Martinez", "10:00 - 12:00", "Confirmada"},
                new String[]{"24 Ene 2026", "Corte de Paleta",  "Ana Lopez",       "14:30 - 15:30", "Pendiente"}
        );

        // Historial
        historialTable.getItems().addAll(
                new String[]{"10 Ene 2026", "Corte de Embutido", "Pedro Ruiz",       "16:30 - 17:00", "Realizada"},
                new String[]{"05 Ene 2026", "Corte de Jamon",    "Carlos Martinez",  "10:00 - 12:00", "Realizada"},
                new String[]{"20 Dic 2025", "Corte de Paleta",   "Ana Lopez",        "13:00 - 14:00", "Cancelada"}
        );

        // KPIs
        kpiSemana.setText("1");
        kpiCupoHoy.setText("1 / 2");
        kpiRealizadas.setText("5");
    }

    // ── Navegacion ───────────────────────────────────────────────

    @FXML
    private void goToCalendar() {
        navigateTo("/com/hambooking/frontend/fxml/calendar.fxml", "HamBooking - Nueva Reserva");
    }

    @FXML
    private void goToReservations() {
        // TODO Issue #34: pantalla de historial completo
    }

    @FXML
    private void goToProfile() {
        // TODO
    }

    @FXML
    private void goToNotifications() {
        // TODO
    }

    @FXML
    private void handleLogout() {
        navigateTo("/com/hambooking/frontend/fxml/login.fxml", "HamBooking - Iniciar sesion");
    }

    // ── Utilidades ───────────────────────────────────────────────

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