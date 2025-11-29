package com.inventario.ui;

import com.inventario.config.ConexionBD;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ProveedorPage extends BorderPane {

    // Estilos locales (reutilizando la identidad visual)
    private static final String CSS_STYLES = """
                .root { -fx-background-color: #FDF8F0; }
                .label-form { -fx-font-weight: bold; -fx-text-fill: #555; }
                .text-field { -fx-border-radius: 5; -fx-background-radius: 5; }
                .button-primary { -fx-background-color: #4A90E2; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; }
                .button-danger { -fx-background-color: #EF4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; }
                .button-clear { -fx-background-color: #757575; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; }
                .table-view .column-header { -fx-background-color: #F9FAFB; -fx-font-weight: bold; }
            """;

    private final TextField txtNombre;
    private final TextField txtRuc;
    private final TextField txtTelefono;
    private final TextField txtEmail;
    private final TextField txtDireccion;
    private final ComboBox<String> cmbTipo;
    private final TableView<ProveedorModel> tabla;
    private final ObservableList<ProveedorModel> listaProveedores;
    private final Button btnGuardar;
    private final Button btnEliminar;

    // Variable para saber si estamos editando (guarda el ID)
    private Integer idProveedorEditando = null;

    public ProveedorPage() {
        this.getStylesheets().add("data:text/css," + CSS_STYLES.replace("\n", ""));
        this.getStyleClass().add("root");
        setPadding(new Insets(20));

        // --- 1. Formulario ---
        VBox formContainer = new VBox(10);
        formContainer.setStyle("-fx-background-color: white; -fx-border-color: #DDD; -fx-border-radius: 8; -fx-padding: 15;");

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);

        txtNombre = new TextField();
        txtNombre.setPromptText("Razón Social / Nombre");
        txtRuc = new TextField();
        txtRuc.setPromptText("RUC (11 dígitos)");
        txtTelefono = new TextField();
        txtEmail = new TextField();
        txtDireccion = new TextField();

        cmbTipo = new ComboBox<>();
        cmbTipo.getItems().addAll("Abarrotes", "Carnes", "Lacteos", "Verduras", "Empaques", "Variado");
        cmbTipo.setPromptText("Categoría");
        cmbTipo.setMaxWidth(Double.MAX_VALUE);

        grid.add(crearLabel("Nombre:"), 0, 0);
        grid.add(txtNombre, 1, 0);
        grid.add(crearLabel("RUC:"), 2, 0);
        grid.add(txtRuc, 3, 0);

        grid.add(crearLabel("Tipo:"), 0, 1);
        grid.add(cmbTipo, 1, 1);
        grid.add(crearLabel("Teléfono:"), 2, 1);
        grid.add(txtTelefono, 3, 1);

        grid.add(crearLabel("Email:"), 0, 2);
        grid.add(txtEmail, 1, 2);
        grid.add(crearLabel("Dirección:"), 2, 2);
        grid.add(txtDireccion, 3, 2);

        // Botones Acción
        HBox boxBtns = new HBox(10);
        boxBtns.setAlignment(Pos.CENTER_RIGHT);

        Button btnLimpiar = new Button("Limpiar");
        btnLimpiar.getStyleClass().add("button-clear");
        btnLimpiar.setOnAction(e -> limpiarFormulario());

        btnEliminar = new Button("Eliminar");
        btnEliminar.getStyleClass().add("button-danger");
        btnEliminar.setDisable(true); // Se activa al seleccionar
        btnEliminar.setOnAction(e -> eliminarProveedor());

        btnGuardar = new Button("Guardar Nuevo");
        btnGuardar.getStyleClass().add("button-primary");
        btnGuardar.setOnAction(e -> guardarProveedor());

        boxBtns.getChildren().addAll(btnLimpiar, btnEliminar, btnGuardar);

        formContainer.getChildren().addAll(new Label("Gestión de Proveedores"), grid, new Separator(), boxBtns);

        // --- 2. Tabla ---
        tabla = new TableView<>();
        listaProveedores = FXCollections.observableArrayList();
        configurarTabla();
        cargarDatos();

        // Listener de selección
        tabla.getSelectionModel().selectedItemProperty().addListener((obs, old, nuevo) -> {
            if (nuevo != null) {
                cargarEnFormulario(nuevo);
            }
        });

        setTop(formContainer);
        setCenter(tabla);
        BorderPane.setMargin(tabla, new Insets(20, 0, 0, 0));
    }

    private void configurarTabla() {
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<ProveedorModel, String> c1 = new TableColumn<>("Empresa");
        c1.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        TableColumn<ProveedorModel, String> c2 = new TableColumn<>("RUC");
        c2.setCellValueFactory(new PropertyValueFactory<>("ruc"));
        TableColumn<ProveedorModel, String> c3 = new TableColumn<>("Tipo");
        c3.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        TableColumn<ProveedorModel, String> c4 = new TableColumn<>("Teléfono");
        c4.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        TableColumn<ProveedorModel, String> c5 = new TableColumn<>("Dirección");
        c5.setCellValueFactory(new PropertyValueFactory<>("direccion"));

        tabla.getColumns().addAll(c1, c2, c3, c4, c5);
        tabla.setItems(listaProveedores);
    }

    private void cargarDatos() {
        listaProveedores.clear();
        try (Connection c = ConexionBD.getConnection(); ResultSet rs = c.createStatement().executeQuery("SELECT * FROM proveedores")) {
            while (rs.next()) {
                listaProveedores.add(new ProveedorModel(rs.getInt("IdProveedor"), rs.getString("Nombre_comercial"), rs.getString("RUC"), rs.getString("Tipo_de_proveedor"), rs.getString("Telefono"), rs.getString("Email"), rs.getString("Direccion")));
            }
        } catch (SQLException e) {
            mostrarAlerta("Error cargar", e.getMessage());
        }
    }

    private void cargarEnFormulario(ProveedorModel p) {
        idProveedorEditando = p.id();
        txtNombre.setText(p.nombre());
        txtRuc.setText(p.ruc());
        cmbTipo.setValue(p.tipo());
        txtTelefono.setText(p.telefono());
        txtEmail.setText(p.email());
        txtDireccion.setText(p.direccion());

        btnGuardar.setText("Actualizar");
        btnEliminar.setDisable(false);
    }

    private void limpiarFormulario() {
        idProveedorEditando = null;
        txtNombre.clear();
        txtRuc.clear();
        txtTelefono.clear();
        txtEmail.clear();
        txtDireccion.clear();
        cmbTipo.getSelectionModel().clearSelection();
        btnGuardar.setText("Guardar Nuevo");
        btnEliminar.setDisable(true);
        tabla.getSelectionModel().clearSelection();
    }

    private void guardarProveedor() {
        if (txtNombre.getText().isEmpty() || txtRuc.getText().isEmpty()) {
            mostrarAlerta("Nombre y RUC obligatorios.", "");
            return;
        }

        String sql;
        if (idProveedorEditando == null) {
            sql = "INSERT INTO proveedores (Nombre_comercial, RUC, Tipo_de_proveedor, Telefono, Email, Direccion) VALUES (?,?,?,?,?,?)";
        } else {
            sql = "UPDATE proveedores SET Nombre_comercial=?, RUC=?, Tipo_de_proveedor=?, Telefono=?, Email=?, Direccion=? WHERE IdProveedor=?";
        }

        try (Connection conn = ConexionBD.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, txtNombre.getText());
            ps.setString(2, txtRuc.getText());
            ps.setString(3, cmbTipo.getValue());
            ps.setString(4, txtTelefono.getText());
            ps.setString(5, txtEmail.getText());
            ps.setString(6, txtDireccion.getText());

            if (idProveedorEditando != null) {
                ps.setInt(7, idProveedorEditando);
            }

            ps.executeUpdate();
            mostrarAlerta("Éxito", "Proveedor guardado correctamente.");
            limpiarFormulario();
            cargarDatos();

        } catch (SQLException e) {
            mostrarAlerta("Error BD", e.getMessage());
        }
    }

    private void eliminarProveedor() {
        if (idProveedorEditando == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "¿Eliminar este proveedor?", ButtonType.YES, ButtonType.NO);
        if (alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            try (Connection c = ConexionBD.getConnection(); PreparedStatement ps = c.prepareStatement("DELETE FROM proveedores WHERE IdProveedor=?")) {
                ps.setInt(1, idProveedorEditando);
                ps.executeUpdate();
                mostrarAlerta("Eliminado", "Proveedor eliminado.");
                limpiarFormulario();
                cargarDatos();
            } catch (SQLException e) {
                mostrarAlerta("Error", "No se puede eliminar (quizás tenga productos asociados).");
            }
        }
    }

    private Label crearLabel(String t) {
        Label l = new Label(t);
        l.getStyleClass().add("label-form");
        return l;
    }

    private void mostrarAlerta(String t, String m) {
        new Alert(Alert.AlertType.INFORMATION, m).show();
    }

    // Clase Modelo Interna
    public record ProveedorModel(int id, String nombre, String ruc, String tipo, String telefono, String email,
                                 String direccion) {
    }
}