package com.inventario.logic;

import org.junit.jupiter.api.*;
import java.sql.*;
import static org.junit.jupiter.api.Assertions.*;

class AuthServiceTest {

    private Connection connection;

    @BeforeEach
    public void setUp() throws Exception {
        // 1. Conectamos a una BD en memoria (rápida y limpia)
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");

        // 2. Creamos la tabla 'empleado'
        String sqlCrearTabla = "CREATE TABLE empleado (" +
                "id INTEGER PRIMARY KEY, " +
                "user TEXT, " +
                "password TEXT, " +
                "Nombre_y_Apellido TEXT, " +
                "Tipo_de_empleado TEXT" +
                ");";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sqlCrearTabla);

            // 3. Insertamos un usuario BASE de prueba (Fixtures)
            // Este usuario "juan123" nos sirve para probar que el login normal funciona
            stmt.execute("INSERT INTO empleado (user, password, Nombre_y_Apellido, Tipo_de_empleado) " +
                    "VALUES ('juan123', 'secreto', 'Juan Perez', 'Vendedor')");
        }
    }

    @AfterEach
    public void tearDown() throws Exception {
        if (connection != null) connection.close();
    }

    // ==========================================
    // 🧪 TESTS DE FUNCIONALIDAD BÁSICA (Legacy)
    // ==========================================

    @Test
    public void validarUsuarioDebeAceptarCredencialesCorrectas() {
        // Probamos el método simple booleano
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
        // Prueba el método obtenerTipoEmpleado para el caso especial 'admin'
        String tipo = AuthService.obtenerTipoEmpleado(connection, "admin");
        assertEquals("Administrador", tipo);
    }

    // ==========================================
    // 🛡️ TESTS DE SEGURIDAD Y LOGIN NUEVO
    // ==========================================

    @Test
    void adminTemporal_DebeEntrar_SiNoHayAdminReal() {
        // En el setUp solo insertamos un 'Vendedor', así que NO hay 'Administrador' real.
        // Por tanto, la puerta trasera (admin/1234) debería estar ABIERTA.

        AuthService.ResultadoLogin resultado = AuthService.intentarLogin(connection, "admin", "1234");

        assertEquals(AuthService.ResultadoLogin.EXITO, resultado,
                "El admin temporal debería permitirse si no hay administradores en BD");
    }

    @Test
    void adminTemporal_DebeBloquearse_SiExisteAdminReal() throws SQLException {
        // 1. Insertamos un Administrador real en la BD (simulando que ya creaste tu usuario)
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("INSERT INTO empleado (user, password, Tipo_de_empleado) VALUES ('jefe', 'pass', 'Administrador')");
        }

        // 2. Intentamos entrar con el admin temporal
        AuthService.ResultadoLogin resultado = AuthService.intentarLogin(connection, "admin", "1234");

        // 3. Debería estar BLOQUEADO
        assertEquals(AuthService.ResultadoLogin.BLOQUEADO_POR_ADMIN_EXISTENTE, resultado,
                "El admin temporal debe bloquearse por seguridad si ya existe un admin real");
    }

    @Test
    void usuarioNormal_DebeEntrar_ConCredencialesCorrectas() {
        // Probamos el login nuevo con el usuario 'juan123' que creamos en el setUp
        AuthService.ResultadoLogin resultado = AuthService.intentarLogin(connection, "juan123", "secreto");

        assertEquals(AuthService.ResultadoLogin.EXITO, resultado);
    }

    @Test
    void usuarioNormal_DebeFallar_ConPasswordIncorrecto() {
        AuthService.ResultadoLogin resultado = AuthService.intentarLogin(connection, "juan123", "badpass");

        assertEquals(AuthService.ResultadoLogin.CREDENCIALES_INCORRECTAS, resultado);
    }
}