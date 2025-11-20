package com.inventario.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class ConexionBD {

    // La ruta ahora es simple: "jdbc:sqlite:" + nombre del archivo
    // El archivo se creará en la carpeta raíz de tu proyecto/exe
    private static final String URL = "jdbc:sqlite:mamatania_inventario.db";

    public static Connection getConnection() throws SQLException {
        try {
            // Cargar el driver (a veces necesario en versiones antiguas de Java)
            Class.forName("org.sqlite.JDBC");

            Connection conn = DriverManager.getConnection(URL);

            // IMPORTANTE: En SQLite hay que activar las Foreign Keys manualmente
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON;");
            }

            return conn;

        } catch (ClassNotFoundException e) {
            throw new SQLException("No se encontró el driver de SQLite", e);
        }
    }
}