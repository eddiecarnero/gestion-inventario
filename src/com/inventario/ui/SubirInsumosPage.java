package com.inventario.ui;

import com.inventario.config.ConexionBD;
import com.inventario.dao.IngresoInsumosDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SubirInsumosPage extends BorderPane {

    // --- ESTILOS CSS ---
    private static final String CSS_STYLES = """
        .root { -fx-background-color: #FDF8F0; -fx-font-family: 'Segoe UI'; }
        .header-title { -fx-font-size: 2.2em; -fx-font-weight: bold; -fx-text-fill: #333333; }
        .header-description { -fx-font-size: 1.1em; -fx-text-fill: #555555; }
        .tab-content-area { -fx-padding: 20 0 0 0; }
        .tab-pane .tab-header-area .tab-header-background { -fx-background-color: transparent; }
        .tab-pane .tab { -fx-background-color: transparent; -fx-border-color: transparent; -fx-padding: 8 15 8 15; -fx-font-size: 1.1em; }
        .tab-pane .tab:selected { -fx-background-color: transparent; -fx-border-color: #4A90E2; -fx-border-width: 0 0 3 0; -fx-text-fill: #4A90E2; -fx-font-weight: bold; }
        .card { -fx-background-color: white; -fx-border-color: #E0E0E0; -fx-border-width: 1; -fx-border-radius: 8; -fx-padding: 20px; }
        .card-title { -fx-font-size: 1.4em; -fx-font-weight: bold; -fx-text-fill: #333333; }
        .label { -fx-font-size: 1.05em; -fx-font-weight: 500; -fx-text-fill: #333333; }
        .combo-box, .text-field, .date-picker { -fx-font-size: 1.05em; -fx-pref-height: 38px; -fx-border-color: #CCCCCC; -fx-border-radius: 5; }
        .button-primary { -fx-background-color: #4A90E2; -fx-text-fill: white; -fx-font-weight: bold; -fx-pref-height: 40px; -fx-cursor: hand; -fx-background-radius: 5; }
        .button-secondary { -fx-background-color: #777777; -fx-text-fill: white; -fx-font-weight: bold; -fx-pref-height: 38px; -fx-cursor: hand; -fx-background-radius: 5; }
        .button-success { -fx-background-color: #22C55E; -fx-text-fill: white; -fx-font-weight: bold; -fx-pref-height: 38px; -fx-cursor: hand; -fx-background-radius: 5; }
        .button-add { -fx-background-color: #F59E0B; -fx-text-fill: white; -fx-font-weight: bold; -fx-pref-height: 38px; -fx-cursor: hand; -fx-background-radius: 5; }
        .table-view .column-header { -fx-background-color: #F9FAFB; -fx-font-weight: bold; }
    """;

    private final IngresoInsumosDAO dao = new IngresoInsumosDAO();
    private final ObservableList<ProductoInfo> productosBD = FXCollections.observableArrayList();
    private final ObservableList<ProveedorInfo> proveedoresBD = FXCollections.observableArrayList();

    // UI Masiva
    private TableView<FilaInsumo> tablaPreview;
    private ObservableList<FilaInsumo> datosCarga = FXCollections.observableArrayList();
    private ComboBox<ProveedorInfo> cmbProveedorMasivo;

    public SubirInsumosPage() {
        // Cargar Estilos
        this.getStylesheets().add("data:text/css," + CSS_STYLES.replace("\n", ""));
        this.getStyleClass().add("root");
        setPadding(new Insets(30, 40, 30, 40));

        // Header
        VBox headerBox = new VBox(5);
        Label titulo = new Label("Ingreso de Insumos (Almacén 1)");
        titulo.getStyleClass().add("header-title");
        Label subtitulo = new Label("Registro manual rápido o carga masiva de inventario inicial");
        subtitulo.getStyleClass().add("header-description");
        headerBox.getChildren().addAll(titulo, subtitulo);
        setTop(headerBox);
        BorderPane.setMargin(headerBox, new Insets(0,0,20,0));

        cargarDatosBD();

        TabPane tabs = new TabPane();
        tabs.getStyleClass().add("tab-pane");
        tabs.getTabs().addAll(crearTabManual(), crearTabMasiva());

        setCenter(tabs);
    }

    // --- TAB 1: MANUAL (Carrito Rápido) ---
    private Tab crearTabManual() {
        VBox layout = new VBox(20);
        layout.getStyleClass().add("tab-content-area");

        VBox card = new VBox(20);
        card.getStyleClass().add("card");

        Label cardTitle = new Label("Registro Rápido");
        cardTitle.getStyleClass().add("card-title");

        // Formulario en Grid para mejor alineación
        GridPane grid = new GridPane();
        grid.setHgap(15); grid.setVgap(15);

        ComboBox<ProveedorInfo> cmbProv = new ComboBox<>(proveedoresBD);
        cmbProv.setPromptText("Seleccione Proveedor");
        cmbProv.setMaxWidth(Double.MAX_VALUE);

        ComboBox<ProductoInfo> cmbProd = new ComboBox<>(productosBD);
        cmbProd.setPromptText("Buscar Insumo");
        cmbProd.setEditable(true);
        cmbProd.setMaxWidth(Double.MAX_VALUE);

        TextField txtCant = new TextField(); txtCant.setPromptText("Cantidad");
        TextField txtCosto = new TextField(); txtCosto.setPromptText("Costo Unit. ($)");
        DatePicker dateVenc = new DatePicker(); dateVenc.setPromptText("F. Vencimiento (Opcional)");
        dateVenc.setMaxWidth(Double.MAX_VALUE);

        // Añadir al grid
        grid.add(crearLabel("Proveedor:"), 0, 0); grid.add(cmbProv, 1, 0, 3, 1);
        grid.add(crearLabel("Producto:"), 0, 1);  grid.add(cmbProd, 1, 1, 3, 1);

        grid.add(crearLabel("Cantidad:"), 0, 2);  grid.add(txtCant, 1, 2);
        grid.add(crearLabel("Costo:"), 2, 2);     grid.add(txtCosto, 3, 2);

        grid.add(crearLabel("Vencimiento:"), 0, 3); grid.add(dateVenc, 1, 3, 3, 1);

        Button btnAdd = new Button("⬇ Agregar a Lista");
        btnAdd.getStyleClass().add("button-add"); // Naranja
        btnAdd.setMaxWidth(Double.MAX_VALUE);
        btnAdd.setOnAction(e -> {
            try {
                if(cmbProd.getValue() == null) return;
                double cant = Double.parseDouble(txtCant.getText());
                double costo = Double.parseDouble(txtCosto.getText());
                datosCarga.add(new FilaInsumo(
                        cmbProd.getValue().nombre, cant, costo,
                        dateVenc.getValue() != null ? dateVenc.getValue().toString() : "",
                        "Listo", cmbProd.getValue().id
                ));
                txtCant.clear(); txtCosto.clear(); dateVenc.setValue(null);
            } catch(Exception ex) { mostrarAlerta("Error", "Datos numéricos inválidos"); }
        });

        grid.add(btnAdd, 0, 4, 4, 1);

        // Ajuste de columnas del grid
        ColumnConstraints cLabels = new ColumnConstraints(); cLabels.setPercentWidth(15);
        ColumnConstraints cFields = new ColumnConstraints(); cFields.setPercentWidth(35);
        grid.getColumnConstraints().addAll(cLabels, cFields, cLabels, cFields);

        // Tabla de items a ingresar
        TableView<FilaInsumo> tablaManual = new TableView<>(datosCarga);
        tablaManual.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(tablaManual, Priority.ALWAYS);
        configurarColumnasTabla(tablaManual);

        Button btnProcesar = new Button("💾 Guardar Ingreso");
        btnProcesar.getStyleClass().add("button-success"); // Verde
        btnProcesar.setMaxWidth(Double.MAX_VALUE);
        btnProcesar.setOnAction(e -> procesarIngreso(cmbProv.getValue(), "Ingreso Manual Rápido"));

        card.getChildren().addAll(cardTitle, grid, new Separator(), tablaManual, btnProcesar);
        layout.getChildren().add(card);

        Tab tab = new Tab("Ingreso Manual", layout);
        tab.setClosable(false);
        return tab;
    }

    // --- TAB 2: MASIVA (Excel/CSV) ---
    private Tab crearTabMasiva() {
        VBox layout = new VBox(20);
        layout.getStyleClass().add("tab-content-area");

        VBox card = new VBox(20);
        card.getStyleClass().add("card");

        Label cardTitle = new Label("Carga Masiva desde Excel (CSV)");
        cardTitle.getStyleClass().add("card-title");
        Label instrucciones = new Label("Instrucciones: Columnas requeridas: NombreInsumo; Cantidad; CostoUnitario; FechaVencimiento(YYYY-MM-DD)");
        instrucciones.setStyle("-fx-text-fill: #666; -fx-font-style: italic;");

        cmbProveedorMasivo = new ComboBox<>(proveedoresBD);
        cmbProveedorMasivo.setPromptText("Proveedor para este lote");
        cmbProveedorMasivo.setMaxWidth(Double.MAX_VALUE);

        HBox botones = new HBox(15);
        botones.setAlignment(Pos.CENTER_LEFT);

        Button btnPlantilla = new Button("📥 Descargar Plantilla");
        btnPlantilla.getStyleClass().add("button-secondary");
        btnPlantilla.setOnAction(e -> descargarPlantilla());

        Button btnSubir = new Button("📤 Subir CSV");
        btnSubir.getStyleClass().add("button-primary");
        btnSubir.setOnAction(e -> subirArchivo());

        Button btnProcesar = new Button("💾 Procesar Todo");
        btnProcesar.getStyleClass().add("button-success");
        btnProcesar.setOnAction(e -> procesarIngreso(cmbProveedorMasivo.getValue(), "Carga Masiva CSV"));

        botones.getChildren().addAll(btnPlantilla, btnSubir, btnProcesar);

        tablaPreview = new TableView<>(datosCarga);
        configurarColumnasTabla(tablaPreview);
        VBox.setVgrow(tablaPreview, Priority.ALWAYS);

        card.getChildren().addAll(cardTitle, instrucciones, cmbProveedorMasivo, botones, tablaPreview);
        layout.getChildren().add(card);

        Tab tab = new Tab("Carga Masiva", layout);
        tab.setClosable(false);
        return tab;
    }

    private void configurarColumnasTabla(TableView<FilaInsumo> t) {
        t.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<FilaInsumo, String> c1 = new TableColumn<>("Insumo"); c1.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        TableColumn<FilaInsumo, Double> c2 = new TableColumn<>("Cant."); c2.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        TableColumn<FilaInsumo, Double> c3 = new TableColumn<>("Costo"); c3.setCellValueFactory(new PropertyValueFactory<>("costo"));
        TableColumn<FilaInsumo, String> c4 = new TableColumn<>("Vence"); c4.setCellValueFactory(new PropertyValueFactory<>("vencimiento"));

        TableColumn<FilaInsumo, String> c5 = new TableColumn<>("Estado");
        c5.setCellValueFactory(new PropertyValueFactory<>("estado"));
        // Colorear estado
        c5.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.contains("No existe") || item.contains("Error")) {
                        setStyle("-fx-text-fill: #EF4444; -fx-font-weight: bold;");
                    } else if (item.equals("Listo")) {
                        setStyle("-fx-text-fill: #F59E0B; -fx-font-weight: bold;");
                    } else if (item.equals("Registrado")) {
                        setStyle("-fx-text-fill: #22C55E; -fx-font-weight: bold;");
                    }
                }
            }
        });

        t.getColumns().addAll(c1, c2, c3, c4, c5);
    }

    private Label crearLabel(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("label");
        return l;
    }

    // --- LOGICA ---
    private void procesarIngreso(ProveedorInfo prov, String obs) {
        if (prov == null) { mostrarAlerta("Error", "Seleccione un proveedor."); return; }
        if (datosCarga.isEmpty()) { mostrarAlerta("Error", "La lista está vacía."); return; }

        List<IngresoInsumosDAO.ItemIngreso> itemsDAO = new ArrayList<>();
        boolean hayErrores = false;

        for (FilaInsumo fila : datosCarga) {
            if (fila.idProducto == null) {
                fila.setEstado("ID no encontrado");
                hayErrores = true;
                continue;
            }
            LocalDate venc = null;
            try { if(!fila.vencimiento.isEmpty()) venc = LocalDate.parse(fila.vencimiento); } catch(Exception e){}

            itemsDAO.add(new IngresoInsumosDAO.ItemIngreso(fila.idProducto, fila.cantidad, fila.costo, venc));
        }

        if (hayErrores) {
            mostrarAlerta("Atención", "Hay items con errores (sin ID). Corríjalos o bórrelos.");
            tablaPreview.refresh();
            return;
        }

        if (dao.registrarIngresoMasivo(prov.id, itemsDAO, obs)) {
            mostrarAlerta("Éxito", "Ingreso registrado correctamente.");
            datosCarga.clear();
        } else {
            mostrarAlerta("Error", "Falló el registro en BD.");
        }
    }

    private void subirArchivo() {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
        File f = fc.showOpenDialog(null);
        if (f != null) {
            datosCarga.clear();
            try (BufferedReader br = new BufferedReader(new FileReader(f, StandardCharsets.UTF_8))) {
                String line; boolean first=true;
                while((line=br.readLine())!=null) {
                    if(first){first=false; continue;}
                    String[] d = line.split(";");
                    if(d.length>=3) {
                        String nom = d[0].trim();
                        double cant = Double.parseDouble(d[1].trim());
                        double cost = Double.parseDouble(d[2].trim());
                        String venc = (d.length>3) ? d[3].trim() : "";

                        Integer id = dao.obtenerIdProducto(nom);
                        String est = (id!=null) ? "Listo" : "No existe";
                        datosCarga.add(new FilaInsumo(nom, cant, cost, venc, est, id));
                    }
                }
            } catch(Exception e) { mostrarAlerta("Error", e.getMessage()); }
        }
    }

    private void descargarPlantilla() {
        FileChooser fc = new FileChooser();
        fc.setInitialFileName("plantilla_insumos.csv");
        File f = fc.showSaveDialog(null);
        if(f!=null) {
            try(PrintWriter pw = new PrintWriter(f, StandardCharsets.UTF_8)) {
                pw.println("NombreInsumo;Cantidad;CostoUnitario;FechaVencimiento(YYYY-MM-DD)");
                pw.println("Harina Especial;50;45.50;2025-12-31");
                mostrarAlerta("Éxito", "Plantilla guardada.");
            } catch(Exception e){}
        }
    }

    private void cargarDatosBD() {
        try(Connection c = ConexionBD.getConnection(); java.sql.Statement s = c.createStatement()) {
            ResultSet rs = s.executeQuery("SELECT IdProducto, Tipo_de_Producto FROM producto");
            while(rs.next()) productosBD.add(new ProductoInfo(rs.getInt(1), rs.getString(2)));

            rs = s.executeQuery("SELECT IdProveedor, Nombre_comercial FROM proveedores");
            while(rs.next()) proveedoresBD.add(new ProveedorInfo(rs.getInt(1), rs.getString(2)));
        } catch(Exception e){}
    }

    private void mostrarAlerta(String t, String m) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        if(t.startsWith("Error")) a.setAlertType(Alert.AlertType.ERROR);
        a.setTitle(t); a.setContentText(m); a.showAndWait();
    }

    // --- CLASES AUX ---
    public static class FilaInsumo {
        String nombre; double cantidad, costo; String vencimiento, estado; Integer idProducto;
        public FilaInsumo(String n, double c, double co, String v, String e, Integer id) {
            nombre=n; cantidad=c; costo=co; vencimiento=v; estado=e; idProducto=id;
        }
        public String getNombre(){return nombre;} public double getCantidad(){return cantidad;}
        public double getCosto(){return costo;} public String getVencimiento(){return vencimiento;}
        public String getEstado(){return estado;} public void setEstado(String e){estado=e;}
    }
    private static class ProductoInfo { int id; String nombre; public ProductoInfo(int i, String n){id=i;nombre=n;} @Override public String toString(){return nombre;} }
    private static class ProveedorInfo { int id; String nombre; public ProveedorInfo(int i, String n){id=i;nombre=n;} @Override public String toString(){return nombre;} }
}