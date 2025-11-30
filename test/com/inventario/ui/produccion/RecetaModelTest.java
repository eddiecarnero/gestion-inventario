package com.inventario.ui.produccion;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RecetaModelTest {
    @Test
    void debeGuardarDatosCorrectamente() {
        // Datos de prueba
        RecetaModel receta = new RecetaModel(1, "Masa de Pizza", 5.0, "Kg", "INTERMEDIO");

        assertAll("Verificando propiedades",
                () -> assertEquals(1, receta.getId()),
                () -> assertEquals("Masa de Pizza", receta.getNombre()),
                () -> assertEquals(5.0, receta.getCantidadProducida()),
                () -> assertEquals("Kg", receta.getUnidadProducida()),
                () -> assertEquals("INTERMEDIO", receta.getTipoDestino())
        );
    }
}