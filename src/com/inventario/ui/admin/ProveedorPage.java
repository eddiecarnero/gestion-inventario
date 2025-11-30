package com.inventario.ui.admin;

import com.inventario.config.ConexionBD;
import com.inventario.dao.ProveedorDAO;
import com.inventario.ui.admin.ProveedorModel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import java.sql.*;

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

    private TextField txtNombre, txtRuc, txtTelefono, txtEmail, txtDireccion;
    private ComboBox<String> cmbTipo;
    private TableView<ProveedorModel> tabla;
    private ObservableList<ProveedorModel> listaProveedores;
    private Button btnGuardar, btnEliminar;

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
        grid.setHgap(15); grid.setVgap(10);

        txtNombre = new TextField(); txtNombre.setPromptText("Razón Social / Nombre");
        txtRuc = new TextField(); txtRuc.setPromptText("RUC (11 dígitos)");
        txtTelefono = new TextField();
        txtEmail = new TextField();
        txtDireccion = new TextField();

        cmbTipo = new ComboBox<>();
        cmbTipo.getItems().addAll("Abarrotes", "Carnes", "Lacteos", "Verduras", "Empaques", "Variado");
        cmbTipo.setPromptText("Categoría");
        cmbTipo.setMaxWidth(Double.MAX_VALUE);

        grid.add(crearLabel("Nombre:"), 0, 0); grid.add(txtNombre, 1, 0);
        grid.add(crearLabel("RUC:"), 2, 0);    grid.add(txtRuc, 3, 0);

        grid.add(crearLabel("Tipo:"), 0, 1);   grid.add(cmbTipo, 1, 1);
        grid.add(crearLabel("Teléfono:"), 2, 1); grid.add(txtTelefono, 3, 1);

        grid.add(crearLabel("Email:"), 0, 2);  grid.add(txtEmail, 1, 2);
        grid.add(crearLabel("Dirección:"), 2, 2); grid.add(txtDireccion, 3, 2);

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

        TableColumn<ProveedorModel, String> c1 = new TableColumn<>("Empresa"); c1.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        TableColumn<ProveedorModel, String> c2 = new TableColumn<>("RUC"); c2.setCellValueFactory(new PropertyValueFactory<>("ruc"));
        TableColumn<ProveedorModel, String> c3 = new TableColumn<>("Tipo"); c3.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        TableColumn<ProveedorModel, String> c4 = new TableColumn<>("Teléfono"); c4.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        TableColumn<ProveedorModel, String> c5 = new TableColumn<>("Dirección"); c5.setCellValueFactory(new PropertyValueFactory<>("direccion"));

        tabla.getColumns().addAll(c1, c2, c3, c4, c5);
        tabla.setItems(listaProveedores);
    }

    private void cargarDatos() {
        listaProveedores.clear();
        try (Connection c = ConexionBD.getConnection();
             ResultSet rs = c.createStatement().executeQuery("SELECT * FROM proveedores")) {
            while (rs.next()) {
                listaProveedores.add(new ProveedorModel(
                        rs.getInt("IdProveedor"),
                        rs.getString("Nombre_comercial"),
                        rs.getString("RUC"),
                        rs.getString("Tipo_de_proveedor"),
                        rs.getString("Telefono"),
                        rs.getString("Email"),
                        rs.getString("Direccion")
                ));
            }
        } catch (SQLException e) { mostrarAlerta("Error cargar", e.getMessage()); }
    }

    private void cargarEnFormulario(ProveedorModel p) {
        idProveedorEditando = p.getId();
        txtNombre.setText(p.getNombre());
        txtRuc.setText(p.getRuc());
        cmbTipo.setValue(p.getTipo());
        txtTelefono.setText(p.getTelefono());
        txtEmail.setText(p.getEmail());
        txtDireccion.setText(p.getDireccion());

        btnGuardar.setText("Actualizar");
        btnEliminar.setDisable(false);
    }

    private void limpiarFormulario() {
        idProveedorEditando = null;
        txtNombre.clear(); txtRuc.clear(); txtTelefono.clear(); txtEmail.clear(); txtDireccion.clear();
        cmbTipo.getSelectionModel().clearSelection();
        btnGuardar.setText("Guardar Nuevo");
        btnEliminar.setDisable(true);
        tabla.getSelectionModel().clearSelection();
    }

    private void guardarProveedor() {
        if(txtNombre.getText().isEmpty() || txtRuc.getText().isEmpty()) {
            mostrarAlerta("Nombre y RUC obligatorios.", ""); return;
        }

        int id = (idProveedorEditando == null) ? 0 : idProveedorEditando;
        boolean esNuevo = (idProveedorEditando == null);

        ProveedorModel proveedor = new ProveedorModel(
                id,
                txtNombre.getText(),
                txtRuc.getText(),
                cmbTipo.getValue(),
                txtTelefono.getText(),
                txtEmail.getText(),
                txtDireccion.getText()
        );

        ProveedorDAO dao = new ProveedorDAO();
        if (dao.guardarProveedor(proveedor, esNuevo)) {
            mostrarAlerta("Éxito", "Proveedor guardado correctamente.");
            limpiarFormulario();
            cargarDatos(); // Recargar tabla
        } else {
            mostrarAlerta("Error", "No se pudo guardar en la base de datos.");
        }
    }

    private void eliminarProveedor() {
        if (idProveedorEditando == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "¿Eliminar este proveedor?", ButtonType.YES, ButtonType.NO);
        if (alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            ProveedorDAO dao = new ProveedorDAO();
            if (dao.eliminarProveedor(idProveedorEditando)) {
                mostrarAlerta("Eliminado", "Proveedor eliminado.");
                limpiarFormulario();
                cargarDatos();
            } else {
                mostrarAlerta("Error", "No se puede eliminar (quizás tenga productos asociados).");
            }
        }
    }

    private Label crearLabel(String t) { Label l = new Label(t); l.getStyleClass().add("label-form"); return l; }
    private void mostrarAlerta(String t, String m) { new Alert(Alert.AlertType.INFORMATION, m).show(); }
}