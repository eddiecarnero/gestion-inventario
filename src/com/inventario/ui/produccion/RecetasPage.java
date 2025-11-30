package com.inventario.ui.produccion;

import com.inventario.config.ConexionBD;
import com.inventario.util.ConversorUnidades;
import com.inventario.dao.RecetaDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.util.StringConverter;

import java.sql.*;
import java.util.List;

public class RecetasPage extends BorderPane {

    // --- Variables de UI ---
    private final TableView<IngredienteItem> tablaItems;
    private final TextField nombreRecetaField;
    private final TextField cantidadProducidaField;
    private final ComboBox<String> unidadProducidaCombo;
    private final ComboBox<String> tipoDestinoCombo;
    private final ComboBox<IngredienteSeleccionable> ingredienteCombo;
    private final TextField cantidadIngredienteField;
    private final ComboBox<String> unidadIngredienteCombo;

    // Botones y Control
    private Button btnGuardar;
    private TabPane tabPane;
    private Integer idRecetaEditando = null; // Null = Nueva, ID = Editando

    // --- Variables para el Recetario ---
    private TableView<RecetaModel> tablaRecetas;
    private TableView<IngredienteItem> tablaDetalleReceta;
    private ObservableList<RecetaModel> listaRecetas = FXCollections.observableArrayList();
    private ObservableList<IngredienteItem> listaDetalleReceta = FXCollections.observableArrayList();

    // --- Listas de Datos ---
    private final ObservableList<IngredienteItem> items = FXCollections.observableArrayList();
    private final ObservableList<IngredienteSeleccionable> todosLosIngredientesPosibles = FXCollections.observableArrayList();

    public RecetasPage() {
        // Estilos CSS integrados
        String css = """
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
            .button-secondary { -fx-background-color: #9CA3AF; -fx-text-fill: white; -fx-font-weight: bold; -fx-pref-height: 40px; -fx-background-radius: 5; -fx-cursor: hand; }
            .button-add { -fx-background-color: #22C55E; -fx-text-fill: white; -fx-font-weight: bold; -fx-pref-height: 38px; -fx-cursor: hand; }
            .button-danger { -fx-background-color: #EF4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; }
            .button-edit { -fx-background-color: #F59E0B; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; }
            .table-view .column-header { -fx-background-color: #F9FAFB; -fx-font-weight: bold; }
        """;
        this.getStylesheets().add("data:text/css," + css.replace("\n", ""));
        this.getStyleClass().add("root");
        setPadding(new Insets(30, 40, 30, 40));

        tablaItems = new TableView<>();
        nombreRecetaField = new TextField();
        cantidadProducidaField = new TextField();
        unidadProducidaCombo = new ComboBox<>();
        tipoDestinoCombo = new ComboBox<>();
        ingredienteCombo = new ComboBox<>();
        cantidadIngredienteField = new TextField();
        unidadIngredienteCombo = new ComboBox<>();

        VBox mainContent = new VBox(20);
        VBox headerBox = new VBox(5);
        Label header = new Label("Gestión de Recetas"); header.getStyleClass().add("header-title");
        headerBox.getChildren().addAll(header, new Label("Define cómo se crean los productos intermedios y finales"));

        tabPane = new TabPane();
        tabPane.getStyleClass().add("tab-pane");

        Tab tabNueva = new Tab("Formulario Receta", crearTabNuevaReceta());
        tabNueva.setClosable(false);

        Tab tabRecetario = new Tab("Recetario (Ver/Editar)", crearTabRecetario());
        tabRecetario.setClosable(false);
        tabRecetario.setOnSelectionChanged(e -> { if (tabRecetario.isSelected()) cargarRecetas(); });

        tabPane.getTabs().addAll(tabNueva, tabRecetario);
        mainContent.getChildren().addAll(headerBox, tabPane);
        setCenter(mainContent);

        cargarTodosLosIngredientesPosibles();
    }

    // --- PESTAÑA 1: NUEVA/EDITAR RECETA ---
    private Node crearTabNuevaReceta() {
        VBox layout = new VBox(20); layout.getStyleClass().add("tab-content-area");
        VBox card = new VBox(25); card.getStyleClass().add("card");

        HBox cardHeader = new HBox(10, new Text("🍳"), new Label("Detalles de la Receta"));
        ((Label)cardHeader.getChildren().get(1)).getStyleClass().add("card-title");
        cardHeader.setAlignment(Pos.CENTER_LEFT);

        GridPane grid = new GridPane(); grid.setHgap(20); grid.setVgap(10);
        nombreRecetaField.setPromptText("Ej. Base de Galleta o Helado de Fresa");
        grid.add(crearCampo("Nombre de la Receta", nombreRecetaField), 0, 0);

        tipoDestinoCombo.getItems().addAll("Producto Intermedio (Almacén 2)", "Producto Terminado (Almacén 3)");
        tipoDestinoCombo.getSelectionModel().selectFirst(); tipoDestinoCombo.setMaxWidth(Double.MAX_VALUE);
        grid.add(crearCampo("Tipo de Producto Resultante", tipoDestinoCombo), 1, 0);

        cantidadProducidaField.setPromptText("Ej. 1");
        grid.add(crearCampo("Cantidad Resultante", cantidadProducidaField), 0, 1);

        unidadProducidaCombo.getItems().addAll("Unidad", "Kg", "Litro", "Porción", "Bote 1L", "Paleta");
        unidadProducidaCombo.getSelectionModel().selectFirst(); unidadProducidaCombo.setMaxWidth(Double.MAX_VALUE);
        grid.add(crearCampo("Unidad Resultante", unidadProducidaCombo), 1, 1);

        ColumnConstraints c1 = new ColumnConstraints(); c1.setPercentWidth(50);
        grid.getColumnConstraints().addAll(c1, c1);

        VBox itemsBox = new VBox(15); itemsBox.setStyle("-fx-border-color: #E0E0E0; -fx-border-radius: 8; -fx-padding: 15; -fx-background-color: #F9FAFB;");
        Label lblIng = new Label("Ingredientes (Insumos o Intermedios)");
        lblIng.setStyle("-fx-font-weight: bold; -fx-text-fill: #555;");

        GridPane itemsGrid = new GridPane(); itemsGrid.setHgap(15);
        ingredienteCombo.setPromptText("Buscar ingrediente..."); ingredienteCombo.setMaxWidth(Double.MAX_VALUE);
        ingredienteCombo.setItems(todosLosIngredientesPosibles);
        ingredienteCombo.setConverter(new StringConverter<>() {
            @Override public String toString(IngredienteSeleccionable i) { return i != null ? i.toString() : null; }
            @Override public IngredienteSeleccionable fromString(String s) { return null; }
        });

        ingredienteCombo.setOnAction(e -> {
            IngredienteSeleccionable prod = ingredienteCombo.getValue();
            if (prod != null && prod.getUnidad() != null) {
                List<String> compatibles = ConversorUnidades.obtenerUnidadesCompatibles(prod.getUnidad());
                unidadIngredienteCombo.getItems().setAll(compatibles);
                unidadIngredienteCombo.setValue(prod.getUnidad());
            }
        });

        itemsGrid.add(crearCampo("Seleccionar Ingrediente", ingredienteCombo), 0, 0);
        cantidadIngredienteField.setPromptText("0");
        itemsGrid.add(crearCampo("Cant.", cantidadIngredienteField), 1, 0);
        unidadIngredienteCombo.getItems().addAll("Kg", "Gramo", "Litro", "ml", "Unidad", "Cucharada");
        unidadIngredienteCombo.getSelectionModel().selectFirst();
        itemsGrid.add(crearCampo("Unid.", unidadIngredienteCombo), 2, 0);

        Button addBtn = new Button("➕ Agregar"); addBtn.getStyleClass().add("button-add");
        addBtn.setOnAction(e -> agregarItem());
        itemsGrid.add(new VBox(new Label(""), addBtn), 3, 0);

        ColumnConstraints ic1 = new ColumnConstraints(); ic1.setPercentWidth(50);
        ColumnConstraints ic2 = new ColumnConstraints(); ic2.setPercentWidth(15);
        ColumnConstraints ic3 = new ColumnConstraints(); ic3.setPercentWidth(15);
        ColumnConstraints ic4 = new ColumnConstraints(); ic4.setPercentWidth(20);
        itemsGrid.getColumnConstraints().addAll(ic1, ic2, ic3, ic4);

        itemsBox.getChildren().addAll(lblIng, itemsGrid);
        configurarTabla();
        VBox.setVgrow(tablaItems, Priority.ALWAYS);

        // Botones de acción
        HBox btnBox = new HBox(10);
        btnBox.setAlignment(Pos.CENTER_RIGHT);

        Button btnLimpiar = new Button("Cancelar / Limpiar");
        btnLimpiar.getStyleClass().add("button-secondary");
        btnLimpiar.setOnAction(e -> limpiarFormulario());

        btnGuardar = new Button("💾 Guardar Receta Maestra");
        btnGuardar.getStyleClass().add("button-primary");
        btnGuardar.setPrefWidth(200);
        btnGuardar.setOnAction(e -> guardarReceta());

        btnBox.getChildren().addAll(btnLimpiar, btnGuardar);

        card.getChildren().addAll(cardHeader, grid, itemsBox, tablaItems, btnBox);
        layout.getChildren().add(card);
        return layout;
    }

    private void configurarTabla() {
        tablaItems.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        TableColumn<IngredienteItem, String> cTipo = new TableColumn<>("Tipo");
        cTipo.setCellValueFactory(new PropertyValueFactory<>("tipoOrigen"));
        TableColumn<IngredienteItem, String> c1 = new TableColumn<>("Ingrediente");
        c1.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        TableColumn<IngredienteItem, Double> c2 = new TableColumn<>("Cantidad");
        c2.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        TableColumn<IngredienteItem, String> c3 = new TableColumn<>("Unidad");
        c3.setCellValueFactory(new PropertyValueFactory<>("unidad"));

        TableColumn<IngredienteItem, Void> c4 = new TableColumn<>("Acción");
        c4.setCellFactory(p -> new TableCell<>(){
            Button b = new Button("🗑️");
            {
                b.setStyle("-fx-background-color: #EF4444; -fx-text-fill: white; -fx-cursor: hand;");
                b.setOnAction(e -> items.remove(getIndex()));
            }
            @Override protected void updateItem(Void i, boolean e){super.updateItem(i,e); setGraphic(e?null:b);}
        });

        tablaItems.getColumns().addAll(cTipo, c1, c2, c3, c4);
        tablaItems.setItems(items);
    }

    // --- PESTAÑA 2: RECETARIO ---
    private Node crearTabRecetario() {
        HBox layout = new HBox(20);
        layout.getStyleClass().add("tab-content-area");
        layout.setPadding(new Insets(20, 0, 0, 0));

        // Izquierda: Lista
        VBox leftPanel = new VBox(10); leftPanel.setPrefWidth(450); leftPanel.getStyleClass().add("card");
        Label lblRecetas = new Label("Recetas Disponibles"); lblRecetas.getStyleClass().add("card-title");
        tablaRecetas = new TableView<>(); tablaRecetas.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<RecetaModel, String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        TableColumn<RecetaModel, String> colDestino = new TableColumn<>("Tipo");
        colDestino.setCellValueFactory(new PropertyValueFactory<>("tipoDestino"));

        TableColumn<RecetaModel, Void> colAccion = new TableColumn<>("Acciones");

        colAccion.setCellFactory(param -> new TableCell<>() {
            // 1. Definimos los componentes como variables de la celda
            private final Button btnEdit = new Button("✏️");
            private final Button btnDel = new Button("❌");
            private final HBox box = new HBox(5, btnEdit, btnDel);

            {
                // 2. Configuramos estilos y acciones en el bloque de inicialización

                // Estilo Botón Editar
                btnEdit.setStyle("-fx-background-color: transparent; -fx-text-fill: #F59E0B; -fx-font-size: 1.2em; -fx-cursor: hand;");
                btnEdit.setTooltip(new Tooltip("Editar Receta"));
                btnEdit.setOnAction(e -> {
                    // Obtenemos el objeto de la fila actual de forma segura
                    RecetaModel data = getTableView().getItems().get(getIndex());
                    cargarParaEdicion(data);
                });

                // Estilo Botón Eliminar
                btnDel.setStyle("-fx-background-color: transparent; -fx-text-fill: red; -fx-font-weight: bold; -fx-font-size: 1.2em; -fx-cursor: hand;");
                btnDel.setTooltip(new Tooltip("Eliminar Receta"));
                btnDel.setOnAction(e -> {
                    RecetaModel data = getTableView().getItems().get(getIndex());
                    eliminarReceta(data);
                });

                box.setAlignment(Pos.CENTER);
            }

            // 3. Sobrescribimos updateItem (¡ESTO ES LO QUE FALTABA!)
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null); // Si la fila está vacía, no mostramos nada
                } else {
                    setGraphic(box);  // Si hay datos, mostramos la caja con los botones
                }
            }
        });
        colAccion.setMinWidth(90);

        tablaRecetas.getColumns().addAll(colNombre, colDestino, colAccion);
        tablaRecetas.setItems(listaRecetas);

        tablaRecetas.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) cargarDetalleReceta(newVal);
        });

        leftPanel.getChildren().addAll(lblRecetas, tablaRecetas);
        VBox.setVgrow(tablaRecetas, Priority.ALWAYS);

        // Derecha: Detalles
        VBox rightPanel = new VBox(15); rightPanel.getStyleClass().add("card"); HBox.setHgrow(rightPanel, Priority.ALWAYS);
        Label lblDetalles = new Label("Vista Previa Ingredientes"); lblDetalles.getStyleClass().add("card-title");
        Label lblInfoExtra = new Label("Selecciona una receta para ver detalles...");
        lblInfoExtra.setStyle("-fx-text-fill: #777; -fx-font-style: italic;");

        tablaDetalleReceta = new TableView<>(); tablaDetalleReceta.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<IngredienteItem, String> cIng = new TableColumn<>("Ingrediente"); cIng.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        TableColumn<IngredienteItem, Double> cCant = new TableColumn<>("Cantidad"); cCant.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        TableColumn<IngredienteItem, String> cUnd = new TableColumn<>("Unidad"); cUnd.setCellValueFactory(new PropertyValueFactory<>("unidad"));
        TableColumn<IngredienteItem, String> cOrigen = new TableColumn<>("Origen"); cOrigen.setCellValueFactory(new PropertyValueFactory<>("tipoOrigen"));

        tablaDetalleReceta.getColumns().addAll(cIng, cCant, cUnd, cOrigen);
        tablaDetalleReceta.setItems(listaDetalleReceta);
        tablaDetalleReceta.setPlaceholder(new Label("Sin ingredientes seleccionados"));

        rightPanel.getChildren().addAll(lblDetalles, lblInfoExtra, tablaDetalleReceta);
        VBox.setVgrow(tablaDetalleReceta, Priority.ALWAYS);

        tablaRecetas.getSelectionModel().selectedItemProperty().addListener((obs, old, val) -> {
            if (val != null) {
                lblInfoExtra.setText("Produce: " + val.getCantidadProducida() + " " + val.getUnidadProducida());
                lblInfoExtra.setStyle("-fx-text-fill: #4A90E2; -fx-font-weight: bold;");
            } else {
                lblInfoExtra.setText("Selecciona una receta para ver detalles...");
                lblInfoExtra.setStyle("-fx-text-fill: #777;");
            }
        });

        layout.getChildren().addAll(leftPanel, rightPanel);
        return layout;
    }

    // --- LÓGICA DE DATOS ---

    private void cargarRecetas() {
        listaRecetas.clear(); listaDetalleReceta.clear();
        String sql = "SELECT id, nombre, cantidad_producida, unidad_producida, tipo_destino FROM recetas ORDER BY id DESC";
        try(Connection c = ConexionBD.getConnection(); ResultSet rs = c.createStatement().executeQuery(sql)) {
            while(rs.next()) listaRecetas.add(new RecetaModel(rs.getInt("id"), rs.getString("nombre"), rs.getDouble("cantidad_producida"), rs.getString("unidad_producida"), rs.getString("tipo_destino")));
        } catch(Exception e) { e.printStackTrace(); }
    }

    // Carga para solo visualización (Tab 2)
    private void cargarDetalleReceta(RecetaModel receta) {
        listaDetalleReceta.clear();
        String sql = "SELECT p.Tipo_de_Producto, pi.Nombre, i.cantidad, i.unidad, i.tipo_origen FROM ingredientes i LEFT JOIN producto p ON i.IdProducto = p.IdProducto LEFT JOIN productos_intermedios pi ON i.IdIntermedio = pi.IdProductoIntermedio WHERE i.receta_id = ?";
        try(Connection c = ConexionBD.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, receta.getId()); ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                String nombre = "INSUMO".equals(rs.getString("tipo_origen")) ? rs.getString(1) : rs.getString(2);
                listaDetalleReceta.add(new IngredienteItem(0, nombre, rs.getDouble("cantidad"), rs.getString("unidad"), rs.getString("tipo_origen")));
            }
        } catch(Exception e) { e.printStackTrace(); }
    }

    // Carga para edición (Tab 1)
    private void cargarParaEdicion(RecetaModel r) {
        limpiarFormulario(); // Resetear campos primero
        idRecetaEditando = r.getId();

        // Llenar campos cabecera
        nombreRecetaField.setText(r.getNombre());
        cantidadProducidaField.setText(String.valueOf(r.getCantidadProducida()));
        unidadProducidaCombo.setValue(r.getUnidadProducida());

        if("INTERMEDIO".equals(r.getTipoDestino())) tipoDestinoCombo.getSelectionModel().select(0);
        else tipoDestinoCombo.getSelectionModel().select(1);

        // Cambiar UI a modo edición
        btnGuardar.setText("💾 Actualizar Receta");

        // Cargar ingredientes a la lista editable 'items'
        String sql = "SELECT i.cantidad, i.unidad, i.tipo_origen, i.IdProducto, i.IdIntermedio, p.Tipo_de_Producto, pi.Nombre FROM ingredientes i LEFT JOIN producto p ON i.IdProducto = p.IdProducto LEFT JOIN productos_intermedios pi ON i.IdIntermedio = pi.IdProductoIntermedio WHERE i.receta_id = ?";

        try(Connection c = ConexionBD.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, r.getId());
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                String tipo = rs.getString("tipo_origen");
                int idRef = "INSUMO".equals(tipo) ? rs.getInt("IdProducto") : rs.getInt("IdIntermedio");
                String nombre = "INSUMO".equals(tipo) ? rs.getString("Tipo_de_Producto") : rs.getString("Nombre");

                items.add(new IngredienteItem(idRef, nombre, rs.getDouble("cantidad"), rs.getString("unidad"), tipo));
            }
        } catch(Exception e) {
            mostrarAlerta("Error", "No se pudieron cargar los ingredientes: " + e.getMessage());
        }

        // Cambiar al tab de edición
        tabPane.getSelectionModel().select(0);
    }

    private void limpiarFormulario() {
        idRecetaEditando = null;
        nombreRecetaField.clear();
        cantidadProducidaField.clear();
        unidadProducidaCombo.getSelectionModel().selectFirst();
        tipoDestinoCombo.getSelectionModel().selectFirst();
        items.clear();
        ingredienteCombo.getSelectionModel().clearSelection();
        cantidadIngredienteField.clear();
        unidadIngredienteCombo.getSelectionModel().selectFirst();

        btnGuardar.setText("💾 Guardar Receta Maestra");
    }

    private void eliminarReceta(RecetaModel r) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "¿Seguro que desea eliminar la receta '" + r.getNombre() + "'?", ButtonType.YES, ButtonType.NO);
        if (alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            try (Connection conn = ConexionBD.getConnection(); PreparedStatement stmt = conn.prepareStatement("DELETE FROM recetas WHERE id = ?")) {
                stmt.setInt(1, r.getId()); stmt.executeUpdate();
                cargarRecetas();
                // Si estábamos editando esta misma receta, limpiar
                if(idRecetaEditando != null && idRecetaEditando == r.getId()) limpiarFormulario();
            } catch (SQLException e) { mostrarAlerta("Error", e.getMessage()); }
        }
    }

    private void cargarTodosLosIngredientesPosibles() {
        todosLosIngredientesPosibles.clear();
        try (Connection c = ConexionBD.getConnection(); Statement s = c.createStatement()) {
            ResultSet rs1 = s.executeQuery("SELECT IdProducto, Tipo_de_Producto, Unidad_de_medida FROM producto");
            while (rs1.next()) todosLosIngredientesPosibles.add(new IngredienteSeleccionable(rs1.getInt(1), rs1.getString(2), rs1.getString(3), "INSUMO"));
            rs1.close();
            try {
                ResultSet rs2 = s.executeQuery("SELECT IdProductoIntermedio, Nombre, Unidad_de_medida FROM productos_intermedios");
                while (rs2.next()) todosLosIngredientesPosibles.add(new IngredienteSeleccionable(rs2.getInt(1), rs2.getString(2), rs2.getString(3), "INTERMEDIO"));
                rs2.close();
            } catch(Exception ignored){}
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void agregarItem() {
        try {
            IngredienteSeleccionable s = ingredienteCombo.getValue();
            double q = Double.parseDouble(cantidadIngredienteField.getText());
            if (s != null && q > 0) {
                items.add(new IngredienteItem(s.getId(), s.getNombre(), q, unidadIngredienteCombo.getValue(), s.getTipo()));
                ingredienteCombo.getSelectionModel().clearSelection();
                cantidadIngredienteField.clear();
            }
        } catch (Exception e) { mostrarAlerta("Error", "Datos inválidos"); }
    }

    private void guardarReceta() {
        if (nombreRecetaField.getText().isEmpty() || items.isEmpty()) { mostrarAlerta("Error", "Faltan datos o ingredientes."); return; }
        String tipoDestino = tipoDestinoCombo.getValue().contains("Intermedio") ? "INTERMEDIO" : "FINAL";

        // Preparar datos para enviar al DAO
        // Nota: Asegúrate de tener importado: import com.inventario.dao.RecetaDAO;
        RecetaDAO dao = new RecetaDAO();

        try {
            String nombre = nombreRecetaField.getText();
            double cantidad = Double.parseDouble(cantidadProducidaField.getText());
            String unidad = unidadProducidaCombo.getValue();

            // Llamada al método nuevo del DAO
            boolean exito = dao.guardarRecetaCompleta(
                    nombre,
                    cantidad,
                    unidad,
                    tipoDestino,
                    items, // Tu lista observable de IngredienteItem
                    idRecetaEditando // null si es nuevo, ID si es edición
            );

            if (exito) {
                mostrarAlerta("Éxito", idRecetaEditando == null ? "Receta guardada." : "Receta actualizada.");
                limpiarFormulario();
                cargarRecetas();
            } else {
                mostrarAlerta("Error", "No se pudo guardar la receta en la base de datos.");
            }

        } catch (NumberFormatException e) {
            mostrarAlerta("Error", "La cantidad producida debe ser un número válido.");
        }
    }

    private VBox crearCampo(String l, Control c) { VBox v=new VBox(5); Label lbl=new Label(l); lbl.getStyleClass().add("label"); v.getChildren().addAll(lbl, c); return v; }
    private void mostrarAlerta(String t, String m) { new Alert(Alert.AlertType.INFORMATION, m).showAndWait(); }

    // Clases Modelo (Inner)
    public static class IngredienteSeleccionable {
        private final int id; private final String nombre, unidad, tipo;
        public IngredienteSeleccionable(int i, String n, String u, String t) { id=i; nombre=n; unidad=u; tipo=t; }
        public int getId(){return id;} public String getNombre(){return nombre;} public String getUnidad(){return unidad;} public String getTipo(){return tipo;}
        @Override public String toString() { return (tipo.equals("INSUMO")?"[Insumo] ":"[Inter] ") + nombre; }
    }
}