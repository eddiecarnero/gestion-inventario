package com.inventario.ui;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class GestionInsumosPage extends BorderPane {

    // Estilos CSS unificados
    private static final String CSS_STYLES = """
        .root { -fx-background-color: #FDF8F0; -fx-font-family: 'Segoe UI'; }
        .header-title { -fx-font-size: 2.2em; -fx-font-weight: bold; -fx-text-fill: #333333; }
        .header-description { -fx-font-size: 1.1em; -fx-text-fill: #555555; }
        .tab-pane .tab-header-area .tab-header-background { -fx-background-color: transparent; }
        .tab-pane .tab { 
            -fx-background-color: transparent; 
            -fx-border-color: transparent; 
            -fx-padding: 10 20 10 20; 
            -fx-font-size: 1.1em; 
            -fx-font-weight: bold;
            -fx-text-fill: #777;
        }
        .tab-pane .tab:selected { 
            -fx-border-color: #4A90E2; 
            -fx-border-width: 0 0 3 0; 
            -fx-text-fill: #4A90E2; 
        }
        .tab-content-area { -fx-padding: 20 0 0 0; }
    """;

    public GestionInsumosPage() {
        this.getStylesheets().add("data:text/css," + CSS_STYLES.replace("\n", ""));
        this.getStyleClass().add("root");
        setPadding(new Insets(20, 40, 20, 40));

        // --- Encabezado General ---
        VBox headerBox = new VBox(5);
        Label title = new Label("Gestión Integral de Insumos");
        title.getStyleClass().add("header-title");
        Label subtitle = new Label("Administra el catálogo, realiza compras y actualiza inventario masivamente.");
        subtitle.getStyleClass().add("header-description");
        headerBox.getChildren().addAll(title, subtitle);

        setTop(headerBox);
        BorderPane.setMargin(headerBox, new Insets(0, 0, 10, 0));

        // --- Pestañas ---
        TabPane tabPane = new TabPane();
        tabPane.getStyleClass().add("tab-pane");

        // 1. Pestaña: Catálogo (Crear/Editar Insumos)
        // Usamos ProductosPage, pero le quitamos su padding para que encaje mejor
        ProductosPage productosPage = new ProductosPage();
        //productosPage.setPadding(new Insets(10, 0, 0, 0));
        // Opcional: Si ProductosPage tiene su propio header y quieres ocultarlo,
        // podrías acceder a su 'Top' y ponerlo null, pero dejémoslo por ahora.
        Tab tabCatalogo = new Tab("📋 Catálogo de Insumos", productosPage);
        tabCatalogo.setClosable(false);

        // 2. Pestaña: Compras (Ordenes de Compra)
        OrdenesPage ordenesPage = new OrdenesPage();
        //ordenesPage.setPadding(new Insets(10, 0, 0, 0));
        Tab tabCompras = new Tab("🛒 Órdenes de Compra", ordenesPage);
        tabCompras.setClosable(false);

        // 3. Pestaña: Carga Rápida (Subir Insumos)
        SubirInsumosPage subirPage = new SubirInsumosPage();
        //subirPage.setPadding(new Insets(10, 0, 0, 0));
        Tab tabCarga = new Tab("⚡ Ingreso Rápido / Masivo", subirPage);
        tabCarga.setClosable(false);

        tabPane.getTabs().addAll(tabCatalogo, tabCompras, tabCarga);

        setCenter(tabPane);
    }
}