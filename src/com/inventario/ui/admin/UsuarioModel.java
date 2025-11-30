package com.inventario.ui.admin;

public class UsuarioModel {
    private int id;
    private String nombre;
    private String rol;
    private String telefono;
    private String dni;
    private String turno;
    private String horario;
    private String user;
    private String pass;

    public UsuarioModel(int id, String n, String r, String t, String d, String tu, String h, String u, String p) {
        this.id = id;
        this.nombre = n;
        this.rol = r;
        this.telefono = t;
        this.dni = d;
        this.turno = tu;
        this.horario = h;
        this.user = u;
        this.pass = p;
    }

    // --- Getters necesarios para la Tabla ---
    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getRol() { return rol; }
    public String getTelefono() { return telefono; }
    public String getDni() { return dni; }
    public String getTurno() { return turno; }
    public String getHorario() { return horario; }
    public String getUser() { return user; }
    public String getPass() { return pass; }
}