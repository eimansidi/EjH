module com.example.ejh {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.ejh to javafx.fxml;
    exports com.example.ejh;
}