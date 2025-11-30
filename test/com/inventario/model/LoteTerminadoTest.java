package com.inventario.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LoteTerminadoTest {

    @Test
    void constructorDebeAsignarValoresCorrectamente() {
        // 1. Datos de prueba
        int idEsperado = 101;
        int cantidadEsperada = 500;
        String fechaEsperada = "2025-12-31";

        // 2. Ejecución: Creamos el objeto
        LoteTerminado lote = new LoteTerminado(idEsperado, cantidadEsperada, fechaEsperada);

        // 3. Verificación: Comprobamos que cada getter devuelva lo correcto
        // Usamos assertAll para que si falla uno, nos avise de todos los fallos a la vez
        assertAll("Verificando propiedades del Lote",
                () -> assertEquals(idEsperado, lote.getIdLote(), "El ID del lote no coincide"),
                () -> assertEquals(cantidadEsperada, lote.getCantidad(), "La cantidad no coincide"),
                () -> assertEquals(fechaEsperada, lote.getVencimiento(), "El vencimiento no coincide")
        );
    }
}