package com.inventario.config;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AuthService {

    // Validar credenciales (BD)
    public static boolean validarUsuario(String usuario, String contrasena) {
        String sql = "SELECT * FROM empleado WHERE user = ? AND password = ?";
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, usuario);
            stmt.setString(2, contrasena);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String obtenerNombre(String usuario) {
        // Si es el admin hardcoded
        if ("admin".equals(usuario)) return "Administrador del Sistema (Temp)";

        String sql = "SELECT Nombre_y_Apellido FROM empleado WHERE user = ?";
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, usuario);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getString("Nombre_y_Apellido");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Usuario";
    }

    public static String obtenerTipoEmpleado(String usuario) {
        if ("admin".equals(usuario)) return "Administrador"; // El hardcoded es Admin por defecto

        String sql = "SELECT Tipo_de_empleado FROM empleado WHERE user = ?";
        try (Connection con = ConexionBD.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, usuario);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getString("Tipo_de_empleado");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Empleado";
    }

    // --- NUEVO: Verificar si ya existe un Admin real en la BD ---
    public static boolean existeAdministradorEnBD() {
        String sql = "SELECT COUNT(*) FROM empleado WHERE Tipo_de_empleado = 'Administrador'";
        try (Connection conn = ConexionBD.getConnection();
             java.sql.Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1) > 0; // Devuelve true si hay al menos 1 admin
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}