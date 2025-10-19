package com;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.inicio.ui.*;
import com.inventario.ui.*;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.File;
import java.util.Locale;

import com.inventario.ui.ProductosPage;
import com.inventario.ui.InventarioPanelBase;

public class Main extends JFrame {
   private JPanel mainPanel;

    public Main() {
        setTitle("Aplicación Principal");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1020, 640);
        setLocationRelativeTo(null);
        setResizable(false);

        // creamos el panel principal
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        setContentPane(mainPanel);

        // mostrar el login al iniciar
        mostrarLogin();

        setVisible(true);
    }

    public void mostrarLogin() {
        login loginForm = new login(this);
        mainPanel.removeAll();
        mainPanel.add(loginForm.getLogin(), BorderLayout.CENTER);
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    public void mostrarDashboard() {
        MainDashboard dashboard = new MainDashboard(this);
        mainPanel.removeAll();
        mainPanel.add(dashboard.getMainDashboard(), BorderLayout.CENTER);
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    public void mostrarInventario() {
        InventarioPage dashboard = new InventarioPage(this);
        mainPanel.removeAll();
        mainPanel.add(dashboard.getInventario(), BorderLayout.CENTER);
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::new);
    }

    public void mostrarProductos() {
        InventarioPanelBase inventarioBase = new InventarioPanelBase(this); // Creamos la nueva base con menú
        mainPanel.removeAll();
        mainPanel.add(inventarioBase, BorderLayout.CENTER);
        mainPanel.revalidate();
        mainPanel.repaint();
    }
}
