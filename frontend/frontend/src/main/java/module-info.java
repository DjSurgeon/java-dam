module com.hambooking.frontend {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.fasterxml.jackson.databind;

    opens com.hambooking.frontend to javafx.fxml;
    exports com.hambooking.frontend;
}