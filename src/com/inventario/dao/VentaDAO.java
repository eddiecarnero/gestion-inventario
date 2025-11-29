package com.inventario.dao;

import com.inventario.config.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class VentaDAO {

    // Método principal para registrar una venta individual
    public boolean registrarVenta(int idProductoTerminado, int cantidad, double precio, String cliente, LocalDate fecha) {
        Connection conn = null;
        try {
            conn = ConexionBD.getConnection();
            conn.setAutoCommit(false); // Transacción

            // 1. Verificar Stock Disponible Total
            if (!hayStockSuficiente(conn, idProductoTerminado, cantidad)) {
                throw new SQLException("Stock insuficiente para el producto ID: " + idProductoTerminado);
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

            // 3. Descontar de Lotes (FEFO - Primero en vencer, primero en salir)
            descontarDeLotes(conn, idProductoTerminado, cantidad);

            conn.commit();
            return true;

        } catch (SQLException e) {
            if (conn != null) try {
                conn.rollback();
            } catch (Exception ex) {
            }
            e.printStackTrace();
            System.err.println("Error al registrar venta: " + e.getMessage());
            return false;
        } finally {
            if (conn != null) try {
                conn.setAutoCommit(true);
                conn.close();
            } catch (Exception ex) {
            }
        }
    }

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
            throw new SQLException("Inconsistencia: El stock total indicaba suficiente, pero los lotes no alcanzaron.");
        }
    }

    // Método para buscar producto por nombre exacto (útil para el Excel)
    public Integer obtenerIdPorNombre(String nombre) {
        String sql = "SELECT IdProductoTerminado FROM productos_terminados WHERE lower(Nombre) = lower(?)";
        try (Connection conn = ConexionBD.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nombre.trim());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Obtener precio actual
    public Double obtenerPrecioActual(int id) {
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT PrecioVenta FROM productos_terminados WHERE IdProductoTerminado=?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble(1);
        } catch (Exception e) {
        }
        return 0.0;
    }
}