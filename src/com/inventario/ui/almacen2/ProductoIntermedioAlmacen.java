package com.inventario.ui.almacen2;

public class ProductoIntermedioAlmacen {

    private final int id;
    private final int stock;
    private final int stockMin;
    private final String nombre;
    private final String unidad;
    private final double costo;

    // Constructor
    public ProductoIntermedioAlmacen(int id, String n, int s, int sm, String u, double c) {
        this.id = id;
        this.nombre = n;
        this.stock = s;
        this.stockMin = sm;
        this.unidad = u;
        this.costo = c;
    }

    // --- GETTERS (Estos son los que te faltaban o estaban ocultos) ---

    public int getId() { return id; }

    public String getNombre() { return nombre; }  // <-- ¡Este es el que te daba error!

    public int getStock() { return stock; }

    public int getStockMinimo() { return stockMin; }

    public String getUnidad() { return unidad; }

    public double getCostoUnitario() { return costo; }

    // --- LÓGICA DE NEGOCIO ---

    public boolean esStockBajo() {
        return stock <= stockMin;
    }

    public String getEstado() {
        return stock <= stockMin ? "Bajo Stock" : "Normal";
    }
}