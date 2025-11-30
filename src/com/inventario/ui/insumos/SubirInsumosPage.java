package com.inventario.ui.insumos;

import com.inventario.config.ConexionBD;
import com.inventario.ui.insumos.FilaCreacion;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class SubirInsumosPage extends BorderPane {

    // --- ESTILOS ---
    private static final String CSS_STYLES = """
        .root { -fx-background-color: #FDF8F0; -fx-font-family: 'Segoe UI'; }
        .header-title { -fx-font-size: 2.2em; -fx-font-weight: bold; -fx-text-fill: #333333; }
        .header-description { -fx-font-size: 1.1em; -fx-text-fill: #555555; }
        .tab-content-area { -fx-padding: 20 0 0 0; }
        .card { -fx-background-color: white; -fx-border-color: #E0E0E0; -fx-border-width: 1; -fx-border-radius: 8; -fx-padding: 20px; }
        .card-title { -fx-font-size: 1.4em; -fx-font-weight: bold; -fx-text-fill: #333333; }
        .label { -fx-font-size: 1.05em; -fx-font-weight: 500; -fx-text-fill: #333333; }
        .combo-box, .text-field { -fx-font-size: 1.05em; -fx-pref-height: 38px; -fx-border-color: #CCCCCC; -fx-border-radius: 5; }
        .button-primary { -fx-background-color: #4A90E2; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 5; }
        .button-success { -fx-background-color: #22C55E; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 5; }
        .button-secondary { -fx-background-color: #777777; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 5; }
        .button-add { -fx-background-color: #F59E0B; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 5; }
        .table-view .column-header { -fx-background-color: #F9FAFB; -fx-font-weight: bold; }
    """;

    // Datos para validación
    private final ObservableList<String> listaNombresProveedores = FXCollections.observableArrayList();
    private final Map<String, Integer> mapaProveedores = new HashMap<>();
    private final ObservableList<String> listaUnidades = FXCollections.observableArrayList(
            "Unidad", "Kg", "Gramo", "Litro", "ml", "Caja", "Paquete", "Saco", "Botella", "Lata"
    );

    // Tabla y Datos
    private TableView<FilaCreacion> tablaPreview;
    private final ObservableList<FilaCreacion> datosCarga = FXCollections.observableArrayList();

    public SubirInsumosPage() {
        this.getStylesheets().add("data:text/css," + CSS_STYLES.replace("\n", ""));
        this.getStyleClass().add("root");
        setPadding(new Insets(30, 40, 30, 40));

        cargarProveedores(); // Cargar lista real de la BD

        // Pestañas
        TabPane tabs = new TabPane();
        tabs.setStyle("-fx-background-color: transparent;");
        tabs.getTabs().addAll(crearTabManual(), crearTabMasiva());

        setCenter(tabs);
    }

    // --- 1. PESTAÑA MANUAL ---
    private Tab crearTabManual() {
        VBox layout = new VBox(20); layout.getStyleClass().add("tab-content-area");
        VBox card = new VBox(20); card.getStyleClass().add("card");

        Label cardTitle = new Label("Detalles del Producto"); cardTitle.getStyleClass().add("card-title");

        GridPane grid = new GridPane();
        grid.setHgap(20); grid.setVgap(15);

        // Campos exactos solicitados
        TextField txtNombre = new TextField(); txtNombre.setPromptText("Ej. Harina");
        ComboBox<String> cmbProv = new ComboBox<>(listaNombresProveedores);
        cmbProv.setPromptText("Seleccione..."); cmbProv.setMaxWidth(Double.MAX_VALUE);
        TextField txtPrecio = new TextField(); txtPrecio.setPromptText("0.0");

        TextField txtContenido = new TextField(); txtContenido.setPromptText("1.0");
        ComboBox<String> cmbUnidad = new ComboBox<>(listaUnidades);
        cmbUnidad.setPromptText("Seleccione..."); cmbUnidad.setMaxWidth(Double.MAX_VALUE);
        TextField txtMin = new TextField(); txtMin.setPromptText("0");

        TextField txtUbicacion = new TextField(); txtUbicacion.setPromptText("Ej. Estante A");

        // Fila 1
        grid.add(crearLabel("Nombre Producto"), 0, 0); grid.add(txtNombre, 0, 1);
        grid.add(crearLabel("Proveedor"), 1, 0);       grid.add(cmbProv, 1, 1);
        grid.add(crearLabel("Precio Compra ($)"), 2, 0); grid.add(txtPrecio, 2, 1);

        // Fila 2
        grid.add(crearLabel("Contenido (Cant. por Envase)"), 0, 2); grid.add(txtContenido, 0, 3);
        grid.add(crearLabel("Unidad de Medida"), 1, 2);             grid.add(cmbUnidad, 1, 3);
        grid.add(crearLabel("Stock Mínimo"), 2, 2);                 grid.add(txtMin, 2, 3);

        // Fila 3
        grid.add(crearLabel("Ubicación (Almacén)"), 0, 4); grid.add(txtUbicacion, 0, 5);

        // Columnas balanceadas
        ColumnConstraints c = new ColumnConstraints(); c.setPercentWidth(33.33);
        grid.getColumnConstraints().addAll(c, c, c);

        // Botón Agregar
        HBox boxBtn = new HBox(); boxBtn.setAlignment(Pos.CENTER_RIGHT);
        Button btnAgregar = new Button("⬇ Agregar a Lista");
        btnAgregar.getStyleClass().add("button-add");

        btnAgregar.setOnAction(e -> {
            try {
                if(txtNombre.getText().isEmpty()) { mostrarAlerta("Error", "Nombre es obligatorio"); return; }

                Double precio = parseDouble(txtPrecio.getText());
                Double cont = parseDouble(txtContenido.getText());
                Integer min = parseInt(txtMin.getText());
                String prov = cmbProv.getValue();
                String uni = cmbUnidad.getValue();
                String ubi = txtUbicacion.getText().isEmpty() ? null : txtUbicacion.getText();

                FilaCreacion fila = new FilaCreacion(txtNombre.getText(), prov, precio, cont, uni, min, ubi);
                validarFila(fila);
                datosCarga.add(fila);

                // Limpiar
                txtNombre.clear(); txtPrecio.clear(); txtContenido.clear(); txtMin.clear(); txtUbicacion.clear();
                cmbProv.getSelectionModel().clearSelection(); cmbUnidad.getSelectionModel().clearSelection();

            } catch(Exception ex) { mostrarAlerta("Error", "Verifique los números."); }
        });
        boxBtn.getChildren().add(btnAgregar);

        // Inicializar tabla
        crearTablaPreview();
        VBox.setVgrow(tablaPreview, Priority.ALWAYS);

        Button btnGuardar = new Button("💾 Guardar Todo en BD");
        btnGuardar.getStyleClass().add("button-success"); btnGuardar.setMaxWidth(Double.MAX_VALUE);
        btnGuardar.setOnAction(e -> guardarEnBD());

        card.getChildren().addAll(cardTitle, grid, boxBtn, new Separator(), new Label("Lista de carga:"), tablaPreview, btnGuardar);
        layout.getChildren().add(card);

        Tab tab = new Tab("Manual", layout); tab.setClosable(false);
        return tab;
    }

    // --- 2. PESTAÑA MASIVA (Excel/CSV) ---
    private Tab crearTabMasiva() {
        VBox layout = new VBox(20); layout.getStyleClass().add("tab-content-area");
        VBox card = new VBox(20); card.getStyleClass().add("card");

        Label title = new Label("Carga desde Excel (CSV)"); title.getStyleClass().add("card-title");
        Label inst = new Label("Estructura requerida: Nombre;Proveedor;Precio;Contenido;Unidad;StockMin;Ubicacion");
        inst.setStyle("-fx-text-fill: #666; -fx-font-style: italic; -fx-font-weight: bold;");

        HBox boxBtn = new HBox(15);
        Button btnPlantilla = new Button("📥 Descargar Plantilla"); btnPlantilla.getStyleClass().add("button-secondary");
        btnPlantilla.setOnAction(e -> descargarPlantilla());

        Button btnSubir = new Button("📤 Subir CSV"); btnSubir.getStyleClass().add("button-primary");
        btnSubir.setOnAction(e -> subirCSV());

        boxBtn.getChildren().addAll(btnPlantilla, btnSubir);

        Button btnGuardar = new Button("💾 Guardar Todo en BD");
        btnGuardar.getStyleClass().add("button-success"); btnGuardar.setMaxWidth(Double.MAX_VALUE);
        btnGuardar.setOnAction(e -> guardarEnBD());

        card.getChildren().addAll(title, inst, boxBtn, new Separator(), new Label("Previsualización (Edita celdas rojas):"), tablaPreview, btnGuardar);
        layout.getChildren().add(card);

        Tab tab = new Tab("Masiva", layout); tab.setClosable(false);
        return tab;
    }

    // --- TABLA EDITABLE Y VALIDACIÓN ---
    private void crearTablaPreview() {
        tablaPreview = new TableView<>(datosCarga);
        tablaPreview.setEditable(true);
        tablaPreview.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // 1. Nombre
        TableColumn<FilaCreacion, String> colNom = new TableColumn<>("Nombre");
        colNom.setCellValueFactory(cell -> cell.getValue().nombreProperty());
        colNom.setCellFactory(TextFieldTableCell.forTableColumn());

        // 2. Proveedor (CORRECCIÓN INTELIGENTE)
        TableColumn<FilaCreacion, String> colProv = new TableColumn<>("Proveedor");
        colProv.setCellValueFactory(cell -> cell.getValue().proveedorProperty());
        // Usa un ComboBox con los proveedores REALES de la BD para corregir errores
        colProv.setCellFactory(ComboBoxTableCell.forTableColumn(listaNombresProveedores));
        colProv.setOnEditCommit(e -> {
            e.getRowValue().setProveedor(e.getNewValue());
            validarFila(e.getRowValue());
            tablaPreview.refresh();
        });

        // 3. Precio (CORREGIDO: eliminado .asObject())
        TableColumn<FilaCreacion, Double> colPrecio = new TableColumn<>("Precio");
        colPrecio.setCellValueFactory(cell -> cell.getValue().precioProperty());
        colPrecio.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));

        // 4. Contenido (CORREGIDO: eliminado .asObject())
        TableColumn<FilaCreacion, Double> colCont = new TableColumn<>("Contenido");
        colCont.setCellValueFactory(cell -> cell.getValue().contenidoProperty());
        colCont.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));

        // 5. Unidad
        TableColumn<FilaCreacion, String> colUni = new TableColumn<>("Unidad");
        colUni.setCellValueFactory(cell -> cell.getValue().unidadProperty());
        colUni.setCellFactory(ComboBoxTableCell.forTableColumn(listaUnidades));

        // 6. Stock Min (CORREGIDO: eliminado .asObject())
        TableColumn<FilaCreacion, Integer> colMin = new TableColumn<>("Min");
        colMin.setCellValueFactory(cell -> cell.getValue().stockMinProperty());
        colMin.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));

        // 7. Ubicacion
        TableColumn<FilaCreacion, String> colUbi = new TableColumn<>("Ubicación");
        colUbi.setCellValueFactory(cell -> cell.getValue().ubicacionProperty());
        colUbi.setCellFactory(TextFieldTableCell.forTableColumn());

        // 8. Estado (Visualización de Error)
        TableColumn<FilaCreacion, String> colEstado = new TableColumn<>("Estado");
        colEstado.setCellValueFactory(cell -> cell.getValue().estadoProperty());
        colEstado.setCellFactory(column -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null); setStyle("");
                } else {
                    setText(item);
                    if (item.startsWith("Error") || item.startsWith("Falta")) {
                        setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    }
                }
            }
        });

        tablaPreview.getColumns().addAll(colNom, colProv, colPrecio, colCont, colUni, colMin, colUbi, colEstado);
    }

    private void validarFila(FilaCreacion f) {
        if (f.getProveedor() == null || f.getProveedor().isEmpty()) {
            f.setEstado("Falta Prov.");
        } else if (!mapaProveedores.containsKey(f.getProveedor())) {
            f.setEstado("Error: Prov. desconocido");
        } else {
            f.setEstado("Listo");
        }
    }

    // --- LÓGICA DE ARCHIVOS ---

    private void descargarPlantilla() {
        FileChooser fc = new FileChooser();
        fc.setInitialFileName("plantilla_insumos.csv");
        File f = fc.showSaveDialog(null);
        if(f!=null) {
            try(PrintWriter pw = new PrintWriter(f, StandardCharsets.UTF_8)) {
                // Estructura exacta solicitada
                pw.println("Nombre;Proveedor;Precio;Contenido;Unidad;StockMin;Ubicacion");
                pw.println("Azucar Blanca;Makro Supermayorista;4.50;1000;Gramo;10;Estante B");
                mostrarAlerta("Info", "Plantilla descargada.");
            } catch(Exception e){}
        }
    }

    private void subirCSV() {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
        File f = fc.showOpenDialog(null);
        if (f != null) {
            datosCarga.clear();
            try (BufferedReader br = new BufferedReader(new FileReader(f, StandardCharsets.UTF_8))) {
                String line; boolean first=true;
                while((line=br.readLine())!=null) {
                    if(first){first=false;continue;}
                    String[] d = line.split(";");

                    // Parseo exacto de 7 columnas
                    if(d.length >= 2) {
                        String nom = getVal(d, 0);
                        String prov = getVal(d, 1);
                        Double prec = parseDouble(getVal(d, 2));
                        Double cont = parseDouble(getVal(d, 3));
                        String uni = getVal(d, 4);
                        Integer min = parseInt(getVal(d, 5));
                        String ubi = getVal(d, 6);

                        FilaCreacion fila = new FilaCreacion(nom, prov, prec, cont, uni, min, ubi);
                        validarFila(fila);
                        datosCarga.add(fila);
                    }
                }
            } catch(Exception e) { mostrarAlerta("Error", "Error leyendo archivo: " + e.getMessage()); }
        }
    }

    private void guardarEnBD() {
        if(datosCarga.isEmpty()) return;

        // Bloquear si hay errores
        if(datosCarga.stream().anyMatch(f -> f.getEstado().startsWith("Error") || f.getEstado().startsWith("Falta"))) {
            mostrarAlerta("Atención", "Corrija las filas en rojo (seleccione el proveedor correcto en la tabla) antes de guardar.");
            return;
        }

        Connection conn = null;
        try {
            conn = ConexionBD.getConnection(); conn.setAutoCommit(false);
            // Insertamos NULL si el campo está vacío
            String sql = "INSERT INTO producto (Tipo_de_Producto, IdProveedor, PrecioUnitario, Contenido, Unidad_de_medida, Stock_Minimo, Ubicacion, Stock) VALUES (?,?,?,?,?,?,?,0)";

            try(PreparedStatement ps = conn.prepareStatement(sql)) {
                for(FilaCreacion f : datosCarga) {
                    ps.setString(1, f.getNombre());
                    ps.setInt(2, mapaProveedores.get(f.getProveedor())); // ID real
                    setDoubleOrNull(ps, 3, f.getPrecio());
                    setDoubleOrNull(ps, 4, f.getContenido());
                    setStrOrNull(ps, 5, f.getUnidad());
                    setIntOrNull(ps, 6, f.getStockMin());
                    setStrOrNull(ps, 7, f.getUbicacion());
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            conn.commit();
            mostrarAlerta("Éxito", "Productos creados correctamente.");
            datosCarga.clear();
        } catch(Exception e) {
            if(conn!=null) try{conn.rollback();}catch(Exception ex){}
            mostrarAlerta("Error BD", e.getMessage());
        } finally {
            if(conn!=null) try{conn.setAutoCommit(true);conn.close();}catch(Exception ex){}
        }
    }

    // --- HELPERS ---
    private void cargarProveedores() {
        listaNombresProveedores.clear(); mapaProveedores.clear();
        try(Connection c=ConexionBD.getConnection(); ResultSet rs=c.createStatement().executeQuery("SELECT IdProveedor, Nombre_comercial FROM proveedores")) {
            while(rs.next()) {
                String n = rs.getString(2);
                listaNombresProveedores.add(n);
                mapaProveedores.put(n, rs.getInt(1));
            }
        } catch(Exception e){}
    }

    private Label crearLabel(String t) { Label l = new Label(t); l.getStyleClass().add("label"); return l; }
    private void mostrarAlerta(String t, String m) { Alert a=new Alert(Alert.AlertType.INFORMATION); a.setTitle(t); a.setContentText(m); a.showAndWait(); }

    private String getVal(String[] d, int i) { return (i<d.length && d[i]!=null && !d[i].trim().isEmpty()) ? d[i].trim() : null; }
    private Double parseDouble(String s) { try{return Double.parseDouble(s);}catch(Exception e){return null;} }
    private Integer parseInt(String s) { try{return Integer.parseInt(s);}catch(Exception e){return null;} }

    private void setStrOrNull(PreparedStatement ps, int i, String v) throws SQLException { if(v==null) ps.setNull(i, Types.VARCHAR); else ps.setString(i, v); }
    private void setDoubleOrNull(PreparedStatement ps, int i, Double v) throws SQLException { if(v==null) ps.setNull(i, Types.REAL); else ps.setDouble(i, v); }
    private void setIntOrNull(PreparedStatement ps, int i, Integer v) throws SQLException { if(v==null) ps.setNull(i, Types.INTEGER); else ps.setInt(i, v); }
}