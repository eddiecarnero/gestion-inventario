package com.inventario.logic;

import org.junit.jupiter.api.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class GestorVencimientosTest {

    private Connection conn;

    @BeforeEach
    void setUp() throws SQLException {
        conn = DriverManager.getConnection("jdbc:sqlite::memory:");
        try (Statement stmt = conn.createStatement()) {
            // Crear tablas necesarias
            stmt.execute("CREATE TABLE producto (IdProducto INTEGER PRIMARY KEY, Tipo_de_Producto TEXT, Stock REAL)");
            stmt.execute("CREATE TABLE lotes (IdLote INTEGER PRIMARY KEY, IdProducto INTEGER, CantidadActual REAL, FechaVencimiento TEXT)");
            stmt.execute("CREATE TABLE kardex (IdKardex INTEGER PRIMARY KEY, IdProducto INTEGER, Fecha TEXT, Motivo TEXT, TipoMovimiento TEXT, IdEmpleado INTEGER, Cantidad REAL)");

            // DATOS DE PRUEBA
            // Producto: Leche (ID 100), Stock total inicial = 20
            stmt.execute("INSERT INTO producto VALUES (100, 'Leche', 20.0)");

            // Lote 1 (VENCIDO): 5 unidades, venció ayer
            String ayer = LocalDate.now().minusDays(1).toString();
            stmt.execute("INSERT INTO lotes VALUES (1, 100, 5.0, '" + ayer + "')");

            // Lote 2 (FRESCO): 15 unidades, vence mañana
            String manana = LocalDate.now().plusDays(1).toString();
            stmt.execute("INSERT INTO lotes VALUES (2, 100, 15.0, '" + manana + "')");
        }
    }

    @AfterEach
    void tearDown() throws SQLException {
        conn.close();
    }

    @Test
    void debeEliminarSoloLotesVencidos() throws SQLException {
        // Ejecutar limpieza
        List<String> reporte = GestorVencimientos.verificarYLimpiarVencidos(conn);

        // 1. Verificar que reportó la eliminación
        assertEquals(1, reporte.size());
        assertTrue(reporte.get(0).contains("5.0 de Leche"));

        // 2. Verificar que el lote vencido (ID 1) ya NO existe
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM lotes WHERE IdLote = 1");
            rs.next();
            assertEquals(0, rs.getInt(1), "El lote vencido debió ser borrado");
        }

        // 3. Verificar que el lote fresco (ID 2) AÚN existe
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM lotes WHERE IdLote = 2");
            rs.next();
            assertEquals(1, rs.getInt(1), "El lote fresco NO debió ser borrado");
        }

        // 4. Verificar que el Stock Total bajó de 20 a 15
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT Stock FROM producto WHERE IdProducto = 100");
            rs.next();
            assertEquals(15.0, rs.getDouble(1), 0.01, "El stock total debió actualizarse");
        }
    }
}