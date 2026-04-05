package com.hambooking.frontend.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Optional;

/**
 * Utilidad estática para mostrar cuadros de diálogo y alertas estándar de JavaFX.
 * Mantiene la consistencia visual y reduce la duplicación de código en la UI.
 */
public class AlertHelper {

    /**
     * Constructor privado para evitar la instanciación (clase utilitaria estática).
     */
    private AlertHelper() {}

    /**
     * Muestra una alerta informativa al usuario.
     * Útil para mensajes de éxito o indicaciones no críticas.
     *
     * @param title   Título de la ventana de alerta.
     * @param message Contenido de la alerta con los detalles.
     */
    public static void showInfo(String title, String message) {
        showAlert(Alert.AlertType.INFORMATION, title, null, message);
    }

    /**
     * Muestra una alerta de error.
     * Se debe invocar cuando ocurre un fallo o excepción no recuperable en una operación.
     *
     * @param title   Título de la ventana de alerta (ej. "Error de red").
     * @param message Descripción clara del problema.
     */
    public static void showError(String title, String message) {
        showAlert(Alert.AlertType.ERROR, title, null, message);
    }

    /**
     * Muestra una alerta de advertencia.
     * Indica una acción que el usuario debería revisar o que no se pudo completar idealmente.
     *
     * @param title   Título de la ventana de alerta.
     * @param message Motivo de la advertencia.
     */
    public static void showWarning(String title, String message) {
        showAlert(Alert.AlertType.WARNING, title, null, message);
    }

    /**
     * Muestra un diálogo de confirmación solicitando al usuario aprobar o rechazar una acción.
     * Se bloqueará el hilo de la UI hasta que el usuario escoja una opción.
     *
     * @param title   Título de la ventana de alerta.
     * @param header  Cabecera de texto (opcional, si es null o vacío no se mostrará cabecera destacada).
     * @param message Descripción de la acción destructiva o importante.
     * @return Un {@code Optional<ButtonType>} con el botón pulsado por el usuario (ej. ButtonType.OK).
     */
    public static Optional<ButtonType> showConfirmation(String title, String header, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);
        return alert.showAndWait();
    }

    /**
     * Método interno para la construcción genérica de las alertas de UI.
     *
     * @param type    El tipo de alerta (INFO, ERROR, WARNING).
     * @param title   El título de la ventana.
     * @param header  El texto del encabezado. Si es null no se reserva un gran bloque superior.
     * @param message El cuerpo o contenido del diálogo.
     */
    private static void showAlert(Alert.AlertType type, String title, String header, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
