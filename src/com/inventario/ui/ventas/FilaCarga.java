package com.inventario.ui.ventas;

public class FilaCarga {
    private final String nombreProducto;
    private final int cantidad;
    private final String fecha;
    private final String cliente;
    private String estado;
    private final Integer idProducto;

    public FilaCarga(String n, int c, String f, String cl, String e, Integer id) {
        this.nombreProducto = n;
        this.cantidad = c;
        this.fecha = f;
        this.cliente = cl;
        this.estado = e;
        this.idProducto = id;
    }

    // --- Getters y Setters ---

    public String getNombreProducto() {
        return nombreProducto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public String getFecha() {
        return fecha;
    }

    public String getCliente() {
        return cliente;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Integer getIdProducto() {
        return idProducto;
    }
}