package com.inventario.ui;

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

public class OrdenesPage extends BorderPane {

    // Colores equivalentes
    private static final String PRIMARY = "#4A90E2";
    private static final String BG_LIGHT = "#FDF8F0";
    private static final String SIDEBAR_LIGHT = "#EAE0D1";
    private static final String TEXT_LIGHT = "#333333";
    private static final String BORDER_LIGHT = "#CCCCCC";

    private final TableView<ItemOrden> tabla;
    private final Label totalLabel;
    private final TextField insumoField;
    private final TextField cantidadField;
    private final ComboBox<String> unidadCombo;
    private final TextField precioField;
    private final ObservableList<ItemOrden> items = FXCollections.observableArrayList();

    public OrdenesPage() {
        setBackground(new Background(new BackgroundFill(Color.web(BG_LIGHT), CornerRadii.EMPTY, Insets.EMPTY)));

        // --- Contenido principal ---
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

        // --- Campos superiores ---
        HBox camposSuperiores = new HBox(20);
        camposSuperiores.setAlignment(Pos.CENTER_LEFT);

        ComboBox<String> proveedorCombo = new ComboBox<>();
        proveedorCombo.getItems().addAll(
                "Seleccionar Proveedor",
                "Proveedor Lácteos del Sur",
                "Distribuidora de Frutas S.A.",
                "Insumos de Café 'El Grano Dorado'"
        );
        proveedorCombo.getSelectionModel().selectFirst();

        DatePicker fechaPicker = new DatePicker();
        fechaPicker.setPromptText("Fecha de la Orden");

        VBox proveedorBox = crearCampo("Proveedor", proveedorCombo);
        VBox fechaBox = crearCampo("Fecha de la Orden", fechaPicker);
        camposSuperiores.getChildren().addAll(proveedorBox, fechaBox);

        // --- Tabla ---
        Label detalleLabel = new Label("Detalle de la Orden");
        detalleLabel.setFont(Font.font("Segoe UI", 22));
        detalleLabel.setTextFill(Color.web(TEXT_LIGHT));

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

        TableColumn<ItemOrden, Void> eliminarCol = new TableColumn<>("Acción");
        eliminarCol.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("🗑️");

            {
                btn.setStyle("-fx-background-color: transparent; -fx-font-size: 16; -fx-text-fill: #EF4444;");
                btn.setOnAction(e -> {
                    ItemOrden item = getTableView().getItems().get(getIndex());
                    items.remove(item);
                    actualizarTotal();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        tabla.getColumns().addAll(insumoCol, cantidadCol, unidadCol, precioCol, totalCol, eliminarCol);

        // Datos iniciales
        items.addAll(
                new ItemOrden("Leche Entera", 10, "Litros", 20),
                new ItemOrden("Azúcar Refinada", 5, "Kilos", 15)
        );
        tabla.setItems(items);

        // --- Fila de entrada ---
        GridPane inputGrid = new GridPane();
        inputGrid.setHgap(10);
        inputGrid.setVgap(10);
        inputGrid.setPadding(new Insets(15));
        inputGrid.setBackground(new Background(new BackgroundFill(Color.web("#FAFAFA"), new CornerRadii(5), Insets.EMPTY)));
        inputGrid.setBorder(new Border(new BorderStroke(Color.web(BORDER_LIGHT),
                BorderStrokeStyle.SOLID, new CornerRadii(5), BorderWidths.DEFAULT)));

        insumoField = new TextField();
        insumoField.setPromptText("Buscar insumo...");

        cantidadField = new TextField();
        cantidadField.setPromptText("0");

        unidadCombo = new ComboBox<>();
        unidadCombo.getItems().addAll("Unidad", "Litro", "Kilo", "Gramo");
        unidadCombo.getSelectionModel().selectFirst();

        precioField = new TextField();
        precioField.setPromptText("0.00");

        Button addButton = new Button("➕ Agregar");
        addButton.setStyle("-fx-background-color: #22C55E; -fx-text-fill: white; -fx-font-weight: bold;");
        addButton.setOnMouseEntered(e -> addButton.setStyle("-fx-background-color: #16A34A; -fx-text-fill: white; -fx-font-weight: bold;"));
        addButton.setOnMouseExited(e -> addButton.setStyle("-fx-background-color: #22C55E; -fx-text-fill: white; -fx-font-weight: bold;"));
        addButton.setOnAction(e -> agregarItem());

        inputGrid.add(insumoField, 0, 0);
        inputGrid.add(cantidadField, 1, 0);
        inputGrid.add(unidadCombo, 2, 0);
        inputGrid.add(precioField, 3, 0);
        inputGrid.add(addButton, 4, 0);

        ColumnConstraints[] cols = new ColumnConstraints[5];
        for (int i = 0; i < 5; i++) {
            cols[i] = new ColumnConstraints();
            cols[i].setPercentWidth(20);
            inputGrid.getColumnConstraints().add(cols[i]);
        }

        // --- Total ---
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

        // --- Botones finales ---
        HBox buttons = new HBox(10);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        Button guardarBtn = new Button("Guardar");
        guardarBtn.setStyle("-fx-background-color: white; -fx-text-fill: " + PRIMARY + "; -fx-border-color: " + PRIMARY + "; -fx-font-weight: bold;");
        Button enviarBtn = new Button("Enviar para Aprobación");
        enviarBtn.setStyle("-fx-background-color: " + PRIMARY + "; -fx-text-fill: white; -fx-font-weight: bold;");
        buttons.getChildren().addAll(guardarBtn, enviarBtn);

        // --- Ensamblar todo ---
        formContainer.getChildren().addAll(camposSuperiores, detalleLabel, tabla, inputGrid, totalBox, buttons);
        content.getChildren().addAll(header, formContainer);

        setCenter(content);
        actualizarTotal();
    }

    private VBox crearCampo(String labelText, Control input) {
        VBox vbox = new VBox(5);
        Label label = new Label(labelText);
        label.setFont(Font.font("Segoe UI", 13));
        label.setTextFill(Color.web(TEXT_LIGHT));
        input.setMaxWidth(Double.MAX_VALUE);
        vbox.getChildren().addAll(label, input);
        VBox.setVgrow(input, Priority.NEVER);
        return vbox;
    }

    private void agregarItem() {
        try {
            String insumo = insumoField.getText().trim();
            double cantidad = Double.parseDouble(cantidadField.getText().trim());
            String unidad = unidadCombo.getValue();
            double precio = Double.parseDouble(precioField.getText().trim());

            if (insumo.isEmpty() || cantidad <= 0 || precio <= 0) {
                mostrarAlerta("Error", "Por favor, completa todos los campos correctamente.");
                return;
            }

            items.add(new ItemOrden(insumo, cantidad, unidad, precio));
            insumoField.clear();
            cantidadField.clear();
            precioField.clear();
            unidadCombo.getSelectionModel().selectFirst();
            actualizarTotal();

        } catch (NumberFormatException e) {
            mostrarAlerta("Error", "Cantidad y precio deben ser valores numéricos.");
        }
    }

    private void actualizarTotal() {
        double total = items.stream().mapToDouble(ItemOrden::getTotal).sum();
        totalLabel.setText(String.format("$%.2f", total));
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    // --- Clase auxiliar para los items ---
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

    // --- Test local ---
    public static void main(String[] args) {
        javafx.application.Application.launch(TestApp.class);
    }

    public static class TestApp extends javafx.application.Application {
        @Override
        public void start(Stage stage) {
            stage.setTitle("Orden de Compra - JavaFX");
            stage.setScene(new Scene(new OrdenesPage(), 1400, 850));
            stage.show();
        }
    }
}
