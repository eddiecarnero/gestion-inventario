package com.inventario.dao;

import com.inventario.config.ConexionBD;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;

public class IngresoInsumosDAO {

    public boolean registrarIngresoMasivo(int idProveedor, List<ItemIngreso> items, String observacion) {
        Connection conn = null;
        try {
            conn = ConexionBD.getConnection();
            conn.setAutoCommit(false); // Inicio Transacción

            double totalCompra = items.stream().mapToDouble(i -> i.cantidad * i.costo).sum();

            // 1. Crear Orden de Compra "Automática" (Ya completada)
            int idOrden = 0;
            String sqlOrden = "INSERT INTO orden_compra (IdProveedor, IdEmpleado, Fecha_de_Compra, Fecha_de_Entrega, Precio_total, Estado, Observacion) " +
                    "VALUES (?, ?, ?, ?, ?, 'Completada', ?)";

            try (PreparedStatement ps = conn.prepareStatement(sqlOrden, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, idProveedor);
                ps.setInt(2, 1); // Usuario Admin por defecto
                ps.setString(3, LocalDate.now().toString());
                ps.setString(4, LocalDate.now().toString());
                ps.setDouble(5, totalCompra);
                ps.setString(6, observacion);
                ps.executeUpdate();

                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) idOrden = rs.getInt(1);
            }

            // Preparar queries para el bucle
            String sqlDetalle = "INSERT INTO detalle_compra (IdCompra, IdProducto, Cantidad, PrecioUnitario, SubTotal) VALUES (?, ?, ?, ?, ?)";
            String sqlLote = "INSERT INTO lotes (IdProducto, CantidadActual, FechaVencimiento, FechaIngreso) VALUES (?, ?, ?, ?)";
            String sqlStock = "UPDATE producto SET Stock = Stock + ? WHERE IdProducto = ?";
            String sqlKardex = "INSERT INTO kardex (IdProducto, Fecha, Motivo, TipoMovimiento, IdEmpleado, Cantidad) VALUES (?, ?, ?, ?, ?, ?)";

            try (PreparedStatement psDet = conn.prepareStatement(sqlDetalle);
                 PreparedStatement psLote = conn.prepareStatement(sqlLote);
                 PreparedStatement psStock = conn.prepareStatement(sqlStock);
                 PreparedStatement psKar = conn.prepareStatement(sqlKardex)) {

                for (ItemIngreso item : items) {
                    // 2. Detalle Compra
                    psDet.setInt(1, idOrden);
                    psDet.setInt(2, item.idProducto);
                    psDet.setDouble(3, item.cantidad);
                    psDet.setDouble(4, item.costo);
                    psDet.setDouble(5, item.cantidad * item.costo);
                    psDet.addBatch();

                    // 3. Crear Lote (CRÍTICO para Almacén 2)
                    psLote.setInt(1, item.idProducto);
                    psLote.setDouble(2, item.cantidad);
                    if (item.fechaVencimiento != null)
                        psLote.setString(3, item.fechaVencimiento.toString());
                    else
                        psLote.setNull(3, Types.VARCHAR);

                    psLote.setString(4, LocalDate.now().toString());
                    psLote.addBatch();

                    // 4. Actualizar Stock Global
                    psStock.setDouble(1, item.cantidad);
                    psStock.setInt(2, item.idProducto);
                    psStock.addBatch();

                    // 5. Kardex
                    psKar.setInt(1, item.idProducto);
                    psKar.setString(2, LocalDate.now().toString());
                    psKar.setString(3, "Carga Masiva (Orden #" + idOrden + ")");
                    psKar.setString(4, "Entrada");
                    psKar.setInt(5, 1);
                    psKar.setDouble(6, item.cantidad);
                    psKar.addBatch();
                }

                psDet.executeBatch();
                psLote.executeBatch();
                psStock.executeBatch();
                psKar.executeBatch();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            if (conn != null) try {
                conn.rollback();
            } catch (Exception ex) {
            }
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) try {
                conn.setAutoCommit(true);
                conn.close();
            } catch (Exception ex) {
            }
        }
    }

    public Integer obtenerIdProducto(String nombre) {
        String sql = "SELECT IdProducto FROM producto WHERE lower(Tipo_de_Producto) = lower(?)";
        try (Connection conn = ConexionBD.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nombre.trim());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) {
        }
        return null;
    }

    // Clase auxiliar para transportar los datos
    public static class ItemIngreso {
        public int idProducto;
        public double cantidad;
        public double costo;
        public LocalDate fechaVencimiento;

        public ItemIngreso(int id, double c, double costo, LocalDate v) {
            this.idProducto = id;
            this.cantidad = c;
            this.costo = costo;
            this.fechaVencimiento = v;
        }
    }
}