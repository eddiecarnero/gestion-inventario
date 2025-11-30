package com.inventario.dao;

import com.inventario.ui.produccion.IngredienteItem;
import com.inventario.ui.produccion.RecetaModel;
import org.junit.jupiter.api.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// IMPORTS PARA EL SISTEMA ANTIGUO (Asegúrate de que existan o comenta el test 'Legacy' si te da error)
import com.paraEliminar.CreationRecipePage.Receta;
import com.paraEliminar.CreationRecipePage.Ingrediente;

import static org.junit.jupiter.api.Assertions.*;

class RecetaDAOTest {

    private Connection conn;
    private RecetaDAO dao;

    @BeforeEach
    void setUp() throws SQLException {
        // 1. Usamos base de datos en memoria (rápida y limpia)
        conn = DriverManager.getConnection("jdbc:sqlite::memory:");
        dao = new RecetaDAO();

        // 2. Creamos la estructura de tablas HÍBRIDA (Soporta lo viejo y lo nuevo)
        try (Statement stmt = conn.createStatement()) {
            // Tabla Recetas (Campos viejos + nuevos como 'tipo_destino')
            stmt.execute("CREATE TABLE recetas (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "nombre TEXT, " +
                    "cantidad_producida REAL, " +
                    "unidad_producida TEXT, " +
                    "tipo_destino TEXT, " + // Campo del sistema nuevo
                    "fecha_creacion DATE DEFAULT (datetime('now','localtime'))" +
                    ");");

            // Tabla Ingredientes (Campos viejos + nuevos IDs)
            stmt.execute("CREATE TABLE ingredientes (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "receta_id INTEGER, " +
                    "nombre TEXT, " +
                    "cantidad REAL, " +
                    "unidad TEXT, " +
                    "IdProducto INTEGER, " +      // Nuevo
                    "IdIntermedio INTEGER, " +    // Nuevo
                    "tipo_origen TEXT, " +        // Nuevo
                    "FOREIGN KEY(receta_id) REFERENCES recetas(id)" +
                    ");");
        }
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (conn != null) conn.close();
    }

    // ==========================================
    // 🧪 TEST SISTEMA NUEVO (El Futuro)
    // ==========================================

    @Test
    void sistemaNuevo_GuardarRecetaCompleta_DebeInsertarTodo() throws SQLException {
        // 1. Preparar Ingredientes (Insumo y Intermedio)
        List<IngredienteItem> ingredientes = new ArrayList<>();
        ingredientes.add(new IngredienteItem(10, "Harina", 1.0, "Kg", "INSUMO"));
        ingredientes.add(new IngredienteItem(20, "Masa Madre", 0.5, "Lt", "INTERMEDIO"));

        // 2. Ejecutar Guardado (Nuevo -> idEdicion = null)
        boolean exito = dao.guardarRecetaCompleta(conn, "Pan Artesanal", 10.0, "Unidad", "FINAL", ingredientes, null);

        assertTrue(exito, "El guardado debería ser exitoso");

        // 3. Verificaciones en BD
        try (Statement stmt = conn.createStatement()) {
            // A. Verificar Cabecera
            ResultSet rsReceta = stmt.executeQuery("SELECT * FROM recetas WHERE nombre = 'Pan Artesanal'");
            assertTrue(rsReceta.next());
            assertEquals("FINAL", rsReceta.getString("tipo_destino"));
            assertEquals(10.0, rsReceta.getDouble("cantidad_producida"));
            int idReceta = rsReceta.getInt("id");

            // B. Verificar Ingredientes
            ResultSet rsIng = stmt.executeQuery("SELECT * FROM ingredientes WHERE receta_id = " + idReceta);

            // Ingrediente 1: Harina
            assertTrue(rsIng.next());
            assertEquals(10, rsIng.getInt("IdProducto")); // Verifica que guardó el ID referencia
            assertEquals("INSUMO", rsIng.getString("tipo_origen"));

            // Ingrediente 2: Masa Madre
            assertTrue(rsIng.next());
            assertEquals(20, rsIng.getInt("IdIntermedio"));
            assertEquals("INTERMEDIO", rsIng.getString("tipo_origen"));
        }
    }

    @Test
    void sistemaNuevo_CargarRecetas_DebeRetornarRecetaModel() throws SQLException {
        // Insertamos datos a mano
        conn.createStatement().execute("INSERT INTO recetas (nombre, cantidad_producida, unidad_producida, tipo_destino) VALUES ('Galletas', 50, 'Paquete', 'INTERMEDIO')");

        // Ejecutamos cargarRecetas (versión nueva)
        // Nota: Como cargarRecetas() en tu DAO abre su propia conexión,
        // aquí usamos una versión simulada o asumimos que tu DAO tiene un método 'cargarRecetas(Connection)'
        // Si no lo tiene, prueba añadiéndole uno sobrecargado.
        // Para este test, usaremos tu método sobrecargado si existe, o simularemos la lectura:

        // *Truco*: Si tu DAO no tiene cargarRecetas(conn), instanciamos el modelo a mano para probar la clase RecetaModel.
        List<RecetaModel> lista = dao.cargarRecetas(conn); // Asumiendo que añadiste este método sobrecargado en el paso anterior

        // Si no añadiste la sobrecarga cargarRecetas(Connection c) en el DAO, este test fallará al compilar.
        // Asegúrate de tener: public List<RecetaModel> cargarRecetas(Connection conn) { ... } en RecetaDAO.

        assertFalse(lista.isEmpty());
        assertEquals("Galletas", lista.get(0).getNombre());
        assertEquals("INTERMEDIO", lista.get(0).getTipoDestino());
    }

    @Test
    void sistemaNuevo_EliminarPorId_DebeBorrarCascada() throws SQLException {
        // Insertar receta
        conn.createStatement().execute("INSERT INTO recetas (id, nombre) VALUES (99, 'A Borrar')");
        conn.createStatement().execute("INSERT INTO ingredientes (receta_id, nombre) VALUES (99, 'Ingrediente X')");

        // Eliminar por ID
        boolean eliminado = dao.eliminarReceta(conn, 99); // Usando método nuevo (int)

        assertTrue(eliminado);

        // Verificar que no existe
        ResultSet rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM recetas WHERE id=99");
        rs.next();
        assertEquals(0, rs.getInt(1));
    }

    // ==========================================
    // 🏚️ TEST SISTEMA ANTIGUO (Legacy)
    // ==========================================

    @Test
    void sistemaAntiguo_GuardarReceta_DebeFuncionar() throws SQLException {
        // NOTA: Este test asume que tienes las clases Receta e Ingrediente del paquete 'com.paraEliminar'
        // Si te da error de compilación aquí, revisa los constructores.

        // 1. Crear Ingredientes Viejos
        List<Ingrediente> ingsViejos = new ArrayList<>();
        // Asumo constructor: Ingrediente(nombre, cantidad, unidad)
        ingsViejos.add(new Ingrediente("Azúcar", 100, "gr"));

        // 2. Crear Receta Vieja
        // Asumo constructor: Receta(nombre, cantidad, unidad, listaIngredientes)
        Receta recetaVieja = new Receta("Caramelo", 1, "Taza", ingsViejos);

        // 3. Guardar usando método antiguo
        boolean exito = dao.guardarReceta(conn, recetaVieja);

        assertTrue(exito, "El sistema antiguo debería seguir funcionando");

        // 4. Verificar en BD (Debe haberse guardado sin los campos nuevos como tipo_destino)
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT nombre, unidad_producida FROM recetas WHERE nombre = 'Caramelo'");
            assertTrue(rs.next());
            assertEquals("Taza", rs.getString("unidad_producida"));

            // Verificar ingrediente (nombre plano, sin ID referencia)
            ResultSet rsIng = stmt.executeQuery("SELECT nombre FROM ingredientes WHERE nombre = 'Azúcar'");
            assertTrue(rsIng.next());
        }
    }

    @Test
    void sistemaAntiguo_EliminarPorNombre() throws SQLException {
        conn.createStatement().execute("INSERT INTO recetas (nombre) VALUES ('Receta Vieja')");

        boolean eliminado = dao.eliminarReceta(conn, "Receta Vieja");

        assertTrue(eliminado);
        ResultSet rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM recetas WHERE nombre='Receta Vieja'");
        rs.next();
        assertEquals(0, rs.getInt(1));
    }
    @Test
    void verificarConexion_DebeRetornarTrue() throws SQLException {
        boolean status = dao.verificarConexion(conn);
        assertTrue(status, "La verificación de conexión debería ser exitosa con la BD en memoria");
    }
}