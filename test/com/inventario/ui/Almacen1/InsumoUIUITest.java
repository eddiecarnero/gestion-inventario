package com.inventario.ui.Almacen1;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

// Importamos la clase interna estática
import com.inventario.ui.almacen1.InsumoUI;

class InsumoUIUITest {

    @Test
    void detectarBajoStock() {
        // Stock 5, Mínimo 10 -> DEBE ser bajo
        InsumoUI insumo = new InsumoUI(1, "Harina", 5, 10, "kg", 5.0, "Prov", 1.0);

        assertTrue(insumo.esStockBajo(), "Debería detectar bajo stock");
        assertEquals("Bajo Stock", insumo.getEstado());
    }

    @Test
    void detectarStockMedio() {
        // Mínimo 10. Medio es hasta 15 (10 * 1.5). Probamos con 12.
        InsumoUI insumo = new InsumoUI(1, "Azúcar", 12, 10, "kg", 5.0, "Prov", 1.0);

        assertFalse(insumo.esStockBajo(), "No debería ser bajo stock");
        assertEquals("Medio", insumo.getEstado(), "Debería ser estado Medio");
    }

    @Test
    void detectarStockNormal() {
        // Mínimo 10. Normal es > 15. Probamos con 20.
        InsumoUI insumo = new InsumoUI(1, "Sal", 20, 10, "kg", 5.0, "Prov", 1.0);

        assertFalse(insumo.esStockBajo());
        assertEquals("Normal", insumo.getEstado());
    }
}