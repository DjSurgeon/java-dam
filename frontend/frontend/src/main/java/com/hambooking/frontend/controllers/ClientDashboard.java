package com.hambooking.frontend.controllers;

import com.hambooking.frontend.SessionManager;
import com.hambooking.frontend.dto.AppDTO;
import com.hambooking.frontend.service.ApiClient;
import com.hambooking.frontend.util.AlertHelper;
import com.hambooking.frontend.util.ViewManager;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Controlador para el panel principal del cliente (Dashboard).
 * Gestiona la visualización de las próximas reservas, el historial del usuario,
 * los indicadores clave de rendimiento (KPIs) y la cancelación de citas.
 */
public class ClientDashboard implements Initializable {

    // ── Sidebar ──────────────────────────────────────────────────
    /** Nombre completo del usuario en el panel lateral. */
    @FXML private Label sidebarUserName;
    /** Rol actual del usuario ("Cliente"). */
    @FXML private Label sidebarUserRole;

    // ── Cabecera ─────────────────────────────────────────────────
    /** Etiqueta que muestra la fecha actual formateada. */
    @FXML private Label fechaHoyLabel;

    // ── KPIs ─────────────────────────────────────────────────────
    /** Indicador de total de reservas para la semana actual. */
    @FXML private Label kpiSemana;
    /** Indicador de cupo de reservas utilizadas hoy. */
    @FXML private Label kpiCupoHoy;
    /** Indicador de total de reservas ya completadas históricamente. */
    @FXML private Label kpiRealizadas;

    // ── Tabla proximas ───────────────────────────────────────────
    /** Tabla para mostrar las reservas activas (pendientes o confirmadas). */
    @FXML private TableView<AppDTO.ReservationResponse> proximasTable;
    @FXML private TableColumn<AppDTO.ReservationResponse, String> colFecha;
    @FXML private TableColumn<AppDTO.ReservationResponse, String> colServicio;
    @FXML private TableColumn<AppDTO.ReservationResponse, String> colCortador;
    @FXML private TableColumn<AppDTO.ReservationResponse, String> colHora;
    @FXML private TableColumn<AppDTO.ReservationResponse, String> colEstado;
    @FXML private TableColumn<AppDTO.ReservationResponse, String> colAcciones;

    // ── Tabla historial ──────────────────────────────────────────
    /** Tabla para mostrar reservas pasadas, canceladas o completadas. */
    @FXML private TableView<AppDTO.ReservationResponse> historialTable;
    @FXML private TableColumn<AppDTO.ReservationResponse, String> hColFecha;
    @FXML private TableColumn<AppDTO.ReservationResponse, String> hColServicio;
    @FXML private TableColumn<AppDTO.ReservationResponse, String> hColCortador;
    @FXML private TableColumn<AppDTO.ReservationResponse, String> hColHora;
    @FXML private TableColumn<AppDTO.ReservationResponse, String> hColEstado;

    /** Formateador estándar para la visualización de fechas en las tablas. */
    private static final DateTimeFormatter FMT_FECHA =
            DateTimeFormatter.ofPattern("dd MMM yyyy", new Locale("es", "ES"));

    /**
     * Inicializa el panel configurando los datos del usuario, las tablas y disparando
     * la carga asíncrona de las reservas desde el servidor.
     */
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

    // ── Configuración de tablas ──────────────────────────────────

    /**
     * Configura las columnas y factorías de la tabla de próximas reservas.
     */
    private void configurarTablaProximas() {
        colFecha.setCellValueFactory(d -> new SimpleStringProperty(formatFecha(d.getValue().reservationDate)));
        colServicio.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().serviceName != null ? d.getValue().serviceName : ""));
        colCortador.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCarverFullName()));
        colHora.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getHoraStr()));
        colEstado.setCellValueFactory(d -> new SimpleStringProperty(traducirEstado(d.getValue().status)));
        colAcciones.setCellFactory(accionesFactory());
    }

    /**
     * Configura las columnas de la tabla de historial de reservas.
     */
    private void configurarTablaHistorial() {
        hColFecha.setCellValueFactory(d -> new SimpleStringProperty(formatFecha(d.getValue().reservationDate)));
        hColServicio.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().serviceName != null ? d.getValue().serviceName : ""));
        hColCortador.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCarverFullName()));
        hColHora.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getHoraStr()));
        hColEstado.setCellValueFactory(d -> new SimpleStringProperty(traducirEstado(d.getValue().status)));
    }

    /**
     * Crea la factoría para la columna de acciones (botones de cancelación).
     */
    private Callback<TableColumn<AppDTO.ReservationResponse, String>,
            TableCell<AppDTO.ReservationResponse, String>> accionesFactory() {

        return col -> new TableCell<AppDTO.ReservationResponse, String>() {
            private final Button btnCancelar = new Button("Cancelar");
            private final HBox   box         = new HBox(6, btnCancelar);

            {
                box.setPadding(new Insets(2, 0, 2, 0));
                btnCancelar.setStyle("-fx-font-size:11px; -fx-padding:3 8 3 8; -fx-background-color:#e74c3c; -fx-text-fill:white;");
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
                // Solo cancelable si está PENDING/CONFIRMED y es fecha futura/hoy
                boolean cancelable = ("PENDING".equals(r.status) || "CONFIRMED".equals(r.status))
                        && r.reservationDate != null && !r.reservationDate.isBefore(LocalDate.now());
                setGraphic(cancelable ? box : null);
            }
        };
    }

    // ── Lógica de negocio ────────────────────────────────────────

    /**
     * Inicia el proceso de cancelación de una reserva tras confirmar con el usuario.
     */
    private void cancelarReserva(AppDTO.ReservationResponse reserva) {
        AlertHelper.showConfirmation("Cancelar reserva",
                "¿Cancelar tu reserva de " + reserva.serviceName + "?",
                "Fecha: " + formatFecha(reserva.reservationDate) + "\nHora: " + reserva.getHoraStr() + "\n\nEsta acción no se puede deshacer.")
        .ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                ejecutarTareaCancelacion(reserva.id);
            }
        });
    }

    /**
     * Ejecuta la petición PATCH para cancelar la reserva de forma asíncrona.
     */
    private void ejecutarTareaCancelacion(Long reservationId) {
        Task<Void> cancelTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                ApiClient.getInstance().patch("/reservations/" + reservationId + "/cancel");
                return null;
            }
        };

        cancelTask.setOnSucceeded(e -> {
            AlertHelper.showInfo("Éxito", "Reserva cancelada correctamente.");
            cargarReservas();
        });

        cancelTask.setOnFailed(e -> AlertHelper.showError("Error", cancelTask.getException().getMessage()));

        new Thread(cancelTask).start();
    }

    /**
     * Carga todas las reservas del cliente y actualiza las tablas y KPIs.
     */
    private void cargarReservas() {
        Long clientId = SessionManager.getInstance().getUserId();

        Task<List<AppDTO.ReservationResponse>> loadTask = new Task<>() {
            @Override
            protected List<AppDTO.ReservationResponse> call() throws Exception {
                return ApiClient.getInstance().getList("/reservations/client/" + clientId, AppDTO.ReservationResponse.class);
            }
        };

        loadTask.setOnSucceeded(e -> procesarDatosReservas(loadTask.getValue()));
        loadTask.setOnFailed(e -> fechaHoyLabel.setText("Error al cargar: " + loadTask.getException().getMessage()));

        new Thread(loadTask).start();
    }

    /**
     * Clasifica las reservas en próximas e historial y calcula los indicadores.
     */
    private void procesarDatosReservas(List<AppDTO.ReservationResponse> todas) {
        LocalDate hoy = LocalDate.now();

        List<AppDTO.ReservationResponse> proximas = todas.stream()
                .filter(r -> r.reservationDate != null && !r.reservationDate.isBefore(hoy)
                        && !"CANCELLED".equals(r.status) && !"COMPLETED".equals(r.status))
                .collect(Collectors.toList());

        List<AppDTO.ReservationResponse> historial = todas.stream()
                .filter(r -> r.reservationDate != null && (r.reservationDate.isBefore(hoy)
                        || "CANCELLED".equals(r.status) || "COMPLETED".equals(r.status)))
                .collect(Collectors.toList());

        long semana = proximas.stream().filter(r -> !r.reservationDate.isAfter(hoy.plusDays(6))).count();
        long hoyCount = proximas.stream().filter(r -> r.reservationDate.equals(hoy)).count();
        long realizadas = todas.stream().filter(r -> "COMPLETED".equals(r.status)).count();

        proximasTable.getItems().setAll(proximas);
        historialTable.getItems().setAll(historial);
        kpiSemana.setText(String.valueOf(semana));
        kpiCupoHoy.setText(hoyCount + " / 2");
        kpiRealizadas.setText(String.valueOf(realizadas));
    }

    // ── Navegación ───────────────────────────────────────────────

    @FXML private void goToCalendar() { navigateTo("/com/hambooking/frontend/fxml/calendar.fxml", "HamBooking - Nueva Reserva"); }
    @FXML private void goToReservations() { historialTable.requestFocus(); historialTable.scrollTo(0); }
    @FXML private void goToProfile() { navigateTo("/com/hambooking/frontend/fxml/profile.fxml", "HamBooking - Mi Perfil"); }
    @FXML private void goToNotifications() { navigateTo("/com/hambooking/frontend/fxml/notifications.fxml", "HamBooking - Notificaciones"); }

    @FXML private void handleLogout() {
        SessionManager.getInstance().clear();
        navigateTo("/com/hambooking/frontend/fxml/login.fxml", "HamBooking - Iniciar sesión");
    }

    // ── Utilidades ───────────────────────────────────────────────

    /** Traduce el estado técnico de la reserva a un término amigable para el usuario. */
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

    /** Formatea la fecha para su visualización. */
    private String formatFecha(LocalDate date) {
        return date != null ? date.format(FMT_FECHA) : "";
    }

    /** Método centralizado para la navegación a través del ViewManager. */
    private void navigateTo(String fxmlPath, String title) {
        try {
            ViewManager.getInstance().navigateTo(fxmlPath, title);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
