package com.inventario.config;

import java.sql.Connection;
import java.sql.SQLException;

public class TestConexion {
    public static void main(String[] args) {
        try (Connection conn = ConexionBD.getConnection()) {
            if (conn != null) {
                System.out.println("✅ Conexión exitosa a MySQL.");
            }
        } catch (SQLException e) {
            System.out.println("❌ Error de conexión: " + e.getMessage());
        }
    }
}
