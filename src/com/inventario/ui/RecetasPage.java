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
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RecetasPage extends BorderPane {

    // --- Variables de UI ---
    private final TableView<IngredienteItem> tablaItems;
    private final TextField nombreRecetaField;
    private final TextField cantidadProducidaField;
    private final ComboBox<String> unidadProducidaCombo;
    private final ComboBox<ProductoSimple> ingredienteCombo;
    private final TextField cantidadIngredienteField;
    private final ComboBox<String> unidadIngredienteCombo;
    private VBox historialContainer;

    // --- Listas de Datos ---
    private final ObservableList<IngredienteItem> items = FXCollections.observableArrayList();
    private final ObservableList<ProductoSimple> todosLosProductos = FXCollections.observableArrayList();

    // (CSS_STYLES ... sin cambios)
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

    public RecetasPage() {
        this.getStylesheets().add("data:text/css," + CSS_STYLES.replace("\n", ""));
        this.getStyleClass().add("root");
        setPadding(new Insets(30, 40, 30, 40));

        tablaItems = new TableView<>();
        nombreRecetaField = new TextField();
        cantidadProducidaField = new TextField();
        unidadProducidaCombo = new ComboBox<>();
        ingredienteCombo = new ComboBox<>();
        cantidadIngredienteField = new TextField();
        unidadIngredienteCombo = new ComboBox<>();

        VBox mainContent = new VBox(20);
        VBox headerBox = new VBox(5);
        Label header = new Label("Gestión de Recetas"); header.getStyleClass().add("header-title");
        headerBox.getChildren().addAll(header, new Label("Crear y gestionar recetas y sus ingredientes"));

        TabPane tabPane = new TabPane(); tabPane.getStyleClass().add("tab-pane");
        Tab tabNueva = new Tab("Nueva Receta", crearTabNuevaReceta()); tabNueva.setClosable(false);
        Tab tabHistorial = new Tab("Historial", crearTabHistorial()); tabHistorial.setClosable(false);

        tabHistorial.setOnSelectionChanged(e -> { if (tabHistorial.isSelected()) cargarHistorial(historialContainer); });

        tabPane.getTabs().addAll(tabNueva, tabHistorial);
        mainContent.getChildren().addAll(headerBox, tabPane);
        setCenter(mainContent);

        cargarProductos();
    }

    private Node crearTabNuevaReceta() {
        VBox layout = new VBox(20); layout.getStyleClass().add("tab-content-area");
        VBox card = new VBox(25); card.getStyleClass().add("card");

        HBox cardHeader = new HBox(10, new Text("🍳"), new Label("Crear Nueva Receta"));
        ((Label)cardHeader.getChildren().get(1)).getStyleClass().add("card-title");
        cardHeader.setAlignment(Pos.CENTER_LEFT);

        GridPane grid = new GridPane(); grid.setHgap(20); grid.setVgap(10);
        nombreRecetaField.setPromptText("Ej. Pie de Limón");
        grid.add(crearCampo("Nombre Receta", nombreRecetaField), 0, 0);
        cantidadProducidaField.setPromptText("Ej. 1");
        grid.add(crearCampo("Cantidad Producida", cantidadProducidaField), 1, 0);
        unidadProducidaCombo.getItems().addAll("Unidad", "Kg", "Litro", "Porción");
        unidadProducidaCombo.getSelectionModel().selectFirst();
        grid.add(crearCampo("Unidad", unidadProducidaCombo), 2, 0);
        ColumnConstraints c1 = new ColumnConstraints(); c1.setPercentWidth(50);
        ColumnConstraints c2 = new ColumnConstraints(); c2.setPercentWidth(25);
        grid.getColumnConstraints().addAll(c1, c2, c2);

        // Items
        VBox itemsBox = new VBox(15); itemsBox.setStyle("-fx-border-color: #E0E0E0; -fx-border-radius: 8; -fx-padding: 15;");
        GridPane itemsGrid = new GridPane(); itemsGrid.setHgap(15);

        ingredienteCombo.setPromptText("Ingrediente"); ingredienteCombo.setMaxWidth(Double.MAX_VALUE);
        ingredienteCombo.setItems(todosLosProductos); ingredienteCombo.setConverter(new ProductoSimpleConverter());

        // --- NUEVO: Auto-seleccionar unidad al elegir producto ---
        ingredienteCombo.setOnAction(e -> {
            ProductoSimple prod = ingredienteCombo.getValue();
            if (prod != null && prod.getUnidad() != null) {
                // Si la unidad no está en la lista, la agregamos para que no falle
                if (!unidadIngredienteCombo.getItems().contains(prod.getUnidad())) {
                    unidadIngredienteCombo.getItems().add(prod.getUnidad());
                }
                // Seleccionamos la unidad por defecto del producto
                unidadIngredienteCombo.setValue(prod.getUnidad());
            }
        });
        // -------------------------------------------------------

        itemsGrid.add(crearCampo("Ingrediente", ingredienteCombo), 0, 0);
        cantidadIngredienteField.setPromptText("0");
        itemsGrid.add(crearCampo("Cant.", cantidadIngredienteField), 1, 0);

        unidadIngredienteCombo.getItems().addAll("Kg", "Gramo", "Litro", "Unidad", "Cucharada", "ml");
        unidadIngredienteCombo.getSelectionModel().selectFirst();
        itemsGrid.add(crearCampo("Unid.", unidadIngredienteCombo), 2, 0);

        Button addBtn = new Button("➕"); addBtn.getStyleClass().add("button-add");
        addBtn.setOnAction(e -> agregarItem());
        itemsGrid.add(new VBox(new Label(), addBtn), 3, 0);

        ColumnConstraints ic1 = new ColumnConstraints(); ic1.setPercentWidth(50);
        ColumnConstraints ic2 = new ColumnConstraints(); ic2.setPercentWidth(15);
        ColumnConstraints ic3 = new ColumnConstraints(); ic3.setPercentWidth(15);
        ColumnConstraints ic4 = new ColumnConstraints(); ic4.setPercentWidth(10);
        itemsGrid.getColumnConstraints().addAll(ic1, ic2, ic3, ic4);

        itemsBox.getChildren().addAll(new Label("Ingredientes"), itemsGrid);

        configurarTabla();
        VBox.setVgrow(tablaItems, Priority.ALWAYS);

        Button saveBtn = new Button("💾 Guardar Receta");
        saveBtn.getStyleClass().add("button-primary"); saveBtn.setMaxWidth(Double.MAX_VALUE);
        saveBtn.setOnAction(e -> guardarReceta());

        card.getChildren().addAll(cardHeader, grid, itemsBox, tablaItems, saveBtn);
        layout.getChildren().add(card);
        return layout;
    }

    private void configurarTabla() {
        tablaItems.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        TableColumn<IngredienteItem, String> c1 = new TableColumn<>("Ingrediente"); c1.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        TableColumn<IngredienteItem, Double> c2 = new TableColumn<>("Cantidad"); c2.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        TableColumn<IngredienteItem, String> c3 = new TableColumn<>("Unidad"); c3.setCellValueFactory(new PropertyValueFactory<>("unidad"));
        TableColumn<IngredienteItem, Void> c4 = new TableColumn<>("Acción");
        c4.setCellFactory(p -> new TableCell<>(){
            Button b = new Button("🗑️"); { b.getStyleClass().add("button-danger"); b.setOnAction(e -> items.remove(getIndex())); }
            @Override protected void updateItem(Void i, boolean e){super.updateItem(i,e); setGraphic(e?null:b);}
        });
        tablaItems.getColumns().addAll(c1, c2, c3, c4);
        tablaItems.setItems(items);
    }

    // --- HISTORIAL ---
    private Node crearTabHistorial() {
        VBox layout = new VBox(20); layout.getStyleClass().add("tab-content-area");
        VBox card = new VBox(15); card.getStyleClass().add("card");
        card.getChildren().add(new Label("Historial de Recetas"));
        historialContainer = new VBox(10); historialContainer.setPadding(new Insets(10));
        ScrollPane sp = new ScrollPane(historialContainer); sp.setFitToWidth(true); sp.setStyle("-fx-background-color:transparent;");
        card.getChildren().add(sp); VBox.setVgrow(sp, Priority.ALWAYS);
        layout.getChildren().add(card);
        return layout;
    }

    private void cargarHistorial(VBox container) {
        container.getChildren().clear();
        String sqlRec = "SELECT id, nombre, cantidad_producida, unidad_producida FROM recetas ORDER BY fecha_creacion DESC";
        String sqlIng = "SELECT p.Tipo_de_Producto, i.cantidad, i.unidad FROM ingredientes i JOIN producto p ON i.IdProducto=p.IdProducto WHERE i.receta_id=?";
        try (Connection c = ConexionBD.getConnection(); Statement s = c.createStatement(); ResultSet rs = s.executeQuery(sqlRec)) {
            while(rs.next()) {
                int id = rs.getInt(1);
                List<IngredienteItem> ings = new ArrayList<>();
                try(PreparedStatement ps = c.prepareStatement(sqlIng)){
                    ps.setInt(1, id); ResultSet r2 = ps.executeQuery();
                    while(r2.next()) ings.add(new IngredienteItem(0, r2.getString(1), r2.getDouble(2), r2.getString(3)));
                }
                container.getChildren().add(crearCardReceta(new RecetaHistorial(id, rs.getString(2), rs.getDouble(3), rs.getString(4), ings)));
            }
        } catch(Exception e) { e.printStackTrace(); }
    }

    private Node crearCardReceta(RecetaHistorial r) {
        VBox card = new VBox(10); card.getStyleClass().add("card"); card.setStyle("-fx-border-color: #E0E0E0;");

        BorderPane header = new BorderPane();
        VBox titleBox = new VBox(2, new Label(r.getNombre()), new Label("Produce: " + r.getCantidadProducida() + " " + r.getUnidadProducida()));
        ((Label)titleBox.getChildren().get(0)).setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        ((Label)titleBox.getChildren().get(1)).setStyle("-fx-text-fill: #555;");

        Button deleteBtn = new Button("Eliminar");
        deleteBtn.getStyleClass().add("button-delete-card");
        deleteBtn.setOnAction(e -> eliminarReceta(r));

        header.setLeft(titleBox);
        header.setRight(deleteBtn);

        VBox body = new VBox(5); body.setPadding(new Insets(5,0,0,15));
        for(IngredienteItem i : r.getItems()) body.getChildren().add(new Label("• " + i.getNombre() + " (" + i.getCantidad() + " " + i.getUnidad() + ")"));

        card.getChildren().addAll(header, new Separator(), body);
        return card;
    }

    // --- ACCIONES ---
    private void eliminarReceta(RecetaHistorial r) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Eliminar Receta");
        alert.setHeaderText("¿Está seguro de eliminar la receta '" + r.getNombre() + "'?");
        alert.setContentText("Esto no se puede deshacer.");

        if (alert.showAndWait().get() == ButtonType.OK) {
            try (Connection conn = ConexionBD.getConnection(); PreparedStatement stmt = conn.prepareStatement("DELETE FROM recetas WHERE id = ?")) {
                stmt.setInt(1, r.getId());
                stmt.executeUpdate();
                cargarHistorial(historialContainer); // Refrescar
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void agregarItem() {
        try {
            ProductoSimple p = ingredienteCombo.getValue();
            double q = Double.parseDouble(cantidadIngredienteField.getText());
            if(p!=null && q>0) { items.add(new IngredienteItem(p.getId(), p.getNombre(), q, unidadIngredienteCombo.getValue())); limpiarCamposItem(); }
        } catch(Exception e) { mostrarAlerta("Error", "Datos inválidos"); }
    }

    private void guardarReceta() {
        if(nombreRecetaField.getText().isEmpty() || items.isEmpty()) { mostrarAlerta("Error", "Faltan datos"); return; }
        try(Connection c = ConexionBD.getConnection()) { c.setAutoCommit(false);
            int id = 0;
            try(PreparedStatement ps = c.prepareStatement("INSERT INTO recetas (nombre, cantidad_producida, unidad_producida) VALUES (?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, nombreRecetaField.getText()); ps.setDouble(2, Double.parseDouble(cantidadProducidaField.getText())); ps.setString(3, unidadProducidaCombo.getValue());
                ps.executeUpdate(); ResultSet rs = ps.getGeneratedKeys(); if(rs.next()) id = rs.getInt(1);
            }
            try(PreparedStatement ps = c.prepareStatement("INSERT INTO ingredientes (receta_id, IdProducto, cantidad, unidad) VALUES (?,?,?,?)")) {
                for(IngredienteItem i : items) { ps.setInt(1, id); ps.setInt(2, i.getIdProducto()); ps.setDouble(3, i.getCantidad()); ps.setString(4, i.getUnidad()); ps.addBatch(); }
                ps.executeBatch();
            }
            c.commit(); mostrarAlerta("Éxito", "Receta guardada."); limpiarFormulario();
        } catch(Exception e) { e.printStackTrace(); }
    }

    // --- NUEVO: Cargar productos y sus UNIDADES ---
    private void cargarProductos() {
        try(Connection c=ConexionBD.getConnection(); ResultSet rs=c.createStatement().executeQuery("SELECT IdProducto, Tipo_de_Producto, Unidad_de_medida FROM producto")) {
            while(rs.next()) todosLosProductos.add(new ProductoSimple(rs.getInt(1), rs.getString(2), rs.getString(3)));
        } catch(Exception e){}
    }

    private VBox crearCampo(String l, Control c) { VBox v=new VBox(5); Label lbl=new Label(l); lbl.getStyleClass().add("label"); v.getChildren().addAll(lbl, c); return v; }
    private void mostrarAlerta(String t, String m) { Alert a=new Alert(Alert.AlertType.INFORMATION); a.setTitle(t); a.setContentText(m); a.showAndWait(); }
    private void limpiarCamposItem() { ingredienteCombo.getSelectionModel().clearSelection(); cantidadIngredienteField.clear(); }
    private void limpiarFormulario() { nombreRecetaField.clear(); cantidadProducidaField.clear(); items.clear(); }

    // --- CLASES INTERNAS ACTUALIZADAS ---
    public static class ProductoSimple {
        private final int id; private final String nombre; private final String unidad;
        public ProductoSimple(int i, String n, String u){id=i;nombre=n;unidad=u;}
        public int getId(){return id;} public String getNombre(){return nombre;} public String getUnidad(){return unidad;}
        @Override public String toString(){return nombre;}
    }

    public static class ProductoSimpleConverter extends StringConverter<ProductoSimple> { @Override public String toString(ProductoSimple p){return p!=null?p.getNombre():null;} @Override public ProductoSimple fromString(String s){return null;} }
    public static class IngredienteItem { private final int idProducto; private final String nombre; private final double cantidad; private final String unidad; public IngredienteItem(int i, String n, double c, String u){idProducto=i;nombre=n;cantidad=c;unidad=u;} public int getIdProducto(){return idProducto;} public String getNombre(){return nombre;} public double getCantidad(){return cantidad;} public String getUnidad(){return unidad;} }
    public static class RecetaHistorial { private final int id; private final String nombre, unidad; private final double cantidad; private final List<IngredienteItem> items; public RecetaHistorial(int i, String n, double c, String u, List<IngredienteItem> l){id=i;nombre=n;cantidad=c;unidad=u;items=l;} public int getId(){return id;} public String getNombre(){return nombre;} public double getCantidadProducida(){return cantidad;} public String getUnidadProducida(){return unidad;} public List<IngredienteItem> getItems(){return items;} }

    public static class TestApp extends Application { @Override public void start(Stage s) { s.setScene(new Scene(new RecetasPage(), 1000, 700)); s.show(); } }
    public static void main(String[] args) { Application.launch(TestApp.class, args); }
}