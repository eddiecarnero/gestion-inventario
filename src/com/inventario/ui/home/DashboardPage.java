package com.inventario.ui.home;

import com.inventario.logic.DashboardService;
import com.inventario.logic.DashboardStats;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class DashboardPage extends BorderPane {

    // --- Estilos Mejorados ---
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
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 2);
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
        /* --- ALERTA MEJORADA --- */
        .alert-box-warning {
            -fx-background-color: #FFF4E5; /* Naranja muy suave */
            -fx-border-color: #FFCC80;      /* Naranja borde */
            -fx-border-width: 0 0 0 4px;    /* Borde izquierdo más grueso */
            -fx-border-radius: 4;
            -fx-background-radius: 4;
            -fx-padding: 15px;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);
        }
        .alert-title {
            -fx-text-fill: #D84315; /* Naranja Oscuro Intenso */
            -fx-font-weight: bold;
            -fx-font-size: 1.1em;
        }
        .alert-body {
            -fx-text-fill: #BF360C; /* Marrón rojizo */
            -fx-font-weight: normal;
            -fx-font-size: 1.0em;
        }
        /* ----------------------- */
        
        .stats-card-clickable {
            -fx-background-color: white;
            -fx-border-color: #E0E0E0;
            -fx-border-width: 1 1 1 4px;
            -fx-border-radius: 8;
            -fx-background-radius: 8;
            -fx-padding: 15px 20px;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 2);
        }
        .stats-card-navigable {
            -fx-cursor: hand;
        }
        .stats-card-navigable:hover {
            -fx-background-color: #FAFAFA;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 8, 0, 0, 3);
        }
        
        .stats-card-border-primary { -fx-border-color: #E0E0E0 #E0E0E0 #E0E0E0 #4A90E2; }
        .stats-card-border-secondary { -fx-border-color: #E0E0E0 #E0E0E0 #E0E0E0 #6D4C41; }
        .stats-card-border-tertiary { -fx-border-color: #E0E0E0 #E0E0E0 #E0E0E0 #B8956A; }
        
        .stats-card-title {
            -fx-font-size: 0.95em;
            -fx-font-weight: 600;
            -fx-text-fill: #555555;
        }
        .stats-card-content {
            -fx-font-size: 2.0em;
            -fx-font-weight: bold;
            -fx-text-fill: #222222;
        }
        .stats-card-desc {
            -fx-font-size: 0.9em;
            -fx-text-fill: #666666;
        }
        
        .badge {
            -fx-padding: 2 8 2 8;
            -fx-background-radius: 10;
            -fx-font-size: 0.85em;
            -fx-font-weight: bold;
            -fx-text-fill: white;
        }
        .badge-destructive {
            -fx-background-color: #EF4444; 
        }
        
        .quick-action-button {
            -fx-background-color: white;
            -fx-text-fill: #333333;
            -fx-font-weight: 600;
            -fx-font-size: 1.0em;
            -fx-pref-height: 45px;
            -fx-background-radius: 6;
            -fx-border-color: #DDDDDD;
            -fx-border-radius: 6;
            -fx-cursor: hand;
            -fx-alignment: center-left;
            -fx-padding: 0 15;
        }
        .quick-action-button:hover {
            -fx-background-color: #F3F4F6; 
            -fx-border-color: #BBBBBB;
        }
        .sales-item {
            -fx-padding: 10;
            -fx-border-color: #EEEEEE;
            -fx-border-width: 0 0 1 0;
        }
    """;

    // --- UI Elementos Dinámicos ---
    private final HBox alertBoxContainer;

    // Almacén 1
    private final Label countInsumosLabel;
    private final Label descInsumosLabel;
    private final Label badgeInsumosLabel;

    // Almacén 2
    private final Label countIntermediosLabel;
    private final Label descIntermediosLabel;

    // Almacén 3
    private final Label countTerminadosLabel;
    private final Label descTerminadosLabel;

    // Ventas
    private final VBox ventasListContainer;

    // --- Datos ---
    private List<String> insumosLowStock;

    private int totalInsumos;
    private double totalStockInsumos;

    private int totalIntermedios;
    private double totalStockIntermedios;

    private int totalTerminados;
    private double totalStockTerminados;

    private final List<String> topVentas = new ArrayList<>();

    // --- Navegación ---
    private final Consumer<String> onNavigate;

    public DashboardPage(Consumer<String> onNavigate) {
        this.onNavigate = onNavigate;
        // Cargar estilos
        this.getStylesheets().add("data:text/css," + CSS_STYLES.replace("\n", ""));
        this.getStyleClass().add("root");

        // Inicializar UI Labels
        alertBoxContainer = new HBox();
        alertBoxContainer.setAlignment(Pos.CENTER_LEFT);
        alertBoxContainer.setFillHeight(true);

        countInsumosLabel = new Label("0");
        descInsumosLabel = new Label("Cargando...");
        badgeInsumosLabel = new Label();
        badgeInsumosLabel.setVisible(false);

        countIntermediosLabel = new Label("0");
        descIntermediosLabel = new Label("Cargando...");

        countTerminadosLabel = new Label("0");
        descTerminadosLabel = new Label("Cargando...");

        ventasListContainer = new VBox(10);

        // --- Layout Principal ---
        VBox mainContent = new VBox(25);
        mainContent.setPadding(new Insets(30, 40, 30, 40));

        // Rejilla de Estadísticas
        Node statsGrid = crearStatsGrid();

        // Acciones y Ventas
        Node actionsGrid = crearActionsGrid();

        mainContent.getChildren().addAll(alertBoxContainer, statsGrid, actionsGrid);

        ScrollPane scrollPane = new ScrollPane(mainContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        setCenter(scrollPane);

        // Cargar datos
        cargarDatos();
        actualizarUIConDatos();
    }

    private HBox crearAlertBox(List<String> lowStockItems) {
        HBox alertBox = new HBox(15);
        alertBox.getStyleClass().add("alert-box-warning");
        alertBox.setAlignment(Pos.CENTER_LEFT);
        alertBox.setMaxWidth(Double.MAX_VALUE); // Ocupar ancho disponible

        // Icono más visual
        Label iconLabel = new Label("⚠️");
        iconLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: #EF6C00;");

        VBox textBox = new VBox(4);

        Label title = new Label("¡Atención! " + lowStockItems.size() + " insumo(s) con stock crítico");
        title.getStyleClass().add("alert-title");

        String items = String.join(", ", lowStockItems);
        if (items.length() > 120) items = items.substring(0, 120) + "...";

        Label itemsLabel = new Label("Revisar: " + items);
        itemsLabel.getStyleClass().add("alert-body");
        itemsLabel.setWrapText(true); // Permitir que el texto baje si es muy largo

        textBox.getChildren().addAll(title, itemsLabel);
        alertBox.getChildren().addAll(iconLabel, textBox);
        HBox.setHgrow(textBox, Priority.ALWAYS); // Que el texto ocupe el espacio restante

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
                "stats-card-border-primary", "almacen1"
        );
        grid.add(cardInsumos, 0, 0);

        // --- Card 2: Intermedios (Clicable) ---
        Node cardIntermedios = createStatCard(
                "Almacén 2 - Intermedios", "🧪",
                countIntermediosLabel, descIntermediosLabel, null,
                "stats-card-border-secondary", "almacen2"
        );
        grid.add(cardIntermedios, 1, 0);

        // --- Card 3: Terminados (Clicable) ---
        Node cardTerminados = createStatCard(
                "Almacén 3 - Terminados", "🍦",
                countTerminadosLabel, descTerminadosLabel, null,
                "stats-card-border-tertiary", "almacen3"
        );
        grid.add(cardTerminados, 2, 0);

        return grid;
    }

    private Node createStatCard(String title, String icon, Label countLabel, Label descLabel, Label badgeLabel, String styleClass, String pageName) {
        VBox card = new VBox(8); // Spacing entre elementos
        card.getStyleClass().addAll("stats-card-clickable", styleClass);

        // Header
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("stats-card-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 1.5em;"); // Icono como Label para mejor renderizado

        header.getChildren().addAll(titleLabel, spacer, iconLabel);

        // Content
        VBox content = new VBox(2);
        countLabel.getStyleClass().add("stats-card-content");
        descLabel.getStyleClass().add("stats-card-desc");
        content.getChildren().addAll(countLabel, descLabel);

        if (badgeLabel != null) {
            badgeLabel.getStyleClass().add("badge");
            badgeLabel.getStyleClass().add("badge-destructive");
            // Contenedor para el badge para margen
            HBox badgeContainer = new HBox(badgeLabel);
            badgeContainer.setPadding(new Insets(5, 0, 0, 0));
            content.getChildren().add(badgeContainer);
        }

        card.getChildren().addAll(header, content);

        if (pageName != null && !pageName.isEmpty()) {
            card.getStyleClass().add("stats-card-navigable");
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
        Label descAcciones = new Label("Accesos directos a operaciones frecuentes");
        descAcciones.getStyleClass().add("card-description");

        VBox buttonsBox = new VBox(10);
        buttonsBox.getChildren().addAll(
                crearQuickActionButton("📦 Nueva Orden de Compra", "orden-compra"),
                crearQuickActionButton("⬇ Ingreso Rápido Insumos", "gestion-insumos"),
                crearQuickActionButton("📈 Registrar Ventas", "subir-ventas"),
                crearQuickActionButton("🍳 Crear Receta", "recetas")
        );

        cardAcciones.getChildren().addAll(titleAcciones, descAcciones, new Separator(), buttonsBox);

        // --- Card 2: Más Vendidos ---
        VBox cardVentas = new VBox(15);
        cardVentas.getStyleClass().add("card");
        Label titleVentas = new Label("Top Productos Vendidos");
        titleVentas.getStyleClass().add("card-title");
        Label descVentas = new Label("Ranking global por cantidad vendida");
        descVentas.getStyleClass().add("card-description");

        ScrollPane scrollVentas = new ScrollPane(ventasListContainer);
        scrollVentas.setFitToWidth(true);
        scrollVentas.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scrollVentas.setMinHeight(180); // Altura fija mínima

        cardVentas.getChildren().addAll(titleVentas, descVentas, new Separator(), scrollVentas);

        grid.add(cardAcciones, 0, 0);
        grid.add(cardVentas, 1, 0);
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
        // 1. Mantenimiento automático
        com.inventario.logic.GestorVencimientos.verificarYLimpiarVencidos();

        // 2. Instanciar Servicio
        DashboardService service = new DashboardService();

        // 3. Cargar Estadísticas Generales
        DashboardStats stats = service.obtenerEstadisticas();
        totalInsumos = stats.totalInsumos;
        totalStockInsumos = stats.stockInsumos;
        totalIntermedios = stats.totalIntermedios;
        totalStockIntermedios = stats.stockIntermedios;
        totalTerminados = stats.totalTerminados;
        totalStockTerminados = stats.stockTerminados;

        // 4. Cargar Listas (Sin SQL aquí, todo lo hace el servicio)
        insumosLowStock = service.obtenerNombresBajoStock();

        topVentas.clear();
        topVentas.addAll(service.obtenerTopVentas());
    }

    // Separa el Top Ventas en un métodito privado para no mezclar

    private void actualizarUIConDatos() {
        // --- Alertas ---
        alertBoxContainer.getChildren().clear();
        if (insumosLowStock != null && !insumosLowStock.isEmpty()) {
            alertBoxContainer.getChildren().add(crearAlertBox(insumosLowStock));
        }

        // --- Card 1 ---
        countInsumosLabel.setText(String.valueOf(totalInsumos));
        descInsumosLabel.setText(String.format("Total en stock: %.1f", totalStockInsumos));
        if (!insumosLowStock.isEmpty()) {
            badgeInsumosLabel.setText(insumosLowStock.size() + " bajo stock");
            badgeInsumosLabel.setVisible(true);
        } else {
            badgeInsumosLabel.setVisible(false);
        }

        // --- Card 2 ---
        countIntermediosLabel.setText(String.valueOf(totalIntermedios));
        descIntermediosLabel.setText(String.format("Stock en proceso: %.1f", totalStockIntermedios));

        // --- Card 3 ---
        countTerminadosLabel.setText(String.valueOf(totalTerminados));
        descTerminadosLabel.setText(String.format("Listo para venta: %.0f", totalStockTerminados));

        // --- Top Ventas ---
        ventasListContainer.getChildren().clear();
        if(topVentas.isEmpty()) {
            Label l = new Label("No hay ventas registradas aún.");
            l.setStyle("-fx-text-fill: #999; -fx-font-style: italic;");
            ventasListContainer.getChildren().add(l);
        } else {
            int rank = 1;
            for(String v : topVentas) {
                HBox row = new HBox(10);
                row.setAlignment(Pos.CENTER_LEFT);
                row.getStyleClass().add("sales-item");

                Label lblRank = new Label("#" + rank);
                lblRank.setStyle("-fx-font-weight: bold; -fx-text-fill: #4A90E2; -fx-font-size: 1.1em;");

                Label lblName = new Label(v);
                lblName.setStyle("-fx-text-fill: #333;");

                row.getChildren().addAll(lblRank, lblName);
                ventasListContainer.getChildren().add(row);
                rank++;
            }
        }
    }

    public static class TestApp extends Application {
        @Override
        public void start(Stage stage) {
            stage.setTitle("Dashboard - JavaFX");
            stage.setScene(new Scene(new DashboardPage(s -> System.out.println(s)), 1300, 900));
            stage.show();
        }
    }

    public static void main(String[] args) {
        Application.launch(TestApp.class, args);
    }
}