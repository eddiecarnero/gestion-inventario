package com;

import javax.swing.*;

public class Main extends JFrame {
    private JPanel panel1;
    private JTextField ingreseSuUsuarioTextField;
    private JPasswordField passwordField1;
    private JButton iniciarSesionButton;
    private JLabel imageCafe;

    public Main() {
        setContentPane(panel1);
        setTitle("Gestion Inventario");
        setSize(1020, 640);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new Main().setVisible(true);
        });
    }
}
