package com.inventario.config;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AuthService {
    public static boolean validarUsuario(String usuario, String contrasena) {
        String sql = "SELECT * FROM sql5804315.empleado WHERE user = ? AND password = ?";

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, usuario);
            stmt.setString(2, contrasena);

            ResultSet rs = stmt.executeQuery();

            return rs.next(); // true si encontró coincidencia
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String obtenerNombre(String usuario) {
        String sql = "SELECT Nombre_y_Apellido FROM sql5804315.empleado WHERE user = ?";

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, usuario);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String nombre = rs.getString("Nombre_y_Apellido");
                System.out.println("🔹 Nombre obtenido desde BD: " + nombre);
                return nombre;
            } else {
                System.out.println("⚠️ No se encontró registro para el usuario: " + usuario);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return "Error";
    }

    public static String obtenerTipoEmpleado(String usuario) {
        String sql = "SELECT Tipo_de_empleado FROM empleado WHERE user = ?";

        try (Connection con = ConexionBD.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setString(1, usuario);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String rango = rs.getString("tipo_de_empleado");
                System.out.println("Tipo encontrado en BD: " + rango);
                return rango.equalsIgnoreCase("Administrador") ? "Administrador" : "Empleado";
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Si no encuentra el usuario o ocurre error, devuelve "Empleado" por defecto
        return "Empleado";
    }





}


