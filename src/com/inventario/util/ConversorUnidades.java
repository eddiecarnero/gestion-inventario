package com.inventario.util;

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

        // --- CONVERSIONES DE MASA (Kg <-> Gramos) ---

        // De Kg a Gramos (1Kg -> 1000g)
        if (esKilo(origen) && esGramo(destino)) {
            return cantidad * 1000;
        }

        // De Gramos a Kg (1000g -> 1Kg)
        if (esGramo(origen) && esKilo(destino)) {
            return cantidad / 1000;
        }

        if(esGramo(origen) && esMiligramo(destino)){
            return cantidad / 1000;
        }

        if(esKilo(origen) && esMiligramo(destino)){
            return cantidad / 1000000;
        }

        // Si no encuentra conversión, devuelve el valor original (1:1)
        return cantidad;
    }

    // Helpers para detectar variantes de nombres
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