package com.inventario.config;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AuthService {
    public static boolean validarUsuario(String usuario, String contrasena) {
        String sql = "SELECT * FROM railway.empleado WHERE user = ? AND password = ?";

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


}


