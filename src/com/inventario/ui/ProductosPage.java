package com.inventario.ui;

import com.inventario.config.ConexionBD;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.sql.*;
import java.util.HashMap;

public class ProductosPage extends BorderPane {

    // --- Estilos (Mismos que en las otras páginas) ---
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
        .card {
            -fx-background-color: white;
            -fx-border-color: #E0E0E0;
            -fx-border-width: 1;
            -fx-border-radius: 8;
            -fx-background-radius: 8;
            -fx-padding: 20px;
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
        .combo-box, .text-field {
            -fx-font-size: 1.05em;
            -fx-pref-height: 38px;
            -fx-border-color: #CCCCCC;
            -fx-border-radius: 5;
            -fx-background-radius: 5;
        }
        .button-primary {
            -fx-background-color: #4A90E2; /* PRIMARY */
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
        .button-secondary {
            -fx-background-color: #777777;
            -fx-text-fill: white;
            -fx-font-weight: bold;
            -fx-font-size: 1.1em;
            -fx-pref-height: 40px;
            -fx-background-radius: 5;
            -fx-cursor: hand;
        }
        .table-view .column-header {
            -fx-background-color: #F9FAFB;
            -fx-font-weight: bold;
            -fx-font-size: 1.05em;
        }
    """;

    // --- Variables de UI ---
    private final TableView<Producto> tablaProductos;
    private final TextField nombreField;
    private final ComboBox<Proveedor> proveedorCombo;
    private final TextField precioField;
    private final TextField stockMinField;
    private final TextField stockMaxField;
    private final TextField stockInicialField;
    private final TextField unidadField;
    private final TextField ubicacionField;
    private final Button guardarButton;
    private final Button limpiarButton;

    // Label oculta para guardar el ID del producto seleccionado
    private final Label idProductoLabel;

    // --- Listas de Datos ---
    private final ObservableList<Producto> listaProductos = FXCollections.observableArrayList();
    private final ObservableList<Proveedor> listaProveedores = FXCollections.observableArrayList();
    private final HashMap<Integer, Proveedor> mapaProveedores = new HashMap<>();

    public ProductosPage() {
        this.getStylesheets().add("data:text/css," + CSS_STYLES.replace("\n", ""));
        this.getStyleClass().add("root");
        setPadding(new Insets(30, 40, 30, 40));

        // --- Inicializar campos ---
        tablaProductos = new TableView<>();
        nombreField = new TextField();
        proveedorCombo = new ComboBox<>();
        precioField = new TextField();
        stockMinField = new TextField();
        stockMaxField = new TextField();
        stockInicialField = new TextField();
        unidadField = new TextField();
        ubicacionField = new TextField();
        guardarButton = new Button("💾 Guardar");
        limpiarButton = new Button("✨ Limpiar");
        idProductoLabel = new Label(); // Oculta

        VBox mainContent = new VBox(20);

        // 1. Header
        VBox headerBox = new VBox(5);
        Label header = new Label("Gestión de Productos");
        header.getStyleClass().add("header-title");
        Label description = new Label("Crear y editar productos del inventario maestro (insumos)");
        description.getStyleClass().add("header-description");
        headerBox.getChildren().addAll(header, description);

        // 2. Formulario de Creación/Edición
        Node formularioCard = crearFormulario();

        // 3. Tabla de Productos
        Node tablaCard = crearTablaProductos();

        mainContent.getChildren().addAll(headerBox, formularioCard, tablaCard);
        setCenter(mainContent);

        // --- Cargar Datos y Asignar Acciones ---
        cargarProveedores();
        cargarDatosTabla();

        // Acción al seleccionar un item de la tabla
        tablaProductos.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        popularFormulario(newSelection);
                    }
                }
        );

        // Acciones de botones
        limpiarButton.setOnAction(e -> limpiarFormulario());
        guardarButton.setOnAction(e -> guardarProducto());
    }

    /**
     * Crea la Card que contiene el formulario de edición/creación.
     */
    private Node crearFormulario() {
        VBox card = new VBox(15);
        card.getStyleClass().add("card");

        Label cardTitle = new Label("Detalles del Producto");
        cardTitle.getStyleClass().add("card-title");
        card.getChildren().add(cardTitle);

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(15);

        // Fila 1
        grid.add(crearCampo("Nombre Producto", nombreField), 0, 0);

        // Configurar ComboBox de Proveedor
        proveedorCombo.setConverter(new ProveedorStringConverter());
        proveedorCombo.setItems(listaProveedores);
        grid.add(crearCampo("Proveedor", proveedorCombo), 1, 0);

        grid.add(crearCampo("Precio Unitario ($)", precioField), 2, 0);

        // Fila 2
        grid.add(crearCampo("Stock Mínimo", stockMinField), 0, 1);
        grid.add(crearCampo("Stock Máximo", stockMaxField), 1, 1);
        grid.add(crearCampo("Stock Inicial", stockInicialField), 2, 1);

        // Fila 3
        grid.add(crearCampo("Unidad (Kg, Lt, Un)", unidadField), 0, 2);
        grid.add(crearCampo("Ubicación (Almacén)", ubicacionField), 1, 2);

        // Configurar columnas del grid
        ColumnConstraints col1 = new ColumnConstraints(); col1.setPercentWidth(40);
        ColumnConstraints col2 = new ColumnConstraints(); col2.setPercentWidth(30);
        ColumnConstraints col3 = new ColumnConstraints(); col3.setPercentWidth(30);
        grid.getColumnConstraints().addAll(col1, col2, col3);

        // Botones
        guardarButton.getStyleClass().add("button-primary");
        limpiarButton.getStyleClass().add("button-secondary");
        HBox buttonBox = new HBox(10, guardarButton, limpiarButton);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        card.getChildren().addAll(grid, buttonBox);
        return card;
    }

    /**
     * Crea la Card que contiene la tabla de productos.
     */
    private Node crearTablaProductos() {
        VBox card = new VBox(15);
        card.getStyleClass().add("card");

        Label cardTitle = new Label("Listado de Productos en Inventario");
        cardTitle.getStyleClass().add("card-title");

        configurarTabla();

        card.getChildren().addAll(cardTitle, tablaProductos);
        VBox.setVgrow(tablaProductos, Priority.ALWAYS);
        return card;
    }

    private void configurarTabla() {
        tablaProductos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tablaProductos.setItems(listaProductos);

        TableColumn<Producto, String> nombreCol = new TableColumn<>("Nombre");
        nombreCol.setCellValueFactory(new PropertyValueFactory<>("nombre"));

        TableColumn<Producto, String> provCol = new TableColumn<>("Proveedor");
        provCol.setCellValueFactory(new PropertyValueFactory<>("proveedorNombre"));

        TableColumn<Producto, Double> precioCol = new TableColumn<>("Precio");
        precioCol.setCellValueFactory(new PropertyValueFactory<>("precioUnitario"));
        precioCol.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                setText(empty ? null : String.format("$%.2f", price));
            }
        });

        TableColumn<Producto, Integer> stockCol = new TableColumn<>("Stock");
        stockCol.setCellValueFactory(new PropertyValueFactory<>("stock"));

        TableColumn<Producto, Integer> stockMinCol = new TableColumn<>("Stock Mín.");
        stockMinCol.setCellValueFactory(new PropertyValueFactory<>("stockMinimo"));

        TableColumn<Producto, String> unidadCol = new TableColumn<>("Unidad");
        unidadCol.setCellValueFactory(new PropertyValueFactory<>("unidad"));

        tablaProductos.getColumns().addAll(nombreCol, provCol, precioCol, stockCol, stockMinCol, unidadCol);
        tablaProductos.setPlaceholder(new Label("No hay productos en el inventario."));
    }

    // --- Métodos de Lógica ---

    private void cargarProveedores() {
        listaProveedores.clear();
        mapaProveedores.clear();
        String sql = "SELECT IdProveedor, Nombre_comercial FROM proveedores";
        try (Connection conn = ConexionBD.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Proveedor p = new Proveedor(rs.getInt("IdProveedor"), rs.getString("Nombre_comercial"));
                listaProveedores.add(p);
                mapaProveedores.put(p.getId(), p);
            }
        } catch (SQLException e) {
            mostrarAlerta("Error", "No se pudo cargar la lista de proveedores.");
        }
    }

    private void cargarDatosTabla() {
        listaProductos.clear();
        String sql = "SELECT * FROM producto";
        try (Connection conn = ConexionBD.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Producto p = new Producto(
                        rs.getInt("IdProducto"),
                        rs.getString("Tipo_de_Producto"),
                        rs.getInt("Stock"),
                        rs.getInt("Stock_Minimo"),
                        rs.getInt("Stock_Maximo"),
                        rs.getString("Unidad_de_medida"),
                        rs.getString("Ubicacion"),
                        rs.getInt("IdProveedor"),
                        rs.getDouble("PrecioUnitario")
                );
                // Asignar nombre del proveedor
                if (mapaProveedores.containsKey(p.getIdProveedor())) {
                    p.setProveedorNombre(mapaProveedores.get(p.getIdProveedor()).getNombre());
                }
                listaProductos.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo cargar los productos: " + e.getMessage());
        }
    }

    private void popularFormulario(Producto p) {
        idProductoLabel.setText(String.valueOf(p.getId()));
        nombreField.setText(p.getNombre());
        precioField.setText(String.format("%.2f", p.getPrecioUnitario()));
        stockMinField.setText(String.valueOf(p.getStockMinimo()));
        stockMaxField.setText(String.valueOf(p.getStockMaximo()));
        stockInicialField.setText(String.valueOf(p.getStock()));
        stockInicialField.setDisable(true); // No se puede editar stock aquí (usar Kardex)
        unidadField.setText(p.getUnidad());
        ubicacionField.setText(p.getUbicacion());

        // Seleccionar proveedor en ComboBox
        proveedorCombo.setValue(mapaProveedores.get(p.getIdProveedor()));

        guardarButton.setText("💾 Actualizar");
    }

    private void limpiarFormulario() {
        idProductoLabel.setText("");
        nombreField.clear();
        precioField.clear();
        stockMinField.clear();
        stockMaxField.clear();
        stockInicialField.clear();
        stockInicialField.setDisable(false); // Habilitar para nuevos productos
        unidadField.clear();
        ubicacionField.clear();
        proveedorCombo.getSelectionModel().clearSelection();

        guardarButton.setText("💾 Guardar Nuevo");
        tablaProductos.getSelectionModel().clearSelection();
    }

    private void guardarProducto() {
        // Validaciones
        if (nombreField.getText().isEmpty() ||
                precioField.getText().isEmpty() ||
                stockMinField.getText().isEmpty() ||
                proveedorCombo.getValue() == null) {
            mostrarAlerta("Error", "Complete todos los campos obligatorios (Nombre, Proveedor, Precio, Stock Mín).");
            return;
        }

        try {
            String nombre = nombreField.getText();
            int idProveedor = proveedorCombo.getValue().getId();
            double precio = Double.parseDouble(precioField.getText());
            int stockMin = Integer.parseInt(stockMinField.getText());
            int stockMax = stockMaxField.getText().isEmpty() ? 100 : Integer.parseInt(stockMaxField.getText());
            int stock = stockInicialField.getText().isEmpty() ? 0 : Integer.parseInt(stockInicialField.getText());
            String unidad = unidadField.getText();
            String ubicacion = ubicacionField.getText();

            // Decidir si es INSERT o UPDATE
            if (idProductoLabel.getText().isEmpty()) {
                // INSERT
                accionInsertar(nombre, idProveedor, precio, stockMin, stockMax, stock, unidad, ubicacion);
            } else {
                // UPDATE
                int idProducto = Integer.parseInt(idProductoLabel.getText());
                accionActualizar(idProducto, nombre, idProveedor, precio, stockMin, stockMax, unidad, ubicacion);
            }

        } catch (NumberFormatException e) {
            mostrarAlerta("Error", "Los campos de precio y stock deben ser numéricos.");
        }
    }

    private void accionInsertar(String nombre, int idProveedor, double precio, int stockMin, int stockMax, int stock, String unidad, String ubicacion) {
        String sql = "INSERT INTO producto (Tipo_de_Producto, Stock, Stock_Minimo, Stock_Maximo, Unidad_de_medida, Ubicacion, IdProveedor, PrecioUnitario) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nombre);
            stmt.setInt(2, stock);
            stmt.setInt(3, stockMin);
            stmt.setInt(4, stockMax);
            stmt.setString(5, unidad);
            stmt.setString(6, ubicacion);
            stmt.setInt(7, idProveedor);
            stmt.setDouble(8, precio);

            int filas = stmt.executeUpdate();
            if (filas > 0) {
                mostrarAlerta("Éxito", "Producto creado correctamente.");
                cargarDatosTabla(); // Recargar la tabla
                limpiarFormulario(); // Limpiar el formulario
            }
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo guardar el producto: " + e.getMessage());
        }
    }

    private void accionActualizar(int idProducto, String nombre, int idProveedor, double precio, int stockMin, int stockMax, String unidad, String ubicacion) {
        String sql = "UPDATE producto SET Tipo_de_Producto = ?, Stock_Minimo = ?, Stock_Maximo = ?, " +
                "Unidad_de_medida = ?, Ubicacion = ?, IdProveedor = ?, PrecioUnitario = ? " +
                "WHERE IdProducto = ?";

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nombre);
            stmt.setInt(2, stockMin);
            stmt.setInt(3, stockMax);
            stmt.setString(4, unidad);
            stmt.setString(5, ubicacion);
            stmt.setInt(6, idProveedor);
            stmt.setDouble(7, precio);
            stmt.setInt(8, idProducto);

            int filas = stmt.executeUpdate();
            if (filas > 0) {
                mostrarAlerta("Éxito", "Producto actualizado correctamente.");
                cargarDatosTabla(); // Recargar la tabla
                limpiarFormulario(); // Limpiar el formulario
            }
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo actualizar el producto: " + e.getMessage());
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
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    // --- Clases de Datos Internas ---

    // Clase para manejar los Proveedores en el ComboBox
    public static class Proveedor {
        private final int id;
        private final String nombre;
        public Proveedor(int id, String nombre) { this.id = id; this.nombre = nombre; }
        public int getId() { return id; }
        public String getNombre() { return nombre; }
    }

    // Convertidor para que el ComboBox muestre el nombre del Proveedor
    private static class ProveedorStringConverter extends javafx.util.StringConverter<Proveedor> {
        @Override
        public String toString(Proveedor p) {
            return (p == null) ? null : p.getNombre();
        }
        @Override
        public Proveedor fromString(String string) {
            return null; // No necesario para un ComboBox no editable
        }
    }

    // Clase para manejar los Productos en la Tabla
    public static class Producto {
        private final int id;
        private final String nombre;
        private final int stock;
        private final int stockMinimo;
        private final int stockMaximo;
        private final String unidad;
        private final String ubicacion;
        private final int idProveedor;
        private final double precioUnitario;
        private String proveedorNombre;

        public Producto(int id, String nombre, int stock, int stockMinimo, int stockMaximo, String unidad, String ubicacion, int idProveedor, double precioUnitario) {
            this.id = id;
            this.nombre = nombre;
            this.stock = stock;
            this.stockMinimo = stockMinimo;
            this.stockMaximo = stockMaximo;
            this.unidad = unidad;
            this.ubicacion = ubicacion;
            this.idProveedor = idProveedor;
            this.precioUnitario = precioUnitario;
            this.proveedorNombre = "N/A"; // Default
        }

        public int getId() { return id; }
        public String getNombre() { return nombre; }
        public int getStock() { return stock; }
        public int getStockMinimo() { return stockMinimo; }
        public int getStockMaximo() { return stockMaximo; }
        public String getUnidad() { return unidad; }
        public String getUbicacion() { return ubicacion; }
        public int getIdProveedor() { return idProveedor; }
        public double getPrecioUnitario() { return precioUnitario; }
        public String getProveedorNombre() { return proveedorNombre; }
        public void setProveedorNombre(String nombre) { this.proveedorNombre = nombre; }
    }


    // --- Clase de Test para Ejecutar ---
    public static class TestApp extends Application {
        @Override
        public void start(Stage stage) {
            stage.setTitle("Gestión de Productos - JavaFX");
            stage.setScene(new Scene(new ProductosPage(), 1300, 900));
            stage.show();
        }
    }

    public static void main(String[] args) {
        Application.launch(TestApp.class, args);
    }
}