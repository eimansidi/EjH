package com.example.ejh;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class HelloController implements Initializable {

    @FXML
    private TableView<Persona> tableView;

    @FXML
    private TableColumn<Persona, String> nombre;

    @FXML
    private TableColumn<Persona, String> apellidos;

    @FXML
    private TableColumn<Persona, Integer> edad;

    @FXML
    private TextField txtNombre;
    @FXML
    private TextField txtApellidos;
    @FXML
    private TextField txtEdad;

    private Connection connection;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        connection = conectarBaseDatos(); // Conectar a la base de datos
        crearBaseDatos(); // Crear la base de datos si no existe
        crearTablaPersonas(); // Crear la tabla si no existe

        // Configurar las columnas de la tabla
        nombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        apellidos.setCellValueFactory(new PropertyValueFactory<>("apellidos"));
        edad.setCellValueFactory(new PropertyValueFactory<>("edad"));

        // Cargar datos desde la base de datos
        cargarDatosDesdeBaseDeDatos();
    }

    private Connection conectarBaseDatos() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection("jdbc:mysql://database-1.cr60ewocg533.us-east-1.rds.amazonaws.com:3306/personas", "root", "12345678");
            System.out.println("Conexión establecida con la base de datos.");
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlertaError("Error de conexión", "No se pudo conectar a la base de datos.");
        }
        return conn;
    }

    private void crearBaseDatos() {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/", "root", "12345678");
             Statement stmt = conn.createStatement()) {
            // Crear la base de datos si no existe
            String sqlCrearDB = "CREATE DATABASE IF NOT EXISTS personas";
            stmt.executeUpdate(sqlCrearDB);
            System.out.println("Base de datos 'personas' creada o ya existe.");
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlertaError("Error al crear la base de datos", "No se pudo crear la base de datos 'personas'.");
        }
    }

    private void crearTablaPersonas() {
        String sqlCrearTabla = "CREATE TABLE IF NOT EXISTS persona ("
                + "id INT NOT NULL AUTO_INCREMENT, "
                + "nombre VARCHAR(250) NULL DEFAULT NULL, "
                + "apellidos VARCHAR(250) NULL DEFAULT NULL, "
                + "edad INT NULL DEFAULT NULL, "
                + "PRIMARY KEY (id)"
                + ") ENGINE=InnoDB DEFAULT CHARSET=latin1;";

        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(sqlCrearTabla);
            System.out.println("Tabla 'Persona' creada o ya existe.");
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlertaError("Error al crear la tabla", "No se pudo crear la tabla Persona.");
        }
    }

    private void cargarDatosDesdeBaseDeDatos() {
        String sql = "SELECT * FROM Persona";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String nombre = rs.getString("nombre");
                String apellidos = rs.getString("apellidos");
                int edad = rs.getInt("edad");
                tableView.getItems().add(new Persona(id, nombre, apellidos, edad));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlertaError("Error de carga", "No se pudieron cargar los datos de la base de datos.");
        }
    }

    @FXML
    void agregar(ActionEvent event) {
        String nombre = txtNombre.getText();
        String apellidos = txtApellidos.getText();
        int edad;

        try {
            edad = Integer.parseInt(txtEdad.getText());
        } catch (NumberFormatException e) {
            mostrarAlertaError("Error", "La edad debe ser un número válido.");
            return;
        }

        Persona persona = new Persona(0, nombre, apellidos, edad);

        String sql = "INSERT INTO Persona (nombre, apellidos, edad) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, persona.getNombre());
            pstmt.setString(2, persona.getApellidos());
            pstmt.setInt(3, persona.getEdad());
            pstmt.executeUpdate();

            ResultSet generatedKeys = pstmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                int id = generatedKeys.getInt(1);
                persona = new Persona(id, persona.getNombre(), persona.getApellidos(), persona.getEdad());
                tableView.getItems().add(persona);
                mostrarAlertaExito("Éxito", "Persona agregada exitosamente.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlertaError("Error al agregar", "No se pudo agregar la persona a la base de datos.");
        }

    }


    @FXML
    void modificar(ActionEvent event) {
        Persona personaSeleccionada = tableView.getSelectionModel().getSelectedItem();
        if (personaSeleccionada == null) {
            mostrarAlertaError("Error", "Debes seleccionar una persona para modificar.");
            return;
        }

        String nombre = txtNombre.getText();
        String apellidos = txtApellidos.getText();
        int edad = Integer.parseInt(txtEdad.getText());

        String sql = "UPDATE Persona SET nombre = ?, apellidos = ?, edad = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, nombre);
            pstmt.setString(2, apellidos);
            pstmt.setInt(3, edad);
            pstmt.setInt(4, personaSeleccionada.getId());
            pstmt.executeUpdate();

            personaSeleccionada.setNombre(nombre);
            personaSeleccionada.setApellidos(apellidos);
            personaSeleccionada.setEdad(edad);
            tableView.refresh(); // Actualiza la tabla para mostrar los cambios
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlertaError("Error al modificar", "No se pudo modificar la persona en la base de datos.");
        }
    }

    @FXML
    void eliminar(ActionEvent event) {
        Persona personaSeleccionada = tableView.getSelectionModel().getSelectedItem();
        if (personaSeleccionada == null) {
            mostrarAlertaError("Error", "Debes seleccionar una persona para eliminar.");
            return;
        }

        String sql = "DELETE FROM Persona WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, personaSeleccionada.getId());
            pstmt.executeUpdate();
            tableView.getItems().remove(personaSeleccionada);
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlertaError("Error al eliminar", "No se pudo eliminar la persona de la base de datos.");
        }
    }

    public boolean existePersona(Persona persona) {
        return tableView.getItems().contains(persona);
    }

    public void agregarPersonaTabla(Persona persona) {
        tableView.getItems().add(persona);
    }

    public void modificarPersonaTabla(Persona personaOriginal, Persona personaModificada) {
        int indice = tableView.getItems().indexOf(personaOriginal);
        tableView.getItems().set(indice, personaModificada);
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
