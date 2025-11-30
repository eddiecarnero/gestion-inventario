package com.inventario.dao;

import com.inventario.config.ConexionBD;
import com.inventario.ui.admin.ProveedorModel;
import java.sql.*;

public class ProveedorDAO {

    public boolean guardarProveedor(Connection conn, ProveedorModel p, boolean esNuevo) {
        String sql = esNuevo
                ? "INSERT INTO proveedores (Nombre_comercial, RUC, Tipo_de_proveedor, Telefono, Email, Direccion) VALUES (?,?,?,?,?,?)"
                : "UPDATE proveedores SET Nombre_comercial=?, RUC=?, Tipo_de_proveedor=?, Telefono=?, Email=?, Direccion=? WHERE IdProveedor=?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getNombre());
            ps.setString(2, p.getRuc());
            ps.setString(3, p.getTipo());
            ps.setString(4, p.getTelefono());
            ps.setString(5, p.getEmail());
            ps.setString(6, p.getDireccion());
            if (!esNuevo) ps.setInt(7, p.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean eliminarProveedor(Connection conn, int id) {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM proveedores WHERE IdProveedor=?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    // Métodos puente para la UI
    public boolean guardarProveedor(ProveedorModel p, boolean esNuevo) {
        try (Connection c = ConexionBD.getConnection()) { return guardarProveedor(c, p, esNuevo); } catch (Exception e) { return false; }
    }
    public boolean eliminarProveedor(int id) {
        try (Connection c = ConexionBD.getConnection()) { return eliminarProveedor(c, id); } catch (Exception e) { return false; }
    }
}