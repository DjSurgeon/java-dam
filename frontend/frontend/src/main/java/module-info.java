module com.hambooking.frontend {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.fasterxml.jackson.databind;

    // Necesario para HttpClient (ApiClient.java)
    requires java.net.http;

    // Jackson necesita acceso para serializar/deserializar los DTOs
    opens com.hambooking.frontend.dto to com.fasterxml.jackson.databind;

    // javafx.fxml necesita acceso por reflexion para cargar controladores
    opens com.hambooking.frontend to javafx.fxml;
    opens com.hambooking.frontend.controllers to javafx.fxml;

    exports com.hambooking.frontend;
}