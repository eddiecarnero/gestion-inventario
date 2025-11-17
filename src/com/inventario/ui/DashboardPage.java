package com.inventario.ui;

import com.inventario.config.ConexionBD;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
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
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class DashboardPage extends BorderPane {

    // --- Estilos ---
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
        .alert-box-warning {
            -fx-background-color: #FFF7ED; /* orange-50 */
            -fx-border-color: #FEEBCF; /* orange-200 */
            -fx-border-width: 1;
            -fx-border-radius: 8;
            -fx-background-radius: 8;
            -fx-padding: 15px;
        }
        .alert-text-warning {
            -fx-fill: #9A3412; /* orange-900 */
            -fx-font-weight: 500;
        }
        .alert-text-warning-bold {
            -fx-fill: #9A3412; /* orange-900 */
            -fx-font-weight: bold;
        }
        .stats-card-clickable {
            -fx-background-color: white;
            -fx-border-color: #E0E0E0;
            -fx-border-width: 1 1 1 4px;
            -fx-border-radius: 8;
            -fx-background-radius: 8;
            -fx-padding: 15px 20px;
        }
        /* Estilo separado para los que sí son clicables */
        .stats-card-navigable {
            -fx-cursor: hand;
        }
        .stats-card-navigable:hover {
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0.15, 0, 3);
        }
        
        .stats-card-border-primary { -fx-border-color: #E0E0E0 #E0E0E0 #E0E0E0 #4A90E2; }
        .stats-card-border-secondary { -fx-border-color: #E0E0E0 #E0E0E0 #E0E0E0 #6D4C41; }
        .stats-card-border-tertiary { -fx-border-color: #E0E0E0 #E0E0E0 #E0E0E0 #B8956A; }
        
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
        .stats-card-desc {
            -fx-font-size: 0.85em;
            -fx-text-fill: #555555;
        }
        
        .badge {
            -fx-padding: 4 10 4 10;
            -fx-background-radius: 12;
            -fx-font-size: 0.9em;
            -fx-font-weight: bold;
            -fx-text-fill: white;
        }
        .badge-destructive {
            -fx-background-color: #EF4444; /* red-500 */
        }
        .badge-secondary {
            -fx-background-color: #6D4C41;
        }
        
        .quick-action-button {
            -fx-background-color: white;
            -fx-text-fill: #333333;
            -fx-font-weight: 500;
            -fx-font-size: 1.0em;
            -fx-pref-height: 40px;
            -fx-background-radius: 5;
            -fx-border-color: #CCCCCC;
            -fx-border-radius: 5;
            -fx-cursor: hand;
            -fx-alignment: center-left;
        }
        .quick-action-button:hover {
            -fx-background-color: #F9FAFB; /* gray-50 */
        }
    """;

    // --- UI Elementos Dinámicos ---
    private final HBox alertBoxContainer;
    private final Label countInsumosLabel;
    private final Label descInsumosLabel;
    private final Label badgeInsumosLabel;
    private final Label countRecetasLabel; // Sigue siendo la label, pero mostrará 0
    private final Label descRecetasLabel; // Sigue siendo la label, pero mostrará "N/A"
    private final Label countTerminadosLabel;
    private final Label descTerminadosLabel;

    // --- Datos ---
    private List<String> insumosLowStock;
    private int totalInsumos;
    private double totalStockInsumos;
    // private int totalRecetas; // Ya no se necesita cargar

    // --- Navegación ---
    private final Consumer<String> onNavigate;

    public DashboardPage(Consumer<String> onNavigate) {
        this.onNavigate = onNavigate;
        this.getStylesheets().add("data:text/css," + CSS_STYLES.replace("\n", ""));
        this.getStyleClass().add("root");

        // Inicializar UI Labels
        alertBoxContainer = new HBox();
        countInsumosLabel = new Label("0");
        descInsumosLabel = new Label("Total en stock: 0 unidades");
        badgeInsumosLabel = new Label();
        badgeInsumosLabel.setVisible(false);

        countRecetasLabel = new Label("0");
        descRecetasLabel = new Label("N/A - No implementado");

        countTerminadosLabel = new Label("0");
        descTerminadosLabel = new Label("N/A - No implementado");

        // --- Layout Principal ---
        VBox mainContent = new VBox(25);
        mainContent.setPadding(new Insets(30, 40, 30, 40));

        // 1. Header
        Node header = crearHeader();

        // 2. Rejilla de Estadísticas
        Node statsGrid = crearStatsGrid();

        // 3. Acciones
        Node actionsGrid = crearActionsGrid();

        mainContent.getChildren().addAll(header, alertBoxContainer, statsGrid, actionsGrid);

        ScrollPane scrollPane = new ScrollPane(mainContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        setCenter(scrollPane);

        // Cargar datos
        cargarDatos();
        actualizarUIConDatos();
    }

    private Node crearHeader() {
        VBox headerBox = new VBox(5);
        Label header = new Label("Dashboard Principal");
        header.getStyleClass().add("header-title");
        Label description = new Label("Resumen general del inventario de Mamatania");
        description.getStyleClass().add("header-description");
        return headerBox;
    }

    private HBox crearAlertBox(List<String> lowStockItems) {
        HBox alertBox = new HBox(15);
        alertBox.getStyleClass().add("alert-box-warning");
        alertBox.setAlignment(Pos.CENTER_LEFT);

        Text icon = new Text("⚠️");
        icon.setFont(Font.font(20));
        icon.setFill(Color.web("#D97706"));

        VBox textBox = new VBox(3);
        Label title = new Label(lowStockItems.size() + " insumo(s) por debajo del stock mínimo");
        title.getStyleClass().add("alert-text-warning-bold");

        String items = String.join(", ", lowStockItems);
        if (items.length() > 100) items = items.substring(0, 100) + "...";
        Label itemsLabel = new Label(items);
        itemsLabel.getStyleClass().add("alert-text-warning");

        textBox.getChildren().addAll(title, itemsLabel);
        alertBox.getChildren().addAll(icon, textBox);
        return alertBox;
    }

    private Node crearStatsGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(20);

        ColumnConstraints col = new ColumnConstraints();
        col.setPercentWidth(33.33);
        grid.getColumnConstraints().addAll(col, col, col);

        // --- Card 1: Insumos (Clicable) ---
        Node cardInsumos = createStatCard(
                "Almacén 1 - Insumos", "📦",
                countInsumosLabel, descInsumosLabel, badgeInsumosLabel,
                "stats-card-border-primary", "almacen1" // "almacen1" -> Navega
        );
        grid.add(cardInsumos, 0, 0);

        // --- Card 2: Intermedios (No Clicable) ---
        Node cardIntermedios = createStatCard(
                "Almacén 2 - Intermedios", "🧪",
                countRecetasLabel, descRecetasLabel, null,
                "stats-card-border-secondary", null // null -> No navega
        );
        grid.add(cardIntermedios, 1, 0);

        // --- Card 3: Terminados (No Clicable) ---
        Node cardTerminados = createStatCard(
                "Almacén 3 - Terminados", "🍦",
                countTerminadosLabel, descTerminadosLabel, null,
                "stats-card-border-tertiary", null // null -> No navega
        );
        grid.add(cardTerminados, 2, 0);

        return grid;
    }

    private Node createStatCard(String title, String icon, Label countLabel, Label descLabel, Label badgeLabel, String styleClass, String pageName) {
        VBox card = new VBox(5);
        card.getStyleClass().addAll("stats-card-clickable", styleClass); // Mantiene el borde de color

        // Header
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("stats-card-title");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Text iconText = new Text(icon);
        iconText.setFont(Font.font(16));
        header.getChildren().addAll(titleLabel, spacer, iconText);

        // Content
        VBox content = new VBox(5);
        countLabel.getStyleClass().add("stats-card-content");
        descLabel.getStyleClass().add("stats-card-desc");
        content.getChildren().addAll(countLabel, descLabel);

        if (badgeLabel != null) {
            content.getChildren().add(badgeLabel);
            badgeLabel.getStyleClass().add("badge");
            badgeLabel.getStyleClass().add("badge-destructive");
            VBox.setMargin(badgeLabel, new Insets(5, 0, 0, 0));
        }

        card.getChildren().addAll(header, content);

        // --- CORREGIDO: Añadir la acción solo si pageName existe ---
        if (pageName != null && !pageName.isEmpty()) {
            card.getStyleClass().add("stats-card-navigable"); // Añade cursor y hover
            card.setOnMouseClicked(e -> onNavigate.accept(pageName));
        }

        return card;
    }

    private Node crearActionsGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(20);
        ColumnConstraints col = new ColumnConstraints();
        col.setPercentWidth(50);
        grid.getColumnConstraints().addAll(col, col);

        // --- Card 1: Acciones Rápidas ---
        VBox cardAcciones = new VBox(15);
        cardAcciones.getStyleClass().add("card");

        Label titleAcciones = new Label("Acciones Rápidas");
        titleAcciones.getStyleClass().add("card-title");
        Label descAcciones = new Label("Operaciones comunes del sistema");
        descAcciones.getStyleClass().add("card-description");

        VBox buttonsBox = new VBox(10);
        buttonsBox.getChildren().addAll(
                crearQuickActionButton("📦 Nueva Orden de Compra", "orden-compra"),
                crearQuickActionButton("📈 Registrar Ventas Diarias", "subir-ventas"),
                crearQuickActionButton("📋 Ver Kardex General", "kardex")
        );

        cardAcciones.getChildren().addAll(titleAcciones, descAcciones, new Separator(), buttonsBox);

        // --- Card 2: Placeholder para "Más Vendidos" ---
        VBox cardPlaceholder = new VBox(15);
        cardPlaceholder.getStyleClass().add("card");
        Label titleVentas = new Label("Productos Más Vendidos");
        titleVentas.getStyleClass().add("card-title");
        Label descVentas = new Label("Top productos de esta semana");
        descVentas.getStyleClass().add("card-description");

        Label placeholderText = new Label("Módulo de ventas no implementado.\nSe requiere una tabla 'ventas' para esta función.");
        placeholderText.setWrapText(true);
        VBox placeholderBox = new VBox(placeholderText);
        placeholderBox.setAlignment(Pos.CENTER);
        placeholderBox.setMinHeight(150);

        cardPlaceholder.getChildren().addAll(titleVentas, descVentas, new Separator(), placeholderBox);

        grid.add(cardAcciones, 0, 0);
        grid.add(cardPlaceholder, 1, 0);
        return grid;
    }

    private Button crearQuickActionButton(String text, String pageName) {
        Button btn = new Button(text);
        btn.getStyleClass().add("quick-action-button");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setOnAction(e -> onNavigate.accept(pageName));
        return btn;
    }

    // --- Carga de Datos ---
    private void cargarDatos() {
        // Inicializar
        insumosLowStock = new ArrayList<>();
        totalInsumos = 0; // Total de *tipos* de producto
        totalStockInsumos = 0; // Suma de *stock* de lotes
        // totalRecetas = 0; // (Ya está comentado, bien)

        // --- SQL CORREGIDO ---
        // 1. Obtener el total de *tipos* de producto (ej. "Harina", "Arroz" -> 2)
        String sqlTiposProducto = "SELECT COUNT(*) as count FROM producto";

        // 2. Obtener la suma total de *stock* de todos los lotes
        String sqlTotalStock = "SELECT SUM(CantidadActual) as total_stock FROM lotes";

        // 3. Obtener los nombres de los productos que están por debajo del stock mínimo
        //    (Compara la suma de los lotes de un producto con su Stock_Minimo)
        String sqlLowStock = "SELECT p.Tipo_de_Producto " +
                "FROM producto p " +
                "LEFT JOIN (SELECT IdProducto, SUM(CantidadActual) as total_stock " +
                "           FROM lotes " +
                "           GROUP BY IdProducto) as LoteSum " +
                "ON p.IdProducto = LoteSum.IdProducto " +
                "WHERE COALESCE(LoteSum.total_stock, 0) <= p.Stock_Minimo";
        // (Uso COALESCE para incluir productos con 0 stock que están por debajo del mínimo)


        try (Connection conn = ConexionBD.getConnection();
             Statement stmt = conn.createStatement()) { // Un solo Statement es suficiente

            // 1. Cargar total de *tipos* de producto
            try (ResultSet rs = stmt.executeQuery(sqlTiposProducto)) {
                if (rs.next()) {
                    totalInsumos = rs.getInt("count");
                }
            }

            // 2. Cargar suma de *stock* de lotes
            try (ResultSet rs = stmt.executeQuery(sqlTotalStock)) {
                if (rs.next()) {
                    totalStockInsumos = rs.getDouble("total_stock");
                }
            }

            // 3. Cargar Insumos con Stock Bajo
            try (ResultSet rs = stmt.executeQuery(sqlLowStock)) {
                while (rs.next()) {
                    insumosLowStock.add(rs.getString("Tipo_de_Producto"));
                }
            }

            // (La consulta de recetas ya estaba comentada, lo cual es correcto)

        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error de BD", "No se pudo cargar el dashboard: " + e.getMessage());
        }
    }



    private void actualizarUIConDatos() {
        // --- Actualizar Alerta ---
        alertBoxContainer.getChildren().clear();
        if (insumosLowStock != null && !insumosLowStock.isEmpty()) {
            alertBoxContainer.getChildren().add(crearAlertBox(insumosLowStock));
        }

        // --- Actualizar Card 1 (Insumos) ---
        countInsumosLabel.setText(String.valueOf(totalInsumos));
        descInsumosLabel.setText(String.format("Total en stock: %.1f unidades", totalStockInsumos));
        if (insumosLowStock != null && !insumosLowStock.isEmpty()) {
            badgeInsumosLabel.setText(insumosLowStock.size() + " bajo stock");
            badgeInsumosLabel.setVisible(true);
        } else {
            badgeInsumosLabel.setVisible(false);
        }

        // --- CORREGIDO: Actualizar Card 2 (Intermedios) ---
        countRecetasLabel.setText("0");
        descRecetasLabel.setText("N/A - No implementado");

        // --- Actualizar Card 3 (Terminados) ---
        countTerminadosLabel.setText("0");
        descTerminadosLabel.setText("N/A - No implementado");
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
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

            stage.setTitle("Dashboard - JavaFX");
            stage.setScene(new Scene(new DashboardPage(navigationHandler), 1300, 900));
            stage.show();
        }
    }

    public static void main(String[] args) {
        Application.launch(TestApp.class, args);
    }
}