package com;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.File;

public class Main extends JFrame {
    private JPanel panel1;
    private JTextField ingreseSuUsuarioTextField;
    private JPasswordField passwordField1;
    private JButton iniciarSesionButton;
    private JLabel logotexto;
    private JLabel imageCafe;

    public Main() {
        setContentPane(panel1);
        setTitle("Gestion Inventario");
        setSize(1020, 640);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        logotexto = new JLabel("MamaTania");
        try {
            Font fuente = Font.createFont(Font.TRUETYPE_FONT,
                    getClass().getResourceAsStream("/com/images/fontlogo.ttf")).deriveFont(36f);
            logotexto.setFont(fuente);
            System.out.println("✅ Fuente cargada correctamente: " + fuente.getFontName());
            System.out.println("Fuente aplicada al JLabel: " + logotexto.getFont().getFontName());
        } catch (Exception e) {
            e.printStackTrace();
            logotexto.setFont(new Font("Arial", Font.PLAIN, 20));
            System.out.println("❌ No se pudo cargar la fuente:");
        }
        ingreseSuUsuarioTextField.setText("Ingrese su usuario");
        ingreseSuUsuarioTextField.setForeground(Color.GRAY);
        ingreseSuUsuarioTextField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (ingreseSuUsuarioTextField.getText().equals("Ingrese su usuario")) {
                    ingreseSuUsuarioTextField.setText("");
                    ingreseSuUsuarioTextField.setForeground(Color.BLACK);
                }
            }
            public void focusLost(FocusEvent e) {
                if(ingreseSuUsuarioTextField.getText().isEmpty()) {
                    ingreseSuUsuarioTextField.setText("Ingrese su usuario");
                    ingreseSuUsuarioTextField.setForeground(Color.GRAY);
                }
            }
        });
        passwordField1.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if(passwordField1.getText().equals("*************")) {
                    passwordField1.setText("");
                    passwordField1.setForeground(Color.BLACK);
                }
            }
            public void focusLost(FocusEvent e) {
                if(passwordField1.getText().isEmpty()) {
                    passwordField1.setText("**************");
                    passwordField1.setForeground(Color.GRAY);
                }
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new Main().setVisible(true);
        });
    }
}
