package com.inventario.dao;

import com.inventario.config.ConexionBD;
import com.inventario.model.OrdenCompra;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class OrdenCompraDAO {
    // Registrar una nueva orden en la base de datos
    public void registrarOrden(OrdenCompra orden) {
        String sql = "INSERT INTO orden_compra (IdProveedor, IdEmpleado, Tipo_de_Compra, Precio_total, " +
                "Fecha_de_Compra, Fecha_de_Entrega, Observacion) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, orden.getIdProveedor());
            stmt.setInt(2, orden.getIdEmpleado());
            stmt.setString(3, orden.getTipoDeCompra());
            stmt.setDouble(4, orden.getPrecioTotal());
            stmt.setDate(5, new java.sql.Date(orden.getFechaDeCompra().getTime()));
            stmt.setDate(6, new java.sql.Date(orden.getFechaDeEntrega().getTime()));
            stmt.setString(7, orden.getObservacion());

            int filas = stmt.executeUpdate();

            if (filas > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        orden.setIdCompra(rs.getInt(1));
                    }
                }
                System.out.println("✅ Orden registrada con éxito. ID generado: " + orden.getIdCompra());
            }

        } catch (SQLException e) {
            System.out.println("❌ Error al registrar orden: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Listar todas las órdenes de compra
    public List<OrdenCompra> listarOrdenes() {
        List<OrdenCompra> lista = new ArrayList<>();
        String sql = "SELECT * FROM orden_compra";

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                OrdenCompra orden = new OrdenCompra();
                orden.setIdCompra(rs.getInt("IdCompra"));
                orden.setIdProveedor(rs.getInt("IdProveedor"));
                orden.setIdEmpleado(rs.getInt("IdEmpleado"));
                orden.setTipoDeCompra(rs.getString("Tipo_de_Compra"));
                orden.setPrecioTotal(rs.getDouble("Precio_total"));
                orden.setFechaDeCompra(rs.getDate("Fecha_de_Compra"));
                orden.setFechaDeEntrega(rs.getDate("Fecha_de_Entrega"));
                orden.setObservacion(rs.getString("Observacion"));

                lista.add(orden);
            }

        } catch (SQLException e) {
            System.out.println("❌ Error al listar órdenes: " + e.getMessage());
            e.printStackTrace();
        }

        return lista;
    }
}
