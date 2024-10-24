package com.example.ejh;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AgregarController {

    @FXML
    private TextField txtNombre;

    @FXML
    private TextField txtApellidos;

    @FXML
    private TextField txtEdad;

    @FXML
    private Button btnGuardar;

    @FXML
    private Button btnCancelar;

    private HelloController helloController;
    private Persona personaOriginal;
    private boolean modoModificar;

    public void setMainController(HelloController helloController) {
        this.helloController = helloController;
    }

    public void setModoModificar(boolean modificar) {
        this.modoModificar = modificar;
        btnGuardar.setText(modificar ? "Modificar" : "Agregar");
    }

    public void llenarCampos(Persona persona) {
        this.personaOriginal = persona;
        txtNombre.setText(persona.getNombre());
        txtApellidos.setText(persona.getApellidos());
        txtEdad.setText(String.valueOf(persona.getEdad()));
    }

    @FXML
    void guardar(ActionEvent event) {
        String nombre = txtNombre.getText();
        String apellidos = txtApellidos.getText();
        String edadStr = txtEdad.getText();

        StringBuilder errores = new StringBuilder();
        if (nombre.isEmpty()) {
            errores.append("El campo nombre es obligatorio.\n");
        }

        if (apellidos.isEmpty()) {
            errores.append("El campo apellidos es obligatorio.\n");
        }

        int edad = -1;
        if (edadStr.isEmpty()) {
            errores.append("El campo edad es obligatorio.\n");
        } else {
            try {
                edad = Integer.parseInt(edadStr);
            } catch (NumberFormatException e) {
                errores.append("La edad debe ser un número entero.\n");
            }
        }

        if (!errores.isEmpty()) {
            mostrarAlertaError("Datos invalidos", errores.toString());
            return;
        }

        int id = 0;

        Persona nuevaPersona = new Persona(id, nombre, apellidos, edad);

        if (modoModificar && personaOriginal != null) {
            helloController.modificarPersonaTabla(personaOriginal, nuevaPersona);
            mostrarAlertaExito("Info", "Persona modificada correctamente");
        } else {
            if (helloController.existePersona(nuevaPersona)) {
                mostrarAlertaError("Error", "No puede haber dos personas iguales.");
                return;
            }
            helloController.agregarPersonaTabla(nuevaPersona);
            mostrarAlertaExito("Info", "Persona añadida correctamente");
        }

        Stage stage = (Stage) btnGuardar.getScene().getWindow();
        stage.close();
    }

    @FXML
    void cancelar(ActionEvent event) {
        Stage stage = (Stage) btnCancelar.getScene().getWindow();
        stage.close();
    }

    private void mostrarAlertaExito(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarAlertaError(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
