module com.example.ejh {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens com.example.ejh to javafx.fxml;
    exports com.example.ejh;
    exports com.example.ejh.model;
    opens com.example.ejh.model to javafx.fxml;
}