package com.inventario.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionBD {

    // ✅ Datos de la BD en la nube
    private static final String URL_CLOUD =
            "jdbc:mysql://sql5.freesqldatabase.com:3306/sql5804315?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USER_CLOUD = "sql5804315";
    private static final String PASS_CLOUD = "kNgqlGh3aL";

    // ✅ Datos de la BD local (MODIFICA AQUÍ SI TU PUERTO / DB SON DIFERENTES)
    private static final String URL_LOCAL =
            "jdbc:mysql://localhost:3306/sql5804315?useSSL=false&serverTimezone=UTC";
    private static final String USER_LOCAL = "root";
    private static final String PASS_LOCAL = "root";

    // ✅ MÉTODO PRINCIPAL → intenta nube y si falla usa local
    public static Connection getConnection() throws SQLException {

        // Cargar driver
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver MySQL no encontrado: " + e.getMessage());
        }

        // 1️⃣ Intentar conectar a la nube
        try {
            System.out.println("Intentando conectar a la base de datos en la NUBE...");
            return DriverManager.getConnection(URL_CLOUD, USER_CLOUD, PASS_CLOUD);
        } catch (SQLException e) {
            System.out.println("⚠️ ERROR al conectar a la nube: " + e.getMessage());
            System.out.println("Intentando conectar a la base de datos LOCAL...");
        }

        // 2️⃣ Si la nube falla → probar local
        try {
            return DriverManager.getConnection(URL_LOCAL, USER_LOCAL, PASS_LOCAL);
        } catch (SQLException e) {
            System.out.println("❌ ERROR al conectar a la base LOCAL: " + e.getMessage());
            throw new SQLException("No se pudo conectar ni a la nube ni a la base local.");
        }
    }
}
