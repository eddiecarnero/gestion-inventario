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
import javafx.util.StringConverter;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RecetasPage extends BorderPane {

    // --- Variables de UI ---
    private final TableView<IngredienteItem> tablaItems;
    private final TextField nombreRecetaField;
    private final TextField cantidadProducidaField;
    private final ComboBox<String> unidadProducidaCombo;
    private final ComboBox<ProductoSimple> ingredienteCombo;
    private final TextField cantidadIngredienteField;
    private final ComboBox<String> unidadIngredienteCombo;

    // --- Listas de Datos ---
    private final ObservableList<IngredienteItem> items = FXCollections.observableArrayList();
    private final ObservableList<ProductoSimple> todosLosProductos = FXCollections.observableArrayList();

    // (CSS_STYLES ... sin cambios)
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
            -fx-border-color: #E0E0E0; /* BORDER_LIGHT */
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
        .card-description {
            -fx-font-size: 1em;
            -fx-text-fill: #555555;
        }
        .label {
            -fx-font-size: 1.05em;
            -fx-font-weight: 500;
            -fx-text-fill: #333333;
        }
        .combo-box, .text-field, .date-picker {
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
        .button-add {
            -fx-background-color: #22C55E;
            -fx-text-fill: white;
            -fx-font-weight: bold;
            -fx-font-size: 1.1em;
            -fx-pref-height: 38px;
            -fx-background-radius: 5;
            -fx-cursor: hand;
        }
        .button-danger {
            -fx-background-color: transparent;
            -fx-text-fill: #EF4444;
            -fx-font-size: 1.4em;
            -fx-cursor: hand;
            -fx-padding: 5;
        }
        .button-danger:hover {
            -fx-background-color: #FEE2E2;
        }
        .table-view .column-header {
            -fx-background-color: #F9FAFB;
            -fx-font-weight: bold;
            -fx-font-size: 1.05em;
        }
    """;

    public RecetasPage() {
        this.getStylesheets().add("data:text/css," + CSS_STYLES.replace("\n", ""));
        this.getStyleClass().add("root");
        setPadding(new Insets(30, 40, 30, 40));

        // --- Inicializar campos ---
        tablaItems = new TableView<>();
        nombreRecetaField = new TextField();
        cantidadProducidaField = new TextField();
        unidadProducidaCombo = new ComboBox<>();
        ingredienteCombo = new ComboBox<>();
        cantidadIngredienteField = new TextField();
        unidadIngredienteCombo = new ComboBox<>();

        // --- Estructura Principal ---
        VBox mainContent = new VBox(20);

        // 1. Header
        VBox headerBox = new VBox(5);
        Label header = new Label("Gestión de Recetas");
        header.getStyleClass().add("header-title");
        Label description = new Label("Crear y gestionar recetas y sus ingredientes");
        description.getStyleClass().add("header-description");
        headerBox.getChildren().addAll(header, description);

        // 2. TabPane
        TabPane tabPane = new TabPane();
        tabPane.getStyleClass().add("tab-pane");

        Tab tabNueva = new Tab("Nueva Receta", crearTabNuevaReceta());
        tabNueva.setClosable(false);
        Tab tabHistorial = new Tab("Historial", crearTabHistorial());
        tabHistorial.setClosable(false);
        // Recargar historial al seleccionar
        tabHistorial.setOnSelectionChanged(e -> {
            if (tabHistorial.isSelected()) {
                cargarHistorial((VBox) ((ScrollPane) ((VBox) tabHistorial.getContent()).getChildren().get(0)).getContent());
            }
        });

        tabPane.getTabs().addAll(tabNueva, tabHistorial);

        mainContent.getChildren().addAll(headerBox, tabPane);
        setCenter(mainContent);

        cargarProductos();
    }

    private Node crearTabNuevaReceta() {
        VBox layout = new VBox(20);
        layout.getStyleClass().add("tab-content-area");

        // --- Card Principal ---
        VBox card = new VBox(25);
        card.getStyleClass().add("card");

        // Título de la Card
        Text icon = new Text("🍳");
        icon.setFont(Font.font(20));
        Label cardTitle = new Label("Crear Nueva Receta");
        cardTitle.getStyleClass().add("card-title");
        HBox cardHeader = new HBox(10, icon, cardTitle);
        cardHeader.setAlignment(Pos.CENTER_LEFT);

        Label cardDescription = new Label("Define el producto final y sus ingredientes.");
        cardDescription.getStyleClass().add("card-description");
        VBox cardHeaderBox = new VBox(5, cardHeader, cardDescription);

        // --- Formulario de Receta (Header) ---
        GridPane gridSup = new GridPane();
        gridSup.setHgap(20);
        gridSup.setVgap(10);

        nombreRecetaField.setPromptText("Ej. Pie de Limón");
        gridSup.add(crearCampo("Nombre de la Receta", nombreRecetaField), 0, 0);

        cantidadProducidaField.setPromptText("Ej. 1");
        gridSup.add(crearCampo("Cantidad Producida", cantidadProducidaField), 1, 0);

        unidadProducidaCombo.getItems().addAll("Unidad", "Kg", "Litro", "Porción");
        unidadProducidaCombo.getSelectionModel().selectFirst();
        gridSup.add(crearCampo("Unidad Producida", unidadProducidaCombo), 2, 0);

        ColumnConstraints col1 = new ColumnConstraints(); col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints(); col2.setPercentWidth(25);
        ColumnConstraints col3 = new ColumnConstraints(); col3.setPercentWidth(25);
        gridSup.getColumnConstraints().addAll(col1, col2, col3);

        // --- "Agregar Items" Box ---
        VBox addItemsBox = new VBox(15);
        addItemsBox.setPadding(new Insets(15));
        addItemsBox.setStyle("-fx-border-color: #E0E0E0; -fx-border-radius: 8; -fx-background-radius: 8;");
        Label addItemsTitle = new Label("Agregar Ingredientes");
        addItemsTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));

        GridPane addItemsGrid = new GridPane();
        addItemsGrid.setHgap(15);
        addItemsGrid.setVgap(10);

        ColumnConstraints colIng = new ColumnConstraints(); colIng.setPercentWidth(50);
        ColumnConstraints colCant = new ColumnConstraints(); colCant.setPercentWidth(15);
        ColumnConstraints colUni = new ColumnConstraints(); colUni.setPercentWidth(15);
        ColumnConstraints colBtn = new ColumnConstraints(); colBtn.setPercentWidth(20);
        addItemsGrid.getColumnConstraints().addAll(colIng, colCant, colUni, colBtn);

        ingredienteCombo.setPromptText("Seleccionar producto");
        ingredienteCombo.setMaxWidth(Double.MAX_VALUE);
        ingredienteCombo.setItems(todosLosProductos);
        ingredienteCombo.setConverter(new ProductoSimpleConverter());
        addItemsGrid.add(crearCampo("Ingrediente (de Almacén 1)", ingredienteCombo), 0, 0);

        cantidadIngredienteField.setPromptText("0");
        addItemsGrid.add(crearCampo("Cantidad", cantidadIngredienteField), 1, 0);

        unidadIngredienteCombo.getItems().addAll("Kg", "Gramo", "Litro", "Unidad", "Cucharada");
        unidadIngredienteCombo.getSelectionModel().selectFirst();
        addItemsGrid.add(crearCampo("Unidad", unidadIngredienteCombo), 2, 0);

        Button addButton = new Button("➕ Agregar");
        addButton.getStyleClass().add("button-add");
        addButton.setMaxWidth(Double.MAX_VALUE);
        addButton.setOnAction(e -> agregarItem());
        VBox btnBox = new VBox(addButton);
        btnBox.setAlignment(Pos.BOTTOM_CENTER);
        btnBox.setPadding(new Insets(19, 0, 0, 0));
        addItemsGrid.add(btnBox, 3, 0);

        addItemsBox.getChildren().addAll(addItemsTitle, addItemsGrid);

        // --- Tabla de Items ---
        configurarTabla();
        VBox.setVgrow(tablaItems, Priority.ALWAYS);

        // --- Botón Guardar Receta ---
        Button crearRecetaBtn = new Button("💾 Guardar Receta");
        crearRecetaBtn.getStyleClass().add("button-primary");
        crearRecetaBtn.setMaxWidth(Double.MAX_VALUE);
        crearRecetaBtn.setOnAction(e -> guardarReceta());

        // Ensamblar Card
        card.getChildren().addAll(cardHeaderBox, gridSup, addItemsBox, tablaItems, crearRecetaBtn);
        layout.getChildren().add(card);
        return layout;
    }

    private void configurarTabla() {
        tablaItems.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<IngredienteItem, String> ingredienteCol = new TableColumn<>("Ingrediente");
        ingredienteCol.setCellValueFactory(new PropertyValueFactory<>("nombre"));

        TableColumn<IngredienteItem, Double> cantidadCol = new TableColumn<>("Cantidad");
        cantidadCol.setCellValueFactory(new PropertyValueFactory<>("cantidad"));

        TableColumn<IngredienteItem, String> unidadCol = new TableColumn<>("Unidad");
        unidadCol.setCellValueFactory(new PropertyValueFactory<>("unidad"));

        TableColumn<IngredienteItem, Void> accionCol = new TableColumn<>("Acción");
        accionCol.setCellFactory(param -> new TableCell<>() {
            private final Button deleteButton = new Button("🗑️");
            {
                deleteButton.getStyleClass().add("button-danger");
                deleteButton.setOnAction(event -> {
                    IngredienteItem item = getTableView().getItems().get(getIndex());
                    eliminarItem(item);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteButton);
                setAlignment(Pos.CENTER);
            }
        });

        tablaItems.getColumns().addAll(ingredienteCol, cantidadCol, unidadCol, accionCol);
        tablaItems.setItems(items);
        tablaItems.setPlaceholder(new Label("Agregue ingredientes a la receta"));
    }

    private Node crearTabHistorial() {
        VBox layout = new VBox(20);
        layout.getStyleClass().add("tab-content-area");

        VBox card = new VBox(15);
        card.getStyleClass().add("card");
        Label cardTitle = new Label("Historial de Recetas");
        cardTitle.getStyleClass().add("card-title");
        Label cardDescription = new Label("Todas las recetas guardadas");
        cardDescription.getStyleClass().add("card-description");
        card.getChildren().addAll(cardTitle, cardDescription);

        VBox historialContainer = new VBox(10);
        historialContainer.setPadding(new Insets(10, 0, 10, 0));

        ScrollPane scrollPane = new ScrollPane(historialContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background-color: transparent;");

        // Cargar datos
        cargarHistorial(historialContainer);

        card.getChildren().add(scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        layout.getChildren().add(card);
        return layout;
    }

    private void cargarHistorial(VBox container) {
        container.getChildren().clear();
        String sqlReceta = "SELECT id, nombre, cantidad_producida, unidad_producida, fecha_creacion " +
                "FROM recetas ORDER BY fecha_creacion DESC";

        // --- CAMBIO: Leer IdProducto y unir con 'producto' para obtener el nombre ---
        String sqlIngred = "SELECT p.Tipo_de_Producto, i.cantidad, i.unidad " +
                "FROM ingredientes i " +
                "JOIN producto p ON i.IdProducto = p.IdProducto " +
                "WHERE i.receta_id = ?";

        try (Connection conn = ConexionBD.getConnection();
             Statement stmtReceta = conn.createStatement();
             ResultSet rsReceta = stmtReceta.executeQuery(sqlReceta)) {

            while (rsReceta.next()) {
                int idReceta = rsReceta.getInt("id");
                String nombre = rsReceta.getString("nombre");
                double cant = rsReceta.getDouble("cantidad_producida");
                String unidad = rsReceta.getString("unidad_producida");

                List<IngredienteItem> itemsHistorial = new ArrayList<>();
                try (PreparedStatement stmtIngred = conn.prepareStatement(sqlIngred)) {
                    stmtIngred.setInt(1, idReceta);
                    ResultSet rsIngred = stmtIngred.executeQuery();
                    while (rsIngred.next()) {
                        itemsHistorial.add(new IngredienteItem(
                                0, // El ID no importa para mostrar el historial
                                rsIngred.getString("Tipo_de_Producto"), // <-- Nombre del producto
                                rsIngred.getDouble("cantidad"),
                                rsIngred.getString("unidad")
                        ));
                    }
                }

                RecetaHistorial receta = new RecetaHistorial(idReceta, nombre, cant, unidad, itemsHistorial);
                container.getChildren().add(crearCardReceta(receta));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error de BD", "No se pudo cargar el historial de recetas: " + e.getMessage());
        }
    }

    private Node crearCardReceta(RecetaHistorial receta) {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");
        card.setStyle("-fx-border-color: #E0E0E0;");

        Label titulo = new Label(receta.getNombre());
        titulo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        Label subtitulo = new Label(String.format("Produce: %.2f %s", receta.getCantidadProducida(), receta.getUnidadProducida()));
        subtitulo.getStyleClass().add("card-description");
        VBox headerBox = new VBox(2, titulo, subtitulo);

        VBox itemsBox = new VBox(5);
        itemsBox.setPadding(new Insets(10, 0, 0, 15));

        if (receta.getItems().isEmpty()) {
            itemsBox.getChildren().add(new Label("Esta receta no tiene ingredientes registrados."));
        } else {
            for (IngredienteItem item : receta.getItems()) {
                Label itemLabel = new Label(String.format("• %s (%.2f %s)",
                        item.getNombre(), item.getCantidad(), item.getUnidad()));
                itemsBox.getChildren().add(itemLabel);
            }
        }

        card.getChildren().addAll(headerBox, new Separator(), itemsBox);
        return card;
    }

    private VBox crearCampo(String labelText, Control input) {
        VBox vbox = new VBox(5);
        Label label = new Label(labelText);
        label.getStyleClass().add("label");
        vbox.getChildren().addAll(label, input);
        return vbox;
    }

    private void agregarItem() {
        try {
            // --- CAMBIO: Lee del ComboBox ---
            ProductoSimple productoSeleccionado = ingredienteCombo.getValue();
            double cantidad = Double.parseDouble(cantidadIngredienteField.getText().trim());
            String unidad = unidadIngredienteCombo.getValue();

            if (productoSeleccionado == null || cantidad <= 0) {
                mostrarAlerta("Error", "Seleccione un producto e ingrese una cantidad válida.");
                return;
            }

            // --- CAMBIO: Guarda el ID y el Nombre ---
            items.add(new IngredienteItem(
                    productoSeleccionado.getId(),
                    productoSeleccionado.getNombre(),
                    cantidad,
                    unidad
            ));
            limpiarCamposItem();

        } catch (NumberFormatException e) {
            mostrarAlerta("Error", "La cantidad del ingrediente debe ser un número.");
        }
    }

    private void eliminarItem(IngredienteItem item) {
        items.remove(item);
    }

    private void limpiarCamposItem() {
        ingredienteCombo.getSelectionModel().clearSelection();
        cantidadIngredienteField.clear();
        unidadIngredienteCombo.getSelectionModel().selectFirst();
    }

    private void limpiarFormularioReceta() {
        nombreRecetaField.clear();
        cantidadProducidaField.clear();
        unidadProducidaCombo.getSelectionModel().selectFirst();
        items.clear();
        limpiarCamposItem();
    }

    private void guardarReceta() {
        String nombreReceta = nombreRecetaField.getText().trim();
        String unidadReceta = unidadProducidaCombo.getValue();
        double cantidadReceta;

        try {
            cantidadReceta = Double.parseDouble(cantidadProducidaField.getText().trim());
        } catch (NumberFormatException e) {
            mostrarAlerta("Error", "La 'Cantidad Producida' de la receta debe ser un número.");
            return;
        }

        if (nombreReceta.isEmpty() || cantidadReceta <= 0) {
            mostrarAlerta("Error", "Complete los campos de la receta (Nombre, Cantidad).");
            return;
        }
        if (items.isEmpty()) {
            mostrarAlerta("Error", "Agregue al menos un ingrediente a la receta.");
            return;
        }

        Connection conn = null;
        try {
            conn = ConexionBD.getConnection();
            conn.setAutoCommit(false);

            String sqlReceta = "INSERT INTO recetas (nombre, cantidad_producida, unidad_producida) " +
                    "VALUES (?, ?, ?)";
            int idRecetaGenerada;
            try (PreparedStatement stmtReceta = conn.prepareStatement(sqlReceta, Statement.RETURN_GENERATED_KEYS)) {
                stmtReceta.setString(1, nombreReceta);
                stmtReceta.setDouble(2, cantidadReceta);
                stmtReceta.setString(3, unidadReceta);

                stmtReceta.executeUpdate();
                ResultSet rsKeys = stmtReceta.getGeneratedKeys();
                if (rsKeys.next()) {
                    idRecetaGenerada = rsKeys.getInt(1);
                } else {
                    throw new SQLException("No se pudo obtener el ID de la receta.");
                }
            }

            // --- CAMBIO: Insertar IdProducto en lugar de nombre ---
            String sqlIngred = "INSERT INTO ingredientes (receta_id, IdProducto, cantidad, unidad) " +
                    "VALUES (?, ?, ?, ?)";
            try (PreparedStatement stmtIngred = conn.prepareStatement(sqlIngred)) {
                for (IngredienteItem item : items) {
                    stmtIngred.setInt(1, idRecetaGenerada);
                    stmtIngred.setInt(2, item.getIdProducto()); // <-- ID del producto
                    stmtIngred.setDouble(3, item.getCantidad());
                    stmtIngred.setString(4, item.getUnidad());
                    stmtIngred.addBatch();
                }
                stmtIngred.executeBatch();
            }

            conn.commit();
            mostrarAlerta("Éxito", "✅ Receta guardada correctamente.");
            limpiarFormularioReceta();

        } catch (SQLException ex) {
            if (conn != null) try { conn.rollback(); } catch (SQLException e) { e.printStackTrace(); }
            ex.printStackTrace();
            mostrarAlerta("Error", "Ocurrió un problema: " + ex.getMessage());
        } finally {
            if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    private void cargarProductos() {
        todosLosProductos.clear();
        String sql = "SELECT IdProducto, Tipo_de_Producto FROM producto";
        try (Connection conn = ConexionBD.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                todosLosProductos.add(new ProductoSimple(
                        rs.getInt("IdProducto"),
                        rs.getString("Tipo_de_Producto")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudieron cargar los productos de Almacén 1.");
        }
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

    // --- Clases de Datos Internas ---

    public static class ProductoSimple {
        private final int id;
        private final String nombre;

        public ProductoSimple(int id, String nombre) { this.id = id; this.nombre = nombre; }
        public int getId() { return id; }
        public String getNombre() { return nombre; }
        @Override
        public String toString() { return nombre; }
    }

    public static class ProductoSimpleConverter extends StringConverter<ProductoSimple> {
        @Override
        public String toString(ProductoSimple producto) {
            return (producto == null) ? null : producto.getNombre();
        }
        @Override
        public ProductoSimple fromString(String string) { return null; }
    }

    /**
     * CAMBIO: Ahora guarda el IdProducto
     */
    public static class IngredienteItem {
        private final int idProducto; // <-- NUEVO
        private final String nombre;
        private final double cantidad;
        private final String unidad;

        public IngredienteItem(int idProducto, String nombre, double cantidad, String unidad) {
            this.idProducto = idProducto; // <-- NUEVO
            this.nombre = nombre;
            this.cantidad = cantidad;
            this.unidad = unidad;
        }

        public int getIdProducto() { return idProducto; } // <-- NUEVO
        public String getNombre() { return nombre; }
        public double getCantidad() { return cantidad; }
        public String getUnidad() { return unidad; }
    }

    public static class RecetaHistorial {
        private final int id;
        private final String nombre;
        private final double cantidadProducida;
        private final String unidadProducida;
        private final List<IngredienteItem> items;

        public RecetaHistorial(int id, String nombre, double cantidad, String unidad, List<IngredienteItem> items) {
            this.id = id;
            this.nombre = nombre;
            this.cantidadProducida = cantidad;
            this.unidadProducida = unidad;
            this.items = items;
        }

        public int getId() { return id; }
        public String getNombre() { return nombre; }
        public double getCantidadProducida() { return cantidadProducida; }
        public String getUnidadProducida() { return unidadProducida; }
        public List<IngredienteItem> getItems() { return items; }
    }

    // --- Clase de Test para Ejecutar ---
    public static class TestApp extends Application {
        @Override
        public void start(Stage stage) {
            stage.setTitle("Gestión de Recetas - JavaFX");
            stage.setScene(new Scene(new RecetasPage(), 1300, 900));
            stage.show();
        }
    }

    public static void main(String[] args) {
        Application.launch(TestApp.class, args);
    }
}