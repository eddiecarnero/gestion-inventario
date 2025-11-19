package com.inventario.ui;

import com.inventario.config.ConexionBD;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PerfilPage extends BorderPane {

    // --- Estilos (Consistentes con el resto de la App) ---
    private static final String CSS_STYLES = """
        .root {
            -fx-background-color: #FDF8F0;
            -fx-font-family: 'Segoe UI';
        }
        .header-title {
            -fx-font-size: 2.2em;
            -fx-font-weight: bold;
            -fx-text-fill: #333333;
        }
        .header-description {
            -fx-font-size: 1.1em;
            -fx-text-fill: #555555;
        }
        .tab-pane .tab-header-area .tab-header-background {
            -fx-background-color: transparent;
        }
        .tab-pane .tab {
            -fx-background-color: transparent;
            -fx-border-color: transparent;
            -fx-padding: 8 15 8 15;
            -fx-font-size: 1.1em;
        }
        .tab-pane .tab:selected {
            -fx-background-color: transparent;
            -fx-border-color: #4A90E2; /* PRIMARY */
            -fx-border-width: 0 0 3 0;
            -fx-text-fill: #4A90E2;
            -fx-font-weight: bold;
        }
        .tab-content-area {
            -fx-background-color: transparent;
            -fx-padding: 20 0 0 0;
        }
        .card {
            -fx-background-color: white;
            -fx-border-color: #E0E0E0;
            -fx-border-width: 1;
            -fx-border-radius: 8;
            -fx-background-radius: 8;
            -fx-padding: 30px; /* Un poco más de padding para formularios */
        }
        .card-title {
            -fx-font-size: 1.4em;
            -fx-font-weight: bold;
            -fx-text-fill: #333333;
        }
        .label {
            -fx-font-size: 1.05em;
            -fx-font-weight: 500;
            -fx-text-fill: #333333;
        }
        .text-field, .combo-box, .password-field {
            -fx-font-size: 1.05em;
            -fx-pref-height: 38px;
            -fx-border-color: #CCCCCC;
            -fx-border-radius: 5;
            -fx-background-radius: 5;
        }
        .button-primary {
            -fx-background-color: #4A90E2;
            -fx-text-fill: white;
            -fx-font-weight: bold;
            -fx-font-size: 1.1em;
            -fx-pref-height: 40px;
            -fx-background-radius: 5;
            -fx-cursor: hand;
        }
        .button-primary:hover {
            -fx-background-color: #357ABD;
        }
    """;

    public PerfilPage() {
        this.getStylesheets().add("data:text/css," + CSS_STYLES.replace("\n", ""));
        this.getStyleClass().add("root");
        setPadding(new Insets(30, 40, 30, 40));

        // --- Estructura Principal ---
        VBox mainContent = new VBox();

        // Header
        Node header = crearHeader();

        // Tabs
        TabPane tabPane = new TabPane();
        tabPane.getStyleClass().add("tab-pane");

        Tab tabUsuarios = new Tab("Registrar Usuario", crearFormularioUsuario());
        tabUsuarios.setClosable(false);

        Tab tabProveedores = new Tab("Registrar Proveedor", crearFormularioProveedor());
        tabProveedores.setClosable(false);

        tabPane.getTabs().addAll(tabUsuarios, tabProveedores);

        mainContent.getChildren().addAll(header, tabPane);
        setCenter(mainContent);
    }

    private Node crearHeader() {
        VBox headerBox = new VBox(5);
        headerBox.setPadding(new Insets(0, 0, 20, 0));
        Label header = new Label("Administración del Sistema");
        header.getStyleClass().add("header-title");
        Label description = new Label("Gestión de usuarios, roles y proveedores");
        description.getStyleClass().add("header-description");
        headerBox.getChildren().addAll(header, description);
        return headerBox;
    }

    // ==========================================
    //      PESTAÑA 1: REGISTRAR USUARIO
    // ==========================================

    private Node crearFormularioUsuario() {
        VBox layout = new VBox(20);
        layout.getStyleClass().add("tab-content-area");

        VBox card = new VBox(20);
        card.getStyleClass().add("card");
        card.setMaxWidth(800); // Limitar ancho para que se vea bien
        card.setAlignment(Pos.TOP_CENTER);

        Label title = new Label("Nuevo Empleado / Usuario");
        title.getStyleClass().add("card-title");

        // --- Campos del Formulario ---
        TextField nombreField = new TextField();
        nombreField.setPromptText("Nombre y Apellido");

        TextField dniField = new TextField();
        dniField.setPromptText("DNI");

        TextField telefonoField = new TextField();
        telefonoField.setPromptText("Teléfono");

        ComboBox<String> rolCombo = new ComboBox<>();
        rolCombo.getItems().addAll("Administrador", "Cocinero", "Almacenero", "Vendedor");
        rolCombo.setPromptText("Seleccionar Rol");
        rolCombo.setMaxWidth(Double.MAX_VALUE);

        TextField turnoField = new TextField();
        turnoField.setPromptText("Ej. Mañana, Tarde, Completo");

        TextField horarioField = new TextField();
        horarioField.setPromptText("Ej. 8:00 AM - 5:00 PM");

        Separator sep = new Separator();

        TextField usuarioField = new TextField();
        usuarioField.setPromptText("Nombre de usuario (para login)");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Contraseña");

        // --- Layout en Grid ---
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(15);

        grid.add(crearCampo("Nombre Completo", nombreField), 0, 0, 2, 1); // Ocupa 2 columnas
        grid.add(crearCampo("DNI", dniField), 0, 1);
        grid.add(crearCampo("Teléfono", telefonoField), 1, 1);
        grid.add(crearCampo("Rol / Puesto", rolCombo), 0, 2);
        grid.add(crearCampo("Turno", turnoField), 1, 2);
        grid.add(crearCampo("Horario", horarioField), 0, 3, 2, 1);

        grid.add(sep, 0, 4, 2, 1); // Separador visual

        grid.add(crearCampo("Usuario", usuarioField), 0, 5);
        grid.add(crearCampo("Contraseña", passwordField), 1, 5);

        ColumnConstraints col = new ColumnConstraints();
        col.setPercentWidth(50);
        grid.getColumnConstraints().addAll(col, col);

        // Botón Guardar
        Button btnGuardar = new Button("💾 Registrar Empleado");
        btnGuardar.getStyleClass().add("button-primary");
        btnGuardar.setMaxWidth(Double.MAX_VALUE);

        btnGuardar.setOnAction(e -> {
            registrarEmpleado(
                    nombreField.getText(), dniField.getText(), telefonoField.getText(),
                    rolCombo.getValue(), turnoField.getText(), horarioField.getText(),
                    usuarioField.getText(), passwordField.getText()
            );
            // Limpiar campos tras éxito (opcional)
            nombreField.clear(); dniField.clear(); telefonoField.clear();
            turnoField.clear(); horarioField.clear(); usuarioField.clear(); passwordField.clear();
            rolCombo.getSelectionModel().clearSelection();
        });

        card.getChildren().addAll(title, grid, new Separator(), btnGuardar);
        layout.getChildren().add(card);
        return layout;
    }

    private void registrarEmpleado(String nombre, String dni, String telefono, String rol, String turno, String horario, String user, String pass) {
        if (nombre.isEmpty() || dni.isEmpty() || user.isEmpty() || pass.isEmpty() || rol == null) {
            mostrarAlerta("Error", "Los campos Nombre, DNI, Rol, Usuario y Contraseña son obligatorios.");
            return;
        }

        String sql = "INSERT INTO empleado (Tipo_de_empleado, Nombre_y_Apellido, Telefono, DNI, Turnos, Horario, user, password) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, rol);
            stmt.setString(2, nombre);
            stmt.setString(3, telefono);
            stmt.setString(4, dni);
            stmt.setString(5, turno);
            stmt.setString(6, horario);
            stmt.setString(7, user);
            stmt.setString(8, pass);

            stmt.executeUpdate();
            mostrarAlerta("Éxito", "Empleado '" + nombre + "' registrado correctamente.");

        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error de Base de Datos", "No se pudo registrar el empleado.\nVerifique que el DNI o Usuario no estén duplicados.");
        }
    }


    // ==========================================
    //      PESTAÑA 2: REGISTRAR PROVEEDOR
    // ==========================================

    private Node crearFormularioProveedor() {
        VBox layout = new VBox(20);
        layout.getStyleClass().add("tab-content-area");

        VBox card = new VBox(20);
        card.getStyleClass().add("card");
        card.setMaxWidth(800);

        Label title = new Label("Nuevo Proveedor");
        title.getStyleClass().add("card-title");

        TextField nombreField = new TextField();
        nombreField.setPromptText("Nombre Comercial");

        TextField rucField = new TextField();
        rucField.setPromptText("RUC (11 dígitos)");

        ComboBox<String> tipoCombo = new ComboBox<>();
        tipoCombo.getItems().addAll("Variado", "Lacteos", "Carnes", "Abarrotes", "Verduras", "Empaques");
        tipoCombo.setMaxWidth(Double.MAX_VALUE);

        TextField telefonoField = new TextField();

        TextField emailField = new TextField();

        TextField direccionField = new TextField();

        // Grid Layout
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(15);

        grid.add(crearCampo("Nombre Comercial", nombreField), 0, 0);
        grid.add(crearCampo("RUC", rucField), 1, 0);
        grid.add(crearCampo("Tipo de Proveedor", tipoCombo), 0, 1);
        grid.add(crearCampo("Teléfono", telefonoField), 1, 1);
        grid.add(crearCampo("Email", emailField), 0, 2);
        grid.add(crearCampo("Dirección", direccionField), 1, 2);

        ColumnConstraints col = new ColumnConstraints();
        col.setPercentWidth(50);
        grid.getColumnConstraints().addAll(col, col);

        Button btnGuardar = new Button("💾 Registrar Proveedor");
        btnGuardar.getStyleClass().add("button-primary");
        btnGuardar.setMaxWidth(Double.MAX_VALUE);

        btnGuardar.setOnAction(e -> {
            registrarProveedor(
                    nombreField.getText(), rucField.getText(), tipoCombo.getValue(),
                    telefonoField.getText(), emailField.getText(), direccionField.getText()
            );
            // Limpiar
            nombreField.clear(); rucField.clear(); tipoCombo.getSelectionModel().clearSelection();
            telefonoField.clear(); emailField.clear(); direccionField.clear();
        });

        card.getChildren().addAll(title, grid, new Separator(), btnGuardar);
        layout.getChildren().add(card);
        return layout;
    }

    private void registrarProveedor(String nombre, String ruc, String tipo, String telefono, String email, String direccion) {
        if (nombre.isEmpty() || ruc.isEmpty()) {
            mostrarAlerta("Error", "Nombre Comercial y RUC son obligatorios.");
            return;
        }

        String sql = "INSERT INTO proveedores (Nombre_comercial, RUC, Tipo_de_proveedor, Telefono, Email, Direccion) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nombre);
            stmt.setString(2, ruc);
            stmt.setString(3, tipo != null ? tipo : "Variado");
            stmt.setString(4, telefono);
            stmt.setString(5, email);
            stmt.setString(6, direccion);

            stmt.executeUpdate();
            mostrarAlerta("Éxito", "Proveedor '" + nombre + "' registrado correctamente.");

        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error de Base de Datos", "No se pudo registrar el proveedor.\nVerifique que el RUC no esté duplicado.");
        }
    }


    private VBox crearCampo(String labelText, Control input) {
        VBox vbox = new VBox(5);
        Label label = new Label(labelText);
        label.getStyleClass().add("label");
        vbox.getChildren().addAll(label, input);
        return vbox;
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        if (titulo.startsWith("Error")) {
            alert.setAlertType(Alert.AlertType.ERROR);
        }
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    // --- Test App ---
    public static class TestApp extends Application {
        @Override
        public void start(Stage stage) {
            stage.setTitle("Administración - JavaFX");
            stage.setScene(new Scene(new PerfilPage(), 1000, 700));
            stage.show();
        }
    }

    public static void main(String[] args) {
        Application.launch(TestApp.class, args);
    }
}