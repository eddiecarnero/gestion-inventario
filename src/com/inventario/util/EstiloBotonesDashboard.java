package com.inventario.util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class EstiloBotonesDashboard {
    public static void aplicarEstiloBotones(JPanel panel) {
        for (Component comp : panel.getComponents()) {
            if (comp instanceof JButton boton) {

                Color colorNormal = new Color(252, 252, 252);
                Color colorHover = new Color(240, 240, 240); // gris muy claro
                Color colorClick = new Color(220, 220, 220); // gris más notorio

                boton.setBackground(colorNormal);
                boton.setForeground(new Color(40, 40, 40));
                boton.setFont(new Font("Segoe UI", Font.BOLD, 14));
                boton.setFocusPainted(false);
                boton.setBorderPainted(false);
                boton.setCursor(new Cursor(Cursor.HAND_CURSOR));

                boton.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        boton.setBackground(colorHover);
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        boton.setBackground(colorNormal);
                    }

                    @Override
                    public void mousePressed(MouseEvent e) {
                        boton.setBackground(colorClick);
                    }

                    @Override
                    public void mouseReleased(MouseEvent e) {
                        boton.setBackground(colorHover);
                    }
                });

                boton.addActionListener(e -> System.out.println("Click en: " + boton.getText()));
            } else if (comp instanceof JPanel subPanel) {
                aplicarEstiloBotones(subPanel);
            }
        }
    }
}
