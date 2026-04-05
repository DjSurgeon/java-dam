package com.hambooking.frontend;

import com.hambooking.frontend.util.ViewManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HamBookingApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // Inicializar el gestor de vistas con el Stage principal
        ViewManager.getInstance().setMainStage(stage);

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/hambooking/frontend/fxml/login.fxml")
        );
        Parent root = loader.load();
        Scene scene = new Scene(root);

        // El CSS también está declarado en el FXML (stylesheets="@../css/hambooking.css")
        // pero lo añadimos aquí también para garantizar que carga en todas las escenas
        scene.getStylesheets().add(
                getClass().getResource("/com/hambooking/frontend/css/hambooking.css")
                        .toExternalForm()
        );

        stage.setTitle("HamBooking");
        stage.setScene(scene);
        stage.setMinWidth(900);
        stage.setMinHeight(560);
        stage.setResizable(true);
        stage.show();
    }
}