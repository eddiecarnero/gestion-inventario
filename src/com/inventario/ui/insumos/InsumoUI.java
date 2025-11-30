package com.inventario.ui.insumos;

public class InsumoUI {
    private final int id;
    private final int idProv;
    private final String nombre;
    private final String unidad;
    private final double precio;
    private final double contenido;

    public InsumoUI(int i, int ip, String n, double p, String u, double cont) {
        this.id = i;
        this.idProv = ip;
        this.nombre = n;
        this.precio = p;
        this.unidad = u;
        this.contenido = (cont <= 0 ? 1 : cont);
    }

    public int getId() { return id; }
    public int getIdProveedor() { return idProv; }
    public String getNombre() { return nombre; }
    public double getPrecioUnitario() { return precio; }
    public String getUnidad() { return unidad; }
    public double getContenido() { return contenido; }

    @Override
    public String toString() {
        return nombre + " ($" + precio + ")";
    }
}