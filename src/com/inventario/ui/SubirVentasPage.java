package com.inventario.ui;

import com.inventario.config.ConexionBD;
import com.inventario.dao.VentaDAO;
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

public class SubirVentasPage extends BorderPane {

    // --- ESTILOS CSS ---
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
                .combo-box, .text-field, .date-picker { -fx-font-size: 1.05em; -fx-pref-height: 38px; -fx-border-color: #CCCCCC; -fx-border-radius: 5; }
                .button-primary { -fx-background-color: #4A90E2; -fx-text-fill: white; -fx-font-weight: bold; -fx-pref-height: 40px; -fx-background-radius: 5; -fx-cursor: hand; }
                .button-secondary { -fx-background-color: #777777; -fx-text-fill: white; -fx-font-weight: bold; -fx-pref-height: 38px; -fx-cursor: hand; -fx-background-radius: 5; }
                .button-success { -fx-background-color: #22C55E; -fx-text-fill: white; -fx-font-weight: bold; -fx-pref-height: 38px; -fx-cursor: hand; -fx-background-radius: 5; }
                .table-view .column-header { -fx-background-color: #F9FAFB; -fx-font-weight: bold; }
            """;

    private final VentaDAO ventaDAO = new VentaDAO();
    private final ObservableList<ProductoVenta> productosDisponibles = FXCollections.observableArrayList();
    private final ObservableList<FilaCarga> datosCarga = FXCollections.observableArrayList();
    // UI Manual
    private ComboBox<ProductoVenta> comboProducto;
    private TextField txtCantidad, txtCliente;
    private DatePicker dateVenta;
    private Label lblPrecio, lblTotal;
    // UI Masiva
    private TableView<FilaCarga> tablaPreview;

    public SubirVentasPage() {
        // Cargar estilos
        this.getStylesheets().add("data:text/css," + CSS_STYLES.replace("\n", ""));
        this.getStyleClass().add("root");
        setPadding(new Insets(30, 40, 30, 40));

        // Título Principal
        VBox headerBox = new VBox(5);
        Label titulo = new Label("Registro de Ventas");
        titulo.getStyleClass().add("header-title");
        Label subtitulo = new Label("Ingreso manual o carga masiva de historial de ventas");
        subtitulo.setStyle("-fx-font-size: 1.1em; -fx-text-fill: #555555;");
        headerBox.getChildren().addAll(titulo, subtitulo);
        setTop(headerBox);
        BorderPane.setMargin(headerBox, new Insets(0, 0, 20, 0));

        // Tabs
        TabPane tabs = new TabPane();
        tabs.getStyleClass().add("tab-pane");
        tabs.getTabs().addAll(crearTabManual(), crearTabMasiva());

        setCenter(tabs);

        cargarProductos();
    }

    // --- PESTAÑA 1: VENTA MANUAL ---
    private Tab crearTabManual() {
        VBox layout = new VBox(20);
        layout.getStyleClass().add("tab-content-area");

        VBox card = new VBox(20);
        card.getStyleClass().add("card");

        Label cardTitle = new Label("Ingreso Manual");
        cardTitle.getStyleClass().add("card-title");

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(15);

        comboProducto = new ComboBox<>();
        comboProducto.setPromptText("Seleccione Producto");
        comboProducto.setMaxWidth(Double.MAX_VALUE); // Para que llene la celda
        comboProducto.setItems(productosDisponibles);
        comboProducto.setOnAction(e -> actualizarCalculos());

        txtCantidad = new TextField();
        txtCantidad.setPromptText("Cantidad");
        txtCantidad.textProperty().addListener((o, old, val) -> actualizarCalculos());

        lblPrecio = new Label("Precio Unit.: $0.00");
        lblPrecio.getStyleClass().add("label");

        lblTotal = new Label("Total: $0.00");
        lblTotal.setStyle("-fx-font-size: 1.2em; -fx-font-weight: bold; -fx-text-fill: #333;");

        txtCliente = new TextField("Público General");
        dateVenta = new DatePicker(LocalDate.now());
        dateVenta.setMaxWidth(Double.MAX_VALUE);

        // Añadir al grid usando helper o directamente
        grid.add(crearLabel("Producto:"), 0, 0);
        grid.add(comboProducto, 1, 0);
        grid.add(crearLabel("Fecha:"), 0, 1);
        grid.add(dateVenta, 1, 1);
        grid.add(crearLabel("Cliente:"), 0, 2);
        grid.add(txtCliente, 1, 2);
        grid.add(crearLabel("Cantidad:"), 0, 3);
        grid.add(txtCantidad, 1, 3);
        grid.add(lblPrecio, 1, 4);
        grid.add(lblTotal, 1, 5);

        // Ajuste columnas
        ColumnConstraints col = new ColumnConstraints();
        col.setPercentWidth(30);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(70);
        grid.getColumnConstraints().addAll(col, col2);

        Button btnGuardar = new Button("Registrar Venta");
        btnGuardar.getStyleClass().add("button-success"); // Verde
        btnGuardar.setMaxWidth(Double.MAX_VALUE);
        btnGuardar.setOnAction(e -> registrarManual());

        card.getChildren().addAll(cardTitle, grid, new Separator(), btnGuardar);
        layout.getChildren().add(card);

        Tab tab = new Tab("Ingreso Manual", layout);
        tab.setClosable(false);
        return tab;
    }

    private Label crearLabel(String texto) {
        Label l = new Label(texto);
        l.getStyleClass().add("label");
        return l;
    }

    private void actualizarCalculos() {
        try {
            ProductoVenta p = comboProducto.getValue();
            if (p != null) {
                lblPrecio.setText(String.format("Precio Unit.: $%.2f", p.precio));
                int cant = Integer.parseInt(txtCantidad.getText());
                lblTotal.setText(String.format("Total: $%.2f", p.precio * cant));
            }
        } catch (NumberFormatException e) {
            lblTotal.setText("Total: $0.00");
        }
    }

    private void registrarManual() {
        try {
            ProductoVenta p = comboProducto.getValue();
            int cant = Integer.parseInt(txtCantidad.getText());
            if (p == null || cant <= 0) throw new Exception("Datos incompletos");

            boolean ok = ventaDAO.registrarVenta(p.id, cant, p.precio, txtCliente.getText(), dateVenta.getValue());
            if (ok) {
                mostrarAlerta("Éxito", "Venta registrada correctamente.");
                txtCantidad.clear();
                comboProducto.getSelectionModel().clearSelection();
                lblTotal.setText("Total: $0.00");
            } else {
                mostrarAlerta("Error", "No se pudo registrar (Posible falta de stock).");
            }
        } catch (Exception e) {
            mostrarAlerta("Error", "Verifique los datos: " + e.getMessage());
        }
    }

    // --- PESTAÑA 2: CARGA MASIVA (EXCEL/CSV) ---
    private Tab crearTabMasiva() {
        VBox layout = new VBox(20);
        layout.getStyleClass().add("tab-content-area");

        VBox card = new VBox(20);
        card.getStyleClass().add("card");

        Label cardTitle = new Label("Carga Masiva desde Excel (CSV)");
        cardTitle.getStyleClass().add("card-title");
        Label instrucciones = new Label("Instrucciones: Descargue la plantilla, llénela en Excel y guárdela como CSV.");
        instrucciones.setStyle("-fx-text-fill: #666; -fx-font-style: italic;");

        HBox botones = new HBox(15);
        botones.setAlignment(Pos.CENTER_LEFT);

        Button btnPlantilla = new Button("📥 Descargar Plantilla");
        btnPlantilla.getStyleClass().add("button-secondary"); // Gris
        btnPlantilla.setOnAction(e -> descargarPlantilla());

        Button btnSubir = new Button("📤 Subir Archivo (.csv)");
        btnSubir.getStyleClass().add("button-primary"); // Azul
        btnSubir.setOnAction(e -> subirArchivo());

        Button btnProcesar = new Button("💾 Procesar Todo");
        btnProcesar.getStyleClass().add("button-success"); // Verde
        btnProcesar.setOnAction(e -> procesarCargaMasiva());

        botones.getChildren().addAll(btnPlantilla, btnSubir, btnProcesar);

        tablaPreview = new TableView<>();
        tablaPreview.setItems(datosCarga);
        tablaPreview.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(tablaPreview, Priority.ALWAYS);

        TableColumn<FilaCarga, String> cProd = new TableColumn<>("Producto");
        cProd.setCellValueFactory(new PropertyValueFactory<>("nombreProducto"));

        TableColumn<FilaCarga, Integer> cCant = new TableColumn<>("Cantidad");
        cCant.setCellValueFactory(new PropertyValueFactory<>("cantidad"));

        TableColumn<FilaCarga, String> cFecha = new TableColumn<>("Fecha");
        cFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));

        TableColumn<FilaCarga, String> cEstado = new TableColumn<>("Estado");
        cEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        cEstado.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.contains("Error") || item.contains("No")) {
                        setStyle("-fx-text-fill: #EF4444; -fx-font-weight: bold;");
                    } else if (item.equals("Listo")) {
                        setStyle("-fx-text-fill: #F59E0B; -fx-font-weight: bold;");
                    } else if (item.equals("Registrado")) {
                        setStyle("-fx-text-fill: #22C55E; -fx-font-weight: bold;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });

        tablaPreview.getColumns().addAll(cProd, cCant, cFecha, cEstado);

        card.getChildren().addAll(cardTitle, instrucciones, botones, tablaPreview);
        layout.getChildren().add(card);

        Tab tab = new Tab("Carga Masiva (Excel)", layout);
        tab.setClosable(false);
        return tab;
    }

    private void descargarPlantilla() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar Plantilla de Ventas");
        fileChooser.setInitialFileName("plantilla_ventas.csv");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos CSV", "*.csv"));
        File file = fileChooser.showSaveDialog(null);

        if (file != null) {
            try (PrintWriter writer = new PrintWriter(file, StandardCharsets.UTF_8)) {
                writer.println("NombreProducto;Cantidad;Fecha(YYYY-MM-DD);Cliente");
                writer.println("Helado Chocolate;5;2025-01-30;Juan Perez");
                mostrarAlerta("Éxito", "Plantilla guardada. Ábrala con Excel.");
            } catch (IOException ex) {
                mostrarAlerta("Error", "No se pudo guardar: " + ex.getMessage());
            }
        }
    }

    private void subirArchivo() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar Archivo de Ventas");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos CSV", "*.csv"));
        File file = fileChooser.showOpenDialog(null);

        if (file != null) {
            datosCarga.clear();
            try (BufferedReader br = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
                String line;
                boolean first = true;
                while ((line = br.readLine()) != null) {
                    if (first) {
                        first = false;
                        continue;
                    } // Saltar cabecera
                    String[] data = line.split(";");
                    if (data.length >= 2) {
                        String prod = data[0].trim();
                        try {
                            int cant = Integer.parseInt(data[1].trim());
                            String fecha = (data.length > 2 && !data[2].isEmpty()) ? data[2] : LocalDate.now().toString();
                            String cliente = (data.length > 3) ? data[3] : "Varios";

                            // Validar si existe producto
                            Integer id = ventaDAO.obtenerIdPorNombre(prod);
                            String estado = (id != null) ? "Listo" : "Producto no encontrado";

                            datosCarga.add(new FilaCarga(prod, cant, fecha, cliente, estado, id));
                        } catch (NumberFormatException e) {
                            // Ignorar líneas mal formadas
                        }
                    }
                }
            } catch (Exception ex) {
                mostrarAlerta("Error", "Error leyendo archivo: " + ex.getMessage());
            }
        }
    }

    private void procesarCargaMasiva() {
        int exitos = 0;
        int errores = 0;

        for (FilaCarga fila : datosCarga) {
            if (fila.estado.equals("Registrado")) continue; // No procesar doble

            if (fila.idProducto != null) {
                try {
                    double precio = ventaDAO.obtenerPrecioActual(fila.idProducto);
                    boolean ok = ventaDAO.registrarVenta(fila.idProducto, fila.cantidad, precio, fila.cliente, LocalDate.parse(fila.fecha));
                    if (ok) {
                        fila.setEstado("Registrado");
                        exitos++;
                    } else {
                        fila.setEstado("Error Stock");
                        errores++;
                    }
                } catch (Exception e) {
                    fila.setEstado("Error: " + e.getMessage());
                    errores++;
                }
            } else {
                fila.setEstado("Ignorado (ID Nulo)");
                errores++;
            }
        }
        tablaPreview.refresh();
        mostrarAlerta("Proceso Finalizado", "Éxitos: " + exitos + "\nErrores: " + errores);
    }

    // --- UTILS ---
    private void cargarProductos() {
        productosDisponibles.clear();
        try (Connection c = ConexionBD.getConnection(); ResultSet rs = c.createStatement().executeQuery("SELECT IdProductoTerminado, Nombre, PrecioVenta FROM productos_terminados")) {
            while (rs.next()) {
                productosDisponibles.add(new ProductoVenta(rs.getInt(1), rs.getString(2), rs.getDouble(3)));
            }
        } catch (Exception e) {
        }
    }

    private void mostrarAlerta(String t, String m) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        if (t.startsWith("Error")) a.setAlertType(Alert.AlertType.ERROR);
        a.setTitle(t);
        a.setContentText(m);
        a.showAndWait();
    }

    // --- CLASES MODELO INTERNAS ---
    public static class ProductoVenta {
        int id;
        String nombre;
        double precio;

        public ProductoVenta(int id, String n, double p) {
            this.id = id;
            this.nombre = n;
            this.precio = p;
        }

        @Override
        public String toString() {
            return nombre;
        }
    }

    public static class FilaCarga {
        String nombreProducto;
        int cantidad;
        String fecha;
        String cliente;
        String estado;
        Integer idProducto;

        public FilaCarga(String n, int c, String f, String cl, String e, Integer id) {
            this.nombreProducto = n;
            this.cantidad = c;
            this.fecha = f;
            this.cliente = cl;
            this.estado = e;
            this.idProducto = id;
        }

        public String getNombreProducto() {
            return nombreProducto;
        }

        public int getCantidad() {
            return cantidad;
        }

        public String getFecha() {
            return fecha;
        }

        public String getEstado() {
            return estado;
        }

        public void setEstado(String e) {
            this.estado = e;
        }
    }
}