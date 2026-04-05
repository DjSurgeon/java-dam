package com.hambooking.frontend.controllers;

import com.hambooking.frontend.SessionManager;
import com.hambooking.frontend.dto.AppDTO;
import com.hambooking.frontend.service.ApiClient;
import com.hambooking.frontend.util.ViewManager;
import javafx.application.Platform;
import javafx.concurrent.Task;
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

/**
 * Controlador para la vista del calendario de disponibilidad.
 * Permite a los clientes consultar los huecos libres de los cortadores activos para un servicio
 * y fecha determinados, generando una cuadrícula interactiva para iniciar el proceso de reserva.
 */
public class CalendarController implements Initializable {

    /** Selector desplegable para el tipo de servicio. */
    @FXML private ComboBox<String> servicioCombo;
    /** Selector de fecha para la consulta de disponibilidad. */
    @FXML private DatePicker fechaPicker;
    /** Cuadrícula donde se renderizan dinámicamente los slots de tiempo y cortadores. */
    @FXML private GridPane calendarGrid;
    /** Etiqueta que detalla información del servicio seleccionado (precio, duración). */
    @FXML private Label servicioInfoLabel;
    /** Etiqueta para mensajes de estado, advertencias o errores en la búsqueda. */
    @FXML private Label legendInfoLabel;
    /** Nombre del usuario actual en el panel lateral. */
    @FXML private Label sidebarUserName;
    /** Rol del usuario actual en el panel lateral. */
    @FXML private Label sidebarUserRole;

    /** Lista de servicios disponibles recuperados de la API. */
    private List<AppDTO.ServiceResponse> servicios;
    /** Lista de cortadores activos recuperados de la API. */
    private List<AppDTO.CarverResponse> cortadores;

    /** Hora de inicio de la jornada laboral (10:00). */
    private static final LocalTime HORA_INICIO = LocalTime.of(10, 0);
    /** Hora de fin de la jornada laboral (18:00). */
    private static final LocalTime HORA_FIN    = LocalTime.of(18, 0);

    /**
     * Inicializa la interfaz configurando el usuario, las restricciones de fecha
     * y disparando la carga inicial de datos desde la API.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        SessionManager session = SessionManager.getInstance();
        sidebarUserName.setText(session.getFullName());
        sidebarUserRole.setText(session.isAdmin() ? "Administrador" : "Cliente");

        configurarRestriccionesFecha();
        cargarDatosIniciales();
    }

    /**
     * Configura el DatePicker para impedir la selección de fechas pasadas o fines de semana.
     */
    private void configurarRestriccionesFecha() {
        fechaPicker.setValue(LocalDate.now().plusDays(1));
        fechaPicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                DayOfWeek day = date.getDayOfWeek();
                boolean deshabilitado = day == DayOfWeek.SATURDAY
                        || day == DayOfWeek.SUNDAY
                        || date.isBefore(LocalDate.now().plusDays(1));
                setDisable(deshabilitado);
                if (deshabilitado) setStyle("-fx-background-color: #F2F3F4;");
            }
        });
    }

    /**
     * Recupera la lista de servicios y cortadores activos de forma asíncrona.
     */
    private void cargarDatosIniciales() {
        servicioInfoLabel.setText("Cargando servicios...");

        Task<Void> initTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                servicios = ApiClient.getInstance().getList("/services", AppDTO.ServiceResponse.class);
                cortadores = ApiClient.getInstance().getList("/carvers/active", AppDTO.CarverResponse.class);
                return null;
            }
        };

        initTask.setOnSucceeded(e -> {
            poblarComboServicios();
            servicioInfoLabel.setText("Servicios cargados.");
        });

        initTask.setOnFailed(e -> {
            servicioInfoLabel.setText("Error al cargar datos: " + initTask.getException().getMessage());
        });

        new Thread(initTask).start();
    }

    /**
     * Rellena el ComboBox con los nombres de los servicios recuperados.
     */
    private void poblarComboServicios() {
        servicioCombo.getItems().clear();
        for (AppDTO.ServiceResponse s : servicios) {
            servicioCombo.getItems().add(s.getDisplayName());
        }
        if (!servicioCombo.getItems().isEmpty()) {
            servicioCombo.getSelectionModel().selectFirst();
            actualizarInfoServicio();
        }
        servicioCombo.setOnAction(e -> actualizarInfoServicio());
    }

    /**
     * Gestiona la búsqueda de disponibilidad para la fecha y servicio seleccionados.
     * Limpia la cuadrícula y lanza peticiones paralelas para cada cortador.
     */
    @FXML
    private void handleBuscarDisponibilidad() {
        LocalDate fecha = fechaPicker.getValue();
        int idx = servicioCombo.getSelectionModel().getSelectedIndex();

        if (fecha == null || servicios == null || cortadores == null || idx < 0) return;

        AppDTO.ServiceResponse servicio = servicios.get(idx);

        if (cortadores.isEmpty()) {
            legendInfoLabel.setText("⚠ No hay cortadores activos disponibles.");
            return;
        }

        limpiarCuadricula();
        prepararCabecerasCuadricula(servicio);

        for (int col = 0; col < cortadores.size(); col++) {
            consultarDisponibilidadCortador(col, cortadores.get(col), servicio, fecha);
        }
    }

    /**
     * Realiza la consulta asíncrona de slots libres para un cortador específico.
     * 
     * @param col      Índice de la columna en la cuadrícula.
     * @param carver   Datos del cortador.
     * @param servicio Servicio a prestar.
     * @param fecha    Fecha de la consulta.
     */
    private void consultarDisponibilidadCortador(int col, AppDTO.CarverResponse carver, 
                                                 AppDTO.ServiceResponse servicio, LocalDate fecha) {
        
        Task<List<LocalTime>> task = new Task<>() {
            @Override
            protected List<LocalTime> call() throws Exception {
                String endpoint = "/availability?carverId=" + carver.id
                        + "&date=" + fecha
                        + "&serviceId=" + servicio.id;
                return ApiClient.getInstance().getList(endpoint, LocalTime.class);
            }
        };

        task.setOnSucceeded(e -> renderColumnaSlots(col, carver, servicio, task.getValue(), fecha));
        task.setOnFailed(e -> legendInfoLabel.setText("Error en cortador " + carver.firstName + ": " + task.getException().getMessage()));

        new Thread(task).start();
    }

    /**
     * Renderiza los botones de tiempo para un cortador determinado.
     */
    private void renderColumnaSlots(int col, AppDTO.CarverResponse carver, AppDTO.ServiceResponse servicio,
                                    List<LocalTime> slotsLibres, LocalDate fecha) {
        LocalTime slot = HORA_INICIO;
        int row = 1;
        while (slot.isBefore(HORA_FIN)) {
            boolean suficiente = !slot.plusMinutes(servicio.durationMinutes).isAfter(HORA_FIN);
            boolean libre      = slotsLibres.contains(slot);

            final LocalTime slotFinal = slot;
            Button btn = buildSlotButton(suficiente, libre, () ->
                    handleSlotSeleccionado(slotFinal, slotFinal.plusMinutes(servicio.durationMinutes), carver, servicio, fecha)
            );
            calendarGrid.add(btn, col + 1, row);

            slot = slot.plusMinutes(30);
            row++;
        }
    }

    /**
     * Prepara las cabeceras de cortadores y las etiquetas laterales de hora en la cuadrícula.
     */
    private void prepararCabecerasCuadricula(AppDTO.ServiceResponse servicio) {
        legendInfoLabel.setText("Cargando disponibilidad...");

        // Esquina superior izquierda vacía
        Label emptyHeader = new Label();
        emptyHeader.setPrefWidth(55);
        calendarGrid.add(emptyHeader, 0, 0);

        // Cabeceras de cortadores
        for (int col = 0; col < cortadores.size(); col++) {
            calendarGrid.add(buildCarverHeader(cortadores.get(col)), col + 1, 0);
        }

        // Etiquetas de hora
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

        legendInfoLabel.setText("Servicio: " + servicio.name + " | " + servicio.getPrecioStr());
    }

    /**
     * Limpia todos los elementos y restricciones de la cuadrícula.
     */
    private void limpiarCuadricula() {
        calendarGrid.getChildren().clear();
        calendarGrid.getColumnConstraints().clear();
        calendarGrid.getRowConstraints().clear();
    }

    /**
     * Gestiona la selección de un hueco libre navegando al formulario de reserva.
     */
    private void handleSlotSeleccionado(LocalTime horaInicio, LocalTime horaFin,
                                        AppDTO.CarverResponse carver, AppDTO.ServiceResponse servicio,
                                        LocalDate fecha) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hambooking/frontend/fxml/booking-form.fxml"));
            Parent root = loader.load();

            BookingController ctrl = loader.getController();
            ctrl.initData(servicio.name, servicio.getPrecioStr(), carver.getDisplayName(),
                    carver.specialty != null ? carver.specialty : "General",
                    fecha, horaInicio, horaFin, carver.id, servicio.id);

            Stage stage = ViewManager.getInstance().getMainStage();
            stage.getScene().setRoot(root);
            stage.setTitle("HamBooking - Confirmar Reserva");

        } catch (IOException e) {
            legendInfoLabel.setText("Error al abrir el formulario.");
        }
    }

    // ── Navegación ───────────────────────────────────────────────

    /** Navega al panel principal del cliente. */
    @FXML private void goToDashboard() { navigateTo("/com/hambooking/frontend/fxml/client-dashboard.fxml", "HamBooking"); }
    /** Navega a la lista de reservas del cliente. */
    @FXML private void goToReservations() { navigateTo("/com/hambooking/frontend/fxml/client-dashboard.fxml", "HamBooking - Mis Reservas"); }
    /** Navega al perfil del usuario. */
    @FXML private void goToProfile() { navigateTo("/com/hambooking/frontend/fxml/profile.fxml", "HamBooking - Mi Perfil"); }
    /** Navega a la vista de notificaciones. */
    @FXML private void goToNotifications() { navigateTo("/com/hambooking/frontend/fxml/notifications.fxml", "HamBooking - Notificaciones"); }
    
    /** Cierra la sesión y vuelve al login. */
    @FXML private void handleLogout() {
        SessionManager.getInstance().clear();
        navigateTo("/com/hambooking/frontend/fxml/login.fxml", "HamBooking - Iniciar sesión");
    }

    // ── Utilidades de Construcción UI ────────────────────────────

    /** Crea el contenedor visual para la cabecera de un cortador. */
    private VBox buildCarverHeader(AppDTO.CarverResponse carver) {
        VBox box = new VBox(2);
        box.getStyleClass().add("calendar-carver-header");
        box.setPrefWidth(140);
        Label nameLabel = new Label(carver.getDisplayName());
        nameLabel.setStyle("-fx-font-weight:bold; -fx-font-size:12px;");
        Label subLabel = new Label(carver.specialty != null ? carver.specialty : "General");
        subLabel.setStyle("-fx-font-size:10px; -fx-text-fill:#9A7B6A;");
        box.getChildren().addAll(nameLabel, subLabel);
        return box;
    }

    /** Crea un botón para un slot de tiempo con el estilo adecuado según su estado. */
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

    /** Actualiza la etiqueta informativa del servicio seleccionado. */
    private void actualizarInfoServicio() {
        int idx = servicioCombo.getSelectionModel().getSelectedIndex();
        if (servicios != null && idx >= 0 && idx < servicios.size()) {
            AppDTO.ServiceResponse s = servicios.get(idx);
            servicioInfoLabel.setText(s.name + " | " + s.getPrecioStr());
        }
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
