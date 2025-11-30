package com.inventario.dao;

import org.junit.jupiter.api.*;
import java.sql.*;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class VentaDAOTest {

    private Connection connection;
    private VentaDAO dao;

    @BeforeEach
    void setUp() throws SQLException {
        // 1. Crear BD en memoria
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        dao = new VentaDAO();

        // 2. Crear tablas necesarias para que la venta funcione
        try (Statement stmt = connection.createStatement()) {
            // Tabla productos
            stmt.execute("CREATE TABLE productos_terminados (IdProductoTerminado INTEGER PRIMARY KEY, Nombre TEXT, PrecioVenta REAL);");

            // Tabla lotes (Aquí está la magia del stock)
            stmt.execute("CREATE TABLE lotes_terminados (" +
                    "IdLoteTerminado INTEGER PRIMARY KEY, " +
                    "IdProductoTerminado INTEGER, " +
                    "CantidadActual INTEGER, " +
                    "FechaVencimiento TEXT);");

            // Tabla ventas
            stmt.execute("CREATE TABLE ventas (" +
                    "IdVenta INTEGER PRIMARY KEY, " +
                    "IdProductoTerminado INTEGER, " +
                    "FechaVenta TEXT, " +
                    "Cantidad INTEGER, " +
                    "PrecioUnitario REAL, " +
                    "Total REAL, " +
                    "Cliente TEXT);");

            // 3. Insertar DATOS DE PRUEBA (Fixtures)
            // Producto ID 1: "Helado Vainilla" a 10.00
            stmt.execute("INSERT INTO productos_terminados VALUES (1, 'Helado Vainilla', 10.0)");

            // Lote A (ID 10): 5 unidades, Vence AYER (Debe salir primero)
            stmt.execute("INSERT INTO lotes_terminados VALUES (10, 1, 5, '2023-01-01')");

            // Lote B (ID 20): 10 unidades, Vence MAÑANA (Debe salir segundo)
            stmt.execute("INSERT INTO lotes_terminados VALUES (20, 1, 10, '2099-12-31')");
        }
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (connection != null) connection.close();
    }

    @Test
    void ventaExitosaDebeRegistrarseYDescontarStock() throws SQLException {
        // Intentamos vender 3 helados
        boolean resultado = dao.registrarVenta(connection, 1, 3, 10.0, "Cliente X", LocalDate.now());

        assertTrue(resultado, "La venta debería ser exitosa");

        // Verificar que se guardó en la tabla ventas
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM ventas")) {
            rs.next();
            assertEquals(1, rs.getInt(1), "Debe haber 1 registro en ventas");
        }
    }

    @Test
    void ventaSinStockDebeFallar() {
        // Hay 15 en total (5 del lote A + 10 del lote B). Pedimos 20.
        boolean resultado = dao.registrarVenta(connection, 1, 20, 10.0, "Cliente Y", LocalDate.now());

        assertFalse(resultado, "La venta no debe proceder si falta stock");
    }

    @Test
    void logicaFEFODebeFuncionarCorrectamente() throws SQLException {
        // Escenario CRÍTICO:
        // Tenemos Lote A (5 unids, viejo) y Lote B (10 unids, nuevo).
        // Vendemos 8 unidades.
        // Debería vaciar el Lote A (5) y tomar 3 del Lote B.
        // Quedarían 7 en el Lote B.

        dao.registrarVenta(connection, 1, 8, 10.0, "Cliente FEFO", LocalDate.now());

        // Verificamos Lote A (Debería haber desaparecido o estar en 0)
        try (Statement stmt = connection.createStatement()) {
            ResultSet rsA = stmt.executeQuery("SELECT Count(*) FROM lotes_terminados WHERE IdLoteTerminado = 10");
            rsA.next();
            assertEquals(0, rsA.getInt(1), "El Lote antiguo debió ser eliminado (delete) porque se agotó");

            // Verificamos Lote B (10 - 3 = 7 restantes)
            ResultSet rsB = stmt.executeQuery("SELECT CantidadActual FROM lotes_terminados WHERE IdLoteTerminado = 20");
            rsB.next();
            assertEquals(7, rsB.getInt(1), "El Lote nuevo debería tener 7 unidades restantes");
        }
    }
}