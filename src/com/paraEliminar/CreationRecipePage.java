package com.paraEliminar;

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
import java.util.ArrayList;
import java.util.List;

public class CreationRecipePage extends BorderPane {

    // Colores equivalentes
    private static final String PRIMARY = "#4A90E2";
    private static final String BG_LIGHT = "#FDF8F0";
    private static final String TEXT_LIGHT = "#333333";
    private static final String BORDER_LIGHT = "#CCCCCC";

    private final ObservableList<Receta> recetas = FXCollections.observableArrayList();
    private final VBox mainContent;
    private final com.inventario.dao.RecetaDAO recetaDAO;

    public CreationRecipePage() {
        // Fondo blanco para integrarse con el dashboard
        setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));

        mainContent = new VBox(30);
        mainContent.setPadding(new Insets(30, 40, 30, 40));

        // Inicializar DAO y cargar recetas desde la base de datos
        recetaDAO = new com.inventario.dao.RecetaDAO();
        cargarRecetasDesdeBD();

        setCenter(mainContent);
        mostrarVistaInicial();
    }

    private void cargarRecetasDesdeBD() {
        // Primero verificar la conexión
        if (recetaDAO.verificarConexion()) {
            List<Receta> recetasCargadas = recetaDAO.cargarTodasLasRecetas();
            recetas.addAll(recetasCargadas);
            System.out.println("📊 Total de recetas en memoria: " + recetas.size());
        } else {
            System.err.println("❌ No se pudieron cargar las recetas - sin conexión a BD");
        }
    }

    private void mostrarVistaInicial() {
        mainContent.getChildren().clear();

        if (recetas.isEmpty()) {
            mostrarVistaSinRecetas();
        } else {
            mostrarVistaConRecetas();
        }
    }

    private void mostrarVistaSinRecetas() {
        VBox emptyState = new VBox(30);
        emptyState.setAlignment(Pos.CENTER);
        emptyState.setPadding(new Insets(100));

        Label titleLabel = new Label("No hay recetas registradas");
        titleLabel.setFont(Font.font("Segoe UI", 28));
        titleLabel.setTextFill(Color.web(TEXT_LIGHT));

        Label subtitleLabel = new Label("Comienza creando tu primera receta");
        subtitleLabel.setFont(Font.font("Segoe UI", 16));
        subtitleLabel.setTextFill(Color.GRAY);

        Button crearBtn = new Button("➕ Crear Nueva Receta");
        crearBtn.setStyle("-fx-background-color: " + PRIMARY + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16; -fx-padding: 12 24;");
        crearBtn.setOnMouseEntered(e -> crearBtn.setStyle("-fx-background-color: #3A7BC8; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16; -fx-padding: 12 24;"));
        crearBtn.setOnMouseExited(e -> crearBtn.setStyle("-fx-background-color: " + PRIMARY + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16; -fx-padding: 12 24;"));
        crearBtn.setOnAction(e -> mostrarFormularioCreacion());

        emptyState.getChildren().addAll(titleLabel, subtitleLabel, crearBtn);
        mainContent.getChildren().add(emptyState);
    }

    private void mostrarVistaConRecetas() {
        Label header = new Label("Gestión de Recetas");
        header.setFont(Font.font("Segoe UI", 32));
        header.setTextFill(Color.web(TEXT_LIGHT));

        // Botón para crear nueva receta
        Button crearBtn = new Button("➕ Crear Nueva Receta");
        crearBtn.setStyle("-fx-background-color: " + PRIMARY + "; -fx-text-fill: white; -fx-font-weight: bold;");
        crearBtn.setOnMouseEntered(e -> crearBtn.setStyle("-fx-background-color: #3A7BC8; -fx-text-fill: white; -fx-font-weight: bold;"));
        crearBtn.setOnMouseExited(e -> crearBtn.setStyle("-fx-background-color: " + PRIMARY + "; -fx-text-fill: white; -fx-font-weight: bold;"));
        crearBtn.setOnAction(e -> mostrarFormularioCreacion());

        HBox headerBox = new HBox(20);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.getChildren().addAll(header, crearBtn);
        HBox.setHgrow(header, Priority.ALWAYS);

        // Sección de cálculo
        VBox calculoContainer = new VBox(20);
        calculoContainer.setPadding(new Insets(30));
        calculoContainer.setBackground(new Background(new BackgroundFill(Color.WHITE, new CornerRadii(5), Insets.EMPTY)));
        calculoContainer.setBorder(new Border(new BorderStroke(Color.web(BORDER_LIGHT),
                BorderStrokeStyle.SOLID, new CornerRadii(5), BorderWidths.DEFAULT)));

        Label calculoLabel = new Label("Calcular Ingredientes");
        calculoLabel.setFont(Font.font("Segoe UI", 22));
        calculoLabel.setTextFill(Color.web(TEXT_LIGHT));

        HBox calculoInputs = new HBox(15);
        calculoInputs.setAlignment(Pos.CENTER_LEFT);

        ComboBox<String> recetaCombo = new ComboBox<>();
        recetaCombo.setPromptText("Seleccionar Receta");
        for (Receta r : recetas) {
            recetaCombo.getItems().add(r.nombre());
        }
        recetaCombo.setPrefWidth(250);

        TextField cantidadField = new TextField();
        cantidadField.setPromptText("Cantidad deseada");
        cantidadField.setPrefWidth(150);

        ComboBox<String> unidadCombo = new ComboBox<>();
        unidadCombo.getItems().addAll("Kg", "Litros");
        unidadCombo.getSelectionModel().selectFirst();
        unidadCombo.setPrefWidth(100);

        Button calcularBtn = new Button("Calcular");
        calcularBtn.setStyle("-fx-background-color: #22C55E; -fx-text-fill: white; -fx-font-weight: bold;");
        calcularBtn.setOnMouseEntered(e -> calcularBtn.setStyle("-fx-background-color: #16A34A; -fx-text-fill: white; -fx-font-weight: bold;"));
        calcularBtn.setOnMouseExited(e -> calcularBtn.setStyle("-fx-background-color: #22C55E; -fx-text-fill: white; -fx-font-weight: bold;"));
        calcularBtn.setOnAction(e -> {
            String recetaNombre = recetaCombo.getValue();
            String cantidadStr = cantidadField.getText().trim();
            String unidad = unidadCombo.getValue();

            if (recetaNombre != null && !cantidadStr.isEmpty()) {
                calcularIngredientes(recetaNombre, cantidadStr, unidad);
            } else {
                mostrarAlerta("Error", "Por favor, selecciona una receta y especifica la cantidad.");
            }
        });

        calculoInputs.getChildren().addAll(
                new Label("Receta:"), recetaCombo,
                new Label("Cantidad:"), cantidadField, unidadCombo,
                calcularBtn
        );

        calculoContainer.getChildren().addAll(calculoLabel, calculoInputs);

        // Lista de recetas existentes
        VBox listaContainer = new VBox(15);
        listaContainer.setPadding(new Insets(30));
        listaContainer.setBackground(new Background(new BackgroundFill(Color.WHITE, new CornerRadii(5), Insets.EMPTY)));
        listaContainer.setBorder(new Border(new BorderStroke(Color.web(BORDER_LIGHT),
                BorderStrokeStyle.SOLID, new CornerRadii(5), BorderWidths.DEFAULT)));

        Label listaLabel = new Label("Recetas Registradas");
        listaLabel.setFont(Font.font("Segoe UI", 22));
        listaLabel.setTextFill(Color.web(TEXT_LIGHT));

        VBox recetasList = new VBox(10);
        for (Receta receta : recetas) {
            HBox recetaItem = crearItemReceta(receta);
            recetasList.getChildren().add(recetaItem);
        }

        listaContainer.getChildren().addAll(listaLabel, recetasList);

        mainContent.getChildren().addAll(headerBox, calculoContainer, listaContainer);
    }

    private HBox crearItemReceta(Receta receta) {
        HBox item = new HBox(15);
        item.setPadding(new Insets(15));
        item.setAlignment(Pos.CENTER_LEFT);
        item.setBackground(new Background(new BackgroundFill(Color.web("#FAFAFA"), new CornerRadii(5), Insets.EMPTY)));
        item.setBorder(new Border(new BorderStroke(Color.web(BORDER_LIGHT),
                BorderStrokeStyle.SOLID, new CornerRadii(5), BorderWidths.DEFAULT)));

        VBox info = new VBox(5);
        Label nombreLabel = new Label(receta.nombre());
        nombreLabel.setFont(Font.font("Segoe UI", 16));
        nombreLabel.setTextFill(Color.web(TEXT_LIGHT));

        Label rendimientoLabel = new Label("Rinde: " + receta.cantidadProducida() + " " + receta.unidadProducida());
        rendimientoLabel.setFont(Font.font("Segoe UI", 13));
        rendimientoLabel.setTextFill(Color.GRAY);

        info.getChildren().addAll(nombreLabel, rendimientoLabel);
        HBox.setHgrow(info, Priority.ALWAYS);

        Button verBtn = new Button("👁 Ver Detalles");
        verBtn.setStyle("-fx-background-color: white; -fx-text-fill: " + PRIMARY + "; -fx-border-color: " + PRIMARY + ";");
        verBtn.setOnAction(e -> mostrarDetalleReceta(receta));

        Button eliminarBtn = new Button("🗑️");
        eliminarBtn.setStyle("-fx-background-color: transparent; -fx-font-size: 16; -fx-text-fill: #EF4444;");
        eliminarBtn.setOnAction(e -> {
            // Eliminar de la base de datos
            if (recetaDAO.eliminarReceta(receta.nombre())) {
                recetas.remove(receta);
                mostrarVistaInicial();
            } else {
                mostrarAlerta("Error", "No se pudo eliminar la receta de la base de datos.");
            }
        });

        item.getChildren().addAll(info, verBtn, eliminarBtn);
        return item;
    }

    private void mostrarFormularioCreacion() {
        mainContent.getChildren().clear();

        Label header = new Label("Crear Nueva Receta");
        header.setFont(Font.font("Segoe UI", 32));
        header.setTextFill(Color.web(TEXT_LIGHT));

        VBox formContainer = new VBox(30);
        formContainer.setPadding(new Insets(30));
        formContainer.setBackground(new Background(new BackgroundFill(Color.WHITE, new CornerRadii(5), Insets.EMPTY)));
        formContainer.setBorder(new Border(new BorderStroke(Color.web(BORDER_LIGHT),
                BorderStrokeStyle.SOLID, new CornerRadii(5), BorderWidths.DEFAULT)));

        // Información básica
        Label infoLabel = new Label("Información Básica");
        infoLabel.setFont(Font.font("Segoe UI", 20));
        infoLabel.setTextFill(Color.web(TEXT_LIGHT));

        TextField nombreField = new TextField();
        nombreField.setPromptText("Nombre de la receta");

        HBox rendimientoBox = new HBox(10);
        rendimientoBox.setAlignment(Pos.CENTER_LEFT);
        TextField cantidadField = new TextField();
        cantidadField.setPromptText("Cantidad");
        cantidadField.setPrefWidth(150);
        ComboBox<String> unidadCombo = new ComboBox<>();
        unidadCombo.getItems().addAll("Kg", "Litros", "Unidades");
        unidadCombo.getSelectionModel().selectFirst();
        unidadCombo.setPrefWidth(150);
        rendimientoBox.getChildren().addAll(new Label("Rinde:"), cantidadField, unidadCombo);

        // Tabla de insumos
        Label insumosLabel = new Label("Insumos Necesarios");
        insumosLabel.setFont(Font.font("Segoe UI", 20));
        insumosLabel.setTextFill(Color.web(TEXT_LIGHT));

        TableView<Ingrediente> tabla = new TableView<>();
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabla.setPrefHeight(300);

        TableColumn<Ingrediente, String> nombreCol = new TableColumn<>("Insumo");
        nombreCol.setCellValueFactory(new PropertyValueFactory<>("nombre"));

        TableColumn<Ingrediente, Double> cantCol = new TableColumn<>("Cantidad");
        cantCol.setCellValueFactory(new PropertyValueFactory<>("cantidad"));

        TableColumn<Ingrediente, String> unidCol = new TableColumn<>("Unidad");
        unidCol.setCellValueFactory(new PropertyValueFactory<>("unidad"));

        TableColumn<Ingrediente, Void> eliminarCol = new TableColumn<>("Acción");
        eliminarCol.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("🗑️");
            {
                btn.setStyle("-fx-background-color: transparent; -fx-font-size: 16; -fx-text-fill: #EF4444;");
                btn.setOnAction(e -> {
                    Ingrediente ing = getTableView().getItems().get(getIndex());
                    getTableView().getItems().remove(ing);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        tabla.getColumns().addAll(nombreCol, cantCol, unidCol, eliminarCol);
        ObservableList<Ingrediente> ingredientes = FXCollections.observableArrayList();
        tabla.setItems(ingredientes);

        // Entrada de insumos
        GridPane inputGrid = new GridPane();
        inputGrid.setHgap(10);
        inputGrid.setVgap(10);
        inputGrid.setPadding(new Insets(15));
        inputGrid.setBackground(new Background(new BackgroundFill(Color.web("#FAFAFA"), new CornerRadii(5), Insets.EMPTY)));
        inputGrid.setBorder(new Border(new BorderStroke(Color.web(BORDER_LIGHT),
                BorderStrokeStyle.SOLID, new CornerRadii(5), BorderWidths.DEFAULT)));

        TextField insumoField = new TextField();
        insumoField.setPromptText("Nombre del insumo");

        TextField cantInsumoField = new TextField();
        cantInsumoField.setPromptText("Cantidad");

        ComboBox<String> unidadInsumoCombo = new ComboBox<>();
        unidadInsumoCombo.getItems().addAll("Kg", "Litros", "Gramos", "ml", "Unidades");
        unidadInsumoCombo.getSelectionModel().selectFirst();

        Button addBtn = new Button("➕ Agregar Insumo");
        addBtn.setStyle("-fx-background-color: #22C55E; -fx-text-fill: white; -fx-font-weight: bold;");
        addBtn.setOnMouseEntered(e -> addBtn.setStyle("-fx-background-color: #16A34A; -fx-text-fill: white; -fx-font-weight: bold;"));
        addBtn.setOnMouseExited(e -> addBtn.setStyle("-fx-background-color: #22C55E; -fx-text-fill: white; -fx-font-weight: bold;"));
        addBtn.setOnAction(e -> {
            try {
                String nombre = insumoField.getText().trim();
                double cantidad = Double.parseDouble(cantInsumoField.getText().trim());
                String unidad = unidadInsumoCombo.getValue();

                if (nombre.isEmpty() || cantidad <= 0) {
                    mostrarAlerta("Error", "Completa todos los campos correctamente.");
                    return;
                }

                ingredientes.add(new Ingrediente(nombre, cantidad, unidad));
                insumoField.clear();
                cantInsumoField.clear();

            } catch (NumberFormatException ex) {
                mostrarAlerta("Error", "La cantidad debe ser un valor numérico.");
            }
        });

        inputGrid.add(insumoField, 0, 0);
        inputGrid.add(cantInsumoField, 1, 0);
        inputGrid.add(unidadInsumoCombo, 2, 0);
        inputGrid.add(addBtn, 3, 0);

        for (int i = 0; i < 4; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(25);
            inputGrid.getColumnConstraints().add(col);
        }

        // Botones finales
        HBox buttonsBox = new HBox(10);
        buttonsBox.setAlignment(Pos.CENTER_RIGHT);

        Button cancelarBtn = new Button("Cancelar");
        cancelarBtn.setStyle("-fx-background-color: white; -fx-text-fill: #EF4444; -fx-border-color: #EF4444; -fx-font-weight: bold;");
        cancelarBtn.setOnAction(e -> mostrarVistaInicial());

        Button guardarBtn = new Button("Guardar Receta");
        guardarBtn.setStyle("-fx-background-color: " + PRIMARY + "; -fx-text-fill: white; -fx-font-weight: bold;");
        guardarBtn.setOnMouseEntered(e -> guardarBtn.setStyle("-fx-background-color: #3A7BC8; -fx-text-fill: white; -fx-font-weight: bold;"));
        guardarBtn.setOnMouseExited(e -> guardarBtn.setStyle("-fx-background-color: " + PRIMARY + "; -fx-text-fill: white; -fx-font-weight: bold;"));
        guardarBtn.setOnAction(e -> {
            try {
                String nombre = nombreField.getText().trim();
                double cantidad = Double.parseDouble(cantidadField.getText().trim());
                String unidad = unidadCombo.getValue();

                if (nombre.isEmpty() || cantidad <= 0 || ingredientes.isEmpty()) {
                    mostrarAlerta("Error", "Completa todos los campos y agrega al menos un insumo.");
                    return;
                }

                List<Ingrediente> listaIngredientes = new ArrayList<>(ingredientes);
                Receta nuevaReceta = new Receta(nombre, cantidad, unidad, listaIngredientes);

                // Guardar en la base de datos
                if (recetaDAO.guardarReceta(nuevaReceta)) {
                    recetas.add(nuevaReceta);
                    mostrarAlerta("Éxito", "Receta creada y guardada exitosamente.");
                    mostrarVistaInicial();
                } else {
                    mostrarAlerta("Error", "No se pudo guardar la receta en la base de datos.");
                }

            } catch (NumberFormatException ex) {
                mostrarAlerta("Error", "La cantidad debe ser un valor numérico.");
            }
        });

        buttonsBox.getChildren().addAll(cancelarBtn, guardarBtn);

        formContainer.getChildren().addAll(
                infoLabel, nombreField, rendimientoBox,
                insumosLabel, tabla, inputGrid, buttonsBox
        );

        mainContent.getChildren().addAll(header, formContainer);
    }

    private void mostrarDetalleReceta(Receta receta) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Detalles de Receta");
        alert.setHeaderText(receta.nombre());

        StringBuilder contenido = new StringBuilder();
        contenido.append("Rinde: ").append(receta.cantidadProducida())
                .append(" ").append(receta.unidadProducida()).append("\n\n");
        contenido.append("Ingredientes:\n");

        for (Ingrediente ing : receta.ingredientes()) {
            contenido.append("• ").append(ing.nombre())
                    .append(": ").append(ing.cantidad())
                    .append(" ").append(ing.unidad()).append("\n");
        }

        alert.setContentText(contenido.toString());
        alert.showAndWait();
    }

    private void calcularIngredientes(String recetaNombre, String cantidadStr, String unidadDeseada) {
        try {
            double cantidadDeseada = Double.parseDouble(cantidadStr);

            Receta receta = recetas.stream()
                    .filter(r -> r.nombre().equals(recetaNombre))
                    .findFirst()
                    .orElse(null);

            if (receta == null) return;

            // Calcular factor de escala
            double factor = cantidadDeseada / receta.cantidadProducida();

            StringBuilder resultado = new StringBuilder();
            resultado.append("Para producir ").append(cantidadDeseada).append(" ")
                    .append(unidadDeseada).append(" de ").append(receta.nombre())
                    .append(":\n\n");

            for (Ingrediente ing : receta.ingredientes()) {
                double cantidadNecesaria = ing.cantidad() * factor;
                resultado.append("• ").append(ing.nombre()).append(": ")
                        .append(String.format("%.2f", cantidadNecesaria))
                        .append(" ").append(ing.unidad()).append("\n");
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Cálculo de Ingredientes");
            alert.setHeaderText("Ingredientes Necesarios");
            alert.setContentText(resultado.toString());
            alert.showAndWait();

        } catch (NumberFormatException e) {
            mostrarAlerta("Error", "La cantidad debe ser un valor numérico.");
        }
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    // Clases auxiliares
        public record Receta(String nombre, double cantidadProducida, String unidadProducida,
                             List<Ingrediente> ingredientes) {
    }

    public record Ingrediente(String nombre, double cantidad, String unidad) {
    }

    // Test local (opcional - para probar la página de forma independiente)
    public static void main(String[] args) {
        javafx.application.Application.launch(TestApp.class);
    }

    public static class TestApp extends javafx.application.Application {
        @Override
        public void start(Stage stage) {
            stage.setTitle("Gestión de Recetas - JavaFX");
            stage.setScene(new Scene(new CreationRecipePage(), 1400, 850));
            stage.show();
        }
    }
}