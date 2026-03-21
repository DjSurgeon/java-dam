package com.hambooking.frontend.controllers;

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
import java.util.ResourceBundle;

/**
 * Controlador de la pantalla Calendario de disponibilidad.
 * Al hacer clic en un slot disponible navega a booking-form.fxml
 * pasando los datos del slot via BookingController.initData().
 */
public class CalendarController implements Initializable {

    // ── fx:id ────────────────────────────────────────────────────
    @FXML private ComboBox<String> servicioCombo;
    @FXML private DatePicker       fechaPicker;
    @FXML private GridPane         calendarGrid;
    @FXML private Label            servicioInfoLabel;
    @FXML private Label            legendInfoLabel;
    @FXML private Label            sidebarUserName;
    @FXML private Label            sidebarUserRole;

    private static final String[] SERVICIOS  = {
            "Corte de Jamon (2h)",
            "Corte de Paleta (1h)",
            "Corte de Embutido (30min)"
    };
    private static final int[]    DURACIONES = {120, 60, 30};
    private static final String[] PRECIOS    = {"50,00 EUR", "35,00 EUR", "25,00 EUR"};
    private static final String[] ESPECIALIDADES = {
            "Jamon Iberico", "Paleta Iberica", "Embutidos"
    };

    // Cortadores de ejemplo (en Issue #36 vendran de la API)
    private static final String[] CORTADORES = {
            "Carlos Martinez", "Ana Lopez", "Pedro Ruiz"
    };
    private static final Long[]   CORTADOR_IDS = {1L, 2L, 3L};

    private static final LocalTime HORA_INICIO = LocalTime.of(10, 0);
    private static final LocalTime HORA_FIN    = LocalTime.of(18, 0);

    // ── Inicializacion ───────────────────────────────────────────
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        servicioCombo.getItems().addAll(SERVICIOS);
        servicioCombo.getSelectionModel().selectFirst();

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

        servicioCombo.setOnAction(e -> actualizarInfoServicio());
        actualizarInfoServicio();
    }

    // ── Handlers ─────────────────────────────────────────────────

    @FXML
    private void handleBuscarDisponibilidad() {
        LocalDate fecha = fechaPicker.getValue();
        if (fecha == null) return;

        int servicioIdx  = servicioCombo.getSelectionModel().getSelectedIndex();
        int duracionMins = DURACIONES[servicioIdx];

        calendarGrid.getChildren().clear();
        calendarGrid.getColumnConstraints().clear();
        calendarGrid.getRowConstraints().clear();

        // Celda vacia (0,0)
        Label emptyHeader = new Label();
        emptyHeader.setPrefWidth(55);
        calendarGrid.add(emptyHeader, 0, 0);

        // Cabeceras de cortadores
        for (int col = 0; col < CORTADORES.length; col++) {
            VBox header = buildCarverHeader(CORTADORES[col]);
            calendarGrid.add(header, col + 1, 0);
        }

        // Filas de slots
        LocalTime slot = HORA_INICIO;
        int row = 1;
        while (slot.isBefore(HORA_FIN)) {
            Label horaLabel = new Label(slot.toString());
            horaLabel.getStyleClass().add("calendar-hour-label");
            horaLabel.setPrefWidth(55);
            calendarGrid.add(horaLabel, 0, row);

            for (int col = 0; col < CORTADORES.length; col++) {
                boolean suficiente = tieneEspacioSuficiente(slot, duracionMins);
                boolean ocupado    = esOcupado(col, row);

                // Capturamos variables finales para el lambda
                final int        colFinal         = col;
                final LocalTime  slotFinal        = slot;
                final int        servicioIdxFinal = servicioIdx;
                final int        duracionFinal    = duracionMins;

                Button slotBtn = buildSlotButton(suficiente, ocupado, slot,
                        () -> handleSlotSeleccionado(
                                slotFinal, slotFinal.plusMinutes(duracionFinal),
                                CORTADORES[colFinal], CORTADOR_IDS[colFinal],
                                SERVICIOS[servicioIdxFinal], PRECIOS[servicioIdxFinal],
                                ESPECIALIDADES[servicioIdxFinal],
                                (long)(servicioIdxFinal + 1),
                                fecha
                        )
                );
                calendarGrid.add(slotBtn, col + 1, row);
            }

            slot = slot.plusMinutes(30);
            row++;
        }

        legendInfoLabel.setText(
                "Servicio: " + SERVICIOS[servicioIdx] + "  |  " + PRECIOS[servicioIdx]
        );
    }

    // ── Navegacion ───────────────────────────────────────────────

    @FXML private void goToDashboard()     { navigateTo("/com/hambooking/frontend/fxml/client-dasboard.fxml", "HamBooking"); }
    @FXML private void goToReservations()  { /* TODO Issue #34 */ }
    @FXML private void goToProfile()       { /* TODO */ }
    @FXML private void goToNotifications() { /* TODO */ }

    @FXML
    private void handleLogout() {
        navigateTo("/com/hambooking/frontend/fxml/login.fxml", "HamBooking - Iniciar sesion");
    }

    // ── Logica de slots ──────────────────────────────────────────

    /**
     * Se ejecuta al hacer clic en un slot disponible.
     * Carga booking-form.fxml e inyecta los datos del slot.
     */
    private void handleSlotSeleccionado(LocalTime horaInicio,
                                        LocalTime horaFin,
                                        String cortador,
                                        Long cortadorId,
                                        String servicio,
                                        String precio,
                                        String especialidad,
                                        Long servicioId,
                                        LocalDate fecha) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/hambooking/frontend/fxml/booking-form.fxml")
            );
            Parent root = loader.load();

            // Inyectar datos en el BookingController
            BookingController ctrl = loader.getController();
            ctrl.initData(servicio, precio, cortador, especialidad,
                    fecha, horaInicio, horaFin,
                    cortadorId, servicioId);

            Stage stage = (Stage) calendarGrid.getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.setTitle("HamBooking - Confirmar Reserva");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ── Construccion de componentes del grid ─────────────────────

    private VBox buildCarverHeader(String nombre) {
        VBox box = new VBox(2);
        box.getStyleClass().add("calendar-carver-header");
        box.setPrefWidth(140);
        Label nameLabel = new Label(nombre);
        nameLabel.setStyle("-fx-font-weight:bold; -fx-font-size:12px;");
        Label subLabel = new Label("max 3/dia");
        subLabel.setStyle("-fx-font-size:10px; -fx-text-fill:#9A7B6A;");
        box.getChildren().addAll(nameLabel, subLabel);
        return box;
    }

    private Button buildSlotButton(boolean suficiente, boolean ocupado,
                                   LocalTime hora, Runnable onClickAction) {
        Button btn = new Button();
        btn.setPrefWidth(140);
        btn.setPrefHeight(30);

        if (!suficiente) {
            btn.setText("-");
            btn.getStyleClass().add("slot-insufficient");
            btn.setDisable(true);
        } else if (ocupado) {
            btn.setText("Ocupado");
            btn.getStyleClass().add("slot-occupied");
            btn.setDisable(true);
        } else {
            btn.setText("Libre");
            btn.getStyleClass().add("slot-available");
            btn.setOnAction(e -> onClickAction.run());
        }
        return btn;
    }

    private boolean esOcupado(int col, int row) {
        return (col == 0 && (row == 1 || row == 2 || row == 3 || row == 4))
                || (col == 2 && (row == 3 || row == 4 || row == 7 || row == 8));
    }

    private boolean tieneEspacioSuficiente(LocalTime slot, int duracionMins) {
        return !slot.plusMinutes(duracionMins).isAfter(HORA_FIN);
    }

    private void actualizarInfoServicio() {
        int idx = servicioCombo.getSelectionModel().getSelectedIndex();
        if (idx >= 0) {
            servicioInfoLabel.setText(SERVICIOS[idx] + " | " + PRECIOS[idx]);
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