package com.inventario.ui.almacen2; // El paquete nuevo

public class RecetaProduccionUI { // Nombre cambiado para evitar conflictos
    private final int id, idIntermedio;
    private final String nombre, unidad;
    private final double cantidad;

    public RecetaProduccionUI(int id, String n, double c, String u, int idi) {
        this.id=id; this.nombre=n; this.cantidad=c; this.unidad=u; this.idIntermedio=idi;
    }

    // ... Getters y toString ...
    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public double getCantidadBase() { return cantidad; }
    public String getUnidad() { return unidad; }
    public int getIdProductoIntermedio() { return idIntermedio; }

    @Override public String toString() { return nombre; }
}