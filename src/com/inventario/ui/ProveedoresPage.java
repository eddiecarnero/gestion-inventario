package com.inventario.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.paint.Color;

import java.sql.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import com.inventario.config.ConexionBD;


public class ProveedoresPage extends BorderPane  {
    // ==========================================
    // CAMPOS DE FORMULARIO
    // ==========================================
    private TextField txtNombre;
    private TextField txtRUC;
    private TextField txtTipo;
    private TextField txtTelefono;
    private TextField txtEmail;
    private TextField txtDireccion;

    private TableView<Proveedor> tabla;
    private ObservableList<Proveedor> listaProveedores;

    // ==========================================
    // CONSTRUCTOR
    // ==========================================
    public ProveedoresPage() {
        setPadding(new Insets(10));

        crearPanelSuperior();
        setLeft(crearPanelFormulario());
        setCenter(crearPanelTabla());

        cargarProveedores();
    }

    // ==========================================
    // PANEL SUPERIOR (TÍTULO)
    // ==========================================
    private void crearPanelSuperior() {
        Label titulo = new Label("GESTIÓN DE PROVEEDORES");
        titulo.setFont(Font.font("Arial", 28));
        titulo.setTextFill(Color.WHITE);

        HBox header = new HBox(titulo);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(20));
        header.setStyle("-fx-background-color: #3498db;");

        setTop(header);
    }

    // ==========================================
    // PANEL IZQUIERDO (FORMULARIO)
    // ==========================================
    private VBox crearPanelFormulario() {
        VBox form = new VBox(10);
        form.setPadding(new Insets(20));
        form.setPrefWidth(320);
        form.setStyle("-fx-background-color: #ecf0f1;");

        Label lblForm = new Label("Agregar Nuevo Proveedor");
        lblForm.setFont(Font.font("Arial", 18));

        txtNombre = crearCampo("Nombre Comercial:");
        txtRUC = crearCampo("RUC (11 dígitos):");
        txtTipo = crearCampo("Tipo de Proveedor:");
        txtTelefono = crearCampo("Teléfono:");
        txtEmail = crearCampo("Email:");
        txtDireccion = crearCampo("Dirección:");

        Button btnAgregar = new Button("Agregar");
        btnAgregar.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold;");
        btnAgregar.setOnAction(e -> agregarProveedor());

        Button btnLimpiar = new Button("Limpiar");
        btnLimpiar.setStyle("-fx-background-color: #f1c40f; -fx-text-fill: white; -fx-font-weight: bold;");
        btnLimpiar.setOnAction(e -> limpiarCampos());

        HBox botones = new HBox(10, btnAgregar, btnLimpiar);
        botones.setAlignment(Pos.CENTER);

        form.getChildren().addAll(lblForm, txtNombre, txtRUC, txtTipo, txtTelefono, txtEmail, txtDireccion, botones);

        return form;
    }

    // Método auxiliar para crear campos
    private TextField crearCampo(String etiqueta) {
        TextField campo = new TextField();
        campo.setPromptText(etiqueta);
        campo.setFont(Font.font("Arial", 14));
        campo.setPrefHeight(30);
        return campo;
    }

    // ==========================================
    // PANEL CENTRAL (TABLA)
    // ==========================================
    private VBox crearPanelTabla() {
        VBox contenedor = new VBox(10);
        contenedor.setPadding(new Insets(20));

        Label lblTabla = new Label("Lista de Proveedores");
        lblTabla.setFont(Font.font("Arial", 18));

        Button btnRefrescar = new Button("🔄 Refrescar");
        btnRefrescar.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        btnRefrescar.setOnAction(e -> cargarProveedores());

        HBox headerTabla = new HBox(lblTabla, btnRefrescar);
        headerTabla.setSpacing(20);
        headerTabla.setAlignment(Pos.CENTER_LEFT);

        tabla = new TableView<>();
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        listaProveedores = FXCollections.observableArrayList();

        String[][] columnas = {
                {"id", "ID"},
                {"nombre", "Nombre"},
                {"ruc", "RUC"},
                {"tipo", "Tipo"},
                {"telefono", "Teléfono"},
                {"email", "Email"},
                {"direccion", "Dirección"}
        };

        for (String[] col : columnas) {
            TableColumn<Proveedor, String> columna = new TableColumn<>(col[1]);
            columna.setCellValueFactory(new PropertyValueFactory<>(col[0]));
            tabla.getColumns().add(columna);
        }

        tabla.setItems(listaProveedores);

        contenedor.getChildren().addAll(headerTabla, tabla);
        return contenedor;
    }

    // ==========================================
    // CONEXIÓN BASE DE DATOS
    // ==========================================
    private Connection conectar() throws SQLException {
        return ConexionBD.getConnection();
    }

    // ==========================================
    // AGREGAR PROVEEDOR
    // ==========================================
    private void agregarProveedor() {
        if (txtNombre.getText().trim().isEmpty()) {
            mostrarAlerta("Error", "El nombre comercial es obligatorio.", Alert.AlertType.ERROR);
            txtNombre.requestFocus();
            return;
        }

        if (txtRUC.getText().trim().isEmpty() || txtRUC.getText().trim().length() != 11) {
            mostrarAlerta("Error", "El RUC debe tener exactamente 11 dígitos.", Alert.AlertType.ERROR);
            txtRUC.requestFocus();
            return;
        }

        String sql = "INSERT INTO proveedores (Nombre_comercial, RUC, Tipo_de_proveedor, Telefono, Email, Direccion) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, txtNombre.getText().trim());
            pstmt.setString(2, txtRUC.getText().trim());
            pstmt.setString(3, txtTipo.getText().trim());
            pstmt.setString(4, txtTelefono.getText().trim());
            pstmt.setString(5, txtEmail.getText().trim());
            pstmt.setString(6, txtDireccion.getText().trim());

            int filas = pstmt.executeUpdate();
            if (filas > 0) {
                mostrarAlerta("Éxito", "Proveedor agregado correctamente.", Alert.AlertType.INFORMATION);
                limpiarCampos();
                cargarProveedores();
            }

        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate entry")) {
                mostrarAlerta("Error", "El RUC ya existe en la base de datos.", Alert.AlertType.ERROR);
            } else {
                mostrarAlerta("Error de Base de Datos", e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    // ==========================================
    // CARGAR PROVEEDORES
    // ==========================================
    private void cargarProveedores() {
        listaProveedores.clear();

        String sql = "SELECT * FROM proveedores ORDER BY IdProveedor DESC";
        try (Connection conn = conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                listaProveedores.add(new Proveedor(
                        rs.getInt("IdProveedor"),
                        rs.getString("Nombre_comercial"),
                        rs.getString("RUC"),
                        rs.getString("Tipo_de_proveedor"),
                        rs.getString("Telefono"),
                        rs.getString("Email"),
                        rs.getString("Direccion")
                ));
            }

        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al cargar proveedores: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // ==========================================
    // LIMPIAR CAMPOS
    // ==========================================
    private void limpiarCampos() {
        txtNombre.clear();
        txtRUC.clear();
        txtTipo.clear();
        txtTelefono.clear();
        txtEmail.clear();
        txtDireccion.clear();
        txtNombre.requestFocus();
    }

    // ==========================================
    // ALERTAS
    // ==========================================
    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    // ==========================================
    // CLASE MODELO INTERNA
    // ==========================================
    public static class Proveedor {
        private int id;
        private String nombre, ruc, tipo, telefono, email, direccion;

        public Proveedor(int id, String nombre, String ruc, String tipo, String telefono, String email, String direccion) {
            this.id = id;
            this.nombre = nombre;
            this.ruc = ruc;
            this.tipo = tipo;
            this.telefono = telefono;
            this.email = email;
            this.direccion = direccion;
        }

        public int getId() { return id; }
        public String getNombre() { return nombre; }
        public String getRuc() { return ruc; }
        public String getTipo() { return tipo; }
        public String getTelefono() { return telefono; }
        public String getEmail() { return email; }
        public String getDireccion() { return direccion; }
    }


}