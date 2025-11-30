package com.inventario.ui.insumos;

import java.time.LocalDate;

public class ItemRecepcion {
    private final int idProducto;
    private final String nombreProducto;
    private final double cantidad;
    private LocalDate fechaVencimiento;

    public ItemRecepcion(int id, String nom, double cant) {
        this.idProducto = id;
        this.nombreProducto = nom;
        this.cantidad = cant;
        this.fechaVencimiento = LocalDate.now().plusDays(30); // Default
    }

    public int getIdProducto() { return idProducto; }
    public String getNombreProducto() { return nombreProducto; }
    public double getCantidad() { return cantidad; }
    public LocalDate getFechaVencimiento() { return fechaVencimiento; }

    public void setFechaVencimiento(LocalDate f) { this.fechaVencimiento = f; }
}