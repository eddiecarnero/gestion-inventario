package com.inventario.logic;

import com.inventario.config.ConexionBD;
import java.sql.*;

public class AuthService {

    // Enum para controlar los resultados del Login de forma precisa
    public enum ResultadoLogin {
        EXITO,
        CREDENCIALES_INCORRECTAS,
        BLOQUEADO_POR_ADMIN_EXISTENTE
    }

    // ==========================================
    // 1. MÉTODO DE LOGIN CENTRALIZADO (NUEVO)
    // ==========================================

    // Versión para la App (Abre conexión)
    public static ResultadoLogin intentarLogin(String usuario, String pass) {
        try (Connection conn = ConexionBD.getConnection()) {
            return intentarLogin(conn, usuario, pass);
        } catch (SQLException e) {
            e.printStackTrace();
            return ResultadoLogin.CREDENCIALES_INCORRECTAS;
        }
    }

    // Versión para Test (Lógica Pura)
    public static ResultadoLogin intentarLogin(Connection conn, String usuario, String pass) {
        // A. Caso Especial: Usuario temporal "admin"
        if ("admin".equals(usuario) && "1234".equals(pass)) {
            // Regla de Negocio: Si ya hay un admin real, bloqueamos la puerta trasera
            if (existeAdministradorEnBD(conn)) {
                return ResultadoLogin.BLOQUEADO_POR_ADMIN_EXISTENTE;
            }
            return ResultadoLogin.EXITO;
        }

        // B. Caso Normal: Verificar en Base de Datos
        if (validarUsuario(conn, usuario, pass)) {
            return ResultadoLogin.EXITO;
        }

        return ResultadoLogin.CREDENCIALES_INCORRECTAS;
    }

    // ==========================================
    // 2. MÉTODOS DE CONSULTA (Validaciones)
    // ==========================================

    // Validar Usuario y Contraseña
    public static boolean validarUsuario(String usuario, String contrasena) {
        try (Connection conn = ConexionBD.getConnection()) { return validarUsuario(conn, usuario, contrasena); } catch (SQLException e) { return false; }
    }

    public static boolean validarUsuario(Connection conn, String usuario, String contrasena) {
        String sql = "SELECT * FROM empleado WHERE user = ? AND password = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, usuario);
            stmt.setString(2, contrasena);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) { return false; }
    }

    // Verificar si existe algún Administrador real
    public static boolean existeAdministradorEnBD() {
        try (Connection conn = ConexionBD.getConnection()) { return existeAdministradorEnBD(conn); } catch (SQLException e) { return false; }
    }

    public static boolean existeAdministradorEnBD(Connection conn) {
        String sql = "SELECT COUNT(*) FROM empleado WHERE Tipo_de_empleado = 'Administrador'";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    // ==========================================
    // 3. MÉTODOS DE DATOS (Nombre y Rol)
    // ==========================================

    public static String obtenerNombre(String usuario) {
        if ("admin".equals(usuario)) return "Administrador del Sistema (Temp)";
        try (Connection conn = ConexionBD.getConnection()) { return obtenerNombre(conn, usuario); } catch (SQLException e) { return "Usuario"; }
    }

    public static String obtenerNombre(Connection conn, String usuario) {
        if ("admin".equals(usuario)) return "Administrador del Sistema (Temp)";
        String sql = "SELECT Nombre_y_Apellido FROM empleado WHERE user = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, usuario);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getString("Nombre_y_Apellido");
        } catch (SQLException e) { e.printStackTrace(); }
        return "Usuario";
    }

    public static String obtenerTipoEmpleado(String usuario) {
        if ("admin".equals(usuario)) return "Administrador";
        try (Connection conn = ConexionBD.getConnection()) { return obtenerTipoEmpleado(conn, usuario); } catch (SQLException e) { return "Empleado"; }
    }

    public static String obtenerTipoEmpleado(Connection conn, String usuario) {
        if ("admin".equals(usuario)) return "Administrador";
        String sql = "SELECT Tipo_de_empleado FROM empleado WHERE user = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, usuario);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getString("Tipo_de_empleado");
        } catch (SQLException e) { e.printStackTrace(); }
        return "Empleado";
    }
}