package com.inventario.ui;

import com.inicio.ui.LoginPage;
import com.inventario.config.AuthService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;

public class SideBar {
    private BorderPane root;
    private VBox sidebar;
    private StackPane mainContent;
    private Scene scene;

    public SideBar(Stage stage, String usuario) {

        Color turquesa = Color.web("#1F9B7F");
        Color crema = Color.web("#F9F1E6");
        Color marron = Color.web("#736049");
        Font poppins = Font.loadFont(
                getClass().getResourceAsStream("/com/images/global.ttf"), 14
        );

        ImageView iconDashboard = crearIcono("/com/images/iconos/monitor.png");
        ImageView iconAlmacen1 = crearIcono("/com/images/iconos/paquete.png");
        ImageView iconAlmacen2 = crearIcono("/com/images/iconos/nivel-intermedio.png");
        ImageView iconAlmacen3 = crearIcono("/com/images/iconos/taza-de-cafe.png");
        ImageView iconKardex = crearIcono("/com/images/iconos/portapapeles.png");
        ImageView iconOrdenCompra = crearIcono("/com/images/iconos/carrito-de-compras.png");
        ImageView iconVentas = crearIcono("/com/images/iconos/subir.png");
        ImageView iconUsuarios = crearIcono("/com/images/iconos/usuario.png");

        sidebar = new VBox(0);
        sidebar.setPrefWidth(250);
        sidebar.setStyle("-fx-background-color: #736049; -fx-text-fill: white; -fx-font-size: 14px;");

        ScrollPane scrollSidebar = new ScrollPane(sidebar);
        scrollSidebar.setFitToWidth(true);
        scrollSidebar.setFitToHeight(true);
        scrollSidebar.setPrefWidth(256);
        scrollSidebar.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollSidebar.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollSidebar.setPannable(true);
        scrollSidebar.setStyle("""
            -fx-background-color: transparent;
            -fx-background: transparent;
            -fx-border-color: transparent;
            -fx-padding: 0;
        """);

        Label logo = new Label("MamaTania");
        logo.setTextFill(Color.web("#1F9B7F"));
        logo.setFont(Font.loadFont(getClass().getResourceAsStream("/com/images/fontlogo.ttf"), 40));
        Label texto = new Label("Gestion Inventario");
        texto.setTextFill(Paint.valueOf("white"));
        texto.setFont(poppins);

        Region separator1 = new Region();
        separator1.setPrefHeight(0.5);
        separator1.setMaxWidth(Double.MAX_VALUE);
        separator1.setStyle("-fx-background-color: #999999;");

        VBox header = new VBox(2);
        header.getChildren().addAll(logo, texto, separator1);
        header.setPadding(new Insets(0, 24, 0, 24));
        header.setMargin(texto, new Insets(4, 0, 0, 0));

        Button btnDashboard = new Button("Dashboard", iconDashboard);
        Button btnAlmacen1 = new Button("Almacen 1 - Insumos", iconAlmacen1);
        Button btnAlmacen2 = new Button("Almacen 2 - Intermedios", iconAlmacen2);
        Button btnAlmacen3 = new Button("Almacen 3 - Terminados", iconAlmacen3);
        Button btnKardex = new Button("Kardex General", iconKardex);
        Button btnOrdenCompra = new Button("Orden de Compra", iconOrdenCompra);
        Button btnSubirVenta = new Button("Subir Venta", iconVentas);
        Button btnPerfilUsuario = new Button("Perfil de Usuario", iconUsuarios);

        btnDashboard.setOnAction(e -> root.setCenter(new ProductosPage()));
        btnOrdenCompra.setOnAction(e -> root.setCenter(new OrdenesPage()));



        String nombreUsuario = AuthService.obtenerNombre(usuario);
        String rangoUsuario = AuthService.obtenerTipoEmpleado(usuario);
        Label lblnombreUsuario = new Label(nombreUsuario);
        lblnombreUsuario.setTextFill(Paint.valueOf("white"));
        lblnombreUsuario.setFont(Font.loadFont(getClass().getResourceAsStream("/com/images/global.ttf"), 16));
        lblnombreUsuario.setPadding(new Insets(0, 0, -1, 16));
        lblnombreUsuario.setStyle("-fx-font-weight: bold;");
        lblnombreUsuario.setLineSpacing(1);

        Label lblrangoUsuario = new Label(rangoUsuario);
        lblrangoUsuario.setTextFill(Paint.valueOf("#C0BCB6"));
        lblrangoUsuario.setFont(Font.loadFont(getClass().getResourceAsStream("/com/images/global.ttf"), 12));
        lblrangoUsuario.setPadding(new Insets(-1, 0, 0, 16));


        VBox labelfooter = new VBox(2);
        labelfooter.setPadding(new Insets(5, 0, 0, 0));
        for (Label lbl : new Label[]{lblnombreUsuario, lblrangoUsuario}) {
            lbl.setMaxWidth(Double.MAX_VALUE);
            lbl.setAlignment(Pos.CENTER_LEFT);
            lbl.setContentDisplay(ContentDisplay.LEFT);
            labelfooter.getChildren().add(lbl);
        }


        Button btnCerrarSesion = new Button("Cerrar Sesión");
        btnCerrarSesion.setOnAction(e -> {
            LoginPage login = new LoginPage(stage);
            stage.setScene(login.getScene());
        });
        //btnCerrarSesion.setStyle("-fx-border-width: 3px;-fx-border-color: white");
        btnCerrarSesion.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-border-color: #ff4d4d;" +
                        "-fx-border-width: 2px;" +
                        "-fx-border-radius: 6px;" +
                        "-fx-text-fill: #ff4d4d;" +
                        "-fx-padding: 8 14;"
        );

        VBox sidebarcontent = new VBox(2);
        for (Button btn : new Button[]{btnDashboard, btnAlmacen1, btnAlmacen2, btnAlmacen3, btnKardex, btnOrdenCompra, btnSubirVenta, btnPerfilUsuario}) {
            aplicarEstiloBoton(btn);
            sidebarcontent.getChildren().add(btn);
            VBox.setMargin(btn, new Insets(0,0,8,0));
        }
        sidebarcontent.setPadding(new Insets(16));

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Region separator2 = new Region();
        separator2.setPrefHeight(0.5);
        separator2.setMaxWidth(Double.MAX_VALUE);
        separator2.setStyle("-fx-background-color: #999999;");

        VBox footer = new VBox(2);
        footer.getChildren().addAll(new Region(), separator2, labelfooter, btnCerrarSesion);
        footer.setPadding(new Insets(16));

        sidebar.getChildren().addAll(header, sidebarcontent, spacer, footer);
        aplicarEstiloBoton(btnCerrarSesion);


        mainContent = new StackPane();
        mainContent.setStyle("-fx-background-color: #F9F1E6;");

        root = new BorderPane();
        root.setLeft(scrollSidebar);
        root.setCenter(mainContent);

        scene = new Scene(root, 1551, 862);
        stage.setScene(scene);
        stage.show();
    }

    private ImageView crearIcono(String ruta) {
        ImageView icon = new ImageView(new Image(getClass().getResourceAsStream(ruta)));
        icon.setFitWidth(18);
        icon.setFitHeight(18);
        icon.setPreserveRatio(true);

        javafx.scene.effect.Blend blend = new javafx.scene.effect.Blend(
                javafx.scene.effect.BlendMode.SRC_ATOP,
                null,
                new javafx.scene.effect.ColorInput(
                        0, 0,
                        icon.getFitWidth(),
                        icon.getFitHeight(),
                        Color.web("#FFFFFF")
                )
        );
        icon.setEffect(blend);

        return icon;
    }

    private void aplicarEstiloBoton(Button btn) {
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setMinHeight(44);
        btn.setFont(Font.loadFont(getClass().getResourceAsStream("/com/images/global.ttf"), 15));
        btn.setGraphicTextGap(20);
        btn.setPadding(new Insets(12,16,12,16));
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setContentDisplay(ContentDisplay.LEFT);

        String normal = "-fx-background-color: #736049; -fx-text-fill: white;" +
                "-fx-border-radius: 10; -fx-background-radius: 15;";
        String hover = "-fx-background-color: rgba(255,255,255,0.3);" +
                "-fx-text-fill: white; -fx-border-radius: 10; -fx-background-radius: 15;";

        btn.setStyle(normal);
        btn.setWrapText(true);
        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited(e -> btn.setStyle(normal));
    }


    public Scene getScene() {
        return scene;
    }
}
