package com.inventario.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/sistema_inventario";
    private static final String USER = "root"; // Cambia esto por tu usuario
    private static final String PASSWORD = "root"; // Cambia esto por tu contraseña

    private static Connection connection = null;

    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("✓ Conexión exitosa a la base de datos");
            }
        } catch (ClassNotFoundException e) {
            System.err.println("❌ Error: Driver de MySQL no encontrado");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("❌ Error de conexión a la base de datos");
            e.printStackTrace();
        }
        return connection;
    }

    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("✓ Conexión cerrada");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}