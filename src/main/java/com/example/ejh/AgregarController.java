package com.example.ejh;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
        String nombre = txtNombre.getText().trim();
        String apellidos = txtApellidos.getText().trim();
        String edadStr = txtEdad.getText().trim();

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
                if (edad < 0) {
                    errores.append("La edad debe ser un número positivo.\n");
                }
            } catch (NumberFormatException e) {
                errores.append("La edad debe ser un número entero.\n");
            }
        }

        if (errores.length() > 0) {
            mostrarAlertaError("Datos inválidos", errores.toString());
            return;
        }

        Connection connection = helloController.conectarBaseDatos("personas"); // Obtener conexión a la base de datos

        if (modoModificar && personaOriginal != null) {
            // Modificar persona existente en la base de datos
            String sql = "UPDATE Persona SET nombre = ?, apellidos = ?, edad = ? WHERE id = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, nombre);
                pstmt.setString(2, apellidos);
                pstmt.setInt(3, edad);
                pstmt.setInt(4, personaOriginal.getId());
                pstmt.executeUpdate();

                // Actualizar persona en la TableView
                Persona personaModificada = new Persona(personaOriginal.getId(), nombre, apellidos, edad);
                helloController.modificarPersonaTabla(personaOriginal, personaModificada);
                mostrarAlertaExito("Info", "Persona modificada correctamente");
            } catch (SQLException e) {
                e.printStackTrace();
                mostrarAlertaError("Error al modificar", "No se pudo modificar la persona en la base de datos.");
                return;
            }
        } else {
            // Insertar nueva persona en la base de datos
            String sql = "INSERT INTO Persona (nombre, apellidos, edad) VALUES (?, ?, ?)";
            try (PreparedStatement pstmt = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, nombre);
                pstmt.setString(2, apellidos);
                pstmt.setInt(3, edad);
                pstmt.executeUpdate();

                // Obtener el ID generado por la base de datos
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int id = generatedKeys.getInt(1);
                    Persona nuevaPersona = new Persona(id, nombre, apellidos, edad);

                    // Agregar persona a la TableView
                    helloController.agregarPersonaTabla(nuevaPersona);
                    mostrarAlertaExito("Info", "Persona añadida correctamente");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                mostrarAlertaError("Error al agregar", "No se pudo agregar la persona a la base de datos.");
                return;
            }
        }

        // Cerrar la ventana después de guardar
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
