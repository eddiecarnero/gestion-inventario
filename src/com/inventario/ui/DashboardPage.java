package com.inventario.ui;

import com.inicio.ui.LoginPage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class DashboardPage {
    private boolean sidebarExpanded = true;
    private VBox sidebar;
    private StackPane content;
    private BorderPane root;
    private Label titleLabel;
    private Scene scene;

    public DashboardPage(Stage stage) {
        // ----- Colores principales -----
        Color crema = Color.web("#FFF8E7");
        Color cafe = Color.web("#6F4E37");
        Color turquesa = Color.web("#4DB6AC");

        // ----- Contenedor principal -----
        root = new BorderPane();
        root.setBackground(new Background(new BackgroundFill(crema, CornerRadii.EMPTY, Insets.EMPTY)));

        // ----- Sidebar -----
        sidebar = crearSidebar(cafe, turquesa);

        // ----- Encabezado superior -----
        HBox topBar = crearTopBar(cafe, turquesa, stage);

        // ----- Área central -----
        content = new StackPane();
        content.setBackground(new Background(new BackgroundFill(Color.WHITE, new CornerRadii(20), Insets.EMPTY)));
        content.setEffect(new DropShadow(8, Color.rgb(0, 0, 0, 0.15)));
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.CENTER);

        Label msg = new Label("Panel principal");
        msg.setStyle("-fx-font-size: 20px; -fx-text-fill: #333;");
        content.getChildren().add(msg);

        // ----- Ensamblar todo -----
        root.setLeft(sidebar);
        root.setTop(topBar);
        root.setCenter(content);

        scene = new Scene(root, 1000, 600);
    }

    private VBox crearSidebar(Color cafe, Color turquesa) {
        VBox side = new VBox(10);
        side.setPadding(new Insets(15));
        side.setPrefWidth(220);
        side.setBackground(new Background(new BackgroundFill(cafe, new CornerRadii(0, 20, 20, 0, false), Insets.EMPTY)));

        Button toggle = new Button("☰");
        toggle.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 18;");
        toggle.setOnAction(e -> toggleSidebar());

        Label title = new Label("CAFÉ & HELADOS");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");
        title.setPadding(new Insets(5, 0, 10, 0));

        Button inventarioBtn = crearBotonMenu("☕ Inventario", turquesa);
        Button reportesBtn = crearBotonMenu("📊 Reportes", turquesa);
        Button recetasBtn = crearBotonMenu("🍦 Recetas", turquesa);
        Button usuariosBtn = crearBotonMenu("👥 Usuarios", turquesa);
        Button proveedoresBtn = crearBotonMenu("🚚 Proveedores", turquesa);
        Button ordenesBtn = crearBotonMenu("🧾 Órdenes", turquesa);

        ordenesBtn.setOnAction(e -> root.setCenter(new OrdenesPage()));
        recetasBtn.setOnAction(e -> root.setCenter(new CreationRecipePage())); // 👈 AGREGAR ESTA LÍNEA

// (Aquí podrías hacer lo mismo para los otros botones si quieres más vistas)
        side.getChildren().addAll(
                toggle, title,
                inventarioBtn,
                reportesBtn,
                recetasBtn,
                usuariosBtn,
                proveedoresBtn,
                ordenesBtn
        );

        return side;
    }

    private HBox crearTopBar(Color cafe, Color turquesa, Stage stage) {
        HBox top = new HBox();
        top.setPadding(new Insets(10, 20, 10, 20));
        top.setBackground(new Background(new BackgroundFill(turquesa, CornerRadii.EMPTY, Insets.EMPTY)));
        top.setAlignment(Pos.CENTER_RIGHT);
        top.setSpacing(10);

        titleLabel = new Label("Bienvenido, Usuario");
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");

        Button logout = new Button("Cerrar sesión");
        logout.setStyle("-fx-background-color: white; -fx-text-fill: " + colorToHex(cafe) + "; -fx-background-radius: 20;");

        // 👇 Aquí defines qué pasa al cerrar sesión
        logout.setOnAction(e -> {
            LoginPage login = new LoginPage(stage);
            stage.setScene(login.getScene());
        });

        top.getChildren().addAll(titleLabel, logout);
        return top;
    }

    private Button crearBotonMenu(String texto, Color turquesa) {
        Button btn = new Button(texto);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 14;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: " + colorToHex(turquesa) + "; -fx-text-fill: white;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white;"));
        btn.setTooltip(new Tooltip(texto));
        return btn;
    }

    private void toggleSidebar() {
        sidebarExpanded = !sidebarExpanded;

        if (sidebarExpanded) {
            sidebar.setPrefWidth(220);
            for (var node : sidebar.getChildren()) {
                if (node instanceof Button b && !b.getText().equals("☰")) {
                    b.setText(b.getTooltip().getText());
                }
            }
        } else {
            sidebar.setPrefWidth(60);
            for (var node : sidebar.getChildren()) {
                if (node instanceof Button b && !b.getText().equals("☰")) {
                    b.setText(b.getText().substring(0, 2)); // Solo icono
                }
            }
        }
    }

    private String colorToHex(Color c) {
        return String.format("#%02X%02X%02X",
                (int) (c.getRed() * 255),
                (int) (c.getGreen() * 255),
                (int) (c.getBlue() * 255));
    }

    public Scene getScene() {
        return scene;
    }

}