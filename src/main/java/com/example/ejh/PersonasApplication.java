package com.example.ejh;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class PersonasApplication extends Application {
    /**
     * Metodo principal que inicia la aplicacion, cargando la interfaz grafica desde el archivo FXML.
     *
     * @param stage La ventana principal de la aplicacion.
     * @throws IOException Si ocurre un error al cargar el archivo FXML.
     */
    @Override
    public void start(Stage stage) throws IOException {
        // Carga el archivo FXML que define la interfaz grafica
        FXMLLoader fxmlLoader = new FXMLLoader(PersonasApplication.class.getResource("EjH.fxml"));
        // Crea la escena con el contenido cargado desde el archivo FXML
        Scene scene = new Scene(fxmlLoader.load());
        // Establece el titulo de la ventana
        stage.setTitle("PERSONAS");
        // Asigna la escena a la ventana principal
        stage.setScene(scene);
        // Establece que la ventana no se pueda redimensionar
        stage.setResizable(false);
        // Muestra la ventana principal
        stage.show();
    }

    /**
     * Metodo main para iniciar la aplicacion.
     *
     * @param args Argumentos de la linea de comandos.
     */
    public static void main(String[] args) {
        // Lanza la aplicacion JavaFX
        launch();
    }
}
