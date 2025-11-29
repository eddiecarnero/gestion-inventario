package com.inventario.ui;

import com.inventario.config.ConexionBD;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.sql.*;
import java.time.LocalDate;
import java.util.function.Consumer;

public class Almacen1Page extends BorderPane {

    private static final String CSS_STYLES = """
        .root { -fx-background-color: #FDF8F0; -fx-font-family: 'Segoe UI'; }
        .header-title { -fx-font-size: 2.2em; -fx-font-weight: bold; -fx-text-fill: #333333; }
        .header-description { -fx-font-size: 1.1em; -fx-text-fill: #555555; }
        .card { -fx-background-color: white; -fx-border-color: #E0E0E0; -fx-border-width: 1; -fx-border-radius: 8; -fx-padding: 20px; }
        .card-title { -fx-font-size: 1.4em; -fx-font-weight: bold; -fx-text-fill: #333333; }
        .stats-card-content { -fx-font-size: 1.8em; -fx-font-weight: bold; -fx-text-fill: #111827; }
        .stats-card-content-danger { -fx-text-fill: #DC2626; }
        .badge { -fx-padding: 4 10; -fx-background-radius: 12; -fx-font-size: 0.9em; -fx-font-weight: bold; -fx-text-fill: white; }
        .badge-stock-low { -fx-background-color: #EF4444; }
        .badge-stock-medium { -fx-background-color: #F97316; }
        .badge-stock-normal { -fx-background-color: #22C55E; }
        .cell-stock-low { -fx-text-fill: #DC2626; -fx-font-weight: bold; }
        .table-view .column-header { -fx-background-color: #F9FAFB; -fx-font-weight: bold; -fx-font-size: 1.05em; }
    """;

    private final Label totalInsumosLabel;
    private final Label stockBajoLabel;
    private final Label valorTotalLabel;
    private final Label totalProveedoresLabel;
    private final TableView<InsumoAlmacen> tablaInsumos;
    private final TextField searchField;
    private final ObservableList<InsumoAlmacen> todosLosInsumos = FXCollections.observableArrayList();
    private FilteredList<InsumoAlmacen> filteredInsumos;
    private int lowStockCount = 0;
    private final Consumer<String> onNavigate;

    public Almacen1Page(Consumer<String> onNavigate) {
        this.onNavigate = onNavigate;
        this.getStylesheets().add("data:text/css," + CSS_STYLES.replace("\n", ""));
        this.getStyleClass().add("root");
        setPadding(new Insets(30, 40, 30, 40));

        VBox mainContent = new VBox(20);

        // UI Init
        tablaInsumos = new TableView<>();
        searchField = new TextField();
        totalInsumosLabel = new Label("0");
        stockBajoLabel = new Label("0");
        valorTotalLabel = new Label("$0.00");
        totalProveedoresLabel = new Label("0");

        // Header
        VBox headerBox = new VBox(5);
        Label header = new Label("Almacén 1 - Insumos Básicos");
        header.getStyleClass().add("header-title");
        Label desc = new Label("Gestión de materias primas y stock de insumos");
        desc.getStyleClass().add("header-description");
        headerBox.getChildren().addAll(header, desc);

        // Actions
        HBox actionsBar = new HBox(15);
        actionsBar.setAlignment(Pos.CENTER_LEFT);
        searchField.setPromptText("Buscar insumos...");
        searchField.setPrefHeight(40);
        Button nuevaOrdenBtn = new Button("🛒 Nueva Orden de Compra");
        nuevaOrdenBtn.setStyle("-fx-background-color: #4A90E2; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 1.1em; -fx-pref-height: 40px; -fx-cursor: hand; -fx-background-radius: 5;");
        nuevaOrdenBtn.setOnAction(e -> onNavigate.accept("orden-compra"));
        actionsBar.getChildren().addAll(searchField, nuevaOrdenBtn);
        HBox.setHgrow(searchField, Priority.ALWAYS);

        // Stats
        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(20);
        ColumnConstraints col = new ColumnConstraints(); col.setPercentWidth(25);
        statsGrid.getColumnConstraints().addAll(col, col, col, col);
        statsGrid.add(createStatCard("Total Insumos", totalInsumosLabel, null), 0, 0);
        statsGrid.add(createStatCard("Valor Total", valorTotalLabel, null), 1, 0);
        statsGrid.add(createStatCard("Stock Bajo", stockBajoLabel, "stats-card-content-danger"), 2, 0);
        statsGrid.add(createStatCard("Proveedores", totalProveedoresLabel, null), 3, 0);

        // Table
        VBox tableCard = new VBox(15);
        tableCard.getStyleClass().add("card");
        Label tableTitle = new Label("Inventario de Insumos");
        tableTitle.getStyleClass().add("card-title");
        configurarTabla();
        tableCard.getChildren().addAll(tableTitle, tablaInsumos);
        VBox.setVgrow(tablaInsumos, Priority.ALWAYS);

        mainContent.getChildren().addAll(headerBox, actionsBar, statsGrid, tableCard);
        setCenter(mainContent);

        cargarDatos();
        setupFiltering();
    }

    private VBox createStatCard(String title, Label value, String style) {
        VBox card = new VBox(5);
        card.setStyle("-fx-background-color: white; -fx-border-color: #E0E0E0; -fx-border-radius: 8; -fx-padding: 15px;");
        Label lblTitle = new Label(title); lblTitle.setStyle("-fx-text-fill: #333; -fx-font-weight: 500;");
        value.getStyleClass().add("stats-card-content");
        if (style != null) value.getStyleClass().add(style);
        card.getChildren().addAll(lblTitle, value);
        return card;
    }

    private void configurarTabla() {
        tablaInsumos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<InsumoAlmacen, String> nombreCol = new TableColumn<>("Insumo");
        nombreCol.setCellValueFactory(new PropertyValueFactory<>("nombre"));

        TableColumn<InsumoAlmacen, Integer> stockCol = new TableColumn<>("Cantidad");
        stockCol.setCellValueFactory(new PropertyValueFactory<>("stock"));
        stockCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); return; }
                setText(item.toString());
                InsumoAlmacen row = getTableView().getItems().get(getIndex());
                if (row.esStockBajo()) setStyle("-fx-text-fill: #DC2626; -fx-font-weight: bold;");
                else setStyle("");
            }
        });

        TableColumn<InsumoAlmacen, String> unidadCol = new TableColumn<>("Unidad");
        unidadCol.setCellValueFactory(new PropertyValueFactory<>("unidad"));

        TableColumn<InsumoAlmacen, Double> precioCol = new TableColumn<>("Precio Unit.");
        precioCol.setCellValueFactory(new PropertyValueFactory<>("precioUnitario"));
        precioCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else setText(String.format("$%.2f", item));
            }
        });

        TableColumn<InsumoAlmacen, Integer> minCol = new TableColumn<>("Stock Mín.");
        minCol.setCellValueFactory(new PropertyValueFactory<>("stockMinimo"));

        TableColumn<InsumoAlmacen, String> provCol = new TableColumn<>("Proveedor");
        provCol.setCellValueFactory(new PropertyValueFactory<>("proveedorNombre"));

        TableColumn<InsumoAlmacen, String> estadoCol = new TableColumn<>("Estado");
        estadoCol.setCellValueFactory(new PropertyValueFactory<>("estado"));
        estadoCol.setCellFactory(col -> new TableCell<>() {
            final Label badge = new Label();
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                badge.setText(item);
                badge.getStyleClass().clear();
                badge.getStyleClass().add("badge");
                if (item.equals("Bajo Stock")) badge.getStyleClass().add("badge-stock-low");
                else if (item.equals("Medio")) badge.getStyleClass().add("badge-stock-medium");
                else badge.getStyleClass().add("badge-stock-normal");
                setGraphic(badge);
                setAlignment(Pos.CENTER_LEFT);
            }
        });

        TableColumn<InsumoAlmacen, Void> accionesCol = new TableColumn<>("Detalle");
        accionesCol.setCellFactory(param -> new TableCell<>() {
            private final Button btnVer = new Button("👁 Ver Lotes");

            {
                btnVer.setStyle("-fx-background-color: #4A90E2; -fx-text-fill: white; -fx-cursor: hand; -fx-font-size: 0.9em;");
                btnVer.setOnAction(event -> {
                    InsumoAlmacen insumo = getTableView().getItems().get(getIndex());
                    mostrarDialogoLotes(insumo);
                });
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

        // Asegúrate de agregar accionesCol a la lista de columnas
        tablaInsumos.getColumns().addAll(nombreCol, stockCol, unidadCol, precioCol, minCol, provCol, estadoCol, accionesCol);
        tablaInsumos.setPlaceholder(new Label("No hay insumos registrados."));
    }

    private void mostrarDialogoLotes(InsumoAlmacen insumo) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Lotes de: " + insumo.nombre());
        dialog.setHeaderText("Desglose de lotes y vencimientos");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        TableView<LoteInfo> tablaLotes = new TableView<>();
        tablaLotes.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<LoteInfo, Integer> colId = new TableColumn<>("ID Lote");
        colId.setCellValueFactory(new PropertyValueFactory<>("idLote"));

        TableColumn<LoteInfo, Double> colCant = new TableColumn<>("Cantidad");
        colCant.setCellValueFactory(new PropertyValueFactory<>("cantidad"));

        TableColumn<LoteInfo, String> colIngreso = new TableColumn<>("Ingreso");
        colIngreso.setCellValueFactory(new PropertyValueFactory<>("fechaIngreso"));

        TableColumn<LoteInfo, String> colVenc = new TableColumn<>("Vencimiento");
        colVenc.setCellValueFactory(new PropertyValueFactory<>("fechaVencimiento"));

        TableColumn<LoteInfo, String> colEstado = new TableColumn<>("Estado");
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        // Colorear filas según estado
        colEstado.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                if (item == null || empty) {
                    setStyle("");
                } else if (item.equals("VENCIDO")) {
                    setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                } else if (item.equals("Por Vencer")) {
                    setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                } else {
                    setStyle("-fx-text-fill: green;");
                }
            }
        });

        tablaLotes.getColumns().addAll(colId, colCant, colIngreso, colVenc, colEstado);

        // Cargar datos
        ObservableList<LoteInfo> datosLotes = FXCollections.observableArrayList();
        String sql = "SELECT IdLote, CantidadActual, FechaIngreso, FechaVencimiento FROM lotes WHERE IdProducto = ? AND CantidadActual > 0 ORDER BY FechaVencimiento ASC";

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, insumo.id());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                datosLotes.add(new LoteInfo(
                        rs.getInt("IdLote"),
                        rs.getDouble("CantidadActual"),
                        rs.getString("FechaIngreso"),
                        rs.getString("FechaVencimiento")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        tablaLotes.setItems(datosLotes);
        tablaLotes.setPlaceholder(new Label("No hay lotes activos para este producto."));
        tablaLotes.setPrefWidth(600);
        tablaLotes.setPrefHeight(300);

        dialog.getDialogPane().setContent(tablaLotes);
        dialog.showAndWait();
    }

    private void cargarDatos() {
        todosLosInsumos.clear();
        lowStockCount = 0;
        double valorTotal = 0;
        int provCount = 0;

        // Consulta SIMPLE que NO lee fechas
        String sql = "SELECT p.IdProducto, p.Tipo_de_Producto, p.Stock, p.Stock_Minimo, p.Unidad_de_medida, p.PrecioUnitario, prov.Nombre_comercial " +
                "FROM producto p LEFT JOIN proveedores prov ON p.IdProveedor = prov.IdProveedor";

        try (Connection conn = ConexionBD.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                InsumoAlmacen insumo = new InsumoAlmacen(
                        rs.getInt("IdProducto"), rs.getString("Tipo_de_Producto"), rs.getInt("Stock"), rs.getInt("Stock_Minimo"),
                        rs.getString("Unidad_de_medida"), rs.getDouble("PrecioUnitario"), rs.getString("Nombre_comercial")
                );
                todosLosInsumos.add(insumo);
                valorTotal += insumo.stock() * insumo.precioUnitario();
                if (insumo.esStockBajo()) lowStockCount++;
            }
            // Contar proveedores
            ResultSet rsProv = stmt.executeQuery("SELECT COUNT(*) FROM proveedores");
            if (rsProv.next()) provCount = rsProv.getInt(1);

        } catch (SQLException e) {
            e.printStackTrace();
            // Mostrar alerta solo si falla la carga
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setTitle("Error de BD");
            a.setContentText("No se pudo cargar el inventario: " + e.getMessage());
            a.showAndWait();
        }

        totalInsumosLabel.setText(String.valueOf(todosLosInsumos.size()));
        stockBajoLabel.setText(String.valueOf(lowStockCount));
        valorTotalLabel.setText(String.format("$%.2f", valorTotal));
        totalProveedoresLabel.setText(String.valueOf(provCount));
    }

    private void setupFiltering() {
        filteredInsumos = new FilteredList<>(todosLosInsumos, p -> true);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredInsumos.setPredicate(i -> {
                if (newVal == null || newVal.isEmpty()) return true;
                return i.nombre().toLowerCase().contains(newVal.toLowerCase());
            });
        });
        tablaInsumos.setItems(filteredInsumos);
    }

    private void actualizarUI() {
        // La carga de datos ya actualiza las etiquetas
    }

    public record InsumoAlmacen(int id, String nombre, int stock, int stockMinimo, String unidad, double precioUnitario,
                                String proveedorNombre) {
            public InsumoAlmacen(int id, String nombre, int stock, int stockMinimo, String unidad, double precioUnitario, String proveedorNombre) {
                this.id = id;
                this.nombre = nombre;
                this.stock = stock;
                this.stockMinimo = stockMinimo;
                this.unidad = unidad;
                this.precioUnitario = precioUnitario;
                this.proveedorNombre = proveedorNombre != null ? proveedorNombre : "N/A";
            }

        public boolean esStockBajo() {
            return stock <= stockMinimo;
        }

        public String getEstado() {
            return stock <= stockMinimo ? "Bajo Stock" : (stock <= stockMinimo * 1.5 ? "Medio" : "Normal");
        }
        }

    public static class LoteInfo {
        private final int idLote;
        private final double cantidad;
        private final String fechaIngreso;
        private final String fechaVencimiento;
        private final String estado;

        public LoteInfo(int id, double cant, String ingreso, String vencimiento) {
            String estado1;
            this.idLote = id;
            this.cantidad = cant;
            this.fechaIngreso = ingreso;
            this.fechaVencimiento = vencimiento;

            if (vencimiento == null || vencimiento.isEmpty()) {
                estado1 = "No Vence";
            } else {
                try {
                    LocalDate fechaVenc = LocalDate.parse(vencimiento);
                    LocalDate hoy = LocalDate.now();

                    if (fechaVenc.isBefore(hoy)) {
                        estado1 = "VENCIDO";
                    } else if (fechaVenc.isBefore(hoy.plusDays(7))) { // Alerta 7 días antes
                        estado1 = "Por Vencer";
                    } else {
                        estado1 = "Ok";
                    }
                } catch (Exception e) {
                    estado1 = "Error Fecha";
                }
            }
            this.estado = estado1;
        }

        public int getIdLote() {
            return idLote;
        }
        public double getCantidad() {
            return cantidad;
        }
        public String getFechaIngreso() {
            return fechaIngreso;
        }
        public String getFechaVencimiento() {
            return fechaVencimiento;
        }
        public String getEstado() {
            return estado;
        }
    }




}