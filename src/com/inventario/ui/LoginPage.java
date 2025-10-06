package com.inventario.ui;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.*;

public class LoginPage {

    public static void main(String[] args) {
        // Crear el marco (JFrame)
        JFrame marco = new JFrame("Hola Swing");
        marco.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        marco.setSize(1200, 675);

        // Crear un panel para añadir componentes
        JPanel panel = new JPanel();

        // Crear un botón
        JTextField field1 = new JTextField(50);
        String s = "Ingrese su usuario";

        // Crear una etiqueta
        JLabel etiqueta = new JLabel("Ingrese su usuario: ");

        // Agregar funcionalidad al botón
        field1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                etiqueta.setText("¡Botón pulsado!");
            }
        });

        // Añadir componentes al panel
        panel.add(etiqueta);
        panel.add(field1);

        // Añadir panel al marco
        marco.add(panel);

        // Hacer visible la ventana
        marco.setVisible(true);
    }
}
