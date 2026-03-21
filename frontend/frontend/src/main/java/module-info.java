module com.hambooking.frontend {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.fasterxml.jackson.databind;

    // Abre el paquete raíz a javafx.fxml (para HamBookingApp y Launcher)
    opens com.hambooking.frontend to javafx.fxml;

    // Abre el paquete de controladores a javafx.fxml
    // SIN esto IntelliJ muestra "cannot resolve class" en todos los fx:controller
    opens com.hambooking.frontend.controllers to javafx.fxml;

    exports com.hambooking.frontend;
}