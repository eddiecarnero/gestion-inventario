package com.inventario.ui;

import com.inventario.model.Producto;
import com.inventario.service.ProductoService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.util.List;

public class ProductosPage extends BorderPane {

    private final ProductoService productoService;
    private final TableView<Producto> tablaProductos;
    private final ObservableList<Producto> listaProductos;

    private final TextField txtNombre;
    private final TextField txtDescripcion;
    private final TextField txtPrecio;
    private final TextField txtStockActual;
    private final TextField txtStockMinimo;

    private final TextField txtIdActualizar;
    private final TextField txtCantidadActualizar;
    private final TextField txtIdEliminar;

    // Colores
    private static final String COLOR_PRIMARIO = "#4DB6AC"; // Turquesa
    private static final String COLOR_MARRON = "#6F4E37";
    private static final String COLOR_CREMA = "#FFF8E7";
    private static final String COLOR_PELIGRO = "#DC3545";

    public ProductosPage() {
        this.productoService = new ProductoService();
        this.listaProductos = FXCollections.observableArrayList();

        setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
        setPadding(new Insets(15));

        // Panel Izquierdo (Formulario)
        VBox panelIzquierdo = new VBox(10);
        panelIzquierdo.setPadding(new Insets(15));
        panelIzquierdo.setBackground(new Background(new BackgroundFill(Color.web(COLOR_CREMA), new CornerRadii(10), Insets.EMPTY)));
        panelIzquierdo.setPrefWidth(250);

        Label lblTitulo = new Label("Nuevo Producto");
        lblTitulo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + COLOR_MARRON + ";");

        txtNombre = crearCampo("Nombre");
        txtDescripcion = crearCampo("Descripción");
        txtPrecio = crearCampo("Precio (S/.)");
        txtStockActual = crearCampo("Stock actual");
        txtStockMinimo = crearCampo("Stock mínimo");
        txtStockActual.setText("0");
        txtStockMinimo.setText("0");

        Button btnAgregar = crearBoton("Agregar Producto", "#5CB85C");
        btnAgregar.setOnAction(e -> agregarProducto());

        panelIzquierdo.getChildren().addAll(
                lblTitulo, txtNombre, txtDescripcion, txtPrecio, txtStockActual, txtStockMinimo, btnAgregar
        );

        // Panel Central (Tabla)
        tablaProductos = new TableView<>();
        tablaProductos.setPrefHeight(400);
        configurarTabla();
        cargarProductos();

        VBox panelCentral = new VBox(10, new Label("📦 Inventario Actual"), tablaProductos);
        panelCentral.setPadding(new Insets(10));

        // Panel Derecho (Control de stock y eliminar)
        VBox panelDerecho = new VBox(10);
        panelDerecho.setPadding(new Insets(15));
        panelDerecho.setBackground(new Background(new BackgroundFill(Color.web(COLOR_CREMA), new CornerRadii(10), Insets.EMPTY)));
        panelDerecho.setPrefWidth(250);

        Label lblControl = new Label("Control de Inventario");
        lblControl.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: " + COLOR_MARRON + ";");

        txtIdActualizar = crearCampo("ID Producto");
        txtCantidadActualizar = crearCampo("Cantidad a agregar");
        Button btnActualizar = crearBoton("➕ Aumentar Stock", COLOR_PRIMARIO);
        btnActualizar.setOnAction(e -> aumentarStock());

        Separator sep1 = new Separator();

        txtIdEliminar = crearCampo("ID a eliminar");
        Button btnEliminar = crearBoton("🗑️ Eliminar Producto", COLOR_PELIGRO);
        btnEliminar.setOnAction(e -> eliminarProducto());

        Separator sep2 = new Separator();

        Button btnRefrescar = crearBoton("🔄 Refrescar Tabla", "#007BFF");
        btnRefrescar.setOnAction(e -> cargarProductos());

        panelDerecho.getChildren().addAll(
                lblControl, txtIdActualizar, txtCantidadActualizar, btnActualizar,
                sep1, txtIdEliminar, btnEliminar,
                sep2, btnRefrescar
        );

        setLeft(panelIzquierdo);
        setCenter(panelCentral);
        setRight(panelDerecho);
    }

    private TextField crearCampo(String prompt) {
        TextField campo = new TextField();
        campo.setPromptText(prompt);
        campo.setPrefHeight(30);
        campo.setStyle("-fx-border-color: #ccc; -fx-border-radius: 5; -fx-background-radius: 5;");
        return campo;
    }

    private Button crearBoton(String texto, String colorHex) {
        Button btn = new Button(texto);
        btn.setStyle("-fx-background-color: " + colorHex + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8;");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setPrefHeight(35);
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: derive(" + colorHex + ", 20%); -fx-text-fill: white; -fx-font-weight: bold;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: " + colorHex + "; -fx-text-fill: white; -fx-font-weight: bold;"));
        return btn;
    }

    private void configurarTabla() {
        TableColumn<Producto, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setPrefWidth(60);

        TableColumn<Producto, String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colNombre.setPrefWidth(150);

        TableColumn<Producto, Double> colPrecio = new TableColumn<>("Precio");
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colPrecio.setPrefWidth(80);

        TableColumn<Producto, Integer> colStock = new TableColumn<>("Stock Actual");
        colStock.setCellValueFactory(new PropertyValueFactory<>("stockActual"));
        colStock.setPrefWidth(100);

        TableColumn<Producto, Integer> colMinimo = new TableColumn<>("Stock Mínimo");
        colMinimo.setCellValueFactory(new PropertyValueFactory<>("stockMinimo"));
        colMinimo.setPrefWidth(100);

        TableColumn<Producto, String> colEstado = new TableColumn<>("Estado");
        colEstado.setCellValueFactory(param -> {
            Producto p = param.getValue();
            String estado = (p.getStockActual() <= p.getStockMinimo()) ? "🚨 BAJO STOCK" : "EN STOCK";
            return new javafx.beans.property.SimpleStringProperty(estado);
        });
        colEstado.setPrefWidth(120);

        tablaProductos.getColumns().addAll(colId, colNombre, colPrecio, colStock, colMinimo, colEstado);
        tablaProductos.setItems(listaProductos);
    }

    private void cargarProductos() {
        listaProductos.clear();
        List<Producto> productos = productoService.obtenerTodosLosProductos();
        listaProductos.addAll(productos);
    }

    private void agregarProducto() {
        try {
            String nombre = txtNombre.getText().trim();
            String descripcion = txtDescripcion.getText().trim();
            double precio = Double.parseDouble(txtPrecio.getText().trim());
            int stockActual = Integer.parseInt(txtStockActual.getText().trim());
            int stockMinimo = Integer.parseInt(txtStockMinimo.getText().trim());

            if (nombre.isEmpty()) {
                mostrarAlerta("Error", "El nombre del producto es obligatorio.", Alert.AlertType.ERROR);
                return;
            }

            Producto nuevo = new Producto(0, nombre, descripcion, precio, stockActual, stockMinimo);
            if (productoService.agregarNuevoProducto(nuevo)) {
                mostrarAlerta("Éxito", "Producto agregado correctamente.", Alert.AlertType.INFORMATION);
                limpiarCampos();
                cargarProductos();
            } else {
                mostrarAlerta("Error", "No se pudo agregar el producto (posiblemente duplicado).", Alert.AlertType.ERROR);
            }
        } catch (Exception ex) {
            mostrarAlerta("Error", "Verifique los datos ingresados.", Alert.AlertType.ERROR);
        }
    }

    private void aumentarStock() {
        try {
            int id = Integer.parseInt(txtIdActualizar.getText().trim());
            int cantidad = Integer.parseInt(txtCantidadActualizar.getText().trim());

            if (cantidad <= 0) {
                mostrarAlerta("Error", "La cantidad debe ser positiva.", Alert.AlertType.WARNING);
                return;
            }

            if (productoService.aumentarStockProducto(id, cantidad)) {
                mostrarAlerta("Éxito", "Stock actualizado correctamente.", Alert.AlertType.INFORMATION);
                txtIdActualizar.clear();
                txtCantidadActualizar.clear();
                cargarProductos();
            } else {
                mostrarAlerta("Error", "No se pudo actualizar el stock. Verifique el ID.", Alert.AlertType.ERROR);
            }
        } catch (NumberFormatException ex) {
            mostrarAlerta("Error", "Ingrese valores numéricos válidos.", Alert.AlertType.WARNING);
        }
    }

    private void eliminarProducto() {
        try {
            int id = Integer.parseInt(txtIdEliminar.getText().trim());
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "¿Eliminar producto con ID " + id + "?", ButtonType.YES, ButtonType.NO);
            confirm.setTitle("Confirmar Eliminación");
            confirm.showAndWait();

            if (confirm.getResult() == ButtonType.YES) {
                if (productoService.eliminarProducto(id)) {
                    mostrarAlerta("Éxito", "Producto eliminado correctamente.", Alert.AlertType.INFORMATION);
                    cargarProductos();
                } else {
                    mostrarAlerta("Error", "No se pudo eliminar el producto.", Alert.AlertType.ERROR);
                }
            }
        } catch (NumberFormatException ex) {
            mostrarAlerta("Error", "Ingrese un ID válido.", Alert.AlertType.WARNING);
        }
    }

    private void limpiarCampos() {
        txtNombre.clear();
        txtDescripcion.clear();
        txtPrecio.clear();
        txtStockActual.setText("0");
        txtStockMinimo.setText("0");
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}
