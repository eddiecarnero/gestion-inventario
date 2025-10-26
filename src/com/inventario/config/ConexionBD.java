package com.inventario.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionBD {
    private static final String URL = "jdbc:mysql://sql5.freesqldatabase.com:3306/sql5804315?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USER = "sql5804315";
    private static final String PASSWORD = "kNgqlGh3aL";


    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); // Carga explícita del driver
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver MySQL no encontrado: " + e.getMessage());
        }

        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

}
