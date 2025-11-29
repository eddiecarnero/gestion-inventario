package com.inventario.ui;

import com.inventario.config.AuthService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class SideBar {
    private final BorderPane root;
    private final VBox sidebar;
    private final StackPane mainContent; // <-- Este es el panel donde irá el contenido
    private final Scene scene;

    private final String usuarioActualSesion;

    public SideBar(Stage stage, String usuario) {
        this.usuarioActualSesion = usuario;
        // --- Estilos y Fuentes ---
        Color turquesa = Color.web("#1F9B7F");
        Color crema = Color.web("#F9F1E6");
        Color marron = Color.web("#736049");
        Font poppins = Font.loadFont(getClass().getResourceAsStream("/com/images/global.ttf"), 14);

        // --- Iconos ---
        ImageView iconDashboard = crearIcono("/com/images/iconos/monitor.png");
        ImageView iconAlmacen1 = crearIcono("/com/images/iconos/paquete.png");
        ImageView iconAlmacen2 = crearIcono("/com/images/iconos/nivel-intermedio.png");
        ImageView iconAlmacen3 = crearIcono("/com/images/iconos/taza-de-cafe.png");
        ImageView iconIngrediente = crearIcono("/com/images/iconos/ingrediente.png");
        ImageView iconOrdenCompra = crearIcono("/com/images/iconos/carrito-de-compras.png");
        ImageView iconVentas = crearIcono("/com/images/iconos/subir.png");
        ImageView iconUsuarios = crearIcono("/com/images/iconos/usuario.png");
        ImageView iconRecetas = crearIcono("/com/images/iconos/receta.png");
        ImageView iconSubirInsumos = crearIcono("/com/images/iconos/descarga.png");
        // ImageView iconKardex = crearIcono("/com/images/iconos/portapapeles.png");


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

        // --- Header del Sidebar ---
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
        VBox.setMargin(texto, new Insets(4, 0, 0, 0));

        // --- Botones del Menú ---
        Button btnDashboard = new Button("Dashboard", iconDashboard);
        Button btnAlmacen1 = new Button("Almacen 1 - Vista", iconAlmacen1);
        Button btnIngrediente = new Button("Gestionar Productos", iconIngrediente);
        Button btnAlmacen2 = new Button("Almacen 2 - Intermedios", iconAlmacen2);
        Button btnAlmacen3 = new Button("Almacen 3 - Terminados", iconAlmacen3);
        Button btnRecetas = new Button("Recetas", iconRecetas);
        Button btnOrdenCompra = new Button("Orden de Compra", iconOrdenCompra);
        Button btnSubirVenta = new Button("Subir Venta", iconVentas);
        Button btnPerfilUsuario = new Button("Perfil de Usuario", iconUsuarios);
        // Button btnKardex = new Button("Kardex", iconKardex);
        Button btnSubirInsumos = new Button("Ingreso Insumos", iconSubirInsumos);
        Button btnGestionInsumos = new Button("Gestionar Insumos", iconIngrediente);

        // --- LÓGICA DE NAVEGACIÓN CENTRALIZADA ---
        mainContent = new StackPane();
        mainContent.setStyle("-fx-background-color: #F9F1E6;");

        btnDashboard.setOnAction(e -> navegar("dashboard"));
        btnAlmacen1.setOnAction(e -> navegar("almacen1")); // Vista de Almacen1Page
        btnIngrediente.setOnAction(e -> navegar("productos-crud")); // Vista de ProductosPage
        btnOrdenCompra.setOnAction(e -> navegar("orden-compra"));
        btnRecetas.setOnAction(e -> navegar("recetas"));

        // Botones no implementados

        btnAlmacen2.setOnAction(e -> navegar("almacen2"));
        btnAlmacen3.setOnAction(e -> navegar("almacen3"));
        btnSubirVenta.setOnAction(e -> navegar("subir-ventas"));
        btnPerfilUsuario.setOnAction(e -> navegar("perfil"));
        btnSubirInsumos.setOnAction(e -> navegar("subir-insumos"));
        btnGestionInsumos.setOnAction(e -> navegar("gestion-insumos"));

        // btnKardex.setOnAction(e -> navegar("kardex"));

        navegar("dashboard");


        // --- Footer del Sidebar (Info de Usuario) ---
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

        // --- Ensamblaje del Sidebar ---
        VBox sidebarcontent = new VBox(2);

        for (Button btn : new Button[]{btnDashboard, btnAlmacen1, btnAlmacen2, btnAlmacen3, btnGestionInsumos, btnRecetas, btnSubirVenta, btnPerfilUsuario}) {
            aplicarEstiloBoton(btn);
            sidebarcontent.getChildren().add(btn);
            VBox.setMargin(btn, new Insets(0, 0, 8, 0));
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

        // --- Ensamblaje Final de la Escena ---
        root = new BorderPane();
        root.setLeft(scrollSidebar);
        root.setCenter(mainContent);

        scene = new Scene(root, 1200, 720);
        stage.setScene(scene);
        stage.show();
    }

    // --- MÉTODO DE NAVEGACIÓN CENTRALIZADO ---
    private void navegar(String pageName) {
        Node pagina = null;

        switch (pageName) {
            case "almacen2":
                pagina = new Almacen2Page(this::navegar);
                break;
            case "dashboard":
                pagina = new DashboardPage(this::navegar);
                break;
            case "almacen1":
                pagina = new Almacen1Page(this::navegar);
                break;
            case "productos-crud": // Botón "Ingrediente"
                pagina = new ProductosPage();
                break;
            case "orden-compra":
                pagina = new OrdenesPage();
                break;
            case "recetas":
                pagina = new RecetasPage();
                break;
            case "almacen3":
                pagina = new Almacen3Page(this::navegar);
                break;
            case "subir-ventas":
                pagina = new SubirVentasPage();
                break;
            case "subir-insumos":
                pagina = new SubirInsumosPage();
                break;
            case "gestion-insumos":
                pagina = new GestionInsumosPage();
                break;
            case "perfil":
                pagina = new PerfilPage(usuarioActualSesion);
                break;
            default:
                System.out.println("Navegación a página no implementada: " + pageName);
                Label label = new Label("Página '" + pageName + "' no encontrada.");
                label.setAlignment(Pos.CENTER);
                pagina = new StackPane(label);
                break;
        }

        if (pagina != null && mainContent != null) {
            mainContent.getChildren().setAll(pagina);
        } else {
            System.err.println("Error: mainContent es nulo o la página es nula.");
        }
    }


    private ImageView crearIcono(String ruta) {
        Image img = new Image(getClass().getResourceAsStream(ruta));
        if (img.isError()) {
            System.err.println("Error al cargar icono: " + ruta);
            return new ImageView();
        }

        ImageView icon = new ImageView(img);
        icon.setFitWidth(18);
        icon.setFitHeight(18);
        icon.setPreserveRatio(true);

        javafx.scene.effect.Blend blend = new javafx.scene.effect.Blend(javafx.scene.effect.BlendMode.SRC_ATOP, null, new javafx.scene.effect.ColorInput(0, 0, icon.getFitWidth(), icon.getFitHeight(), Color.web("#FFFFFF")));
        icon.setEffect(blend);

        return icon;
    }

    private void aplicarEstiloBoton(Button btn) {
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setMinHeight(44);
        btn.setFont(Font.loadFont(getClass().getResourceAsStream("/com/images/global.ttf"), 15));
        btn.setGraphicTextGap(20);
        btn.setPadding(new Insets(12, 16, 12, 16));
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setContentDisplay(ContentDisplay.LEFT);

        String normal = "-fx-background-color: #736049; -fx-text-fill: white;" + "-fx-border-radius: 10; -fx-background-radius: 15;";
        String hover = "-fx-background-color: rgba(255,255,255,0.3);" + "-fx-text-fill: white; -fx-border-radius: 10; -fx-background-radius: 15;";

        btn.setStyle(normal);
        btn.setWrapText(true);
        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited(e -> btn.setStyle(normal));
    }


    public Scene getScene() {
        return scene;
    }
}