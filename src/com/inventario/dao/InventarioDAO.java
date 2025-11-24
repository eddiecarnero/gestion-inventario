package com.inventario.dao;

import com.inventario.config.ConexionBD;
import com.inventario.model.LoteTerminado;
import com.inventario.model.ProductoTerminado;
import com.inventario.model.RecetaSimple;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.time.LocalDate;

public class InventarioDAO {

    public ObservableList<ProductoTerminado> getProductosTerminados() throws SQLException {
        ObservableList<ProductoTerminado> listaTerminados = FXCollections.observableArrayList();
        String sql = "SELECT p.IdProductoTerminado, p.Nombre, p.PrecioVenta, p.StockMinimo, " +
                "COALESCE(SUM(l.CantidadActual), 0) as StockTotal " +
                "FROM productos_terminados p " +
                "LEFT JOIN lotes_terminados l ON p.IdProductoTerminado = l.IdProductoTerminado " +
                "GROUP BY p.IdProductoTerminado";

        try (Connection conn = ConexionBD.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                ProductoTerminado p = new ProductoTerminado(
                        rs.getInt("IdProductoTerminado"),
                        rs.getString("Nombre"),
                        rs.getDouble("PrecioVenta"),
                        rs.getInt("StockMinimo"),
                        rs.getInt("StockTotal")
                );
                listaTerminados.add(p);
            }
        }
        return listaTerminados;
    }

    public ObservableList<LoteTerminado> getLotesPorProducto(int idProducto) throws SQLException {
        ObservableList<LoteTerminado> data = FXCollections.observableArrayList();
        String sql = "SELECT IdLoteTerminado, CantidadActual, FechaVencimiento FROM lotes_terminados WHERE IdProductoTerminado=? AND CantidadActual>0";
        try(Connection c = ConexionBD.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idProducto);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                data.add(new LoteTerminado(rs.getInt(1), rs.getInt(2), rs.getString(3)));
            }
        }
        return data;
    }

    public ObservableList<RecetaSimple> getRecetasFinales() throws SQLException {
        ObservableList<RecetaSimple> lista = FXCollections.observableArrayList();
        String sql = "SELECT id, nombre, cantidad_producida, unidad_producida FROM recetas WHERE tipo_destino = 'FINAL'";
        try (Connection c = ConexionBD.getConnection();
             Statement stmt = c.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
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
