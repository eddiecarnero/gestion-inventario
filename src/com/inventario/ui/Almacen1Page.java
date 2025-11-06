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
import java.util.List;
import java.util.function.Consumer;

public class Almacen1Page extends BorderPane {

    // --- Estilos ---
    // (CSS_STYLES sin cambios... puedes mantener el que ya tenías)
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
    """;

    // --- Variables de UI ---
    private final VBox mainContent;
    private final HBox alertBoxContainer;
    private Label totalInsumosLabel;
    private Label stockBajoLabel;
    private Label valorTotalLabel; // <-- NUEVO
    private Label totalProveedoresLabel; // <-- NUEVO
    private TableView<InsumoAlmacen> tablaInsumos;
    private TextField searchField;

    // --- Listas de Datos ---
    private final ObservableList<InsumoAlmacen> todosLosInsumos = FXCollections.observableArrayList();
    private FilteredList<InsumoAlmacen> filteredInsumos;
    private List<InsumoAlmacen> lowStockInsumos;

    // --- Navegación ---
    private final Consumer<String> onNavigate;

    public Almacen1Page(Consumer<String> onNavigate) {
        this.onNavigate = onNavigate;
        this.getStylesheets().add("data:text/css," + CSS_STYLES.replace("\n", ""));
        this.getStyleClass().add("root");
        setPadding(new Insets(30, 40, 30, 40));

        mainContent = new VBox(20);

        // Inicializar variables de clase
        this.tablaInsumos = new TableView<>();
        this.searchField = new TextField();
        this.totalInsumosLabel = new Label("0");
        this.stockBajoLabel = new Label("0");
        this.valorTotalLabel = new Label("$0.00"); // <-- NUEVO
        this.totalProveedoresLabel = new Label("0"); // <-- NUEVO

        alertBoxContainer = new HBox();

        // 1. Header
        Node header = crearHeader();
        // 2. Barra de Acciones (Búsqueda y Botón)
        Node actionsBar = crearActionsBar();
        // 3. Rejilla de Estadísticas
        Node statsGrid = crearStatsGrid();
        // 4. Tabla de Insumos
        Node tableCard = crearTablaInsumos();

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
        Label description = new Label("Gestión de materias primas y stock de insumos");
        description.getStyleClass().add("header-description");
        headerBox.getChildren().addAll(header, description);
        return headerBox;
    }

    private HBox crearAlertBox(int count) {
        HBox alertBox = new HBox(15);
        alertBox.getStyleClass().add("alert-box");
        alertBox.setAlignment(Pos.CENTER_LEFT);

        Text icon = new Text("⚠️"); // Emoji para AlertCircle
        icon.setFont(Font.font(20));
        icon.setFill(Color.web("#DC2626"));

        Label alertText = new Label("¡Atención! " + count + " insumo(s) requieren reabastecimiento.");
        alertText.getStyleClass().add("alert-text");

        alertBox.getChildren().addAll(icon, alertText);
        return alertBox;
    }

    private Node crearActionsBar() {
        HBox actionsBar = new HBox(15);
        actionsBar.setAlignment(Pos.CENTER_LEFT);

        // Barra de Búsqueda
        StackPane searchStack = new StackPane();
        searchStack.getStyleClass().add("search-field-stack");
        searchField.setPromptText("Buscar insumos...");
        searchField.getStyleClass().add("search-field");
        Text searchIcon = new Text("🔍");
        searchIcon.setFill(Color.web("#9CA3AF"));
        StackPane.setAlignment(searchIcon, Pos.CENTER_LEFT);
        StackPane.setMargin(searchIcon, new Insets(0, 0, 0, 12));
        searchStack.getChildren().addAll(searchField, searchIcon);

        // Botón
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

        // --- AHORA 4 COLUMNAS ---
        ColumnConstraints col = new ColumnConstraints();
        col.setPercentWidth(25);
        grid.getColumnConstraints().addAll(col, col, col, col);

        // --- Stat 1: Total Insumos ---
        grid.add(createStatCard("Total Insumos", totalInsumosLabel, null), 0, 0);

        // --- Stat 2: Valor Total ---
        grid.add(createStatCard("Valor Total", valorTotalLabel, null), 1, 0); // <-- NUEVO

        // --- Stat 3: Stock Bajo ---
        grid.add(createStatCard("Stock Bajo", stockBajoLabel, "stats-card-content-danger"), 2, 0);

        // --- Stat 4: Proveedores ---
        grid.add(createStatCard("Proveedores", totalProveedoresLabel, null), 3, 0); // <-- NUEVO

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

    private Node crearTablaInsumos() {
        VBox card = new VBox(15);
        card.getStyleClass().add("card");

        // Header de la Card
        Label cardTitle = new Label("Inventario de Insumos");
        cardTitle.getStyleClass().add("card-title");
        Label cardDescription = new Label("Lista completa de materias primas disponibles");
        cardDescription.getStyleClass().add("card-description");
        VBox cardHeader = new VBox(5, cardTitle, cardDescription);

        // Configurar Tabla
        configurarTabla();

        card.getChildren().addAll(cardHeader, tablaInsumos);
        VBox.setVgrow(tablaInsumos, Priority.ALWAYS);
        return card;
    }

    private void configurarTabla() {
        tablaInsumos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<InsumoAlmacen, String> insumoCol = new TableColumn<>("Insumo");
        insumoCol.setCellValueFactory(new PropertyValueFactory<>("nombre"));

        TableColumn<InsumoAlmacen, Integer> cantidadCol = new TableColumn<>("Cantidad");
        cantidadCol.setCellValueFactory(new PropertyValueFactory<>("stock"));
        cantidadCol.setCellFactory(column -> new TableCell<InsumoAlmacen, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                this.getStyleClass().remove("cell-stock-low");
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.toString());
                    if (getIndex() >= 0 && getIndex() < getTableView().getItems().size()) {
                        InsumoAlmacen insumo = getTableView().getItems().get(getIndex());
                        if (insumo.esStockBajo()) {
                            this.getStyleClass().add("cell-stock-low");
                        }
                    }
                }
            }
        });

        TableColumn<InsumoAlmacen, String> unidadCol = new TableColumn<>("Unidad");
        unidadCol.setCellValueFactory(new PropertyValueFactory<>("unidad"));

        // --- NUEVA COLUMNA ---
        TableColumn<InsumoAlmacen, Double> precioCol = new TableColumn<>("Precio Unit.");
        precioCol.setCellValueFactory(new PropertyValueFactory<>("precioUnitario"));
        precioCol.setCellFactory(tc -> new TableCell<InsumoAlmacen, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                setText(empty ? null : String.format("$%.2f", price));
            }
        });

        TableColumn<InsumoAlmacen, Integer> stockMinCol = new TableColumn<>("Stock Mín.");
        stockMinCol.setCellValueFactory(new PropertyValueFactory<>("stockMinimo"));

        // --- NUEVA COLUMNA ---
        TableColumn<InsumoAlmacen, String> proveedorCol = new TableColumn<>("Proveedor");
        proveedorCol.setCellValueFactory(new PropertyValueFactory<>("proveedorNombre"));

        TableColumn<InsumoAlmacen, String> estadoCol = new TableColumn<>("Estado");
        estadoCol.setCellValueFactory(new PropertyValueFactory<>("estado"));
        estadoCol.setCellFactory(column -> new TableCell<InsumoAlmacen, String>() {
            private final Label badge = new Label();
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    badge.setText(item);
                    badge.getStyleClass().clear();
                    badge.getStyleClass().add("badge");
                    switch (item) {
                        case "Bajo Stock": badge.getStyleClass().add("badge-stock-low"); break;
                        case "Medio": badge.getStyleClass().add("badge-stock-medium"); break;
                        case "Normal": badge.getStyleClass().add("badge-stock-normal"); break;
                    }
                    setGraphic(badge);
                    setAlignment(Pos.CENTER_LEFT);
                }
            }
        });

        // --- COLUMNAS ACTUALIZADAS ---
        tablaInsumos.getColumns().addAll(insumoCol, cantidadCol, unidadCol, precioCol, stockMinCol, proveedorCol, estadoCol);
        tablaInsumos.setPlaceholder(new Label("No se encontraron insumos."));
    }

    private void cargarDatos() {
        todosLosInsumos.clear();
        // --- SQL ACTUALIZADO CON JOIN Y NUEVAS COLUMNAS ---
        String sql = "SELECT p.IdProducto, p.Tipo_de_Producto, p.Stock, p.Stock_Minimo, " +
                "p.Unidad_de_medida, p.PrecioUnitario, prov.Nombre_comercial " +
                "FROM producto p " +
                "LEFT JOIN proveedores prov ON p.IdProveedor = prov.IdProveedor";

        try (Connection conn = ConexionBD.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                todosLosInsumos.add(new InsumoAlmacen(
                        rs.getInt("IdProducto"),
                        rs.getString("Tipo_de_Producto"),
                        rs.getInt("Stock"),
                        rs.getInt("Stock_Minimo"),
                        rs.getString("Unidad_de_medida"),
                        rs.getDouble("PrecioUnitario"), // <-- NUEVO
                        rs.getString("Nombre_comercial") // <-- NUEVO
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error de BD", "No se pudo cargar el inventario: " + e.getMessage());
        }
    }

    private void setupFiltering() {
        filteredInsumos = new FilteredList<>(todosLosInsumos, p -> true);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredInsumos.setPredicate(insumo -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();

                // Filtrar por nombre o proveedor
                if (insumo.getNombre().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (insumo.getProveedorNombre() != null && insumo.getProveedorNombre().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                return false;
            });
        });

        tablaInsumos.setItems(filteredInsumos);
    }

    private void actualizarUIConDatos() {
        // Calcular stock bajo
        lowStockInsumos = todosLosInsumos.stream()
                .filter(InsumoAlmacen::esStockBajo)
                .toList();

        // --- CÁLCULOS NUEVOS ---
        double valorTotal = todosLosInsumos.stream()
                .mapToDouble(insumo -> insumo.getStock() * insumo.getPrecioUnitario())
                .sum();

        long numProveedores = todosLosInsumos.stream()
                .map(InsumoAlmacen::getProveedorNombre)
                .filter(nombre -> nombre != null && !nombre.isEmpty())
                .distinct()
                .count();

        // Actualizar Stats
        totalInsumosLabel.setText(String.valueOf(todosLosInsumos.size()));
        stockBajoLabel.setText(String.valueOf(lowStockInsumos.size()));
        valorTotalLabel.setText(String.format("$%.2f", valorTotal)); // <-- NUEVO
        totalProveedoresLabel.setText(String.valueOf(numProveedores)); // <-- NUEVO

        // Mostrar u ocultar alerta
        alertBoxContainer.getChildren().clear();
        if (!lowStockInsumos.isEmpty()) {
            alertBoxContainer.getChildren().add(crearAlertBox(lowStockInsumos.size()));
        }
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }


    // --- Clase de Datos Interna (ACTUALIZADA) ---
    public static class InsumoAlmacen {
        private final int id;
        private final String nombre;
        private final int stock;
        private final int stockMinimo;
        private final String unidad;
        private final double precioUnitario; // <-- NUEVO
        private final String proveedorNombre; // <-- NUEVO

        public InsumoAlmacen(int id, String nombre, int stock, int stockMinimo, String unidad, double precio, String proveedor) {
            this.id = id;
            this.nombre = nombre;
            this.stock = stock;
            this.stockMinimo = stockMinimo;
            this.unidad = (unidad != null) ? unidad : "Unidad";
            this.precioUnitario = precio;
            this.proveedorNombre = (proveedor != null) ? proveedor : "N/A";
        }

        public int getId() { return id; }
        public String getNombre() { return nombre; }
        public int getStock() { return stock; }
        public int getStockMinimo() { return stockMinimo; }
        public String getUnidad() { return unidad; }
        public double getPrecioUnitario() { return precioUnitario; }
        public String getProveedorNombre() { return proveedorNombre; }

        public boolean esStockBajo() {
            return stock <= stockMinimo;
        }

        public String getEstado() {
            if (stock <= stockMinimo) {
                return "Bajo Stock";
            } else if (stock <= stockMinimo * 1.5) {
                return "Medio";
            } else {
                return "Normal";
            }
        }
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