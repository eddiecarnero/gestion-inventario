package com.inventario.logic;

import com.inventario.config.ConexionBD;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DashboardService {

    // Método para la App
    public DashboardStats obtenerEstadisticas() {
        try (Connection conn = ConexionBD.getConnection()) {
            return obtenerEstadisticas(conn);
        } catch (SQLException e) {
            e.printStackTrace();
            return new DashboardStats();
        }
    }

    // Método para el Test (Recibe conexión)
    public DashboardStats obtenerEstadisticas(Connection conn) throws SQLException {
        DashboardStats stats = new DashboardStats();
        Statement stmt = conn.createStatement();

        // 1. ALMACÉN 1 (Insumos)
        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM producto");
        if (rs.next()) stats.totalInsumos = rs.getInt(1);

        rs = stmt.executeQuery("SELECT SUM(CantidadActual) FROM lotes");
        if (rs.next()) stats.stockInsumos = rs.getDouble(1);

        // Alertas Stock Bajo (Lógica compleja SQL)
        String sqlAlertas = "SELECT COUNT(*) FROM producto p " +
                "LEFT JOIN (SELECT IdProducto, SUM(CantidadActual) as total FROM lotes GROUP BY IdProducto) l " +
                "ON p.IdProducto=l.IdProducto " +
                "WHERE COALESCE(l.total,0) <= p.Stock_Minimo";
        rs = stmt.executeQuery(sqlAlertas);
        if (rs.next()) stats.alertasStockBajo = rs.getInt(1);

        // 2. ALMACÉN 2 (Intermedios)
        try {
            rs = stmt.executeQuery("SELECT COUNT(*) FROM productos_intermedios");
            if (rs.next()) stats.totalIntermedios = rs.getInt(1);

            rs = stmt.executeQuery("SELECT SUM(CantidadActual) FROM lotes_intermedios");
            if (rs.next()) stats.stockIntermedios = rs.getDouble(1);
        } catch(Exception e) { /* Ignorar si no existen tablas aun */ }

        // 3. ALMACÉN 3 (Terminados)
        try {
            rs = stmt.executeQuery("SELECT COUNT(*) FROM productos_terminados");
            if (rs.next()) stats.totalTerminados = rs.getInt(1);

            rs = stmt.executeQuery("SELECT SUM(CantidadActual) FROM lotes_terminados");
            if (rs.next()) stats.stockTerminados = rs.getDouble(1);
        } catch(Exception e) { /* Ignorar */ }

        return stats;
    }

    // Método extra para obtener lista de alertas (si la necesitas en UI)
    public List<String> obtenerNombresBajoStock(Connection conn) {
        List<String> lista = new ArrayList<>();
        String sql = "SELECT p.Tipo_de_Producto FROM producto p " +
                "LEFT JOIN (SELECT IdProducto, SUM(CantidadActual) as total FROM lotes GROUP BY IdProducto) l " +
                "ON p.IdProducto=l.IdProducto " +
                "WHERE COALESCE(l.total,0) <= p.Stock_Minimo";
        try (Statement s = conn.createStatement(); ResultSet rs = s.executeQuery(sql)) {
            while(rs.next()) lista.add(rs.getString(1));
        } catch(Exception e) {}
        return lista;
    }
    // --- NUEVOS MÉTODOS PARA LIMPIAR LA VISTA ---

    // 1. Obtener lista de alertas (Abre su propia conexión)
    public List<String> obtenerNombresBajoStock() {
        try (Connection conn = ConexionBD.getConnection()) {
            return obtenerNombresBajoStock(conn);
        } catch (Exception e) { return new ArrayList<>(); }
    }

    // (La versión interna que recibe conexión ya la tienes, déjala ahí)

    // 2. Obtener Top Ventas (Nuevo)
    public List<String> obtenerTopVentas() {
        List<String> lista = new ArrayList<>();
        String sql = "SELECT p.Nombre, SUM(v.Cantidad) as total " +
                "FROM ventas v " +
                "JOIN productos_terminados p ON v.IdProductoTerminado = p.IdProductoTerminado " +
                "GROUP BY v.IdProductoTerminado " +
                "ORDER BY total DESC LIMIT 5";

        try (Connection conn = ConexionBD.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(rs.getString(1) + " (" + rs.getInt(2) + " unid.)");
            }
        } catch (Exception e) { e.printStackTrace(); }
        return lista;
    }
}