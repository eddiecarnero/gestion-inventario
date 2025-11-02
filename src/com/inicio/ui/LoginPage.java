package com.inicio.ui;
import com.inventario.config.AuthService;

import com.inventario.ui.SideBar;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import javafx.scene.paint.Color;
import javafx.scene.effect.DropShadow;

public class LoginPage {
    private Scene scene;
    public LoginPage(Stage stage) {
        // PANEL IZQUIERDO
        VBox panelIzquierdo = new VBox(20);
        panelIzquierdo.setAlignment(Pos.CENTER);
        panelIzquierdo.setStyle("-fx-background-color: #F5EDE0;"); // color crema claro
        panelIzquierdo.setPrefWidth(700);

        // TÍTULO PRINCIPAL
        Text titulo = new Text("Mamatania");
        titulo.setFont(Font.loadFont(getClass().getResourceAsStream("/com/images/fontlogo.ttf"), 45));
        titulo.setFill(Color.web("#3E2723"));
        titulo.setEffect(new DropShadow(3, Color.rgb(120, 90, 70)));

        // SUBTÍTULOS
        Label subtitulo = new Label("Bienvenido a Mamatania");
        subtitulo.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        subtitulo.setTextFill(Color.web("#3E2723"));

        Label instruccion = new Label("Ingrese sus datos");
        instruccion.setFont(Font.font("Arial", 20));
        instruccion.setTextFill(Color.web("#4E342E"));

        // CAMPOS
        VBox campos = new VBox(15);
        campos.setAlignment(Pos.CENTER);

        Label usuarioLabel = new Label("USUARIO");
        usuarioLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        usuarioLabel.setTextFill(Color.web("#3E2723"));
        usuarioLabel.setAlignment(Pos.CENTER_LEFT);

        TextField campoUsuario = new TextField();
        campoUsuario.setPromptText("Ingrese su usuario");
        campoUsuario.setMinWidth(300);
        campoUsuario.setPrefWidth(300);
        campoUsuario.setMaxWidth(300);
        campoUsuario.setStyle("-fx-background-color: white; -fx-border-color: #D7CCC8; -fx-border-radius: 8; -fx-background-radius: 8;");

        Label contrasenaLabel = new Label("CONTRASEÑA");
        contrasenaLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        contrasenaLabel.setTextFill(Color.web("#3E2723"));
        contrasenaLabel.setAlignment(Pos.CENTER_LEFT);

        PasswordField campoContrasena = new PasswordField();
        campoContrasena.setPromptText("************");
        campoContrasena.setMinWidth(300);
        campoContrasena.setPrefWidth(300);
        campoContrasena.setMaxWidth(300);
        campoContrasena.setStyle("-fx-background-color: white; -fx-border-color: #D7CCC8; -fx-border-radius: 8; -fx-background-radius: 8;");


        Button btnLogin = new Button("Iniciar Sesión");
        btnLogin.setPrefWidth(300);
        btnLogin.setPrefHeight(40);
        btnLogin.setStyle(
                "-fx-background-color: #6D4C41; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-radius: 8;"
        );
        btnLogin.setOnMouseEntered(e -> btnLogin.setStyle("-fx-background-color: #5D4037; -fx-text-fill: white; -fx-background-radius: 8;"));
        btnLogin.setOnMouseExited(e -> btnLogin.setStyle("-fx-background-color: #6D4C41; -fx-text-fill: white; -fx-background-radius: 8;"));
        btnLogin.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        btnLogin.setOnAction(e -> {
            String usuario = campoUsuario.getText().trim();
            String contrasena = campoContrasena.getText().trim();

            if (usuario.isEmpty() || contrasena.isEmpty()) {
                mostrarAlerta("Por favor, complete todos los campos.");
                return;
            }

            boolean valido = AuthService.validarUsuario(usuario, contrasena);

            if (valido) {
                SideBar dashboard = new SideBar(stage, usuario);
                stage.setScene(dashboard.getScene());
            } else {
                mostrarAlerta("Usuario o contraseña incorrectos.");
            }
        });

        Label olvidaste = new Label("¿Olvidaste Contraseña?");
        olvidaste.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
        olvidaste.setTextFill(Color.web("#4E342E"));
        olvidaste.setOnMouseEntered(e -> olvidaste.setTextFill(Color.web("#6D4C41")));
        olvidaste.setOnMouseExited(e -> olvidaste.setTextFill(Color.web("#4E342E")));

        campos.getChildren().addAll(usuarioLabel, campoUsuario, contrasenaLabel, campoContrasena, btnLogin, olvidaste);

        panelIzquierdo.getChildren().addAll(titulo, subtitulo, instruccion, campos);

        // PANEL DERECHO (IMAGEN AJUSTABLE)
        Pane panelDerecho = new Pane();
        panelDerecho.setStyle(
                "-fx-background-image: url('/com/images/cafe_1.jpg');" +
                        "-fx-background-size: cover;" +
                        "-fx-background-position: center;" +
                        "-fx-background-repeat: no-repeat;"
        );
        panelDerecho.setPrefWidth(600);

        HBox root = new HBox();
        root.getChildren().addAll(panelIzquierdo, panelDerecho);
        HBox.setHgrow(panelDerecho, Priority.ALWAYS);

        scene = new Scene(root, 1100, 700);
        stage.setTitle("Mamatania - Login");


        System.out.println(getClass().getResource("/com/images/cafe_1.jpg"));
        System.out.println(getClass().getResource("/com/images/fontlogo.ttf"));


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

}



