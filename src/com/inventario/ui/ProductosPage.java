package com.inventario.ui;

import com.Main;
import com.inventario.model.Producto;
import com.inventario.service.ProductoService;
import com.inventario.util.EstiloBotonesDashboard;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ProductosPage extends JPanel {

    private Main mainFrame;
    private ProductoService productoService;

    // Componentes del formulario (panel izquierdo)
    private JTextField txtNombre;
    private JTextField txtDescripcion;
    private JTextField txtPrecio;
    private JTextField txtStockActual;
    private JTextField txtStockMinimo;
    private JButton btnAgregarProducto;

    // Componentes de la tabla central
    private JTable tablaProductos;
    private DefaultTableModel modeloTabla;

    // Componentes del panel derecho (control de stock)
    private JTextField txtIdProductoActualizar;
    private JTextField txtCantidadAAgregar;
    private JButton btnActualizarStock;
    private JButton btnRefrescar;

    // Paleta de Colores Mejorada
    private static final Color COLOR_PRIMARIO = new Color(64, 180, 168); // Turquesa principal
    private static final Color COLOR_MARRON = new Color(109, 79, 57);   // Marrón del menú
    private static final Color COLOR_FONDO_CONTROLES = new Color(250, 248, 245); // Crema muy claro
    private static final Color COLOR_ACENTO_VERDE = new Color(92, 184, 92); // Verde para botones de éxito
    private static final Color COLOR_PELIGRO = new Color(220, 53, 69); // Rojo para eliminar
    private static final Color COLOR_ALERTA_STOCK = new Color(255, 243, 205); // Amarillo claro para alertas

    public ProductosPage(Main mainFrame, boolean mostrarPanelSuperior) {
        this.mainFrame = mainFrame;
        this.productoService = new ProductoService();

        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBackground(Color.WHITE);

        crearPanelFormulario();
        crearPanelTabla(contentPanel);
        crearPanelControlesStock();

        add(contentPanel, BorderLayout.CENTER);

        cargarProductosEnTabla();
    }

    private void crearPanelFormulario() {
        JPanel panelIzquierdo = new JPanel();
        panelIzquierdo.setLayout(new BoxLayout(panelIzquierdo, BoxLayout.Y_AXIS));
        panelIzquierdo.setBackground(COLOR_FONDO_CONTROLES);
        panelIzquierdo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        panelIzquierdo.setPreferredSize(new Dimension(200, 0)); // Reducido de 290 a 270

        // Título del formulario
        JLabel lblTitulo = new JLabel("NUEVO PRODUCTO");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitulo.setForeground(COLOR_MARRON);
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelIzquierdo.add(lblTitulo);
        panelIzquierdo.add(Box.createVerticalStrut(15));

        txtNombre = crearCampoConEtiqueta(panelIzquierdo, "Nombre del Producto:");
        txtDescripcion = crearCampoConEtiqueta(panelIzquierdo, "Descripción:");
        txtPrecio = crearCampoConEtiqueta(panelIzquierdo, "Precio (S/.):");
        txtStockActual = crearCampoConEtiqueta(panelIzquierdo, "Stock Actual:");
        txtStockMinimo = crearCampoConEtiqueta(panelIzquierdo, "Stock Mínimo:");

        txtStockActual.setText("0");
        txtStockMinimo.setText("0");

        panelIzquierdo.add(Box.createVerticalStrut(20));

        btnAgregarProducto = new JButton("Agregar Producto");
        btnAgregarProducto.setBackground(COLOR_ACENTO_VERDE);
        btnAgregarProducto.setForeground(Color.WHITE);
        btnAgregarProducto.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnAgregarProducto.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnAgregarProducto.setFocusPainted(false);
        btnAgregarProducto.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btnAgregarProducto.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAgregarProducto.addActionListener(e -> agregarNuevoProducto());
        panelIzquierdo.add(btnAgregarProducto);

        EstiloBotonesDashboard.aplicarEstiloBotones(panelIzquierdo);

        add(panelIzquierdo, BorderLayout.WEST);
    }

    private JTextField crearCampoConEtiqueta(JPanel panel, String textoEtiqueta) {
        JLabel etiqueta = new JLabel(textoEtiqueta);
        etiqueta.setFont(new Font("Segoe UI", Font.BOLD, 13));
        etiqueta.setForeground(COLOR_MARRON);
        etiqueta.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextField campo = new JTextField();
        campo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        campo.setMaximumSize(new Dimension(210, 32));
        campo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));

        panel.add(etiqueta);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(campo);
        panel.add(Box.createRigidArea(new Dimension(0, 12)));
        return campo;
    }

    private void crearPanelTabla(JPanel contentPanel) {
        JPanel panelTabla = new JPanel(new BorderLayout(5, 5));
        panelTabla.setBackground(Color.WHITE);
        panelTabla.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        // Título de la tabla
        JLabel lblTituloTabla = new JLabel("📦 Inventario Actual");
        lblTituloTabla.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTituloTabla.setForeground(COLOR_MARRON);
        lblTituloTabla.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        panelTabla.add(lblTituloTabla, BorderLayout.NORTH);

        String[] columnas = {"ID", "Nombre", "Precio", "Stock Actual", "Stock Mínimo", "Estado"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaProductos = new JTable(modeloTabla);
        tablaProductos.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tablaProductos.setRowHeight(30);
        tablaProductos.setShowGrid(false);
        tablaProductos.setIntercellSpacing(new Dimension(0, 0));
        tablaProductos.setSelectionBackground(COLOR_PRIMARIO);
        tablaProductos.setSelectionForeground(Color.WHITE);

        tablaProductos.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tablaProductos.getTableHeader().setBackground(COLOR_PRIMARIO);
        tablaProductos.getTableHeader().setForeground(Color.WHITE);
        tablaProductos.getTableHeader().setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));

        // Renderizador para alerta de stock bajo
        tablaProductos.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                int stockActualCol = 3;
                int stockMinimoCol = 4;

                try {
                    int stockActual = (int) table.getValueAt(row, stockActualCol);
                    int stockMinimo = (int) table.getValueAt(row, stockMinimoCol);

                    if (stockActual <= stockMinimo) {
                        c.setBackground(COLOR_ALERTA_STOCK);
                        c.setForeground(new Color(180, 100, 0));
                        if (column == 5) {
                            c.setFont(c.getFont().deriveFont(Font.BOLD));
                        }
                    } else {
                        c.setBackground(Color.WHITE);
                        c.setForeground(Color.BLACK);
                    }
                } catch (Exception e) {}

                if (isSelected) {
                    c.setBackground(table.getSelectionBackground());
                    c.setForeground(table.getSelectionForeground());
                }
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(tablaProductos);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        panelTabla.add(scrollPane, BorderLayout.CENTER);
        contentPanel.add(panelTabla, BorderLayout.CENTER);
    }

    private void crearPanelControlesStock() {
        JPanel panelDerecho = new JPanel();
        panelDerecho.setLayout(new BoxLayout(panelDerecho, BoxLayout.Y_AXIS));
        panelDerecho.setBackground(COLOR_FONDO_CONTROLES);
        panelDerecho.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(20, 15, 20, 15)
        ));
        panelDerecho.setPreferredSize(new Dimension(200, 0));

        // Título del panel
        JLabel lblTituloControl = new JLabel("CONTROL DE INVENTARIO");
        lblTituloControl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTituloControl.setForeground(COLOR_MARRON);
        lblTituloControl.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelDerecho.add(lblTituloControl);
        panelDerecho.add(Box.createVerticalStrut(20));

        // Sección Aumentar Stock
        JLabel lblAumentar = new JLabel("AUMENTAR STOCK (Recepción)");
        lblAumentar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblAumentar.setForeground(COLOR_MARRON);
        lblAumentar.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelDerecho.add(lblAumentar);
        panelDerecho.add(Box.createVerticalStrut(10));

        txtIdProductoActualizar = crearCampoConEtiqueta(panelDerecho, "ID Producto:");
        txtCantidadAAgregar = crearCampoConEtiqueta(panelDerecho, "Cantidad de Ingreso:");

        btnActualizarStock = new JButton("➕ Añadir al Stock");
        btnActualizarStock.setBackground(COLOR_PRIMARIO);
        btnActualizarStock.setForeground(Color.WHITE);
        btnActualizarStock.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnActualizarStock.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnActualizarStock.setFocusPainted(false);
        btnActualizarStock.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        btnActualizarStock.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnActualizarStock.addActionListener(e -> actualizarStockDeProducto());
        panelDerecho.add(btnActualizarStock);

        // Separador visual
        panelDerecho.add(Box.createVerticalStrut(20));
        JSeparator separador1 = new JSeparator();
        separador1.setMaximumSize(new Dimension(250, 1));
        separador1.setForeground(new Color(200, 200, 200));
        panelDerecho.add(separador1);
        panelDerecho.add(Box.createVerticalStrut(20));

        // Sección Eliminar Producto
        JLabel lblEliminar = new JLabel("ELIMINAR PRODUCTO");
        lblEliminar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblEliminar.setForeground(COLOR_MARRON);
        lblEliminar.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelDerecho.add(lblEliminar);
        panelDerecho.add(Box.createVerticalStrut(10));

        JTextField txtIdProductoEliminar = crearCampoConEtiqueta(panelDerecho, "ID Producto a Eliminar:");

        JButton btnEliminarProducto = new JButton("🗑️ Eliminar Producto");
        btnEliminarProducto.setBackground(COLOR_PELIGRO);
        btnEliminarProducto.setForeground(Color.WHITE);
        btnEliminarProducto.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnEliminarProducto.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnEliminarProducto.setFocusPainted(false);
        btnEliminarProducto.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        btnEliminarProducto.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnEliminarProducto.addActionListener(e -> confirmarYEliminarProducto(txtIdProductoEliminar.getText()));
        panelDerecho.add(btnEliminarProducto);

        panelDerecho.add(Box.createVerticalStrut(20));

        // Separador visual
        JSeparator separador2 = new JSeparator();
        separador2.setMaximumSize(new Dimension(250, 1));
        separador2.setForeground(new Color(200, 200, 200));
        panelDerecho.add(separador2);
        panelDerecho.add(Box.createVerticalStrut(20));

        // Botón Refrescar
        btnRefrescar = new JButton("🔄 Refrescar Tabla");
        btnRefrescar.setBackground(Color.WHITE);
        btnRefrescar.setForeground(COLOR_PRIMARIO);
        btnRefrescar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnRefrescar.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnRefrescar.setFocusPainted(false);
        btnRefrescar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_PRIMARIO, 2),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        btnRefrescar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRefrescar.addActionListener(e -> cargarProductosEnTabla());
        panelDerecho.add(btnRefrescar);

        EstiloBotonesDashboard.aplicarEstiloBotones(panelDerecho);

        add(panelDerecho, BorderLayout.EAST);
    }

    // --- LÓGICA DE NEGOCIO (UI Actions) ---

    private void agregarNuevoProducto() {
        try {
            if (txtNombre.getText().trim().isEmpty() || txtPrecio.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nombre y Precio son obligatorios.", "Error de Validación", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Producto nuevoProducto = new Producto(
                    0,
                    txtNombre.getText().trim(),
                    txtDescripcion.getText().trim(),
                    Double.parseDouble(txtPrecio.getText().trim()),
                    Integer.parseInt(txtStockActual.getText().trim()),
                    Integer.parseInt(txtStockMinimo.getText().trim())
            );

            if (productoService.agregarNuevoProducto(nuevoProducto)) {
                JOptionPane.showMessageDialog(this, "Producto agregado exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                limpiarCamposFormulario();
                cargarProductosEnTabla();
            } else {
                JOptionPane.showMessageDialog(this, "Error al agregar producto. Puede que el nombre ya exista.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Asegúrate de que Precio y Stock sean números válidos.", "Error de Formato", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void actualizarStockDeProducto() {
        try {
            int idProducto = Integer.parseInt(txtIdProductoActualizar.getText().trim());
            int cantidadAAgregar = Integer.parseInt(txtCantidadAAgregar.getText().trim());

            if (cantidadAAgregar <= 0) {
                JOptionPane.showMessageDialog(this, "La cantidad a añadir debe ser positiva.", "Error de Validación", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (productoService.aumentarStockProducto(idProducto, cantidadAAgregar)) {
                JOptionPane.showMessageDialog(this, "Stock actualizado exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                txtIdProductoActualizar.setText("");
                txtCantidadAAgregar.setText("");
                cargarProductosEnTabla();
            } else {
                JOptionPane.showMessageDialog(this, "Error al actualizar stock. Verifica el ID del producto.", "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Por favor, ingresa números válidos para ID y Cantidad.", "Error de Formato", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarProductosEnTabla() {
        modeloTabla.setRowCount(0);
        List<Producto> productos = productoService.obtenerTodosLosProductos();

        for (Producto p : productos) {
            String estado = (p.getStockActual() <= p.getStockMinimo()) ? "🚨 BAJO STOCK" : "EN STOCK";
            Object[] fila = {
                    p.getId(),
                    p.getNombre(),
                    p.getPrecio(),
                    p.getStockActual(),
                    p.getStockMinimo(),
                    estado
            };
            modeloTabla.addRow(fila);
        }
    }

    private void limpiarCamposFormulario() {
        txtNombre.setText("");
        txtDescripcion.setText("");
        txtPrecio.setText("");
        txtStockActual.setText("0");
        txtStockMinimo.setText("0");
        txtNombre.requestFocus();
    }

    private void confirmarYEliminarProducto(String idText) {
        try {
            int idProducto = Integer.parseInt(idText.trim());

            int confirmacion = JOptionPane.showConfirmDialog(
                    this,
                    "¿Está seguro de que desea ELIMINAR el producto con ID: " + idProducto + "?\nEsta acción no se puede deshacer.",
                    "Confirmar Eliminación",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (confirmacion == JOptionPane.YES_OPTION) {
                if (productoService.eliminarProducto(idProducto)) {
                    JOptionPane.showMessageDialog(this, "✅ Producto eliminado exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    cargarProductosEnTabla();
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Error al eliminar. Verifique que el ID sea correcto o que el producto no esté siendo usado en otra tabla.",
                            "Error de Eliminación",
                            JOptionPane.ERROR_MESSAGE);
                }
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Por favor, ingrese un ID de Producto válido (número).", "Error de Formato", JOptionPane.ERROR_MESSAGE);
        }
    }
}