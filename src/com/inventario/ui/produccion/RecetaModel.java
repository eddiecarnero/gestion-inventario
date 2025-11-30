package com.inventario.ui.produccion;

public class RecetaModel {
    private final int id;
    private final String nombre;
    private final String unidadProducida;
    private final String tipoDestino;
    private final double cantidadProducida;

    public RecetaModel(int id, String nombre, double cantidad, String unidad, String tipo) {
        this.id = id;
        this.nombre = nombre;
        this.cantidadProducida = cantidad;
        this.unidadProducida = unidad;
        this.tipoDestino = tipo;
    }

    // --- Getters ---
    public int getId() { return id; }

    public String getNombre() { return nombre; }

    public double getCantidadProducida() { return cantidadProducida; }

    public String getUnidadProducida() { return unidadProducida; }

    public String getTipoDestino() { return tipoDestino; }
}