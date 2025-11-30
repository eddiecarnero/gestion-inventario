package com.inventario.config;

import com.inventario.config.AuthService;
import org.junit.jupiter.api.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import static org.junit.jupiter.api.Assertions.*;

public class AuthServiceTest {

    private Connection connection;

    @BeforeEach
    public void setUp() throws Exception {
        // 1. Conectamos a una BD en memoria (rápida y limpia)
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");

        // 2. Creamos la tabla 'empleado' tal como la espera tu código
        String sqlCrearTabla = "CREATE TABLE empleado (" +
                "id INTEGER PRIMARY KEY, " +
                "user TEXT, " +
                "password TEXT, " +
                "Nombre_y_Apellido TEXT, " +
                "Tipo_de_empleado TEXT" +
                ");";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sqlCrearTabla);

            // 3. Insertamos un usuario de prueba (Fixtures)
            stmt.execute("INSERT INTO empleado (user, password, Nombre_y_Apellido, Tipo_de_empleado) " +
                    "VALUES ('juan123', 'secreto', 'Juan Perez', 'Vendedor')");
        }
    }

    @AfterEach
    public void tearDown() throws Exception {
        // Cerramos la conexión para borrar la BD de memoria
        if (connection != null) connection.close();
    }

    @Test
    public void validarUsuarioDebeAceptarCredencialesCorrectas() {
        // Usamos el método sobrecargado pasando nuestra conexión de prueba
        boolean resultado = AuthService.validarUsuario(connection, "juan123", "secreto");
        assertTrue(resultado, "El usuario juan123 con pass secreto debería ser válido");
    }

    @Test
    public void validarUsuarioDebeRechazarCredencialesIncorrectas() {
        boolean resultado = AuthService.validarUsuario(connection, "juan123", "incorrecto");
        assertFalse(resultado, "El login no debe pasar con contraseña incorrecta");
    }

    @Test
    public void obtenerNombreDebeDevolverNombreBD() {
        String nombre = AuthService.obtenerNombre(connection, "juan123");
        assertEquals("Juan Perez", nombre);
    }

    @Test
    public void hardcodeAdminDebeFuncionarSinBD() {
        // Probamos el caso especial del admin hardcodeado
        // Nota: aquí podemos pasar null o connection, porque el código tiene un 'if' antes de usar la BD
        String tipo = AuthService.obtenerTipoEmpleado(connection, "admin");
        assertEquals("Administrador", tipo);
    }
}