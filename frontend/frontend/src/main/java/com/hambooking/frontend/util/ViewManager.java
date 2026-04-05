package com.hambooking.frontend.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Utilidad centralizada para gestionar la navegación entre vistas (FXML) de JavaFX.
 * Implementa el patrón Singleton para mantener la referencia al Stage principal
 * y evita la duplicación de métodos "navigateTo" en múltiples controladores.
 */
public class ViewManager {

    private static ViewManager instance;
    private Stage mainStage;

    /**
     * Constructor privado para evitar instanciación externa.
     */
    private ViewManager() {}

    /**
     * Obtiene la única instancia de la clase.
     *
     * @return La instancia de ViewManager.
     */
    public static ViewManager getInstance() {
        if (instance == null) {
            instance = new ViewManager();
        }
        return instance;
    }

    /**
     * Inicializa el gestor de vistas con el Stage principal.
     * Este método debe llamarse al arrancar la aplicación.
     *
     * @param mainStage El Stage principal de la aplicación.
     */
    public void setMainStage(Stage mainStage) {
        this.mainStage = mainStage;
    }

    /**
     * Obtiene el Stage principal de la aplicación.
     *
     * @return El Stage principal.
     */
    public Stage getMainStage() {
        return mainStage;
    }

    /**
     * Navega hacia una nueva vista a partir de un archivo FXML.
     * Sustituye la raíz de la escena actual para mantener el mismo Stage.
     *
     * @param fxmlPath Ruta absoluta al archivo FXML (ej. "/com/hambooking/frontend/fxml/login.fxml").
     * @param title    El nuevo título que tendrá la ventana.
     * @throws IOException Si ocurre un problema leyendo el archivo FXML.
     * @throws IllegalStateException Si no se ha configurado el Stage principal previamente.
     */
    public void navigateTo(String fxmlPath, String title) throws IOException {
        if (mainStage == null) {
            throw new IllegalStateException("ViewManager no tiene configurado el mainStage. Llame a setMainStage() primero.");
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();

        Scene scene = mainStage.getScene();
        if (scene == null) {
            scene = new Scene(root);
            mainStage.setScene(scene);
        } else {
            scene.setRoot(root);
        }
        
        mainStage.setTitle(title);
    }
}
