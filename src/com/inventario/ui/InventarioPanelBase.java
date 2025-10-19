package com.inventario.ui;

import com.Main;
import com.inventario.util.EstiloBotonesDashboard;

import javax.swing.*;
import java.awt.*;

public class InventarioPanelBase extends JPanel {

    private Main mainFrame;
    private JPanel contentArea;

    // Paleta de Colores Mejorada (igual que ProductosPage)
    private static final Color COLOR_PRIMARIO = new Color(64, 180, 168); // Turquesa principal
    private static final Color COLOR_MARRON = new Color(109, 79, 57);   // Marrón del menú
    private static final Color COLOR_FONDO_CONTROLES = new Color(250, 248, 245); // Crema muy claro
    private static final Color COLOR_TEXTO_MENU = new Color(245, 245, 245); // Casi blanco

    public InventarioPanelBase(Main mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        crearMenuLateral();
        contentArea = new JPanel(new BorderLayout());
        contentArea.setBackground(Color.WHITE);
        add(contentArea, BorderLayout.CENTER);

        mostrarVistaProductos();
    }

    private void crearMenuLateral() {
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setBackground(COLOR_MARRON);
        menuPanel.setPreferredSize(new Dimension(150, getHeight())); // Aumentado de 200 a 210
        menuPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(25, 10, 25, 10) // Reducido padding horizontal
        ));

        // Título del menú con icono
        JLabel lblTituloMenu = new JLabel("📦 INVENTARIO");
        lblTituloMenu.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblTituloMenu.setForeground(COLOR_TEXTO_MENU);
        lblTituloMenu.setAlignmentX(Component.CENTER_ALIGNMENT);
        menuPanel.add(lblTituloMenu);
        menuPanel.add(Box.createVerticalStrut(35));

        // Botones del menú lateral
        JButton btnGestionProductos = crearBotonMenu("📋 Gestión Productos");
        btnGestionProductos.addActionListener(e -> mostrarVistaProductos());
        menuPanel.add(btnGestionProductos);
        menuPanel.add(Box.createVerticalStrut(10));

        // Espacio para futuros botones
        // JButton btnEntradasSalidas = crearBotonMenu("📊 Entradas / Salidas");
        // btnEntradasSalidas.addActionListener(e -> { /* Cargar otro panel */ });
        // menuPanel.add(btnEntradasSalidas);
        // menuPanel.add(Box.createVerticalStrut(10));

        // Empuja el botón de volver hacia abajo
        menuPanel.add(Box.createVerticalGlue());

        // Separador visual antes del botón de volver
        JSeparator separador = new JSeparator();
        separador.setMaximumSize(new Dimension(190, 1));
        separador.setForeground(new Color(150, 120, 100));
        menuPanel.add(separador);
        menuPanel.add(Box.createVerticalStrut(15));

        JButton btnVolverDashboard = crearBotonMenu("⬅️ Volver Dashboard");
        btnVolverDashboard.setBackground(new Color(90, 65, 50)); // Más oscuro
        btnVolverDashboard.addActionListener(e -> mainFrame.mostrarDashboard());
        menuPanel.add(btnVolverDashboard);

        EstiloBotonesDashboard.aplicarEstiloBotones(menuPanel);

        add(menuPanel, BorderLayout.WEST);
    }

    private JButton crearBotonMenu(String texto) {
        JButton button = new JButton(texto);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 13)); // Reducido de 14 a 13
        button.setForeground(COLOR_TEXTO_MENU);
        button.setBackground(COLOR_MARRON);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(190, 45)); // Aumentado de 180 a 190
        button.setMinimumSize(new Dimension(190, 45));
        button.setPreferredSize(new Dimension(190, 45));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12)); // Reducido padding

        // Efecto hover mejorado
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(COLOR_PRIMARIO);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(COLOR_MARRON);
            }
        });
        return button;
    }

    private void mostrarVistaProductos() {
        contentArea.removeAll();
        ProductosPage productosPage = new ProductosPage(mainFrame, false);
        contentArea.add(productosPage, BorderLayout.CENTER);
        contentArea.revalidate();
        contentArea.repaint();
    }
}