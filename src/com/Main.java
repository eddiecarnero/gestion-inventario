package com;

import com.inventario.ui.login.LoginPage;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application  {
    @Override
    public void start(Stage stage) {
        // Iniciar con la página de login
        LoginPage login = new LoginPage(stage);
        stage.setScene(login.getScene());
        stage.setTitle("Mamatania - Login");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
