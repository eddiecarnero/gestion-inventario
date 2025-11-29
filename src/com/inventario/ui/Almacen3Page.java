package com.inventario.ui;

import com.inventario.config.ConexionBD;
import com.inventario.model.LoteTerminado;
import com.inventario.model.ProductoTerminado;
import com.inventario.model.RecetaSimple;
import com.inventario.util.ConversorUnidades;
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
    private final TableView<ProductoTerminado> tablaTerminados;
    private final TextField searchField;
    private final Label totalTerminadosLabel;
    private final Label stockBajoLabel;
    private final Label valorEstimadoLabel;
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
        Label header = new Label("Almacén 3 - Productos Terminados");
        header.getStyleClass().add("header-title");
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
        ColumnConstraints col = new ColumnConstraints();
        col.setPercentWidth(33.33);
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
        Label tableTitle = new Label("Inventario Disponible");
        tableTitle.getStyleClass().add("card-title");

        tablaTerminados = new TableView<>();
        configurarTabla();

        tableCard.getChildren().addAll(tableTitle, tablaTerminados);
        VBox.setVgrow(tablaTerminados, Priority.ALWAYS);

        mainContent.getChildren().addAll(headerBox, actionsBar, statsGrid, tableCard);
        setCenter(mainContent);

        cargarDatos();
        setupFiltering();
    }

    private VBox createStatCard(String title, Label value, String style) {
        VBox card = new VBox(5);
        card.getStyleClass().add("stats-card");
        Label lblTitle = new Label(title);
        lblTitle.getStyleClass().add("stats-card-title");
        value.getStyleClass().add("stats-card-content");
        if (style != null) value.getStyleClass().add(style);
        card.getChildren().addAll(lblTitle, value);
        return card;
    }

    private void configurarTabla() {
        tablaTerminados.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<ProductoTerminado, String> cNombre = new TableColumn<>("Producto");
        cNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));

        TableColumn<ProductoTerminado, Integer> cStock = new TableColumn<>("Stock Total");
        cStock.setCellValueFactory(new PropertyValueFactory<>("stock"));

        TableColumn<ProductoTerminado, Double> cPrecio = new TableColumn<>("Precio Venta");
        cPrecio.setCellValueFactory(new PropertyValueFactory<>("precioVenta"));
        cPrecio.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : String.format("$%.2f", item));
            }
        });

        TableColumn<ProductoTerminado, String> cEstado = new TableColumn<>("Estado");
        cEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        cEstado.setCellFactory(c -> new TableCell<>() {
            final Label badge = new Label();

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    return;
                }
                badge.setText(item);
                badge.getStyleClass().clear();
                badge.getStyleClass().add("badge");
                badge.getStyleClass().add(item.equals("Bajo Stock") ? "badge-stock-low" : "badge-stock-normal");
                setGraphic(badge);
                setAlignment(Pos.CENTER_LEFT);
            }
        });

        // Columna de Acción: Ver Lotes
        TableColumn<ProductoTerminado, Void> cAccion = new TableColumn<>("Detalle");
        cAccion.setCellFactory(param -> new TableCell<>() {
            private final Button btnVer = new Button("👁 Lotes");

            {
                btnVer.setStyle("-fx-background-color: #4A90E2; -fx-text-fill: white; -fx-cursor: hand; -fx-font-size: 0.85em;");
                btnVer.setOnAction(e -> mostrarDialogoLotes(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btnVer);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        tablaTerminados.getColumns().addAll(cNombre, cStock, cPrecio, cEstado, cAccion);
    }

    private void cargarDatos() {
        listaTerminados.clear();
        int bajoStock = 0;
        double valorTotal = 0;

        String sql = "SELECT p.IdProductoTerminado, p.Nombre, p.PrecioVenta, p.StockMinimo, " + "COALESCE(SUM(l.CantidadActual), 0) as StockTotal " + "FROM productos_terminados p " + "LEFT JOIN lotes_terminados l ON p.IdProductoTerminado = l.IdProductoTerminado " + "GROUP BY p.IdProductoTerminado";

        try (Connection conn = ConexionBD.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                ProductoTerminado p = new ProductoTerminado(rs.getInt("IdProductoTerminado"), rs.getString("Nombre"), rs.getDouble("PrecioVenta"), rs.getInt("StockMinimo"), rs.getInt("StockTotal"));
                listaTerminados.add(p);
                valorTotal += p.getStock() * p.getPrecioVenta();
                if (p.getStock() <= p.getStockMinimo()) bajoStock++;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        totalTerminadosLabel.setText(String.valueOf(listaTerminados.size()));
        stockBajoLabel.setText(String.valueOf(bajoStock));
        valorEstimadoLabel.setText(String.format("$%.2f", valorTotal));
    }

    private void setupFiltering() {
        filteredTerminados = new FilteredList<>(listaTerminados, p -> true);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredTerminados.setPredicate(p -> {
                if (newVal == null || newVal.isEmpty()) return true;
                return p.getNombre().toLowerCase().contains(newVal.toLowerCase());
            });
        });
        tablaTerminados.setItems(filteredTerminados);
    }

    // --- NUEVO SISTEMA DE PRODUCCIÓN (BASADO EN RECETAS FINAL) ---

    private void mostrarDialogoProduccion() {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Producción de Producto Final");
        dialog.setHeaderText("Seleccione una receta para producir");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        // 1. Seleccionar Receta (Solo las de tipo FINAL)
        ComboBox<RecetaSimple> cmbReceta = new ComboBox<>();
        cargarRecetasFinales(cmbReceta);

        TextField txtMultiplicador = new TextField("1");
        txtMultiplicador.setPromptText("Veces a producir");

        Label lblInfo = new Label("Producción estimada: -");

        // Listeners para actualizar info
        cmbReceta.setOnAction(e -> actualizarInfoProduccion(lblInfo, cmbReceta.getValue(), txtMultiplicador.getText()));
        txtMultiplicador.textProperty().addListener((o, old, val) -> actualizarInfoProduccion(lblInfo, cmbReceta.getValue(), val));

        TextField txtPrecio = new TextField();
        txtPrecio.setPromptText("Precio Venta Sugerido ($)");
        DatePicker dateVenc = new DatePicker(LocalDate.now().plusMonths(6)); // Defecto 6 meses

        grid.add(new Label("Receta (Alm3):"), 0, 0);
        grid.add(cmbReceta, 1, 0);
        grid.add(new Label("Multiplicador:"), 0, 1);
        grid.add(txtMultiplicador, 1, 1);
        grid.add(lblInfo, 1, 2);

        grid.add(new Separator(), 0, 3, 2, 1);

        grid.add(new Label("Precio Venta Unit.:"), 0, 4);
        grid.add(txtPrecio, 1, 4);
        grid.add(new Label("Fecha Vencimiento:"), 0, 5);
        grid.add(dateVenc, 1, 5);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    RecetaSimple receta = cmbReceta.getValue();
                    if (receta == null) return false;

                    double multi = Double.parseDouble(txtMultiplicador.getText());
                    if (multi <= 0) return false;

                    double precio = 0;
                    try {
                        precio = Double.parseDouble(txtPrecio.getText());
                    } catch (Exception ex) {
                    } // Opcional

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
        if (r == null) {
            lbl.setText("-");
            return;
        }
        try {
            double m = Double.parseDouble(multiStr);
            double total = r.cantidadBase() * m;
            lbl.setText("Total a crear: " + total + " " + r.unidad());
        } catch (Exception e) {
            lbl.setText("Error en número");
        }
    }

    private boolean procesarProduccionFinal(RecetaSimple receta, double multiplicador, double precioVenta, LocalDate vencimiento) {
        Connection conn = null;
        try {
            conn = ConexionBD.getConnection();
            conn.setAutoCommit(false);

            // 1. LEER INGREDIENTES DE LA RECETA
            String sqlIng = "SELECT IdProducto, IdIntermedio, cantidad, unidad, tipo_origen FROM ingredientes WHERE receta_id = ?";

            try (PreparedStatement ps = conn.prepareStatement(sqlIng)) {
                ps.setInt(1, receta.id());
                ResultSet rs = ps.executeQuery();

                while (rs.next()) {
                    String tipoOrigen = rs.getString("tipo_origen"); // INSUMO o INTERMEDIO
                    double cantReceta = rs.getDouble("cantidad");
                    String unidadReceta = rs.getString("unidad");
                    double cantidadNecesaria = cantReceta * multiplicador;

                    if ("INSUMO".equalsIgnoreCase(tipoOrigen)) {
                        int idProducto = rs.getInt("IdProducto");
                        consumirDeAlmacen1(conn, idProducto, cantidadNecesaria, unidadReceta);

                    } else if ("INTERMEDIO".equalsIgnoreCase(tipoOrigen)) {
                        int idIntermedio = rs.getInt("IdIntermedio");
                        consumirDeAlmacen2(conn, idIntermedio, cantidadNecesaria, unidadReceta);
                    }
                }
            }

            // 2. CREAR O ACTUALIZAR PRODUCTO TERMINADO
            int idTerminado;
            try (PreparedStatement ps = conn.prepareStatement("SELECT IdProductoTerminado FROM productos_terminados WHERE Nombre = ?")) {
                ps.setString(1, receta.nombre());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    idTerminado = rs.getInt(1);
                    if (precioVenta > 0) { // Actualizar precio si se especificó
                        try (PreparedStatement psUpdate = conn.prepareStatement("UPDATE productos_terminados SET PrecioVenta=? WHERE IdProductoTerminado=?")) {
                            psUpdate.setDouble(1, precioVenta);
                            psUpdate.setInt(2, idTerminado);
                            psUpdate.executeUpdate();
                        }
                    }
                } else {
                    try (PreparedStatement psIns = conn.prepareStatement("INSERT INTO productos_terminados (Nombre, PrecioVenta) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                        psIns.setString(1, receta.nombre());
                        psIns.setDouble(2, precioVenta);
                        psIns.executeUpdate();
                        ResultSet gk = psIns.getGeneratedKeys();
                        if (gk.next()) idTerminado = gk.getInt(1);
                        else throw new SQLException("No se pudo crear el producto terminado.");
                    }
                }
            }

            // 3. INSERTAR LOTE EN ALMACEN 3
            double cantidadFinal = receta.cantidadBase() * multiplicador;
            int cantFinalInt = (int) Math.ceil(cantidadFinal);

            String sqlInsLote = "INSERT INTO lotes_terminados (IdProductoTerminado, CantidadActual, FechaProduccion, FechaVencimiento) VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sqlInsLote)) {
                ps.setInt(1, idTerminado);
                ps.setInt(2, cantFinalInt);
                ps.setDate(3, Date.valueOf(LocalDate.now()));
                ps.setDate(4, Date.valueOf(vencimiento));
                ps.executeUpdate();
            }

            conn.commit();
            mostrarAlerta("Éxito", "Producción finalizada. Inventario actualizado.");
            cargarDatos();
            return true;

        } catch (Exception e) {
            if (conn != null) try {
                conn.rollback();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            mostrarAlerta("Error de Producción", e.getMessage());
            return false;
        } finally {
            if (conn != null) try {
                conn.setAutoCommit(true);
                conn.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void consumirDeAlmacen1(Connection conn, int idProducto, double cantidadNecesaria, String unidadReceta) throws SQLException {
        String unidadStock = "Unidad";
        try (PreparedStatement ps = conn.prepareStatement("SELECT Unidad_de_medida FROM producto WHERE IdProducto=?")) {
            ps.setInt(1, idProducto);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) unidadStock = rs.getString(1);
        }

        double cantidadReal = ConversorUnidades.convertir(cantidadNecesaria, unidadReceta, unidadStock);
        double faltante = cantidadReal;

        String sqlLotes = "SELECT IdLote, CantidadActual FROM lotes WHERE IdProducto = ? AND CantidadActual > 0 ORDER BY FechaVencimiento ASC";
        try (PreparedStatement ps = conn.prepareStatement(sqlLotes)) {
            ps.setInt(1, idProducto);
            ResultSet rs = ps.executeQuery();
            while (rs.next() && faltante > 0.001) {
                int idLote = rs.getInt("IdLote");
                double cantLote = rs.getDouble("CantidadActual");
                double aDescontar = Math.min(cantLote, faltante);

                if (Math.abs(cantLote - aDescontar) < 0.001) {
                    try (PreparedStatement psDel = conn.prepareStatement("DELETE FROM lotes WHERE IdLote=?")) {
                        psDel.setInt(1, idLote);
                        psDel.executeUpdate();
                    }
                } else {
                    try (PreparedStatement psUpdate = conn.prepareStatement("UPDATE lotes SET CantidadActual = CantidadActual - ? WHERE IdLote=?")) {
                        psUpdate.setDouble(1, aDescontar);
                        psUpdate.setInt(2, idLote);
                        psUpdate.executeUpdate();
                    }
                }

                try (PreparedStatement psUpdateProd = conn.prepareStatement("UPDATE producto SET Stock = Stock - ? WHERE IdProducto=?")) {
                    psUpdateProd.setDouble(1, aDescontar);
                    psUpdateProd.setInt(2, idProducto);
                    psUpdateProd.executeUpdate();
                }

                faltante -= aDescontar;
            }
        }
        if (faltante > 0.001) throw new SQLException("Falta stock en Almacén 1 (Insumo ID " + idProducto + ")");
    }

    private void consumirDeAlmacen2(Connection conn, int idIntermedio, double cantidadNecesaria, String unidadReceta) throws SQLException {
        String unidadStock = "Unidad";
        try (PreparedStatement ps = conn.prepareStatement("SELECT Unidad_de_medida FROM productos_intermedios WHERE IdProductoIntermedio=?")) {
            ps.setInt(1, idIntermedio);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) unidadStock = rs.getString(1);
        }

        double cantidadReal = ConversorUnidades.convertir(cantidadNecesaria, unidadReceta, unidadStock);
        double faltante = cantidadReal;

        String sqlLotes = "SELECT IdLote, CantidadActual FROM lotes_intermedios WHERE IdProductoIntermedio = ? AND CantidadActual > 0 ORDER BY FechaVencimiento ASC";
        try (PreparedStatement ps = conn.prepareStatement(sqlLotes)) {
            ps.setInt(1, idIntermedio);
            ResultSet rs = ps.executeQuery();
            while (rs.next() && faltante > 0.001) {
                int idLote = rs.getInt("IdLote");
                double cantLote = rs.getDouble("CantidadActual");
                double aDescontar = Math.min(cantLote, faltante);

                if (Math.abs(cantLote - aDescontar) < 0.001) {
                    try (PreparedStatement psDel = conn.prepareStatement("DELETE FROM lotes_intermedios WHERE IdLote=?")) {
                        psDel.setInt(1, idLote);
                        psDel.executeUpdate();
                    }
                } else {
                    try (PreparedStatement psUpdate = conn.prepareStatement("UPDATE lotes_intermedios SET CantidadActual = CantidadActual - ? WHERE IdLote=?")) {
                        psUpdate.setDouble(1, aDescontar);
                        psUpdate.setInt(2, idLote);
                        psUpdate.executeUpdate();
                    }
                }
                faltante -= aDescontar;
            }
        }
        if (faltante > 0.001) throw new SQLException("Falta stock en Almacén 2 (Intermedio ID " + idIntermedio + ")");
    }

    private void cargarRecetasFinales(ComboBox<RecetaSimple> cb) {
        ObservableList<RecetaSimple> lista = FXCollections.observableArrayList();
        String sql = "SELECT id, nombre, cantidad_producida, unidad_producida FROM recetas WHERE tipo_destino = 'FINAL'";
        try (Connection c = ConexionBD.getConnection(); Statement stmt = c.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new RecetaSimple(rs.getInt("id"), rs.getString("nombre"), rs.getDouble("cantidad_producida"), rs.getString("unidad_producida")));
            }
            cb.setItems(lista);
            cb.setConverter(new StringConverter<>() {
                @Override
                public String toString(RecetaSimple r) {
                    return r != null ? r.nombre() : null;
                }

                @Override
                public RecetaSimple fromString(String s) {
                    return null;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void mostrarDialogoLotes(ProductoTerminado p) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Lotes: " + p.getNombre());
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        TableView<LoteTerminado> table = new TableView<>();

        TableColumn<LoteTerminado, Integer> cId = new TableColumn<>("ID");
        cId.setCellValueFactory(new PropertyValueFactory<>("idLote"));
        TableColumn<LoteTerminado, Integer> cCant = new TableColumn<>("Cant.");
        cCant.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        TableColumn<LoteTerminado, String> cVenc = new TableColumn<>("Vence");
        cVenc.setCellValueFactory(new PropertyValueFactory<>("vencimiento"));

        table.getColumns().addAll(cId, cCant, cVenc);

        ObservableList<LoteTerminado> data = FXCollections.observableArrayList();
        try (Connection c = ConexionBD.getConnection(); PreparedStatement ps = c.prepareStatement("SELECT IdLoteTerminado, CantidadActual, FechaVencimiento FROM lotes_terminados WHERE IdProductoTerminado=? AND CantidadActual>0")) {
            ps.setInt(1, p.getId());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) data.add(new LoteTerminado(rs.getInt(1), rs.getInt(2), rs.getString(3)));
        } catch (SQLException e) {
            e.printStackTrace();
        }

        table.setItems(data);
        table.setPrefSize(400, 250);
        dialog.getDialogPane().setContent(table);
        dialog.showAndWait();
    }

    private void mostrarAlerta(String t, String m) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(t);
        a.setHeaderText(null);
        a.setContentText(m);
        a.showAndWait();
    }
}
