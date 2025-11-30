package com.inventario.ui.almacen3;

import com.inventario.config.ConexionBD;
import com.inventario.dao.Almacen3DAO;
import com.inventario.logic.ProduccionService; // <--- IMPORTANTE: Importar el servicio
import com.inventario.model.LoteTerminado;
import com.inventario.model.ProductoTerminado;
import com.inventario.model.RecetaSimple;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.util.StringConverter;

import java.sql.*;
import java.time.LocalDate;
import java.util.function.Consumer;

public class Almacen3Page extends BorderPane {

    // ... (MANTÉN TODO TU CÓDIGO CSS Y VARIABLES UI IGUAL QUE ANTES) ...
    private static final String CSS_STYLES = """
        .root { -fx-background-color: #FDF8F0; -fx-font-family: 'Segoe UI'; }
        .header-title { -fx-font-size: 2.2em; -fx-font-weight: bold; -fx-text-fill: #333333; }
        .header-description { -fx-font-size: 1.1em; -fx-text-fill: #555555; }
        .card { -fx-background-color: white; -fx-border-color: #E0E0E0; -fx-border-width: 1; -fx-border-radius: 8; -fx-padding: 20px; }
        .card-title { -fx-font-size: 1.4em; -fx-font-weight: bold; -fx-text-fill: #333333; }
        .stats-card { -fx-padding: 15px 20px; -fx-background-color: white; -fx-border-color: #E0E0E0; -fx-border-width: 1; -fx-border-radius: 8; }
        .stats-card-title { -fx-font-size: 0.95em; -fx-font-weight: 500; -fx-text-fill: #333333; }
        .stats-card-content { -fx-font-size: 1.8em; -fx-font-weight: bold; -fx-text-fill: #111827; }
        .stats-card-content-danger { -fx-text-fill: #DC2626; }
        .badge { -fx-padding: 4 10; -fx-background-radius: 12; -fx-font-size: 0.9em; -fx-font-weight: bold; -fx-text-fill: white; }
        .badge-stock-low { -fx-background-color: #EF4444; }
        .badge-stock-normal { -fx-background-color: #22C55E; }
        .button-primary { -fx-background-color: #4A90E2; -fx-text-fill: white; -fx-font-weight: bold; -fx-pref-height: 40px; -fx-cursor: hand; -fx-background-radius: 5; }
        .search-field { -fx-pref-height: 40px; -fx-border-color: #CCCCCC; -fx-border-radius: 5; }
    """;

    private final Consumer<String> onNavigate;
    private TableView<ProductoTerminado> tablaTerminados;
    private TextField searchField;
    private Label totalTerminadosLabel, stockBajoLabel, valorEstimadoLabel;
    private final ObservableList<ProductoTerminado> listaTerminados = FXCollections.observableArrayList();
    private FilteredList<ProductoTerminado> filteredTerminados;

    public Almacen3Page(Consumer<String> onNavigate) {
        this.onNavigate = onNavigate;
        this.getStylesheets().add("data:text/css," + CSS_STYLES.replace("\n", ""));
        this.getStyleClass().add("root");
        setPadding(new Insets(30, 40, 30, 40));

        VBox mainContent = new VBox(20);

        // Header
        VBox headerBox = new VBox(5);
        Label header = new Label("Almacén 3 - Productos Terminados"); header.getStyleClass().add("header-title");
        Label desc = new Label("Gestión de stock final basado en recetas de producción.");
        desc.getStyleClass().add("header-description");
        headerBox.getChildren().addAll(header, desc);

        // Actions Bar
        HBox actionsBar = new HBox(15);
        actionsBar.setAlignment(Pos.CENTER_LEFT);
        searchField = new TextField();
        searchField.setPromptText("Buscar producto terminado...");
        searchField.getStyleClass().add("search-field");
        HBox.setHgrow(searchField, Priority.ALWAYS);

        Button btnProducir = new Button("🏭 Producir (Usar Receta)");
        btnProducir.getStyleClass().add("button-primary");
        btnProducir.setOnAction(e -> mostrarDialogoProduccion());

        actionsBar.getChildren().addAll(searchField, btnProducir);

        // Stats Grid
        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(20);
        ColumnConstraints col = new ColumnConstraints(); col.setPercentWidth(33.33);
        statsGrid.getColumnConstraints().addAll(col, col, col);

        totalTerminadosLabel = new Label("0");
        stockBajoLabel = new Label("0");
        valorEstimadoLabel = new Label("$0.00");

        statsGrid.add(createStatCard("Total Productos", totalTerminadosLabel, null), 0, 0);
        statsGrid.add(createStatCard("Valor Venta Est.", valorEstimadoLabel, null), 1, 0);
        statsGrid.add(createStatCard("Stock Bajo", stockBajoLabel, "stats-card-content-danger"), 2, 0);

        // Tabla
        VBox tableCard = new VBox(15);
        tableCard.getStyleClass().add("card");
        Label tableTitle = new Label("Inventario Disponible"); tableTitle.getStyleClass().add("card-title");

        tablaTerminados = new TableView<>();
        configurarTabla();

        tableCard.getChildren().addAll(tableTitle, tablaTerminados);
        VBox.setVgrow(tablaTerminados, Priority.ALWAYS);

        mainContent.getChildren().addAll(headerBox, actionsBar, statsGrid, tableCard);
        setCenter(mainContent);

        cargarDatos();
        setupFiltering();
    }

    // ... (MANTÉN LOS MÉTODOS VISUALES: createStatCard, configurarTabla, cargarDatos, setupFiltering) ...
    // ... (COPIA Y PEGA ESOS MÉTODOS DE TU CÓDIGO ORIGINAL AQUÍ) ...

    private VBox createStatCard(String title, Label value, String style) {
        // (Tu código original...)
        VBox card = new VBox(5); card.getStyleClass().add("stats-card");
        Label lblTitle = new Label(title); lblTitle.getStyleClass().add("stats-card-title");
        value.getStyleClass().add("stats-card-content");
        if (style != null) value.getStyleClass().add(style);
        card.getChildren().addAll(lblTitle, value);
        return card;
    }

    private void configurarTabla() {
        // (Tu código original...)
        tablaTerminados.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        TableColumn<ProductoTerminado, String> cNombre = new TableColumn<>("Producto");
        cNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        TableColumn<ProductoTerminado, Integer> cStock = new TableColumn<>("Stock Total");
        cStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        TableColumn<ProductoTerminado, Double> cPrecio = new TableColumn<>("Precio Venta");
        cPrecio.setCellValueFactory(new PropertyValueFactory<>("precioVenta"));
        cPrecio.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : String.format("$%.2f", item));
            }
        });
        TableColumn<ProductoTerminado, String> cEstado = new TableColumn<>("Estado");
        cEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        cEstado.setCellFactory(c -> new TableCell<>() {
            final Label badge = new Label();
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                badge.setText(item); badge.getStyleClass().clear(); badge.getStyleClass().add("badge");
                badge.getStyleClass().add(item.equals("Bajo Stock") ? "badge-stock-low" : "badge-stock-normal");
                setGraphic(badge); setAlignment(Pos.CENTER_LEFT);
            }
        });
        TableColumn<ProductoTerminado, Void> cAccion = new TableColumn<>("Detalle");
        cAccion.setCellFactory(param -> new TableCell<>() {
            private final Button btnVer = new Button("👁 Lotes");
            {
                btnVer.setStyle("-fx-background-color: #4A90E2; -fx-text-fill: white; -fx-cursor: hand; -fx-font-size: 0.85em;");
                btnVer.setOnAction(e -> mostrarDialogoLotes(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); } else { setGraphic(btnVer); setAlignment(Pos.CENTER); }
            }
        });
        tablaTerminados.getColumns().addAll(cNombre, cStock, cPrecio, cEstado, cAccion);
    }

    private void cargarDatos() {
        listaTerminados.clear();
        int bajoStock = 0;
        double valorTotal = 0;

        // --- USO DEL DAO ---
        Almacen3DAO dao = new Almacen3DAO();
        java.util.List<ProductoTerminado> datos = dao.getProductosTerminados();

        listaTerminados.addAll(datos);

        // Calcular estadísticas en memoria (UI Logic)
        for (ProductoTerminado p : datos) {
            valorTotal += p.getStock() * p.getPrecioVenta();
            if (p.getStock() <= p.getStockMinimo()) bajoStock++;
        }

        totalTerminadosLabel.setText(String.valueOf(listaTerminados.size()));
        stockBajoLabel.setText(String.valueOf(bajoStock));
        valorEstimadoLabel.setText(String.format("$%.2f", valorTotal));
    }

    private void setupFiltering() {
        // (Tu código original...)
        filteredTerminados = new FilteredList<>(listaTerminados, p -> true);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredTerminados.setPredicate(p -> {
                if (newVal == null || newVal.isEmpty()) return true;
                return p.getNombre().toLowerCase().contains(newVal.toLowerCase());
            });
        });
        tablaTerminados.setItems(filteredTerminados);
    }

    private void mostrarDialogoProduccion() {
        // (Mantén esto igual, solo llama al método procesar...)
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Producción de Producto Final");
        dialog.setHeaderText("Seleccione una receta para producir");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane(); grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(20));
        ComboBox<RecetaSimple> cmbReceta = new ComboBox<>();
        cargarRecetasFinales(cmbReceta); // Este método auxiliar se queda aquí porque es solo para llenar el combo
        TextField txtMultiplicador = new TextField("1");
        Label lblInfo = new Label("Producción estimada: -");
        cmbReceta.setOnAction(e -> actualizarInfoProduccion(lblInfo, cmbReceta.getValue(), txtMultiplicador.getText()));
        txtMultiplicador.textProperty().addListener((o, old, val) -> actualizarInfoProduccion(lblInfo, cmbReceta.getValue(), val));
        TextField txtPrecio = new TextField(); txtPrecio.setPromptText("Precio Venta Sugerido ($)");
        DatePicker dateVenc = new DatePicker(LocalDate.now().plusMonths(6));

        grid.add(new Label("Receta (Alm3):"), 0, 0); grid.add(cmbReceta, 1, 0);
        grid.add(new Label("Multiplicador:"), 0, 1); grid.add(txtMultiplicador, 1, 1);
        grid.add(lblInfo, 1, 2);
        grid.add(new Separator(), 0, 3, 2, 1);
        grid.add(new Label("Precio Venta Unit.:"), 0, 4); grid.add(txtPrecio, 1, 4);
        grid.add(new Label("Fecha Vencimiento:"), 0, 5); grid.add(dateVenc, 1, 5);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    RecetaSimple receta = cmbReceta.getValue();
                    if (receta == null) return false;
                    double multi = Double.parseDouble(txtMultiplicador.getText());
                    if (multi <= 0) return false;
                    double precio = 0;
                    try { precio = Double.parseDouble(txtPrecio.getText()); } catch(Exception ex) {}
                    return procesarProduccionFinal(receta, multi, precio, dateVenc.getValue());
                } catch (Exception e) {
                    mostrarAlerta("Error", "Datos inválidos: " + e.getMessage());
                }
            }
            return false;
        });
        dialog.showAndWait();
    }

    private void actualizarInfoProduccion(Label lbl, RecetaSimple r, String multiStr) {
        if (r == null) { lbl.setText("-"); return; }
        try {
            double m = Double.parseDouble(multiStr);
            double total = r.getCantidadBase() * m;
            lbl.setText("Total a crear: " + total + " " + r.getUnidad());
        } catch (Exception e) { lbl.setText("Error en número"); }
    }

    // =========================================================================
    //  AQUÍ ESTÁ EL CAMBIO CLAVE: REEMPLAZO DE LA LÓGICA POR EL SERVICIO
    // =========================================================================

    private boolean procesarProduccionFinal(RecetaSimple receta, double multiplicador, double precioVenta, LocalDate vencimiento) {
        // Creamos la instancia del servicio
        ProduccionService servicio = new ProduccionService();

        // Llamamos al servicio para que haga el trabajo sucio
        boolean exito = servicio.procesarProduccionFinal(receta, multiplicador, precioVenta, vencimiento);

        if (exito) {
            mostrarAlerta("Éxito", "Producción finalizada. Inventario actualizado.");
            cargarDatos(); // Refrescamos la tabla UI
            return true;
        } else {
            mostrarAlerta("Error", "No se pudo realizar la producción. Revise stock o logs.");
            return false;
        }
    }

    // --- Los métodos consumirDeAlmacen1 y consumirDeAlmacen2 HAN SIDO ELIMINADOS DE AQUÍ ---
    // (Ahora viven felices dentro de ProduccionService.java)

    // =========================================================================

    private void cargarRecetasFinales(ComboBox<RecetaSimple> cb) {
        Almacen3DAO dao = new Almacen3DAO();
        cb.getItems().setAll(dao.getRecetasFinales());

        cb.setConverter(new StringConverter<>() {
            @Override public String toString(RecetaSimple r){return r!=null ? r.getNombre() : null;}
            @Override public RecetaSimple fromString(String s){return null;}
        });
    }

    private void mostrarDialogoLotes(ProductoTerminado p) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Lotes: " + p.getNombre());
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        TableView<LoteTerminado> table = new TableView<>();
        // ... (configuración de columnas igual que antes) ...
        TableColumn<LoteTerminado, Integer> cId = new TableColumn<>("ID"); cId.setCellValueFactory(new PropertyValueFactory<>("idLote"));
        TableColumn<LoteTerminado, Integer> cCant = new TableColumn<>("Cant."); cCant.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        TableColumn<LoteTerminado, String> cVenc = new TableColumn<>("Vence"); cVenc.setCellValueFactory(new PropertyValueFactory<>("vencimiento"));
        table.getColumns().addAll(cId, cCant, cVenc);

        // --- USO DEL DAO ---
        Almacen3DAO dao = new Almacen3DAO();
        table.getItems().setAll(dao.getLotesPorProducto(p.getId()));

        table.setPrefSize(400, 250);
        dialog.getDialogPane().setContent(table);
        dialog.showAndWait();
    }

    private void mostrarAlerta(String t, String m) {
        Alert a=new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(t); a.setHeaderText(null); a.setContentText(m);
        a.showAndWait();
    }
}