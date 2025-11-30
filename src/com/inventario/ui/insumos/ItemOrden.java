package com.inventario.ui.insumos;

public class ItemOrden {
    private final int insumoId;
    private final String nombre;
    private final String unidad;
    private double cantidad;
    private final double precioUnitario;
    private final double total;
    private final double contenido;

    // Constructor que recibe el InsumoUI que acabamos de crear
    public ItemOrden(InsumoUI i, double cantidadIngresada) {
        this.insumoId = i.getId();
        this.nombre = i.getNombre();
        this.unidad = i.getUnidad();
        this.cantidad = cantidadIngresada;
        this.precioUnitario = i.getPrecioUnitario();
        this.contenido = i.getContenido();

        // Cálculo del precio total considerando el contenido del paquete
        this.total = (cantidadIngresada / this.contenido) * this.precioUnitario;
    }

    public int getInsumoId() { return insumoId; }
    public String getNombre() { return nombre; }
    public String getUnidad() { return unidad; }
    public double getCantidad() { return cantidad; }
    public double getPrecioUnitario() { return precioUnitario; }
    public double getTotal() { return total; }

    public void setCantidad(double c) {
        this.cantidad = c;
    }
}