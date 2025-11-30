package com.inventario.dao;

import com.inventario.config.ConexionBD;
import java.sql.*;
import java.time.LocalDate;

public class VentaDAO {

    // --- 1. REGISTRAR VENTA ---

    // Método original (Usado por la App)
    public boolean registrarVenta(int idProductoTerminado, int cantidad, double precio, String cliente, LocalDate fecha) {
        Connection conn = null;
        try {
            conn = ConexionBD.getConnection();
            // Llamamos a la versión flexible
            return registrarVenta(conn, idProductoTerminado, cantidad, precio, cliente, fecha);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            // AQUÍ sí cerramos la conexión porque la abrió este método
            if (conn != null) try { conn.close(); } catch (Exception ex) {}
        }
    }

    // Método para TEST (Recibe conexión y NO la cierra al final)
    public boolean registrarVenta(Connection conn, int idProductoTerminado, int cantidad, double precio, String cliente, LocalDate fecha) {
        try {
            conn.setAutoCommit(false); // Iniciamos transacción

            // 1. Verificar Stock
            if (!hayStockSuficiente(conn, idProductoTerminado, cantidad)) {
                conn.rollback(); // Importante hacer rollback si fallamos aquí manual
                return false; // Retornamos false controlado
            }

            // 2. Insertar Venta
            String sqlVenta = "INSERT INTO ventas (IdProductoTerminado, FechaVenta, Cantidad, PrecioUnitario, Total, Cliente) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sqlVenta)) {
                ps.setInt(1, idProductoTerminado);
                ps.setString(2, fecha.toString());
                ps.setInt(3, cantidad);
                ps.setDouble(4, precio);
                ps.setDouble(5, cantidad * precio);
                ps.setString(6, cliente);
                ps.executeUpdate();
            }

            // 3. Descontar de Lotes (FEFO)
            descontarDeLotes(conn, idProductoTerminado, cantidad);

            conn.commit(); // Confirmar cambios
            return true;

        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (Exception ex) {}
            e.printStackTrace();
            System.err.println("Error al registrar venta: " + e.getMessage());
            return false;
        }
        // NOTA: No cerramos la conexión aquí para que el Test pueda seguir usándola
    }

    // --- MÉTODOS PRIVADOS (Se quedan igual, solo ajustamos visibilidad si fuera necesario) ---

    private boolean hayStockSuficiente(Connection conn, int id, int cantidad) throws SQLException {
        String sql = "SELECT SUM(CantidadActual) FROM lotes_terminados WHERE IdProductoTerminado = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) >= cantidad;
            }
        }
        return false;
    }

    private void descontarDeLotes(Connection conn, int idProducto, int cantidadRequerida) throws SQLException {
        int faltante = cantidadRequerida;
        // Ordenamos por FechaVencimiento ASC para cumplir FEFO
        String sqlLotes = "SELECT IdLoteTerminado, CantidadActual FROM lotes_terminados WHERE IdProductoTerminado = ? AND CantidadActual > 0 ORDER BY FechaVencimiento ASC";

        try (PreparedStatement ps = conn.prepareStatement(sqlLotes)) {
            ps.setInt(1, idProducto);
            ResultSet rs = ps.executeQuery();

            while (rs.next() && faltante > 0) {
                int idLote = rs.getInt("IdLoteTerminado");
                int stockLote = rs.getInt("CantidadActual");
                int aDescontar = Math.min(stockLote, faltante);

                String updateSql = (aDescontar == stockLote)
                        ? "DELETE FROM lotes_terminados WHERE IdLoteTerminado = ?"
                        : "UPDATE lotes_terminados SET CantidadActual = CantidadActual - ? WHERE IdLoteTerminado = ?";

                try (PreparedStatement psUpd = conn.prepareStatement(updateSql)) {
                    if (aDescontar < stockLote) {
                        psUpd.setInt(1, aDescontar);
                        psUpd.setInt(2, idLote);
                    } else {
                        psUpd.setInt(1, idLote);
                    }
                    psUpd.executeUpdate();
                }
                faltante -= aDescontar;
            }
        }

        if (faltante > 0) {
            throw new SQLException("Inconsistencia: Stock insuficiente al momento de descontar lotes.");
        }
    }

    // --- MÉTODOS DE CONSULTA (Sobrecarga para Test) ---

    public Integer obtenerIdPorNombre(String nombre) {
        try (Connection conn = ConexionBD.getConnection()) {
            return obtenerIdPorNombre(conn, nombre);
        } catch (SQLException e) { return null; }
    }

    public Integer obtenerIdPorNombre(Connection conn, String nombre) {
        String sql = "SELECT IdProductoTerminado FROM productos_terminados WHERE lower(Nombre) = lower(?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nombre.trim());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public Double obtenerPrecioActual(int id) {
        try (Connection conn = ConexionBD.getConnection()) {
            return obtenerPrecioActual(conn, id);
        } catch(Exception e){ return 0.0; }
    }

    public Double obtenerPrecioActual(Connection conn, int id) {
        try (PreparedStatement ps = conn.prepareStatement("SELECT PrecioVenta FROM productos_terminados WHERE IdProductoTerminado=?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) return rs.getDouble(1);
        } catch(Exception e){}
        return 0.0;
    }
}