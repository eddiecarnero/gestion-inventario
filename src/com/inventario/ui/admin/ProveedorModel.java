package com.inventario.ui.admin;

public class ProveedorModel {
    private final int id;
    private final String nombre;
    private final String ruc;
    private final String tipo;
    private final String telefono;
    private final String email;
    private final String direccion;

    public ProveedorModel(int id, String n, String r, String t, String tel, String e, String d) {
        this.id = id;
        this.nombre = n;
        this.ruc = r;
        this.tipo = t;
        this.telefono = tel;
        this.email = e;
        this.direccion = d;
    }

    // --- Getters necesarios para la Tabla ---
    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getRuc() { return ruc; }
    public String getTipo() { return tipo; }
    public String getTelefono() { return telefono; }
    public String getEmail() { return email; }
    public String getDireccion() { return direccion; }
}