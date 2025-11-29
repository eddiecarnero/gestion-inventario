package com.inventario.ui;

import com.inventario.config.ConexionBD;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
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
    private final DatePicker fechaPicker;
    private final TableView<ItemOrdenGestion> tablaGestion;
    private final ObservableList<ItemOrdenGestion> listaOrdenesPendientes = FXCollections.observableArrayList();
    private VBox historialContainer;

    // --- Listas de Datos ---
    private final ObservableList<ItemOrden> items = FXCollections.observableArrayList();
    private final ObservableList<Insumo> todosLosInsumos = FXCollections.observableArrayList();

    // --- ESTILOS ---
    private static final String CSS_STYLES = """
        .root { -fx-background-color: #FDF8F0; -fx-font-family: 'Segoe UI'; }
        .header-title { -fx-font-size: 2.2em; -fx-font-weight: bold; -fx-text-fill: #333333; }
        .tab-content-area { -fx-padding: 20 0 0 0; }
        .tab-pane .tab-header-area .tab-header-background { -fx-background-color: transparent; }
        .tab-pane .tab { -fx-background-color: transparent; -fx-border-color: transparent; -fx-padding: 8 15 8 15; -fx-font-size: 1.1em; }
        .tab-pane .tab:selected { -fx-background-color: transparent; -fx-border-color: #4A90E2; -fx-border-width: 0 0 3 0; -fx-text-fill: #4A90E2; -fx-font-weight: bold; }
        .card { -fx-background-color: white; -fx-border-color: #E0E0E0; -fx-border-width: 1; -fx-border-radius: 8; -fx-padding: 20px; }
        .card-title { -fx-font-size: 1.4em; -fx-font-weight: bold; -fx-text-fill: #333333; }
        .label { -fx-font-size: 1.05em; -fx-font-weight: 500; -fx-text-fill: #333333; }
        .combo-box, .text-field { -fx-font-size: 1.05em; -fx-pref-height: 38px; -fx-border-color: #CCCCCC; -fx-border-radius: 5; }
        .button-primary { -fx-background-color: #4A90E2; -fx-text-fill: white; -fx-font-weight: bold; -fx-pref-height: 40px; -fx-background-radius: 5; -fx-cursor: hand; }
        .button-add { -fx-background-color: #22C55E; -fx-text-fill: white; -fx-font-weight: bold; -fx-pref-height: 38px; -fx-cursor: hand; }
        .button-danger { -fx-background-color: transparent; -fx-text-fill: #EF4444; -fx-font-size: 1.4em; -fx-cursor: hand; }
        .button-delete-card { -fx-background-color: #EF4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 5; -fx-padding: 5 10; }
        .table-view .column-header { -fx-background-color: #F9FAFB; -fx-font-weight: bold; }
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
        mainContent.getChildren().addAll(crearTabPane());
        setCenter(mainContent);
    }

    /*private Node crearHeader() {
        VBox headerBox = new VBox(5);
        headerBox.setPadding(new Insets(0, 0, 20, 0));
        Label header = new Label("Orden de Compra"); header.getStyleClass().add("header-title");
        headerBox.getChildren().addAll(header, new Label("Gestión de compras y stock"));
        return headerBox;
    }*/

    private Node crearTabPane() {
        TabPane tabPane = new TabPane(); tabPane.setStyle("-fx-background-color: transparent;");

        Tab tabNueva = new Tab("Nueva Orden", crearTabNuevaOrden()); tabNueva.setClosable(false);

        Tab tabGestion = new Tab("Gestionar Órdenes", crearTabGestion()); tabGestion.setClosable(false);
        tabGestion.setOnSelectionChanged(e -> { if(tabGestion.isSelected()) cargarOrdenesPendientes(); });

        Tab tabHistorial = new Tab("Historial", crearTabHistorial()); tabHistorial.setClosable(false);
        tabHistorial.setOnSelectionChanged(e -> { if(tabHistorial.isSelected()) cargarHistorial(historialContainer); });

        tabPane.getTabs().addAll(tabNueva, tabGestion, tabHistorial);
        return tabPane;
    }

    // --- PESTAÑA NUEVA ORDEN ---
    private Node crearTabNuevaOrden() {
        VBox layout = new VBox(25); layout.setPadding(new Insets(20,0,0,0));
        VBox card = new VBox(25); card.getStyleClass().add("card");

        GridPane gridSup = new GridPane(); gridSup.setHgap(20); gridSup.setVgap(10);
        proveedorCombo.setPromptText("Seleccionar proveedor"); proveedorCombo.setMaxWidth(Double.MAX_VALUE);
        gridSup.add(crearCampo("Proveedor", proveedorCombo), 0, 0);
        fechaPicker.setMaxWidth(Double.MAX_VALUE); gridSup.add(crearCampo("Fecha Emisión", fechaPicker), 1, 0);
        ColumnConstraints col1 = new ColumnConstraints(); col1.setPercentWidth(50);
        gridSup.getColumnConstraints().addAll(col1, col1);

        VBox addBox = new VBox(15); addBox.setStyle("-fx-border-color: #E0E0E0; -fx-border-radius: 8; -fx-padding: 15;");

        GridPane addGrid = new GridPane(); addGrid.setHgap(15);
        insumoCombo.setPromptText("Producto"); insumoCombo.setMaxWidth(Double.MAX_VALUE); insumoCombo.setDisable(true);
        insumoCombo.setCellFactory(lv->new InsumoListCell()); insumoCombo.setButtonCell(new InsumoListCell());
        cantidadField.setPromptText("Cantidad Total (ej. 1200 ml)");

        addGrid.add(crearCampo("Producto", insumoCombo), 0, 0);
        addGrid.add(crearCampo("Cantidad", cantidadField), 1, 0);

        Button addButton = new Button("➕ Agregar"); addButton.getStyleClass().add("button-add");
        addButton.setOnAction(e -> agregarItem());
        addGrid.add(new VBox(new Label(), addButton), 2, 0);

        ColumnConstraints c1 = new ColumnConstraints(); c1.setPercentWidth(60);
        ColumnConstraints c2 = new ColumnConstraints(); c2.setPercentWidth(20);
        ColumnConstraints c3 = new ColumnConstraints(); c3.setPercentWidth(20);
        addGrid.getColumnConstraints().addAll(c1, c2, c3);

        addBox.getChildren().addAll(new Label("Agregar Items"), addGrid);

        proveedorCombo.setOnAction(e -> {
            if(proveedorCombo.getValue()!=null) {
                filtrarInsumosPorProveedor(insumoCombo, obtenerIdProveedor(proveedorCombo.getValue()));
                insumoCombo.setDisable(false);
            } else insumoCombo.setDisable(true);
        });

        configurarTablaItems(tablaItems, items, totalLabel);

        HBox totalBox = new HBox(10, new Label("Total:"), totalLabel); totalBox.setAlignment(Pos.CENTER_RIGHT);
        ((Label)totalBox.getChildren().get(0)).setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        totalLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));

        Button saveBtn = new Button("🛒 Guardar Orden (Pendiente)"); saveBtn.getStyleClass().add("button-primary"); saveBtn.setMaxWidth(Double.MAX_VALUE);
        saveBtn.setOnAction(e -> guardarOrden());

        card.getChildren().addAll(new Label("Crear Borrador"), gridSup, addBox, tablaItems, totalBox, saveBtn);
        layout.getChildren().add(card);
        return layout;
    }

    private void configurarTablaItems(TableView<ItemOrden> table, ObservableList<ItemOrden> list, Label lblTotal) {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<ItemOrden, String> c1 = new TableColumn<>("Producto");
        c1.setCellValueFactory(new PropertyValueFactory<>("nombre"));

        TableColumn<ItemOrden, Double> c2 = new TableColumn<>("Cant. Total");
        c2.setCellValueFactory(new PropertyValueFactory<>("cantidad"));

        TableColumn<ItemOrden, String> c3 = new TableColumn<>("Unidad");
        c3.setCellValueFactory(new PropertyValueFactory<>("unidad"));

        TableColumn<ItemOrden, Double> c4 = new TableColumn<>("Subtotal ($)");
        c4.setCellValueFactory(new PropertyValueFactory<>("total"));
        c4.setCellFactory(tc -> new TableCell<>(){ @Override protected void updateItem(Double p, boolean e){super.updateItem(p,e); setText(e?null:String.format("$%.2f", p));}});

        TableColumn<ItemOrden, Void> c5 = new TableColumn<>("");
        c5.setCellFactory(p -> new TableCell<>(){
            final Button b = new Button("🗑️"); { b.getStyleClass().add("button-danger"); b.setOnAction(e->{ list.remove(getIndex()); actualizarTotal(list, lblTotal); }); }
            @Override protected void updateItem(Void i, boolean e){super.updateItem(i,e); setGraphic(e?null:b); setAlignment(Pos.CENTER);}
        });

        table.getColumns().setAll(c1, c2, c3, c4, c5);
        table.setItems(list);
        table.setPlaceholder(new Label("Lista vacía."));
    }

    // --- PESTAÑA GESTIONAR ---
    private Node crearTabGestion() {
        VBox layout = new VBox(20); layout.setPadding(new Insets(20,0,0,0));
        VBox card = new VBox(15); card.getStyleClass().add("card");

        tablaGestion.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        TableColumn<ItemOrdenGestion, Integer> c1 = new TableColumn<>("ID"); c1.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<ItemOrdenGestion, String> c2 = new TableColumn<>("Proveedor"); c2.setCellValueFactory(new PropertyValueFactory<>("proveedorNombre"));
        TableColumn<ItemOrdenGestion, String> c3 = new TableColumn<>("Fecha"); c3.setCellValueFactory(new PropertyValueFactory<>("fechaTexto"));
        TableColumn<ItemOrdenGestion, Double> c4 = new TableColumn<>("Total"); c4.setCellValueFactory(new PropertyValueFactory<>("total"));
        c4.setCellFactory(tc -> new TableCell<>(){ @Override protected void updateItem(Double p, boolean e){super.updateItem(p,e); setText(e?null:String.format("$%.2f", p));}});
        TableColumn<ItemOrdenGestion, String> c5 = new TableColumn<>("Estado"); c5.setCellValueFactory(new PropertyValueFactory<>("estado"));

        TableColumn<ItemOrdenGestion, Void> c6 = new TableColumn<>("Acciones");
        c6.setCellFactory(p -> new TableCell<>(){
            // Botones de Acción
            final Button b1 = new Button("✔"); // Aceptar
            final Button b2 = new Button("✏"); // Editar
            final Button b3 = new Button("✖"); // Rechazar
            final Button b4 = new Button("📄"); // Imprimir PDF (Nuevo)

            final HBox box = new HBox(5, b1, b2, b3, b4);
            {
                b1.getStyleClass().add("button-accept"); b1.setTooltip(new Tooltip("Recibir Mercadería"));
                b2.getStyleClass().add("button-edit");   b2.setTooltip(new Tooltip("Editar Orden"));
                b3.getStyleClass().add("button-reject"); b3.setTooltip(new Tooltip("Rechazar Orden"));
                b4.getStyleClass().add("button-print");  b4.setTooltip(new Tooltip("Descargar/Imprimir PDF"));

                box.setAlignment(Pos.CENTER);

                b1.setOnAction(e -> accionAceptarOrden(getTableView().getItems().get(getIndex())));
                b2.setOnAction(e -> accionEditarOrden(getTableView().getItems().get(getIndex())));
                b3.setOnAction(e -> accionRechazarOrden(getTableView().getItems().get(getIndex())));
                b4.setOnAction(e -> accionImprimirPDF(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void i, boolean e){super.updateItem(i,e); setGraphic(e?null:box);}
        });

        tablaGestion.getColumns().setAll(c1, c2, c3, c4, c5, c6);
        tablaGestion.setItems(listaOrdenesPendientes);

        card.getChildren().addAll(new Label("Órdenes Pendientes"), tablaGestion);
        VBox.setVgrow(tablaGestion, Priority.ALWAYS);
        layout.getChildren().add(card);
        cargarOrdenesPendientes();
        return layout;
    }

    // --- ACCIÓN: IMPRIMIR PDF ---
    private void accionImprimirPDF(ItemOrdenGestion orden) {
        // 1. Obtener detalles completos de la orden
        List<ItemOrden> detalles = new ArrayList<>();
        ObservableList<ItemOrden> obsList = FXCollections.observableArrayList(); // solo para reusar metodo cargar
        cargarItemsDeOrden(orden.id(), obsList);
        detalles.addAll(obsList);

        if (detalles.isEmpty()) {
            mostrarAlerta("Error", "No se encontraron detalles para la orden.");
            return;
        }

        // 2. Construir el Nodo Visual (La "Factura")
        VBox doc = new VBox(20);
        doc.setPadding(new Insets(40));
        doc.setStyle("-fx-background-color: white;");
        doc.setPrefWidth(595); // Ancho aproximado A4 en pixeles (72 DPI)

        // Cabecera
        VBox header = new VBox(5);
        header.setAlignment(Pos.CENTER);
        Label title = new Label("ORDEN DE COMPRA");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        Label subtitle = new Label("Mamatania Inventario System");
        subtitle.setFont(Font.font("Segoe UI", 14));
        header.getChildren().addAll(title, subtitle);

        // Info Orden
        GridPane info = new GridPane();
        info.setHgap(20); info.setVgap(10);
        info.setStyle("-fx-padding: 20 0 20 0; -fx-border-width: 1 0 1 0; -fx-border-color: #EEE;");

        info.add(new Label("Orden N°:"), 0, 0);
        Label lblId = new Label(String.valueOf(orden.id())); lblId.setFont(Font.font("System", FontWeight.BOLD, 12));
        info.add(lblId, 1, 0);

        info.add(new Label("Fecha:"), 0, 1);
        info.add(new Label(orden.fechaTexto()), 1, 1);

        info.add(new Label("Proveedor:"), 2, 0);
        Label lblProv = new Label(orden.proveedorNombre()); lblProv.setFont(Font.font("System", FontWeight.BOLD, 12));
        info.add(lblProv, 3, 0);

        // Tabla Detalles (Construida manualmente para impresión)
        GridPane table = new GridPane();
        table.setHgap(10); table.setVgap(5);
        table.setPadding(new Insets(10, 0, 0, 0));

        // Headers
        String[] headers = {"Producto", "Cant.", "Unid.", "P. Unit", "Subtotal"};
        for (int i=0; i<headers.length; i++) {
            Label h = new Label(headers[i]);
            h.setFont(Font.font("System", FontWeight.BOLD, 12));
            h.setStyle("-fx-border-color: #333; -fx-border-width: 0 0 1 0; -fx-padding: 0 0 5 0;");
            h.setPrefWidth(i==0 ? 200 : 70); // Ancho columnas
            table.add(h, i, 0);
        }

        // Rows
        int row = 1;
        for (ItemOrden item : detalles) {
            table.add(new Label(item.getNombre()), 0, row);
            table.add(new Label(String.valueOf(item.getCantidad())), 1, row);
            table.add(new Label(item.getUnidad()), 2, row);
            table.add(new Label(String.format("$%.2f", item.getPrecioUnitario())), 3, row);
            table.add(new Label(String.format("$%.2f", item.getTotal())), 4, row);
            row++;
        }

        // Footer Total
        HBox footer = new HBox(10);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(20, 0, 0, 0));
        Label lblTotalTxt = new Label("TOTAL:");
        lblTotalTxt.setFont(Font.font("System", FontWeight.BOLD, 14));
        Label lblTotalVal = new Label(String.format("$%.2f", orden.total()));
        lblTotalVal.setFont(Font.font("System", FontWeight.BOLD, 16));
        footer.getChildren().addAll(lblTotalTxt, lblTotalVal);

        doc.getChildren().addAll(header, info, table, footer);

        // 3. Imprimir
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job != null) {
            boolean proceed = job.showPrintDialog(getScene().getWindow());
            if (proceed) {
                // Escalar al ancho de página
                double pageW = job.getJobSettings().getPageLayout().getPrintableWidth();
                double scale = pageW / doc.getPrefWidth();
                doc.getTransforms().add(new javafx.scene.transform.Scale(scale, scale));

                boolean success = job.printPage(doc);
                if (success) {
                    job.endJob();
                    mostrarAlerta("Éxito", "Documento enviado a la cola de impresión/PDF.");
                } else {
                    mostrarAlerta("Error", "Falló la impresión.");
                }
            }
        } else {
            mostrarAlerta("Error", "No se encontró impresora disponible.");
        }
    }

    // --- ACCIÓN: ACEPTAR (ACTUALIZADA CON LOTES) ---
    private void accionAceptarOrden(ItemOrdenGestion orden) {
        // 1. Recuperar los items de la orden desde la BD
        List<ItemRecepcion> itemsParaRecibir = new ArrayList<>();
        String sqlDet = "SELECT d.IdProducto, d.Cantidad, p.Tipo_de_Producto " +
                "FROM detalle_compra d " +
                "JOIN producto p ON d.IdProducto = p.IdProducto " +
                "WHERE d.IdCompra = ?";

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlDet)) {
            stmt.setInt(1, orden.id());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                itemsParaRecibir.add(new ItemRecepcion(
                        rs.getInt("IdProducto"),
                        rs.getString("Tipo_de_Producto"),
                        rs.getDouble("Cantidad")
                ));
            }
        } catch (SQLException e) {
            mostrarAlerta("Error", "No se pudieron cargar los detalles: " + e.getMessage());
            return;
        }

        if (itemsParaRecibir.isEmpty()) { mostrarAlerta("Error", "La orden está vacía."); return; }

        // 2. Diálogo para confirmar Vencimientos
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Recepción de Mercadería - Orden #" + orden.id());
        dialog.setHeaderText("Confirme las fechas de vencimiento para los lotes.");

        ButtonType btnConfirmar = new ButtonType("Confirmar Ingreso", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnConfirmar, ButtonType.CANCEL);

        TableView<ItemRecepcion> tableRecepcion = new TableView<>();
        tableRecepcion.setEditable(true);
        tableRecepcion.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<ItemRecepcion, String> colProd = new TableColumn<>("Producto");
        colProd.setCellValueFactory(new PropertyValueFactory<>("nombreProducto"));

        TableColumn<ItemRecepcion, Double> colCant = new TableColumn<>("Cantidad");
        colCant.setCellValueFactory(new PropertyValueFactory<>("cantidad"));

        TableColumn<ItemRecepcion, LocalDate> colFecha = new TableColumn<>("F. Vencimiento");
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fechaVencimiento"));
        colFecha.setCellFactory(col -> new TableCell<>() {
            private final DatePicker datePicker = new DatePicker();
            { datePicker.setOnAction(e -> commitEdit(datePicker.getValue())); }
            @Override protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null); else { datePicker.setValue(item); setGraphic(datePicker); }
            }
        });
        colFecha.setOnEditCommit(e -> e.getRowValue().setFechaVencimiento(e.getNewValue()));

        tableRecepcion.getColumns().addAll(colProd, colCant, colFecha);
        tableRecepcion.setItems(FXCollections.observableArrayList(itemsParaRecibir));
        tableRecepcion.setPrefHeight(300); tableRecepcion.setPrefWidth(500);

        dialog.getDialogPane().setContent(new VBox(10, new Label("Ajuste las fechas si es necesario:"), tableRecepcion));

        dialog.setResultConverter(btn -> {
            if (btn == btnConfirmar) return procesarIngresoEnBD(orden.id(), itemsParaRecibir);
            return false;
        });

        dialog.showAndWait();
    }

    private boolean procesarIngresoEnBD(int idOrden, List<ItemRecepcion> items) {
        Connection conn = null;
        try {
            conn = ConexionBD.getConnection(); conn.setAutoCommit(false);

            String sqlLote = "INSERT INTO lotes (IdProducto, CantidadActual, FechaVencimiento, FechaIngreso) VALUES (?, ?, ?, ?)";
            String sqlStock = "UPDATE producto SET Stock = Stock + ? WHERE IdProducto = ?";
            String sqlKardex = "INSERT INTO kardex (IdProducto, Fecha, Motivo, TipoMovimiento, IdEmpleado, Cantidad) VALUES (?, ?, ?, ?, ?, ?)";

            try (PreparedStatement psLote = conn.prepareStatement(sqlLote);
                 PreparedStatement psStock = conn.prepareStatement(sqlStock);
                 PreparedStatement psKardex = conn.prepareStatement(sqlKardex)) {

                for (ItemRecepcion item : items) {
                    // Lote
                    psLote.setInt(1, item.getIdProducto()); psLote.setDouble(2, item.getCantidad());
                    if (item.getFechaVencimiento() != null) psLote.setString(3, item.getFechaVencimiento().toString()); else psLote.setNull(3, Types.VARCHAR);
                    psLote.setString(4, LocalDate.now().toString()); psLote.addBatch();

                    // Stock
                    psStock.setDouble(1, item.getCantidad()); psStock.setInt(2, item.getIdProducto()); psStock.addBatch();

                    // Kardex
                    psKardex.setInt(1, item.getIdProducto()); psKardex.setString(2, LocalDate.now().toString());
                    psKardex.setString(3, "Recepción Orden #" + idOrden); psKardex.setString(4, "Entrada");
                    psKardex.setInt(5, 1); psKardex.setDouble(6, item.getCantidad()); psKardex.addBatch();
                }
                psLote.executeBatch(); psStock.executeBatch(); psKardex.executeBatch();
            }

            try (PreparedStatement psOrden = conn.prepareStatement("UPDATE orden_compra SET Estado = 'Completada' WHERE IdCompra = ?")) {
                psOrden.setInt(1, idOrden); psOrden.executeUpdate();
            }

            conn.commit();
            mostrarAlerta("Éxito", "Mercadería ingresada y Lotes creados.");
            cargarOrdenesPendientes(); cargarHistorial(historialContainer);
            return true;

        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (Exception ex) {}
            mostrarAlerta("Error Crítico", "Fallo al ingresar: " + e.getMessage()); return false;
        } finally {
            if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (Exception ex) {}
        }
    }

    private void accionRechazarOrden(ItemOrdenGestion o) {
        try(Connection c = ConexionBD.getConnection(); PreparedStatement ps = c.prepareStatement("UPDATE orden_compra SET Estado='Rechazada' WHERE IdCompra=?")) {
            ps.setInt(1, o.id()); ps.executeUpdate();
            mostrarAlerta("Info", "Orden rechazada."); cargarOrdenesPendientes();
        } catch(Exception e) { mostrarAlerta("Error", e.getMessage()); }
    }

    // --- ACCIÓN: EDITAR ---
    private void accionEditarOrden(ItemOrdenGestion orden) {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Editar Orden #" + orden.id());
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().setPrefWidth(900);

        VBox layout = new VBox(20); layout.setPadding(new Insets(20));

        // Cargar cabecera
        ComboBox<String> cProv = new ComboBox<>(); cargarProveedores(cProv); cProv.setValue(orden.proveedorNombre());
        DatePicker cFecha = new DatePicker(); try{cFecha.setValue(LocalDate.parse(orden.fechaTexto()));}catch(Exception e){}

        // Cargar tabla temporal
        TableView<ItemOrden> tbl = new TableView<>(); Label lblTot = new Label();
        ObservableList<ItemOrden> lst = FXCollections.observableArrayList();
        configurarTablaItems(tbl, lst, lblTot);
        cargarItemsDeOrden(orden.id(), lst); actualizarTotal(lst, lblTot);

        // Formulario para agregar MÁS items
        ComboBox<Insumo> cIns = new ComboBox<>();
        cIns.setCellFactory(lv->new InsumoListCell()); cIns.setButtonCell(new InsumoListCell());

        // Filtro inicial y listener
        int idProv = obtenerIdProveedor(orden.proveedorNombre());
        filtrarInsumosPorProveedor(cIns, idProv);
        cProv.setOnAction(e -> filtrarInsumosPorProveedor(cIns, obtenerIdProveedor(cProv.getValue())));

        TextField cCant = new TextField();
        Button btnAdd = new Button("Agregar");
        btnAdd.setOnAction(e -> agregarItemLogica(cIns, cCant, lst, lblTot));

        HBox addBox = new HBox(10, crearCampo("Producto", cIns), crearCampo("Cant.", cCant), btnAdd);
        addBox.setAlignment(Pos.BOTTOM_LEFT);

        layout.getChildren().addAll(new HBox(20, crearCampo("Proveedor", cProv), crearCampo("Fecha", cFecha)), addBox, tbl, new HBox(new Label("Total: "), lblTot));
        dialog.getDialogPane().setContent(layout);

        dialog.setResultConverter(btn -> {
            if(btn == ButtonType.OK) {
                if(lst.isEmpty()) { mostrarAlerta("Error", "Mínimo 1 item"); return false; }
                return actualizarOrdenEnBD(orden.id(), obtenerIdProveedor(cProv.getValue()), cFecha.getValue(), lst);
            } return false;
        });

        if(dialog.showAndWait().orElse(false)) {
            mostrarAlerta("Éxito", "Orden actualizada.");
            cargarOrdenesPendientes();
        }
    }

    private boolean actualizarOrdenEnBD(int idOrden, int idProv, LocalDate f, ObservableList<ItemOrden> l) {
        try(Connection c = ConexionBD.getConnection()) { c.setAutoCommit(false);
            // 1. Update Cabecera
            try(PreparedStatement ps = c.prepareStatement("UPDATE orden_compra SET IdProveedor=?, Fecha_de_Compra=?, Precio_total=? WHERE IdCompra=?")) {
                ps.setInt(1, idProv); ps.setString(2, f.toString());
                ps.setDouble(3, l.stream().mapToDouble(ItemOrden::getTotal).sum());
                ps.setInt(4, idOrden); ps.executeUpdate();
            }
            // 2. Borrar detalles viejos
            c.createStatement().executeUpdate("DELETE FROM detalle_compra WHERE IdCompra="+idOrden);
            // 3. Insertar nuevos
            try(PreparedStatement ps = c.prepareStatement("INSERT INTO detalle_compra (IdCompra, IdProducto, Cantidad, PrecioUnitario, SubTotal) VALUES (?, ?, ?, ?, ?)")) {
                for(ItemOrden i:l) {
                    ps.setInt(1, idOrden); ps.setInt(2, i.getInsumoId()); ps.setDouble(3, i.getCantidad());
                    ps.setDouble(4, i.getPrecioUnitario()); ps.setDouble(5, i.getTotal()); ps.addBatch();
                }
                ps.executeBatch();
            }
            c.commit(); return true;
        } catch(Exception e) { return false; }
    }

    // --- CARGAS DE DATOS ---
    private void cargarOrdenesPendientes() {
        listaOrdenesPendientes.clear();
        try(Connection c = ConexionBD.getConnection(); ResultSet rs = c.createStatement().executeQuery("SELECT o.IdCompra, p.Nombre_comercial, o.Fecha_de_Compra, o.Precio_total, o.Estado FROM orden_compra o JOIN proveedores p ON o.IdProveedor = p.IdProveedor WHERE o.Estado = 'Pendiente' ORDER BY o.Fecha_de_Compra ASC")) {
            while(rs.next()) listaOrdenesPendientes.add(new ItemOrdenGestion(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getDouble(4), rs.getString(5)));
        } catch(Exception e){}
    }

    private void cargarItemsDeOrden(int id, ObservableList<ItemOrden> lista) {
        lista.clear();
        String sql = "SELECT d.IdProducto, d.Cantidad, p.Tipo_de_Producto, p.PrecioUnitario, p.Unidad_de_medida, p.Contenido FROM detalle_compra d JOIN producto p ON d.IdProducto=p.IdProducto WHERE d.IdCompra=?";
        try(Connection c = ConexionBD.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id); ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                Insumo in = new Insumo(rs.getInt(1), 0, rs.getString(3), rs.getDouble(4), rs.getString(5), rs.getDouble(6));
                lista.add(new ItemOrden(in, rs.getDouble(2)));
            }
        } catch(Exception e){}
    }

    private void guardarOrden() {
        if(items.isEmpty()) { mostrarAlerta("Error", "Faltan items."); return; }
        try(Connection c = ConexionBD.getConnection()) { c.setAutoCommit(false);
            int id=0;
            try(PreparedStatement ps = c.prepareStatement("INSERT INTO orden_compra (IdProveedor, IdEmpleado, Fecha_de_Compra, Precio_total, Estado) VALUES (?, ?, ?, ?, 'Pendiente')", Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, obtenerIdProveedor(proveedorCombo.getValue())); ps.setInt(2, 1); ps.setString(3, fechaPicker.getValue().toString());
                ps.setDouble(4, items.stream().mapToDouble(ItemOrden::getTotal).sum()); ps.executeUpdate();
                ResultSet rs = ps.getGeneratedKeys(); if(rs.next()) id = rs.getInt(1);
            }
            try(PreparedStatement ps = c.prepareStatement("INSERT INTO detalle_compra (IdCompra, IdProducto, Cantidad, PrecioUnitario, SubTotal) VALUES (?, ?, ?, ?, ?)")) {
                for(ItemOrden i : items) {
                    ps.setInt(1, id); ps.setInt(2, i.getInsumoId()); ps.setDouble(3, i.getCantidad());
                    ps.setDouble(4, i.getPrecioUnitario()); ps.setDouble(5, i.getTotal()); ps.addBatch();
                }
                ps.executeBatch();
            }
            c.commit(); mostrarAlerta("Éxito", "Guardada."); items.clear(); actualizarTotal(items, totalLabel); cargarOrdenesPendientes();
        } catch(Exception e) { e.printStackTrace(); }
    }

    // --- HISTORIAL ---
    private Node crearTabHistorial() {
        VBox layout = new VBox(20); layout.getStyleClass().add("tab-content-area");
        VBox card = new VBox(15); card.getStyleClass().add("card");
        card.getChildren().add(new Label("Historial de Compras"));
        historialContainer = new VBox(10);
        ScrollPane scroll = new ScrollPane(historialContainer);
        scroll.setFitToWidth(true); scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        card.getChildren().add(scroll); VBox.setVgrow(scroll, Priority.ALWAYS);
        layout.getChildren().add(card);
        return layout;
    }
    private void cargarHistorial(VBox c) {
        c.getChildren().clear();
        try(Connection conn=ConexionBD.getConnection(); ResultSet rs=conn.createStatement().executeQuery("SELECT o.IdCompra, p.Nombre_comercial, o.Fecha_de_Compra, o.Precio_total FROM orden_compra o JOIN proveedores p ON o.IdProveedor=p.IdProveedor WHERE o.Estado!='Pendiente' ORDER BY o.Fecha_de_Compra DESC LIMIT 50")) {
            while(rs.next()) {
                Label l = new Label("Orden #" + rs.getInt(1) + " - " + rs.getString(2) + " ($" + rs.getDouble(4) + ")");
                l.setStyle("-fx-font-weight:bold;"); c.getChildren().add(new VBox(l, new Separator()));
            }
        } catch(Exception e){}
    }

    // --- UTILS ---
    private VBox crearCampo(String l, Node c) { VBox v=new VBox(5); Label lbl=new Label(l); lbl.getStyleClass().add("label"); v.getChildren().addAll(lbl, c); return v; }
    private void mostrarAlerta(String t, String m) { Alert a=new Alert(Alert.AlertType.INFORMATION); a.setTitle(t); a.setContentText(m); a.showAndWait(); }
    private void agregarItem() { agregarItemLogica(insumoCombo, cantidadField, items, totalLabel); }
    private void agregarItemLogica(ComboBox<Insumo> c, TextField f, ObservableList<ItemOrden> l, Label t) {
        try { Insumo i = c.getValue(); double q = Double.parseDouble(f.getText());
            if(i!=null && q>0) {
                boolean existe = false;
                for(ItemOrden io : l) { if(io.getInsumoId() == i.getId()) { io.setCantidad(io.getCantidad() + q); existe = true; break; } }
                if(!existe) l.add(new ItemOrden(i, q));
                actualizarTotal(l, t); c.getSelectionModel().clearSelection(); f.clear();
            }
        } catch(Exception e) { mostrarAlerta("Error", "Datos incorrectos"); }
    }
    private void actualizarTotal(ObservableList<ItemOrden> l, Label t) { t.setText(String.format("$%.2f", l.stream().mapToDouble(ItemOrden::getTotal).sum())); }
    private void cargarProveedores(ComboBox<String> c) { try(Connection cn=ConexionBD.getConnection(); ResultSet rs=cn.createStatement().executeQuery("SELECT Nombre_comercial FROM proveedores")) { while(rs.next()) c.getItems().add(rs.getString(1)); } catch(Exception e){} }

    private void cargarTodosLosInsumos() {
        try(Connection cn=ConexionBD.getConnection(); ResultSet rs=cn.createStatement().executeQuery("SELECT IdProducto, IdProveedor, Tipo_de_Producto, PrecioUnitario, Unidad_de_medida, Contenido FROM producto")) {
            while(rs.next()) {
                double cont = rs.getDouble("Contenido");
                todosLosInsumos.add(new Insumo(rs.getInt(1), rs.getInt(2), rs.getString(3), rs.getDouble(4), rs.getString(5), cont));
            }
        } catch(Exception e){}
    }
    private int obtenerIdProveedor(String n) { try(Connection cn=ConexionBD.getConnection(); PreparedStatement p=cn.prepareStatement("SELECT IdProveedor FROM proveedores WHERE Nombre_comercial=?")) { p.setString(1,n); ResultSet r=p.executeQuery(); if(r.next()) return r.getInt(1); } catch(Exception e){} return 0; }
    private void filtrarInsumosPorProveedor(ComboBox<Insumo> c, int id) { c.setItems(FXCollections.observableArrayList(todosLosInsumos.stream().filter(i->i.getIdProveedor()==id).collect(Collectors.toList()))); }

    // --- CLASES INTERNAS ---
    public static class Insumo {
        private final int id, idProv; private final String nombre, unidad; private final double precio, contenido;
        public Insumo(int i, int ip, String n, double p, String u, double cont) { id=i;idProv=ip;nombre=n;precio=p;unidad=u;contenido=(cont<=0?1:cont); }
        public int getId() { return id; } public int getIdProveedor() { return idProv; } public String getNombre() { return nombre; }
        public double getPrecioUnitario() { return precio; } public String getUnidad() { return unidad; } public double getContenido() { return contenido; }
        @Override public String toString() { return nombre + " ($" + precio + ")"; }
    }
    private static class InsumoListCell extends ListCell<Insumo>{ @Override protected void updateItem(Insumo i, boolean e){super.updateItem(i,e); setText(i==null?"":i.toString());}}

    public static class ItemOrden {
        private final int insumoId; private final String nombre, unidad; private double cantidad; private final double precioUnitario, total, contenido;
        public ItemOrden(Insumo i, double cantidadIngresada) {
            this.insumoId=i.getId(); this.nombre=i.getNombre(); this.unidad=i.getUnidad(); this.cantidad=cantidadIngresada; this.precioUnitario=i.getPrecioUnitario(); this.contenido=i.getContenido();
            this.total = (cantidadIngresada / this.contenido) * this.precioUnitario;
        }
        public int getInsumoId(){return insumoId;} public String getNombre(){return nombre;} public String getUnidad(){return unidad;}
        public double getCantidad(){return cantidad;} public double getPrecioUnitario(){return precioUnitario;} public double getTotal(){return total;}
        public void setCantidad(double c){ this.cantidad=c; }
    }

    public record ItemOrdenGestion(int id, String proveedorNombre, String fechaTexto, double total, String estado) {
    }

    // Aux para Recepcion
    public static class ItemRecepcion {
        private final int idProducto; private final String nombreProducto; private final double cantidad; private LocalDate fechaVencimiento;
        public ItemRecepcion(int id, String nom, double cant) { this.idProducto = id; this.nombreProducto = nom; this.cantidad = cant; this.fechaVencimiento = LocalDate.now().plusDays(30); }
        public int getIdProducto() { return idProducto; } public String getNombreProducto() { return nombreProducto; } public double getCantidad() { return cantidad; }
        public LocalDate getFechaVencimiento() { return fechaVencimiento; } public void setFechaVencimiento(LocalDate f) { this.fechaVencimiento = f; }
    }

    public static class TestApp extends Application { @Override public void start(Stage s) { s.setScene(new Scene(new OrdenesPage(), 1000, 800)); s.show(); } }
    public static void main(String[] args) { Application.launch(TestApp.class, args); }
}