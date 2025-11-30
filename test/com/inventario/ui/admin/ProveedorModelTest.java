package com.inventario.ui.admin;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ProveedorModelTest {
    @Test
    void constructorYGettersFuncionan() {
        ProveedorModel p = new ProveedorModel(5, "Coca Cola", "20100000001", "Bebidas", "999888777", "contacto@coca.com", "Av. Industrial");

        assertAll("Verificar datos de proveedor",
                () -> assertEquals(5, p.getId()),
                () -> assertEquals("Coca Cola", p.getNombre()),
                () -> assertEquals("20100000001", p.getRuc()),
                () -> assertEquals("Bebidas", p.getTipo())
        );
    }
}