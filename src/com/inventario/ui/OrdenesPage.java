package com.inventario.ui;

import com.inventario.config.ConexionBD;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node; // Importante: Node es la clase padre
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class OrdenesPage extends BorderPane {

    // --- Variables de UI ---
    private final TableView<ItemOrden> tablaItems;
    private final Label totalLabel;
    private final ComboBox<String> proveedorCombo;
    private final ComboBox<Insumo> insumoCombo;
    private final TextField cantidadField;
    private final DatePicker fechaPicker;

    // UI para la pestaña de Gestión
    private final TableView<ItemOrdenGestion> tablaGestion;
    private final ObservableList<ItemOrdenGestion> listaOrdenesPendientes = FXCollections.observableArrayList();
    private VBox historialContainer;

    // --- Listas de Datos ---
    private final ObservableList<ItemOrden> items = FXCollections.observableArrayList();
    private final ObservableList<Insumo> todosLosInsumos = FXCollections.observableArrayList();

    // --- ESTILOS ---
    private static final String CSS_STYLES = """
        .root { -fx-background-color: #FDF8F0; -fx-font-family: 'Segoe UI'; }
        .header-title { -fx-font-size: 2.2em; -fx-font-weight: bold; -fx-text-fill: #333333; -fx-padding: 0 0 -5 0; }
        .header-description { -fx-font-size: 1.1em; -fx-text-fill: #555555; -fx-padding: 0 0 10 0; }
        .tab-pane { -fx-padding: 0; }
        .tab-pane .tab-header-area { -fx-padding: 0; }
        .tab-content-area { -fx-background-color: transparent; -fx-padding: 20 0 0 0; }
        .tab-pane .tab-header-area .tab-header-background { -fx-background-color: transparent; }
        .tab-pane .tab { -fx-background-color: transparent; -fx-border-color: transparent; -fx-padding: 8 15 8 15; -fx-font-size: 1.1em; }
        .tab-pane .tab:selected { -fx-background-color: transparent; -fx-border-color: #4A90E2; -fx-border-width: 0 0 3 0; -fx-text-fill: #4A90E2; -fx-font-weight: bold; }
        .card { -fx-background-color: white; -fx-border-color: #E0E0E0; -fx-border-width: 1; -fx-border-radius: 8; -fx-padding: 20px; }
        .card-title { -fx-font-size: 1.4em; -fx-font-weight: bold; -fx-text-fill: #333333; }
        .card-description { -fx-font-size: 1em; -fx-text-fill: #555555; }
        .label { -fx-font-size: 1.05em; -fx-font-weight: 500; -fx-text-fill: #333333; }
        .combo-box, .text-field, .date-picker { -fx-font-size: 1.05em; -fx-pref-height: 38px; -fx-border-color: #CCCCCC; -fx-border-radius: 5; }
        .button-primary { -fx-background-color: #4A90E2; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 1.1em; -fx-pref-height: 40px; -fx-cursor: hand; -fx-background-radius: 5; }
        .button-add { -fx-background-color: #22C55E; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 1.1em; -fx-pref-height: 38px; -fx-cursor: hand; -fx-background-radius: 5; }
        .button-danger { -fx-background-color: transparent; -fx-text-fill: #EF4444; -fx-font-size: 1.4em; -fx-cursor: hand; -fx-padding: 5; }
        .button-accept { -fx-background-color: #22C55E; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 5; }
        .button-edit { -fx-background-color: #F97316; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 5; }
        .button-reject { -fx-background-color: #EF4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 5; }
        .table-view .column-header { -fx-background-color: #F9FAFB; -fx-font-weight: bold; -fx-font-size: 1.05em; }
        .badge { -fx-padding: 4 10; -fx-background-radius: 12; -fx-font-size: 0.9em; -fx-font-weight: bold; -fx-text-fill: white; }
    """;

    public OrdenesPage() {
        this.getStylesheets().add("data:text/css," + CSS_STYLES.replace("\n", ""));
        this.getStyleClass().add("root");
        setPadding(new Insets(30, 40, 30, 40));

        proveedorCombo = new ComboBox<>();
        insumoCombo = new ComboBox<>();
        cantidadField = new TextField();
        tablaItems = new TableView<>();
        totalLabel = new Label("$0.00");
        fechaPicker = new DatePicker(LocalDate.now());
        tablaGestion = new TableView<>();

        cargarProveedores(proveedorCombo);
        cargarTodosLosInsumos();

        VBox mainContent = new VBox();
        Node header = crearHeader();
        Node tabPane = crearTabPane();
        mainContent.getChildren().addAll(header, tabPane);
        setCenter(mainContent);
    }

    private Node crearHeader() {
        VBox headerBox = new VBox(5);
        headerBox.setPadding(new Insets(0, 0, 20, 0));
        Label header = new Label("Orden de Compra");
        header.getStyleClass().add("header-title");
        Label description = new Label("Crear y gestionar órdenes de compra de insumos");
        description.getStyleClass().add("header-description");
        headerBox.getChildren().addAll(header, description);
        return headerBox;
    }

    private Node crearTabPane() {
        TabPane tabPane = new TabPane();
        tabPane.getStyleClass().add("tab-pane");

        Tab tabNueva = new Tab("Nueva Orden", crearTabNuevaOrden());
        tabNueva.setClosable(false);

        Tab tabGestion = new Tab("Gestionar Órdenes", crearTabGestion());
        tabGestion.setClosable(false);
        tabGestion.setOnSelectionChanged(e -> {
            if (tabGestion.isSelected()) cargarOrdenesPendientes();
        });

        Tab tabHistorial = new Tab("Historial", crearTabHistorial());
        tabHistorial.setClosable(false);
        tabHistorial.setOnSelectionChanged(e -> {
            if (tabHistorial.isSelected()) cargarHistorial(historialContainer);
        });

        tabPane.getTabs().addAll(tabNueva, tabGestion, tabHistorial);
        return tabPane;
    }

    // --- PESTAÑA NUEVA ORDEN (Simplificada: Sin fechas aquí) ---
    private Node crearTabNuevaOrden() {
        VBox layout = new VBox(25);
        layout.getStyleClass().add("tab-content-area");
        VBox card = new VBox(25);
        card.getStyleClass().add("card");

        HBox cardHeader = new HBox(10);
        cardHeader.setAlignment(Pos.CENTER_LEFT);
        Text icon = new Text("🛒");
        icon.setFont(Font.font(20));
        Label cardTitle = new Label("Nueva Orden de Compra");
        cardTitle.getStyleClass().add("card-title");
        cardHeader.getChildren().addAll(icon, cardTitle);
        Label cardDescription = new Label("Complete los detalles de la orden (las fechas se asignan al recibir)");
        cardDescription.getStyleClass().add("card-description");

        GridPane gridSup = new GridPane();
        gridSup.setHgap(20);
        gridSup.setVgap(10);
        proveedorCombo.setPromptText("Seleccionar proveedor");
        proveedorCombo.setMaxWidth(Double.MAX_VALUE);
        gridSup.add(crearCampo("Proveedor", proveedorCombo), 0, 0);
        fechaPicker.setMaxWidth(Double.MAX_VALUE);
        gridSup.add(crearCampo("Fecha de Emisión", fechaPicker), 1, 0);
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        gridSup.getColumnConstraints().addAll(col1, col2);

        VBox addItemsBox = new VBox(15);
        addItemsBox.setPadding(new Insets(15));
        addItemsBox.setStyle("-fx-border-color: #E0E0E0; -fx-border-radius: 8; -fx-background-radius: 8;");
        Label addItemsTitle = new Label("Agregar Items");
        addItemsTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));

        GridPane addItemsGrid = new GridPane();
        addItemsGrid.setHgap(15);
        addItemsGrid.setVgap(10);
        ColumnConstraints colInsumo = new ColumnConstraints();
        colInsumo.setPercentWidth(60);
        ColumnConstraints colCant = new ColumnConstraints();
        colCant.setPercentWidth(20);
        ColumnConstraints colBtn = new ColumnConstraints();
        colBtn.setPercentWidth(20);
        addItemsGrid.getColumnConstraints().addAll(colInsumo, colCant, colBtn);

        insumoCombo.setPromptText("Seleccionar producto");
        insumoCombo.setMaxWidth(Double.MAX_VALUE);
        insumoCombo.setDisable(true);
        insumoCombo.setCellFactory(lv -> new InsumoListCell());
        insumoCombo.setButtonCell(new InsumoListCell());
        addItemsGrid.add(crearCampo("Producto", insumoCombo), 0, 0);

        cantidadField.setPromptText("0");
        cantidadField.setMaxWidth(Double.MAX_VALUE);
        addItemsGrid.add(crearCampo("Cantidad", cantidadField), 1, 0);

        Button addButton = new Button("➕ Agregar");
        addButton.getStyleClass().add("button-add");
        addButton.setMaxWidth(Double.MAX_VALUE);
        addButton.setOnAction(e -> agregarItem());
        VBox btnBox = new VBox(addButton);
        btnBox.setAlignment(Pos.BOTTOM_CENTER);
        btnBox.setPadding(new Insets(19, 0, 0, 0));
        addItemsGrid.add(btnBox, 2, 0);

        addItemsBox.getChildren().addAll(addItemsTitle, addItemsGrid);

        proveedorCombo.setOnAction(e -> {
            String proveedorNombre = proveedorCombo.getValue();
            if (proveedorNombre != null && !proveedorNombre.isEmpty()) {
                int idProveedor = obtenerIdProveedor(proveedorNombre);
                filtrarInsumosPorProveedor(insumoCombo, idProveedor);
                insumoCombo.setDisable(false);
            } else {
                insumoCombo.getItems().clear();
                insumoCombo.setDisable(true);
            }
        });

        configurarTablaItems(tablaItems, items, totalLabel);
        VBox tablaYTotal = new VBox();
        HBox totalBox = new HBox(10);
        totalBox.setAlignment(Pos.CENTER_RIGHT);
        totalBox.setPadding(new Insets(15));
        Label totalText = new Label("Total:");
        totalText.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        totalLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        totalBox.getChildren().addAll(totalText, totalLabel);
        tablaYTotal.getChildren().addAll(tablaItems, totalBox);
        VBox.setVgrow(tablaItems, Priority.ALWAYS);

        Button crearOrdenBtn = new Button("🛒 Guardar Orden (Pendiente)");
        crearOrdenBtn.getStyleClass().add("button-primary");
        crearOrdenBtn.setMaxWidth(Double.MAX_VALUE);
        crearOrdenBtn.setOnAction(e -> guardarOrden());

        card.getChildren().addAll(new VBox(5, cardHeader.getChildren().toArray(new Node[0])), cardDescription, gridSup, addItemsBox, tablaYTotal, crearOrdenBtn);
        layout.getChildren().add(card);
        return layout;
    }

    private void configurarTablaItems(TableView<ItemOrden> tabla, ObservableList<ItemOrden> lista, Label totalLabel) {
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<ItemOrden, String> insumoCol = new TableColumn<>("Producto");
        insumoCol.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        TableColumn<ItemOrden, Double> cantidadCol = new TableColumn<>("Cantidad");
        cantidadCol.setCellValueFactory(new PropertyValueFactory<>("cantidad"));

        TableColumn<ItemOrden, Double> precioCol = new TableColumn<>("Precio Unit.");
        precioCol.setCellValueFactory(new PropertyValueFactory<>("precioUnitario"));
        precioCol.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                setText(empty ? null : String.format("$%.2f", price));
            }
        });

        TableColumn<ItemOrden, Double> totalCol = new TableColumn<>("Subtotal");
        totalCol.setCellValueFactory(new PropertyValueFactory<>("total"));
        totalCol.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(Double total, boolean empty) {
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
                    lista.remove(item);
                    actualizarTotal(lista, totalLabel);
                });
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteButton);
                setAlignment(Pos.CENTER);
            }
        });

        tabla.getColumns().setAll(insumoCol, cantidadCol, precioCol, totalCol, accionCol);
        tabla.setItems(lista);
        tabla.setPlaceholder(new Label("Agregue productos a la orden"));
    }


    // --- PESTAÑA "GESTIONAR ÓRDENES" ---

    private Node crearTabGestion() {
        VBox layout = new VBox(20);
        layout.getStyleClass().add("tab-content-area");

        VBox card = new VBox(15);
        card.getStyleClass().add("card");
        Label cardTitle = new Label("Órdenes de Compra Pendientes");
        cardTitle.getStyleClass().add("card-title");
        Label cardDescription = new Label("Acepte (ingresando fechas) o rechace las órdenes.");
        cardDescription.getStyleClass().add("card-description");

        configurarTablaGestion();

        card.getChildren().addAll(cardTitle, cardDescription, tablaGestion);
        VBox.setVgrow(tablaGestion, Priority.ALWAYS);
        layout.getChildren().add(card);

        cargarOrdenesPendientes();
        return layout;
    }

    private void configurarTablaGestion() {
        tablaGestion.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<ItemOrdenGestion, Integer> idCol = new TableColumn<>("ID Orden");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<ItemOrdenGestion, String> provCol = new TableColumn<>("Proveedor");
        provCol.setCellValueFactory(new PropertyValueFactory<>("proveedorNombre"));

        TableColumn<ItemOrdenGestion, Date> fechaCol = new TableColumn<>("Fecha");
        fechaCol.setCellValueFactory(new PropertyValueFactory<>("fecha"));

        TableColumn<ItemOrdenGestion, Double> totalCol = new TableColumn<>("Total");
        totalCol.setCellValueFactory(new PropertyValueFactory<>("total"));
        totalCol.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(Double total, boolean empty) {
                super.updateItem(total, empty);
                setText(empty ? null : String.format("$%.2f", total));
            }
        });

        TableColumn<ItemOrdenGestion, String> estadoCol = new TableColumn<>("Estado");
        estadoCol.setCellValueFactory(new PropertyValueFactory<>("estado"));

        TableColumn<ItemOrdenGestion, Void> accionCol = new TableColumn<>("Acciones");
        accionCol.setCellFactory(param -> new TableCell<>() {
            private final Button btnAccept = new Button("Aceptar");
            private final Button btnEdit = new Button("Editar");
            private final Button btnReject = new Button("Rechazar");
            private final HBox pane = new HBox(5, btnAccept, btnEdit, btnReject);

            {
                btnAccept.getStyleClass().add("button-accept");
                btnEdit.getStyleClass().add("button-edit");
                btnReject.getStyleClass().add("button-reject");
                pane.setAlignment(Pos.CENTER);

                btnAccept.setOnAction(event -> accionAceptarOrden(getTableView().getItems().get(getIndex())));
                btnEdit.setOnAction(event -> accionEditarOrden(getTableView().getItems().get(getIndex())));
                btnReject.setOnAction(event -> accionRechazarOrden(getTableView().getItems().get(getIndex())));
            }

            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });

        tablaGestion.getColumns().setAll(idCol, provCol, fechaCol, totalCol, estadoCol, accionCol);
        tablaGestion.setItems(listaOrdenesPendientes);
        tablaGestion.setPlaceholder(new Label("No hay órdenes pendientes."));
    }

    private void cargarOrdenesPendientes() {
        listaOrdenesPendientes.clear();
        String sql = "SELECT o.IdCompra, p.Nombre_comercial, o.Fecha_de_Compra, o.Precio_total, o.Estado " +
                "FROM orden_compra o " +
                "JOIN proveedores p ON o.IdProveedor = p.IdProveedor " +
                "WHERE o.Estado = 'Pendiente' ORDER BY o.Fecha_de_Compra ASC";

        try (Connection conn = ConexionBD.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while(rs.next()) {
                String fechaStr = rs.getString("Fecha_de_Compra");
                Date fecha = null;
                try { if(fechaStr!=null) fecha=Date.valueOf(fechaStr); } catch(Exception e){}

                listaOrdenesPendientes.add(new ItemOrdenGestion(
                        rs.getInt("IdCompra"),
                        rs.getString("Nombre_comercial"),
                        fecha,
                        rs.getDouble("Precio_total"),
                        rs.getString("Estado")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error de BD", "No se pudo cargar las órdenes pendientes: " + e.getMessage());
        }
    }

    // --- LÓGICA DE NEGOCIO: ACEPTAR (Con Diálogo de Fechas) ---

    private void accionAceptarOrden(ItemOrdenGestion orden) {
        ObservableList<ItemOrden> itemsOrden = FXCollections.observableArrayList();
        cargarItemsDeOrden(orden.getId(), itemsOrden);

        if (itemsOrden.isEmpty()) {
            mostrarAlerta("Error", "Esta orden no tiene detalles.");
            return;
        }

        // Diálogo para pedir fechas
        Dialog<HashMap<Integer, LocalDate>> dialog = new Dialog<>();
        dialog.setTitle("Confirmar Recepción - Orden #" + orden.getId());
        dialog.setHeaderText("Ingrese la fecha de vencimiento para cada producto:");
        dialog.getDialogPane().setMinWidth(600);
        ButtonType okButtonType = new ButtonType("Confirmar Recepción", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(15); grid.setVgap(10); grid.setPadding(new Insets(20));
        grid.add(new Label("Producto"), 0, 0);
        grid.add(new Label("Cantidad"), 1, 0);
        grid.add(new Label("Fecha Vencimiento"), 2, 0);
        grid.add(new Label("No Aplica"), 3, 0);

        List<DatePicker> datePickers = new ArrayList<>();
        List<CheckBox> checkBoxes = new ArrayList<>();

        for (int i = 0; i < itemsOrden.size(); i++) {
            ItemOrden item = itemsOrden.get(i);
            int row = i + 1;
            grid.add(new Label(item.getNombre()), 0, row);
            grid.add(new Label(String.valueOf(item.getCantidad())), 1, row);

            DatePicker picker = new DatePicker(LocalDate.now().plusMonths(6));
            CheckBox check = new CheckBox();
            picker.disableProperty().bind(check.selectedProperty());

            grid.add(picker, 2, row);
            grid.add(check, 3, row);
            datePickers.add(picker);
            checkBoxes.add(check);
        }

        ScrollPane scroll = new ScrollPane(grid);
        scroll.setFitToWidth(true);
        scroll.setMaxHeight(400);
        dialog.getDialogPane().setContent(scroll);

        dialog.setResultConverter(btn -> {
            if (btn == okButtonType) {
                HashMap<Integer, LocalDate> mapaFechas = new HashMap<>();
                for (int i = 0; i < itemsOrden.size(); i++) {
                    if (!checkBoxes.get(i).isSelected() && datePickers.get(i).getValue() == null) return null;
                    LocalDate fecha = checkBoxes.get(i).isSelected() ? null : datePickers.get(i).getValue();
                    mapaFechas.put(itemsOrden.get(i).getInsumoId(), fecha);
                }
                return mapaFechas;
            }
            return null;
        });

        Optional<HashMap<Integer, LocalDate>> result = dialog.showAndWait();

        if (result.isPresent()) {
            procesarAceptacionFinal(orden, itemsOrden, result.get());
        }
    }

    private void procesarAceptacionFinal(ItemOrdenGestion orden, List<ItemOrden> items, HashMap<Integer, LocalDate> fechas) {
        Connection conn = null;
        try {
            conn = ConexionBD.getConnection();
            conn.setAutoCommit(false);

            // A. Insertar en LOTES
            String sqlLote = "INSERT INTO lotes (IdProducto, CantidadActual, FechaVencimiento, FechaIngreso, IdCompra) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sqlLote)) {
                for (ItemOrden item : items) {
                    LocalDate fVenc = fechas.get(item.getInsumoId());
                    stmt.setInt(1, item.getInsumoId());
                    stmt.setDouble(2, item.getCantidad());
                    if (fVenc != null) stmt.setString(3, fVenc.toString()); else stmt.setNull(3, Types.VARCHAR);
                    stmt.setString(4, LocalDate.now().toString());
                    stmt.setInt(5, orden.getId());
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }

            // B. Actualizar Stock Total
            String sqlUpdateStock = "UPDATE producto SET Stock = Stock + ? WHERE IdProducto = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sqlUpdateStock)) {
                for (ItemOrden item : items) {
                    stmt.setDouble(1, item.getCantidad());
                    stmt.setInt(2, item.getInsumoId());
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }

            // C. Registrar en KARDEX
            String sqlKardex = "INSERT INTO kardex (IdProducto, Fecha, Motivo, TipoMovimiento, IdEmpleado, Cantidad) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sqlKardex)) {
                for (ItemOrden item : items) {
                    stmt.setInt(1, item.getInsumoId());
                    stmt.setString(2, LocalDate.now().toString());
                    stmt.setString(3, "Recepción Orden #" + orden.getId());
                    stmt.setString(4, "Entrada");
                    stmt.setInt(5, 1);
                    stmt.setDouble(6, item.getCantidad());
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }

            // D. Cerrar Orden
            try (PreparedStatement stmt = conn.prepareStatement("UPDATE orden_compra SET Estado = 'Completada' WHERE IdCompra = ?")) {
                stmt.setInt(1, orden.getId());
                stmt.executeUpdate();
            }

            conn.commit();
            mostrarAlerta("Éxito", "Orden aceptada. Lotes creados y stock actualizado.");
            cargarOrdenesPendientes();
            cargarHistorial(historialContainer);

        } catch (Exception e) {
            if(conn!=null) try{conn.rollback();}catch(Exception ex){}
            e.printStackTrace();
            mostrarAlerta("Error", "Fallo al aceptar orden: " + e.getMessage());
        } finally {
            if(conn!=null) try{conn.setAutoCommit(true); conn.close();}catch(Exception ex){}
        }
    }

    private void accionRechazarOrden(ItemOrdenGestion orden) {
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement stmt = conn.prepareStatement("UPDATE orden_compra SET Estado = 'Rechazada' WHERE IdCompra = ?")) {
            stmt.setInt(1, orden.getId());
            stmt.executeUpdate();
            mostrarAlerta("Éxito", "Orden rechazada.");
            cargarOrdenesPendientes();
            cargarHistorial(historialContainer);
        } catch (SQLException e) {
            mostrarAlerta("Error", e.getMessage());
        }
    }

    // --- LÓGICA: EDITAR (CON VALIDACIÓN) ---
    private void accionEditarOrden(ItemOrdenGestion orden) {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Editar Orden #" + orden.getId());
        dialog.setHeaderText("Modificar orden pendiente");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().setPrefWidth(800);

        VBox layout = new VBox(20); layout.setPadding(new Insets(20));

        ComboBox<String> editProv = new ComboBox<>(); cargarProveedores(editProv); editProv.setValue(orden.getProveedorNombre());
        DatePicker editFecha = new DatePicker(); if(orden.getFecha() != null) editFecha.setValue(orden.getFecha().toLocalDate());
        HBox top = new HBox(20, crearCampo("Proveedor", editProv), crearCampo("Fecha", editFecha));

        TableView<ItemOrden> editTable = new TableView<>();
        ObservableList<ItemOrden> editItems = FXCollections.observableArrayList();
        Label editTotal = new Label();
        configurarTablaItems(editTable, editItems, editTotal);
        cargarItemsDeOrden(orden.getId(), editItems);
        actualizarTotal(editItems, editTotal);

        ComboBox<Insumo> cInsumo = new ComboBox<>();
        cInsumo.setCellFactory(lv->new InsumoListCell()); cInsumo.setButtonCell(new InsumoListCell());
        int idProv = obtenerIdProveedor(orden.getProveedorNombre());
        filtrarInsumosPorProveedor(cInsumo, idProv);
        editProv.setOnAction(e -> filtrarInsumosPorProveedor(cInsumo, obtenerIdProveedor(editProv.getValue())));

        TextField cCant = new TextField();
        Button btnAdd = new Button("Agregar");
        btnAdd.setOnAction(e -> agregarItemLogica(cInsumo, cCant, editItems, editTotal));

        HBox addBox = new HBox(10, crearCampo("Producto", cInsumo), crearCampo("Cant.", cCant), btnAdd);
        addBox.setAlignment(Pos.BOTTOM_LEFT); HBox.setHgrow(addBox.getChildren().get(0), Priority.ALWAYS);

        layout.getChildren().addAll(top, addBox, editTable, new HBox(new Label("Total: "), editTotal));
        dialog.getDialogPane().setContent(layout);

        dialog.setResultConverter(btn -> {
            if(btn==ButtonType.OK) {
                if(editItems.isEmpty()) { mostrarAlerta("Error", "Mínimo 1 item"); return false; }
                return actualizarOrdenEnBD(orden.getId(), obtenerIdProveedor(editProv.getValue()), editFecha.getValue(), editItems);
            } return false;
        });

        if(dialog.showAndWait().orElse(false)) {
            mostrarAlerta("Éxito", "Orden actualizada.");
            cargarOrdenesPendientes();
        }
    }

    private boolean actualizarOrdenEnBD(int idOrden, int idProv, LocalDate fecha, ObservableList<ItemOrden> nuevosItems) {
        try(Connection conn = ConexionBD.getConnection()) {
            conn.setAutoCommit(false);
            try(PreparedStatement ps = conn.prepareStatement("UPDATE orden_compra SET IdProveedor=?, Fecha_de_Compra=?, Precio_total=? WHERE IdCompra=?")) {
                ps.setInt(1, idProv); ps.setString(2, fecha.toString());
                ps.setDouble(3, nuevosItems.stream().mapToDouble(ItemOrden::getTotal).sum());
                ps.setInt(4, idOrden); ps.executeUpdate();
            }
            conn.createStatement().executeUpdate("DELETE FROM detalle_compra WHERE IdCompra=" + idOrden);
            try(PreparedStatement ps = conn.prepareStatement("INSERT INTO detalle_compra (IdCompra, IdProducto, Cantidad, PrecioUnitario, SubTotal) VALUES (?, ?, ?, ?, ?)")) {
                for(ItemOrden i : nuevosItems) {
                    ps.setInt(1, idOrden); ps.setInt(2, i.getInsumoId()); ps.setDouble(3, i.getCantidad());
                    ps.setDouble(4, i.getPrecioUnitario()); ps.setDouble(5, i.getTotal()); ps.addBatch();
                }
                ps.executeBatch();
            }
            conn.commit(); return true;
        } catch(Exception e) { return false; }
    }

    // --- PESTAÑA "HISTORIAL" ---

    private Node crearTabHistorial() {
        VBox layout = new VBox(20);
        layout.getStyleClass().add("tab-content-area");

        VBox card = new VBox(15);
        card.getStyleClass().add("card");
        Label cardTitle = new Label("Historial de Órdenes");
        Label cardDescription = new Label("Órdenes de compra completadas o rechazadas");
        cardTitle.getStyleClass().add("card-title");
        cardDescription.getStyleClass().add("card-description");
        card.getChildren().addAll(cardTitle, cardDescription);

        historialContainer = new VBox(10);
        historialContainer.setPadding(new Insets(10, 0, 10, 0));

        ScrollPane scrollPane = new ScrollPane(historialContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background-color: transparent;");

        card.getChildren().add(scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        layout.getChildren().add(card);
        return layout;
    }

    private void cargarHistorial(VBox container) {
        if (container == null) return;
        container.getChildren().clear();

        String sql = "SELECT o.IdCompra, p.Nombre_comercial, o.Fecha_de_Compra, o.Precio_total, o.Estado " +
                "FROM orden_compra o " +
                "JOIN proveedores p ON o.IdProveedor = p.IdProveedor " +
                "WHERE o.Estado != 'Pendiente' ORDER BY o.Fecha_de_Compra DESC";

        String sqlDetalle = "SELECT i.Tipo_de_Producto, d.Cantidad, d.PrecioUnitario " +
                "FROM detalle_compra d " +
                "JOIN producto i ON d.IdProducto = i.IdProducto " +
                "WHERE d.IdCompra = ?";

        try (Connection conn = ConexionBD.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int idOrden = rs.getInt("IdCompra");
                String fechaStr = rs.getString("Fecha_de_Compra");
                Date fecha = null;
                try { if(fechaStr!=null) fecha=Date.valueOf(fechaStr); } catch(Exception e){}

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

                OrdenHistorial orden = new OrdenHistorial(idOrden, rs.getString("Nombre_comercial"), fecha, rs.getDouble("Precio_total"), rs.getString("Estado"), itemsHistorial);
                container.getChildren().add(crearCardOrden(orden));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error de BD", "No se pudo cargar el historial: " + e.getMessage());
        }
    }

    private Node crearCardOrden(OrdenHistorial orden) {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");
        card.setStyle("-fx-border-color: #E0E0E0;");

        BorderPane header = new BorderPane();
        VBox tituloFecha = new VBox(2);
        Label titulo = new Label(String.format("Orden #%d - %s", orden.getId(), orden.getProveedor()));
        titulo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        Label fecha = new Label(orden.getFecha() != null ? orden.getFecha().toString() : "Sin Fecha");
        fecha.getStyleClass().add("card-description");
        tituloFecha.getChildren().addAll(titulo, fecha);
        header.setLeft(tituloFecha);

        Label badge = new Label(orden.getEstado().toUpperCase());
        badge.getStyleClass().add("badge");
        if ("Completada".equalsIgnoreCase(orden.getEstado())) {
            badge.setStyle("-fx-background-color: #22C55E; -fx-text-fill: white;");
        } else if ("Rechazada".equalsIgnoreCase(orden.getEstado())) {
            badge.setStyle("-fx-background-color: #EF4444; -fx-text-fill: white;");
        } else {
            badge.setStyle("-fx-background-color: #777777; -fx-text-fill: white;");
        }
        header.setRight(badge);

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
        combo.getItems().clear();
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
        String sql = "SELECT IdProducto, IdProveedor, Tipo_de_Producto, PrecioUnitario, Unidad_de_medida FROM producto";
        try (Connection conn = ConexionBD.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                todosLosInsumos.add(new Insumo(
                        rs.getInt("IdProducto"),
                        rs.getInt("IdProveedor"),
                        rs.getString("Tipo_de_Producto"),
                        rs.getDouble("PrecioUnitario"),
                        rs.getString("Unidad_de_medida")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void filtrarInsumosPorProveedor(ComboBox<Insumo> combo, int idProveedor) {
        List<Insumo> filtrados = todosLosInsumos.stream()
                .filter(insumo -> insumo.getIdProveedor() == idProveedor)
                .collect(Collectors.toList());
        combo.setItems(FXCollections.observableArrayList(filtrados));
    }

    private void cargarItemsDeOrden(int idCompra, ObservableList<ItemOrden> lista) {
        lista.clear();
        String sql = "SELECT d.IdProducto, d.Cantidad, p.IdProveedor, p.Tipo_de_Producto, p.PrecioUnitario, p.Unidad_de_medida FROM detalle_compra d JOIN producto p ON d.IdProducto = p.IdProducto WHERE d.IdCompra = ?";
        try (Connection conn = ConexionBD.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idCompra);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Insumo insumo = new Insumo(rs.getInt(1), rs.getInt(3), rs.getString(4), rs.getDouble(5), rs.getString(6));
                lista.add(new ItemOrden(insumo, rs.getDouble(2)));
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }


    // --- Métodos de Lógica de UI ---

    private VBox crearCampo(String labelText, Control input) {
        // IMPORTANTE: Cambiado a Node para aceptar HBox también si fuera necesario,
        // aunque aquí uso Control porque paso ComboBox/TextField.
        // Si pasas HBox, cambia 'Control' a 'Node'.
        VBox vbox = new VBox(5);
        Label label = new Label(labelText);
        label.getStyleClass().add("label");
        vbox.getChildren().addAll(label, input);
        return vbox;
    }

    // Sobrecarga para aceptar HBox
    private VBox crearCampo(String labelText, Node input) {
        VBox vbox = new VBox(5);
        Label label = new Label(labelText);
        label.getStyleClass().add("label");
        vbox.getChildren().addAll(label, input);
        return vbox;
    }

    private void agregarItem() {
        agregarItemLogica(insumoCombo, cantidadField, items, totalLabel);
    }

    private void agregarItemLogica(ComboBox<Insumo> combo, TextField field, ObservableList<ItemOrden> lista, Label labelTotal) {
        try {
            Insumo insumo = combo.getValue();
            double cantidad = Double.parseDouble(field.getText().trim());

            if (insumo == null || cantidad <= 0) {
                mostrarAlerta("Error", "Seleccione un producto y una cantidad válida.");
                return;
            }
            double precio = insumo.getPrecioUnitario();
            boolean existe = false;
            for (ItemOrden item : lista) {
                if (item.getInsumoId() == insumo.getId() && item.getPrecioUnitario() == precio) {
                    item.setCantidad(item.getCantidad() + cantidad);
                    existe = true;
                    break;
                }
            }
            if (!existe) {
                lista.add(new ItemOrden(insumo, cantidad));
            }

            actualizarTotal(lista, labelTotal);

            combo.getSelectionModel().clearSelection();
            field.clear();

        } catch (NumberFormatException e) {
            mostrarAlerta("Error", "Cantidad debe ser un número válido.");
        }
    }

    private void eliminarItem(ItemOrden item) {
        items.remove(item);
        actualizarTotal(items, totalLabel);
    }

    private void limpiarCamposItem() {
        insumoCombo.getSelectionModel().clearSelection();
        cantidadField.clear();
    }

    private void actualizarTotal(ObservableList<ItemOrden> lista, Label labelTotal) {
        double total = lista.stream().mapToDouble(ItemOrden::getTotal).sum();
        labelTotal.setText(String.format("$%.2f", total));
    }

    private void guardarOrden() {
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

            String sqlOrden = "INSERT INTO orden_compra (IdProveedor, IdEmpleado, Fecha_de_Compra, Precio_total, Estado) " +
                    "VALUES (?, ?, ?, ?, 'Pendiente')";
            int idOrdenGenerada;
            try (PreparedStatement stmtOrden = conn.prepareStatement(sqlOrden, Statement.RETURN_GENERATED_KEYS)) {
                stmtOrden.setInt(1, idProveedor);
                stmtOrden.setInt(2, 1); // TODO: Cambiar por ID de sesión
                stmtOrden.setString(3, fecha.toString()); // Guardar como texto para SQLite
                stmtOrden.setDouble(4, total);

                stmtOrden.executeUpdate();
                ResultSet rsKeys = stmtOrden.getGeneratedKeys();
                if (rsKeys.next()) {
                    idOrdenGenerada = rsKeys.getInt(1);
                } else {
                    throw new SQLException("No se pudo obtener el ID de la orden.");
                }
            }

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

            items.clear();
            actualizarTotal(items, totalLabel);
            proveedorCombo.getSelectionModel().clearSelection();
            insumoCombo.getItems().clear();
            insumoCombo.setDisable(true);
            fechaPicker.setValue(LocalDate.now());

            cargarOrdenesPendientes();

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
        if (titulo.startsWith("Error")) {
            alert.setAlertType(Alert.AlertType.ERROR);
        }
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    // --- Clases de Datos Internas ---

    public static class Insumo {
        private final int id;
        private final int idProveedor;
        private final String nombre;
        private final double precioUnitario;
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

        @Override
        public String toString() {
            return String.format("%s - $%.2f (%s)", nombre, precioUnitario, unidad);
        }
    }

    private static class InsumoListCell extends ListCell<Insumo> {
        @Override
        protected void updateItem(Insumo item, boolean empty) {
            super.updateItem(item, empty);
            setText((empty || item == null) ? null : String.format("%s - $%.2f (%s)", item.getNombre(), item.getPrecioUnitario(), item.getUnidad()));
        }
    }

    public static class ItemOrden {
        private final int insumoId;
        private final String nombre;
        private double cantidad;
        private final double precioUnitario;
        private double total;

        public ItemOrden(Insumo insumo, double cantidad) {
            this.insumoId = insumo.getId();
            this.nombre = insumo.getNombre();
            this.cantidad = cantidad;
            this.precioUnitario = insumo.getPrecioUnitario();
            this.total = cantidad * this.precioUnitario;
        }

        public int getInsumoId() { return insumoId; }
        public String getNombre() { return nombre; }
        public double getCantidad() { return cantidad; }
        public double getPrecioUnitario() { return precioUnitario; }
        public double getTotal() { return total; }
        public void setCantidad(double cantidad) { this.cantidad = cantidad; this.total = this.cantidad * this.precioUnitario; }
    }

    public static class OrdenHistorial {
        private final int id;
        private final String proveedor;
        private final Date fecha;
        private final double total;
        private final String estado;
        private final List<ItemHistorial> items;

        public OrdenHistorial(int id, String proveedor, Date fecha, double total, String estado, List<ItemHistorial> items) {
            this.id = id;
            this.proveedor = proveedor;
            this.fecha = fecha;
            this.total = total;
            this.estado = estado;
            this.items = items;
        }
        public int getId() { return id; }
        public String getProveedor() { return proveedor; }
        public Date getFecha() { return fecha; }
        public double getTotal() { return total; }
        public String getEstado() { return estado; }
        public List<ItemHistorial> getItems() { return items; }
    }

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

    public static class ItemOrdenGestion {
        private final int id;
        private final String proveedorNombre;
        private final Date fecha;
        private final double total;
        private final String estado;

        public ItemOrdenGestion(int id, String proveedorNombre, Date fecha, double total, String estado) {
            this.id = id;
            this.proveedorNombre = proveedorNombre;
            this.fecha = fecha;
            this.total = total;
            this.estado = estado;
        }
        public int getId() { return id; }
        public String getProveedorNombre() { return proveedorNombre; }
        public Date getFecha() { return fecha; }
        public double getTotal() { return total; }
        public String getEstado() { return estado; }
    }

    private static class DetalleCompra {
        private final int idProducto;
        private final double cantidad;
        public DetalleCompra(int idProducto, double cantidad) { this.idProducto = idProducto; this.cantidad = cantidad; }
        public int getIdProducto() { return idProducto; }
        public double getCantidad() { return cantidad; }
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