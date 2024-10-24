package com.example.ejh;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.*;
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
    private TextField txtFiltro;

    private Connection connection;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        nombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        apellidos.setCellValueFactory(new PropertyValueFactory<>("apellidos"));
        edad.setCellValueFactory(new PropertyValueFactory<>("edad"));
    }

    private Connection conectarBaseDatos() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/personas", "root", "12345678");
            System.out.println("Conexión establecida con la base de datos.");
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlertaError("Error de conexión", "No se pudo conectar a la base de datos.");
        }
        return conn;
    }

    private void crearTablaPersonas() {
        String sqlCrearTabla = "CREATE TABLE IF NOT EXISTS Persona ("
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
    void agregar(Persona persona) {
        String sql = "INSERT INTO Persona (nombre, apellidos, edad) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, persona.getNombre());
            pstmt.setString(2, persona.getApellidos());
            pstmt.setInt(3, persona.getEdad());
            pstmt.executeUpdate();

            // Obtener el ID generado automáticamente
            ResultSet generatedKeys = pstmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                int id = generatedKeys.getInt(1);
                persona = new Persona(id, persona.getNombre(), persona.getApellidos(), persona.getEdad());
                tableView.getItems().add(persona);
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

    @FXML
    void eliminar(ActionEvent event) {
        Persona personaSeleccionada = tableView.getSelectionModel().getSelectedItem();
        if (personaSeleccionada == null) {
            mostrarAlertaError("Error", "Debes seleccionar una persona para eliminarla.");
            return;
        }

        tableView.getItems().remove(personaSeleccionada);
        mostrarAlertaExito("Eliminacion", "Persona eliminada correctamente.");
    }

    @FXML
    public void filtrarPorNombre() {
        String filtro = txtFiltro.getText().toLowerCase();

        if (filtro.isEmpty()) {
            tableView.setItems(listaPersonas);
        } else {
            ObservableList<Persona> listaFiltrada = FXCollections.observableArrayList();
            for (Persona persona : listaPersonas) {
                if (persona.getNombre().toLowerCase().contains(filtro)) {
                    listaFiltrada.add(persona);
                }
            }
            tableView.setItems(listaFiltrada);
        }
    }

    public void exportar() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar como");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

        fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        File file = fileChooser.showSaveDialog(null);

        if (file != null) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write("Nombre,Apellidos,Edad");
                writer.newLine();

                ObservableList<Persona> personas = tableView.getItems();
                for (Persona persona : personas) {
                    writer.write(String.format("%s,%s,%d", persona.getNombre(), persona.getApellidos(), persona.getEdad()));
                    writer.newLine();
                }

                mostrarAlertaExito("Exportación exitosa", "Los datos han sido exportados correctamente.");
            } catch (IOException e) {
                mostrarAlertaError("Error", "No se pudo exportar el archivo.");
            }
        }
    }

    public void importar() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Abrir archivo");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

        fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        File file = fileChooser.showOpenDialog(null);

        if (file != null) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                boolean isFirstLine = true;
                while ((line = reader.readLine()) != null) {
                    if (isFirstLine) {
                        isFirstLine = false;
                        continue;
                    }

                    String[] data = line.split(",");
                    if (data.length == 3) {
                        String nombre = data[0].trim();
                        String apellidos = data[1].trim();
                        int edad;

                        try {
                            edad = Integer.parseInt(data[2].trim());
                        } catch (NumberFormatException e) {
                            mostrarAlertaError("Error", "La edad debe ser un número válido: " + line);
                            continue;
                        }

                        if (tableView.getItems().stream().noneMatch(p -> p.getNombre().equals(nombre) && p.getApellidos().equals(apellidos))) {
                            tableView.getItems().add(new Persona(nombre, apellidos, edad));
                        } else {
                            mostrarAlertaError("Duplicado", "El registro ya existe: " + line);
                        }
                    } else {
                        mostrarAlertaError("Error", "Formato de línea inválido: " + line);
                    }
                }

                mostrarAlertaExito("Info", "Los datos han sido importados correctamente.");
            } catch (IOException e) {
                mostrarAlertaError("Error", "No se pudo importar el archivo.");
            }
        }
    }

    public boolean existePersona(Persona persona) {
        return tableView.getItems().contains(persona);
    }

    public void agregarPersonaTabla(Persona persona) {
        listaPersonas.add(persona);
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
