package com.inventario.model;

public class RecetaSimple {
    private final int id;
    private final String nombre;
    private final String unidad;
    private final double cantidadBase;

    public RecetaSimple(int id, String nombre, double cantidadBase, String unidad) {
        this.id = id;
        this.nombre = nombre;
        this.cantidadBase = cantidadBase;
        this.unidad = unidad;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getUnidad() {
        return unidad;
    }

    public double getCantidadBase() {
        return cantidadBase;
    }
}
