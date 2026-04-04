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
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.ResourceBundle;

public class BookingController implements Initializable {

    @FXML private Label    lblServicio;
    @FXML private Label    lblPrecio;
    @FXML private Label    lblCortador;
    @FXML private Label    lblEspecialidad;
    @FXML private Label    lblFecha;
    @FXML private Label    lblHora;
    @FXML private TextArea notasField;
    @FXML private Label    errorLabel;
    @FXML private Button   btnConfirmar;

    // Datos inyectados desde CalendarController
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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    /**
     * Inyecta los datos del slot seleccionado.
     * Llamado desde CalendarController justo despues de load().
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

    @FXML
    private void handleConfirmar() {
        if (servicio == null || cortadorId == null || fecha == null) {
            showError("Faltan datos de la reserva. Vuelve al calendario.");
            return;
        }

        btnConfirmar.setDisable(true);
        btnConfirmar.setText("Confirmando...");

        String notas   = notasField.getText().trim();
        Long clientId  = SessionManager.getInstance().getUserId();

        Thread thread = new Thread(() -> {
            try {
                AppDTO.CreateReservationRequest request =
                        new AppDTO.CreateReservationRequest(
                                clientId, cortadorId, servicioId,
                                fecha, horaInicio,
                                notas.isEmpty() ? null : notas
                        );

                AppDTO.ReservationResponse response = ApiClient.getInstance()
                        .post("/reservations", request, AppDTO.ReservationResponse.class);

                // Exito: navegar al dashboard del cliente
                Platform.runLater(() ->
                        navigateTo("/com/hambooking/frontend/fxml/client-dashboard.fxml",
                                "HamBooking - Mi Panel")
                );

            } catch (ApiClient.ApiException e) {
                Platform.runLater(() -> {
                    showError(e.getMessage());
                    btnConfirmar.setDisable(false);
                    btnConfirmar.setText("Confirmar reserva");
                });
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    private void handleCancelar() {
        navigateTo("/com/hambooking/frontend/fxml/calendar.fxml",
                "HamBooking - Nueva Reserva");
    }

    // ── Utilidades ───────────────────────────────────────────────

    private String calcularDuracion() {
        if (horaInicio == null || horaFin == null) return "";
        long minutos = Duration.between(horaInicio, horaFin).toMinutes();
        if (minutos >= 60 && minutos % 60 == 0) {
            return (minutos / 60) + " hora" + (minutos / 60 > 1 ? "s" : "");
        } else if (minutos >= 60) {
            return (minutos / 60) + "h " + (minutos % 60) + "min";
        }
        return minutos + " minutos";
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
            showError("Error al cargar la pantalla.");
        }
    }
}