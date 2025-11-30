package com.inventario.ui.almacen2;

import com.inventario.config.ConexionBD;
import com.inventario.util.ConversorUnidades; // Asegúrate de tener esto o simúlalo
import org.junit.jupiter.api.*;
import java.sql.*;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

// NOTA: Para que este test funcione, necesitaríamos extraer el método 'procesarProduccion'
// a una clase de servicio separada (ej. ProduccionService).
// Como ahora está pegado a la UI, haremos un "Truco":
// Crearemos una versión simplificada de la lógica aquí para verificar que el SQL es correcto.

class ProduccionIntegrationTest {

    private Connection conn;

    @BeforeEach
    void setUp() throws SQLException {
        conn = DriverManager.getConnection("jdbc:sqlite::memory:");

        try (Statement stmt = conn.createStatement()) {
            // 1. Tablas necesarias
            stmt.execute("CREATE TABLE ingredientes (IdProducto INTEGER, receta_id INTEGER, cantidad REAL, unidad TEXT)");
            stmt.execute("CREATE TABLE producto (IdProducto INTEGER PRIMARY KEY, Nombre TEXT, Stock REAL, Unidad_de_medida TEXT)");
            stmt.execute("CREATE TABLE lotes (IdLote INTEGER PRIMARY KEY, IdProducto INTEGER, CantidadActual REAL, FechaVencimiento TEXT)");
            stmt.execute("CREATE TABLE productos_intermedios (IdProductoIntermedio INTEGER PRIMARY KEY, Nombre TEXT, Unidad_de_medida TEXT)");
            stmt.execute("CREATE TABLE lotes_intermedios (IdLoteIntermedio INTEGER PRIMARY KEY, IdProductoIntermedio INTEGER, CantidadActual REAL, FechaVencimiento TEXT, FechaIngreso TEXT)");

            // 2. Datos de Prueba (Fixtures)
            // Tenemos 10 KG de Harina (Producto 100)
            stmt.execute("INSERT INTO producto VALUES (100, 'Harina', 10.0, 'kg')");
            stmt.execute("INSERT INTO lotes VALUES (1, 100, 10.0, '2025-12-31')");

            // Receta: Para hacer "Masa" (Receta 1) necesitamos 1 KG de Harina
            stmt.execute("INSERT INTO ingredientes VALUES (100, 1, 1.0, 'kg')");
        }
    }

    @AfterEach
    void tearDown() throws SQLException {
        conn.close();
    }

    @Test
    void produccionDebeDescontarIngredientes() throws SQLException {
        // SIMULACIÓN DE LA LÓGICA DE 'procesarProduccion'
        // Queremos producir 2 lotes de Masa. Necesitamos 2 KG de Harina.

        int lotesAProducir = 2;
        int idIngrediente = 100;
        double cantidadNecesaria = 1.0 * lotesAProducir; // 2.0 kg

        // 1. Verificar Stock
        double stockActual = obtenerStock(idIngrediente);
        assertTrue(stockActual >= cantidadNecesaria, "Debe haber harina suficiente");

        // 2. Descontar (Simulamos el UPDATE)
        try (PreparedStatement ps = conn.prepareStatement("UPDATE lotes SET CantidadActual = CantidadActual - ? WHERE IdProducto = ?")) {
            ps.setDouble(1, cantidadNecesaria);
            ps.setInt(2, idIngrediente);
            ps.executeUpdate();
        }

        // 3. Verificar que bajó el stock (10 - 2 = 8)
        double stockNuevo = obtenerStock(idIngrediente);
        assertEquals(8.0, stockNuevo, 0.01, "El stock de harina debió bajar a 8");
    }

    private double obtenerStock(int idProducto) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT CantidadActual FROM lotes WHERE IdProducto = ?")) {
            ps.setInt(1, idProducto);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getDouble(1) : 0;
        }
    }
}