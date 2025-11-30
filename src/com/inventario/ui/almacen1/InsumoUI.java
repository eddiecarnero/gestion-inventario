package com.inventario.ui.almacen1;

public class InsumoUI {
    private final int id;
    private final String nombre;
    private final int stock;
    private final int stockMinimo;
    private final String unidad;
    private final double precioUnitario;
    private final String proveedorNombre;
    private final double contenido;

    public InsumoUI(int id, String nombre, int stock, int stockMinimo, String unidad,
                    double precioUnitario, String proveedorNombre, double contenido) {
        this.id = id;
        this.nombre = nombre;
        this.stock = stock;
        this.stockMinimo = stockMinimo;
        this.unidad = unidad;
        this.precioUnitario = precioUnitario;
        this.proveedorNombre = (proveedorNombre != null) ? proveedorNombre : "N/A";
        this.contenido = contenido;
    }

    // --- Getters ---
    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public int getStock() { return stock; }
    public int getStockMinimo() { return stockMinimo; }
    public String getUnidad() { return unidad; }
    public double getPrecioUnitario() { return precioUnitario; }
    public String getProveedorNombre() { return proveedorNombre; }
    public double getContenido() { return contenido; }

    // --- Lógica de Negocio (Alertas) ---

    public boolean esStockBajo() {
        return stock <= stockMinimo;
    }

    public String getEstado() {
        if (stock <= stockMinimo) return "Bajo Stock";
        if (stock <= stockMinimo * 1.5) return "Medio";
        return "Normal";
    }
}