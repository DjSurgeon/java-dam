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
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
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
                        ? d.getValue().experienceYears + " a\u00f1os" : "0 a\u00f1os"));
        cColEstado.setCellValueFactory(d -> new SimpleStringProperty(
                Boolean.TRUE.equals(d.getValue().isActive) ? "Activo" : "Inactivo"));
        cColAcciones.setCellFactory(accionesCortadoresFactory());
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
        uColAcciones.setCellFactory(accionesUsuariosFactory());
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
        rColAcciones.setCellFactory(accionesReservasFactory());
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

    // ── Factories de botones ─────────────────────────────────────────────

    private Callback<TableColumn<AppDTO.CarverResponse, String>,
            TableCell<AppDTO.CarverResponse, String>> accionesCortadoresFactory() {

        return col -> new TableCell<>() {
            private final Button btnEditar = new Button("Editar");
            private final Button btnToggle = new Button();
            private final HBox   box       = new HBox(6, btnEditar, btnToggle);

            {
                box.setPadding(new Insets(2, 0, 2, 0));
                btnEditar.setStyle("-fx-font-size:11px; -fx-padding:3 8 3 8;");

                btnEditar.setOnAction(e -> {
                    AppDTO.CarverResponse c = getTableView().getItems().get(getIndex());
                    mostrarDialogoEditarCortador(c);
                });
                btnToggle.setOnAction(e -> {
                    AppDTO.CarverResponse c = getTableView().getItems().get(getIndex());
                    if (Boolean.TRUE.equals(c.isActive)) confirmarYDesactivarCortador(c);
                    else activarCortador(c);
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                    return;
                }
                AppDTO.CarverResponse c = getTableView().getItems().get(getIndex());
                boolean activo = Boolean.TRUE.equals(c.isActive);
                btnToggle.setText(activo ? "Desactivar" : "Activar");
                btnToggle.setStyle(activo
                        ? "-fx-font-size:11px; -fx-padding:3 8 3 8; -fx-background-color:#e74c3c; -fx-text-fill:white;"
                        : "-fx-font-size:11px; -fx-padding:3 8 3 8; -fx-background-color:#27ae60; -fx-text-fill:white;");
                setGraphic(box);
            }
        };
    }

    private Callback<TableColumn<AppDTO.UserResponse, String>,
            TableCell<AppDTO.UserResponse, String>> accionesUsuariosFactory() {

        return col -> new TableCell<>() {
            private final Button btnToggle = new Button();
            private final HBox   box       = new HBox(6, btnToggle);

            {
                box.setPadding(new Insets(2, 0, 2, 0));
                btnToggle.setOnAction(e -> {
                    AppDTO.UserResponse u = getTableView().getItems().get(getIndex());
                    toggleUsuario(u);
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                    return;
                }
                AppDTO.UserResponse u = getTableView().getItems().get(getIndex());
                boolean activo = Boolean.TRUE.equals(u.isActive);
                btnToggle.setText(activo ? "Desactivar" : "Activar");
                btnToggle.setStyle(activo
                        ? "-fx-font-size:11px; -fx-padding:3 8 3 8; -fx-background-color:#e74c3c; -fx-text-fill:white;"
                        : "-fx-font-size:11px; -fx-padding:3 8 3 8; -fx-background-color:#27ae60; -fx-text-fill:white;");
                setGraphic(box);
            }
        };
    }

    private Callback<TableColumn<AppDTO.ReservationResponse, String>,
            TableCell<AppDTO.ReservationResponse, String>> accionesReservasFactory() {

        return col -> new TableCell<>() {
            private final Button btnConfirmar = new Button("Confirmar");
            private final Button btnCancelar  = new Button("Cancelar");
            private final HBox   box          = new HBox(6, btnConfirmar, btnCancelar);

            {
                box.setPadding(new Insets(2, 0, 2, 0));
                btnConfirmar.setStyle(
                        "-fx-font-size:11px; -fx-padding:3 8 3 8; -fx-background-color:#27ae60; -fx-text-fill:white;");
                btnCancelar.setStyle(
                        "-fx-font-size:11px; -fx-padding:3 8 3 8; -fx-background-color:#e74c3c; -fx-text-fill:white;");

                btnConfirmar.setOnAction(e -> {
                    AppDTO.ReservationResponse r = getTableView().getItems().get(getIndex());
                    confirmarReserva(r);
                });
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
                // No mostrar botones si la fecha ya pasó
                boolean fechaPasada = r.reservationDate != null
                        && r.reservationDate.isBefore(java.time.LocalDate.now());
                if (fechaPasada) {
                    setGraphic(null);
                    return;
                }
                // Confirmar solo visible si PENDING
                btnConfirmar.setVisible("PENDING".equals(r.status));
                btnConfirmar.setManaged("PENDING".equals(r.status));
                // Cancelar visible si PENDING o CONFIRMED
                boolean cancelable = "PENDING".equals(r.status) || "CONFIRMED".equals(r.status);
                btnCancelar.setVisible(cancelable);
                btnCancelar.setManaged(cancelable);
                setGraphic(box);
            }
        };
    }

    // ── Logica de acciones ───────────────────────────────────────────────

    private void mostrarDialogoEditarCortador(AppDTO.CarverResponse carver) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Editar cortador");
        dialog.setHeaderText(carver.firstName + " " + carver.lastName);

        TextField tfEspecialidad = new TextField(carver.specialty != null ? carver.specialty : "");
        TextField tfExperiencia  = new TextField(
                carver.experienceYears != null ? String.valueOf(carver.experienceYears) : "0");
        TextField tfMaxJamones   = new TextField(
                carver.maxHamsPerDay != null ? String.valueOf(carver.maxHamsPerDay) : "3");

        tfEspecialidad.setPromptText("Especialidad");
        tfExperiencia.setPromptText("A\u00f1os de experiencia");
        tfMaxJamones.setPromptText("M\u00e1x. jamones/d\u00eda");

        VBox content = new VBox(8,
                new Label("Especialidad:"), tfEspecialidad,
                new Label("A\u00f1os de experiencia:"), tfExperiencia,
                new Label("M\u00e1x. jamones por d\u00eda:"), tfMaxJamones);
        content.setPadding(new Insets(16));
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                int exp = Integer.parseInt(tfExperiencia.getText().trim());
                int max = Integer.parseInt(tfMaxJamones.getText().trim());
                String body = String.format(
                        "{\"specialty\":\"%s\",\"experienceYears\":%d,\"maxHamsPerDay\":%d}",
                        tfEspecialidad.getText().trim(), exp, max);

                Thread t = new Thread(() -> {
                    try {
                        ApiClient.getInstance().put("/carvers/" + carver.id, body);
                        Platform.runLater(() -> {
                            mostrarAlerta(Alert.AlertType.INFORMATION,
                                    "\u00c9xito", "Cortador actualizado correctamente.");
                            cargarDatos();
                        });
                    } catch (ApiClient.ApiException ex) {
                        Platform.runLater(() ->
                                mostrarAlerta(Alert.AlertType.ERROR,
                                        "Error", "No se pudo actualizar: " + ex.getMessage()));
                    }
                });
                t.setDaemon(true);
                t.start();

            } catch (NumberFormatException ex) {
                mostrarAlerta(Alert.AlertType.WARNING, "Datos inv\u00e1lidos",
                        "Introduce n\u00fameros v\u00e1lidos en los campos num\u00e9ricos.");
            }
        }
    }

    private void confirmarYDesactivarCortador(AppDTO.CarverResponse carver) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Desactivar cortador");
        confirm.setHeaderText("\u00bfDesactivar a " + carver.firstName + " " + carver.lastName + "?");
        confirm.setContentText("El cortador no aparecer\u00e1 disponible para nuevas reservas.");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                Thread t = new Thread(() -> {
                    try {
                        ApiClient.getInstance().patch("/carvers/" + carver.id + "/deactivate");
                        Platform.runLater(() -> {
                            mostrarAlerta(Alert.AlertType.INFORMATION,
                                    "\u00c9xito", "Cortador desactivado.");
                            cargarDatos();
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

    private void activarCortador(AppDTO.CarverResponse carver) {
        Thread t = new Thread(() -> {
            try {
                ApiClient.getInstance().patch("/carvers/" + carver.id + "/activate");
                Platform.runLater(() -> {
                    mostrarAlerta(Alert.AlertType.INFORMATION, "\u00c9xito", "Cortador activado.");
                    cargarDatos();
                });
            } catch (ApiClient.ApiException ex) {
                Platform.runLater(() ->
                        mostrarAlerta(Alert.AlertType.ERROR, "Error", ex.getMessage()));
            }
        });
        t.setDaemon(true);
        t.start();
    }

    private void toggleUsuario(AppDTO.UserResponse user) {
        boolean activar = !Boolean.TRUE.equals(user.isActive);
        String accion   = activar ? "activar" : "desactivar";

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle((activar ? "Activar" : "Desactivar") + " usuario");
        confirm.setHeaderText("\u00bfDeseas " + accion + " a "
                + user.firstName + " " + user.lastName + "?");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                String endpoint = activar
                        ? "/users/" + user.id + "/activate"
                        : "/users/" + user.id + "/deactivate";
                Thread t = new Thread(() -> {
                    try {
                        ApiClient.getInstance().patch(endpoint);
                        Platform.runLater(() -> {
                            mostrarAlerta(Alert.AlertType.INFORMATION, "\u00c9xito",
                                    "Usuario " + (activar ? "activado" : "desactivado") + ".");
                            cargarDatos();
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

    private void confirmarReserva(AppDTO.ReservationResponse reserva) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar reserva");
        confirm.setHeaderText("\u00bfConfirmar la reserva de " + reserva.getClientFullName() + "?");
        confirm.setContentText("Servicio: " + reserva.serviceName
                + "\nFecha: " + (reserva.reservationDate != null
                ? reserva.reservationDate.format(FMT_FECHA) : "")
                + "\nHora: " + reserva.getHoraStr());
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                Thread t = new Thread(() -> {
                    try {
                        ApiClient.getInstance().patch("/reservations/" + reserva.id + "/confirm");
                        Platform.runLater(() -> {
                            mostrarAlerta(Alert.AlertType.INFORMATION,
                                    "\u00c9xito", "Reserva confirmada.");
                            cargarDatos();
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

    private void cancelarReserva(AppDTO.ReservationResponse reserva) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Cancelar reserva");
        confirm.setHeaderText("\u00bfCancelar la reserva de " + reserva.getClientFullName() + "?");
        confirm.setContentText("Esta acci\u00f3n no se puede deshacer.");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                Thread t = new Thread(() -> {
                    try {
                        ApiClient.getInstance().patch("/reservations/" + reserva.id + "/cancel");
                        Platform.runLater(() -> {
                            mostrarAlerta(Alert.AlertType.INFORMATION,
                                    "\u00c9xito", "Reserva cancelada.");
                            cargarDatos();
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

    // ── Carga de datos ───────────────────────────────────────────────────

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
                    usuariosTable.getItems().setAll(
                            usuarios.stream().filter(u -> !"ADMIN".equals(u.role)).toList());
                    reservasTable.getItems().setAll(reservas);
                    notificacionesTable.getItems().setAll(notificaciones);

                    long activos    = cortadores.stream().filter(c -> Boolean.TRUE.equals(c.isActive)).count();
                    long clientes   = usuarios.stream().filter(u -> "CLIENT".equals(u.role)).count();
                    long hoy        = reservas.stream()
                            .filter(r -> r.reservationDate != null
                                    && r.reservationDate.equals(java.time.LocalDate.now()))
                            .count();
                    long pendientes = reservas.stream().filter(r -> "PENDING".equals(r.status)).count();

                    kpiCortadores.setText(String.valueOf(activos));
                    kpiClientes.setText(String.valueOf(clientes));
                    kpiReservasHoy.setText(String.valueOf(hoy));
                    kpiPendientes.setText(String.valueOf(pendientes));
                });

            } catch (ApiClient.ApiException e) {
                Platform.runLater(() ->
                        pageTitle.setText("Error al cargar: " + e.getMessage()));
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    // ── Navegacion ───────────────────────────────────────────────────────

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

    @FXML private void handleNuevo() {
        Tab tabActivo = mainTabPane.getSelectionModel().getSelectedItem();
        if (tabActivo == tabCortadores) {
            System.out.println("TODO: dialogo nuevo cortador");
        } else if (tabActivo == tabUsuarios) {
            System.out.println("TODO: dialogo nuevo usuario");
        }
    }

    @FXML private void handleLogout() {
        SessionManager.getInstance().clear();
        navigateTo("/com/hambooking/frontend/fxml/login.fxml",
                "HamBooking - Iniciar sesi\u00f3n");
    }

    // ── Utilidades ───────────────────────────────────────────────────────

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

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