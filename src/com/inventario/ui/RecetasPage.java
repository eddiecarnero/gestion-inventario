package com.inventario.ui;

import com.inventario.config.ConexionBD;
import com.inventario.util.ConversorUnidades;
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

    // --- NUEVO: Selector de Tipo de Receta ---
    private final ComboBox<String> tipoDestinoCombo;

    // --- NUEVO: Combo que acepta Insumos Y Intermedios ---
    private final ComboBox<IngredienteSeleccionable> ingredienteCombo;

    private final TextField cantidadIngredienteField;
    private final ComboBox<String> unidadIngredienteCombo;
    private VBox historialContainer;

    // --- Listas de Datos ---
    private final ObservableList<IngredienteItem> items = FXCollections.observableArrayList();
    private final ObservableList<IngredienteSeleccionable> todosLosIngredientesPosibles = FXCollections.observableArrayList();

    // (CSS_STYLES se mantiene igual, omitido para brevedad...)
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

        // Inicializar nuevos componentes
        tipoDestinoCombo = new ComboBox<>();
        ingredienteCombo = new ComboBox<>();

        cantidadIngredienteField = new TextField();
        unidadIngredienteCombo = new ComboBox<>();

        VBox mainContent = new VBox(20);
        VBox headerBox = new VBox(5);
        Label header = new Label("Gestión de Recetas"); header.getStyleClass().add("header-title");
        headerBox.getChildren().addAll(header, new Label("Define cómo se crean los productos intermedios y finales"));

        TabPane tabPane = new TabPane(); tabPane.getStyleClass().add("tab-pane");
        Tab tabNueva = new Tab("Nueva Receta", crearTabNuevaReceta()); tabNueva.setClosable(false);
        Tab tabHistorial = new Tab("Historial", crearTabHistorial()); tabHistorial.setClosable(false);

        tabHistorial.setOnSelectionChanged(e -> { if (tabHistorial.isSelected()) cargarHistorial(historialContainer); });

        tabPane.getTabs().addAll(tabNueva, tabHistorial);
        mainContent.getChildren().addAll(headerBox, tabPane);
        setCenter(mainContent);

        cargarTodosLosIngredientesPosibles();
    }

    private Node crearTabNuevaReceta() {
        VBox layout = new VBox(20); layout.getStyleClass().add("tab-content-area");
        VBox card = new VBox(25); card.getStyleClass().add("card");

        HBox cardHeader = new HBox(10, new Text("🍳"), new Label("Definir Nueva Receta"));
        ((Label)cardHeader.getChildren().get(1)).getStyleClass().add("card-title");
        cardHeader.setAlignment(Pos.CENTER_LEFT);

        // --- FILA 1: Datos Generales ---
        GridPane grid = new GridPane(); grid.setHgap(20); grid.setVgap(10);

        nombreRecetaField.setPromptText("Ej. Base de Galleta o Helado de Fresa");
        grid.add(crearCampo("Nombre de la Receta", nombreRecetaField), 0, 0);

        // Selector de Destino
        tipoDestinoCombo.getItems().addAll("Producto Intermedio (Almacén 2)", "Producto Terminado (Almacén 3)");
        tipoDestinoCombo.getSelectionModel().selectFirst();
        tipoDestinoCombo.setMaxWidth(Double.MAX_VALUE);
        grid.add(crearCampo("Tipo de Producto Resultante", tipoDestinoCombo), 1, 0);

        cantidadProducidaField.setPromptText("Ej. 1");
        grid.add(crearCampo("Cantidad Resultante", cantidadProducidaField), 0, 1);

        unidadProducidaCombo.getItems().addAll("Unidad", "Kg", "Litro", "Porción", "Bote 1L", "Paleta");
        unidadProducidaCombo.getSelectionModel().selectFirst();
        unidadProducidaCombo.setMaxWidth(Double.MAX_VALUE);
        grid.add(crearCampo("Unidad Resultante", unidadProducidaCombo), 1, 1);

        ColumnConstraints c1 = new ColumnConstraints(); c1.setPercentWidth(50);
        grid.getColumnConstraints().addAll(c1, c1);

        // --- SECCIÓN: Ingredientes ---
        VBox itemsBox = new VBox(15);
        itemsBox.setStyle("-fx-border-color: #E0E0E0; -fx-border-radius: 8; -fx-padding: 15; -fx-background-color: #F9FAFB;");

        Label lblIng = new Label("Lista de Ingredientes (Insumos o Intermedios)");
        lblIng.setStyle("-fx-font-weight: bold; -fx-text-fill: #555;");

        GridPane itemsGrid = new GridPane(); itemsGrid.setHgap(15);

        // Combo Híbrido (Alm1 y Alm2)
        ingredienteCombo.setPromptText("Buscar ingrediente...");
        ingredienteCombo.setMaxWidth(Double.MAX_VALUE);
        ingredienteCombo.setItems(todosLosIngredientesPosibles);
        ingredienteCombo.setConverter(new StringConverter<>() {
            @Override public String toString(IngredienteSeleccionable i) { return i != null ? i.toString() : null; }
            @Override public IngredienteSeleccionable fromString(String s) { return null; }
        });

        // Auto-seleccionar unidad
        ingredienteCombo.setOnAction(e -> {
            IngredienteSeleccionable prod = ingredienteCombo.getValue();
            if (prod != null && prod.getUnidad() != null) {
                // 1. Obtener solo las unidades válidas para este producto
                List<String> compatibles = ConversorUnidades.obtenerUnidadesCompatibles(prod.getUnidad());

                // 2. Actualizar el combo de unidades
                unidadIngredienteCombo.getItems().setAll(compatibles);

                // 3. Seleccionar la unidad base por defecto
                unidadIngredienteCombo.setValue(prod.getUnidad());
            }
        });

        itemsGrid.add(crearCampo("Seleccionar Ingrediente", ingredienteCombo), 0, 0);
        cantidadIngredienteField.setPromptText("0");
        itemsGrid.add(crearCampo("Cant.", cantidadIngredienteField), 1, 0);

        unidadIngredienteCombo.getItems().addAll("Kg", "Gramo", "Litro", "ml", "Unidad", "Cucharada");
        unidadIngredienteCombo.getSelectionModel().selectFirst();
        itemsGrid.add(crearCampo("Unid.", unidadIngredienteCombo), 2, 0);

        Button addBtn = new Button("➕ Agregar");
        addBtn.getStyleClass().add("button-add");
        addBtn.setOnAction(e -> agregarItem());
        itemsGrid.add(new VBox(new Label(""), addBtn), 3, 0); // Spacer label for alignment

        ColumnConstraints ic1 = new ColumnConstraints(); ic1.setPercentWidth(50);
        ColumnConstraints ic2 = new ColumnConstraints(); ic2.setPercentWidth(15);
        ColumnConstraints ic3 = new ColumnConstraints(); ic3.setPercentWidth(15);
        ColumnConstraints ic4 = new ColumnConstraints(); ic4.setPercentWidth(20);
        itemsGrid.getColumnConstraints().addAll(ic1, ic2, ic3, ic4);

        itemsBox.getChildren().addAll(lblIng, itemsGrid);

        configurarTabla();
        VBox.setVgrow(tablaItems, Priority.ALWAYS);

        Button saveBtn = new Button("💾 Guardar Receta Maestra");
        saveBtn.getStyleClass().add("button-primary");
        saveBtn.setMaxWidth(Double.MAX_VALUE);
        saveBtn.setOnAction(e -> guardarReceta());

        card.getChildren().addAll(cardHeader, grid, itemsBox, tablaItems, saveBtn);
        layout.getChildren().add(card);
        return layout;
    }

    private void configurarTabla() {
        tablaItems.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<IngredienteItem, String> cTipo = new TableColumn<>("Tipo");
        cTipo.setCellValueFactory(new PropertyValueFactory<>("tipoOrigen")); // INSUMO o INTERMEDIO

        TableColumn<IngredienteItem, String> c1 = new TableColumn<>("Ingrediente");
        c1.setCellValueFactory(new PropertyValueFactory<>("nombre"));

        TableColumn<IngredienteItem, Double> c2 = new TableColumn<>("Cantidad");
        c2.setCellValueFactory(new PropertyValueFactory<>("cantidad"));

        TableColumn<IngredienteItem, String> c3 = new TableColumn<>("Unidad");
        c3.setCellValueFactory(new PropertyValueFactory<>("unidad"));

        TableColumn<IngredienteItem, Void> c4 = new TableColumn<>("Acción");
        c4.setCellFactory(p -> new TableCell<>(){
            Button b = new Button("🗑️");
            { b.getStyleClass().add("button-danger"); b.setOnAction(e -> items.remove(getIndex())); }
            @Override protected void updateItem(Void i, boolean e){super.updateItem(i,e); setGraphic(e?null:b);}
        });

        tablaItems.getColumns().addAll(cTipo, c1, c2, c3, c4);
        tablaItems.setItems(items);
    }

    // --- CARGA DE DATOS HÍBRIDA ---
    private void cargarTodosLosIngredientesPosibles() {
        todosLosIngredientesPosibles.clear();
        try (Connection c = ConexionBD.getConnection(); Statement s = c.createStatement()) {

            // 1. Cargar INSUMOS (Almacén 1)
            ResultSet rs1 = s.executeQuery("SELECT IdProducto, Tipo_de_Producto, Unidad_de_medida FROM producto");
            while (rs1.next()) {
                todosLosIngredientesPosibles.add(new IngredienteSeleccionable(
                        rs1.getInt("IdProducto"),
                        rs1.getString("Tipo_de_Producto"),
                        rs1.getString("Unidad_de_medida"),
                        "INSUMO"
                ));
            }
            rs1.close();

            // 2. Cargar INTERMEDIOS (Almacén 2) - ¡Aquí está la magia!
            // Nota: Usamos un ID negativo temporal o un campo separado en la lógica interna,
            // pero la clase Wrapper maneja esto limpiamente.
            ResultSet rs2 = s.executeQuery("SELECT IdProductoIntermedio, Nombre, Unidad_de_medida FROM productos_intermedios");
            while (rs2.next()) {
                todosLosIngredientesPosibles.add(new IngredienteSeleccionable(
                        rs2.getInt("IdProductoIntermedio"),
                        rs2.getString("Nombre"),
                        rs2.getString("Unidad_de_medida"),
                        "INTERMEDIO"
                ));
            }
            rs2.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void agregarItem() {
        try {
            IngredienteSeleccionable seleccion = ingredienteCombo.getValue();
            double q = Double.parseDouble(cantidadIngredienteField.getText());

            if (seleccion != null && q > 0) {
                items.add(new IngredienteItem(
                        seleccion.getId(),
                        seleccion.getNombre(),
                        q,
                        unidadIngredienteCombo.getValue(),
                        seleccion.getTipo() // Pasamos el tipo (INSUMO o INTERMEDIO)
                ));
                limpiarCamposItem();
            }
        } catch (Exception e) {
            mostrarAlerta("Error", "Datos inválidos en el ingrediente");
        }
    }

    private void guardarReceta() {
        if (nombreRecetaField.getText().isEmpty() || items.isEmpty()) {
            mostrarAlerta("Error", "Faltan datos (nombre o ingredientes)");
            return;
        }

        String tipoDestino = tipoDestinoCombo.getValue().contains("Intermedio") ? "INTERMEDIO" : "FINAL";

        try (Connection c = ConexionBD.getConnection()) {
            c.setAutoCommit(false);
            int idReceta = 0;

            // 1. Insertar Cabecera de Receta (con el nuevo campo tipo_destino)
            String sqlReceta = "INSERT INTO recetas (nombre, cantidad_producida, unidad_producida, tipo_destino) VALUES (?,?,?,?)";
            try (PreparedStatement ps = c.prepareStatement(sqlReceta, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, nombreRecetaField.getText());
                ps.setDouble(2, Double.parseDouble(cantidadProducidaField.getText()));
                ps.setString(3, unidadProducidaCombo.getValue());
                ps.setString(4, tipoDestino);
                ps.executeUpdate();
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) idReceta = rs.getInt(1);
            }

            // 2. Insertar Ingredientes (Manejando tipos mixtos)
            // SQL actualizado para guardar IdIntermedio si aplica
            String sqlIng = "INSERT INTO ingredientes (receta_id, IdProducto, IdIntermedio, cantidad, unidad, tipo_origen) VALUES (?,?,?,?,?,?)";

            try (PreparedStatement ps = c.prepareStatement(sqlIng)) {
                for (IngredienteItem i : items) {
                    ps.setInt(1, idReceta);

                    if (i.getTipoOrigen().equals("INSUMO")) {
                        ps.setInt(2, i.getIdReferencia()); // IdProducto
                        ps.setNull(3, Types.INTEGER);      // IdIntermedio es NULL
                    } else {
                        ps.setNull(2, Types.INTEGER);      // IdProducto es NULL
                        ps.setInt(3, i.getIdReferencia()); // IdIntermedio
                    }

                    ps.setDouble(4, i.getCantidad());
                    ps.setString(5, i.getUnidad());
                    ps.setString(6, i.getTipoOrigen()); // Guardamos 'INSUMO' o 'INTERMEDIO'

                    ps.addBatch();
                }
                ps.executeBatch();
            }

            c.commit();
            mostrarAlerta("Éxito", "Receta de tipo " + tipoDestino + " guardada correctamente.");
            limpiarFormulario();

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error SQL", e.getMessage());
        }
    }

    // --- MÉTODOS AUXILIARES ---
    private VBox crearCampo(String l, Control c) {
        VBox v=new VBox(5);
        Label lbl=new Label(l);
        lbl.getStyleClass().add("label");
        v.getChildren().addAll(lbl, c);
        return v;
    }

    private void mostrarAlerta(String t, String m) {
        Alert a=new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(t); a.setContentText(m); a.showAndWait();
    }

    private void limpiarCamposItem() {
        ingredienteCombo.getSelectionModel().clearSelection();
        cantidadIngredienteField.clear();
    }

    private void limpiarFormulario() {
        nombreRecetaField.clear();
        cantidadProducidaField.clear();
        items.clear();
    }

    // --- TABLA HISTORIAL (Simplificada para visualización) ---
    private Node crearTabHistorial() {
        VBox layout = new VBox(20); layout.getStyleClass().add("tab-content-area");
        historialContainer = new VBox(10);
        ScrollPane sp = new ScrollPane(historialContainer); sp.setFitToWidth(true);
        layout.getChildren().add(sp);
        return layout;
    }

    private void cargarHistorial(VBox container) {
        container.getChildren().clear();
        String sql = "SELECT nombre, tipo_destino FROM recetas ORDER BY id DESC LIMIT 20";
        try(Connection c = ConexionBD.getConnection(); ResultSet rs = c.createStatement().executeQuery(sql)){
            while(rs.next()){
                Label l = new Label("Receta: " + rs.getString("nombre") + " [" + rs.getString("tipo_destino") + "]");
                l.setStyle("-fx-font-size: 14px; -fx-padding: 5;");
                container.getChildren().add(new VBox(l, new Separator()));
            }
        } catch(Exception e){}
    }

    // ==========================================
    // CLASES INTERNAS (MODELOS)
    // ==========================================

    /**
     * Clase Wrapper para mostrar tanto Insumos como Intermedios en el mismo ComboBox
     */
    public static class IngredienteSeleccionable {
        private final int id;
        private final String nombre;
        private final String unidad;
        private final String tipo; // "INSUMO" o "INTERMEDIO"

        public IngredienteSeleccionable(int id, String nombre, String unidad, String tipo) {
            this.id = id;
            this.nombre = nombre;
            this.unidad = unidad;
            this.tipo = tipo;
        }

        public int getId() { return id; }
        public String getNombre() { return nombre; }
        public String getUnidad() { return unidad; }
        public String getTipo() { return tipo; }

        @Override
        public String toString() {
            // Mostramos visualmente el origen
            String prefijo = tipo.equals("INSUMO") ? "[Alm1] " : "[Alm2] ";
            return prefijo + nombre;
        }
    }

    /**
     * Modelo para la tabla de items agregados a la receta actual
     */
    public static class IngredienteItem {
        private final int idReferencia; // Puede ser IdProducto o IdIntermedio
        private final String nombre;
        private final double cantidad;
        private final String unidad;
        private final String tipoOrigen; // "INSUMO" o "INTERMEDIO"

        public IngredienteItem(int idRef, String n, double c, String u, String tipo) {
            this.idReferencia = idRef;
            this.nombre = n;
            this.cantidad = c;
            this.unidad = u;
            this.tipoOrigen = tipo;
        }

        public int getIdReferencia() { return idReferencia; }
        public String getNombre() { return nombre; }
        public double getCantidad() { return cantidad; }
        public String getUnidad() { return unidad; }
        public String getTipoOrigen() { return tipoOrigen; }
    }

    public static class TestApp extends Application { @Override public void start(Stage s) { s.setScene(new Scene(new RecetasPage(), 1000, 700)); s.show(); } }
    public static void main(String[] args) { Application.launch(TestApp.class, args); }
}