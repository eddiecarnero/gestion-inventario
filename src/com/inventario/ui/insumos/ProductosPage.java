package com.inventario.ui.insumos;

import com.inventario.config.ConexionBD;
import com.inventario.dao.ProductoDAO;
import com.inventario.model.Producto;
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
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.sql.*;
import java.util.HashMap;

public class ProductosPage extends BorderPane {

    // --- Variables de UI ---
    private final TableView<Producto> tablaProductos;
    private final TextField nombreField;
    private final ComboBox<Proveedor> proveedorCombo;
    private final TextField precioField;
    private final TextField stockMinField;
    private final ComboBox<String> unidadCombo;
    private final TextField ubicacionField;

    // --- NUEVO CAMPO: Contenido (Factor de conversión) ---
    private final TextField contenidoField;

    private final Button guardarButton;
    private final Button limpiarButton;
    private final Label idProductoLabel;

    // --- Listas de Datos ---
    private final ObservableList<Producto> listaProductos = FXCollections.observableArrayList();
    private final ObservableList<Proveedor> listaProveedores = FXCollections.observableArrayList();
    private final HashMap<Integer, Proveedor> mapaProveedores = new HashMap<>();

    // --- ESTILOS ---
    private static final String CSS_STYLES = """
        .root { -fx-background-color: #FDF8F0; -fx-font-family: 'Segoe UI'; }
        .header-title { -fx-font-size: 2.2em; -fx-font-weight: bold; -fx-text-fill: #333333; }
        .header-description { -fx-font-size: 1.1em; -fx-text-fill: #555555; }
        .card { -fx-background-color: white; -fx-border-color: #E0E0E0; -fx-border-width: 1; -fx-border-radius: 8; -fx-padding: 20px; }
        .card-title { -fx-font-size: 1.4em; -fx-font-weight: bold; -fx-text-fill: #333333; }
        .label { -fx-font-size: 1.05em; -fx-font-weight: 500; -fx-text-fill: #333333; }
        .combo-box, .text-field { -fx-font-size: 1.05em; -fx-pref-height: 38px; -fx-border-color: #CCCCCC; -fx-border-radius: 5; }
        .button-primary { -fx-background-color: #4A90E2; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 1.1em; -fx-pref-height: 40px; -fx-cursor: hand; -fx-background-radius: 5; }
        .button-secondary { -fx-background-color: #777777; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 1.1em; -fx-pref-height: 40px; -fx-cursor: hand; -fx-background-radius: 5; }
        .table-view .column-header { -fx-background-color: #F9FAFB; -fx-font-weight: bold; -fx-font-size: 1.05em; }
    """;

    public ProductosPage() {
        this.getStylesheets().add("data:text/css," + CSS_STYLES.replace("\n", ""));
        this.getStyleClass().add("root");
        setPadding(new Insets(30, 40, 30, 40));

        // Inicializar campos
        tablaProductos = new TableView<>();
        nombreField = new TextField();
        proveedorCombo = new ComboBox<>();
        precioField = new TextField();
        stockMinField = new TextField();

        unidadCombo = new ComboBox<>();
        unidadCombo.getItems().addAll("Unidad", "Kg", "Gramo", "Litro", "ml", "Porción");
        unidadCombo.getSelectionModel().selectFirst();

        ubicacionField = new TextField();

        // Nuevo campo inicializado en "1" por defecto
        contenidoField = new TextField("1");
        contenidoField.setPromptText("Ej: 1000 para 1kg");

        guardarButton = new Button("💾 Guardar");
        limpiarButton = new Button("✨ Limpiar");
        idProductoLabel = new Label();

        VBox mainContent = new VBox(20);
        //Node header = crearHeader();
        Node formularioCard = crearFormulario();
        Node tablaCard = crearTablaProductos();

        mainContent.getChildren().addAll(formularioCard, tablaCard);
        setCenter(mainContent);

        cargarProveedores();
        cargarDatosTabla();

        tablaProductos.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                popularFormulario(newSelection);
            }
        });

        limpiarButton.setOnAction(e -> limpiarFormulario());
        guardarButton.setOnAction(e -> guardarProducto());
    }

    /*private Node crearHeader() {
        VBox headerBox = new VBox(5);
        Label header = new Label("Gestión de Productos"); header.getStyleClass().add("header-title");
        Label description = new Label("Definir los productos maestros del inventario (insumos)"); description.getStyleClass().add("header-description");
        headerBox.getChildren().addAll(header, description);
        return headerBox;
    }*/

    private Node crearFormulario() {
        VBox card = new VBox(15); card.getStyleClass().add("card");
        Label cardTitle = new Label("Detalles del Producto"); cardTitle.getStyleClass().add("card-title");
        card.getChildren().add(cardTitle);

        GridPane grid = new GridPane(); grid.setHgap(20); grid.setVgap(15);

        // Fila 1
        grid.add(crearCampo("Nombre Producto", nombreField), 0, 0);
        proveedorCombo.setConverter(new ProveedorStringConverter());
        proveedorCombo.setItems(listaProveedores);
        proveedorCombo.setMaxWidth(Double.MAX_VALUE);
        grid.add(crearCampo("Proveedor", proveedorCombo), 1, 0);
        grid.add(crearCampo("Precio Compra ($)", precioField), 2, 0);

        // Fila 2
        grid.add(crearCampo("Contenido (Cant. por Envase)", contenidoField), 0, 1);
        unidadCombo.setMaxWidth(Double.MAX_VALUE);
        grid.add(crearCampo("Unidad de Medida", unidadCombo), 1, 1);
        grid.add(crearCampo("Stock Mínimo", stockMinField), 2, 1);

        // Fila 3
        grid.add(crearCampo("Ubicación (Almacén)", ubicacionField), 0, 2);

        // Columnas
        ColumnConstraints col1 = new ColumnConstraints(); col1.setPercentWidth(34);
        ColumnConstraints col2 = new ColumnConstraints(); col2.setPercentWidth(33);
        ColumnConstraints col3 = new ColumnConstraints(); col3.setPercentWidth(33);
        grid.getColumnConstraints().addAll(col1, col2, col3);

        // Botones
        guardarButton.getStyleClass().add("button-primary");
        limpiarButton.getStyleClass().add("button-secondary");
        HBox buttonBox = new HBox(10, guardarButton, limpiarButton);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        card.getChildren().addAll(grid, buttonBox);
        return card;
    }

    private Node crearTablaProductos() {
        VBox card = new VBox(15); card.getStyleClass().add("card");
        Label cardTitle = new Label("Listado de Productos en Inventario"); cardTitle.getStyleClass().add("card-title");
        configurarTabla();
        card.getChildren().addAll(cardTitle, tablaProductos);
        VBox.setVgrow(tablaProductos, Priority.ALWAYS);
        return card;
    }

    private void configurarTabla() {
        tablaProductos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tablaProductos.setItems(listaProductos);

        // OJO: "tipoDeProducto" busca getTipoDeProducto()
        TableColumn<Producto, String> c1 = new TableColumn<>("Nombre");
        c1.setCellValueFactory(new PropertyValueFactory<>("tipoDeProducto"));

        TableColumn<Producto, String> c2 = new TableColumn<>("Proveedor");
        c2.setCellValueFactory(new PropertyValueFactory<>("proveedorNombre"));

        TableColumn<Producto, Double> c3 = new TableColumn<>("Precio Compra");
        c3.setCellValueFactory(new PropertyValueFactory<>("precioUnitario"));
        c3.setCellFactory(tc -> new TableCell<>(){
            @Override protected void updateItem(Double p, boolean e){
                super.updateItem(p,e);
                setText(e?null:String.format("$%.2f", p));
            }
        });

        TableColumn<Producto, Double> c4 = new TableColumn<>("Contenido");
        c4.setCellValueFactory(new PropertyValueFactory<>("contenido"));

        // OJO: "unidadDeMedida" busca getUnidadDeMedida()
        TableColumn<Producto, String> c5 = new TableColumn<>("Unidad");
        c5.setCellValueFactory(new PropertyValueFactory<>("unidadDeMedida"));

        TableColumn<Producto, Integer> c6 = new TableColumn<>("Stock Mín.");
        c6.setCellValueFactory(new PropertyValueFactory<>("stockMinimo"));

        tablaProductos.getColumns().setAll(c1, c2, c3, c4, c5, c6);
        tablaProductos.setPlaceholder(new Label("No hay productos definidos."));
    }

    // --- LÓGICA DE CARGA Y GUARDADO ---

    private void cargarProveedores() {
        listaProveedores.clear(); mapaProveedores.clear();
        try (Connection c = ConexionBD.getConnection(); Statement s = c.createStatement(); ResultSet rs = s.executeQuery("SELECT IdProveedor, Nombre_comercial FROM proveedores")) {
            while (rs.next()) {
                Proveedor p = new Proveedor(rs.getInt("IdProveedor"), rs.getString("Nombre_comercial"));
                listaProveedores.add(p); mapaProveedores.put(p.getId(), p);
            }
        } catch (SQLException e) { mostrarAlerta("Error", "Error cargando proveedores."); }
    }

    private void cargarDatosTabla() {
        listaProductos.clear();
        String sql = "SELECT IdProducto, Tipo_de_Producto, Stock_Minimo, Unidad_de_medida, Ubicacion, IdProveedor, PrecioUnitario, Contenido FROM producto";

        try (Connection conn = ConexionBD.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Producto p = new Producto(); // Constructor vacío

                // Usamos los setters correctos del modelo
                p.setIdProducto(rs.getInt("IdProducto"));
                p.setTipoDeProducto(rs.getString("Tipo_de_Producto"));
                p.setStockMinimo(rs.getInt("Stock_Minimo"));
                p.setUnidadDeMedida(rs.getString("Unidad_de_medida"));
                p.setUbicacion(rs.getString("Ubicacion"));
                p.setIdProveedor(rs.getInt("IdProveedor"));
                p.setPrecioUnitario(rs.getDouble("PrecioUnitario"));
                p.setContenido(rs.getDouble("Contenido"));

                // Mapear nombre del proveedor si existe en el mapa
                if (mapaProveedores.containsKey(p.getIdProveedor())) {
                    p.setProveedorNombre(mapaProveedores.get(p.getIdProveedor()).getNombre());
                }

                listaProductos.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Error cargando productos.");
        }
    }

    private void popularFormulario(Producto p) {
        // Usamos getIdProducto() en vez de getId()
        idProductoLabel.setText(String.valueOf(p.getIdProducto()));

        // Usamos getTipoDeProducto() en vez de getNombre()
        nombreField.setText(p.getTipoDeProducto());

        precioField.setText(String.valueOf(p.getPrecioUnitario()));
        stockMinField.setText(String.valueOf(p.getStockMinimo()));

        // Usamos getUnidadDeMedida() en vez de getUnidad()
        unidadCombo.setValue(p.getUnidadDeMedida());

        ubicacionField.setText(p.getUbicacion());
        contenidoField.setText(String.valueOf(p.getContenido()));

        proveedorCombo.setValue(mapaProveedores.get(p.getIdProveedor()));
        guardarButton.setText("💾 Actualizar");
    }

    private void limpiarFormulario() {
        idProductoLabel.setText(""); nombreField.clear(); precioField.clear(); stockMinField.clear();
        unidadCombo.getSelectionModel().selectFirst(); ubicacionField.clear();
        contenidoField.setText("1"); // Reset a 1
        proveedorCombo.getSelectionModel().clearSelection();
        guardarButton.setText("💾 Guardar Nuevo"); tablaProductos.getSelectionModel().clearSelection();
    }

    private void guardarProducto() {
        if (nombreField.getText().isEmpty() || precioField.getText().isEmpty() || proveedorCombo.getValue() == null) {
            mostrarAlerta("Error", "Campos obligatorios vacíos."); return;
        }
        try {
            // 1. Recoger datos del formulario
            String nombre = nombreField.getText();
            // Nota: Asegúrate de que Proveedor y Unidad tengan valores válidos
            int idProv = proveedorCombo.getValue() != null ? proveedorCombo.getValue().getId() : 0;
            double precio = Double.parseDouble(precioField.getText());
            int min = stockMinField.getText().isEmpty() ? 0 : Integer.parseInt(stockMinField.getText());
            String uni = unidadCombo.getValue();
            String ubi = ubicacionField.getText();

            double cont = 1.0;
            try { cont = Double.parseDouble(contenidoField.getText()); } catch(Exception ex){}
            if (cont <= 0) cont = 1.0;

            // 2. Crear el objeto Producto (Usando tu modelo)
            // Si idProductoLabel tiene texto, es edición (ID real). Si no, es nuevo (0).
            int idProd = idProductoLabel.getText().isEmpty() ? 0 : Integer.parseInt(idProductoLabel.getText());
            boolean esNuevo = (idProd == 0);

            // Ojo: Ajusta el constructor según tu modelo 'Producto.java'
            Producto producto = new Producto();
            producto.setIdProducto(idProd);
            producto.setTipoDeProducto(nombre);
            producto.setIdProveedor(idProv);
            producto.setPrecioUnitario(precio);
            producto.setStockMinimo(min);
            producto.setUnidadDeMedida(uni);
            producto.setUbicacion(ubi);
            producto.setContenido(cont);

            // 3. Llamar al DAO
            ProductoDAO dao = new ProductoDAO();
            if (dao.guardarProducto(producto, esNuevo)) {
                mostrarAlerta("Éxito", esNuevo ? "Producto creado." : "Producto actualizado.");
                limpiarFormulario();
                cargarDatosTabla();
            } else {
                mostrarAlerta("Error", "No se pudo guardar en la base de datos.");
            }

        } catch (NumberFormatException e) {
            mostrarAlerta("Error", "Por favor revise los campos numéricos (Precio, Stock, Contenido).");
        }
    }

    private void accionInsertar(String n, int ip, double p, int min, String u, String ub, double cont) {
        String sql = "INSERT INTO producto (Tipo_de_Producto, Stock_Minimo, Unidad_de_medida, Ubicacion, IdProveedor, PrecioUnitario, Contenido) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = ConexionBD.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, n); ps.setInt(2, min); ps.setString(3, u); ps.setString(4, ub); ps.setInt(5, ip); ps.setDouble(6, p); ps.setDouble(7, cont);
            if (ps.executeUpdate() > 0) { mostrarAlerta("Éxito", "Producto creado."); cargarDatosTabla(); limpiarFormulario(); }
        } catch (SQLException e) { mostrarAlerta("Error", e.getMessage()); }
    }

    private void accionActualizar(int id, String n, int ip, double p, int min, String u, String ub, double cont) {
        String sql = "UPDATE producto SET Tipo_de_Producto=?, Stock_Minimo=?, Unidad_de_medida=?, Ubicacion=?, IdProveedor=?, PrecioUnitario=?, Contenido=? WHERE IdProducto=?";
        try (Connection c = ConexionBD.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, n); ps.setInt(2, min); ps.setString(3, u); ps.setString(4, ub); ps.setInt(5, ip); ps.setDouble(6, p); ps.setDouble(7, cont); ps.setInt(8, id);
            if (ps.executeUpdate() > 0) { mostrarAlerta("Éxito", "Actualizado."); cargarDatosTabla(); limpiarFormulario(); }
        } catch (SQLException e) { mostrarAlerta("Error", e.getMessage()); }
    }

    // --- Helpers y Clases ---
    private VBox crearCampo(String l, Node c) { VBox v = new VBox(5); Label lbl = new Label(l); lbl.getStyleClass().add("label"); v.getChildren().addAll(lbl, c); return v; }
    private void mostrarAlerta(String t, String m) { Alert a = new Alert(Alert.AlertType.INFORMATION); if(t.startsWith("Error")) a.setAlertType(Alert.AlertType.ERROR); a.setTitle(t); a.setContentText(m); a.showAndWait(); }

    public static class Proveedor {
        private final int id; private final String nombre; public Proveedor(int id, String n) { this.id=id; this.nombre=n; }
        public int getId(){return id;} public String getNombre(){return nombre;}
    }
    private static class ProveedorStringConverter extends StringConverter<Proveedor> {
        @Override public String toString(Proveedor p){return p==null?null:p.getNombre();}
        @Override public Proveedor fromString(String s){return null;}
    }


    public static class TestApp extends Application { @Override public void start(Stage s) { s.setScene(new Scene(new ProductosPage(), 1000, 800)); s.show(); } }
    public static void main(String[] args) { Application.launch(TestApp.class, args); }
}