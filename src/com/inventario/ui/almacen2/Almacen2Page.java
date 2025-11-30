package com.inventario.ui.almacen2;

import com.inventario.config.ConexionBD;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.StringConverter;

import java.sql.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class Almacen2Page extends BorderPane {

    // (CSS_STYLES ... sin cambios)
    private static final String CSS_STYLES = """
        .root { -fx-background-color: #FDF8F0; -fx-font-family: 'Segoe UI'; }
        .header-title { -fx-font-size: 2.2em; -fx-font-weight: bold; -fx-text-fill: #333333; }
        .header-description { -fx-font-size: 1.1em; -fx-text-fill: #555555; }
        .card { -fx-background-color: white; -fx-border-color: #E0E0E0; -fx-border-width: 1; -fx-border-radius: 8; -fx-padding: 20px; }
        .card-title { -fx-font-size: 1.4em; -fx-font-weight: bold; -fx-text-fill: #333333; }
        .button-primary { -fx-background-color: #4A90E2; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 1.1em; -fx-pref-height: 40px; -fx-cursor: hand; -fx-background-radius: 5; }
        .table-view .column-header { -fx-background-color: #F9FAFB; -fx-font-weight: bold; -fx-font-size: 1.05em; }
        .alert-box { -fx-background-color: #FEF2F2; -fx-border-color: #FEE2E2; -fx-border-width: 1; -fx-border-radius: 8; -fx-padding: 15px; }
        .alert-text { -fx-fill: #991B1B; -fx-font-weight: bold; }
        .search-field { -fx-font-size: 1.05em; -fx-pref-height: 40px; -fx-border-color: #CCCCCC; -fx-border-radius: 5; -fx-padding: 5 5 5 35px; }
        .stats-card { -fx-padding: 15px 20px; -fx-background-color: white; -fx-border-color: #E0E0E0; -fx-border-width: 1; -fx-border-radius: 8; }
        .stats-card-title { -fx-font-size: 0.95em; -fx-font-weight: 500; -fx-text-fill: #333333; }
        .stats-card-content { -fx-font-size: 1.8em; -fx-font-weight: bold; -fx-text-fill: #111827; }
        .stats-card-content-danger { -fx-text-fill: #DC2626; }
        .badge { -fx-padding: 4 10; -fx-background-radius: 12; -fx-font-size: 0.9em; -fx-font-weight: bold; -fx-text-fill: white; }
        .badge-stock-low { -fx-background-color: #EF4444; }
        .badge-stock-medium { -fx-background-color: #F97316; }
        .badge-stock-normal { -fx-background-color: #22C55E; }
        .cell-stock-low { -fx-text-fill: #DC2626; -fx-font-weight: bold; }
    """;

    private final VBox mainContent;
    private final HBox alertBoxContainer;
    private Label totalIntermediosLabel, stockBajoLabel, valorTotalLabel;
    private TableView<ProductoIntermedioAlmacen> tablaIntermedios;
    private TextField searchField;
    private final ObservableList<ProductoIntermedioAlmacen> todosLosIntermedios = FXCollections.observableArrayList();
    private FilteredList<ProductoIntermedioAlmacen> filteredIntermedios;
    private int lowStockCount = 0;
    private final Consumer<String> onNavigate;

    public Almacen2Page(Consumer<String> onNavigate) {
        this.onNavigate = onNavigate;
        this.getStylesheets().add("data:text/css," + CSS_STYLES.replace("\n", ""));
        this.getStyleClass().add("root");
        setPadding(new Insets(30, 40, 30, 40));

        mainContent = new VBox(20);
        tablaIntermedios = new TableView<>();
        searchField = new TextField();
        totalIntermediosLabel = new Label("0");
        stockBajoLabel = new Label("0");
        valorTotalLabel = new Label("$0.00");
        alertBoxContainer = new HBox();

        mainContent.getChildren().addAll(crearHeader(), alertBoxContainer, crearActionsBar(), crearStatsGrid(), crearTablaIntermedios());
        setCenter(mainContent);

        cargarDatos();
        setupFiltering();
        actualizarUIConDatos();
    }

    private Node crearHeader() {
        VBox headerBox = new VBox(5);
        Label header = new Label("Almacén 2 - Intermedios"); header.getStyleClass().add("header-title");
        Label description = new Label("Gestión de productos semi-elaborados (bases, masas, etc.)"); description.getStyleClass().add("header-description");
        headerBox.getChildren().addAll(header, description);
        return headerBox;
    }

    private HBox crearAlertBox(int count) {
        HBox alertBox = new HBox(15); alertBox.getStyleClass().add("alert-box"); alertBox.setAlignment(Pos.CENTER_LEFT);
        Text icon = new Text("⚠️"); icon.setFont(Font.font(20)); icon.setFill(Color.web("#DC2626"));
        Label alertText = new Label("¡Atención! " + count + " producto(s) requieren reabastecimiento."); alertText.getStyleClass().add("alert-text");
        alertBox.getChildren().addAll(icon, alertText);
        return alertBox;
    }

    private Node crearActionsBar() {
        HBox actionsBar = new HBox(15); actionsBar.setAlignment(Pos.CENTER_LEFT);
        StackPane searchStack = new StackPane(); searchStack.getStyleClass().add("search-field-stack");
        searchField.setPromptText("Buscar productos intermedios..."); searchField.getStyleClass().add("search-field");
        Text searchIcon = new Text("🔍"); searchIcon.setFill(Color.web("#9CA3AF"));
        StackPane.setAlignment(searchIcon, Pos.CENTER_LEFT); StackPane.setMargin(searchIcon, new Insets(0, 0, 0, 12));
        searchStack.getChildren().addAll(searchField, searchIcon);

        Button usarRecetaBtn = new Button("🍳 Usar Receta");
        usarRecetaBtn.getStyleClass().add("button-primary");
        usarRecetaBtn.setOnAction(e -> onUsarRecetaClicked());

        actionsBar.getChildren().addAll(searchStack, usarRecetaBtn);
        HBox.setHgrow(searchStack, Priority.ALWAYS);
        return actionsBar;
    }

    private Node crearStatsGrid() {
        GridPane grid = new GridPane(); grid.getStyleClass().add("stats-grid"); grid.setHgap(20); grid.setVgap(20);
        ColumnConstraints col = new ColumnConstraints(); col.setPercentWidth(33.33);
        grid.getColumnConstraints().addAll(col, col, col);
        grid.add(createStatCard("Total Intermedios", totalIntermediosLabel, null), 0, 0);
        grid.add(createStatCard("Valor Costo Total", valorTotalLabel, null), 1, 0);
        grid.add(createStatCard("Stock Bajo", stockBajoLabel, "stats-card-content-danger"), 2, 0);
        return grid;
    }

    private Node createStatCard(String title, Label contentLabel, String contentStyleClass) {
        VBox card = new VBox(5); card.getStyleClass().add("stats-card");
        Label titleLabel = new Label(title); titleLabel.getStyleClass().add("stats-card-title");
        contentLabel.getStyleClass().add("stats-card-content");
        if (contentStyleClass != null) contentLabel.getStyleClass().add(contentStyleClass);
        card.getChildren().addAll(titleLabel, contentLabel);
        return card;
    }

    private Node crearTablaIntermedios() {
        VBox card = new VBox(15); card.getStyleClass().add("card");
        Label cardTitle = new Label("Inventario de Intermedios"); cardTitle.getStyleClass().add("card-title");
        Label cardDescription = new Label("Stock de productos semi-elaborados"); cardDescription.getStyleClass().add("card-description");
        configurarTabla();
        card.getChildren().addAll(new VBox(5, cardTitle, cardDescription), tablaIntermedios);
        VBox.setVgrow(tablaIntermedios, Priority.ALWAYS);
        return card;
    }

    private void configurarTabla() {
        tablaIntermedios.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        TableColumn<ProductoIntermedioAlmacen, String> col1 = new TableColumn<>("Producto"); col1.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        TableColumn<ProductoIntermedioAlmacen, Integer> col2 = new TableColumn<>("Cantidad"); col2.setCellValueFactory(new PropertyValueFactory<>("stock"));
        col2.setCellFactory(c -> new TableCell<>(){ @Override protected void updateItem(Integer i, boolean e){super.updateItem(i,e); if(!e && i!=null){setText(i.toString()); if(getTableView().getItems().get(getIndex()).esStockBajo()) getStyleClass().add("cell-stock-low"); else getStyleClass().remove("cell-stock-low");}}});
        TableColumn<ProductoIntermedioAlmacen, String> col3 = new TableColumn<>("Unidad"); col3.setCellValueFactory(new PropertyValueFactory<>("unidad"));
        TableColumn<ProductoIntermedioAlmacen, Double> col4 = new TableColumn<>("Costo Unit."); col4.setCellValueFactory(new PropertyValueFactory<>("costoUnitario"));
        col4.setCellFactory(c -> new TableCell<>(){ @Override protected void updateItem(Double p, boolean e){super.updateItem(p,e); setText(!e && p!=null ? String.format("$%.2f", p) : null);}});
        TableColumn<ProductoIntermedioAlmacen, Integer> col5 = new TableColumn<>("Stock Mín."); col5.setCellValueFactory(new PropertyValueFactory<>("stockMinimo"));
        TableColumn<ProductoIntermedioAlmacen, String> col6 = new TableColumn<>("Estado"); col6.setCellValueFactory(new PropertyValueFactory<>("estado"));
        col6.setCellFactory(c -> new TableCell<>(){ Label b=new Label(); @Override protected void updateItem(String s, boolean e){super.updateItem(s,e); if(!e && s!=null){b.setText(s); b.getStyleClass().clear(); b.getStyleClass().add("badge"); b.getStyleClass().add(s.equals("Bajo Stock")?"badge-stock-low":"badge-stock-normal"); setGraphic(b); setAlignment(Pos.CENTER_LEFT);} else setGraphic(null);}});

        tablaIntermedios.getColumns().setAll(col1, col2, col3, col4, col5, col6);
        tablaIntermedios.setPlaceholder(new Label("No se encontraron productos intermedios."));
    }

    // --- DATOS ---
    private void cargarDatos() {
        todosLosIntermedios.clear(); lowStockCount = 0; double valorTotal = 0;
        Map<Integer, Double> stockAgregado = new HashMap<>();
        try (Connection conn = ConexionBD.getConnection(); Statement stmt = conn.createStatement()) {
            try(ResultSet rs = stmt.executeQuery("SELECT IdProductoIntermedio, SUM(CantidadActual) FROM lotes_intermedios GROUP BY IdProductoIntermedio")) {
                while(rs.next()) stockAgregado.put(rs.getInt(1), rs.getDouble(2));
            }
            try(ResultSet rs = stmt.executeQuery("SELECT * FROM productos_intermedios")) {
                while(rs.next()) {
                    int id = rs.getInt("IdProductoIntermedio");
                    double stock = stockAgregado.getOrDefault(id, 0.0);
                    double costo = rs.getDouble("CostoUnitario");
                    ProductoIntermedioAlmacen p = new ProductoIntermedioAlmacen(id, rs.getString("Nombre"), (int)stock, rs.getInt("Stock_Minimo"), rs.getString("Unidad_de_medida"), costo);
                    todosLosIntermedios.add(p);
                    valorTotal += stock * costo;
                    if (p.esStockBajo()) lowStockCount++;
                }
            }
        } catch (Exception e) { e.printStackTrace(); mostrarAlerta("Error", "Error cargando datos."); }
        totalIntermediosLabel.setText(String.valueOf(todosLosIntermedios.size()));
        stockBajoLabel.setText(String.valueOf(lowStockCount));
        valorTotalLabel.setText(String.format("$%.2f", valorTotal));
    }

    private void setupFiltering() {
        filteredIntermedios = new FilteredList<>(todosLosIntermedios, p -> true);
        searchField.textProperty().addListener((obs, old, val) -> filteredIntermedios.setPredicate(p -> (val == null || val.isEmpty()) || p.getNombre().toLowerCase().contains(val.toLowerCase())));
        tablaIntermedios.setItems(filteredIntermedios);
    }
    private void actualizarUIConDatos() { alertBoxContainer.getChildren().clear(); if (lowStockCount > 0) alertBoxContainer.getChildren().add(crearAlertBox(lowStockCount)); }

    // --- DIÁLOGO USAR RECETA ---
    private void onUsarRecetaClicked() {
        Dialog<RecetaProduccionResult> dialog = new Dialog<>();
        dialog.setTitle("Usar Receta"); dialog.setHeaderText("Producir un nuevo lote.");
        ButtonType ok = new ButtonType("Producir", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);

        GridPane grid = new GridPane(); grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(20, 150, 10, 10));
        ComboBox<RecetaProduccionUI> cmbReceta = new ComboBox<>();
        cargarRecetasParaDialog(cmbReceta);
        TextField txtCant = new TextField("1");
        DatePicker picker = new DatePicker(LocalDate.now().plusDays(7));
        CheckBox check = new CheckBox("No aplica vencimiento");
        picker.disableProperty().bind(check.selectedProperty());

        grid.add(new Label("Receta:"), 0, 0); grid.add(cmbReceta, 1, 0);
        grid.add(new Label("Lotes:"), 0, 1); grid.add(txtCant, 1, 1);
        grid.add(new Label("Vencimiento:"), 0, 2); grid.add(picker, 1, 2); grid.add(check, 1, 3);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == ok) {
                try {
                    int cant = Integer.parseInt(txtCant.getText());
                    if(cmbReceta.getValue() == null || cant <= 0) return null;
                    LocalDate fecha = check.isSelected() ? null : picker.getValue();
                    if(!check.isSelected() && fecha == null) return null;
                    return new RecetaProduccionResult(cmbReceta.getValue(), cant, fecha);
                } catch(Exception e) { return null; }
            } return null;
        });

        Optional<RecetaProduccionResult> res = dialog.showAndWait();
        res.ifPresent(r -> procesarProduccion(r.getReceta(), r.getCantidad(), r.getFechaVencimiento()));
    }

    private void cargarRecetasParaDialog(ComboBox<RecetaProduccionUI> combo) {
        // --- CORRECCIÓN: Quitamos el filtro WHERE IS NOT NULL para que salgan todas ---
        String sql = "SELECT id, nombre, cantidad_producida, unidad_producida, IdProductoIntermedio FROM recetas";
        ObservableList<RecetaProduccionUI> lista = FXCollections.observableArrayList();
        try (Connection c = ConexionBD.getConnection(); ResultSet rs = c.createStatement().executeQuery(sql)) {
            while(rs.next()) lista.add(new RecetaProduccionUI(rs.getInt(1), rs.getString(2), rs.getDouble(3), rs.getString(4), rs.getInt(5)));
            combo.setItems(lista);
            combo.setConverter(new StringConverter<>() {
                @Override public String toString(RecetaProduccionUI r) { return r!=null?r.getNombre():null; }
                @Override public RecetaProduccionUI fromString(String s) { return null; }
            });
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void procesarProduccion(RecetaProduccionUI receta, int lotesAProducir, LocalDate fechaVencimiento) {
        Connection conn = null;
        try {
            conn = ConexionBD.getConnection();
            conn.setAutoCommit(false);

            // 1. ¿Qué ingredientes necesito? (AHORA CON CONVERSIÓN)
            // Obtenemos la unidad que pide la receta (i.unidad) y la que tiene el stock (p.Unidad_de_medida)
            String sqlIngredientes = "SELECT i.IdProducto, i.cantidad, i.unidad as UnidadReceta, p.Unidad_de_medida as UnidadStock " +
                    "FROM ingredientes i " +
                    "JOIN producto p ON i.IdProducto = p.IdProducto " +
                    "WHERE i.receta_id = ?";

            HashMap<Integer, Double> requisitos = new HashMap<>();

            try (PreparedStatement stmt = conn.prepareStatement(sqlIngredientes)) {
                stmt.setInt(1, receta.getId());
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    int idProd = rs.getInt("IdProducto");
                    double cantReceta = rs.getDouble("cantidad");
                    String unidadReceta = rs.getString("UnidadReceta");
                    String unidadStock = rs.getString("UnidadStock");

                    // ¡MAGIA! Convertimos lo que pide la receta a la unidad que usa el almacén
                    double cantidadRealNecesaria = com.inventario.util.ConversorUnidades.convertir(cantReceta, unidadReceta, unidadStock);

                    // Multiplicamos por los lotes a producir
                    requisitos.put(idProd, cantidadRealNecesaria * lotesAProducir);
                }
            }

            // 2. CONSUMIR INGREDIENTES (Lógica FEFO con cantidades convertidas)
            for (Integer idProd : requisitos.keySet()) {
                double cantidadNecesaria = requisitos.get(idProd); // Esta cantidad ya está en la unidad correcta (ej. ml)

                // Buscar lotes ordenados por fecha de vencimiento
                String sqlLotes = "SELECT IdLote, CantidadActual FROM lotes WHERE IdProducto = ? AND CantidadActual > 0 ORDER BY FechaVencimiento ASC";

                try (PreparedStatement stmtLotes = conn.prepareStatement(sqlLotes)) {
                    stmtLotes.setInt(1, idProd);
                    ResultSet rsLotes = stmtLotes.executeQuery();

                    while (rsLotes.next() && cantidadNecesaria > 0) {
                        int idLote = rsLotes.getInt("IdLote");
                        double cantidadLote = rsLotes.getDouble("CantidadActual");

                        // Aquí la resta es segura porque ambas cantidades están en la misma unidad
                        double aConsumir = Math.min(cantidadLote, cantidadNecesaria);

                        // Restar del lote específico
                        String sqlUpdateLote;
                        if (aConsumir >= cantidadLote) {
                            sqlUpdateLote = "DELETE FROM lotes WHERE IdLote = " + idLote;
                        } else {
                            sqlUpdateLote = "UPDATE lotes SET CantidadActual = CantidadActual - " + aConsumir + " WHERE IdLote = " + idLote;
                        }
                        conn.createStatement().executeUpdate(sqlUpdateLote);

                        // Restar del Stock Total (Producto)
                        String sqlUpdateStock = "UPDATE producto SET Stock = Stock - ? WHERE IdProducto = ?";
                        try(PreparedStatement ps = conn.prepareStatement(sqlUpdateStock)) {
                            ps.setDouble(1, aConsumir);
                            ps.setInt(2, idProd);
                            ps.executeUpdate();
                        }

                        cantidadNecesaria -= aConsumir;
                    }
                }

                if (cantidadNecesaria > 0.001) { // Usamos un margen pequeño por errores de punto flotante
                    throw new SQLException("Stock insuficiente para el ingrediente ID: " + idProd + ". Faltan: " + String.format("%.2f", cantidadNecesaria));
                }
            }

            // 3. CREAR PRODUCTO INTERMEDIO
            int idIntermedio = receta.getIdProductoIntermedio();
            if (idIntermedio == 0) {
                try(PreparedStatement ps = conn.prepareStatement("SELECT IdProductoIntermedio FROM productos_intermedios WHERE Nombre = ?")) {
                    ps.setString(1, receta.getNombre()); ResultSet rs = ps.executeQuery();
                    if(rs.next()) idIntermedio = rs.getInt(1);
                    else {
                        try(PreparedStatement psIn = conn.prepareStatement("INSERT INTO productos_intermedios (Nombre, Unidad_de_medida) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                            psIn.setString(1, receta.getNombre()); psIn.setString(2, receta.getUnidad()); psIn.executeUpdate();
                            ResultSet rsk = psIn.getGeneratedKeys(); if(rsk.next()) idIntermedio = rsk.getInt(1);
                        }
                    }
                }
            }

            double cantFinal = receta.getCantidadBase() * lotesAProducir;
            try(PreparedStatement ps = conn.prepareStatement("INSERT INTO lotes_intermedios (IdProductoIntermedio, CantidadActual, FechaVencimiento, FechaIngreso) VALUES (?, ?, ?, ?)")) {
                ps.setInt(1, idIntermedio); ps.setDouble(2, cantFinal);
                if(fechaVencimiento!=null) ps.setString(3, fechaVencimiento.toString()); else ps.setNull(3, Types.VARCHAR);
                ps.setString(4, LocalDate.now().toString());
                ps.executeUpdate();
            }

            conn.commit();
            mostrarAlerta("Producción Exitosa", "Se han consumido los ingredientes y creado el producto.");
            cargarDatos();
            actualizarUIConDatos();

        } catch (Exception e) {
            if (conn != null) try { conn.rollback(); } catch (Exception ex) {}
            mostrarAlerta("Error", e.getMessage());
            e.printStackTrace();
        } finally {
            if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (Exception ex) {}
        }
    }
    private void mostrarAlerta(String t, String m) { Alert a=new Alert(Alert.AlertType.INFORMATION); if(t.startsWith("Error")) a.setAlertType(Alert.AlertType.ERROR); a.setTitle(t); a.setContentText(m); a.showAndWait(); }

    // CLASES INTERNAS
    private static class RecetaProduccionResult {
        private final RecetaProduccionUI receta; private final int cantidad; private final LocalDate fecha;
        public RecetaProduccionResult(RecetaProduccionUI r, int c, LocalDate f) { receta=r; cantidad=c; fecha=f; }
        public RecetaProduccionUI getReceta() { return receta; } public int getCantidad() { return cantidad; } public LocalDate getFechaVencimiento() { return fecha; }
    }

}