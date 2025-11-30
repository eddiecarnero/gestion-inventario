package com.inventario.ui.almacen2;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

// Asegúrate de importar la clase correcta tras extraerla


class Almacen2LogicTest {

    @Test
    void debeDetectarStockCritico() {
        // Stock 5, Mínimo 10 -> Alerta Roja
        ProductoIntermedioAlmacen prod = new ProductoIntermedioAlmacen(1, "Masa Pizza", 5, 10, "kg", 2.50);

        assertTrue(prod.esStockBajo(), "Debe marcar stock bajo");
        assertEquals("Bajo Stock", prod.getEstado());
    }

    @Test
    void debeDetectarStockNormal() {
        // Stock 20, Mínimo 10 -> Todo bien
        ProductoIntermedioAlmacen prod = new ProductoIntermedioAlmacen(2, "Salsa Tomate", 20, 10, "lt", 5.00);

        assertFalse(prod.esStockBajo());
        assertEquals("Normal", prod.getEstado());
    }
}