package com.inventario.config;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class AuthService {

    // --- 1. VALIDAR USUARIO ---

    // Método original (Usado por LoginPage)
    public static boolean validarUsuario(String usuario, String contrasena) {
        try (Connection conn = ConexionBD.getConnection()) {
            return validarUsuario(conn, usuario, contrasena);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Método para Test (Recibe conexión)
    public static boolean validarUsuario(Connection conn, String usuario, String contrasena) {
        String sql = "SELECT * FROM empleado WHERE user = ? AND password = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, usuario);
            stmt.setString(2, contrasena);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- 2. OBTENER NOMBRE ---

    public static String obtenerNombre(String usuario) {
        if ("admin".equals(usuario)) return "Administrador del Sistema (Temp)";
        try (Connection conn = ConexionBD.getConnection()) {
            return obtenerNombre(conn, usuario);
        } catch (SQLException e) {
            e.printStackTrace();
            return "Usuario";
        }
    }

    public static String obtenerNombre(Connection conn, String usuario) {
        // --- AGREGA ESTA LÍNEA AQUÍ TAMBIÉN ---
        if ("admin".equals(usuario)) return "Administrador del Sistema (Temp)";
        // --------------------------------------

        String sql = "SELECT Nombre_y_Apellido FROM empleado WHERE user = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, usuario);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getString("Nombre_y_Apellido");
        } catch (SQLException e) { e.printStackTrace(); }
        return "Usuario";
    }

    // --- 3. OBTENER TIPO EMPLEADO ---

    public static String obtenerTipoEmpleado(String usuario) {
        if ("admin".equals(usuario)) return "Administrador";
        try (Connection conn = ConexionBD.getConnection()) {
            return obtenerTipoEmpleado(conn, usuario);
        } catch (SQLException e) {
            e.printStackTrace();
            return "Empleado";
        }
    }

    public static String obtenerTipoEmpleado(Connection conn, String usuario) {
        // --- AGREGA ESTA LÍNEA AQUÍ ---
        if ("admin".equals(usuario)) return "Administrador";
        // ------------------------------

        String sql = "SELECT Tipo_de_empleado FROM empleado WHERE user = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, usuario);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getString("Tipo_de_empleado");
        } catch (SQLException e) { e.printStackTrace(); }
        return "Empleado";
    }

    // --- 4. EXISTE ADMINISTRADOR (El que faltaba) ---

    // Método original (Usado por LoginPage)
    public static boolean existeAdministradorEnBD() {
        try (Connection conn = ConexionBD.getConnection()) {
            return existeAdministradorEnBD(conn);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Método para Test
    public static boolean existeAdministradorEnBD(Connection conn) {
        String sql = "SELECT COUNT(*) FROM empleado WHERE Tipo_de_empleado = 'Administrador'";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }
}