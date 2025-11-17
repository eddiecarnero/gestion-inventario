package com.inventario.ui;

import com.inventario.config.ConexionBD;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class Almacen1Page extends BorderPane {

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
        .card-description {
            -fx-font-size: 1em;
            -fx-text-fill: #555555;
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
        .table-view .column-header {
            -fx-background-color: #F9FAFB;
            -fx-font-weight: bold;
            -fx-font-size: 1.05em;
        }
        .alert-box {
            -fx-background-color: #FEF2F2;
            -fx-border-color: #FEE2E2;
            -fx-border-width: 1;
            -fx-border-radius: 8;
            -fx-background-radius: 8;
            -fx-padding: 15px;
        }
        .alert-text {
            -fx-fill: #991B1B;
            -fx-font-weight: bold;
        }
        .search-field-stack {
            -fx-padding: 0;
        }
        .search-field {
            -fx-font-size: 1.05em;
            -fx-pref-height: 40px;
            -fx-border-color: #CCCCCC;
            -fx-border-radius: 5;
            -fx-background-radius: 5;
            -fx-padding: 5 5 5 35px; /* Padding izquierdo para el icono */
        }
        .stats-grid {
             -fx-padding: 0;
        }
        .stats-card {
            -fx-padding: 15px 20px;
            -fx-background-color: white;
            -fx-border-color: #E0E0E0;
            -fx-border-width: 1;
            -fx-border-radius: 8;
            -fx-background-radius: 8;
        }
        .stats-card-title {
            -fx-font-size: 0.95em;
            -fx-font-weight: 500;
            -fx-text-fill: #333333;
        }
        .stats-card-content {
            -fx-font-size: 1.8em;
            -fx-font-weight: bold;
            -fx-text-fill: #111827;
        }
        .stats-card-content-danger {
            -fx-text-fill: #DC2626;
        }
        .badge {
            -fx-padding: 4 10 4 10;
            -fx-background-radius: 12;
            -fx-font-size: 0.9em;
            -fx-font-weight: bold;
            -fx-text-fill: white;
        }
        .badge-stock-low {
            -fx-background-color: #EF4444; /* red-500 */
        }
        .badge-stock-medium {
            -fx-background-color: #F97316; /* orange-500 */
        }
        .badge-stock-normal {
            -fx-background-color: #22C55E; /* green-500 */
        }
        .cell-stock-low {
            -fx-text-fill: #DC2626;
            -fx-font-weight: bold;
        }
        .cell-vencido {
            -fx-text-fill: #EF4444;
            -fx-font-weight: bold;
        }
        .cell-vence-pronto {
            -fx-text-fill: #F97316;
            -fx-font-weight: bold;
        }
    """;

    // --- Variables de UI ---
    private final VBox mainContent;
    private final HBox alertBoxContainer;
    private Label totalInsumosLabel; // Total de *tipos* de producto
    private Label stockBajoLabel;   // Total de *tipos* de producto con stock bajo
    private Label valorTotalLabel;
    private Label totalProveedoresLabel;
    private TableView<LoteAlmacen> tablaLotes; // --- CAMBIO: La tabla ahora es de Lotes
    private TextField searchField;

    // --- Listas de Datos ---
    private final ObservableList<LoteAlmacen> todosLosLotes = FXCollections.observableArrayList();
    private FilteredList<LoteAlmacen> filteredLotes;
    private int lowStockCount = 0; // Contador para el stat

    // --- Navegación ---
    private final Consumer<String> onNavigate;

    public Almacen1Page(Consumer<String> onNavigate) {
        this.onNavigate = onNavigate;
        this.getStylesheets().add("data:text/css," + CSS_STYLES.replace("\n", ""));
        this.getStyleClass().add("root");
        setPadding(new Insets(30, 40, 30, 40));

        mainContent = new VBox(20);

        // Inicializar variables de clase
        this.tablaLotes = new TableView<>(); // --- CAMBIO
        this.searchField = new TextField();
        this.totalInsumosLabel = new Label("0");
        this.stockBajoLabel = new Label("0");
        this.valorTotalLabel = new Label("$0.00");
        this.totalProveedoresLabel = new Label("0");

        alertBoxContainer = new HBox();

        // 1. Header
        Node header = crearHeader();
        // 2. Barra de Acciones (Búsqueda y Botón)
        Node actionsBar = crearActionsBar();
        // 3. Rejilla de Estadísticas
        Node statsGrid = crearStatsGrid();
        // 4. Tabla de Lotes (antes Insumos)
        Node tableCard = crearTablaLotes();

        mainContent.getChildren().addAll(header, alertBoxContainer, actionsBar, statsGrid, tableCard);
        setCenter(mainContent);

        // Cargar datos y configurar filtros
        cargarDatos();
        setupFiltering();
        actualizarUIConDatos();
    }

    private Node crearHeader() {
        VBox headerBox = new VBox(5);
        Label header = new Label("Almacén 1 - Insumos Básicos");
        header.getStyleClass().add("header-title");
        Label description = new Label("Gestión de lotes de materias primas y stock"); // --- CAMBIO
        description.getStyleClass().add("header-description");
        headerBox.getChildren().addAll(header, description);
        return headerBox;
    }

    private HBox crearAlertBox(int count) {
        HBox alertBox = new HBox(15);
        alertBox.getStyleClass().add("alert-box");
        alertBox.setAlignment(Pos.CENTER_LEFT);
        Text icon = new Text("⚠️");
        icon.setFont(Font.font(20));
        icon.setFill(Color.web("#DC2626"));
        Label alertText = new Label("¡Atención! " + count + " tipo(s) de producto están por debajo del stock mínimo.");
        alertText.getStyleClass().add("alert-text");
        alertBox.getChildren().addAll(icon, alertText);
        return alertBox;
    }

    private Node crearActionsBar() {
        HBox actionsBar = new HBox(15);
        actionsBar.setAlignment(Pos.CENTER_LEFT);

        StackPane searchStack = new StackPane();
        searchStack.getStyleClass().add("search-field-stack");
        searchField.setPromptText("Buscar por producto, lote o proveedor..."); // --- CAMBIO
        searchField.getStyleClass().add("search-field");
        Text searchIcon = new Text("🔍");
        searchIcon.setFill(Color.web("#9CA3AF"));
        StackPane.setAlignment(searchIcon, Pos.CENTER_LEFT);
        StackPane.setMargin(searchIcon, new Insets(0, 0, 0, 12));
        searchStack.getChildren().addAll(searchField, searchIcon);

        Button nuevaOrdenBtn = new Button("🛒 Nueva Orden de Compra");
        nuevaOrdenBtn.getStyleClass().add("button-primary");
        nuevaOrdenBtn.setOnAction(e -> onNavigate.accept("orden-compra"));

        actionsBar.getChildren().addAll(searchStack, nuevaOrdenBtn);
        HBox.setHgrow(searchStack, Priority.ALWAYS);
        return actionsBar;
    }

    private Node crearStatsGrid() {
        GridPane grid = new GridPane();
        grid.getStyleClass().add("stats-grid");
        grid.setHgap(20);
        grid.setVgap(20);

        ColumnConstraints col = new ColumnConstraints();
        col.setPercentWidth(25);
        grid.getColumnConstraints().addAll(col, col, col, col);

        grid.add(createStatCard("Tipos de Producto", totalInsumosLabel, null), 0, 0); // --- CAMBIO
        grid.add(createStatCard("Valor Total (Stock)", valorTotalLabel, null), 1, 0); // --- CAMBIO
        grid.add(createStatCard("Productos (Stock Bajo)", stockBajoLabel, "stats-card-content-danger"), 2, 0); // --- CAMBIO
        grid.add(createStatCard("Proveedores", totalProveedoresLabel, null), 3, 0);

        return grid;
    }

    private Node createStatCard(String title, Label contentLabel, String contentStyleClass) {
        VBox card = new VBox(5);
        card.getStyleClass().add("stats-card");
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("stats-card-title");
        contentLabel.getStyleClass().add("stats-card-content");
        if (contentStyleClass != null) {
            contentLabel.getStyleClass().add(contentStyleClass);
        }
        card.getChildren().addAll(titleLabel, contentLabel);
        return card;
    }

    // --- CAMBIO: Método renombrado ---
    private Node crearTablaLotes() {
        VBox card = new VBox(15);
        card.getStyleClass().add("card");

        Label cardTitle = new Label("Inventario de Lotes"); // --- CAMBIO
        cardTitle.getStyleClass().add("card-title");
        Label cardDescription = new Label("Lotes de materias primas disponibles en almacén"); // --- CAMBIO
        cardDescription.getStyleClass().add("header-description");
        VBox cardHeader = new VBox(5, cardTitle, cardDescription);

        configurarTabla();

        card.getChildren().addAll(cardHeader, tablaLotes);
        VBox.setVgrow(tablaLotes, Priority.ALWAYS);
        return card;
    }

    // --- CAMBIO: Tabla reconfigurada para Lotes ---
    private void configurarTabla() {
        tablaLotes.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<LoteAlmacen, Integer> loteCol = new TableColumn<>("ID Lote");
        loteCol.setCellValueFactory(new PropertyValueFactory<>("idLote"));

        TableColumn<LoteAlmacen, String> insumoCol = new TableColumn<>("Producto");
        insumoCol.setCellValueFactory(new PropertyValueFactory<>("productoNombre"));

        TableColumn<LoteAlmacen, Double> cantidadCol = new TableColumn<>("Cant. Actual");
        cantidadCol.setCellValueFactory(new PropertyValueFactory<>("cantidadActual"));

        TableColumn<LoteAlmacen, String> unidadCol = new TableColumn<>("Unidad");
        unidadCol.setCellValueFactory(new PropertyValueFactory<>("unidad"));

        TableColumn<LoteAlmacen, Date> vencCol = new TableColumn<>("Vencimiento");
        vencCol.setCellValueFactory(new PropertyValueFactory<>("fechaVencimiento"));
        // Colorear fechas de vencimiento
        vencCol.setCellFactory(column -> new TableCell<LoteAlmacen, Date>() {
            @Override
            protected void updateItem(Date item, boolean empty) {
                super.updateItem(item, empty);
                this.getStyleClass().removeAll("cell-vencido", "cell-vence-pronto");
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.toString());
                    long diff = item.getTime() - System.currentTimeMillis();
                    long diffDays = diff / (24 * 60 * 60 * 1000);

                    if (diffDays < 0) {
                        this.getStyleClass().add("cell-vencido");
                    } else if (diffDays <= 7) {
                        this.getStyleClass().add("cell-vence-pronto");
                    }
                }
            }
        });


        TableColumn<LoteAlmacen, Date> ingresoCol = new TableColumn<>("Ingreso");
        ingresoCol.setCellValueFactory(new PropertyValueFactory<>("fechaIngreso"));

        TableColumn<LoteAlmacen, String> proveedorCol = new TableColumn<>("Proveedor");
        proveedorCol.setCellValueFactory(new PropertyValueFactory<>("proveedorNombre"));

        TableColumn<LoteAlmacen, Integer> ordenCol = new TableColumn<>("ID Orden");
        ordenCol.setCellValueFactory(new PropertyValueFactory<>("idCompra"));


        tablaLotes.getColumns().setAll(loteCol, insumoCol, cantidadCol, unidadCol, vencCol, ingresoCol, proveedorCol, ordenCol);
        tablaLotes.setPlaceholder(new Label("No hay lotes en el inventario."));
    }

    // --- CAMBIO: Lógica de carga de datos completamente nueva ---
    private void cargarDatos() {
        todosLosLotes.clear();
        lowStockCount = 0;
        double valorTotalStock = 0;
        int totalProveedores = 0;
        int totalTiposProducto = 0;

        // Mapa para sumar el stock total por producto
        Map<Integer, Double> stockAgregado = new HashMap<>();
        // Mapa para las reglas de stock mínimo
        Map<Integer, Integer> stockMinimoReglas = new HashMap<>();

        String sqlLotes = "SELECT " +
                "l.IdLote, l.IdProducto, l.CantidadActual, l.FechaVencimiento, l.FechaIngreso, l.IdCompra, " +
                "p.Tipo_de_Producto, p.Unidad_de_medida, p.PrecioUnitario, p.Stock_Minimo, " +
                "prov.Nombre_comercial " +
                "FROM lotes l " +
                "JOIN producto p ON l.IdProducto = p.IdProducto " +
                "LEFT JOIN proveedores prov ON p.IdProveedor = prov.IdProveedor " +
                "ORDER BY l.FechaVencimiento ASC";

        String sqlProveedores = "SELECT COUNT(*) FROM proveedores";
        String sqlTiposProducto = "SELECT COUNT(*) FROM producto";

        try (Connection conn = ConexionBD.getConnection();
             Statement stmt = conn.createStatement()) {

            // 1. Cargar Lotes
            ResultSet rsLotes = stmt.executeQuery(sqlLotes);
            while (rsLotes.next()) {
                LoteAlmacen lote = new LoteAlmacen(
                        rsLotes.getInt("IdLote"),
                        rsLotes.getInt("IdProducto"),
                        rsLotes.getDouble("CantidadActual"),
                        rsLotes.getDate("FechaVencimiento"),
                        rsLotes.getDate("FechaIngreso"),
                        rsLotes.getInt("IdCompra"),
                        rsLotes.getString("Tipo_de_Producto"),
                        rsLotes.getString("Unidad_de_medida"),
                        rsLotes.getString("Nombre_comercial")
                );
                todosLosLotes.add(lote);

                // 2. Calcular stats mientras se recorre
                valorTotalStock += lote.getCantidadActual() * rsLotes.getDouble("PrecioUnitario");

                // 3. Sumar al stock agregado
                int idProd = lote.getIdProducto();
                stockAgregado.put(idProd, stockAgregado.getOrDefault(idProd, 0.0) + lote.getCantidadActual());
                // Guardar la regla de stock mínimo
                if (!stockMinimoReglas.containsKey(idProd)) {
                    stockMinimoReglas.put(idProd, rsLotes.getInt("Stock_Minimo"));
                }
            }

            // 4. Calcular Stock Bajo
            for (Map.Entry<Integer, Double> entry : stockAgregado.entrySet()) {
                int idProd = entry.getKey();
                double stockTotal = entry.getValue();
                int stockMin = stockMinimoReglas.get(idProd);
                if (stockTotal <= stockMin) {
                    lowStockCount++;
                }
            }

            // 5. Cargar Conteo de Proveedores
            ResultSet rsProv = stmt.executeQuery(sqlProveedores);
            if (rsProv.next()) totalProveedores = rsProv.getInt(1);

            // 6. Cargar Conteo de Tipos de Producto
            ResultSet rsTipos = stmt.executeQuery(sqlTiposProducto);
            if (rsTipos.next()) totalTiposProducto = rsTipos.getInt(1);

            // 7. Actualizar UI
            totalInsumosLabel.setText(String.valueOf(totalTiposProducto));
            totalProveedoresLabel.setText(String.valueOf(totalProveedores));
            valorTotalLabel.setText(String.format("$%.2f", valorTotalStock));
            stockBajoLabel.setText(String.valueOf(lowStockCount));

        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error de BD", "No se pudo cargar el inventario: " + e.getMessage());
        }
    }

    // --- CAMBIO: Lógica de filtrado ---
    private void setupFiltering() {
        filteredLotes = new FilteredList<>(todosLosLotes, p -> true);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredLotes.setPredicate(lote -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();

                // Filtrar por nombre, ID de lote o proveedor
                if (lote.getProductoNombre().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (lote.getProveedorNombre() != null && lote.getProveedorNombre().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (String.valueOf(lote.getIdLote()).contains(lowerCaseFilter)) {
                    return true;
                }
                return false;
            });
        });

        tablaLotes.setItems(filteredLotes);
    }

    // --- CAMBIO: Lógica de actualización de UI ---
    private void actualizarUIConDatos() {
        // La mayoría de los labels se actualizan en cargarDatos()
        // Aquí solo actualizamos la alerta
        alertBoxContainer.getChildren().clear();
        if (lowStockCount > 0) {
            alertBoxContainer.getChildren().add(crearAlertBox(lowStockCount));
        }
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }


    // --- CAMBIO: Clase de Datos Interna -> LoteAlmacen ---
    public static class LoteAlmacen {
        private final int idLote;
        private final int idProducto;
        private final double cantidadActual;
        private final Date fechaVencimiento;
        private final Date fechaIngreso;
        private final int idCompra;
        private final String productoNombre;
        private final String unidad;
        private final String proveedorNombre;

        public LoteAlmacen(int idLote, int idProducto, double cantidadActual, Date fechaVencimiento, Date fechaIngreso, int idCompra, String productoNombre, String unidad, String proveedorNombre) {
            this.idLote = idLote;
            this.idProducto = idProducto;
            this.cantidadActual = cantidadActual;
            this.fechaVencimiento = fechaVencimiento;
            this.fechaIngreso = fechaIngreso;
            this.idCompra = idCompra;
            this.productoNombre = productoNombre;
            this.unidad = (unidad != null) ? unidad : "N/A";
            this.proveedorNombre = (proveedorNombre != null) ? proveedorNombre : "N/A";
        }

        public int getIdLote() { return idLote; }
        public int getIdProducto() { return idProducto; }
        public double getCantidadActual() { return cantidadActual; }
        public Date getFechaVencimiento() { return fechaVencimiento; }
        public Date getFechaIngreso() { return fechaIngreso; }
        public int getIdCompra() { return idCompra; }
        public String getProductoNombre() { return productoNombre; }
        public String getUnidad() { return unidad; }
        public String getProveedorNombre() { return proveedorNombre; }
    }

    // --- Clase de Test para Ejecutar ---
    public static class TestApp extends Application {
        @Override
        public void start(Stage stage) {
            Consumer<String> navigationHandler = (page) -> {
                System.out.println("Navegando a: " + page);
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Navegación");
                alert.setHeaderText(null);
                alert.setContentText("Se solicitó ir a: " + page);
                alert.showAndWait();
            };

            stage.setTitle("Almacén - JavaFX");
            stage.setScene(new Scene(new Almacen1Page(navigationHandler), 1300, 900));
            stage.show();
        }
    }

    public static void main(String[] args) {
        Application.launch(TestApp.class, args);
    }
}