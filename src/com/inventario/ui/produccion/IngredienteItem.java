package com.inventario.ui.produccion;

public class IngredienteItem {
    private final int idReferencia;
    private final String nombre;
    private final String unidad;
    private final String tipoOrigen; // "INSUMO" o "INTERMEDIO"
    private final double cantidad;

    public IngredienteItem(int idRef, String nombre, double cantidad, String unidad, String tipoOrigen) {
        this.idReferencia = idRef;
        this.nombre = nombre;
        this.cantidad = cantidad;
        this.unidad = unidad;
        this.tipoOrigen = tipoOrigen;
    }

    // --- Getters ---
    public int getIdReferencia() { return idReferencia; }

    public String getNombre() { return nombre; }

    public double getCantidad() { return cantidad; }

    public String getUnidad() { return unidad; }

    public String getTipoOrigen() { return tipoOrigen; }
}