package com.inventario.dao;

// Imports del Sistema Antiguo (Mantener para compatibilidad)
import com.paraEliminar.CreationRecipePage.Receta;
import com.paraEliminar.CreationRecipePage.Ingrediente;

// Imports del Sistema Nuevo (Para RecetasPage nuevo)
import com.inventario.ui.produccion.RecetaModel;
import com.inventario.ui.produccion.IngredienteItem;

import com.inventario.config.ConexionBD;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RecetaDAO {

    // ==========================================
    // 1. GUARDAR RECETA
    // ==========================================

    // Método ORIGINAL (El que usa tu App)
    public boolean guardarReceta(Receta receta) {
        Connection conn = null;
        try {
            conn = ConexionBD.getConnection();
            if (conn == null) {
                System.err.println("❌ No hay conexión a la base de datos");
                return false;
            }
            // Delegamos la lógica al método flexible
            return guardarReceta(conn, receta);

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            // AQUÍ cerramos la conexión real
            if (conn != null) {
                try { conn.close(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
        }
    }

    // Método PARA TEST (Recibe la conexión y mantiene TUS LOGS)
    public boolean guardarReceta(Connection conn, Receta receta) {
        PreparedStatement stmtReceta = null;
        PreparedStatement stmtIngrediente = null;

        try {
            // Nota: No abrimos conexión aquí, usamos la que nos pasan (conn)

            // Desactivar auto-commit para hacer transacción
            conn.setAutoCommit(false);

            System.out.println("🔄 Guardando receta: " + receta.getNombre());

            // 1. Insertar la receta
            String sqlReceta = "INSERT INTO recetas (nombre, cantidad_producida, unidad_producida) VALUES (?, ?, ?)";
            stmtReceta = conn.prepareStatement(sqlReceta, Statement.RETURN_GENERATED_KEYS);
            stmtReceta.setString(1, receta.getNombre());
            stmtReceta.setDouble(2, receta.getCantidadProducida());
            stmtReceta.setString(3, receta.getUnidadProducida());

            int filasAfectadas = stmtReceta.executeUpdate();
            System.out.println("   Receta insertada, filas afectadas: " + filasAfectadas);

            // Obtener el ID generado
            ResultSet rs = stmtReceta.getGeneratedKeys();
            int recetaId = 0;
            if (rs.next()) {
                recetaId = rs.getInt(1);
                System.out.println("   ID generado para la receta: " + recetaId);
            } else {
                System.err.println("❌ No se pudo obtener el ID de la receta");
                conn.rollback();
                return false;
            }

            // 2. Insertar los ingredientes
            String sqlIngrediente = "INSERT INTO ingredientes (receta_id, nombre, cantidad, unidad) VALUES (?, ?, ?, ?)";
            stmtIngrediente = conn.prepareStatement(sqlIngrediente);

            int ingredientesGuardados = 0;
            for (Ingrediente ing : receta.getIngredientes()) {
                stmtIngrediente.setInt(1, recetaId);
                stmtIngrediente.setString(2, ing.getNombre());
                stmtIngrediente.setDouble(3, ing.getCantidad());
                stmtIngrediente.setString(4, ing.getUnidad());
                stmtIngrediente.executeUpdate();
                ingredientesGuardados++;
            }

            System.out.println("   " + ingredientesGuardados + " ingredientes insertados");

            // Confirmar transacción
            conn.commit();
            // Importante: En los tests no queremos que se cierre la conexión,
            // así que solo reseteamos el autocommit si es necesario, pero no cerramos conn.
            conn.setAutoCommit(true);

            System.out.println("✅ Receta guardada exitosamente: " + receta.getNombre());
            return true;

        } catch (SQLException e) {
            System.err.println("❌ Error SQL al guardar receta: " + e.getMessage());
            e.printStackTrace();

            if (conn != null) {
                try {
                    conn.rollback();
                    System.err.println("   Transacción revertida");
                } catch (SQLException ex) {
                    System.err.println("❌ Error al revertir transacción: " + ex.getMessage());
                }
            }
            return false;

        } finally {
            // Solo cerramos los statements internos.
            // NO cerramos 'conn' aquí, porque si es un Test, la necesitamos viva.
            try {
                if (stmtReceta != null) stmtReceta.close();
                if (stmtIngrediente != null) stmtIngrediente.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // ==========================================
    // 2. CARGAR RECETAS
    // ==========================================

    public List<Receta> cargarTodasLasRecetas() {
        Connection conn = null;
        try {
            conn = ConexionBD.getConnection();
            if (conn == null) {
                System.err.println("❌ No hay conexión a la base de datos");
                return new ArrayList<>();
            }
            return cargarTodasLasRecetas(conn);
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        } finally {
            if (conn != null) try { conn.close(); } catch (Exception ex) {}
        }
    }

    public List<Receta> cargarTodasLasRecetas(Connection conn) {
        List<Receta> recetas = new ArrayList<>();
        Statement stmt = null;
        PreparedStatement stmtIng = null;

        try {
            System.out.println("🔄 Cargando recetas desde la base de datos...");

            // 1. Cargar recetas
            String sqlRecetas = "SELECT * FROM recetas ORDER BY fecha_creacion DESC";
            stmt = conn.createStatement();
            ResultSet rsRecetas = stmt.executeQuery(sqlRecetas);

            int contadorRecetas = 0;
            while (rsRecetas.next()) {
                int recetaId = rsRecetas.getInt("id");
                String nombre = rsRecetas.getString("nombre");
                double cantidadProducida = rsRecetas.getDouble("cantidad_producida");
                String unidadProducida = rsRecetas.getString("unidad_producida");

                System.out.println("   Cargando receta: " + nombre + " (ID: " + recetaId + ")");

                // 2. Cargar ingredientes de esta receta
                String sqlIngredientes = "SELECT * FROM ingredientes WHERE receta_id = ?";
                stmtIng = conn.prepareStatement(sqlIngredientes);
                stmtIng.setInt(1, recetaId);
                ResultSet rsIng = stmtIng.executeQuery();

                List<Ingrediente> ingredientes = new ArrayList<>();
                int contadorIngredientes = 0;
                while (rsIng.next()) {
                    String nombreIng = rsIng.getString("nombre");
                    double cantidad = rsIng.getDouble("cantidad");
                    String unidad = rsIng.getString("unidad");

                    ingredientes.add(new Ingrediente(nombreIng, cantidad, unidad));
                    contadorIngredientes++;
                }

                System.out.println("      → " + contadorIngredientes + " ingredientes cargados");

                Receta receta = new Receta(nombre, cantidadProducida, unidadProducida, ingredientes);
                recetas.add(receta);
                contadorRecetas++;
            }

            System.out.println("✅ Total de recetas cargadas: " + contadorRecetas);

        } catch (SQLException e) {
            System.err.println("❌ Error SQL al cargar recetas: " + e.getMessage());
            e.printStackTrace();

        } finally {
            // Cerramos statements, pero NO conn
            try {
                if (stmt != null) stmt.close();
                if (stmtIng != null) stmtIng.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return recetas;
    }

    // ==========================================
    // 3. ELIMINAR RECETA
    // ==========================================

    public boolean eliminarReceta(String nombreReceta) {
        Connection conn = null;
        try {
            conn = ConexionBD.getConnection();
            if (conn == null) return false;
            return eliminarReceta(conn, nombreReceta);
        } catch (SQLException e) {
            return false;
        } finally {
            if (conn != null) try { conn.close(); } catch (Exception ex) {}
        }
    }

    public boolean eliminarReceta(Connection conn, String nombreReceta) {
        PreparedStatement stmt = null;
        try {
            System.out.println("🔄 Eliminando receta: " + nombreReceta);

            String sql = "DELETE FROM recetas WHERE nombre = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, nombreReceta);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("✅ Receta eliminada: " + nombreReceta + " (" + rowsAffected + " filas)");
                return true;
            } else {
                System.err.println("⚠️ No se encontró la receta: " + nombreReceta);
                return false;
            }

        } catch (SQLException e) {
            System.err.println("❌ Error SQL al eliminar receta: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try { if (stmt != null) stmt.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }
    // ========================================================================
    //  SECCIÓN 2: SISTEMA NUEVO (RecetasPage) - ¡ACTUALIZADO PARA TESTS!
    // ========================================================================

    // --- A. GUARDAR NUEVO ---

    // 1. Método Lógico (Recibe Conexión -> Este usan los TESTS)
    public boolean guardarRecetaCompleta(Connection conn, String nombre, double cant, String unidad, String destino, List<IngredienteItem> ingredientes, Integer idEdicion) {
        try {
            conn.setAutoCommit(false);
            int idReceta;
            boolean esNuevo = (idEdicion == null);

            // Insertar/Actualizar Cabecera
            if (esNuevo) {
                String sql = "INSERT INTO recetas (nombre, cantidad_producida, unidad_producida, tipo_destino) VALUES (?,?,?,?)";
                try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, nombre); ps.setDouble(2, cant); ps.setString(3, unidad); ps.setString(4, destino);
                    ps.executeUpdate();
                    ResultSet rs = ps.getGeneratedKeys();
                    if (rs.next()) idReceta = rs.getInt(1);
                    else throw new SQLException("No ID generado");
                }
            } else {
                idReceta = idEdicion;
                String sql = "UPDATE recetas SET nombre=?, cantidad_producida=?, unidad_producida=?, tipo_destino=? WHERE id=?";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, nombre); ps.setDouble(2, cant); ps.setString(3, unidad); ps.setString(4, destino); ps.setInt(5, idReceta);
                    ps.executeUpdate();
                }
                try (PreparedStatement psDel = conn.prepareStatement("DELETE FROM ingredientes WHERE receta_id=?")) {
                    psDel.setInt(1, idReceta); psDel.executeUpdate();
                }
            }

            // Insertar Ingredientes
            String sqlIng = "INSERT INTO ingredientes (receta_id, IdProducto, IdIntermedio, cantidad, unidad, tipo_origen) VALUES (?,?,?,?,?,?)";
            try (PreparedStatement ps = conn.prepareStatement(sqlIng)) {
                for (IngredienteItem i : ingredientes) {
                    ps.setInt(1, idReceta);
                    if ("INSUMO".equals(i.getTipoOrigen())) { ps.setInt(2, i.getIdReferencia()); ps.setNull(3, Types.INTEGER); }
                    else { ps.setNull(2, Types.INTEGER); ps.setInt(3, i.getIdReferencia()); }
                    ps.setDouble(4, i.getCantidad()); ps.setString(5, i.getUnidad()); ps.setString(6, i.getTipoOrigen());
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            conn.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            if(conn!=null) try{conn.rollback();}catch(Exception ex){}
            return false;
        }
        // Nota: NO cerramos la conexión aquí porque el Test la controla
    }

    // 2. Método UI (Abre Conexión -> Este usa la APP)
    public boolean guardarRecetaCompleta(String nombre, double cant, String unidad, String destino, List<IngredienteItem> ingredientes, Integer idEdicion) {
        Connection conn = null;
        try {
            conn = ConexionBD.getConnection();
            return guardarRecetaCompleta(conn, nombre, cant, unidad, destino, ingredientes, idEdicion);
        } catch (Exception e) {
            return false;
        } finally {
            if(conn!=null) try{conn.close();}catch(Exception ex){}
        }
    }

    // --- B. CARGAR NUEVO ---

    // 1. Método Lógico (Para Tests)
    public List<RecetaModel> cargarRecetas(Connection conn) throws SQLException {
        List<RecetaModel> lista = new ArrayList<>();
        String sql = "SELECT id, nombre, cantidad_producida, unidad_producida, tipo_destino FROM recetas ORDER BY id DESC";
        try (Statement s = conn.createStatement(); ResultSet rs = s.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new RecetaModel(rs.getInt("id"), rs.getString("nombre"), rs.getDouble("cantidad_producida"), rs.getString("unidad_producida"), rs.getString("tipo_destino")));
            }
        }
        return lista;
    }

    // 2. Método UI (Para la App)
    public List<RecetaModel> cargarRecetas() {
        try (Connection c = ConexionBD.getConnection()) { return cargarRecetas(c); } catch (Exception e) { return new ArrayList<>(); }
    }

    // --- C. ELIMINAR NUEVO (Por ID) ---

    // 1. Método Lógico (Para Tests)
    public boolean eliminarReceta(Connection conn, int id) {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM recetas WHERE id=?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch(Exception e) { return false; }
    }

    // 2. Método UI (Para la App)
    public boolean eliminarReceta(int id) {
        try (Connection c = ConexionBD.getConnection()) { return eliminarReceta(c, id); } catch (Exception e) { return false; }
    }
    // Método para la App (Usa la conexión real)


    // Versión 1: Para la App (Abre su propia conexión real)
    public boolean verificarConexion() {
        try (Connection conn = ConexionBD.getConnection()) {
            return verificarConexion(conn); // Llama a la versión 2
        } catch (SQLException e) {
            System.err.println("❌ Error al intentar conectar: " + e.getMessage());
            return false;
        }
    }

    // Versión 2: Para Tests (Usa la conexión que le pasan)
    public boolean verificarConexion(Connection conn) {
        try {
            if (conn != null && !conn.isClosed()) {
                // Usamos getMetaData para evitar errores con SQLite
                String producto = conn.getMetaData().getDatabaseProductName();
                System.out.println("✅ Conexión verificada con: " + producto);
                return true;
            } else {
                System.err.println("❌ La conexión es nula o está cerrada.");
                return false;
            }
        } catch (SQLException e) {
            System.err.println("❌ Error al verificar conexión: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}