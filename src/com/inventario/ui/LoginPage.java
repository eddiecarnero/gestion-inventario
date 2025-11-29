package com.inventario.ui;

import com.inventario.config.AuthService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class LoginPage {
    private final Scene scene;

    public LoginPage(Stage stage) {
        Font poppins = Font.loadFont(getClass().getResourceAsStream("/com/images/global.ttf"), 14);

        DropShadow sombra = new DropShadow();
        sombra.setOffsetX(0);
        sombra.setOffsetY(25);
        sombra.setRadius(50);
        sombra.setSpread(0.0);
        sombra.setColor(Color.rgb(0, 0, 0, 0.25));

        VBox main = new VBox(20);
        main.setAlignment(Pos.CENTER);
        main.setPrefSize(448, 500);
        main.setMaxSize(448, 500);
        main.setMinSize(448, 500);
        main.setBackground(new Background(new BackgroundFill(Color.web("#F5EDE0"), new CornerRadii(20), Insets.EMPTY)));

        VBox header = new VBox(10);
        header.setAlignment(Pos.CENTER);
        VBox content = new VBox(10);

        VBox logo = new VBox(10);
        logo.setPrefSize(64, 64);
        logo.setMaxSize(64, 64);
        logo.setMinSize(64, 64);
        logo.setBackground(new Background(new BackgroundFill(Color.web("#736049"), new CornerRadii(100), Insets.EMPTY)));
        logo.setEffect(new DropShadow());
        VBox.setMargin(logo, new Insets(0, 0, 16, 0));
        header.setPadding(new Insets(24, 24, 0, 24));

        ImageView iconLogo = crearIcono("/com/images/iconos/helado.png");
        logo.setAlignment(Pos.CENTER);
        Label nombre = new Label("Mamatania");
        Label descripcion = new Label("Sistema de Gestión de Inventario");


        logo.getChildren().addAll(iconLogo);

        for (Label lbl : new Label[]{nombre, descripcion}) {
            lbl.setMaxWidth(Double.MAX_VALUE);
            lbl.setAlignment(Pos.CENTER);
            lbl.setFont(Font.loadFont(getClass().getResourceAsStream("/com/images/global.ttf"), 16));

        }

        header.getChildren().addAll(logo, nombre, descripcion);

        Label lblemail = new Label("Email");
        lblemail.setMaxWidth(Double.MAX_VALUE);
        lblemail.setFont(Font.loadFont(getClass().getResourceAsStream("/com/images/globalSemiBold.ttf"), 14));


        TextField email = new TextField();
        email.setPromptText("usuario@mamatania.com");


        email.setPrefSize(398, 36);
        email.setMaxSize(398, 36);
        email.setMinSize(398, 36);
        email.setPadding(new Insets(4, 12, 4, 12));
        email.setStyle("-fx-background-color: white; -fx-border-color: #D7CCC8; -fx-border-radius: 8; -fx-background-radius: 8; -fx-border-width: 0;-fx-prompt-text-fill: #736049;");
        email.setFont(Font.loadFont(getClass().getResourceAsStream("/com/images/global.ttf"), 14));

        Label lblpass = new Label("Contraseña");
        lblpass.setMaxWidth(Double.MAX_VALUE);
        lblpass.setFont(Font.loadFont(getClass().getResourceAsStream("/com/images/globalSemiBold.ttf"), 14));
        lblpass.setStyle("-fx-font-weight: bold");

        PasswordField pass = new PasswordField();
        pass.setPrefSize(398, 36);
        pass.setMaxSize(398, 36);
        pass.setMinSize(398, 36);
        pass.setPadding(new Insets(4, 12, 4, 12));
        pass.setStyle("-fx-background-color: white; -fx-border-color: #D7CCC8; -fx-border-radius: 8; -fx-background-radius: 8; -fx-border-width: 0;-fx-prompt-text-fill: #736049;");

        Button btnLogin = new Button("Iniciar Sesión");
        btnLogin.setPrefSize(398, 40);
        btnLogin.setMaxSize(398, 40);
        btnLogin.setMinSize(398, 40);
        btnLogin.setStyle("-fx-background-color: #736049; " + "-fx-text-fill: white; " + "-fx-font-weight: bold; " + "-fx-background-radius: 8;");
        btnLogin.setOnMouseEntered(e -> btnLogin.setStyle("-fx-background-color: rgba(115,96,73,0.6); -fx-text-fill: white; -fx-background-radius: 8;"));
        btnLogin.setOnMouseExited(e -> btnLogin.setStyle("-fx-background-color: #736049; -fx-text-fill: white; -fx-background-radius: 8;"));
        btnLogin.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        btnLogin.setOnAction(e -> {
            String usuario = email.getText().trim();
            String contrasena = pass.getText().trim();

            if (usuario.isEmpty() || contrasena.isEmpty()) {
                mostrarAlerta("Complete todos los campos.");
                return;
            }

            if (usuario.equals("admin") && contrasena.equals("1234")) {
                if (AuthService.existeAdministradorEnBD()) {
                    mostrarAlerta("ACCESO DENEGADO: Ya existe un Administrador registrado.\nEl usuario de recuperación 'admin' ha sido deshabilitado por seguridad.");
                    return;
                }
                SideBar dashboard = new SideBar(stage, "admin");
                stage.setScene(dashboard.getScene());
                return;
            }

            if (AuthService.validarUsuario(usuario, contrasena)) {
                SideBar dashboard = new SideBar(stage, usuario);
                stage.setScene(dashboard.getScene());
            } else {
                mostrarAlerta("Usuario o contraseña incorrectos.");
            }
        });

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        content.getChildren().addAll(lblemail, email, lblpass, pass, btnLogin, spacer);
        content.setPadding(new Insets(0, 24, 24, 24));
        main.setEffect(sombra);

        main.getChildren().addAll(header, content);

        // Fondo con degradado
        Stop[] stops = new Stop[]{new Stop(0.4, Color.web("#736049")), new Stop(1, Color.web("#1F9B7F"))};
        LinearGradient linearGradient = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE, stops);

        BackgroundFill backgroundFill = new BackgroundFill(linearGradient, CornerRadii.EMPTY, Insets.EMPTY);

        // StackPane para centrar el contenido
        StackPane root = new StackPane();
        root.setBackground(new Background(backgroundFill));
        root.getChildren().add(main); //q el VBox queda centrado automáticamente

        // Escena
        scene = new Scene(root, 1200, 720);
        stage.setScene(scene);
        stage.show();
    }

    public Scene getScene() {
        return scene;
    }

    private void mostrarAlerta(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle("Error de autenticación");
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    private ImageView crearIcono(String ruta) {
        java.net.URL url = getClass().getResource(ruta);
        if (url == null) {
            System.err.println("No se encontró la imagen: " + ruta);
            return new ImageView();
        }

        Image image = new Image(url.toExternalForm());
        ImageView icon = new ImageView(image);
        icon.setFitWidth(40);
        icon.setFitHeight(40);
        icon.setPreserveRatio(true);
        return icon;
    }

}
