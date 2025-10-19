package com.inventario.dao;

import com.inventario.ui.CreationRecipePage.Receta;
import com.inventario.ui.CreationRecipePage.Ingrediente;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RecetaDAO {

    // Guardar una receta con sus ingredientes
    public boolean guardarReceta(Receta receta) {
        Connection conn = null;
        PreparedStatement stmtReceta = null;
        PreparedStatement stmtIngrediente = null;

        try {
            conn = DatabaseConnection.getConnection();

            if (conn == null) {
                System.err.println("❌ No hay conexión a la base de datos");
                return false;
            }

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
            // Cerrar recursos
            try {
                if (stmtReceta != null) stmtReceta.close();
                if (stmtIngrediente != null) stmtIngrediente.close();
                if (conn != null) conn.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // Cargar todas las recetas con sus ingredientes
    public List<Receta> cargarTodasLasRecetas() {
        List<Receta> recetas = new ArrayList<>();
        Connection conn = null;
        Statement stmt = null;
        PreparedStatement stmtIng = null;

        try {
            conn = DatabaseConnection.getConnection();

            if (conn == null) {
                System.err.println("❌ No hay conexión a la base de datos");
                return recetas;
            }

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

                // 3. Crear la receta completa
                Receta receta = new Receta(nombre, cantidadProducida, unidadProducida, ingredientes);
                recetas.add(receta);
                contadorRecetas++;
            }

            System.out.println("✅ Total de recetas cargadas: " + contadorRecetas);

        } catch (SQLException e) {
            System.err.println("❌ Error SQL al cargar recetas: " + e.getMessage());
            e.printStackTrace();

        } finally {
            // Cerrar recursos
            try {
                if (stmt != null) stmt.close();
                if (stmtIng != null) stmtIng.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return recetas;
    }

    // Eliminar una receta (los ingredientes se eliminan automáticamente por CASCADE)
    public boolean eliminarReceta(String nombreReceta) {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DatabaseConnection.getConnection();

            if (conn == null) {
                System.err.println("❌ No hay conexión a la base de datos");
                return false;
            }

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
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // Método para verificar la conexión
    public boolean verificarConexion() {
        Connection conn = DatabaseConnection.getConnection();
        if (conn != null) {
            try {
                System.out.println("✅ Conexión a la base de datos verificada");
                System.out.println("   Base de datos: " + conn.getCatalog());
                return true;
            } catch (SQLException e) {
                System.err.println("❌ Error al verificar conexión: " + e.getMessage());
                return false;
            }
        }
        System.err.println("❌ No se pudo conectar a la base de datos");
        return false;
    }
}