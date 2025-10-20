package com.inventario.ui;

import com.inventario.model.Producto;
import com.inventario.service.ProductoService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.sql.Date; // Asegúrate de importar java.sql.Date
import java.time.LocalDate; // Para el DatePicker
import java.util.List;

public class ProductosPage extends BorderPane {

    private final ProductoService productoService;
    private final TableView<Producto> tablaProductos;
    private final ObservableList<Producto> listaProductos;

    // --- CAMPOS ACTUALIZADOS ---
    // Eliminados: txtNombre, txtDescripcion, txtPrecio
    // Renombrado: txtStockActual -> txtStock
    private final TextField txtTipoDeProducto;
    private final TextField txtStock;
    private final TextField txtStockMinimo;
    private final TextField txtStockMaximo;
    private final TextField txtUnidadDeMedida;
    private final TextField txtUbicacion;
    private final DatePicker dpFechaDeCaducidad; // Mejor usar un DatePicker para fechas

    // Campos de control (estos estaban bien)
    private final TextField txtIdActualizar;
    private final TextField txtCantidadActualizar;
    private final TextField txtIdEliminar;

    // Colores (sin cambios)
    private static final String COLOR_PRIMARIO = "#4DB6AC";
    private static final String COLOR_MARRON = "#6F4E37";
    private static final String COLOR_CREMA = "#FFF8E7";
    private static final String COLOR_PELIGRO = "#DC3545";

    public ProductosPage() {
        this.productoService = new ProductoService();
        this.listaProductos = FXCollections.observableArrayList();

        setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
        setPadding(new Insets(15));

        // --- Panel Izquierdo (Formulario ACTUALIZADO) ---
        VBox panelIzquierdo = new VBox(10);
        panelIzquierdo.setPadding(new Insets(15));
        panelIzquierdo.setBackground(new Background(new BackgroundFill(Color.web(COLOR_CREMA), new CornerRadii(10), Insets.EMPTY)));
        panelIzquierdo.setPrefWidth(250);

        Label lblTitulo = new Label("Nuevo Producto");
        lblTitulo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + COLOR_MARRON + ";");

        // Inicializamos los nuevos campos
        txtTipoDeProducto = crearCampo("Tipo de Producto (ej: Arroz)");
        txtStock = crearCampo("Stock Actual");
        txtStockMinimo = crearCampo("Stock Mínimo");
        txtStockMaximo = crearCampo("Stock Máximo");
        txtUnidadDeMedida = crearCampo("Unidad de Medida (ej: kg, und)");
        txtUbicacion = crearCampo("Ubicación (ej: A-01)");

        // Configuración del DatePicker
        dpFechaDeCaducidad = new DatePicker();
        dpFechaDeCaducidad.setPromptText("Fecha de Caducidad");
        dpFechaDeCaducidad.setPrefHeight(30);

        // Valores por defecto
        txtStock.setText("0");
        txtStockMinimo.setText("0");
        txtStockMaximo.setText("0");

        Button btnAgregar = crearBoton("Agregar Producto", "#5CB85C");
        btnAgregar.setOnAction(e -> agregarProducto());

        // Añadimos los nuevos campos al panel
        panelIzquierdo.getChildren().addAll(
                lblTitulo, txtTipoDeProducto, txtStock, txtStockMinimo, txtStockMaximo,
                txtUnidadDeMedida, txtUbicacion, dpFechaDeCaducidad, btnAgregar
        );

        // --- Panel Central (Tabla ACTUALIZADA) ---
        tablaProductos = new TableView<>();
        tablaProductos.setPrefHeight(400);
        configurarTabla(); // Método actualizado más abajo
        cargarProductos();

        VBox panelCentral = new VBox(10, new Label("📦 Inventario Actual"), tablaProductos);
        panelCentral.setPadding(new Insets(10));

        // --- Panel Derecho (Sin cambios, ya funcionaba con IDs) ---
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

    // (crearCampo y crearBoton no necesitan cambios)
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

    // --- MÉTODO ACTUALIZADO ---
    private void configurarTabla() {
        // Columna ID (usa "idProducto")
        TableColumn<Producto, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("idProducto"));
        colId.setPrefWidth(50);

        // Columna Tipo (usa "tipoDeProducto")
        TableColumn<Producto, String> colTipo = new TableColumn<>("Tipo Producto");
        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipoDeProducto"));
        colTipo.setPrefWidth(150);

        // Columna Stock (usa "stock")
        TableColumn<Producto, Integer> colStock = new TableColumn<>("Stock");
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        colStock.setPrefWidth(80);

        // Columna Mínimo (usa "stockMinimo")
        TableColumn<Producto, Integer> colMinimo = new TableColumn<>("Stock Mínimo");
        colMinimo.setCellValueFactory(new PropertyValueFactory<>("stockMinimo"));
        colMinimo.setPrefWidth(100);

        // Columna Máximo (usa "stockMaximo")
        TableColumn<Producto, Integer> colMaximo = new TableColumn<>("Stock Máximo");
        colMaximo.setCellValueFactory(new PropertyValueFactory<>("stockMaximo"));
        colMaximo.setPrefWidth(100);

        // Columna Ubicación (usa "ubicacion")
        TableColumn<Producto, String> colUbicacion = new TableColumn<>("Ubicación");
        colUbicacion.setCellValueFactory(new PropertyValueFactory<>("ubicacion"));
        colUbicacion.setPrefWidth(100);

        // Columna Estado (Calculada, usa "getStock()" y "getStockMinimo()")
        TableColumn<Producto, String> colEstado = new TableColumn<>("Estado");
        colEstado.setCellValueFactory(param -> {
            Producto p = param.getValue();
            // Corregido: usa getStock() en lugar de getStockActual()
            String estado = (p.getStock() <= p.getStockMinimo()) ? "🚨 BAJO STOCK" : "EN STOCK";
            return new javafx.beans.property.SimpleStringProperty(estado);
        });
        colEstado.setPrefWidth(120);

        // Añadimos las nuevas columnas
        tablaProductos.getColumns().addAll(colId, colTipo, colStock, colMinimo, colMaximo, colUbicacion, colEstado);
        tablaProductos.setItems(listaProductos);
    }

    // (cargarProductos no necesita cambios)
    private void cargarProductos() {
        listaProductos.clear();
        List<Producto> productos = productoService.obtenerTodosLosProductos();
        listaProductos.addAll(productos);
    }

    // --- MÉTODO ACTUALIZADO ---
    private void agregarProducto() {
        try {
            // Leemos los nuevos campos
            String tipoDeProducto = txtTipoDeProducto.getText().trim();
            int stock = Integer.parseInt(txtStock.getText().trim());
            int stockMinimo = Integer.parseInt(txtStockMinimo.getText().trim());
            int stockMaximo = Integer.parseInt(txtStockMaximo.getText().trim());
            String unidadMedida = txtUnidadDeMedida.getText().trim();
            String ubicacion = txtUbicacion.getText().trim();

            // Convertimos la fecha del DatePicker
            LocalDate localDate = dpFechaDeCaducidad.getValue();
            Date fechaCaducidad = (localDate != null) ? Date.valueOf(localDate) : null;

            if (tipoDeProducto.isEmpty()) {
                mostrarAlerta("Error", "El tipo de producto es obligatorio.", Alert.AlertType.ERROR);
                return;
            }

            // Calculamos el estado de stock
            String estadoStock = (stock <= stockMinimo) ? "BAJO_STOCK" : "EN_STOCK";

            // Creamos el nuevo producto usando setters (más limpio)
            Producto nuevo = new Producto();
            nuevo.setTipoDeProducto(tipoDeProducto);
            nuevo.setStock(stock);
            nuevo.setStockMinimo(stockMinimo);
            nuevo.setStockMaximo(stockMaximo);
            nuevo.setEstadoStock(estadoStock); // Guardamos el estado calculado
            nuevo.setFechaDeCaducidad(fechaCaducidad);
            nuevo.setUnidadDeMedida(unidadMedida);
            nuevo.setUbicacion(ubicacion);

            // Asumimos que el service tiene un método 'agregarNuevoProducto' que recibe el objeto
            // (Este nombre de método 'agregarNuevoProducto' viene de tu código original)
            if (productoService.agregarNuevoProducto(nuevo)) {
                mostrarAlerta("Éxito", "Producto agregado correctamente.", Alert.AlertType.INFORMATION);
                limpiarCampos();
                cargarProductos();
            } else {
                mostrarAlerta("Error", "No se pudo agregar el producto.", Alert.AlertType.ERROR);
            }
        } catch (Exception ex) {
            mostrarAlerta("Error", "Verifique los datos ingresados.", Alert.AlertType.ERROR);
        }
    }

    // (aumentarStock no necesita cambios, ya que funciona con ID)
    private void aumentarStock() {
        try {
            int id = Integer.parseInt(txtIdActualizar.getText().trim());
            int cantidad = Integer.parseInt(txtCantidadActualizar.getText().trim());

            if (cantidad <= 0) {
                mostrarAlerta("Error", "La cantidad debe ser positiva.", Alert.AlertType.WARNING);
                return;
            }

            // Asumimos que el service 'aumentarStockProducto'
            // 1. Obtiene el stock actual
            // 2. Calcula el nuevo stock
            // 3. Llama al DAO 'actualizarStock(id, nuevoStock)'
            // 4. (Idealmente) recalcula y actualiza también el 'Estado_Stock'
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

    // (eliminarProducto no necesita cambios, ya que funciona con ID)
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

    // --- MÉTODO ACTUALIZADO ---
    private void limpiarCampos() {
        txtTipoDeProducto.clear();
        txtUnidadDeMedida.clear();
        txtUbicacion.clear();
        dpFechaDeCaducidad.setValue(null);

        txtStock.setText("0");
        txtStockMinimo.setText("0");
        txtStockMaximo.setText("0");
    }

    // (mostrarAlerta no necesita cambios)
    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}