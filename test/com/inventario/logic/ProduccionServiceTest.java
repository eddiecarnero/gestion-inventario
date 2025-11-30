package com.inventario.logic;

import com.inventario.model.RecetaSimple;
import org.junit.jupiter.api.*;
import java.sql.*;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class ProduccionServiceTest {

    private Connection conn;
    private ProduccionService service;

    @BeforeEach
    void setUp() throws SQLException {
        conn = DriverManager.getConnection("jdbc:sqlite::memory:");
        service = new ProduccionService();

        try (Statement stmt = conn.createStatement()) {
            // --- CREAR TABLAS DE LOS 3 ALMACENES ---

            // Almacén 1 (Insumos)
            stmt.execute("CREATE TABLE producto (IdProducto INTEGER PRIMARY KEY, Unidad_de_medida TEXT, Stock REAL)");
            stmt.execute("CREATE TABLE lotes (IdLote INTEGER PRIMARY KEY, IdProducto INTEGER, CantidadActual REAL, FechaVencimiento TEXT)");

            // Almacén 2 (Intermedios)
            stmt.execute("CREATE TABLE productos_intermedios (IdProductoIntermedio INTEGER PRIMARY KEY, Unidad_de_medida TEXT)");
            stmt.execute("CREATE TABLE lotes_intermedios (IdLote INTEGER PRIMARY KEY, IdProductoIntermedio INTEGER, CantidadActual REAL, FechaVencimiento TEXT)");

            // Almacén 3 (Terminados)
            stmt.execute("CREATE TABLE productos_terminados (IdProductoTerminado INTEGER PRIMARY KEY, Nombre TEXT, PrecioVenta REAL)");
            stmt.execute("CREATE TABLE lotes_terminados (IdLoteTerminado INTEGER PRIMARY KEY, IdProductoTerminado INTEGER, CantidadActual INTEGER, FechaProduccion TEXT, FechaVencimiento TEXT)");

            // Recetas
            stmt.execute("CREATE TABLE ingredientes (receta_id INTEGER, IdProducto INTEGER, IdIntermedio INTEGER, cantidad REAL, unidad TEXT, tipo_origen TEXT)");

            // --- DATOS DE PRUEBA ---

            // 1. Insumo: Harina (ID 10) - Tenemos 10 KG
            stmt.execute("INSERT INTO producto VALUES (10, 'kg', 10.0)");
            stmt.execute("INSERT INTO lotes VALUES (101, 10, 10.0, '2030-01-01')");

            // 2. Intermedio: Cobertura (ID 20) - Tenemos 5 Litros
            stmt.execute("INSERT INTO productos_intermedios VALUES (20, 'Litro')");
            stmt.execute("INSERT INTO lotes_intermedios VALUES (201, 20, 5.0, '2030-01-01')");

            // 3. Receta: "Pastel" (ID 1)
            // Requiere: 1 kg de Harina + 0.5 Litros de Cobertura
            stmt.execute("INSERT INTO ingredientes VALUES (1, 10, NULL, 1.0, 'kg', 'INSUMO')");
            stmt.execute("INSERT INTO ingredientes VALUES (1, NULL, 20, 0.5, 'Litro', 'INTERMEDIO')");
        }
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (conn != null) conn.close();
    }

    @Test
    void produccionDebeConsumirInsumosYCrearProducto() throws SQLException {
        // PREPARACIÓN
        // Receta ID 1, Nombre "Pastel", Cantidad Base 1, Unidad "Unidad", Tipo "FINAL"
        RecetaModelDummy receta = new RecetaModelDummy(1, "Pastel", 1.0, "Unidad", "FINAL");

        // EJECUCIÓN: Producir 2 Pasteles
        // Necesitará: 2 * 1kg Harina = 2kg
        // Necesitará: 2 * 0.5L Cobertura = 1L
        service.procesarProduccionFinal(conn, receta, 2.0, 50.0, LocalDate.now().plusDays(30));

        // VERIFICACIÓN
        try (Statement stmt = conn.createStatement()) {
            // 1. Verificar Harina (Tenía 10, usó 2 -> Quedan 8)
            ResultSet rs1 = stmt.executeQuery("SELECT CantidadActual FROM lotes WHERE IdLote=101");
            rs1.next();
            assertEquals(8.0, rs1.getDouble(1), 0.01, "La harina no se descontó correctamente");

            // 2. Verificar Cobertura (Tenía 5, usó 1 -> Quedan 4)
            ResultSet rs2 = stmt.executeQuery("SELECT CantidadActual FROM lotes_intermedios WHERE IdLote=201");
            rs2.next();
            assertEquals(4.0, rs2.getDouble(1), 0.01, "La cobertura no se descontó correctamente");

            // 3. Verificar Producto Terminado (Debe haber 2 unidades)
            ResultSet rs3 = stmt.executeQuery("SELECT CantidadActual FROM lotes_terminados");
            assertTrue(rs3.next(), "No se creó el lote terminado");
            assertEquals(2, rs3.getInt(1), "Debieron crearse 2 pasteles");
        }
    }

    // Clase Dummy para simular RecetaSimple dentro del test sin dependencias externas complejas
    // (O puedes usar tu clase RecetaSimple real si ya la tienes accesible)
    static class RecetaModelDummy extends RecetaSimple {
        public RecetaModelDummy(int id, String nombre, double cantidad, String unidad, String tipo) {
            super(id, nombre, cantidad, unidad); // Ajusta según tu constructor real
        }
    }
}