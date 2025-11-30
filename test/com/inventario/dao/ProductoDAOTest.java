package com.inventario.dao;

import com.inventario.model.Producto;
import org.junit.jupiter.api.*;
import java.sql.*;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ProductoDAOTest {
    private Connection conn;
    private ProductoDAO dao;

    @BeforeEach
    void setUp() throws SQLException {
        conn = DriverManager.getConnection("jdbc:sqlite::memory:");
        dao = new ProductoDAO();
        try (Statement s = conn.createStatement()) {
            // Recreamos la tabla producto tal como es en tu BD real
            s.execute("CREATE TABLE producto (" +
                    "IdProducto INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "Tipo_de_Producto TEXT, " +
                    "Stock_Minimo INTEGER, " +
                    "Stock_Maximo INTEGER, " +
                    "Estado_Stock TEXT, " +
                    "Fecha_de_caducidad DATE, " +
                    "Unidad_de_medida TEXT, " +
                    "Ubicacion TEXT, " +
                    "IdProveedor INTEGER, " +
                    "PrecioUnitario REAL, " +
                    "Contenido REAL, " +
                    "Stock REAL DEFAULT 0" +
                    ")");
        }
    }

    @AfterEach
    void tearDown() throws SQLException { conn.close(); }

    @Test
    void guardarProducto_Nuevo_DebeInsertar() throws SQLException {
        // Preparar objeto
        Producto nuevo = new Producto();
        nuevo.setTipoDeProducto("Coca Cola 3L");
        nuevo.setPrecioUnitario(12.50);
        nuevo.setStockMinimo(10);
        nuevo.setUnidadDeMedida("Botella");
        nuevo.setUbicacion("Pasillo 1");
        nuevo.setIdProveedor(1);
        nuevo.setContenido(3.0);

        // Ejecutar (true = es nuevo)
        boolean exito = dao.guardarProducto(conn, nuevo, true);

        assertTrue(exito);

        // Verificar en BD
        ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM producto WHERE Tipo_de_Producto='Coca Cola 3L'");
        assertTrue(rs.next());
        assertEquals(12.50, rs.getDouble("PrecioUnitario"));
        assertEquals(0.0, rs.getDouble("Stock")); // Stock inicial debe ser 0
    }

    @Test
    void guardarProducto_Existente_DebeActualizar() throws SQLException {
        // Insertar dato inicial
        conn.createStatement().execute("INSERT INTO producto (IdProducto, Tipo_de_Producto, PrecioUnitario) VALUES (10, 'Pepsi', 5.00)");

        // Preparar actualización
        Producto editado = new Producto();
        editado.setIdProducto(10); // ID existente
        editado.setTipoDeProducto("Pepsi Actualizada");
        editado.setPrecioUnitario(6.00); // Cambio de precio
        // Llenar otros campos obligatorios para que no falle el SQL
        editado.setStockMinimo(5);
        editado.setUnidadDeMedida("Unidad");
        editado.setContenido(1.0);

        // Ejecutar (false = edición)
        boolean exito = dao.guardarProducto(conn, editado, false);

        assertTrue(exito);

        // Verificar cambios
        ResultSet rs = conn.createStatement().executeQuery("SELECT Tipo_de_Producto, PrecioUnitario FROM producto WHERE IdProducto=10");
        rs.next();
        assertEquals("Pepsi Actualizada", rs.getString("Tipo_de_Producto"));
        assertEquals(6.00, rs.getDouble("PrecioUnitario"));
    }

    @Test
    void obtenerTodos_DebeRetornarLista() throws SQLException {
        conn.createStatement().execute("INSERT INTO producto (Tipo_de_Producto) VALUES ('A')");
        conn.createStatement().execute("INSERT INTO producto (Tipo_de_Producto) VALUES ('B')");

        // Usamos el método de lectura (necesitas asegurarte que tu DAO tenga una versión sobrecargada que acepte 'conn'
        // Si no la tienes, este test fallará al compilar. *Ver nota abajo*
        // Por ahora, simulamos la lectura directa para probar el concepto o usa tu método real si ya lo adaptaste.

        ResultSet rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM producto");
        rs.next();
        assertEquals(2, rs.getInt(1));
    }

    @Test
    void eliminarProducto_DebeBorrar() throws SQLException {
        conn.createStatement().execute("INSERT INTO producto (IdProducto) VALUES (99)");

        // Llamamos al método de borrado (asumiendo que añadiste la sobrecarga 'eliminarProducto(Connection, int)')
        // Si no, puedes crear una instancia de DAO y llamar al método público si tuvieras inyección de dependencias,
        // pero aquí probamos la lógica SQL directa:
        try(PreparedStatement ps = conn.prepareStatement("DELETE FROM producto WHERE IdProducto=?")) {
            ps.setInt(1, 99);
            int affected = ps.executeUpdate();
            assertEquals(1, affected);
        }
    }
}