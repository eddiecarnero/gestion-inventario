package com.inventario.dao;

import com.inventario.model.LoteTerminado;
import com.inventario.model.ProductoTerminado;
import com.inventario.model.RecetaSimple;
import org.junit.jupiter.api.*;
import java.sql.*;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class Almacen3DAOTest {

    private Connection conn;
    private Almacen3DAO dao;

    @BeforeEach
    void setUp() throws SQLException {
        // 1. Usamos BD en memoria
        conn = DriverManager.getConnection("jdbc:sqlite::memory:");
        dao = new Almacen3DAO();

        // 2. Creamos las tablas simuladas (Esquema de Almacén 3)
        try (Statement stmt = conn.createStatement()) {

            // Tabla de Productos Terminados
            stmt.execute("CREATE TABLE productos_terminados (" +
                    "IdProductoTerminado INTEGER PRIMARY KEY, " +
                    "Nombre TEXT, " +
                    "PrecioVenta REAL, " +
                    "StockMinimo INTEGER" +
                    ")");

            // Tabla de Lotes Terminados
            stmt.execute("CREATE TABLE lotes_terminados (" +
                    "IdLoteTerminado INTEGER PRIMARY KEY, " +
                    "IdProductoTerminado INTEGER, " +
                    "CantidadActual REAL, " + // Ojo: en tu modelo a veces es int o double, SQLite es flexible
                    "FechaVencimiento TEXT" +
                    ")");

            // Tabla de Recetas (Para el filtro de 'FINAL')
            stmt.execute("CREATE TABLE recetas (" +
                    "id INTEGER PRIMARY KEY, " +
                    "nombre TEXT, " +
                    "cantidad_producida REAL, " +
                    "unidad_producida TEXT, " +
                    "tipo_destino TEXT" +
                    ")");

            // --- 3. INSERTAR DATOS DE PRUEBA (FIXTURES) ---

            // Producto 1: Pastel de Chocolate (ID 100)
            stmt.execute("INSERT INTO productos_terminados VALUES (100, 'Pastel Chocolate', 50.0, 5)");

            // Lotes del Producto 1 (Tiene 2 lotes)
            // Lote A: 10 unidades
            stmt.execute("INSERT INTO lotes_terminados VALUES (1, 100, 10, '2030-01-01')");
            // Lote B: 5 unidades
            stmt.execute("INSERT INTO lotes_terminados VALUES (2, 100, 5, '2030-01-01')");

            // Producto 2: Helado (ID 200) - SIN LOTES (Stock debería ser 0)
            stmt.execute("INSERT INTO productos_terminados VALUES (200, 'Helado', 20.0, 10)");

            // Recetas
            // Receta 1: Es FINAL (Debe aparecer)
            stmt.execute("INSERT INTO recetas VALUES (1, 'Receta Pastel', 1.0, 'Unidad', 'FINAL')");
            // Receta 2: Es INTERMEDIO (NO debe aparecer)
            stmt.execute("INSERT INTO recetas VALUES (2, 'Receta Masa', 10.0, 'Kg', 'INTERMEDIO')");
        }
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (conn != null) conn.close();
    }

    @Test
    void getProductosTerminados_DebeCalcularStockTotal() throws SQLException {
        // Ejecutamos el método del DAO pasando la conexión de prueba
        List<ProductoTerminado> lista = dao.getProductosTerminados(conn);

        // Verificaciones
        assertFalse(lista.isEmpty(), "La lista no debería estar vacía");
        assertEquals(2, lista.size(), "Debería haber 2 productos (Pastel y Helado)");

        // 1. Verificar Pastel (Tiene lotes)
        ProductoTerminado pastel = lista.stream().filter(p -> p.getId() == 100).findFirst().orElse(null);
        assertNotNull(pastel);
        assertEquals("Pastel Chocolate", pastel.getNombre());
        // SUMA CLAVE: 10 (Lote A) + 5 (Lote B) = 15
        assertEquals(15, pastel.getStock(), "El stock total del pastel debería ser la suma de sus lotes (15)");

        // 2. Verificar Helado (Sin lotes)
        ProductoTerminado helado = lista.stream().filter(p -> p.getId() == 200).findFirst().orElse(null);
        assertNotNull(helado);
        assertEquals(0, helado.getStock(), "El producto sin lotes debería tener stock 0 (gracias al COALESCE en SQL)");
    }

    @Test
    void getLotesPorProducto_DebeTraerSoloDeEseID() throws SQLException {
        // Pedimos lotes del Pastel (ID 100)
        List<LoteTerminado> lotes = dao.getLotesPorProducto(conn, 100);

        assertEquals(2, lotes.size(), "El pastel debería tener 2 lotes");

        // Verificar contenido
        assertEquals(10, lotes.get(0).getCantidad());
        assertEquals(5, lotes.get(1).getCantidad());
    }

    @Test
    void getRecetasFinales_DebeFiltrarSoloFinales() throws SQLException {
        // En la BD insertamos una 'FINAL' y una 'INTERMEDIO'
        List<RecetaSimple> recetas = dao.getRecetasFinales(conn);

        assertEquals(1, recetas.size(), "Solo debería traer 1 receta (la de tipo FINAL)");
        assertEquals("Receta Pastel", recetas.get(0).getNombre());
    }
}