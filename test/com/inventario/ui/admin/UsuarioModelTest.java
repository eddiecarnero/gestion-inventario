package com.inventario.ui.admin;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UsuarioModelTest {
    @Test
    void constructorYGettersFuncionan() {
        UsuarioModel u = new UsuarioModel(1, "Juan Perez", "Vendedor", "555-0101", "12345678", "Mañana", "8-4", "juanp", "pass123");

        assertAll("Verificar datos de usuario",
                () -> assertEquals(1, u.getId()),
                () -> assertEquals("Juan Perez", u.getNombre()),
                () -> assertEquals("Vendedor", u.getRol()),
                () -> assertEquals("juanp", u.getUser())
        );
    }
}