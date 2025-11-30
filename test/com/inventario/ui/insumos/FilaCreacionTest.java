package com.inventario.ui.insumos;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FilaCreacionTest {

    @Test
    void debeIniciarEnEstadoPendiente() {
        // Al crear una fila nueva para subir insumo
        FilaCreacion fila = new FilaCreacion("Harina", "Alicorp", 5.0, 1.0, "Kg", 10, "A1");

        // Por defecto debe estar pendiente de revisión
        assertEquals("Pendiente", fila.getEstado());
        assertEquals("Harina", fila.getNombre());
    }

    @Test
    void propiedadesDebenSerReactivas() {
        // Este test confirma que la tabla de JavaFX podrá detectar cambios
        FilaCreacion fila = new FilaCreacion("Azucar", "Prov", 2.0, 1.0, "Kg", 5, "B1");

        // Simulamos que el usuario edita la celda
        fila.setNombre("Azucar Rubia");
        fila.setPrecio(2.50);

        assertEquals("Azucar Rubia", fila.getNombre());
        assertEquals(2.50, fila.getPrecio());

        // Verificamos que no sea null la propiedad (importante para JavaFX)
        assertNotNull(fila.nombreProperty());
        assertNotNull(fila.precioProperty());
    }
}