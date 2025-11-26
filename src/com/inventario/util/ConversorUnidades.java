package com.inventario.util;

import java.util.List;

public class ConversorUnidades {

    public static double convertir(double cantidad, String unidadOrigen, String unidadDestino) {
        // Si son iguales o nulos, no hacer nada
        if (unidadOrigen == null || unidadDestino == null || unidadOrigen.equalsIgnoreCase(unidadDestino)) {
            return cantidad;
        }

        String origen = unidadOrigen.toLowerCase().trim();
        String destino = unidadDestino.toLowerCase().trim();

        // --- CONVERSIONES DE VOLUMEN (Litros <-> ml) ---

        // De Litros a ml (1L -> 1000ml)
        if (esLitro(origen) && esMililitro(destino)) {
            return cantidad * 1000;
        }

        // De ml a Litros (1000ml -> 1L)
        if (esMililitro(origen) && esLitro(destino)) {
            return cantidad / 1000;
        }

        // --- CONVERSIONES DE MASA (Kg <-> Gramos <-> Miligramos) ---

        // 1. De Kg a ...
        if (esKilo(origen)) {
            if (esGramo(destino)) return cantidad * 1000;       // 1 Kg = 1000 g
            if (esMiligramo(destino)) return cantidad * 1000000; // 1 Kg = 1,000,000 mg
        }

        // 2. De Gramos a ...
        if (esGramo(origen)) {
            if (esKilo(destino)) return cantidad / 1000;        // 1000 g = 1 Kg
            if (esMiligramo(destino)) return cantidad * 1000;   // 1 g = 1000 mg (Corregido)
        }

        // 3. De Miligramos a ...
        if (esMiligramo(origen)) {
            if (esGramo(destino)) return cantidad / 1000;       // 1000 mg = 1 g
            if (esKilo(destino)) return cantidad / 1000000;     // 1,000,000 mg = 1 Kg
        }

        // Si no hay conversión compatible (ej. Litros a Kilos sin densidad), devuelve el original
        return cantidad;
    }

    /**
     * Método para filtrar qué unidades mostrar en los ComboBoxes.
     * Si el producto base es "Kg", solo permite seleccionar masas.
     * Si es "Litro", solo volúmenes.
     */
    public static List<String> obtenerUnidadesCompatibles(String unidadBase) {
        if (unidadBase == null) return List.of("Unidad");

        String u = unidadBase.toLowerCase().trim();

        if (esKilo(u) || esGramo(u) || esMiligramo(u)) {
            // Es MASA
            return List.of("Kg", "Gramo", "Miligramo");
        } else if (esLitro(u) || esMililitro(u)) {
            // Es VOLUMEN
            return List.of("Litro", "ml");
        } else {
            // Por defecto o UNIDAD
            return List.of("Unidad");
        }
    }

    // --- Helpers para detectar variantes de nombres ---
    private static boolean esLitro(String u) {
        return u.equals("litro") || u.equals("litros") || u.equals("l") || u.equals("lt");
    }
    private static boolean esMililitro(String u) {
        return u.equals("ml") || u.equals("mililitro") || u.equals("mililitros");
    }
    private static boolean esKilo(String u) {
        return u.equals("kg") || u.equals("kilo") || u.equals("kilogramo") || u.equals("kgs");
    }
    private static boolean esGramo(String u) {
        return u.equals("gramo") || u.equals("gramos") || u.equals("g") || u.equals("gr");
    }
    private static boolean esMiligramo(String u){
        return u.equals("miligramo") || u.equals("mg") || u.equals("mgs");
    }
}