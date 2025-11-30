package com.inventario.dao;

import com.inventario.config.ConexionBD;
import com.inventario.model.LoteTerminado;
import com.inventario.model.ProductoTerminado;
import com.inventario.model.RecetaSimple;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Almacen3DAO {

    // 1. Obtener Productos Terminados (Para la tabla principal)
    public List<ProductoTerminado> getProductosTerminados() {
        try (Connection conn = ConexionBD.getConnection()) {
            return getProductosTerminados(conn);
        } catch (SQLException e) { return new ArrayList<>(); }
    }

    public List<ProductoTerminado> getProductosTerminados(Connection conn) throws SQLException {
        List<ProductoTerminado> lista = new ArrayList<>();
        String sql = "SELECT p.IdProductoTerminado, p.Nombre, p.PrecioVenta, p.StockMinimo, " +
                "COALESCE(SUM(l.CantidadActual), 0) as StockTotal " +
                "FROM productos_terminados p " +
                "LEFT JOIN lotes_terminados l ON p.IdProductoTerminado = l.IdProductoTerminado " +
                "GROUP BY p.IdProductoTerminado";

        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new ProductoTerminado(
                        rs.getInt("IdProductoTerminado"),
                        rs.getString("Nombre"),
                        rs.getDouble("PrecioVenta"),
                        rs.getInt("StockMinimo"),
                        rs.getInt("StockTotal")
                ));
            }
        }
        return lista;
    }

    // 2. Obtener Lotes por Producto (Para el detalle)
    public List<LoteTerminado> getLotesPorProducto(int idProducto) {
        try (Connection conn = ConexionBD.getConnection()) {
            return getLotesPorProducto(conn, idProducto);
        } catch (SQLException e) { return new ArrayList<>(); }
    }

    public List<LoteTerminado> getLotesPorProducto(Connection conn, int idProducto) throws SQLException {
        List<LoteTerminado> data = new ArrayList<>();
        String sql = "SELECT IdLoteTerminado, CantidadActual, FechaVencimiento FROM lotes_terminados WHERE IdProductoTerminado=? AND CantidadActual>0";
        try(PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idProducto);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                data.add(new LoteTerminado(rs.getInt(1), rs.getInt(2), rs.getString(3)));
            }
        }
        return data;
    }

    // 3. Obtener Recetas Finales (Para el Combo de Producción)
    public List<RecetaSimple> getRecetasFinales() {
        try (Connection conn = ConexionBD.getConnection()) {
            return getRecetasFinales(conn);
        } catch (SQLException e) { return new ArrayList<>(); }
    }

    public List<RecetaSimple> getRecetasFinales(Connection conn) throws SQLException {
        List<RecetaSimple> lista = new ArrayList<>();
        String sql = "SELECT id, nombre, cantidad_producida, unidad_producida FROM recetas WHERE tipo_destino = 'FINAL'";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new RecetaSimple(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getDouble("cantidad_producida"),
                        rs.getString("unidad_producida")
                ));
            }
        }
        return lista;
    }
}