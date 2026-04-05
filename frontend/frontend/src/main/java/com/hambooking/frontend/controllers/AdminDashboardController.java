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
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controlador principal para el panel de administración.
 * Centraliza la gestión global del sistema: cortadores, usuarios, reservas y notificaciones.
 * Utiliza un diseño de pestañas (TabPane) para organizar los diferentes dominios de la aplicación.
 */
public class AdminDashboardController implements Initializable {

    // ── Sidebar ──────────────────────────────────────────────────────────
    /** Etiqueta que muestra el nombre del administrador actual. */
    @FXML private Label sidebarUserName;

    // ── Cabecera y Controles Globales ────────────────────────────────────
    /** Título dinámico de la página según la pestaña activa. */
    @FXML private Label pageTitle;
    /** Indicador de ruta (breadcrumb) de la sección actual. */
    @FXML private Label pageBreadcrumb;
    /** Campo de búsqueda global (pendiente de implementación de filtrado). */
    @FXML private TextField searchField;
    /** Botón para crear nuevos registros, cuyo comportamiento varía según la pestaña. */
    @FXML private Button btnNuevo;

    // ── KPIs (Indicadores Clave) ─────────────────────────────────────────
    /** Muestra el número total de cortadores activos. */
    @FXML private Label kpiCortadores;
    /** Muestra el número de reservas programadas para el día de hoy. */
    @FXML private Label kpiReservasHoy;
    /** Muestra el número total de clientes registrados. */
    @FXML private Label kpiClientes;
    /** Muestra el número de reservas pendientes de confirmación. */
    @FXML private Label kpiPendientes;

    // ── TabPane y Secciones ──────────────────────────────────────────────
    /** Contenedor principal de las pestañas de administración. */
    @FXML private TabPane mainTabPane;
    /** Pestaña de gestión de perfiles de cortadores. */
    @FXML private Tab tabCortadores;
    /** Pestaña de gestión de cuentas de usuario. */
    @FXML private Tab tabUsuarios;
    /** Pestaña de supervisión de todas las reservas del sistema. */
    @FXML private Tab tabReservas;
    /** Pestaña de registro histórico de notificaciones enviadas. */
    @FXML private Tab tabNotificaciones;
    /** Pestaña de visualización de estadísticas globales. */
    @FXML private Tab tabEstadisticas;

    // ── Tabla de Cortadores ──────────────────────────────────────────────
    @FXML private TableView<AppDTO.CarverResponse>           cortadoresTable;
    @FXML private TableColumn<AppDTO.CarverResponse, String> cColNombre;
    @FXML private TableColumn<AppDTO.CarverResponse, String> cColDni;
    @FXML private TableColumn<AppDTO.CarverResponse, String> cColEmail;
    @FXML private TableColumn<AppDTO.CarverResponse, String> cColEspecialidad;
    @FXML private TableColumn<AppDTO.CarverResponse, String> cColExperiencia;
    @FXML private TableColumn<AppDTO.CarverResponse, String> cColEstado;
    @FXML private TableColumn<AppDTO.CarverResponse, String> cColAcciones;

    // ── Tabla de Usuarios ────────────────────────────────────────────────
    @FXML private TableView<AppDTO.UserResponse>           usuariosTable;
    @FXML private TableColumn<AppDTO.UserResponse, String> uColNombre;
    @FXML private TableColumn<AppDTO.UserResponse, String> uColDni;
    @FXML private TableColumn<AppDTO.UserResponse, String> uColEmail;
    @FXML private TableColumn<AppDTO.UserResponse, String> uColTelefono;
    @FXML private TableColumn<AppDTO.UserResponse, String> uColEstado;
    @FXML private TableColumn<AppDTO.UserResponse, String> uColAcciones;

    // ── Tabla de Reservas ────────────────────────────────────────────────
    @FXML private TableView<AppDTO.ReservationResponse>           reservasTable;
    @FXML private TableColumn<AppDTO.ReservationResponse, String> rColFecha;
    @FXML private TableColumn<AppDTO.ReservationResponse, String> rColCliente;
    @FXML private TableColumn<AppDTO.ReservationResponse, String> rColCortador;
    @FXML private TableColumn<AppDTO.ReservationResponse, String> rColServicio;
    @FXML private TableColumn<AppDTO.ReservationResponse, String> rColHora;
    @FXML private TableColumn<AppDTO.ReservationResponse, String> rColEstado;
    @FXML private TableColumn<AppDTO.ReservationResponse, String> rColAcciones;

    // ── Tabla de Notificaciones ──────────────────────────────────────────
    @FXML private TableView<AppDTO.NotificationResponse>           notificacionesTable;
    @FXML private TableColumn<AppDTO.NotificationResponse, String> nColFecha;
    @FXML private TableColumn<AppDTO.NotificationResponse, String> nColDestinatario;
    @FXML private TableColumn<AppDTO.NotificationResponse, String> nColTipo;
    @FXML private TableColumn<AppDTO.NotificationResponse, String> nColAsunto;

    /** Formateador para fechas y horas en las tablas. */
    private static final DateTimeFormatter FMT_DATETIME =
            DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm", new Locale("es", "ES"));
    /** Formateador para fechas simples en las tablas. */
    private static final DateTimeFormatter FMT_FECHA =
            DateTimeFormatter.ofPattern("dd MMM yyyy", new Locale("es", "ES"));

    /**
     * Inicializa el controlador configurando el nombre del usuario, las columnas de las tablas
     * y disparando la carga masiva de datos desde la API.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        sidebarUserName.setText(SessionManager.getInstance().getFullName());
        
        configurarTablaCortadores();
        configurarTablaUsuarios();
        configurarTablaReservas();
        configurarTablaNotificaciones();
        
        cargarDatos();
    }

    // ── Región: Configuración de Columnas ────────────────────────────────

    private void configurarTablaCortadores() {
        cColNombre.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().firstName + " " + d.getValue().lastName));
        cColDni.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().dni != null ? d.getValue().dni : ""));
        cColEmail.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().email != null ? d.getValue().email : ""));
        cColEspecialidad.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().specialty != null ? d.getValue().specialty : "-"));
        cColExperiencia.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().experienceYears != null ? d.getValue().experienceYears + " años" : "0 años"));
        cColEstado.setCellValueFactory(d -> new SimpleStringProperty(Boolean.TRUE.equals(d.getValue().isActive) ? "Activo" : "Inactivo"));
        cColAcciones.setCellFactory(accionesCortadoresFactory());
    }

    private void configurarTablaUsuarios() {
        uColNombre.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().firstName + " " + d.getValue().lastName));
        uColDni.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().dni != null ? d.getValue().dni : ""));
        uColEmail.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().email != null ? d.getValue().email : ""));
        uColTelefono.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().phone != null ? d.getValue().phone : ""));
        uColEstado.setCellValueFactory(d -> new SimpleStringProperty(Boolean.TRUE.equals(d.getValue().isActive) ? "Activo" : "Inactivo"));
        uColAcciones.setCellFactory(accionesUsuariosFactory());
    }

    private void configurarTablaReservas() {
        rColFecha.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().reservationDate != null ? d.getValue().reservationDate.format(FMT_FECHA) : ""));
        rColCliente.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getClientFullName()));
        rColCortador.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCarverFullName()));
        rColServicio.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().serviceName != null ? d.getValue().serviceName : ""));
        rColHora.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getHoraStr()));
        rColEstado.setCellValueFactory(d -> new SimpleStringProperty(traducirEstado(d.getValue().status)));
        rColAcciones.setCellFactory(accionesReservasFactory());
    }

    private void configurarTablaNotificaciones() {
        nColFecha.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().sentAt != null ? d.getValue().sentAt.format(FMT_DATETIME) : ""));
        nColDestinatario.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().recipientEmail != null ? d.getValue().recipientEmail : ""));
        nColTipo.setCellValueFactory(d -> new SimpleStringProperty(traducirTipoNotif(d.getValue().notificationType)));
        nColAsunto.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().subject != null ? d.getValue().subject : ""));
    }

    // ── Región: Factorías de Celdas (Botones) ────────────────────────────

    private Callback<TableColumn<AppDTO.CarverResponse, String>, TableCell<AppDTO.CarverResponse, String>> accionesCortadoresFactory() {
        return col -> new TableCell<AppDTO.CarverResponse, String>() {
            private final Button btnEditar = new Button("Editar");
            private final Button btnToggle = new Button();
            private final HBox box = new HBox(6, btnEditar, btnToggle);
            {
                box.setPadding(new Insets(2, 0, 2, 0));
                btnEditar.setStyle("-fx-font-size:11px; -fx-padding:3 8 3 8;");
                btnEditar.setOnAction(e -> ejecutarEdicionCortador(getTableView().getItems().get(getIndex())));
                btnToggle.setOnAction(e -> {
                    AppDTO.CarverResponse c = getTableView().getItems().get(getIndex());
                    if (Boolean.TRUE.equals(c.isActive)) ejecutarDesactivacionCortador(c);
                    else ejecutarActivacionCortador(c);
                });
            }
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() < 0) { setGraphic(null); return; }
                AppDTO.CarverResponse c = getTableView().getItems().get(getIndex());
                boolean activo = Boolean.TRUE.equals(c.isActive);
                btnToggle.setText(activo ? "Desactivar" : "Activar");
                btnToggle.setStyle(activo ? "-fx-background-color:#e74c3c; -fx-text-fill:white; -fx-font-size:11px;" : "-fx-background-color:#27ae60; -fx-text-fill:white; -fx-font-size:11px;");
                setGraphic(box);
            }
        };
    }

    private Callback<TableColumn<AppDTO.UserResponse, String>, TableCell<AppDTO.UserResponse, String>> accionesUsuariosFactory() {
        return col -> new TableCell<AppDTO.UserResponse, String>() {
            private final Button btnToggle = new Button();
            private final HBox box = new HBox(6, btnToggle);
            {
                box.setPadding(new Insets(2, 0, 2, 0));
                btnToggle.setOnAction(e -> ejecutarToggleUsuario(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() < 0) { setGraphic(null); return; }
                boolean activo = Boolean.TRUE.equals(getTableView().getItems().get(getIndex()).isActive);
                btnToggle.setText(activo ? "Desactivar" : "Activar");
                btnToggle.setStyle(activo ? "-fx-background-color:#e74c3c; -fx-text-fill:white; -fx-font-size:11px;" : "-fx-background-color:#27ae60; -fx-text-fill:white; -fx-font-size:11px;");
                setGraphic(box);
            }
        };
    }

    private Callback<TableColumn<AppDTO.ReservationResponse, String>, TableCell<AppDTO.ReservationResponse, String>> accionesReservasFactory() {
        return col -> new TableCell<AppDTO.ReservationResponse, String>() {
            private final Button btnConfirmar = new Button("Confirmar");
            private final Button btnCancelar  = new Button("Cancelar");
            private final HBox box = new HBox(6, btnConfirmar, btnCancelar);
            {
                box.setPadding(new Insets(2, 0, 2, 0));
                btnConfirmar.setStyle("-fx-background-color:#27ae60; -fx-text-fill:white; -fx-font-size:11px;");
                btnCancelar.setStyle("-fx-background-color:#e74c3c; -fx-text-fill:white; -fx-font-size:11px;");
                btnConfirmar.setOnAction(e -> ejecutarConfirmacionReserva(getTableView().getItems().get(getIndex())));
                btnCancelar.setOnAction(e -> ejecutarCancelacionReserva(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() < 0) { setGraphic(null); return; }
                AppDTO.ReservationResponse r = getTableView().getItems().get(getIndex());
                if (r.reservationDate != null && r.reservationDate.isBefore(LocalDate.now())) { setGraphic(null); return; }
                btnConfirmar.setVisible("PENDING".equals(r.status));
                btnConfirmar.setManaged("PENDING".equals(r.status));
                boolean cancelable = "PENDING".equals(r.status) || "CONFIRMED".equals(r.status);
                btnCancelar.setVisible(cancelable);
                btnCancelar.setManaged(cancelable);
                setGraphic(box);
            }
        };
    }

    // ── Región: Lógica de Acciones (Cortadores) ──────────────────────────

    /**
     * Muestra el diálogo de edición para un perfil de cortador y guarda los cambios.
     */
    private void ejecutarEdicionCortador(AppDTO.CarverResponse carver) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Editar cortador");
        dialog.setHeaderText(carver.firstName + " " + carver.lastName);

        TextField tfEspecialidad = new TextField(carver.specialty != null ? carver.specialty : "");
        TextField tfExperiencia  = new TextField(String.valueOf(carver.experienceYears != null ? carver.experienceYears : 0));
        TextField tfMaxJamones   = new TextField(String.valueOf(carver.maxHamsPerDay != null ? carver.maxHamsPerDay : 3));

        VBox content = new VBox(8, new Label("Especialidad:"), tfEspecialidad, 
                                   new Label("Años de experiencia:"), tfExperiencia, 
                                   new Label("Máx. jamones por día:"), tfMaxJamones);
        content.setPadding(new Insets(16));
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    int exp = Integer.parseInt(tfExperiencia.getText().trim());
                    int max = Integer.parseInt(tfMaxJamones.getText().trim());
                    
                    String body = String.format("{\"specialty\":\"%s\",\"experienceYears\":%d,\"maxHamsPerDay\":%d}",
                            tfEspecialidad.getText().trim(), exp, max);

                    Task<Void> task = new Task<>() {
                        @Override protected Void call() throws Exception {
                            ApiClient.getInstance().put("/carvers/" + carver.id, body);
                            return null;
                        }
                    };
                    task.setOnSucceeded(e -> { AlertHelper.showInfo("Éxito", "Cortador actualizado."); cargarDatos(); });
                    task.setOnFailed(e -> AlertHelper.showError("Error", task.getException().getMessage()));
                    new Thread(task).start();

                } catch (NumberFormatException ex) {
                    AlertHelper.showWarning("Datos inválidos", "Introduce números válidos en los campos numéricos.");
                }
            }
        });
    }

    private void ejecutarDesactivacionCortador(AppDTO.CarverResponse carver) {
        AlertHelper.showConfirmation("Desactivar cortador", "¿Desactivar a " + carver.firstName + " " + carver.lastName + "?",
                "El cortador no aparecerá disponible para nuevas reservas.")
        .ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                lanzarTareaPatch("/carvers/" + carver.id + "/deactivate", "Cortador desactivado.");
            }
        });
    }

    private void ejecutarActivacionCortador(AppDTO.CarverResponse carver) {
        lanzarTareaPatch("/carvers/" + carver.id + "/activate", "Cortador activado.");
    }

    // ── Región: Lógica de Acciones (Usuarios y Reservas) ─────────────────

    private void ejecutarToggleUsuario(AppDTO.UserResponse user) {
        boolean activar = !Boolean.TRUE.equals(user.isActive);
        AlertHelper.showConfirmation((activar ? "Activar" : "Desactivar") + " usuario",
                "¿Deseas " + (activar ? "activar" : "desactivar") + " a " + user.firstName + " " + user.lastName + "?", null)
        .ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                String endpoint = "/users/" + user.id + (activar ? "/activate" : "/deactivate");
                lanzarTareaPatch(endpoint, "Usuario " + (activar ? "activado" : "desactivado") + ".");
            }
        });
    }

    private void ejecutarConfirmacionReserva(AppDTO.ReservationResponse reserva) {
        AlertHelper.showConfirmation("Confirmar reserva", "¿Confirmar la reserva de " + reserva.getClientFullName() + "?",
                "Servicio: " + reserva.serviceName + "\nFecha: " + formatFecha(reserva.reservationDate))
        .ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                lanzarTareaPatch("/reservations/" + reserva.id + "/confirm", "Reserva confirmada.");
            }
        });
    }

    private void ejecutarCancelacionReserva(AppDTO.ReservationResponse reserva) {
        AlertHelper.showConfirmation("Cancelar reserva", "¿Cancelar la reserva de " + reserva.getClientFullName() + "?",
                "Esta acción no se puede deshacer.")
        .ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                lanzarTareaPatch("/reservations/" + reserva.id + "/cancel", "Reserva cancelada.");
            }
        });
    }

    /**
     * Utilidad genérica para lanzar tareas PATCH asíncronas.
     */
    private void lanzarTareaPatch(String endpoint, String successMsg) {
        Task<Void> task = new Task<>() {
            @Override protected Void call() throws Exception {
                ApiClient.getInstance().patch(endpoint);
                return null;
            }
        };
        task.setOnSucceeded(e -> { AlertHelper.showInfo("Éxito", successMsg); cargarReservasIndependiente(); });
        task.setOnFailed(e -> AlertHelper.showError("Error", task.getException().getMessage()));
        new Thread(task).start();
    }

    // ── Región: Carga de Datos ───────────────────────────────────────────

    /**
     * Realiza una carga masiva de todos los dominios del administrador de forma asíncrona.
     */
    private void cargarDatos() {
        Task<Object[]> loadTask = new Task<>() {
            @Override protected Object[] call() throws Exception {
                return new Object[] {
                    ApiClient.getInstance().getList("/carvers", AppDTO.CarverResponse.class),
                    ApiClient.getInstance().getList("/users", AppDTO.UserResponse.class),
                    ApiClient.getInstance().getList("/reservations", AppDTO.ReservationResponse.class),
                    ApiClient.getInstance().getList("/notifications", AppDTO.NotificationResponse.class)
                };
            }
        };

        loadTask.setOnSucceeded(e -> procesarCargaMasiva(loadTask.getValue()));
        loadTask.setOnFailed(e -> pageTitle.setText("Error al cargar: " + loadTask.getException().getMessage()));

        new Thread(loadTask).start();
    }

    /**
     * Refresca solo los datos de reservas y KPIs (útil tras acciones puntuales).
     */
    private void cargarReservasIndependiente() {
        cargarDatos(); // Por ahora reutilizamos la carga masiva para mantener consistencia.
    }

    @SuppressWarnings("unchecked")
    private void procesarCargaMasiva(Object[] datos) {
        List<AppDTO.CarverResponse> cortadores = (List<AppDTO.CarverResponse>) datos[0];
        List<AppDTO.UserResponse> usuarios = (List<AppDTO.UserResponse>) datos[1];
        List<AppDTO.ReservationResponse> reservas = (List<AppDTO.ReservationResponse>) datos[2];
        List<AppDTO.NotificationResponse> notificaciones = (List<AppDTO.NotificationResponse>) datos[3];

        cortadoresTable.getItems().setAll(cortadores);
        usuariosTable.getItems().setAll(usuarios.stream().filter(u -> !"ADMIN".equals(u.role)).toList());
        reservasTable.getItems().setAll(reservas);
        notificacionesTable.getItems().setAll(notificaciones);

        actualizarKPIs(cortadores, usuarios, reservas);
    }

    private void actualizarKPIs(List<AppDTO.CarverResponse> c, List<AppDTO.UserResponse> u, List<AppDTO.ReservationResponse> r) {
        long activos    = c.stream().filter(carver -> Boolean.TRUE.equals(carver.isActive)).count();
        long clientes   = u.stream().filter(user -> "CLIENT".equals(user.role)).count();
        long hoy        = r.stream().filter(res -> res.reservationDate != null && res.reservationDate.equals(LocalDate.now())).count();
        long pendientes = r.stream().filter(res -> "PENDING".equals(res.status)).count();

        kpiCortadores.setText(String.valueOf(activos));
        kpiClientes.setText(String.valueOf(clientes));
        kpiReservasHoy.setText(String.valueOf(hoy));
        kpiPendientes.setText(String.valueOf(pendientes));
    }

    // ── Región: Navegación y Pestañas ────────────────────────────────────

    @FXML private void showTabCortadores() { selectTab(tabCortadores, "Gestión de Cortadores", "Inicio · Cortadores"); }
    @FXML private void showTabUsuarios()   { selectTab(tabUsuarios, "Gestión de Usuarios", "Inicio · Usuarios"); }
    @FXML private void showTabReservas()   { selectTab(tabReservas, "Todas las Reservas", "Inicio · Reservas"); }
    @FXML private void showTabNotificaciones() { selectTab(tabNotificaciones, "Notificaciones", "Inicio · Notificaciones"); }
    @FXML private void showTabEstadisticas()   { selectTab(tabEstadisticas, "Estadísticas", "Inicio · Estadísticas"); }

    private void selectTab(Tab tab, String title, String breadcrumb) {
        mainTabPane.getSelectionModel().select(tab);
        pageTitle.setText(title);
        pageBreadcrumb.setText(breadcrumb);
    }

    @FXML private void handleNuevo() {
        if (mainTabPane.getSelectionModel().getSelectedItem() == tabCortadores) {
            ejecutarNuevoCortador();
        }
    }

    private void ejecutarNuevoCortador() {
        // Obtenemos usuarios activos que no son cortadores
        List<Long> idsYaCortadores = cortadoresTable.getItems().stream().map(c -> c.userId).toList();
        List<AppDTO.UserResponse> disponibles = usuariosTable.getItems().stream()
                .filter(u -> !idsYaCortadores.contains(u.id) && Boolean.TRUE.equals(u.isActive)).toList();

        if (disponibles.isEmpty()) {
            AlertHelper.showInfo("Sin usuarios", "Todos los usuarios activos ya son cortadores.");
            return;
        }

        // Construcción del diálogo de creación
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Nuevo cortador");
        ComboBox<AppDTO.UserResponse> cbUser = new ComboBox<>();
        cbUser.getItems().addAll(disponibles);
        cbUser.setPrefWidth(300);
        
        VBox content = new VBox(8, new Label("Selecciona Usuario:"), cbUser);
        content.setPadding(new Insets(16));
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK && cbUser.getValue() != null) {
                Task<Void> task = new Task<>() {
                    @Override protected Void call() throws Exception {
                        java.util.Map<String, Object> b = new java.util.LinkedHashMap<>();
                        b.put("userId", cbUser.getValue().id);
                        b.put("specialty", "General");
                        b.put("experienceYears", 0);
                        b.put("maxHamsPerDay", 3);
                        ApiClient.getInstance().post("/carvers", b, AppDTO.CarverResponse.class);
                        return null;
                    }
                };
                task.setOnSucceeded(e -> { AlertHelper.showInfo("Éxito", "Cortador creado."); cargarDatos(); });
                task.setOnFailed(e -> AlertHelper.showError("Error", task.getException().getMessage()));
                new Thread(task).start();
            }
        });
    }

    @FXML private void handleLogout() {
        SessionManager.getInstance().clear();
        try {
            ViewManager.getInstance().navigateTo("/com/hambooking/frontend/fxml/login.fxml", "HamBooking - Iniciar sesión");
        } catch (IOException e) { e.printStackTrace(); }
    }

    // ── Región: Utilidades ───────────────────────────────────────────────

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
            case "CREATED"   -> "Creación";
            case "MODIFIED"  -> "Modificación";
            case "CANCELLED" -> "Cancelación";
            case "REMINDER"  -> "Recordatorio";
            default          -> tipo;
        };
    }

    private String formatFecha(LocalDate date) { return date != null ? date.format(FMT_FECHA) : ""; }
}
