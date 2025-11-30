package com.inventario.ui.produccion;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class IngredienteItemTest {
    @Test
    void debeGuardarDatosDeIngrediente() {
        // ID Ref 100, Harina, 2.5 kg, es un INSUMO
        IngredienteItem item = new IngredienteItem(100, "Harina", 2.5, "kg", "INSUMO");

        assertAll("Verificando ingrediente",
                () -> assertEquals(100, item.getIdReferencia()),
                () -> assertEquals("Harina", item.getNombre()),
                () -> assertEquals(2.5, item.getCantidad()),
                () -> assertEquals("INSUMO", item.getTipoOrigen())
        );
    }
}