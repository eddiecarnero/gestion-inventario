package com.inventario.model;

public class LoteTerminado {
    private final int idLote;
    private final int cantidad;
    private final String vencimiento;

    public LoteTerminado(int idLote, int cantidad, String vencimiento) {
        this.idLote = idLote;
        this.cantidad = cantidad;
        this.vencimiento = vencimiento;
    }

    public int getIdLote() {
        return idLote;
    }

    public int getCantidad() {
        return cantidad;
    }

    public String getVencimiento() {
        return vencimiento;
    }
}
