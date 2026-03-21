package com.hambooking.frontend.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Controlador del formulario de confirmacion de reserva.
 *
 * Flujo de uso:
 *   1. CalendarController crea un FXMLLoader para booking-form.fxml
 *   2. Llama a controller.initData(...) pasando los datos del slot
 *   3. Cambia la escena a este formulario
 *
 * fx:id declarados en booking-form.fxml:
 *   - lblServicio, lblPrecio
 *   - lblCortador, lblEspecialidad
 *   - lblFecha, lblHora
 *   - notasField
 *   - errorLabel
 *   - btnConfirmar
 *
 * onAction declarados:
 *   - #handleConfirmar
 *   - #handleCancelar
 */
public class BookingController implements Initializable {

    // ── fx:id ────────────────────────────────────────────────────
    @FXML private Label    lblServicio;
    @FXML private Label    lblPrecio;
    @FXML private Label    lblCortador;
    @FXML private Label    lblEspecialidad;
    @FXML private Label    lblFecha;
    @FXML private Label    lblHora;
    @FXML private TextArea notasField;
    @FXML private Label    errorLabel;
    @FXML private Button   btnConfirmar;

    // Datos del slot seleccionado (inyectados desde CalendarController)
    private String    servicio;
    private String    precio;
    private String    cortador;
    private String    especialidad;
    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private Long      cortadorId;
    private Long      servicioId;

    private static final DateTimeFormatter FMT_FECHA =
            DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy", new Locale("es", "ES"));
    private static final DateTimeFormatter FMT_HORA =
            DateTimeFormatter.ofPattern("HH:mm");

    // ── Inicializacion ───────────────────────────────────────────
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    /**
     * Inyecta los datos del slot seleccionado en el formulario.
     * Debe llamarse desde CalendarController justo despues de load().
     *
     * Ejemplo de uso en CalendarController:
     *   FXMLLoader loader = new FXMLLoader(getClass().getResource(".../booking-form.fxml"));
     *   Parent root = loader.load();
     *   BookingController ctrl = loader.getController();
     *   ctrl.initData("Corte de Jamon", "50,00 EUR", "Carlos Martinez",
     *                 "Jamon Iberico", fecha, LocalTime.of(10,0), LocalTime.of(12,0),
     *                 1L, 1L);
     *   stage.getScene().setRoot(root);
     */
    public void initData(String servicio,
                         String precio,
                         String cortador,
                         String especialidad,
                         LocalDate fecha,
                         LocalTime horaInicio,
                         LocalTime horaFin,
                         Long cortadorId,
                         Long servicioId) {
        this.servicio     = servicio;
        this.precio       = precio;
        this.cortador     = cortador;
        this.especialidad = especialidad;
        this.fecha        = fecha;
        this.horaInicio   = horaInicio;
        this.horaFin      = horaFin;
        this.cortadorId   = cortadorId;
        this.servicioId   = servicioId;

        // Actualizar labels con los datos recibidos
        lblServicio.setText(servicio);
        lblPrecio.setText(precio);
        lblCortador.setText(cortador);
        lblEspecialidad.setText("Especialidad: " + especialidad);
        lblFecha.setText(fecha.format(FMT_FECHA));
        lblHora.setText("De " + horaInicio.format(FMT_HORA)
                + " a " + horaFin.format(FMT_HORA)
                + "  (" + calcularDuracion() + ")");
    }

    // ── Handlers ─────────────────────────────────────────────────

    /**
     * Se ejecuta al pulsar "Confirmar reserva".
     * TODO Issue #35: llamar a POST /api/reservations con los datos.
     */
    @FXML
    private void handleConfirmar() {
        String notas = notasField.getText().trim();

        // Validacion: datos minimos presentes
        if (servicio == null || cortador == null || fecha == null) {
            showError("Faltan datos de la reserva. Vuelve al calendario.");
            return;
        }

        // TODO Issue #35 — ApiClient: POST /api/reservations
        // CreateReservationDTO dto = new CreateReservationDTO(
        //     cortadorId, servicioId, fecha, horaInicio, notas
        // );
        // ReservationResponseDTO response = reservationService.create(dto);
        // if (response != null) navigateTo(".../client-dasboard.fxml", "HamBooking");

        // Placeholder hasta Issue #35
        showError("Reserva pendiente de conexion con API (Issue #35)");
    }

    /**
     * Se ejecuta al pulsar "Volver al calendario" o la X.
     * Vuelve a la pantalla del calendario sin crear reserva.
     */
    @FXML
    private void handleCancelar() {
        navigateTo("/com/hambooking/frontend/fxml/calendar.fxml",
                "HamBooking - Nueva Reserva");
    }

    // ── Utilidades privadas ───────────────────────────────────────

    /** Calcula la duracion en horas/minutos para mostrar en el label. */
    private String calcularDuracion() {
        if (horaInicio == null || horaFin == null) return "";
        long minutos = java.time.Duration.between(horaInicio, horaFin).toMinutes();
        if (minutos >= 60 && minutos % 60 == 0) {
            return (minutos / 60) + " hora" + (minutos / 60 > 1 ? "s" : "");
        } else if (minutos >= 60) {
            return (minutos / 60) + "h " + (minutos % 60) + "min";
        } else {
            return minutos + " minutos";
        }
    }

    private void showError(String mensaje) {
        errorLabel.setText(mensaje);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void navigateTo(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) btnConfirmar.getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.setTitle(title);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}