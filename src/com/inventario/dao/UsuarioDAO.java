package com.inventario.dao;

import com.inventario.config.ConexionBD;
import com.inventario.ui.admin.UsuarioModel;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {

    // Método para TEST (Recibe conexión)
    public boolean guardarUsuario(Connection conn, UsuarioModel u, boolean esNuevo) {
        String sql = esNuevo
                ? "INSERT INTO empleado (Tipo_de_empleado, Nombre_y_Apellido, Telefono, DNI, Turnos, Horario, user, password) VALUES (?,?,?,?,?,?,?,?)"
                : "UPDATE empleado SET Tipo_de_empleado=?, Nombre_y_Apellido=?, Telefono=?, DNI=?, Turnos=?, Horario=?, user=?, password=? WHERE IdEmpleado=?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, u.getRol());
            ps.setString(2, u.getNombre());
            ps.setString(3, u.getTelefono());
            ps.setString(4, u.getDni());
            ps.setString(5, u.getTurno());
            ps.setString(6, u.getHorario());
            ps.setString(7, u.getUser());
            ps.setString(8, u.getPass());
            if (!esNuevo) ps.setInt(9, u.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean eliminarUsuario(Connection conn, int id) {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM empleado WHERE IdEmpleado=?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Método para la App real (Llama a ConexionBD)
    public boolean guardarUsuario(UsuarioModel u, boolean esNuevo) {
        try (Connection conn = ConexionBD.getConnection()) { return guardarUsuario(conn, u, esNuevo); } catch (Exception e) { return false; }
    }
    public boolean eliminarUsuario(int id) {
        try (Connection conn = ConexionBD.getConnection()) { return eliminarUsuario(conn, id); } catch (Exception e) { return false; }
    }
}