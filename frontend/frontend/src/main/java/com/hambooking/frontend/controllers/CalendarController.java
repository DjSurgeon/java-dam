package com.hambooking.frontend.controllers;

import com.hambooking.frontend.SessionManager;
import com.hambooking.frontend.dto.AppDTO;
import com.hambooking.frontend.service.ApiClient;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.ResourceBundle;

public class CalendarController implements Initializable {

    @FXML private ComboBox<String> servicioCombo;
    @FXML private DatePicker       fechaPicker;
    @FXML private GridPane         calendarGrid;
    @FXML private Label            servicioInfoLabel;
    @FXML private Label            legendInfoLabel;
    @FXML private Label            sidebarUserName;
    @FXML private Label            sidebarUserRole;

    // Datos cargados desde la API
    private List<AppDTO.ServiceResponse> servicios;
    private List<AppDTO.CarverResponse>  cortadores;

    private static final LocalTime HORA_INICIO = LocalTime.of(10, 0);
    private static final LocalTime HORA_FIN    = LocalTime.of(18, 0);

    // ── Inicializacion ───────────────────────────────────────────
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Info del usuario en el sidebar
        SessionManager session = SessionManager.getInstance();
        sidebarUserName.setText(session.getFullName());
        sidebarUserRole.setText(session.isAdmin() ? "Administrador" : "Cliente");

        // Fecha minima: manana
        fechaPicker.setValue(LocalDate.now().plusDays(1));
        fechaPicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                DayOfWeek day = date.getDayOfWeek();
                boolean disabled = day == DayOfWeek.SATURDAY
                        || day == DayOfWeek.SUNDAY
                        || date.isBefore(LocalDate.now().plusDays(1));
                setDisable(disabled);
                if (disabled) setStyle("-fx-background-color: #F2F3F4;");
            }
        });

        // Cargar servicios y cortadores desde la API
        cargarDatosIniciales();
    }

    // ── Carga inicial desde API ──────────────────────────────────

    private void cargarDatosIniciales() {
        servicioInfoLabel.setText("Cargando servicios...");

        Thread thread = new Thread(() -> {
            try {
                List<AppDTO.ServiceResponse> svcs = ApiClient.getInstance()
                        .getList("/services", AppDTO.ServiceResponse.class);
                List<AppDTO.CarverResponse> crvs = ApiClient.getInstance()
                        .getList("/carvers/active", AppDTO.CarverResponse.class);

                Platform.runLater(() -> {
                    this.servicios   = svcs;
                    this.cortadores  = crvs;

                    servicioCombo.getItems().clear();
                    for (AppDTO.ServiceResponse s : svcs) {
                        servicioCombo.getItems().add(s.getDisplayName());
                    }
                    if (!servicioCombo.getItems().isEmpty()) {
                        servicioCombo.getSelectionModel().selectFirst();
                        actualizarInfoServicio();
                    }

                    servicioCombo.setOnAction(e -> actualizarInfoServicio());
                });

            } catch (ApiClient.ApiException e) {
                Platform.runLater(() ->
                        servicioInfoLabel.setText("Error al cargar datos: " + e.getMessage())
                );
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    // ── Buscar disponibilidad ────────────────────────────────────

    @FXML
    private void handleBuscarDisponibilidad() {
        LocalDate fecha = fechaPicker.getValue();
        if (fecha == null || servicios == null || cortadores == null) return;

        int idx = servicioCombo.getSelectionModel().getSelectedIndex();
        if (idx < 0 || idx >= servicios.size()) return;

        AppDTO.ServiceResponse servicio = servicios.get(idx);

        calendarGrid.getChildren().clear();
        calendarGrid.getColumnConstraints().clear();
        calendarGrid.getRowConstraints().clear();

        legendInfoLabel.setText("Cargando disponibilidad...");

        // Celda vacia (0,0)
        Label emptyHeader = new Label();
        emptyHeader.setPrefWidth(55);
        calendarGrid.add(emptyHeader, 0, 0);

        // Cabeceras de cortadores
        for (int col = 0; col < cortadores.size(); col++) {
            AppDTO.CarverResponse carver = cortadores.get(col);
            VBox header = buildCarverHeader(carver);
            calendarGrid.add(header, col + 1, 0);
        }

        // Para cada cortador cargar sus slots en paralelo
        for (int col = 0; col < cortadores.size(); col++) {
            final int colFinal = col;
            AppDTO.CarverResponse carver = cortadores.get(col);

            Thread thread = new Thread(() -> {
                try {
                    String endpoint = "/availability?carverId=" + carver.id
                            + "&date=" + fecha
                            + "&serviceId=" + servicio.id;

                    List<LocalTime> slotsLibres = ApiClient.getInstance()
                            .getList(endpoint, LocalTime.class);

                    Platform.runLater(() ->
                            renderColumnaSlots(colFinal, carver, servicio,
                                    slotsLibres, fecha)
                    );

                } catch (ApiClient.ApiException e) {
                    Platform.runLater(() ->
                            legendInfoLabel.setText("Error: " + e.getMessage())
                    );
                }
            });
            thread.setDaemon(true);
            thread.start();
        }

        legendInfoLabel.setText(
                "Servicio: " + servicio.name + "  |  " + servicio.getPrecioStr()
        );
    }

    // ── Renderizar columna de slots ──────────────────────────────

    private void renderColumnaSlots(int col,
                                    AppDTO.CarverResponse carver,
                                    AppDTO.ServiceResponse servicio,
                                    List<LocalTime> slotsLibres,
                                    LocalDate fecha) {
        LocalTime slot = HORA_INICIO;
        int row = 1;
        while (slot.isBefore(HORA_FIN)) {
            boolean suficiente = !slot.plusMinutes(servicio.durationMinutes).isAfter(HORA_FIN);
            boolean libre      = slotsLibres.contains(slot);

            final LocalTime slotFinal = slot;
            Button btn = buildSlotButton(suficiente, libre, () ->
                    handleSlotSeleccionado(slotFinal,
                            slotFinal.plusMinutes(servicio.durationMinutes),
                            carver, servicio, fecha)
            );
            calendarGrid.add(btn, col + 1, row);

            slot = slot.plusMinutes(30);
            row++;
        }

        // Anadir etiquetas de hora en la primera pasada (col 0)
        if (col == 0) {
            LocalTime h = HORA_INICIO;
            int r = 1;
            while (h.isBefore(HORA_FIN)) {
                Label horaLabel = new Label(h.toString());
                horaLabel.getStyleClass().add("calendar-hour-label");
                horaLabel.setPrefWidth(55);
                calendarGrid.add(horaLabel, 0, r);
                h = h.plusMinutes(30);
                r++;
            }
        }
    }

    // ── Slot seleccionado → booking-form ─────────────────────────

    private void handleSlotSeleccionado(LocalTime horaInicio,
                                        LocalTime horaFin,
                                        AppDTO.CarverResponse carver,
                                        AppDTO.ServiceResponse servicio,
                                        LocalDate fecha) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/hambooking/frontend/fxml/booking-form.fxml")
            );
            Parent root = loader.load();

            BookingController ctrl = loader.getController();
            ctrl.initData(
                    servicio.name,
                    servicio.getPrecioStr(),
                    carver.getDisplayName(),
                    carver.specialty != null ? carver.specialty : "General",
                    fecha, horaInicio, horaFin,
                    carver.id, servicio.id
            );

            Stage stage = (Stage) calendarGrid.getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.setTitle("HamBooking - Confirmar Reserva");

        } catch (IOException e) {
            legendInfoLabel.setText("Error al abrir el formulario.");
        }
    }

    // ── Navegacion ───────────────────────────────────────────────

    @FXML private void goToDashboard()     { navigateTo("/com/hambooking/frontend/fxml/client-dashboard.fxml", "HamBooking"); }
    @FXML private void goToReservations()  { /* TODO */ }
    @FXML private void goToProfile()       { /* TODO */ }
    @FXML private void goToNotifications() { /* TODO */ }
    @FXML private void handleLogout() {
        SessionManager.getInstance().clear();
        navigateTo("/com/hambooking/frontend/fxml/login.fxml", "HamBooking - Iniciar sesion");
    }

    // ── Utilidades ───────────────────────────────────────────────

    private VBox buildCarverHeader(AppDTO.CarverResponse carver) {
        VBox box = new VBox(2);
        box.getStyleClass().add("calendar-carver-header");
        box.setPrefWidth(140);
        Label nameLabel = new Label("Cortador #" + carver.id);
        nameLabel.setStyle("-fx-font-weight:bold; -fx-font-size:12px;");
        Label subLabel = new Label(carver.specialty != null ? carver.specialty : "General");
        subLabel.setStyle("-fx-font-size:10px; -fx-text-fill:#9A7B6A;");
        box.getChildren().addAll(nameLabel, subLabel);
        return box;
    }

    private Button buildSlotButton(boolean suficiente, boolean libre, Runnable onClick) {
        Button btn = new Button();
        btn.setPrefWidth(140);
        btn.setPrefHeight(30);

        if (!suficiente) {
            btn.setText("-");
            btn.getStyleClass().add("slot-insufficient");
            btn.setDisable(true);
        } else if (!libre) {
            btn.setText("Ocupado");
            btn.getStyleClass().add("slot-occupied");
            btn.setDisable(true);
        } else {
            btn.setText("Libre");
            btn.getStyleClass().add("slot-available");
            btn.setOnAction(e -> onClick.run());
        }
        return btn;
    }

    private void actualizarInfoServicio() {
        int idx = servicioCombo.getSelectionModel().getSelectedIndex();
        if (servicios != null && idx >= 0 && idx < servicios.size()) {
            AppDTO.ServiceResponse s = servicios.get(idx);
            servicioInfoLabel.setText(s.name + " | " + s.getPrecioStr());
        }
    }

    private void navigateTo(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) calendarGrid.getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.setTitle(title);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}