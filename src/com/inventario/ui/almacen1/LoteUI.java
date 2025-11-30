package com.inventario.ui.almacen1;

import java.time.LocalDate;

public class LoteUI {
    private final int idLote;
    private final double cantidad;
    private final String fechaIngreso;
    private final String fechaVencimiento;
    private final String estado;

    public LoteUI(int idLote, double cantidad, String fechaIngreso, String fechaVencimiento) {
        this.idLote = idLote;
        this.cantidad = cantidad;
        this.fechaIngreso = fechaIngreso;
        this.fechaVencimiento = fechaVencimiento;
        this.estado = calcularEstado(fechaVencimiento);
    }

    // Lógica para determinar si está vencido
    private String calcularEstado(String vencimiento) {
        if (vencimiento == null || vencimiento.isEmpty()) {
            return "No Vence";
        }
        try {
            LocalDate fechaVenc = LocalDate.parse(vencimiento);
            LocalDate hoy = LocalDate.now();

            if (fechaVenc.isBefore(hoy)) {
                return "VENCIDO";
            } else if (fechaVenc.isBefore(hoy.plusDays(7))) { // Alerta 7 días antes
                return "Por Vencer";
            } else {
                return "Ok";
            }
        } catch (Exception e) {
            return "Error Fecha";
        }
    }

    // --- Getters ---
    public int getIdLote() { return idLote; }
    public double getCantidad() { return cantidad; }
    public String getFechaIngreso() { return fechaIngreso; }
    public String getFechaVencimiento() { return fechaVencimiento; }
    public String getEstado() { return estado; }
}