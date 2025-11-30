package com.inventario.dao;

import com.inventario.ui.admin.ProveedorModel;
import org.junit.jupiter.api.*;
import java.sql.*;
import static org.junit.jupiter.api.Assertions.*;

class ProveedorDAOTest {
    private Connection conn;
    private ProveedorDAO dao;

    @BeforeEach
    void setUp() throws SQLException {
        conn = DriverManager.getConnection("jdbc:sqlite::memory:");
        dao = new ProveedorDAO();
        try (Statement s = conn.createStatement()) {
            s.execute("CREATE TABLE proveedores (IdProveedor INTEGER PRIMARY KEY, Nombre_comercial TEXT, RUC TEXT, Tipo_de_proveedor TEXT, Telefono TEXT, Email TEXT, Direccion TEXT)");
        }
    }

    @AfterEach
    void tearDown() throws SQLException { conn.close(); }

    @Test
    void guardarProveedorDebeInsertar() throws SQLException {
        ProveedorModel p = new ProveedorModel(0, "Pepsi", "20555", "Bebidas", "123", "a@a.com", "Calle 1");

        boolean res = dao.guardarProveedor(conn, p, true);
        assertTrue(res);

        ResultSet rs = conn.createStatement().executeQuery("SELECT Nombre_comercial FROM proveedores WHERE RUC='20555'");
        rs.next();
        assertEquals("Pepsi", rs.getString(1));
    }

    @Test
    void actualizarProveedorDebeCambiarDatos() throws SQLException {
        // Insertar inicial
        conn.createStatement().execute("INSERT INTO proveedores (IdProveedor, Nombre_comercial) VALUES (10, 'Viejo Nombre')");

        // Actualizar
        ProveedorModel p = new ProveedorModel(10, "Nuevo Nombre", "RUC", "T", "T", "E", "D");
        dao.guardarProveedor(conn, p, false); // false = editar

        ResultSet rs = conn.createStatement().executeQuery("SELECT Nombre_comercial FROM proveedores WHERE IdProveedor=10");
        rs.next();
        assertEquals("Nuevo Nombre", rs.getString(1));
    }
}