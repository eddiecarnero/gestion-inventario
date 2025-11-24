package com.inventario.model;

public class ProductoTerminado {
    private final int id;
    private final int stockMin;
    private final int stock;
    private final String nombre;
    private final double precioVenta;

    public ProductoTerminado(int id, String nombre, double precioVenta, int stockMin, int stock) {
        this.id = id;
        this.nombre = nombre;
        this.precioVenta = precioVenta;
        this.stockMin = stockMin;
        this.stock = stock;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public int getStock() {
        return stock;
    }

    public double getPrecioVenta() {
        return precioVenta;
    }



    public int getStockMinimo() {
        return stockMin;
    }

    public String getEstado() {
        return stock <= stockMin ? "Bajo Stock" : "Normal";
    }

    @Override
    public String toString() {
        return nombre;
    }
}
