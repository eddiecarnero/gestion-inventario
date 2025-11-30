package com.inventario.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ProductoTerminadoTest {

    @Test
    void constructorYGettersDebenFuncionar() {
        // Probamos que los datos se guarden bien
        ProductoTerminado prod = new ProductoTerminado(1, "Helado Chocolate", 15.0, 10, 50);

        assertAll("Verificando datos básicos",
                () -> assertEquals(1, prod.getId()),
                () -> assertEquals("Helado Chocolate", prod.getNombre()),
                () -> assertEquals(15.0, prod.getPrecioVenta()),
                () -> assertEquals(10, prod.getStockMinimo()),
                () -> assertEquals(50, prod.getStock())
        );
    }

    @Test
    void getEstadoDebeDetectarBajoStock() {
        // CASO 1: Stock es MENOR que el mínimo (Alerta)
        // Minimo: 10, Stock: 5
        ProductoTerminado prodCritico = new ProductoTerminado(1, "Vainilla", 10.0, 10, 5);
        assertEquals("Bajo Stock", prodCritico.getEstado(), "Debería avisar bajo stock si es menor al mínimo");

        // CASO 2: Stock es IGUAL al mínimo (Alerta - Caso Borde)
        // Minimo: 10, Stock: 10
        ProductoTerminado prodLimite = new ProductoTerminado(2, "Fresa", 10.0, 10, 10);
        assertEquals("Bajo Stock", prodLimite.getEstado(), "Debería avisar bajo stock si es igual al mínimo");
    }

    @Test
    void getEstadoDebeSerNormalConStockSuficiente() {
        // CASO 3: Stock es MAYOR que el mínimo (Normal)
        // Minimo: 10, Stock: 11
        ProductoTerminado prodNormal = new ProductoTerminado(3, "Lucuma", 10.0, 10, 11);
        assertEquals("Normal", prodNormal.getEstado(), "Debería decir Normal si supera el mínimo");
    }

    @Test
    void toStringDebeDevolverNombre() {
        ProductoTerminado prod = new ProductoTerminado(1, "Oreo", 20.0, 5, 20);
        assertEquals("Oreo", prod.toString());
    }
}