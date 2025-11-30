package com.inventario.dao;

import com.inventario.ui.admin.UsuarioModel;
import org.junit.jupiter.api.*;
import java.sql.*;
import static org.junit.jupiter.api.Assertions.*;

class UsuarioDAOTest {
    private Connection conn;
    private UsuarioDAO dao;

    @BeforeEach
    void setUp() throws SQLException {
        conn = DriverManager.getConnection("jdbc:sqlite::memory:");
        dao = new UsuarioDAO();
        try (Statement s = conn.createStatement()) {
            s.execute("CREATE TABLE empleado (IdEmpleado INTEGER PRIMARY KEY, Tipo_de_empleado TEXT, Nombre_y_Apellido TEXT, Telefono TEXT, DNI TEXT, Turnos TEXT, Horario TEXT, user TEXT, password TEXT)");
        }
    }

    @AfterEach
    void tearDown() throws SQLException { conn.close(); }

    @Test
    void guardarUsuarioDebeInsertar() throws SQLException {
        UsuarioModel nuevo = new UsuarioModel(0, "Maria", "Cocinero", "123", "888", "Tarde", "2-10", "maria", "pass");

        boolean resultado = dao.guardarUsuario(conn, nuevo, true); // true = nuevo

        assertTrue(resultado);
        // Verificar inserción
        ResultSet rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM empleado WHERE user='maria'");
        rs.next();
        assertEquals(1, rs.getInt(1));
    }

    @Test
    void eliminarUsuarioDebeBorrar() throws SQLException {
        // Insertar dummy
        conn.createStatement().execute("INSERT INTO empleado (IdEmpleado, user) VALUES (10, 'borrame')");

        boolean resultado = dao.eliminarUsuario(conn, 10);

        assertTrue(resultado);
        ResultSet rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM empleado WHERE IdEmpleado=10");
        rs.next();
        assertEquals(0, rs.getInt(1));
    }
}