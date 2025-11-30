package com.inventario.util;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ConversorUnidadesTest {

    // --- PRUEBAS DE CONVERSIÓN ---

    @Test
    void debeMantenerValorSiUnidadesSonIguales() {
        assertEquals(5.0, ConversorUnidades.convertir(5.0, "Kg", "Kg"));
        assertEquals(10.0, ConversorUnidades.convertir(10.0, "Litro", "litro")); // Ignora mayúsculas
    }

    @Test
    void debeConvertirMasasCorrectamente() {
        // Kilos a Gramos
        assertEquals(1000.0, ConversorUnidades.convertir(1.0, "Kg", "Gramo"));

        // Gramos a Kilos
        assertEquals(0.5, ConversorUnidades.convertir(500.0, "gr", "kg")); // "gr" variante

        // Gramos a Miligramos
        assertEquals(5000.0, ConversorUnidades.convertir(5.0, "g", "mg"));
    }

    @Test
    void debeConvertirVolumenesCorrectamente() {
        // Litros a Mililitros
        assertEquals(1500.0, ConversorUnidades.convertir(1.5, "Lt", "ml"));

        // Mililitros a Litros
        assertEquals(2.0, ConversorUnidades.convertir(2000.0, "mililitros", "litros"));
    }

    @Test
    void debeManejarVariantesDeTexto() {
        // Prueba espacios y mayúsculas
        assertEquals(1000.0, ConversorUnidades.convertir(1.0, "  kg  ", "GRAMOS"));
    }

    @Test
    void debeDevolverOriginalSiNoSonCompatibles() {
        // Intentar convertir Masa a Volumen sin densidad (no soportado)
        double valorOriginal = 10.0;
        double resultado = ConversorUnidades.convertir(valorOriginal, "Kg", "Litro");

        assertEquals(valorOriginal, resultado, "Si no hay conversión, debe devolver el valor original");
    }

    // --- PRUEBAS DE LISTAS COMPATIBLES (Para la UI) ---

    @Test
    void debeSugerirUnidadesDeMasa() {
        List<String> lista = ConversorUnidades.obtenerUnidadesCompatibles("Kg");
        assertTrue(lista.contains("Gramo"));
        assertTrue(lista.contains("Miligramo"));
        assertFalse(lista.contains("Litro"));
    }

    @Test
    void debeSugerirUnidadesDeVolumen() {
        List<String> lista = ConversorUnidades.obtenerUnidadesCompatibles("ml");
        assertTrue(lista.contains("Litro"));
        assertFalse(lista.contains("Kg"));
    }
}