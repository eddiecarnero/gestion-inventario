package com.inventario.test;

import com.inventario.config.ConexionBD;
import java.sql.*;

public class ListarTablas {
    public static void main(String[] args) {
        String nombreTabla = "producto"; // 🔹 Cambia este valor por la tabla que quieras inspeccionar

        try (Connection conn = ConexionBD.getConnection()) {
            System.out.println("✅ Conectado a la base: " + conn.getCatalog());
            System.out.println("📂 Tabla seleccionada: " + nombreTabla);

            DatabaseMetaData meta = conn.getMetaData();
            ResultSet rs = meta.getColumns(null, null, nombreTabla, null);

            System.out.println("\n📋 Columnas encontradas:");
            while (rs.next()) {
                String columna = rs.getString("COLUMN_NAME");
                String tipo = rs.getString("TYPE_NAME");
                int tamaño = rs.getInt("COLUMN_SIZE");
                String nulo = rs.getString("IS_NULLABLE");
                System.out.printf(" - %s (%s, tamaño: %d, nullable: %s)%n", columna, tipo, tamaño, nulo);
            }

        } catch (SQLException e) {
            System.err.println("❌ Error al listar columnas: " + e.getMessage());
        }
    }
}
