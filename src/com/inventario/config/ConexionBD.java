package com.inventario.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class ConexionBD {

    // La URL real que usa tu aplicación
    private static final String URL_DEFECTO = "jdbc:sqlite:mamatania_inventario.db";

    // Método 1: El que usa tu App (Llama al método flexible con la URL por defecto)
    public static Connection getConnection() throws SQLException {
        return getConnection(URL_DEFECTO);
    }

    // Método 2: El flexible (Para Tests o si quieres cambiar de BD en el futuro)
    public static Connection getConnection(String url) throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
            Connection conn = DriverManager.getConnection(url);

            // Activamos las llaves foráneas
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON;");
            }
            return conn;

        } catch (ClassNotFoundException e) {
            throw new SQLException("No se encontró el driver de SQLite", e);
        }
    }
}