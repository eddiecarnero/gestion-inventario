package com.inventario.model;

import java.sql.Date;

public class Producto {

    // --- Atributos ---
    private int idProducto;
    private String tipoDeProducto;
    private int stock;
    private int stockMinimo;
    private int stockMaximo;
    private String estadoStock;
    private Date fechaDeCaducidad;
    private String unidadDeMedida;
    private String ubicacion;

    // Nuevos campos para la lógica de compra vs inventario
    private double precioUnitario; // Precio de COMPRA (por lata, por saco, etc.)
    private int idProveedor;
    private double contenido;      // Factor de conversión (ej. 400 para 400ml, 1000 para 1kg)
    private String proveedorNombre; // Para mostrar en tablas (no se guarda en tabla producto, es visual)

    // --- Constructores ---

    // Constructor vacío (necesario para el DAO)
    public Producto() {}

    // Constructor completo (Actualizado con todos los campos)
    public Producto(int idProducto, String tipoDeProducto, int stock, int stockMinimo,
                    int stockMaximo, String estadoStock, Date fechaDeCaducidad,
                    String unidadDeMedida, String ubicacion, double precioUnitario,
                    int idProveedor, double contenido) {
        this.idProducto = idProducto;
        this.tipoDeProducto = tipoDeProducto;
        this.stock = stock;
        this.stockMinimo = stockMinimo;
        this.stockMaximo = stockMaximo;
        this.estadoStock = estadoStock;
        this.fechaDeCaducidad = fechaDeCaducidad;
        this.unidadDeMedida = unidadDeMedida;
        this.ubicacion = ubicacion;
        this.precioUnitario = precioUnitario;
        this.idProveedor = idProveedor;
        this.contenido = contenido;
    }

    // --- Getters y Setters ---

    public int getIdProducto() { return idProducto; }
    public void setIdProducto(int idProducto) { this.idProducto = idProducto; }

    public String getTipoDeProducto() { return tipoDeProducto; }
    public void setTipoDeProducto(String tipoDeProducto) { this.tipoDeProducto = tipoDeProducto; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public int getStockMinimo() { return stockMinimo; }
    public void setStockMinimo(int stockMinimo) { this.stockMinimo = stockMinimo; }

    public int getStockMaximo() { return stockMaximo; }
    public void setStockMaximo(int stockMaximo) { this.stockMaximo = stockMaximo; }

    public String getEstadoStock() { return estadoStock; }
    public void setEstadoStock(String estadoStock) { this.estadoStock = estadoStock; }

    public Date getFechaDeCaducidad() { return fechaDeCaducidad; }
    public void setFechaDeCaducidad(Date fechaDeCaducidad) { this.fechaDeCaducidad = fechaDeCaducidad; }

    public String getUnidadDeMedida() { return unidadDeMedida; }
    public void setUnidadDeMedida(String unidadDeMedida) { this.unidadDeMedida = unidadDeMedida; }

    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }

    // --- Getters y Setters para los Nuevos Campos ---

    public double getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(double precioUnitario) { this.precioUnitario = precioUnitario; }

    public int getIdProveedor() { return idProveedor; }
    public void setIdProveedor(int idProveedor) { this.idProveedor = idProveedor; }

    public double getContenido() { return contenido; }
    public void setContenido(double contenido) {
        // Validación simple para evitar dividir por cero en cálculos futuros
        if (contenido <= 0) {
            this.contenido = 1.0;
        } else {
            this.contenido = contenido;
        }
    }

    public String getProveedorNombre() { return proveedorNombre; }
    public void setProveedorNombre(String proveedorNombre) { this.proveedorNombre = proveedorNombre; }

    @Override
    public String toString() {
        return tipoDeProducto; // Para que se vea bien en ComboBoxes si se usa el objeto directo
    }
}