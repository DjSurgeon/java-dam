package com.hambooking.frontend;

import com.hambooking.frontend.util.AlertHelper;
import com.hambooking.frontend.util.ViewManager;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Clase principal de la aplicación HamBooking.
 * Configura el escenario inicial y lanza la vista de inicio de sesión.
 */
public class HamBookingApp extends Application {

    @Override
    public void start(final Stage stage) {
        // Inicializar el gestor de vistas con el Stage principal
        ViewManager.getInstance().setMainStage(stage);

        // Configuración estética y de comportamiento del escenario
        stage.setMinWidth(900);
        stage.setMinHeight(560);
        stage.setResizable(true);

        try {
            // Delegamos la carga inicial al ViewManager para mantener la consistencia
            ViewManager.getInstance().navigateTo(
                    "/com/hambooking/frontend/fxml/login.fxml", 
                    "HamBooking - Iniciar sesión"
            );
            stage.show();
        } catch (IOException e) {
            AlertHelper.showError("Error de Inicio", "No se pudo cargar la interfaz de usuario inicial.");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
