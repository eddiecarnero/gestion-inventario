package com.inventario.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RecetaSimpleTest {

    @Test
    void constructorDebeAsignarTodosLosCampos() {
        // 1. Datos de prueba
        int id = 10;
        String nombre = "Masa de Hojaldre";
        double cantidad = 1500.50; // Probamos con decimales
        String unidad = "gramos";

        // 2. Ejecución
        RecetaSimple receta = new RecetaSimple(id, nombre, cantidad, unidad);

        // 3. Verificación
        // Usamos assertAll para revisar todo de un golpe
        assertAll("Verificando integridad de la Receta",
                () -> assertEquals(id, receta.getId(), "El ID no coincide"),
                () -> assertEquals(nombre, receta.getNombre(), "El nombre no coincide"),
                () -> assertEquals(cantidad, receta.getCantidadBase(), "La cantidad base no coincide"),
                () -> assertEquals(unidad, receta.getUnidad(), "La unidad de medida no coincide")
        );
    }

    @Test
    void debePermitirValoresCeroONegativos() {
        // NOTA: Como tu clase NO tiene validaciones (if cantidad < 0),
        // el comportamiento correcto es que ACEPTE estos valores.
        // Este test confirma que la clase es un simple contenedor de datos.

        RecetaSimple recetaRara = new RecetaSimple(-1, "Error", -100.0, "kg");

        assertEquals(-1, recetaRara.getId());
        assertEquals(-100.0, recetaRara.getCantidadBase());
    }
}