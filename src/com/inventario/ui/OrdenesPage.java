package com.inventario.ui;

import com.inventario.config.ConexionBD;
import com.inventario.service.OrdenCompraService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.sql.*;
import java.time.LocalDate;

public class OrdenesPage extends BorderPane {

    private static final String PRIMARY = "#4A90E2";
    private static final String BG_LIGHT = "#FDF8F0";
    private static final String BORDER_LIGHT = "#CCCCCC";
    private static final String TEXT_LIGHT = "#333333";

    private final TableView<ItemOrden> tabla;
    private final Label totalLabel;
    private final TextField insumoField;
    private final TextField cantidadField;
    private final ComboBox<String> unidadCombo;
    private final TextField precioField;
    private final ObservableList<ItemOrden> items = FXCollections.observableArrayList();

    public OrdenesPage() {
        setBackground(new Background(new BackgroundFill(Color.web(BG_LIGHT), CornerRadii.EMPTY, Insets.EMPTY)));

        VBox content = new VBox(30);
        content.setPadding(new Insets(30, 40, 30, 40));

        Label header = new Label("Crear Nueva Orden de Compra");
        header.setFont(Font.font("Segoe UI", 32));
        header.setTextFill(Color.web(TEXT_LIGHT));

        VBox formContainer = new VBox(30);
        formContainer.setPadding(new Insets(30));
        formContainer.setBackground(new Background(new BackgroundFill(Color.WHITE, new CornerRadii(5), Insets.EMPTY)));
        formContainer.setBorder(new Border(new BorderStroke(Color.web(BORDER_LIGHT),
                BorderStrokeStyle.SOLID, new CornerRadii(5), BorderWidths.DEFAULT)));

        // Campos superiores
        HBox camposSuperiores = new HBox(20);
        camposSuperiores.setAlignment(Pos.CENTER_LEFT);

        ComboBox<String> proveedorCombo = new ComboBox<>();
        proveedorCombo.setPromptText("Seleccionar Proveedor");
        cargarProveedores(proveedorCombo);

        DatePicker fechaPicker = new DatePicker(LocalDate.now());
        fechaPicker.setPromptText("Fecha de la Orden");

        VBox proveedorBox = crearCampo("Proveedor", proveedorCombo);
        VBox fechaBox = crearCampo("Fecha de la Orden", fechaPicker);
        camposSuperiores.getChildren().addAll(proveedorBox, fechaBox);

        // Tabla
        tabla = new TableView<>();
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<ItemOrden, String> insumoCol = new TableColumn<>("Insumo");
        insumoCol.setCellValueFactory(new PropertyValueFactory<>("insumo"));

        TableColumn<ItemOrden, Double> cantidadCol = new TableColumn<>("Cantidad");
        cantidadCol.setCellValueFactory(new PropertyValueFactory<>("cantidad"));

        TableColumn<ItemOrden, String> unidadCol = new TableColumn<>("Unidad");
        unidadCol.setCellValueFactory(new PropertyValueFactory<>("unidad"));

        TableColumn<ItemOrden, Double> precioCol = new TableColumn<>("Precio Unitario");
        precioCol.setCellValueFactory(new PropertyValueFactory<>("precio"));

        TableColumn<ItemOrden, Double> totalCol = new TableColumn<>("Total");
        totalCol.setCellValueFactory(new PropertyValueFactory<>("total"));

        tabla.getColumns().addAll(insumoCol, cantidadCol, unidadCol, precioCol, totalCol);
        tabla.setItems(items);

        // Fila de entrada
        GridPane inputGrid = new GridPane();
        inputGrid.setHgap(10);
        inputGrid.setVgap(10);
        inputGrid.setPadding(new Insets(15));

        insumoField = new TextField();
        insumoField.setPromptText("Insumo");

        cantidadField = new TextField();
        cantidadField.setPromptText("0");

        unidadCombo = new ComboBox<>();
        unidadCombo.getItems().addAll("Unidad", "Litro", "Kilo", "Gramo");
        unidadCombo.getSelectionModel().selectFirst();

        precioField = new TextField();
        precioField.setPromptText("0.00");

        Button addButton = new Button("➕ Agregar");
        addButton.setStyle("-fx-background-color: #22C55E; -fx-text-fill: white; -fx-font-weight: bold;");
        addButton.setOnAction(e -> agregarItem());

        inputGrid.addRow(0, insumoField, cantidadField, unidadCombo, precioField, addButton);

        for (int i = 0; i < 5; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(20);
            inputGrid.getColumnConstraints().add(col);
        }

        // Total
        HBox totalBox = new HBox();
        totalBox.setAlignment(Pos.CENTER_RIGHT);
        totalBox.setPadding(new Insets(10, 0, 0, 0));
        VBox totalInner = new VBox(5);
        Label totalText = new Label("Total de la Orden:");
        totalText.setFont(Font.font("Segoe UI", 16));
        totalLabel = new Label("$0.00");
        totalLabel.setFont(Font.font("Segoe UI", 28));
        totalLabel.setTextFill(Color.web(PRIMARY));
        totalInner.getChildren().addAll(totalText, totalLabel);
        totalBox.getChildren().add(totalInner);

        // Botones
        HBox buttons = new HBox(10);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        Button guardarBtn = new Button("Guardar");
        guardarBtn.setStyle("-fx-background-color: white; -fx-text-fill: " + PRIMARY + "; -fx-border-color: " + PRIMARY + "; -fx-font-weight: bold;");
        buttons.getChildren().addAll(guardarBtn);

        // ✅ Acción del botón "Guardar"
        guardarBtn.setOnAction(e -> {
            try {
                String proveedorNombre = proveedorCombo.getValue();
                if (proveedorNombre == null || proveedorNombre.isEmpty() || proveedorNombre.equals("Seleccionar Proveedor")) {
                    mostrarAlerta("Error", "Selecciona un proveedor válido.");
                    return;
                }

                if (fechaPicker.getValue() == null) {
                    mostrarAlerta("Error", "Selecciona la fecha de la orden.");
                    return;
                }

                double total = items.stream().mapToDouble(ItemOrden::getTotal).sum();
                if (total <= 0) {
                    mostrarAlerta("Error", "Agrega al menos un ítem válido.");
                    return;
                }

                int idProveedor = obtenerIdProveedor(proveedorNombre);
                if (idProveedor == 0) {
                    mostrarAlerta("Error", "No se encontró el proveedor en la base de datos.");
                    return;
                }

                OrdenCompraService service = new OrdenCompraService();
                service.crearOrden(
                        idProveedor,
                        1, // IdEmpleado fijo o tomado de sesión
                        "Compra local",
                        total,
                        java.sql.Date.valueOf(fechaPicker.getValue()),
                        java.sql.Date.valueOf(fechaPicker.getValue()),
                        "Orden creada desde interfaz JavaFX"
                );

                mostrarAlerta("Éxito", "✅ Orden registrada correctamente en MySQL.");

                items.clear();
                actualizarTotal();

            } catch (Exception ex) {
                ex.printStackTrace();
                mostrarAlerta("Error", "Ocurrió un problema: " + ex.getMessage());
            }
        });

        // Ensamblar
        formContainer.getChildren().addAll(camposSuperiores, tabla, inputGrid, totalBox, buttons);
        content.getChildren().addAll(header, formContainer);
        setCenter(content);
    }

    private void cargarProveedores(ComboBox<String> combo) {
        try (Connection conn = ConexionBD.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT Nombre_comercial FROM proveedores")) {

            while (rs.next()) {
                combo.getItems().add(rs.getString("Nombre_comercial"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private int obtenerIdProveedor(String nombre) {
        String sql = "SELECT IdProveedor FROM proveedores WHERE Nombre_comercial = ?";
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nombre);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt("IdProveedor");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private VBox crearCampo(String labelText, Control input) {
        VBox vbox = new VBox(5);
        Label label = new Label(labelText);
        label.setFont(Font.font("Segoe UI", 13));
        label.setTextFill(Color.web(TEXT_LIGHT));
        vbox.getChildren().addAll(label, input);
        return vbox;
    }

    private void agregarItem() {
        try {
            String insumo = insumoField.getText().trim();
            double cantidad = Double.parseDouble(cantidadField.getText().trim());
            String unidad = unidadCombo.getValue();
            double precio = Double.parseDouble(precioField.getText().trim());

            if (insumo.isEmpty() || cantidad <= 0 || precio <= 0) {
                mostrarAlerta("Error", "Completa todos los campos correctamente.");
                return;
            }

            items.add(new ItemOrden(insumo, cantidad, unidad, precio));
            insumoField.clear();
            cantidadField.clear();
            precioField.clear();
            unidadCombo.getSelectionModel().selectFirst();
            actualizarTotal();

        } catch (NumberFormatException e) {
            mostrarAlerta("Error", "Cantidad y precio deben ser numéricos.");
        }
    }

    private void actualizarTotal() {
        double total = items.stream().mapToDouble(ItemOrden::getTotal).sum();
        totalLabel.setText(String.format("$%.2f", total));
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    public static class ItemOrden {
        private final String insumo;
        private final double cantidad;
        private final String unidad;
        private final double precio;
        private final double total;

        public ItemOrden(String insumo, double cantidad, String unidad, double precio) {
            this.insumo = insumo;
            this.cantidad = cantidad;
            this.unidad = unidad;
            this.precio = precio;
            this.total = cantidad * precio;
        }

        public String getInsumo() { return insumo; }
        public double getCantidad() { return cantidad; }
        public String getUnidad() { return unidad; }
        public double getPrecio() { return precio; }
        public double getTotal() { return total; }
    }

    public static void main(String[] args) {
        javafx.application.Application.launch(TestApp.class);
    }

    public static class TestApp extends javafx.application.Application {
        @Override
        public void start(Stage stage) {
            stage.setTitle("Orden de Compra - JavaFX");
            stage.setScene(new Scene(new OrdenesPage(), 1300, 800));
            stage.show();
        }
    }
}
