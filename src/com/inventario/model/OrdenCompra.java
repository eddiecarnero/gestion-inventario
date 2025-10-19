package com.inventario.model;
import java.util.Date;

public class OrdenCompra {
    private int idCompra;
    private int idProveedor;
    private int idEmpleado;
    private String tipoDeCompra;
    private double precioTotal;
    private Date fechaDeCompra;
    private Date fechaDeEntrega;
    private String observacion;

    public OrdenCompra() {}

    public OrdenCompra(int idCompra, int idProveedor, int idEmpleado, String tipoDeCompra,
                       double precioTotal, Date fechaDeCompra, Date fechaDeEntrega, String observacion) {
        this.idCompra = idCompra;
        this.idProveedor = idProveedor;
        this.idEmpleado = idEmpleado;
        this.tipoDeCompra = tipoDeCompra;
        this.precioTotal = precioTotal;
        this.fechaDeCompra = fechaDeCompra;
        this.fechaDeEntrega = fechaDeEntrega;
        this.observacion = observacion;
    }

    public int getIdCompra() { return idCompra; }
    public void setIdCompra(int idCompra) { this.idCompra = idCompra; }

    public int getIdProveedor() { return idProveedor; }
    public void setIdProveedor(int idProveedor) { this.idProveedor = idProveedor; }

    public int getIdEmpleado() { return idEmpleado; }
    public void setIdEmpleado(int idEmpleado) { this.idEmpleado = idEmpleado; }

    public String getTipoDeCompra() { return tipoDeCompra; }
    public void setTipoDeCompra(String tipoDeCompra) { this.tipoDeCompra = tipoDeCompra; }

    public double getPrecioTotal() { return precioTotal; }
    public void setPrecioTotal(double precioTotal) { this.precioTotal = precioTotal; }

    public Date getFechaDeCompra() { return fechaDeCompra; }
    public void setFechaDeCompra(Date fechaDeCompra) { this.fechaDeCompra = fechaDeCompra; }

    public Date getFechaDeEntrega() { return fechaDeEntrega; }
    public void setFechaDeEntrega(Date fechaDeEntrega) { this.fechaDeEntrega = fechaDeEntrega; }

    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }

}
