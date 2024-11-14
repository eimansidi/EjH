package com.example.ejh;

import com.example.ejh.model.Persona;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class PersonasController implements Initializable {

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
    private final String db_url = "jdbc:mysql://database-1.cr60ewocg533.us-east-1.rds.amazonaws.com:3306/";
    private final String user = "admin";
    private final String password = "12345678";

    /**
     * Metodo inicial que configura la conexion con la base de datos, la tabla
     * y carga los datos de la base de datos si la conexion es exitosa.
     *
     * @param location La URL de la ubicacion del archivo FXML.
     * @param resources El recurso utilizado por la vista FXML.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        connection = conectarBaseDatos("personas");
        if (connection != null) {
            crearTablaPersonas();
            cargarDatosDesdeBaseDeDatos();
        }
        // Configurar columnas de la tabla
        nombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        apellidos.setCellValueFactory(new PropertyValueFactory<>("apellidos"));
        edad.setCellValueFactory(new PropertyValueFactory<>("edad"));
    }

    /**
     * Metodo para conectar con la base de datos. Si la base de datos no existe, la crea.
     *
     * @param dbName El nombre de la base de datos a conectar.
     * @return La conexion a la base de datos.
     */
    Connection conectarBaseDatos(String dbName) {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(db_url + dbName, user, password);
            mostrarAlertaExito("Info", "Conexión exitosa a la base de datos: " + dbName);
        } catch (SQLException e) {
            if (e.getErrorCode() == 1049) {
                crearBaseDatos();
                conn = conectarBaseDatos(dbName);
            } else {
                mostrarAlertaError("Error de conexión", "No se pudo conectar a la base de datos.");
            }
        }
        return conn;
    }

    /**
     * Metodo para crear la base de datos si no existe.
     */
    private void crearBaseDatos() {
        try (Connection conn = DriverManager.getConnection(db_url, user, password);
             Statement stmt = conn.createStatement()) {
            String sql = "CREATE DATABASE IF NOT EXISTS personas";
            stmt.executeUpdate(sql);
            System.out.println("Base de datos 'personas' creada o ya existía.");
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlertaError("Error de creación", "No se pudo crear la base de datos.");
        }
    }

    /**
     * Metodo para crear la tabla 'Persona' en la base de datos si no existe.
     */
    private void crearTablaPersonas() {
        if (!tablaExiste("Persona")) {
            String sqlCrearTabla = "CREATE TABLE IF NOT EXISTS Persona ("
                    + "id INT NOT NULL AUTO_INCREMENT, "
                    + "nombre VARCHAR(250) NULL DEFAULT NULL, "
                    + "apellidos VARCHAR(250) NULL DEFAULT NULL, "
                    + "edad INT NULL DEFAULT NULL, "
                    + "PRIMARY KEY (id)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=latin1;";

            try (Statement stmt = connection.createStatement()) {
                stmt.executeUpdate(sqlCrearTabla);
                System.out.println("Tabla 'Persona' creada.");
            } catch (SQLException e) {
                e.printStackTrace();
                mostrarAlertaError("Error al crear la tabla", "No se pudo crear la tabla Persona.");
            }
        } else {
            System.out.println("La tabla 'Persona' ya existe.");
        }
    }

    /**
     * Verifica si la tabla con el nombre proporcionado existe en la base de datos.
     *
     * @param nombreTabla El nombre de la tabla a verificar.
     * @return true si la tabla existe, false si no existe.
     */
    private boolean tablaExiste(String nombreTabla) {
        try (ResultSet rs = connection.getMetaData().getTables(null, null, nombreTabla, null)) {
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlertaError("Error", "No se pudo verificar si la tabla " + nombreTabla + " existe.");
            return false;
        }
    }

    /**
     * Metodo para cargar los datos de la tabla Persona desde la base de datos
     * y agregarlos a la tabla en la interfaz de usuario.
     */
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

    /**
     * Abre una ventana para agregar una nueva persona.
     *
     * @param event El evento de acción.
     */
    @FXML
    void agregar(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("agregar.fxml"));
            Scene scene = new Scene(loader.load());

            AgregarController agregarController = loader.getController();
            agregarController.setMainController(this);
            agregarController.setModoModificar(false);

            Stage stage = new Stage();
            stage.setTitle("Nueva Persona");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(scene);
            stage.setResizable(false);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Abre una ventana para modificar una persona seleccionada en la tabla.
     *
     * @param event El evento de acción.
     */
    @FXML
    void modificar(ActionEvent event) {
        Persona personaSeleccionada = tableView.getSelectionModel().getSelectedItem();
        if (personaSeleccionada == null) {
            mostrarAlertaError("Error", "Debes seleccionar una persona para modificarla.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("agregar.fxml"));
            Scene scene = new Scene(loader.load());

            AgregarController agregarController = loader.getController();
            agregarController.setMainController(this);
            agregarController.setModoModificar(true);
            agregarController.llenarCampos(personaSeleccionada);

            Stage stage = new Stage();
            stage.setTitle("Editar persona");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(scene);
            stage.setResizable(false);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Elimina la persona seleccionada de la tabla y la base de datos.
     *
     * @param event El evento de acción.
     */
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
            mostrarAlertaExito("Info", "Persona eliminada correctamente");
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlertaError("Error al eliminar", "No se pudo eliminar la persona de la base de datos.");
        }
    }

    /**
     * Agrega una persona a la tabla en la interfaz de usuario.
     *
     * @param persona La persona a agregar.
     */
    public void agregarPersonaTabla(Persona persona) {
        tableView.getItems().add(persona);
    }

    /**
     * Modifica los datos de una persona en la tabla.
     *
     * @param personaOriginal La persona original que se quiere modificar.
     * @param personaModificada La persona con los nuevos datos.
     */
    public void modificarPersonaTabla(Persona personaOriginal, Persona personaModificada) {
        int indice = tableView.getItems().indexOf(personaOriginal);
        tableView.getItems().set(indice, personaModificada);
    }

    /**
     * Muestra una alerta de éxito con el mensaje proporcionado.
     *
     * @param titulo El título de la alerta.
     * @param mensaje El mensaje que se mostrará.
     */
    private void mostrarAlertaExito(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    /**
     * Muestra una alerta de error con el mensaje proporcionado.
     *
     * @param titulo El título de la alerta.
     * @param mensaje El mensaje que se mostrará.
     */
    private void mostrarAlertaError(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
