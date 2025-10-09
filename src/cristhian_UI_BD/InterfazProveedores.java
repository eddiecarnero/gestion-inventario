package cristhian_UI_BD;
/*
CRISTHIAN NOTE:
Deben instalar el mysql-connector para poder conectar la base de datos a JAVA
Para ello deben ir a la siguiente ruta en IntelIJ
FILE -> PROJECT STRUCTURE -> LIBRARIES -> + -> JAVA -> "Buscan donde tienen el arhivo descargado y lo agregan"
*/

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class InterfazProveedores extends JFrame {

    // ==========================================
    // COMPONENTES DE LA INTERFAZ
    // ==========================================

    // Campos de texto para ingresar datos
    private JTextField txtNombre;
    private JTextField txtRUC;
    private JTextField txtTipo;
    private JTextField txtTelefono;
    private JTextField txtEmail;
    private JTextField txtDireccion;

    // Botones
    private JButton btnAgregar;
    private JButton btnLimpiar;
    private JButton btnRefrescar;

    // Tabla para mostrar proveedores
    private JTable tablaProveedores;
    private DefaultTableModel modeloTabla;

    // ==========================================
    // CONFIGURACIÓN DE LA BASE DE DATOS
    // ==========================================

    private static final String URL = "jdbc:mysql://localhost:3306/sistema_inventario";
    private static final String USUARIO = "root";
    private static final String PASSWORD = "root"; // EL PASSWORD Y USUARIO DEBEN SER EL MISMO QUE EL DE US BASE DE DATOS LOCAL

    // ==========================================
    // CONSTRUCTOR - Aquí se construye la ventana
    // ==========================================

    public InterfazProveedores() {
        // Configurar la ventana principal
        setTitle("Sistema de Gestión de Proveedores");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Centrar en la pantalla
        setLayout(new BorderLayout(10, 10));

        // Crear los componentes
        crearPanelSuperior();
        crearPanelFormulario();
        crearPanelTabla();

        // Cargar los proveedores al iniciar
        cargarProveedores();
    }

    // ==========================================
    // PANEL SUPERIOR (Título)
    // ==========================================

    private void crearPanelSuperior() {
        JPanel panelTitulo = new JPanel();
        panelTitulo.setBackground(new Color(52, 152, 219)); // Azul bonito
        panelTitulo.setPreferredSize(new Dimension(0, 80));

        JLabel lblTitulo = new JLabel("GESTIÓN DE PROVEEDORES");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 28));
        lblTitulo.setForeground(Color.WHITE);

        panelTitulo.add(lblTitulo);
        add(panelTitulo, BorderLayout.NORTH);
    }

    // ==========================================
    // PANEL IZQUIERDO (Formulario)
    // ==========================================

    private void crearPanelFormulario() {
        JPanel panelIzquierdo = new JPanel();
        panelIzquierdo.setLayout(new BoxLayout(panelIzquierdo, BoxLayout.Y_AXIS));
        panelIzquierdo.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panelIzquierdo.setPreferredSize(new Dimension(350, 0));
        panelIzquierdo.setBackground(new Color(236, 240, 241)); // Gris claro

        // Título del formulario
        JLabel lblFormulario = new JLabel("Agregar Nuevo Proveedor");
        lblFormulario.setFont(new Font("Arial", Font.BOLD, 18));
        lblFormulario.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelIzquierdo.add(lblFormulario);
        panelIzquierdo.add(Box.createRigidArea(new Dimension(0, 20)));

        // Crear campos de texto con sus etiquetas
        txtNombre = crearCampoConEtiqueta(panelIzquierdo, "Nombre Comercial:");
        txtRUC = crearCampoConEtiqueta(panelIzquierdo, "RUC (11 dígitos):");
        txtTipo = crearCampoConEtiqueta(panelIzquierdo, "Tipo de Proveedor:");
        txtTelefono = crearCampoConEtiqueta(panelIzquierdo, "Teléfono:");
        txtEmail = crearCampoConEtiqueta(panelIzquierdo, "Email:");
        txtDireccion = crearCampoConEtiqueta(panelIzquierdo, "Dirección:");

        panelIzquierdo.add(Box.createRigidArea(new Dimension(0, 20)));

        // Panel de botones
        JPanel panelBotones = new JPanel();
        panelBotones.setLayout(new FlowLayout());
        panelBotones.setBackground(new Color(236, 240, 241));

        // Botón Agregar
        btnAgregar = new JButton("Agregar");
        btnAgregar.setFont(new Font("Arial", Font.BOLD, 14));
        btnAgregar.setBackground(new Color(46, 204, 113)); // Verde
        btnAgregar.setForeground(Color.WHITE);
        btnAgregar.setFocusPainted(false);
        btnAgregar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAgregar.addActionListener(e -> agregarProveedor());

        // Botón Limpiar
        btnLimpiar = new JButton("Limpiar");
        btnLimpiar.setFont(new Font("Arial", Font.BOLD, 14));
        btnLimpiar.setBackground(new Color(241, 196, 15)); // Amarillo
        btnLimpiar.setForeground(Color.WHITE);
        btnLimpiar.setFocusPainted(false);
        btnLimpiar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLimpiar.addActionListener(e -> limpiarCampos());

        panelBotones.add(btnAgregar);
        panelBotones.add(btnLimpiar);

        panelIzquierdo.add(panelBotones);

        add(panelIzquierdo, BorderLayout.WEST);
    }

    // ==========================================
    // MÉTODO AUXILIAR para crear campos
    // ==========================================

    private JTextField crearCampoConEtiqueta(JPanel panel, String textoEtiqueta) {
        JLabel etiqueta = new JLabel(textoEtiqueta);
        etiqueta.setFont(new Font("Arial", Font.PLAIN, 14));
        etiqueta.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField campo = new JTextField();
        campo.setFont(new Font("Arial", Font.PLAIN, 14));
        campo.setMaximumSize(new Dimension(300, 30));
        campo.setAlignmentX(Component.LEFT_ALIGNMENT);
        campo.setForeground(Color.BLACK);        // Texto en negro
        campo.setBackground(Color.WHITE);        // Fondo blanco
        campo.setCaretColor(Color.BLACK);        // Cursor en negro
        campo.setOpaque(true);                   // Hacer el fondo visible

        panel.add(etiqueta);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(campo);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        return campo;
    }

    // ==========================================
    // PANEL DERECHO (Tabla)
    // ==========================================

    private void crearPanelTabla() {
        JPanel panelDerecho = new JPanel(new BorderLayout(10, 10));
        panelDerecho.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 20));

        // Título de la tabla
        JLabel lblTabla = new JLabel("Lista de Proveedores");
        lblTabla.setFont(new Font("Arial", Font.BOLD, 18));

        // Botón refrescar
        btnRefrescar = new JButton("🔄 Refrescar");
        btnRefrescar.setFont(new Font("Arial", Font.BOLD, 12));
        btnRefrescar.setBackground(new Color(52, 152, 219));
        btnRefrescar.setForeground(Color.WHITE);
        btnRefrescar.setFocusPainted(false);
        btnRefrescar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRefrescar.addActionListener(e -> cargarProveedores());

        JPanel panelTituloTabla = new JPanel(new BorderLayout());
        panelTituloTabla.add(lblTabla, BorderLayout.WEST);
        panelTituloTabla.add(btnRefrescar, BorderLayout.EAST);

        // Crear la tabla
        String[] columnas = {"ID", "Nombre", "RUC", "Tipo", "Teléfono", "Email", "Dirección"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Hacer que las celdas no sean editables
            }
        };

        tablaProveedores = new JTable(modeloTabla);
        tablaProveedores.setFont(new Font("Arial", Font.PLAIN, 12));
        tablaProveedores.setRowHeight(25);
        tablaProveedores.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        tablaProveedores.getTableHeader().setBackground(new Color(52, 73, 94));
        tablaProveedores.getTableHeader().setForeground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(tablaProveedores);

        panelDerecho.add(panelTituloTabla, BorderLayout.NORTH);
        panelDerecho.add(scrollPane, BorderLayout.CENTER);

        add(panelDerecho, BorderLayout.CENTER);
    }

    // ==========================================
    // MÉTODO: Conectar a la base de datos
    // ==========================================

    private Connection conectar() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USUARIO, PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver de MySQL no encontrado: " + e.getMessage());
        }
    }

    // ==========================================
    // MÉTODO: Agregar proveedor
    // ==========================================

    private void agregarProveedor() {
        // Validar que los campos obligatorios no estén vacíos
        if (txtNombre.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "El nombre comercial es obligatorio",
                    "Error de Validación",
                    JOptionPane.ERROR_MESSAGE);
            txtNombre.requestFocus();
            return;
        }

        if (txtRUC.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "El RUC es obligatorio",
                    "Error de Validación",
                    JOptionPane.ERROR_MESSAGE);
            txtRUC.requestFocus();
            return;
        }

        // Validar que el RUC tenga 11 dígitos
        if (txtRUC.getText().trim().length() != 11) {
            JOptionPane.showMessageDialog(this,
                    "El RUC debe tener exactamente 11 dígitos",
                    "Error de Validación",
                    JOptionPane.ERROR_MESSAGE);
            txtRUC.requestFocus();
            return;
        }

        // Insertar en la base de datos
        String sql = "INSERT INTO PROVEEDORES (Nombre_comercial, RUC, Tipo_de_proveedor, " +
                "Telefono, Email, Direccion) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, txtNombre.getText().trim());
            pstmt.setString(2, txtRUC.getText().trim());
            pstmt.setString(3, txtTipo.getText().trim());
            pstmt.setString(4, txtTelefono.getText().trim());
            pstmt.setString(5, txtEmail.getText().trim());
            pstmt.setString(6, txtDireccion.getText().trim());

            int filasAfectadas = pstmt.executeUpdate();

            if (filasAfectadas > 0) {
                JOptionPane.showMessageDialog(this,
                        "✓ Proveedor agregado exitosamente",
                        "Éxito",
                        JOptionPane.INFORMATION_MESSAGE);

                limpiarCampos();
                cargarProveedores(); // Actualizar la tabla
            }

        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate entry")) {
                JOptionPane.showMessageDialog(this,
                        "El RUC '" + txtRUC.getText() + "' ya existe en la base de datos",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Error al agregar proveedor:\n" + e.getMessage(),
                        "Error de Base de Datos",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ==========================================
    // MÉTODO: Cargar proveedores en la tabla
    // ==========================================

    private void cargarProveedores() {
        // Limpiar la tabla
        modeloTabla.setRowCount(0);

        String sql = "SELECT * FROM PROVEEDORES ORDER BY IdProveedor DESC";

        try (Connection conn = conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Object[] fila = {
                        rs.getInt("IdProveedor"),
                        rs.getString("Nombre_comercial"),
                        rs.getString("RUC"),
                        rs.getString("Tipo_de_proveedor"),
                        rs.getString("Telefono"),
                        rs.getString("Email"),
                        rs.getString("Direccion")
                };
                modeloTabla.addRow(fila);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Error al cargar proveedores:\n" + e.getMessage(),
                    "Error de Base de Datos",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // ==========================================
    // MÉTODO: Limpiar campos
    // ==========================================

    private void limpiarCampos() {
        txtNombre.setText("");
        txtRUC.setText("");
        txtTipo.setText("");
        txtTelefono.setText("");
        txtEmail.setText("");
        txtDireccion.setText("");
        txtNombre.requestFocus(); // Poner el cursor en el primer campo
    }

    // ==========================================
    // MÉTODO MAIN - Ejecutar el programa
    // ==========================================

    public static void main(String[] args) {
        // Configurar colores personalizados para los campos de texto
        UIManager.put("TextField.foreground", Color.BLACK);
        UIManager.put("TextField.background", Color.WHITE);
        UIManager.put("TextField.caretForeground", Color.BLACK);

        // Crear y mostrar la ventana
        SwingUtilities.invokeLater(() -> {
            InterfazProveedores ventana = new InterfazProveedores();
            ventana.setVisible(true);
        });
    }
}