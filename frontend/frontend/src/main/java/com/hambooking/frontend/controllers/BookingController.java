package com.hambooking.frontend.controllers;

import com.hambooking.frontend.SessionManager;
import com.hambooking.frontend.dto.AppDTO;
import com.hambooking.frontend.service.ApiClient;
import com.hambooking.frontend.util.ViewManager;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Controlador para la vista de confirmación de reserva.
 * Esta pantalla recibe los datos de un slot seleccionado en el calendario y permite
 * al usuario añadir notas opcionales antes de crear la reserva definitiva en el sistema.
 */
public class BookingController implements Initializable {

    /** Etiqueta que muestra el nombre del servicio seleccionado. */
    @FXML private Label lblServicio;
    /** Etiqueta que muestra el precio base del servicio. */
    @FXML private Label lblPrecio;
    /** Etiqueta que muestra el nombre del cortador asignado. */
    @FXML private Label lblCortador;
    /** Etiqueta que muestra la especialidad del cortador. */
    @FXML private Label lblEspecialidad;
    /** Etiqueta que muestra la fecha de la reserva. */
    @FXML private Label lblFecha;
    /** Etiqueta que muestra el rango horario de la reserva. */
    @FXML private Label lblHora;
    /** Campo de texto para que el usuario añada observaciones o peticiones especiales. */
    @FXML private TextArea notasField;
    /** Etiqueta para mostrar mensajes de error durante la confirmación. */
    @FXML private Label errorLabel;
    /** Botón para confirmar y enviar la reserva a la API. */
    @FXML private Button btnConfirmar;

    // ── Datos inyectados desde CalendarController ────────────────

    private String    servicio;
    private String    precio;
    private String    cortador;
    private String    especialidad;
    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private Long      cortadorId;
    private Long      servicioId;

    /** Formateador para fechas con nombre de día y mes en español. */
    private static final DateTimeFormatter FMT_FECHA =
            DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy", new Locale("es", "ES"));
    /** Formateador estándar para horas (HH:mm). */
    private static final DateTimeFormatter FMT_HORA =
            DateTimeFormatter.ofPattern("HH:mm");

    /**
     * Inicializa el estado visual de la etiqueta de error.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    /**
     * Inyecta y visualiza los datos del servicio y horario seleccionado.
     * Este método es invocado por el controlador del calendario antes de mostrar esta vista.
     *
     * @param servicio     Nombre del servicio.
     * @param precio       Precio formateado.
     * @param cortador     Nombre del cortador.
     * @param especialidad Especialidad del cortador.
     * @param fecha        Fecha elegida.
     * @param horaInicio   Hora de comienzo.
     * @param horaFin      Hora de finalización estimada.
     * @param cortadorId   ID único del cortador.
     * @param servicioId   ID único del servicio.
     */
    public void initData(String servicio, String precio,
                         String cortador, String especialidad,
                         LocalDate fecha, LocalTime horaInicio, LocalTime horaFin,
                         Long cortadorId, Long servicioId) {
        this.servicio     = servicio;
        this.precio       = precio;
        this.cortador     = cortador;
        this.especialidad = especialidad;
        this.fecha        = fecha;
        this.horaInicio   = horaInicio;
        this.horaFin      = horaFin;
        this.cortadorId   = cortadorId;
        this.servicioId   = servicioId;

        lblServicio.setText(servicio);
        lblPrecio.setText(precio);
        lblCortador.setText(cortador);
        lblEspecialidad.setText("Especialidad: " + especialidad);
        lblFecha.setText(fecha.format(FMT_FECHA));
        lblHora.setText("De " + horaInicio.format(FMT_HORA)
                + " a " + horaFin.format(FMT_HORA)
                + "  (" + calcularDuracion() + ")");
    }

    /**
     * Gestiona el clic en el botón de confirmación.
     * Lanza una tarea asíncrona para registrar la reserva en el backend.
     */
    @FXML
    private void handleConfirmar() {
        if (servicio == null || cortadorId == null || fecha == null) {
            showError("Faltan datos de la reserva. Vuelve al calendario.");
            return;
        }

        configurarEstadoCargando(true);

        Task<AppDTO.ReservationResponse> bookingTask = crearTareaReserva();

        bookingTask.setOnSucceeded(event -> {
            // Navegar al dashboard del cliente tras el éxito
            navigateTo("/com/hambooking/frontend/fxml/client-dashboard.fxml", "HamBooking - Mi Panel");
        });

        bookingTask.setOnFailed(event -> {
            showError(bookingTask.getException().getMessage());
            configurarEstadoCargando(false);
        });

        Thread thread = new Thread(bookingTask);
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Crea la tarea encargada de realizar la petición POST de reserva.
     */
    private Task<AppDTO.ReservationResponse> crearTareaReserva() {
        String notas   = notasField.getText().trim();
        Long clientId  = SessionManager.getInstance().getUserId();

        AppDTO.CreateReservationRequest request = new AppDTO.CreateReservationRequest(
                clientId, cortadorId, servicioId,
                fecha, horaInicio,
                notas.isEmpty() ? null : notas
        );

        return new Task<>() {
            @Override
            protected AppDTO.ReservationResponse call() throws Exception {
                return ApiClient.getInstance().post("/reservations", request, AppDTO.ReservationResponse.class);
            }
        };
    }

    /**
     * Cancela la operación actual y regresa al calendario de disponibilidad.
     */
    @FXML
    private void handleCancelar() {
        navigateTo("/com/hambooking/frontend/fxml/calendar.fxml", "HamBooking - Nueva Reserva");
    }

    /**
     * Calcula y formatea la duración de la reserva a partir de las horas de inicio y fin.
     * 
     * @return Cadena formateada (ej. "1 hora", "45 minutos").
     */
    private String calcularDuracion() {
        if (horaInicio == null || horaFin == null) return "";
        long minutos = Duration.between(horaInicio, horaFin).toMinutes();
        if (minutos >= 60 && minutos % 60 == 0) {
            long horas = minutos / 60;
            return horas + " hora" + (horas > 1 ? "s" : "");
        } else if (minutos >= 60) {
            return (minutos / 60) + "h " + (minutos % 60) + "min";
        }
        return minutos + " minutos";
    }

    /**
     * Cambia el estado de los controles durante la carga.
     */
    private void configurarEstadoCargando(boolean cargando) {
        btnConfirmar.setDisable(cargando);
        btnConfirmar.setText(cargando ? "Confirmando..." : "Confirmar reserva");
    }

    /**
     * Muestra un mensaje de error en la etiqueta correspondiente.
     */
    private void showError(String mensaje) {
        errorLabel.setText(mensaje);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    /**
     * Método centralizado para la navegación a través del ViewManager.
     */
    private void navigateTo(String fxmlPath, String title) {
        try {
            ViewManager.getInstance().navigateTo(fxmlPath, title);
        } catch (IOException e) {
            showError("Error al cargar la pantalla.");
        }
    }
}
