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
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class AdminDashboardController implements Initializable {

    // ── Sidebar ──────────────────────────────────────────────────────────
    @FXML private Label sidebarUserName;

    // ── Cabecera ─────────────────────────────────────────────────────────
    @FXML private Label     pageTitle;
    @FXML private Label     pageBreadcrumb;
    @FXML private TextField searchField;

    // ── KPIs ─────────────────────────────────────────────────────────────
    @FXML private Label kpiCortadores;
    @FXML private Label kpiReservasHoy;
    @FXML private Label kpiClientes;
    @FXML private Label kpiPendientes;

    // ── TabPane ──────────────────────────────────────────────────────────
    @FXML private TabPane mainTabPane;
    @FXML private Tab     tabCortadores;
    @FXML private Tab     tabUsuarios;
    @FXML private Tab     tabReservas;
    @FXML private Tab     tabNotificaciones;
    @FXML private Tab     tabEstadisticas;

    // ── Tabla cortadores ─────────────────────────────────────────────────
    @FXML private TableView<AppDTO.CarverResponse>           cortadoresTable;
    @FXML private TableColumn<AppDTO.CarverResponse, String> cColNombre;
    @FXML private TableColumn<AppDTO.CarverResponse, String> cColDni;
    @FXML private TableColumn<AppDTO.CarverResponse, String> cColEmail;
    @FXML private TableColumn<AppDTO.CarverResponse, String> cColEspecialidad;
    @FXML private TableColumn<AppDTO.CarverResponse, String> cColExperiencia;
    @FXML private TableColumn<AppDTO.CarverResponse, String> cColEstado;
    @FXML private TableColumn<AppDTO.CarverResponse, String> cColAcciones;

    // ── Tabla usuarios ───────────────────────────────────────────────────
    @FXML private TableView<AppDTO.UserResponse>           usuariosTable;
    @FXML private TableColumn<AppDTO.UserResponse, String> uColNombre;
    @FXML private TableColumn<AppDTO.UserResponse, String> uColDni;
    @FXML private TableColumn<AppDTO.UserResponse, String> uColEmail;
    @FXML private TableColumn<AppDTO.UserResponse, String> uColTelefono;
    @FXML private TableColumn<AppDTO.UserResponse, String> uColEstado;
    @FXML private TableColumn<AppDTO.UserResponse, String> uColAcciones;

    // ── Tabla reservas ───────────────────────────────────────────────────
    @FXML private TableView<AppDTO.ReservationResponse>           reservasTable;
    @FXML private TableColumn<AppDTO.ReservationResponse, String> rColFecha;
    @FXML private TableColumn<AppDTO.ReservationResponse, String> rColCliente;
    @FXML private TableColumn<AppDTO.ReservationResponse, String> rColCortador;
    @FXML private TableColumn<AppDTO.ReservationResponse, String> rColServicio;
    @FXML private TableColumn<AppDTO.ReservationResponse, String> rColHora;
    @FXML private TableColumn<AppDTO.ReservationResponse, String> rColEstado;
    @FXML private TableColumn<AppDTO.ReservationResponse, String> rColAcciones;

    // ── Tabla notificaciones ─────────────────────────────────────────────
    @FXML private TableView<AppDTO.NotificationResponse>           notificacionesTable;
    @FXML private TableColumn<AppDTO.NotificationResponse, String> nColFecha;
    @FXML private TableColumn<AppDTO.NotificationResponse, String> nColDestinatario;
    @FXML private TableColumn<AppDTO.NotificationResponse, String> nColTipo;
    @FXML private TableColumn<AppDTO.NotificationResponse, String> nColAsunto;

    private static final DateTimeFormatter FMT_DATETIME =
            DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm", new Locale("es", "ES"));
    private static final DateTimeFormatter FMT_FECHA =
            DateTimeFormatter.ofPattern("dd MMM yyyy", new Locale("es", "ES"));

    // ── Inicializacion ───────────────────────────────────────────────────
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        sidebarUserName.setText(SessionManager.getInstance().getFullName());

        configurarTablaCortadores();
        configurarTablaUsuarios();
        configurarTablaReservas();
        configurarTablaNotificaciones();

        cargarDatos();
    }

    // ── Configuracion de columnas ────────────────────────────────────────

    private void configurarTablaCortadores() {
        cColNombre.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().firstName + " " + d.getValue().lastName));
        cColDni.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().dni != null ? d.getValue().dni : ""));
        cColEmail.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().email != null ? d.getValue().email : ""));
        cColEspecialidad.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().specialty != null ? d.getValue().specialty : "-"));
        cColExperiencia.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().experienceYears != null
                        ? d.getValue().experienceYears + " a\u00f1os"
                        : "0 a\u00f1os"));
        cColEstado.setCellValueFactory(d -> new SimpleStringProperty(
                Boolean.TRUE.equals(d.getValue().isActive) ? "Activo" : "Inactivo"));
        cColAcciones.setCellValueFactory(d -> new SimpleStringProperty("Editar | Desactivar"));
    }

    private void configurarTablaUsuarios() {
        uColNombre.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().firstName + " " + d.getValue().lastName));
        uColDni.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().dni != null ? d.getValue().dni : ""));
        uColEmail.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().email != null ? d.getValue().email : ""));
        uColTelefono.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().phone != null ? d.getValue().phone : ""));
        uColEstado.setCellValueFactory(d -> new SimpleStringProperty(
                Boolean.TRUE.equals(d.getValue().isActive) ? "Activo" : "Inactivo"));
        uColAcciones.setCellValueFactory(d -> new SimpleStringProperty("Activar | Desactivar"));
    }

    private void configurarTablaReservas() {
        rColFecha.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().reservationDate != null
                        ? d.getValue().reservationDate.format(FMT_FECHA) : ""));
        rColCliente.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getClientFullName()));
        rColCortador.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getCarverFullName()));
        rColServicio.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().serviceName != null ? d.getValue().serviceName : ""));
        rColHora.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getHoraStr()));
        rColEstado.setCellValueFactory(d -> new SimpleStringProperty(
                traducirEstado(d.getValue().status)));
        rColAcciones.setCellValueFactory(d -> new SimpleStringProperty("Ver | Cancelar"));
    }

    private void configurarTablaNotificaciones() {
        nColFecha.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().sentAt != null ? d.getValue().sentAt.format(FMT_DATETIME) : ""));
        nColDestinatario.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().recipientEmail != null ? d.getValue().recipientEmail : ""));
        nColTipo.setCellValueFactory(d -> new SimpleStringProperty(
                traducirTipoNotif(d.getValue().notificationType)));
        nColAsunto.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().subject != null ? d.getValue().subject : ""));
    }

    // ── Carga de datos desde la API ──────────────────────────────────────

    private void cargarDatos() {
        Thread thread = new Thread(() -> {
            try {
                List<AppDTO.CarverResponse> cortadores = ApiClient.getInstance()
                        .getList("/carvers", AppDTO.CarverResponse.class);

                List<AppDTO.UserResponse> usuarios = ApiClient.getInstance()
                        .getList("/users", AppDTO.UserResponse.class);

                List<AppDTO.ReservationResponse> reservas = ApiClient.getInstance()
                        .getList("/reservations", AppDTO.ReservationResponse.class);

                List<AppDTO.NotificationResponse> notificaciones = ApiClient.getInstance()
                        .getList("/notifications", AppDTO.NotificationResponse.class);

                Platform.runLater(() -> {
                    cortadoresTable.getItems().setAll(cortadores);
                    // Solo mostrar clientes (no al propio admin)
                    usuariosTable.getItems().setAll(
                            usuarios.stream()
                                    .filter(u -> !"ADMIN".equals(u.role))
                                    .toList()
                    );
                    reservasTable.getItems().setAll(reservas);
                    notificacionesTable.getItems().setAll(notificaciones);

                    // KPIs calculados de los datos reales
                    long activos = cortadores.stream()
                            .filter(c -> Boolean.TRUE.equals(c.isActive)).count();
                    long clientes = usuarios.stream()
                            .filter(u -> "CLIENT".equals(u.role)).count();
                    long hoy = reservas.stream()
                            .filter(r -> r.reservationDate != null
                                    && r.reservationDate.equals(java.time.LocalDate.now()))
                            .count();
                    long pendientes = reservas.stream()
                            .filter(r -> "PENDING".equals(r.status))
                            .count();

                    kpiCortadores.setText(String.valueOf(activos));
                    kpiClientes.setText(String.valueOf(clientes));
                    kpiReservasHoy.setText(String.valueOf(hoy));
                    kpiPendientes.setText(String.valueOf(pendientes));
                });

            } catch (ApiClient.ApiException e) {
                Platform.runLater(() ->
                        pageTitle.setText("Error al cargar: " + e.getMessage())
                );
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    // ── Navegacion entre tabs ────────────────────────────────────────────

    @FXML private void showTabCortadores() {
        mainTabPane.getSelectionModel().select(tabCortadores);
        pageTitle.setText("Gesti\u00f3n de Cortadores");
        pageBreadcrumb.setText("Inicio \u00b7 Cortadores");
    }

    @FXML private void showTabUsuarios() {
        mainTabPane.getSelectionModel().select(tabUsuarios);
        pageTitle.setText("Gesti\u00f3n de Usuarios");
        pageBreadcrumb.setText("Inicio \u00b7 Usuarios");
    }

    @FXML private void showTabReservas() {
        mainTabPane.getSelectionModel().select(tabReservas);
        pageTitle.setText("Todas las Reservas");
        pageBreadcrumb.setText("Inicio \u00b7 Reservas");
    }

    @FXML private void showTabNotificaciones() {
        mainTabPane.getSelectionModel().select(tabNotificaciones);
        pageTitle.setText("Notificaciones");
        pageBreadcrumb.setText("Inicio \u00b7 Notificaciones");
    }

    @FXML private void showTabEstadisticas() {
        mainTabPane.getSelectionModel().select(tabEstadisticas);
        pageTitle.setText("Estad\u00edsticas");
        pageBreadcrumb.setText("Inicio \u00b7 Estad\u00edsticas");
    }

    // ── Acciones ─────────────────────────────────────────────────────────

    @FXML private void handleNuevo() {
        Tab tabActivo = mainTabPane.getSelectionModel().getSelectedItem();
        if (tabActivo == tabCortadores) {
            System.out.println("TODO: abrir dialogo nuevo cortador");
        } else if (tabActivo == tabUsuarios) {
            System.out.println("TODO: abrir dialogo nuevo usuario");
        }
    }

    @FXML private void handleLogout() {
        SessionManager.getInstance().clear();
        navigateTo("/com/hambooking/frontend/fxml/login.fxml", "HamBooking - Iniciar sesi\u00f3n");
    }

    // ── Utilidades ───────────────────────────────────────────────────────

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

    private String traducirTipoNotif(String tipo) {
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) mainTabPane.getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.setTitle(title);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}