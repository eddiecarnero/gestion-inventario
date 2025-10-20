package com.inventario.model;

public class Producto {
    private int id;
    private String nombre;
    private String descripcion;
    private double precio;
    private int stockActual;    // Agregado
    private int stockMinimo;    // Agregado

    // Constructor vacío
    public Producto() {}

    // Constructor completo
    public Producto(int id, String nombre, String descripcion, double precio, int stockActual, int stockMinimo) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.stockActual = stockActual;
        this.stockMinimo = stockMinimo;
    }

    // --- Getters y Setters ---

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }

    // Getters y Setters para STOCK (Los nuevos)
    public int getStockActual() { return stockActual; }
    public void setStockActual(int stockActual) { this.stockActual = stockActual; }
    public int getStockMinimo() { return stockMinimo; }
    public void setStockMinimo(int stockMinimo) { this.stockMinimo = stockMinimo; }
}