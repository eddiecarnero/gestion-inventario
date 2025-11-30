package com.inventario.logic;

import org.junit.jupiter.api.*;
import java.sql.*;
import static org.junit.jupiter.api.Assertions.*;

class DashboardServiceTest {
    private Connection conn;
    private DashboardService service;

    @BeforeEach
    void setUp() throws SQLException {
        conn = DriverManager.getConnection("jdbc:sqlite::memory:");
        service = new DashboardService();

        try (Statement s = conn.createStatement()) {
            // Crear tablas mínimas necesarias
            s.execute("CREATE TABLE producto (IdProducto INTEGER PRIMARY KEY, Stock_Minimo INTEGER, Tipo_de_Producto TEXT)");
            s.execute("CREATE TABLE lotes (IdLote INTEGER, IdProducto INTEGER, CantidadActual REAL)");

            // Tablas opcionales (para que no falle el try-catch)
            s.execute("CREATE TABLE productos_intermedios (id INTEGER)");
            s.execute("CREATE TABLE lotes_intermedios (id INTEGER, CantidadActual REAL)");
            s.execute("CREATE TABLE productos_terminados (id INTEGER)");
            s.execute("CREATE TABLE lotes_terminados (id INTEGER, CantidadActual REAL)");
        }
    }

    @AfterEach
    void tearDown() throws SQLException { conn.close(); }

    @Test
    void debeContarInsumosYStockCorrectamente() throws SQLException {
        // 1. Insertar Datos de Prueba
        // Producto 1: Stock Min 10. Lotes: 5 + 20 = 25 (Normal)
        conn.createStatement().execute("INSERT INTO producto VALUES (1, 10, 'Arroz')");
        conn.createStatement().execute("INSERT INTO lotes VALUES (10, 1, 5.0)");
        conn.createStatement().execute("INSERT INTO lotes VALUES (11, 1, 20.0)");

        // Producto 2: Stock Min 10. Lotes: 2 = 2 (Bajo Stock)
        conn.createStatement().execute("INSERT INTO producto VALUES (2, 10, 'Azúcar')");
        conn.createStatement().execute("INSERT INTO lotes VALUES (12, 2, 2.0)");

        // Producto 3: Stock Min 5. Lotes: 0 (Bajo Stock)
        conn.createStatement().execute("INSERT INTO producto VALUES (3, 5, 'Sal')");

        // 2. Ejecutar Lógica
        DashboardStats stats = service.obtenerEstadisticas(conn);

        // 3. Verificar
        assertEquals(3, stats.totalInsumos, "Debe haber 3 productos");
        assertEquals(27.0, stats.stockInsumos, 0.01, "El stock total debe ser 25+2=27");
        assertEquals(2, stats.alertasStockBajo, "Debe haber 2 productos bajo stock (Azúcar y Sal)");
    }
}