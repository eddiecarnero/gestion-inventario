package com.inventario.ui;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class OrdenesPage extends JPanel {
    private static final Color PRIMARY = new Color(74, 144, 226);
    private static final Color BG_LIGHT = new Color(253, 248, 240);
    private static final Color SIDEBAR_LIGHT = new Color(234, 224, 209);
    private static final Color TEXT_LIGHT = new Color(51, 51, 51);
    private static final Color BORDER_LIGHT = new Color(204, 204, 204);

    private DefaultTableModel tableModel;
    private JLabel totalLabel;
    private JTextField insumoField, cantidadField, precioField;
    private JComboBox<String> unidadCombo;

    public OrdenesPage() {
        setLayout(new BorderLayout());
        setBackground(BG_LIGHT);

        // Sidebar
        add(createSidebar(), BorderLayout.WEST);

        // Contenido principal
        add(createMainContent(), BorderLayout.CENTER);
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(SIDEBAR_LIGHT);
        sidebar.setPreferredSize(new Dimension(260, 0));
        sidebar.setBorder(new EmptyBorder(20, 15, 20, 15));

        // Logo y título
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(SIDEBAR_LIGHT);
        headerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel titleLabel = new JLabel("Heladería & Cafetería");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(TEXT_LIGHT);

        JLabel subtitleLabel = new JLabel("Administrador");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLabel.setForeground(TEXT_LIGHT);

        headerPanel.add(titleLabel);
        headerPanel.add(Box.createRigidArea(new Dimension(0, 3)));
        headerPanel.add(subtitleLabel);
        headerPanel.add(Box.createRigidArea(new Dimension(0, 30)));

        sidebar.add(headerPanel);

        // Menú de navegación
        String[] menuItems = {"Dashboard", "Inventario", "Proveedores", "Kardex", "Recetas", "Ventas"};
        String[] menuIcons = {"📊", "📦", "👥", "📜", "📖", "📈"};

        for (int i = 0; i < menuItems.length; i++) {
            JButton menuButton = createMenuButton(menuIcons[i] + "  " + menuItems[i], i == 1);
            sidebar.add(menuButton);
            sidebar.add(Box.createRigidArea(new Dimension(0, 5)));
        }

        // Espaciador
        sidebar.add(Box.createVerticalGlue());

        // Botón de cerrar sesión
        JButton logoutButton = createMenuButton("🚪  Cerrar Sesión", false);
        sidebar.add(logoutButton);

        return sidebar;
    }

    private JButton createMenuButton(String text, boolean selected) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        button.setForeground(TEXT_LIGHT);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        if (selected) {
            button.setBackground(new Color(74, 144, 226, 50));
            button.setForeground(PRIMARY);
            button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        } else {
            button.setBackground(SIDEBAR_LIGHT);
        }

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (!selected) {
                    button.setBackground(new Color(74, 144, 226, 30));
                }
            }
            public void mouseExited(MouseEvent e) {
                if (!selected) {
                    button.setBackground(SIDEBAR_LIGHT);
                }
            }
        });

        return button;
    }

    private JPanel createMainContent() {
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(BG_LIGHT);
        contentPanel.setBorder(new EmptyBorder(30, 40, 30, 40));

        // Header
        JLabel headerLabel = new JLabel("Crear Nueva Orden de Compra");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        headerLabel.setForeground(TEXT_LIGHT);
        headerLabel.setBorder(new EmptyBorder(0, 0, 25, 0));

        contentPanel.add(headerLabel, BorderLayout.NORTH);

        // Formulario principal
        JPanel formPanel = new JPanel(new BorderLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_LIGHT, 1),
                new EmptyBorder(25, 30, 30, 30)
        ));

        // Panel de campos superiores
        JPanel topFieldsPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        topFieldsPanel.setBackground(Color.WHITE);
        topFieldsPanel.setBorder(new EmptyBorder(0, 0, 30, 0));

        // Proveedor
        JPanel proveedorPanel = createFieldPanel("Proveedor",
                new JComboBox<>(new String[]{"Seleccionar Proveedor", "Proveedor Lácteos del Sur",
                        "Distribuidora de Frutas S.A.", "Insumos de Café \"El Grano Dorado\""}));

        // Fecha
        JTextField fechaField = new JTextField(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        JPanel fechaPanel = createFieldPanel("Fecha de la Orden", fechaField);

        topFieldsPanel.add(proveedorPanel);
        topFieldsPanel.add(fechaPanel);

        formPanel.add(topFieldsPanel, BorderLayout.NORTH);

        // Tabla de detalles
        formPanel.add(createTablePanel(), BorderLayout.CENTER);

        // Panel de botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(new EmptyBorder(20, 0, 0, 0));

        JButton saveButton = createStyledButton("Guardar", false);
        JButton submitButton = createStyledButton("Enviar para Aprobación", true);

        buttonPanel.add(saveButton);
        buttonPanel.add(submitButton);

        formPanel.add(buttonPanel, BorderLayout.SOUTH);

        contentPanel.add(formPanel, BorderLayout.CENTER);

        return contentPanel;
    }

    private JPanel createFieldPanel(String label, JComponent component) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);

        JLabel labelComp = new JLabel(label);
        labelComp.setFont(new Font("Segoe UI", Font.BOLD, 13));
        labelComp.setForeground(TEXT_LIGHT);
        labelComp.setAlignmentX(Component.LEFT_ALIGNMENT);

        component.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        component.setPreferredSize(new Dimension(0, 40));
        component.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        component.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(labelComp);
        panel.add(Box.createRigidArea(new Dimension(0, 8)));
        panel.add(component);

        return panel;
    }

    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);

        // Título de sección
        JLabel sectionTitle = new JLabel("Detalle de la Orden");
        sectionTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        sectionTitle.setForeground(TEXT_LIGHT);
        sectionTitle.setBorder(new EmptyBorder(20, 0, 15, 0));
        tablePanel.add(sectionTitle, BorderLayout.NORTH);

        // Tabla
        String[] columns = {"Insumo", "Cantidad", "Unidad", "Precio Unitario", "Total", "Acción"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5;
            }
        };

        // Datos de ejemplo
        tableModel.addRow(new Object[]{"Leche Entera", "10", "Litros", "$20,00", "$200,00", "Eliminar"});
        tableModel.addRow(new Object[]{"Azúcar Refinada", "5", "Kilos", "$15,00", "$75,00", "Eliminar"});

        JTable table = new JTable(tableModel);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setRowHeight(40);
        table.setShowGrid(true);
        table.setGridColor(BORDER_LIGHT);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(Color.WHITE);
        table.getTableHeader().setForeground(TEXT_LIGHT);

        // Renderizador para el botón de eliminar
        table.getColumn("Acción").setCellRenderer(new ButtonRenderer());
        table.getColumn("Acción").setCellEditor(new ButtonEditor(new JCheckBox()));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new LineBorder(BORDER_LIGHT, 1));

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(Color.WHITE);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        // Panel de entrada de nuevos items - ASEGURAR QUE SE VEA
        JPanel inputRowWrapper = new JPanel(new BorderLayout());
        inputRowWrapper.setBackground(Color.WHITE);
        inputRowWrapper.setBorder(new EmptyBorder(10, 0, 0, 0));
        inputRowWrapper.add(createInputRow(), BorderLayout.CENTER);
        centerPanel.add(inputRowWrapper, BorderLayout.SOUTH);

        // Panel de total
        JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        totalPanel.setBackground(Color.WHITE);
        totalPanel.setBorder(new EmptyBorder(15, 0, 0, 0));

        JPanel totalBox = new JPanel();
        totalBox.setLayout(new BoxLayout(totalBox, BoxLayout.Y_AXIS));
        totalBox.setBackground(Color.WHITE);

        JLabel totalTextLabel = new JLabel("Total de la Orden:");
        totalTextLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        totalTextLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);

        totalLabel = new JLabel("$275,00");
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        totalLabel.setForeground(PRIMARY);
        totalLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);

        totalBox.add(totalTextLabel);
        totalBox.add(totalLabel);

        totalPanel.add(totalBox);

        // Contenedor para input y total
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.add(inputRowWrapper, BorderLayout.NORTH);
        bottomPanel.add(totalPanel, BorderLayout.SOUTH);

        centerPanel.add(bottomPanel, BorderLayout.SOUTH);

        tablePanel.add(centerPanel, BorderLayout.CENTER);

        return tablePanel;
    }

    private JPanel createInputRow() {
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBackground(new Color(250, 250, 250));
        inputPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(229, 231, 235), 1),
                new EmptyBorder(12, 12, 12, 12)
        ));
        inputPanel.setPreferredSize(new Dimension(0, 70)); // Altura fija para asegurar visibilidad

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 5, 0, 5);
        gbc.gridy = 0;

        // Campo Insumo con placeholder
        insumoField = new JTextField();
        insumoField.setPreferredSize(new Dimension(200, 38));
        insumoField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        insumoField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_LIGHT, 1, true),
                new EmptyBorder(5, 10, 5, 10)
        ));
        addPlaceholder(insumoField, "Buscar insumo...");

        // Campo Cantidad
        cantidadField = new JTextField();
        cantidadField.setPreferredSize(new Dimension(80, 38));
        cantidadField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cantidadField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_LIGHT, 1, true),
                new EmptyBorder(5, 10, 5, 10)
        ));
        addPlaceholder(cantidadField, "0");

        // ComboBox Unidad
        unidadCombo = new JComboBox<>(new String[]{"Unidad", "Litro", "Kilo", "Gramo"});
        unidadCombo.setPreferredSize(new Dimension(100, 38));
        unidadCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        // Campo Precio
        precioField = new JTextField();
        precioField.setPreferredSize(new Dimension(100, 38));
        precioField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        precioField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_LIGHT, 1, true),
                new EmptyBorder(5, 10, 5, 10)
        ));
        addPlaceholder(precioField, "0.00");

        // Label Total dinámico
        JLabel totalNewLabel = new JLabel("$0.00", SwingConstants.RIGHT);
        totalNewLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        totalNewLabel.setForeground(new Color(107, 114, 128));
        totalNewLabel.setPreferredSize(new Dimension(100, 38));

        // Listener para calcular total en tiempo real
        KeyAdapter calcListener = new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                calculateNewItemTotal(totalNewLabel);
            }
        };
        cantidadField.addKeyListener(calcListener);
        precioField.addKeyListener(calcListener);

        gbc.gridx = 0;
        gbc.weightx = 0.3;
        inputPanel.add(insumoField, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.12;
        inputPanel.add(cantidadField, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.12;
        inputPanel.add(unidadCombo, gbc);

        gbc.gridx = 3;
        gbc.weightx = 0.12;
        inputPanel.add(precioField, gbc);

        gbc.gridx = 4;
        gbc.weightx = 0.12;
        inputPanel.add(totalNewLabel, gbc);

        gbc.gridx = 5;
        gbc.weightx = 0.15;
        JButton addButton = createAddButton();
        inputPanel.add(addButton, gbc);

        return inputPanel;
    }

    private JButton createAddButton() {
        JButton addButton = new JButton("➕ Agregar");
        addButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        addButton.setBackground(new Color(34, 197, 94));
        addButton.setForeground(Color.WHITE);
        addButton.setBorderPainted(false);
        addButton.setFocusPainted(false);
        addButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addButton.setPreferredSize(new Dimension(110, 38));
        addButton.setMinimumSize(new Dimension(110, 38));
        addButton.setMaximumSize(new Dimension(110, 38));

        // Efecto hover mejorado
        addButton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                addButton.setBackground(new Color(22, 163, 74));
            }
            public void mouseExited(MouseEvent e) {
                addButton.setBackground(new Color(34, 197, 94));
            }
            public void mousePressed(MouseEvent e) {
                addButton.setBackground(new Color(21, 128, 61));
            }
            public void mouseReleased(MouseEvent e) {
                addButton.setBackground(new Color(22, 163, 74));
            }
        });

        addButton.addActionListener(e -> addNewItem());

        // Enter key en los campos también agrega el item
        KeyAdapter enterListener = new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    addNewItem();
                }
            }
        };
        insumoField.addKeyListener(enterListener);
        cantidadField.addKeyListener(enterListener);
        precioField.addKeyListener(enterListener);

        return addButton;
    }

    private void addPlaceholder(JTextField textField, String placeholder) {
        textField.setForeground(Color.GRAY);
        textField.setText(placeholder);

        textField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (textField.getText().equals(placeholder)) {
                    textField.setText("");
                    textField.setForeground(TEXT_LIGHT);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (textField.getText().isEmpty()) {
                    textField.setForeground(Color.GRAY);
                    textField.setText(placeholder);
                }
            }
        });
    }

    private void calculateNewItemTotal(JLabel totalLabel) {
        try {
            String cantidadText = cantidadField.getText().trim();
            String precioText = precioField.getText().trim();

            // Verificar que no sean placeholders ni vacíos
            boolean cantidadValida = !cantidadText.isEmpty() &&
                    !cantidadText.equals("0") &&
                    !cantidadText.equals("Buscar insumo...");
            boolean precioValido = !precioText.isEmpty() &&
                    !precioText.equals("0.00") &&
                    !precioText.equals("Buscar insumo...");

            if (cantidadValida && precioValido) {
                double cantidad = Double.parseDouble(cantidadText);
                double precio = Double.parseDouble(precioText);

                if (cantidad > 0 && precio > 0) {
                    double total = cantidad * precio;
                    totalLabel.setText("$" + String.format("%.2f", total));
                    totalLabel.setForeground(PRIMARY);
                } else {
                    totalLabel.setText("$0.00");
                    totalLabel.setForeground(new Color(107, 114, 128));
                }
            } else {
                totalLabel.setText("$0.00");
                totalLabel.setForeground(new Color(107, 114, 128));
            }
        } catch (NumberFormatException e) {
            totalLabel.setText("$0.00");
            totalLabel.setForeground(new Color(107, 114, 128));
        }
    }

    private void addNewItem() {
        String insumo = insumoField.getText().trim();
        String cantidad = cantidadField.getText().trim();
        String unidad = (String) unidadCombo.getSelectedItem();
        String precio = precioField.getText().trim();

        // Validar que no sean los placeholders ni estén vacíos
        boolean insumoValido = !insumo.isEmpty() && !insumo.equals("Buscar insumo...");
        boolean cantidadValida = !cantidad.isEmpty() && !cantidad.equals("0");
        boolean precioValido = !precio.isEmpty() && !precio.equals("0.00");

        // Validación de campos
        if (!insumoValido) {
            JOptionPane.showMessageDialog(this,
                    "Por favor, ingrese el nombre del insumo.",
                    "Campo requerido",
                    JOptionPane.WARNING_MESSAGE);
            insumoField.requestFocus();
            insumoField.selectAll();
            return;
        }

        if (!cantidadValida) {
            JOptionPane.showMessageDialog(this,
                    "Por favor, ingrese la cantidad.",
                    "Campo requerido",
                    JOptionPane.WARNING_MESSAGE);
            cantidadField.requestFocus();
            cantidadField.selectAll();
            return;
        }

        if (!precioValido) {
            JOptionPane.showMessageDialog(this,
                    "Por favor, ingrese el precio unitario.",
                    "Campo requerido",
                    JOptionPane.WARNING_MESSAGE);
            precioField.requestFocus();
            precioField.selectAll();
            return;
        }

        try {
            double cantidadNum = Double.parseDouble(cantidad);
            double precioNum = Double.parseDouble(precio);

            if (cantidadNum <= 0) {
                JOptionPane.showMessageDialog(this,
                        "La cantidad debe ser mayor a 0.",
                        "Valor inválido",
                        JOptionPane.WARNING_MESSAGE);
                cantidadField.requestFocus();
                cantidadField.selectAll();
                return;
            }

            if (precioNum <= 0) {
                JOptionPane.showMessageDialog(this,
                        "El precio debe ser mayor a 0.",
                        "Valor inválido",
                        JOptionPane.WARNING_MESSAGE);
                precioField.requestFocus();
                precioField.selectAll();
                return;
            }

            double total = cantidadNum * precioNum;

            // Agregar fila a la tabla
            tableModel.addRow(new Object[]{
                    insumo,
                    cantidad,
                    unidad,
                    "$" + String.format("%.2f", precioNum),
                    "$" + String.format("%.2f", total),
                    "Eliminar"
            });

            // Limpiar campos y restaurar placeholders
            clearFieldWithPlaceholder(insumoField, "Buscar insumo...");
            clearFieldWithPlaceholder(cantidadField, "0");
            clearFieldWithPlaceholder(precioField, "0.00");
            unidadCombo.setSelectedIndex(0);

            // Actualizar total
            updateTotal();

            // Focus al campo de insumo para seguir agregando
            SwingUtilities.invokeLater(() -> {
                insumoField.requestFocus();
                insumoField.selectAll();
            });

            System.out.println("✓ Producto agregado: " + insumo + " - Total actualizado");

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Por favor, ingrese valores numéricos válidos para cantidad y precio.\n" +
                            "Cantidad ingresada: '" + cantidad + "'\n" +
                            "Precio ingresado: '" + precio + "'",
                    "Error de formato",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearFieldWithPlaceholder(JTextField field, String placeholder) {
        field.setText(placeholder);
        field.setForeground(Color.GRAY);
    }

    private void updateTotal() {
        double total = 0;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            try {
                String totalStr = tableModel.getValueAt(i, 4).toString();
                // Remover el símbolo $ y espacios antes de parsear
                totalStr = totalStr.replace("$", "").replace(",", ".").trim();
                double valor = Double.parseDouble(totalStr);
                total += valor;
                System.out.println("Fila " + i + ": $" + valor + " - Total acumulado: $" + total);
            } catch (NumberFormatException e) {
                System.err.println("Error al parsear fila " + i + ": " + tableModel.getValueAt(i, 4));
            }
        }
        totalLabel.setText("$" + String.format("%.2f", total));
        System.out.println("Total final actualizado: $" + String.format("%.2f", total));
    }

    private JButton createStyledButton(String text, boolean primary) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setPreferredSize(new Dimension(200, 40));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setFocusPainted(false);

        if (primary) {
            button.setBackground(PRIMARY);
            button.setForeground(Color.WHITE);
            button.setBorderPainted(false);

            button.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    button.setBackground(PRIMARY.darker());
                }
                public void mouseExited(MouseEvent e) {
                    button.setBackground(PRIMARY);
                }
            });
        } else {
            button.setBackground(Color.WHITE);
            button.setForeground(PRIMARY);
            button.setBorder(new LineBorder(PRIMARY, 2));

            button.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    button.setBackground(new Color(74, 144, 226, 20));
                }
                public void mouseExited(MouseEvent e) {
                    button.setBackground(Color.WHITE);
                }
            });
        }

        return button;
    }

    // Clases auxiliares para el botón de eliminar en la tabla
    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
            setText("🗑️");
            setFont(new Font("Segoe UI", Font.PLAIN, 16));
            setForeground(new Color(239, 68, 68));
            setBorderPainted(false);
            setFocusPainted(false);
            setBackground(Color.WHITE);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            return this;
        }
    }

    class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private String label;
        private boolean isPushed;
        private int editingRow;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.setText("🗑️");
            button.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            button.setForeground(new Color(239, 68, 68));
            button.setBorderPainted(false);
            button.setFocusPainted(false);
            button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            button.addActionListener(e -> fireEditingStopped());
        }

        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            label = (value == null) ? "" : value.toString();
            button.setText("🗑️");
            isPushed = true;
            editingRow = row;
            return button;
        }

        public Object getCellEditorValue() {
            if (isPushed) {
                int confirm = JOptionPane.showConfirmDialog(
                        null,
                        "¿Está seguro de eliminar este producto?",
                        "Confirmar eliminación",
                        JOptionPane.YES_NO_OPTION
                );

                if (confirm == JOptionPane.YES_OPTION) {
                    tableModel.removeRow(editingRow);
                    updateTotal();
                }
            }
            isPushed = false;
            return label;
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Sistema de Orden de Compra - Heladería & Cafetería");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1400, 850);
            frame.setLocationRelativeTo(null);
            frame.add(new OrdenesPage());
            frame.setVisible(true);
        });
    }
}