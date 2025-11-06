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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class OrdenesPage extends BorderPane {

    // --- Variables de UI ---
    private final TableView<ItemOrden> tablaItems;
    private final Label totalLabel;
    private final ComboBox<String> proveedorCombo;
    private final ComboBox<Insumo> insumoCombo;
    private final TextField cantidadField;
    // private final TextField precioField; // <-- ELIMINADO
    private final DatePicker fechaPicker;

    // --- Listas de Datos ---
    private final ObservableList<ItemOrden> items = FXCollections.observableArrayList();
    private final ObservableList<Insumo> todosLosInsumos = FXCollections.observableArrayList();

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

    public OrdenesPage() {
        this.getStylesheets().add("data:text/css," + CSS_STYLES.replace("\n", ""));
        this.getStyleClass().add("root");
        setPadding(new Insets(30, 40, 30, 40));

        // --- Campos de UI reutilizables ---
        proveedorCombo = new ComboBox<>();
        insumoCombo = new ComboBox<>();
        cantidadField = new TextField();
        // precioField ELIMINADO
        tablaItems = new TableView<>();
        totalLabel = new Label("$0.00");
        fechaPicker = new DatePicker(LocalDate.now());

        // --- Cargar Datos Iniciales ---
        cargarProveedores(proveedorCombo);
        cargarTodosLosInsumos(); // Carga todos los 'producto'

        // --- Estructura Principal ---
        VBox mainContent = new VBox(20);

        // 1. Header
        VBox headerBox = new VBox(5);
        Label header = new Label("Orden de Compra");
        header.getStyleClass().add("header-title");
        Label description = new Label("Crear y gestionar órdenes de compra de insumos");
        description.getStyleClass().add("header-description");
        headerBox.getChildren().addAll(header, description);

        // 2. TabPane
        TabPane tabPane = new TabPane();
        tabPane.getStyleClass().add("tab-pane");

        Tab tabNueva = new Tab("Nueva Orden", crearTabNuevaOrden());
        tabNueva.setClosable(false);
        Tab tabHistorial = new Tab("Historial", crearTabHistorial());
        tabHistorial.setClosable(false);

        tabPane.getTabs().addAll(tabNueva, tabHistorial);

        mainContent.getChildren().addAll(headerBox, tabPane);
        setCenter(mainContent);
    }

    /**
     * Crea el contenido de la pestaña "Nueva Orden".
     */
    private Node crearTabNuevaOrden() {
        VBox layout = new VBox(20);
        layout.getStyleClass().add("tab-content-area");

        // --- Card Principal ---
        VBox card = new VBox(25);
        card.getStyleClass().add("card");

        // Título de la Card
        HBox cardHeader = new HBox(10);
        cardHeader.setAlignment(Pos.CENTER_LEFT);
        Text icon = new Text("🛒");
        icon.setFont(Font.font(20));
        Label cardTitle = new Label("Nueva Orden de Compra");
        cardTitle.getStyleClass().add("card-title");
        cardHeader.getChildren().addAll(icon, cardTitle);
        Label cardDescription = new Label("Complete los detalles de la orden");
        cardDescription.getStyleClass().add("card-description");

        VBox cardHeaderBox = new VBox(5, cardHeader, cardDescription);

        // --- Proveedor y Fecha ---
        GridPane gridSup = new GridPane();
        gridSup.setHgap(20);
        gridSup.setVgap(10);
        proveedorCombo.setPromptText("Seleccionar proveedor");
        proveedorCombo.setMaxWidth(Double.MAX_VALUE);
        gridSup.add(crearCampo("Proveedor", proveedorCombo), 0, 0);
        fechaPicker.setMaxWidth(Double.MAX_VALUE);
        gridSup.add(crearCampo("Fecha", fechaPicker), 1, 0);
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        gridSup.getColumnConstraints().addAll(col1, col2);

        // --- "Agregar Items" Box ---
        VBox addItemsBox = new VBox(15);
        addItemsBox.setPadding(new Insets(15));
        addItemsBox.setStyle("-fx-border-color: #E0E0E0; -fx-border-radius: 8; -fx-background-radius: 8;");
        Label addItemsTitle = new Label("Agregar Items");
        addItemsTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));

        GridPane addItemsGrid = new GridPane();
        addItemsGrid.setHgap(15);
        addItemsGrid.setVgap(10);
        // --- LAYOUT DE 3 COLUMNAS ---
        ColumnConstraints colInsumo = new ColumnConstraints();
        colInsumo.setPercentWidth(60);
        ColumnConstraints colCant = new ColumnConstraints();
        colCant.setPercentWidth(20);
        ColumnConstraints colBtn = new ColumnConstraints();
        colBtn.setPercentWidth(20);
        addItemsGrid.getColumnConstraints().addAll(colInsumo, colCant, colBtn);

        // ComboBox de Insumos (Productos)
        insumoCombo.setPromptText("Seleccionar producto");
        insumoCombo.setMaxWidth(Double.MAX_VALUE);
        insumoCombo.setDisable(true); // Deshabilitado hasta seleccionar proveedor
        insumoCombo.setCellFactory(lv -> new InsumoListCell());
        insumoCombo.setButtonCell(new InsumoListCell());
        addItemsGrid.add(crearCampo("Producto", insumoCombo), 0, 0);

        // Campo Cantidad
        cantidadField.setPromptText("0");
        cantidadField.setMaxWidth(Double.MAX_VALUE);
        addItemsGrid.add(crearCampo("Cantidad", cantidadField), 1, 0);

        // Campo Precio ELIMINADO

        // Botón Agregar
        Button addButton = new Button("➕ Agregar");
        addButton.getStyleClass().add("button-add");
        addButton.setMaxWidth(Double.MAX_VALUE);
        addButton.setOnAction(e -> agregarItem());
        VBox btnBox = new VBox(addButton);
        btnBox.setAlignment(Pos.BOTTOM_CENTER);
        btnBox.setPadding(new Insets(19, 0, 0, 0));
        addItemsGrid.add(btnBox, 2, 0); // <-- Posición 2

        addItemsBox.getChildren().addAll(addItemsTitle, addItemsGrid);

        // --- LÓGICA DE FILTRADO (RE-INTRODUCIDA) ---
        proveedorCombo.setOnAction(e -> {
            String proveedorNombre = proveedorCombo.getValue();
            if (proveedorNombre != null && !proveedorNombre.isEmpty()) {
                int idProveedor = obtenerIdProveedor(proveedorNombre);
                filtrarInsumosPorProveedor(idProveedor);
                insumoCombo.setDisable(false);
            } else {
                insumoCombo.getItems().clear();
                insumoCombo.setDisable(true);
            }
        });

        // --- Tabla de Items ---
        configurarTabla();
        VBox tablaYTotal = new VBox();

        // --- Total ---
        HBox totalBox = new HBox(10);
        totalBox.setAlignment(Pos.CENTER_RIGHT);
        totalBox.setPadding(new Insets(15));
        totalBox.setStyle("-fx-border-color: #E0E0E0; -fx-border-width: 0 1 1 1; -fx-background-color: #F9FAFB; -fx-border-radius: 0 0 8 8; -fx-background-radius: 0 0 8 8;");
        Label totalText = new Label("Total:");
        totalText.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        totalLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        totalBox.getChildren().addAll(totalText, totalLabel);

        tablaYTotal.getChildren().addAll(tablaItems, totalBox);
        VBox.setVgrow(tablaItems, Priority.ALWAYS);

        // --- Botón Crear Orden ---
        Button crearOrdenBtn = new Button("🛒 Crear Orden de Compra");
        crearOrdenBtn.getStyleClass().add("button-primary");
        crearOrdenBtn.setMaxWidth(Double.MAX_VALUE);
        crearOrdenBtn.setOnAction(e -> guardarOrden());

        // Ensamblar Card
        card.getChildren().addAll(cardHeaderBox, gridSup, addItemsBox, tablaYTotal, crearOrdenBtn);
        layout.getChildren().add(card);
        return layout;
    }

    /**
     * Configura las columnas y propiedades de la tabla de items.
     */
    private void configurarTabla() {
        tablaItems.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<ItemOrden, String> insumoCol = new TableColumn<>("Producto");
        insumoCol.setCellValueFactory(new PropertyValueFactory<>("nombre"));

        TableColumn<ItemOrden, Double> cantidadCol = new TableColumn<>("Cantidad");
        cantidadCol.setCellValueFactory(new PropertyValueFactory<>("cantidad"));

        TableColumn<ItemOrden, Double> precioCol = new TableColumn<>("Precio Unit.");
        precioCol.setCellValueFactory(new PropertyValueFactory<>("precioUnitario"));
        precioCol.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                setText(empty ? null : String.format("$%.2f", price));
            }
        });

        TableColumn<ItemOrden, Double> totalCol = new TableColumn<>("Subtotal");
        totalCol.setCellValueFactory(new PropertyValueFactory<>("total"));
        totalCol.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Double total, boolean empty) {
                super.updateItem(total, empty);
                setText(empty ? null : String.format("$%.2f", total));
            }
        });

        TableColumn<ItemOrden, Void> accionCol = new TableColumn<>("Acción");
        accionCol.setCellFactory(param -> new TableCell<>() {
            private final Button deleteButton = new Button("🗑️");
            {
                deleteButton.getStyleClass().add("button-danger");
                deleteButton.setOnAction(event -> {
                    ItemOrden item = getTableView().getItems().get(getIndex());
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

        tablaItems.getColumns().addAll(insumoCol, cantidadCol, precioCol, totalCol, accionCol);
        tablaItems.setItems(items);
        tablaItems.setPlaceholder(new Label("Agregue productos a la orden"));
    }

    /**
     * Crea el contenido de la pestaña "Historial".
     */
    private Node crearTabHistorial() {
        VBox layout = new VBox(20);
        layout.getStyleClass().add("tab-content-area");

        // Card para el historial
        VBox card = new VBox(15);
        card.getStyleClass().add("card");
        Label cardTitle = new Label("Historial de Órdenes");
        cardTitle.getStyleClass().add("card-title");
        Label cardDescription = new Label("Todas las órdenes de compra registradas");
        cardDescription.getStyleClass().add("card-description");
        card.getChildren().addAll(cardTitle, cardDescription);

        // Contenedor para las cards de órdenes
        VBox historialContainer = new VBox(10);
        historialContainer.setPadding(new Insets(10, 0, 10, 0));

        // ScrollPane para el historial
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

    /**
     * Carga el historial de órdenes desde la BD y las añade al VBox.
     */
    private void cargarHistorial(VBox container) {
        container.getChildren().clear();
        String sql = "SELECT o.IdCompra, p.Nombre_comercial, o.Fecha_de_Compra, o.Precio_total " +
                "FROM orden_compra o " +
                "JOIN proveedores p ON o.IdProveedor = p.IdProveedor " +
                "ORDER BY o.Fecha_de_Compra DESC";

        String sqlDetalle = "SELECT i.Tipo_de_Producto, d.Cantidad, d.PrecioUnitario " +
                "FROM detalle_compra d " +
                "JOIN producto i ON d.IdProducto = i.IdProducto " +
                "WHERE d.IdCompra = ?";

        try (Connection conn = ConexionBD.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int idOrden = rs.getInt("IdCompra");
                String proveedor = rs.getString("Nombre_comercial");
                Date fecha = rs.getDate("Fecha_de_Compra");
                double total = rs.getDouble("Precio_total");

                List<ItemHistorial> itemsHistorial = new ArrayList<>();
                try (PreparedStatement stmtDet = conn.prepareStatement(sqlDetalle)) {
                    stmtDet.setInt(1, idOrden);
                    ResultSet rsDet = stmtDet.executeQuery();
                    while (rsDet.next()) {
                        itemsHistorial.add(new ItemHistorial(
                                rsDet.getString("Tipo_de_Producto"),
                                rsDet.getDouble("Cantidad"),
                                rsDet.getDouble("PrecioUnitario")
                        ));
                    }
                }

                OrdenHistorial orden = new OrdenHistorial(idOrden, proveedor, fecha, total, itemsHistorial);
                container.getChildren().add(crearCardOrden(orden));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error de BD", "No se pudo cargar el historial: " + e.getMessage());
        }
    }

    /**
     * Crea un "Card" de JavaFX para una orden individual del historial.
     */
    private Node crearCardOrden(OrdenHistorial orden) {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");
        card.setStyle("-fx-border-color: #E0E0E0;");

        // Header
        BorderPane header = new BorderPane();
        VBox tituloFecha = new VBox(2);
        Label titulo = new Label(String.format("Orden #%d - %s", orden.getId(), orden.getProveedor()));
        titulo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        Label fecha = new Label(orden.getFecha().toLocalDate().toString());
        fecha.getStyleClass().add("card-description");
        tituloFecha.getChildren().addAll(titulo, fecha);
        header.setLeft(tituloFecha);

        // Content
        VBox itemsBox = new VBox(5);
        itemsBox.setPadding(new Insets(10, 0, 10, 0));
        for (ItemHistorial item : orden.getItems()) {
            BorderPane itemPane = new BorderPane();
            Label nombreItem = new Label(item.getNombre());
            Label detalleItem = new Label(String.format("%.2f x $%.2f = $%.2f",
                    item.getCantidad(), item.getPrecioUnitario(), item.getSubtotal()));
            detalleItem.setStyle("-fx-text-fill: #555;");
            itemPane.setLeft(nombreItem);
            itemPane.setRight(detalleItem);
            itemsBox.getChildren().add(itemPane);
        }

        // Footer
        BorderPane footer = new BorderPane();
        footer.setPadding(new Insets(10, 0, 0, 0));
        footer.setStyle("-fx-border-color: #E0E0E0; -fx-border-width: 1 0 0 0;");
        Label totalText = new Label("Total:");
        totalText.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        Label totalValor = new Label(String.format("$%.2f", orden.getTotal()));
        totalValor.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        footer.setLeft(totalText);
        footer.setRight(totalValor);

        card.getChildren().addAll(header, new Separator(), itemsBox, footer);
        return card;
    }

    // --- Métodos de Carga de Datos ---

    private void cargarProveedores(ComboBox<String> combo) {
        try (Connection conn = ConexionBD.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT Nombre_comercial FROM proveedores")) {
            while (rs.next()) {
                combo.getItems().add(rs.getString("Nombre_comercial"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private int obtenerIdProveedor(String nombre) {
        String sql = "SELECT IdProveedor FROM proveedores WHERE Nombre_comercial = ?";
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nombre);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt("IdProveedor");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private void cargarTodosLosInsumos() {
        todosLosInsumos.clear();
        // --- SQL ACTUALIZADO ---
        String sql = "SELECT IdProducto, IdProveedor, Tipo_de_Producto, PrecioUnitario, Unidad_de_medida FROM producto";
        try (Connection conn = ConexionBD.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                todosLosInsumos.add(new Insumo(
                        rs.getInt("IdProducto"),
                        rs.getInt("IdProveedor"), // <-- NUEVO
                        rs.getString("Tipo_de_Producto"),
                        rs.getDouble("PrecioUnitario"), // <-- NUEVO
                        rs.getString("Unidad_de_medida")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // --- MÉTODO DE FILTRADO (RE-INTRODUCIDO) ---
    private void filtrarInsumosPorProveedor(int idProveedor) {
        List<Insumo> filtrados = todosLosInsumos.stream()
                .filter(insumo -> insumo.getIdProveedor() == idProveedor)
                .collect(Collectors.toList());
        insumoCombo.setItems(FXCollections.observableArrayList(filtrados));
    }


    // --- Métodos de Lógica de UI ---

    private VBox crearCampo(String labelText, Control input) {
        VBox vbox = new VBox(5);
        Label label = new Label(labelText);
        label.getStyleClass().add("label");
        vbox.getChildren().addAll(label, input);
        return vbox;
    }

    private void agregarItem() {
        try {
            Insumo insumo = insumoCombo.getValue();
            double cantidad = Double.parseDouble(cantidadField.getText().trim());
            // double precio = ... <-- ELIMINADO

            if (insumo == null || cantidad <= 0) {
                mostrarAlerta("Error", "Seleccione un producto y una cantidad válida.");
                return;
            }

            // --- LÓGICA DE PRECIO ACTUALIZADA ---
            // El precio se obtiene del insumo, no de un campo de texto
            double precio = insumo.getPrecioUnitario();

            // Verificar si el item ya existe para actualizar cantidad
            boolean existe = false;
            for (ItemOrden item : items) {
                if (item.getInsumoId() == insumo.getId() && item.getPrecioUnitario() == precio) {
                    item.setCantidad(item.getCantidad() + cantidad);
                    existe = true;
                    break;
                }
            }

            if (!existe) {
                // Constructor actualizado
                items.add(new ItemOrden(insumo, cantidad));
            }

            tablaItems.refresh();
            limpiarCamposItem();
            actualizarTotal();

        } catch (NumberFormatException e) {
            mostrarAlerta("Error", "Cantidad debe ser un número válido.");
        }
    }

    private void eliminarItem(ItemOrden item) {
        items.remove(item);
        actualizarTotal();
    }

    private void limpiarCamposItem() {
        insumoCombo.getSelectionModel().clearSelection();
        cantidadField.clear();
        // precioField.clear(); // <-- ELIMINADO
    }

    private void actualizarTotal() {
        double total = items.stream().mapToDouble(ItemOrden::getTotal).sum();
        totalLabel.setText(String.format("$%.2f", total));
    }

    private void guardarOrden() {
        // (Esta función ya estaba bien y coincidía con tu BD)
        // (No necesita cambios)
        String proveedorNombre = proveedorCombo.getValue();
        LocalDate fecha = fechaPicker.getValue();

        if (proveedorNombre == null || proveedorNombre.isEmpty() || fecha == null) {
            mostrarAlerta("Error", "Seleccione un proveedor y una fecha.");
            return;
        }
        if (items.isEmpty()) {
            mostrarAlerta("Error", "Agregue al menos un ítem a la orden.");
            return;
        }
        int idProveedor = obtenerIdProveedor(proveedorNombre);
        if (idProveedor == 0) {
            mostrarAlerta("Error", "Proveedor no encontrado.");
            return;
        }
        double total = items.stream().mapToDouble(ItemOrden::getTotal).sum();

        Connection conn = null;
        try {
            conn = ConexionBD.getConnection();
            conn.setAutoCommit(false);

            // 1. Insertar Orden
            String sqlOrden = "INSERT INTO orden_compra (IdProveedor, IdEmpleado, Fecha_de_Compra, Precio_total) " +
                    "VALUES (?, ?, ?, ?)";
            int idOrdenGenerada;
            try (PreparedStatement stmtOrden = conn.prepareStatement(sqlOrden, Statement.RETURN_GENERATED_KEYS)) {
                stmtOrden.setInt(1, idProveedor);
                stmtOrden.setInt(2, 1); // TODO: Cambiar por ID de sesión
                stmtOrden.setDate(3, java.sql.Date.valueOf(fecha));
                stmtOrden.setDouble(4, total);

                stmtOrden.executeUpdate();
                ResultSet rsKeys = stmtOrden.getGeneratedKeys();
                if (rsKeys.next()) {
                    idOrdenGenerada = rsKeys.getInt(1);
                } else {
                    throw new SQLException("No se pudo obtener el ID de la orden.");
                }
            }

            // 2. Insertar Detalles
            String sqlDetalle = "INSERT INTO detalle_compra (IdCompra, IdProducto, Cantidad, PrecioUnitario, SubTotal) " +
                    "VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement stmtDetalle = conn.prepareStatement(sqlDetalle)) {
                for (ItemOrden item : items) {
                    stmtDetalle.setInt(1, idOrdenGenerada);
                    stmtDetalle.setInt(2, item.getInsumoId());
                    stmtDetalle.setDouble(3, item.getCantidad());
                    stmtDetalle.setDouble(4, item.getPrecioUnitario());
                    stmtDetalle.setDouble(5, item.getTotal());
                    stmtDetalle.addBatch();
                }
                stmtDetalle.executeBatch();
            }

            conn.commit();
            mostrarAlerta("Éxito", "✅ Orden registrada correctamente.");

            // Limpiar UI
            items.clear();
            actualizarTotal();
            proveedorCombo.getSelectionModel().clearSelection();
            insumoCombo.getItems().clear();
            insumoCombo.setDisable(true);
            fechaPicker.setValue(LocalDate.now());

        } catch (SQLException ex) {
            if (conn != null) try { conn.rollback(); } catch (SQLException e) { e.printStackTrace(); }
            ex.printStackTrace();
            mostrarAlerta("Error", "Ocurrió un problema: " + ex.getMessage());
        } finally {
            if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    // --- Clases de Datos Internas ---

    /**
     * Representa un Insumo (Producto) cargado desde la base de datos.
     * === CLASE ACTUALIZADA ===
     */
    public static class Insumo {
        private final int id;
        private final int idProveedor; // <-- NUEVO
        private final String nombre;
        private final double precioUnitario; // <-- NUEVO
        private final String unidad;

        public Insumo(int id, int idProveedor, String nombre, double precio, String unidad) {
            this.id = id;
            this.idProveedor = idProveedor;
            this.nombre = nombre;
            this.precioUnitario = precio;
            this.unidad = (unidad != null) ? unidad : "Unidad";
        }

        public int getId() { return id; }
        public int getIdProveedor() { return idProveedor; }
        public String getNombre() { return nombre; }
        public double getPrecioUnitario() { return precioUnitario; }
        public String getUnidad() { return unidad; }

        // Esto es lo que se muestra en el ComboBox
        @Override
        public String toString() {
            return String.format("%s - $%.2f (%s)", nombre, precioUnitario, unidad);
        }
    }

    /**
     * Personaliza cómo se muestra un Insumo en el ComboBox.
     * === CLASE ACTUALIZADA ===
     */
    private static class InsumoListCell extends ListCell<Insumo> {
        @Override
        protected void updateItem(Insumo item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
            } else {
                setText(String.format("%s - $%.2f (%s)", item.getNombre(), item.getPrecioUnitario(), item.getUnidad()));
            }
        }
    }

    /**
     * Representa un ítem en la tabla de la nueva orden.
     * === CLASE ACTUALIZADA ===
     */
    public static class ItemOrden {
        private final int insumoId;
        private final String nombre;
        private double cantidad;
        private final double precioUnitario;
        private double total;

        // Constructor actualizado (ya no recibe precio manual)
        public ItemOrden(Insumo insumo, double cantidad) {
            this.insumoId = insumo.getId();
            this.nombre = insumo.getNombre();
            this.cantidad = cantidad;
            this.precioUnitario = insumo.getPrecioUnitario(); // <-- Obtiene precio del insumo
            this.total = cantidad * this.precioUnitario;
        }

        public int getInsumoId() { return insumoId; }
        public String getNombre() { return nombre; }
        public double getCantidad() { return cantidad; }
        public double getPrecioUnitario() { return precioUnitario; }
        public double getTotal() { return total; }

        public void setCantidad(double cantidad) {
            this.cantidad = cantidad;
            this.total = this.cantidad * this.precioUnitario;
        }
    }

    /**
     * Representa una orden para la pestaña de Historial.
     */
    public static class OrdenHistorial {
        private final int id;
        private final String proveedor;
        private final Date fecha;
        private final double total;
        private final List<ItemHistorial> items;

        public OrdenHistorial(int id, String proveedor, Date fecha, double total, List<ItemHistorial> items) {
            this.id = id;
            this.proveedor = proveedor;
            this.fecha = fecha;
            this.total = total;
            this.items = items;
        }

        public int getId() { return id; }
        public String getProveedor() { return proveedor; }
        public Date getFecha() { return fecha; }
        public double getTotal() { return total; }
        public List<ItemHistorial> getItems() { return items; }
    }

    /**
     * Representa un ítem dentro de una OrdenHistorial.
     */
    public static class ItemHistorial {
        private final String nombre;
        private final double cantidad;
        private final double precioUnitario;

        public ItemHistorial(String nombre, double cantidad, double precioUnitario) {
            this.nombre = nombre;
            this.cantidad = cantidad;
            this.precioUnitario = precioUnitario;
        }

        public String getNombre() { return nombre; }
        public double getCantidad() { return cantidad; }
        public double getPrecioUnitario() { return precioUnitario; }
        public double getSubtotal() { return cantidad * precioUnitario; }
    }

    // --- Clase de Test para Ejecutar ---
    public static class TestApp extends Application {
        @Override
        public void start(Stage stage) {
            stage.setTitle("Orden de Compra - JavaFX");
            stage.setScene(new Scene(new OrdenesPage(), 1300, 900));
            stage.show();
        }
    }

    public static void main(String[] args) {
        Application.launch(TestApp.class, args);
    }
}