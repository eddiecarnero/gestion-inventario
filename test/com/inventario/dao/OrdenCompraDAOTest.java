package com.inventario.dao;

import com.inventario.ui.insumos.InsumoUI;
import com.inventario.ui.insumos.ItemOrden;
import com.inventario.ui.insumos.ItemRecepcion;
import org.junit.jupiter.api.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class OrdenCompraDAOTest {
    private Connection conn;
    private OrdenCompraDAO dao;

    @BeforeEach
    void setUp() throws SQLException {
        conn = DriverManager.getConnection("jdbc:sqlite::memory:");
        dao = new OrdenCompraDAO();
        try (Statement s = conn.createStatement()) {
            // Tablas necesarias
            s.execute("CREATE TABLE orden_compra (IdCompra INTEGER PRIMARY KEY AUTOINCREMENT, IdProveedor INTEGER, IdEmpleado INTEGER, Fecha_de_Compra TEXT, Precio_total REAL, Estado TEXT)");
            s.execute("CREATE TABLE detalle_compra (IdDetalle INTEGER PRIMARY KEY AUTOINCREMENT, IdCompra INTEGER, IdProducto INTEGER, Cantidad REAL, PrecioUnitario REAL, SubTotal REAL)");

            s.execute("CREATE TABLE producto (IdProducto INTEGER PRIMARY KEY, Stock REAL DEFAULT 0)");
            s.execute("CREATE TABLE lotes (IdLote INTEGER PRIMARY KEY, IdProducto INTEGER, CantidadActual REAL, FechaVencimiento TEXT, FechaIngreso TEXT)");
            s.execute("CREATE TABLE kardex (IdKardex INTEGER PRIMARY KEY, IdProducto INTEGER, Fecha TEXT, Motivo TEXT, TipoMovimiento TEXT, IdEmpleado INTEGER, Cantidad REAL)");

            s.execute("INSERT INTO producto (IdProducto, Stock) VALUES (100, 0)");
        }
    }

    @AfterEach
    void tearDown() throws SQLException { conn.close(); }

    @Test
    void cicloCompleto_GuardarYRecepcionar() throws SQLException {
        // --- 1. GUARDAR ORDEN ---

        // CORRECCIÓN AQUÍ: Constructor de 6 argumentos
        // InsumoUI(id, idProv, nombre, precio, unidad, contenido)
        InsumoUI insumo = new InsumoUI(100, 1, "Caja Leche", 10.0, "Caja", 1.0);

        List<ItemOrden> itemsCompra = new ArrayList<>();
        itemsCompra.add(new ItemOrden(insumo, 5.0));

        // Usamos el método sobrecargado que acepta 'conn'
        boolean guardado = dao.guardarOrden(conn, 1, LocalDate.now(), itemsCompra);
        assertTrue(guardado, "La orden debió guardarse");

        // Verificar estado pendiente
        ResultSet rsOrden = conn.createStatement().executeQuery("SELECT Estado FROM orden_compra WHERE IdCompra=1");
        assertTrue(rsOrden.next());
        assertEquals("Pendiente", rsOrden.getString(1));

        // --- 2. RECEPCIONAR MERCADERÍA ---
        List<ItemRecepcion> itemsRecibidos = new ArrayList<>();
        ItemRecepcion rec = new ItemRecepcion(100, "Caja Leche", 5.0);
        rec.setFechaVencimiento(LocalDate.now().plusDays(30));
        itemsRecibidos.add(rec);

        // Usamos el método sobrecargado que acepta 'conn'
        boolean recepcionado = dao.procesarRecepcion(conn, 1, itemsRecibidos);
        assertTrue(recepcionado, "La recepción debió ser exitosa");

        // --- 3. VERIFICACIONES ---
        // Stock actualizado
        ResultSet rsStock = conn.createStatement().executeQuery("SELECT Stock FROM producto WHERE IdProducto=100");
        assertTrue(rsStock.next());
        assertEquals(5.0, rsStock.getDouble(1), 0.01);

        // Estado completado
        rsOrden = conn.createStatement().executeQuery("SELECT Estado FROM orden_compra WHERE IdCompra=1");
        assertTrue(rsOrden.next());
        assertEquals("Completada", rsOrden.getString(1));
    }
}