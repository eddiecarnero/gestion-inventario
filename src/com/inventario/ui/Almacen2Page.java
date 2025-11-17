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
import javafx.util.StringConverter;

import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Consumer;

public class Almacen2Page extends BorderPane {

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
    """;

    // --- Variables de UI ---
    private final VBox mainContent;
    private final HBox alertBoxContainer;
    private Label totalIntermediosLabel;
    private Label stockBajoLabel;
    private Label valorTotalLabel;
    private TableView<ProductoIntermedioAlmacen> tablaIntermedios;
    private TextField searchField;

    // --- Listas de Datos ---
    private final ObservableList<ProductoIntermedioAlmacen> todosLosIntermedios = FXCollections.observableArrayList();
    private FilteredList<ProductoIntermedioAlmacen> filteredIntermedios;
    private int lowStockCount = 0; // --- CAMBIO: Usado para el stat

    // --- Navegación ---
    private final Consumer<String> onNavigate;

    public Almacen2Page(Consumer<String> onNavigate) {
        this.onNavigate = onNavigate;
        this.getStylesheets().add("data:text/css," + CSS_STYLES.replace("\n", ""));
        this.getStyleClass().add("root");
        setPadding(new Insets(30, 40, 30, 40));

        mainContent = new VBox(20);

        // Inicializar variables de clase
        this.tablaIntermedios = new TableView<>();
        this.searchField = new TextField();
        this.totalIntermediosLabel = new Label("0");
        this.stockBajoLabel = new Label("0");
        this.valorTotalLabel = new Label("$0.00");

        alertBoxContainer = new HBox();

        // 1. Header
        Node header = crearHeader();
        // 2. Barra de Acciones (Búsqueda y Botón)
        Node actionsBar = crearActionsBar();
        // 3. Rejilla de Estadísticas
        Node statsGrid = crearStatsGrid();
        // 4. Tabla de Insumos
        Node tableCard = crearTablaIntermedios();

        mainContent.getChildren().addAll(header, alertBoxContainer, actionsBar, statsGrid, tableCard);
        setCenter(mainContent);

        // Cargar datos y configurar filtros
        cargarDatos();
        setupFiltering();
        actualizarUIConDatos();
    }

    private Node crearHeader() {
        VBox headerBox = new VBox(5);
        Label header = new Label("Almacén 2 - Intermedios");
        header.getStyleClass().add("header-title");
        Label description = new Label("Gestión de productos semi-elaborados (bases, masas, etc.)");
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

        Label alertText = new Label("¡Atención! " + count + " producto(s) requieren reabastecimiento.");
        alertText.getStyleClass().add("alert-text");

        alertBox.getChildren().addAll(icon, alertText);
        return alertBox;
    }

    private Node crearActionsBar() {
        HBox actionsBar = new HBox(15);
        actionsBar.setAlignment(Pos.CENTER_LEFT);

        StackPane searchStack = new StackPane();
        searchStack.getStyleClass().add("search-field-stack");
        searchField.setPromptText("Buscar productos intermedios...");
        searchField.getStyleClass().add("search-field");
        Text searchIcon = new Text("🔍");
        searchIcon.setFill(Color.web("#9CA3AF"));
        StackPane.setAlignment(searchIcon, Pos.CENTER_LEFT);
        StackPane.setMargin(searchIcon, new Insets(0, 0, 0, 12));
        searchStack.getChildren().addAll(searchField, searchIcon);

        Button usarRecetaBtn = new Button("🍳 Usar Receta");
        usarRecetaBtn.getStyleClass().add("button-primary");
        usarRecetaBtn.setOnAction(e -> onUsarRecetaClicked());

        actionsBar.getChildren().addAll(searchStack, usarRecetaBtn);
        HBox.setHgrow(searchStack, Priority.ALWAYS);

        return actionsBar;
    }

    private Node crearStatsGrid() {
        GridPane grid = new GridPane();
        grid.getStyleClass().add("stats-grid");
        grid.setHgap(20);
        grid.setVgap(20);

        ColumnConstraints col = new ColumnConstraints();
        col.setPercentWidth(33.33);
        grid.getColumnConstraints().addAll(col, col, col);

        grid.add(createStatCard("Total Intermedios", totalIntermediosLabel, null), 0, 0);
        grid.add(createStatCard("Valor Costo Total", valorTotalLabel, null), 1, 0);
        grid.add(createStatCard("Stock Bajo", stockBajoLabel, "stats-card-content-danger"), 2, 0);

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

    private Node crearTablaIntermedios() {
        VBox card = new VBox(15);
        card.getStyleClass().add("card");

        Label cardTitle = new Label("Inventario de Intermedios");
        cardTitle.getStyleClass().add("card-title");
        Label cardDescription = new Label("Stock de productos semi-elaborados");
        cardDescription.getStyleClass().add("card-description");
        VBox cardHeader = new VBox(5, cardTitle, cardDescription);

        configurarTabla();

        card.getChildren().addAll(cardHeader, tablaIntermedios);
        VBox.setVgrow(tablaIntermedios, Priority.ALWAYS);
        return card;
    }

    // --- CAMBIO: La tabla ahora es un RESUMEN, pero la lógica de stock bajo es correcta ---
    private void configurarTabla() {
        tablaIntermedios.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<ProductoIntermedioAlmacen, String> nombreCol = new TableColumn<>("Producto");
        nombreCol.setCellValueFactory(new PropertyValueFactory<>("nombre"));

        TableColumn<ProductoIntermedioAlmacen, Integer> cantidadCol = new TableColumn<>("Cantidad");
        cantidadCol.setCellValueFactory(new PropertyValueFactory<>("stock")); // 'stock' es la suma de lotes
        cantidadCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                this.getStyleClass().remove("cell-stock-low");
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.toString());
                    if (getIndex() >= 0 && getIndex() < getTableView().getItems().size()) {
                        ProductoIntermedioAlmacen producto = getTableView().getItems().get(getIndex());
                        if (producto.esStockBajo()) {
                            this.getStyleClass().add("cell-stock-low");
                        }
                    }
                }
            }
        });

        TableColumn<ProductoIntermedioAlmacen, String> unidadCol = new TableColumn<>("Unidad");
        unidadCol.setCellValueFactory(new PropertyValueFactory<>("unidad"));

        TableColumn<ProductoIntermedioAlmacen, Double> precioCol = new TableColumn<>("Costo Unit.");
        precioCol.setCellValueFactory(new PropertyValueFactory<>("costoUnitario"));
        precioCol.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                setText(empty ? null : String.format("$%.2f", price));
            }
        });

        TableColumn<ProductoIntermedioAlmacen, Integer> stockMinCol = new TableColumn<>("Stock Mín.");
        stockMinCol.setCellValueFactory(new PropertyValueFactory<>("stockMinimo"));

        TableColumn<ProductoIntermedioAlmacen, String> estadoCol = new TableColumn<>("Estado");
        estadoCol.setCellValueFactory(new PropertyValueFactory<>("estado"));
        estadoCol.setCellFactory(column -> new TableCell<>() {
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

        tablaIntermedios.getColumns().addAll(nombreCol, cantidadCol, unidadCol, precioCol, stockMinCol, estadoCol);
        tablaIntermedios.setItems(filteredIntermedios);
        tablaIntermedios.setPlaceholder(new Label("No se encontraron productos intermedios."));
    }

    // --- CAMBIO: Lógica de carga de datos usa 'lotes_intermedios' ---
    private void cargarDatos() {
        todosLosIntermedios.clear();
        lowStockCount = 0;
        double valorTotalStock = 0;

        // Mapa para sumar el stock total por producto
        Map<Integer, Double> stockAgregado = new HashMap<>();

        // 1. Obtener la suma de stock de todos los lotes, agrupados por producto
        String sqlLotes = "SELECT IdProductoIntermedio, SUM(CantidadActual) as total_stock " +
                "FROM lotes_intermedios " +
                "GROUP BY IdProductoIntermedio";

        try (Connection conn = ConexionBD.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rsLotes = stmt.executeQuery(sqlLotes)) {

            while(rsLotes.next()) {
                stockAgregado.put(rsLotes.getInt("IdProductoIntermedio"), rsLotes.getDouble("total_stock"));
            }

            // 2. Obtener la definición de todos los productos intermedios
            String sqlProductos = "SELECT * FROM productos_intermedios";
            ResultSet rsProds = stmt.executeQuery(sqlProductos);

            while(rsProds.next()) {
                int idProd = rsProds.getInt("IdProductoIntermedio");
                double stockActual = stockAgregado.getOrDefault(idProd, 0.0);
                int stockMin = rsProds.getInt("Stock_Minimo");
                double costo = rsProds.getDouble("CostoUnitario");

                ProductoIntermedioAlmacen p = new ProductoIntermedioAlmacen(
                        idProd,
                        rsProds.getString("Nombre"),
                        (int) stockActual, // La tabla muestra el stock agregado
                        stockMin,
                        rsProds.getString("Unidad_de_medida"),
                        costo
                );
                todosLosIntermedios.add(p);

                // 3. Calcular Stats
                valorTotalStock += stockActual * costo;
                if (p.esStockBajo()) {
                    lowStockCount++;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error de BD", "No se pudo cargar el inventario: " + e.getMessage());
        }

        // 4. Actualizar Stats Globales
        totalIntermediosLabel.setText(String.valueOf(todosLosIntermedios.size()));
        stockBajoLabel.setText(String.valueOf(lowStockCount));
        valorTotalLabel.setText(String.format("$%.2f", valorTotalStock));
    }

    private void setupFiltering() {
        filteredIntermedios = new FilteredList<>(todosLosIntermedios, p -> true);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredIntermedios.setPredicate(producto -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();
                return producto.getNombre().toLowerCase().contains(lowerCaseFilter);
            });
        });

        tablaIntermedios.setItems(filteredIntermedios);
    }

    private void actualizarUIConDatos() {
        // Recargar los datos de la BD
        cargarDatos();

        // La alerta se actualiza basada en el nuevo lowStockCount
        alertBoxContainer.getChildren().clear();
        if (lowStockCount > 0) {
            alertBoxContainer.getChildren().add(crearAlertBox(lowStockCount));
        }
    }


    // --- Lógica para el Diálogo "Usar Receta" (ACTUALIZADO) ---

    private void onUsarRecetaClicked() {
        Dialog<RecetaProduccionResult> dialog = new Dialog<>();
        dialog.setTitle("Usar Receta");
        dialog.setHeaderText("Producir un nuevo lote de producto intermedio.");

        ButtonType okButtonType = new ButtonType("Producir Lote", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        // --- Layout del Diálogo (Añadido Fecha Vencimiento) ---
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        ComboBox<RecetaSimple> recetaCombo = new ComboBox<>();
        recetaCombo.setPromptText("Seleccione una receta");
        cargarRecetasParaDialog(recetaCombo);

        TextField cantidadField = new TextField("1");
        cantidadField.setPromptText("Lotes a producir");

        DatePicker fechaVencimientoPicker = new DatePicker(LocalDate.now().plusDays(7));
        CheckBox noVenceCheck = new CheckBox("No aplica fecha de vencimiento");

        noVenceCheck.setOnAction(e -> {
            fechaVencimientoPicker.setDisable(noVenceCheck.isSelected());
        });

        grid.add(new Label("Receta:"), 0, 0);
        grid.add(recetaCombo, 1, 0);
        grid.add(new Label("Lotes a Producir:"), 0, 1);
        grid.add(cantidadField, 1, 1);
        grid.add(new Label("Fecha Vencimiento:"), 0, 2);
        grid.add(fechaVencimientoPicker, 1, 2);
        grid.add(noVenceCheck, 1, 3);

        dialog.getDialogPane().setContent(grid);

        // Convertir el resultado
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                try {
                    int cantidad = Integer.parseInt(cantidadField.getText());
                    if (recetaCombo.getValue() == null) {
                        mostrarAlerta("Error", "Debe seleccionar una receta.");
                        return null;
                    }
                    if (cantidad <= 0) {
                        mostrarAlerta("Error", "La cantidad debe ser mayor a 0.");
                        return null;
                    }

                    LocalDate fechaVencimiento = noVenceCheck.isSelected() ? null : fechaVencimientoPicker.getValue();
                    if (fechaVencimiento == null && !noVenceCheck.isSelected()) {
                        mostrarAlerta("Error", "Debe seleccionar una fecha de vencimiento o marcar 'No Aplica'.");
                        return null;
                    }

                    return new RecetaProduccionResult(recetaCombo.getValue(), cantidad, fechaVencimiento);
                } catch (NumberFormatException e) {
                    mostrarAlerta("Error", "La cantidad debe ser un número entero.");
                    return null;
                }
            }
            return null;
        });

        Optional<RecetaProduccionResult> result = dialog.showAndWait();
        result.ifPresent(datos -> {
            procesarProduccion(datos.getReceta(), datos.getCantidad(), datos.getFechaVencimiento());
        });
    }

    private void cargarRecetasParaDialog(ComboBox<RecetaSimple> combo) {
        ObservableList<RecetaSimple> recetas = FXCollections.observableArrayList();

        // --- CAMBIO: SQL ahora une con 'productos_intermedios' ---
        String sql = "SELECT r.id, r.nombre, r.cantidad_producida, r.unidad_producida, " +
                "r.IdProductoIntermedio " +
                "FROM recetas r " +
                "WHERE r.IdProductoIntermedio IS NOT NULL";

        try (Connection conn = ConexionBD.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while(rs.next()) {
                recetas.add(new RecetaSimple(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getDouble("cantidad_producida"),
                        rs.getString("unidad_producida"),
                        rs.getInt("IdProductoIntermedio")
                ));
            }
            combo.setItems(recetas);
            combo.setConverter(new RecetaSimpleConverter());

        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudieron cargar las recetas.");
        }
    }

    /**
     * Lógica de producción actualizada para manejar Lotes (FEFO)
     */
    private void procesarProduccion(RecetaSimple receta, int lotesAProducir, LocalDate fechaVencimiento) {
        Connection conn = null;
        try {
            conn = ConexionBD.getConnection();
            conn.setAutoCommit(false); // Iniciar Transacción

            // 1. Obtener los ingredientes (IDs y cantidades)
            String sqlIngredientes = "SELECT IdProducto, cantidad FROM ingredientes WHERE receta_id = ?";
            HashMap<Integer, Double> ingredientesNecesarios = new HashMap<>(); // <IdProducto, CantidadTotal>
            try (PreparedStatement stmt = conn.prepareStatement(sqlIngredientes)) {
                stmt.setInt(1, receta.getId());
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    ingredientesNecesarios.put(rs.getInt("IdProducto"), rs.getDouble("cantidad") * lotesAProducir);
                }
            }

            // 2. Comprobar stock y consumir lotes (Lógica FEFO - First Expired First Out)
            for (Map.Entry<Integer, Double> ingrediente : ingredientesNecesarios.entrySet()) {
                int idProducto = ingrediente.getKey();
                double cantidadNecesaria = ingrediente.getValue();

                // Obtener lotes de este ingrediente, ordenados por vencimiento (NULLs al final)
                String sqlGetLotes = "SELECT IdLote, CantidadActual, FechaVencimiento " +
                        "FROM lotes WHERE IdProducto = ? AND CantidadActual > 0 " +
                        "ORDER BY FechaVencimiento ASC"; // FEFO

                List<Lote> lotesDisponibles = new ArrayList<>();
                try (PreparedStatement stmt = conn.prepareStatement(sqlGetLotes)) {
                    stmt.setInt(1, idProducto);
                    ResultSet rs = stmt.executeQuery();
                    while (rs.next()) {
                        lotesDisponibles.add(new Lote(rs.getInt("IdLote"), rs.getDouble("CantidadActual")));
                    }
                }

                // Verificar si el total de stock es suficiente
                double stockTotal = lotesDisponibles.stream().mapToDouble(Lote::getCantidadActual).sum();
                if (stockTotal < cantidadNecesaria) {
                    throw new SQLException("Stock insuficiente para el producto ID: " + idProducto + ". Necesita " + cantidadNecesaria + ", solo tiene " + stockTotal + ".");
                }

                // Consumir de los lotes (FEFO)
                double cantidadRestante = cantidadNecesaria;
                for (Lote lote : lotesDisponibles) {
                    if (cantidadRestante <= 0) break;

                    double cantidadAConsumir = Math.min(lote.getCantidadActual(), cantidadRestante);
                    lote.consumir(cantidadAConsumir);
                    cantidadRestante -= cantidadAConsumir;

                    // Actualizar este lote en la BD
                    if (lote.getCantidadActual() == 0) {
                        // Borrar lote si se agotó
                        String sqlDeleteLote = "DELETE FROM lotes WHERE IdLote = ?";
                        try (PreparedStatement stmt = conn.prepareStatement(sqlDeleteLote)) {
                            stmt.setInt(1, lote.getIdLote());
                            stmt.executeUpdate();
                        }
                    } else {
                        // Actualizar cantidad si sobró
                        String sqlUpdateLote = "UPDATE lotes SET CantidadActual = ? WHERE IdLote = ?";
                        try (PreparedStatement stmt = conn.prepareStatement(sqlUpdateLote)) {
                            stmt.setDouble(1, lote.getCantidadActual());
                            stmt.setInt(2, lote.getIdLote());
                            stmt.executeUpdate();
                        }
                    }

                    // Registrar en Kardex la SALIDA
                    String sqlKardex = "INSERT INTO kardex (IdProducto, Fecha, Motivo, TipoMovimiento, IdEmpleado, Cantidad) " +
                            "VALUES (?, ?, ?, ?, ?, ?)";
                    try (PreparedStatement stmtKardex = conn.prepareStatement(sqlKardex)) {
                        stmtKardex.setInt(1, idProducto);
                        stmtKardex.setDate(2, Date.valueOf(LocalDate.now()));
                        stmtKardex.setString(3, "Producción Receta #" + receta.getId() + " (Lote: " + lote.getIdLote() + ")");
                        stmtKardex.setString(4, "Salida");
                        stmtKardex.setInt(5, 1); // TODO: Usar ID de empleado
                        stmtKardex.setDouble(6, cantidadAConsumir);
                        stmtKardex.executeUpdate();
                    }
                }
            }

            // 3. SUMAR el stock al producto intermedio (crear nuevo lote)
            double cantidadProducida = receta.getCantidadBase() * lotesAProducir;
            String sqlInsertIntermedio = "INSERT INTO lotes_intermedios (IdProductoIntermedio, CantidadActual, FechaVencimiento, FechaIngreso) " +
                    "VALUES (?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sqlInsertIntermedio)) {
                stmt.setInt(1, receta.getIdProductoIntermedio());
                stmt.setDouble(2, cantidadProducida);
                if (fechaVencimiento != null) {
                    stmt.setDate(3, Date.valueOf(fechaVencimiento));
                } else {
                    stmt.setNull(3, Types.DATE);
                }
                stmt.setDate(4, Date.valueOf(LocalDate.now()));
                stmt.executeUpdate();
            }

            // (Aquí podrías añadir un registro en KARDEX para el producto intermedio, si lo deseas)

            // 4. Confirmar transacción
            conn.commit();
            mostrarAlerta("Éxito", "Producción registrada. Stock de ingredientes (Almacén 1) reducido y stock de intermedios (Almacén 2) aumentado.");

        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            mostrarAlerta("Error de Producción", e.getMessage());
        } finally {
            if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); }

            // 5. Recargar la vista
            actualizarUIConDatos(); // Llama al método que recarga y actualiza
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

    // Para el diálogo de "Usar Receta"
    private static class RecetaSimple {
        private final int id;
        private final String nombre;
        private final double cantidadBase;
        private final String unidad;
        private final int idProductoIntermedio; // --- NUEVO ---

        public RecetaSimple(int id, String nombre, double cantidadBase, String unidad, int idProdIntermedio) {
            this.id = id; this.nombre = nombre; this.cantidadBase = cantidadBase; this.unidad = unidad;
            this.idProductoIntermedio = idProdIntermedio;
        }
        public int getId() { return id; }
        public String getNombre() { return nombre; }
        public double getCantidadBase() { return cantidadBase; }
        public String getUnidad() { return unidad; }
        public int getIdProductoIntermedio() { return idProductoIntermedio; }

        @Override
        public String toString() { return nombre; }
    }

    // Convertidor para ComboBox de RecetaSimple
    private static class RecetaSimpleConverter extends StringConverter<RecetaSimple> {
        @Override
        public String toString(RecetaSimple receta) {
            return (receta == null) ? null : receta.getNombre();
        }
        @Override
        public RecetaSimple fromString(String string) { return null; }
    }

    // Para pasar el resultado del diálogo
    private static class RecetaProduccionResult {
        private final RecetaSimple receta;
        private final int cantidad;
        private final LocalDate fechaVencimiento; // --- NUEVO ---

        public RecetaProduccionResult(RecetaSimple receta, int cantidad, LocalDate fecha) {
            this.receta = receta; this.cantidad = cantidad; this.fechaVencimiento = fecha;
        }
        public RecetaSimple getReceta() { return receta; }
        public int getCantidad() { return cantidad; }
        public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    }

    // Para la lógica FEFO
    private static class Lote {
        private final int idLote;
        private double cantidadActual;
        public Lote(int id, double cantidad) { this.idLote = id; this.cantidadActual = cantidad; }
        public int getIdLote() { return idLote; }
        public double getCantidadActual() { return cantidadActual; }
        public void consumir(double cantidad) { this.cantidadActual -= cantidad; }
    }

    // Para la tabla de esta página (Vista Agregada)
    public static class ProductoIntermedioAlmacen {
        private final int id;
        private final String nombre;
        private final int stock;
        private final int stockMinimo;
        private final String unidad;
        private final double costoUnitario;

        public ProductoIntermedioAlmacen(int id, String nombre, int stock, int stockMinimo, String unidad, double costo) {
            this.id = id;
            this.nombre = nombre;
            this.stock = stock;
            this.stockMinimo = stockMinimo;
            this.unidad = (unidad != null) ? unidad : "Unidad";
            this.costoUnitario = costo;
        }

        public int getId() { return id; }
        public String getNombre() { return nombre; }
        public int getStock() { return stock; }
        public int getStockMinimo() { return stockMinimo; }
        public String getUnidad() { return unidad; }
        public double getCostoUnitario() { return costoUnitario; }

        public boolean esStockBajo() { return stock <= stockMinimo; }

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

            stage.setTitle("Almacén 2 - Intermedios");
            stage.setScene(new Scene(new Almacen2Page(navigationHandler), 1300, 900));
            stage.show();
        }
    }

    public static void main(String[] args) {
        Application.launch(TestApp.class, args);
    }
}