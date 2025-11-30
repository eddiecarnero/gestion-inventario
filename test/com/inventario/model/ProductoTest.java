package com.inventario.model;

import org.junit.jupiter.api.Test;
import java.sql.Date;
import static org.junit.jupiter.api.Assertions.*;

class ProductoTest {

    @Test
    void constructorCompletoDebeAsignarTodosLosValores() {
        // 1. Datos de prueba
        Date fecha = Date.valueOf("2025-12-31");

        // 2. Ejecución
        Producto prod = new Producto(
                1, "Leche Entera", 100, 10, 500, "Disponible",
                fecha, "Litros", "Estante A", 25.50, 5, 1000.0
        );

        // 3. Verificación masiva
        assertAll("Propiedades del Producto",
                () -> assertEquals(1, prod.getIdProducto()),
                () -> assertEquals("Leche Entera", prod.getTipoDeProducto()),
                () -> assertEquals(100, prod.getStock()),
                () -> assertEquals(25.50, prod.getPrecioUnitario()),
                () -> assertEquals(1000.0, prod.getContenido())
        );
    }

    @Test
    void setContenidoDebeEvitarValoresCeroONegativos() {
        Producto prod = new Producto();

        // Caso 1: Intentamos poner 0
        prod.setContenido(0);
        assertEquals(1.0, prod.getContenido(), "El contenido 0 debería cambiarse a 1.0 automáticamente");

        // Caso 2: Intentamos poner negativo
        prod.setContenido(-50.5);
        assertEquals(1.0, prod.getContenido(), "El contenido negativo debería cambiarse a 1.0");

        // Caso 3: Valor correcto
        prod.setContenido(500.0);
        assertEquals(500.0, prod.getContenido(), "Un contenido válido debe guardarse tal cual");
    }

    @Test
    void toStringDebeDevolverSoloElNombre() {
        // Esto es importante si usas este objeto dentro de un ComboBox en la UI
        Producto prod = new Producto();
        prod.setTipoDeProducto("Azúcar Blanca");

        assertEquals("Azúcar Blanca", prod.toString());
    }

    @Test
    void settersYGettersDebenFuncionar() {
        // Prueba rápida para verificar que los setters básicos funcionan
        Producto prod = new Producto();
        prod.setStockMinimo(5);
        prod.setUbicacion("Bodega 1");

        assertEquals(5, prod.getStockMinimo());
        assertEquals("Bodega 1", prod.getUbicacion());
    }
}