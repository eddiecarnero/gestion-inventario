package com.inventario.model;

// Importamos java.sql.Date para manejar la columna DATE de la base de datos
import java.sql.Date;

public class Producto {

    // --- Atributos ---
    // Estos nombres (en camelCase) deben coincidir con los getters/setters
    // que usamos en el DAO, aunque las columnas en la BD tengan mayúsculas o guiones bajos.

    private int idProducto;         // Mapea a 'IdProducto'
    private String tipoDeProducto;  // Mapea a 'Tipo_de_Producto'
    private int stock;              // Mapea a 'Stock'
    private int stockMinimo;        // Mapea a 'Stock_Minimo'
    private int stockMaximo;        // Mapea a 'Stock_Maximo'
    private String estadoStock;     // Mapea a 'Estado_Stock'
    private Date fechaDeCaducidad;  // Mapea a 'Fecha_de_caducidad'
    private String unidadDeMedida;  // Mapea a 'Unidad_de_medida'
    private String ubicacion;       // Mapea a 'Ubicacion'

    // --- Constructores ---

    // Constructor vacío (necesario para el DAO al leer datos)
    public Producto() {}

    // Constructor completo (útil para crear nuevos productos)
    public Producto(int idProducto, String tipoDeProducto, int stock, int stockMinimo,
                    int stockMaximo, String estadoStock, Date fechaDeCaducidad,
                    String unidadDeMedida, String ubicacion) {
        this.idProducto = idProducto;
        this.tipoDeProducto = tipoDeProducto;
        this.stock = stock;
        this.stockMinimo = stockMinimo;
        this.stockMaximo = stockMaximo;
        this.estadoStock = estadoStock;
        this.fechaDeCaducidad = fechaDeCaducidad;
        this.unidadDeMedida = unidadDeMedida;
        this.ubicacion = ubicacion;
    }

    // --- Getters y Setters ---
    // (Generados para TODOS los atributos)

    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    public String getTipoDeProducto() {
        return tipoDeProducto;
    }

    public void setTipoDeProducto(String tipoDeProducto) {
        this.tipoDeProducto = tipoDeProducto;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public int getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(int stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public int getStockMaximo() {
        return stockMaximo;
    }

    public void setStockMaximo(int stockMaximo) {
        this.stockMaximo = stockMaximo;
    }

    public String getEstadoStock() {
        return estadoStock;
    }

    public void setEstadoStock(String estadoStock) {
        this.estadoStock = estadoStock;
    }

    public Date getFechaDeCaducidad() {
        return fechaDeCaducidad;
    }

    public void setFechaDeCaducidad(Date fechaDeCaducidad) {
        this.fechaDeCaducidad = fechaDeCaducidad;
    }

    public String getUnidadDeMedida() {
        return unidadDeMedida;
    }

    public void setUnidadDeMedida(String unidadDeMedida) {
        this.unidadDeMedida = unidadDeMedida;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }
}