package com.hambooking.frontend.util;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

/**
 * Gestor centralizado de navegación para la aplicación HamBooking.
 * Implementa el patrón Singleton para controlar el Stage principal y facilitar
 * el intercambio de vistas FXML de forma consistente.
 */
public final class ViewManager {

    private Stage mainStage;

    /**
     * Constructor privado para el patrón Singleton.
     */
    private ViewManager() {}

    /**
     * Clase estática interna para la inicialización segura del Singleton (Bill Pugh Singleton).
     */
    private static class Holder {
        private static final ViewManager INSTANCE = new ViewManager();
    }

    /**
     * Obtiene la instancia única del gestor de vistas.
     *
     * @return La instancia de ViewManager.
     */
    public static ViewManager getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * Configura el escenario (Stage) principal de la aplicación.
     * Este método debe llamarse una sola vez durante el inicio de la aplicación.
     *
     * @param mainStage El escenario principal de JavaFX.
     */
    public void setMainStage(final Stage mainStage) {
        this.mainStage = mainStage;
    }

    /**
     * Obtiene el escenario principal de la aplicación.
     *
     * @return El Stage principal.
     */
    public Stage getMainStage() {
        return mainStage;
    }

    /**
     * Navega a una nueva vista cargando un archivo FXML.
     * Sustituye la raíz de la escena actual para mantener el mismo Stage.
     *
     * @param fxmlPath Ruta al recurso FXML (ej. "/com/hambooking/frontend/fxml/login.fxml").
     * @param title    Nuevo título para la ventana.
     * @throws IOException Si ocurre un error al cargar el archivo FXML.
     * @throws IllegalStateException Si el Stage principal no ha sido configurado previamente.
     */
    public void navigateTo(final String fxmlPath, final String title) throws IOException {
        if (mainStage == null) {
            throw new IllegalStateException("ViewManager: El Stage principal no ha sido configurado. Llame a setMainStage() primero.");
        }

        URL resource = getClass().getResource(fxmlPath);
        if (resource == null) {
            throw new IOException("No se pudo encontrar el archivo FXML en: " + fxmlPath);
        }

        FXMLLoader loader = new FXMLLoader(resource);
        Parent root = loader.load();

        Platform.runLater(() -> {
            Scene scene = mainStage.getScene();
            if (scene == null) {
                scene = new Scene(root);
                mainStage.setScene(scene);
            } else {
                scene.setRoot(root);
            }
            mainStage.setTitle(title);
            mainStage.centerOnScreen();
        });
    }
}
