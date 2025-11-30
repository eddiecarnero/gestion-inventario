package com.inventario.config;

import org.junit.jupiter.api.Test;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

class ConexionBDTest {

    @Test
    void debeConectarCorrectamente() throws SQLException {
        // Probamos conectando a la memoria RAM para no crear archivos
        try (Connection conn = ConexionBD.getConnection("jdbc:sqlite::memory:")) {

            // 1. Verificar que no es nula
            assertNotNull(conn, "La conexión no debería ser nula");

            // 2. Verificar que está abierta
            assertFalse(conn.isClosed(), "La conexión debería estar abierta");

            // 3. Verificar que es SQLite
            String nombreBD = conn.getMetaData().getDatabaseProductName();
            assertEquals("SQLite", nombreBD, "Debería estar conectado a SQLite");
        }
    }

    @Test
    void debeActivarForeignKeys() throws SQLException {
        // Verificar que tu código realmente activa las llaves foráneas (PRAGMA)
        try (Connection conn = ConexionBD.getConnection("jdbc:sqlite::memory:");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("PRAGMA foreign_keys;")) {

            if (rs.next()) {
                boolean activas = rs.getBoolean(1); // 1 = true, 0 = false
                assertTrue(activas, "Las llaves foráneas deberían estar activadas (ON)");
            } else {
                fail("No se pudo verificar el estado de foreign_keys");
            }
        }
    }
}