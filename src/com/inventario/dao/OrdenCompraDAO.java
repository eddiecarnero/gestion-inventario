package com.inventario.dao;

import com.inventario.config.ConexionBD;
import com.inventario.ui.insumos.ItemOrden;
import com.inventario.ui.insumos.ItemRecepcion;
import java.sql.*;
import java.time.LocalDate;
import java.util.List;

public class OrdenCompraDAO {

    // ==========================================
    // 1. GUARDAR ORDEN (Estado: Pendiente)
    // ==========================================

    // Método Lógico (Para Tests: Recibe Conexión)
    public boolean guardarOrden(Connection conn, int idProveedor, LocalDate fecha, List<ItemOrden> items) {
        try {
            conn.setAutoCommit(false); // Transacción

            // A. Cabecera
            int idCompra = 0;
            String sqlCab = "INSERT INTO orden_compra (IdProveedor, IdEmpleado, Fecha_de_Compra, Precio_total, Estado) VALUES (?, ?, ?, ?, 'Pendiente')";
            double total = items.stream().mapToDouble(ItemOrden::getTotal).sum();

            try (PreparedStatement ps = conn.prepareStatement(sqlCab, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, idProveedor);
                ps.setInt(2, 1); // IdEmpleado default
                ps.setString(3, fecha.toString());
                ps.setDouble(4, total);
                ps.executeUpdate();
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) idCompra = rs.getInt(1);
                else throw new SQLException("No se generó ID para la orden");
            }

            // B. Detalles
            String sqlDet = "INSERT INTO detalle_compra (IdCompra, IdProducto, Cantidad, PrecioUnitario, SubTotal) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sqlDet)) {
                for (ItemOrden i : items) {
                    ps.setInt(1, idCompra);
                    ps.setInt(2, i.getInsumoId());
                    ps.setDouble(3, i.getCantidad());
                    ps.setDouble(4, i.getPrecioUnitario());
                    ps.setDouble(5, i.getTotal());
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            try { conn.rollback(); } catch(Exception ex){}
            return false;
        }
    }

    // Método Puente (Para la App: Abre Conexión)
    public boolean guardarOrden(int idProveedor, LocalDate fecha, List<ItemOrden> items) {
        try (Connection conn = ConexionBD.getConnection()) {
            return guardarOrden(conn, idProveedor, fecha, items);
        } catch (Exception e) { return false; }
    }

    // ==========================================
    // 2. RECEPCIONAR MERCADERÍA
    // ==========================================

    // Método Lógico (Para Tests: Recibe Conexión)
    public boolean procesarRecepcion(Connection conn, int idOrden, List<ItemRecepcion> itemsRecibidos) {
        try {
            conn.setAutoCommit(false);

            String sqlLote = "INSERT INTO lotes (IdProducto, CantidadActual, FechaVencimiento, FechaIngreso) VALUES (?, ?, ?, ?)";
            String sqlStock = "UPDATE producto SET Stock = Stock + ? WHERE IdProducto = ?";
            String sqlKardex = "INSERT INTO kardex (IdProducto, Fecha, Motivo, TipoMovimiento, IdEmpleado, Cantidad) VALUES (?, ?, ?, ?, ?, ?)";

            try (PreparedStatement psLote = conn.prepareStatement(sqlLote);
                 PreparedStatement psStock = conn.prepareStatement(sqlStock);
                 PreparedStatement psKardex = conn.prepareStatement(sqlKardex)) {

                for (ItemRecepcion item : itemsRecibidos) {
                    // Lote
                    psLote.setInt(1, item.getIdProducto());
                    psLote.setDouble(2, item.getCantidad());
                    if (item.getFechaVencimiento() != null) psLote.setString(3, item.getFechaVencimiento().toString());
                    else psLote.setNull(3, Types.VARCHAR);
                    psLote.setString(4, LocalDate.now().toString());
                    psLote.addBatch();

                    // Stock
                    psStock.setDouble(1, item.getCantidad());
                    psStock.setInt(2, item.getIdProducto());
                    psStock.addBatch();

                    // Kardex
                    psKardex.setInt(1, item.getIdProducto());
                    psKardex.setString(2, LocalDate.now().toString());
                    psKardex.setString(3, "Recepción Orden #" + idOrden);
                    psKardex.setString(4, "Entrada");
                    psKardex.setInt(5, 1);
                    psKardex.setDouble(6, item.getCantidad());
                    psKardex.addBatch();
                }
                psLote.executeBatch();
                psStock.executeBatch();
                psKardex.executeBatch();
            }

            // Actualizar Estado Orden
            try (PreparedStatement psOrden = conn.prepareStatement("UPDATE orden_compra SET Estado = 'Completada' WHERE IdCompra = ?")) {
                psOrden.setInt(1, idOrden);
                psOrden.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            try { conn.rollback(); } catch(Exception ex){}
            return false;
        }
    }

    // Método Puente (Para la App: Abre Conexión)
    public boolean procesarRecepcion(int idOrden, List<ItemRecepcion> itemsRecibidos) {
        try (Connection conn = ConexionBD.getConnection()) {
            return procesarRecepcion(conn, idOrden, itemsRecibidos);
        } catch (Exception e) { return false; }
    }
}