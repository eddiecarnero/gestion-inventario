package com.inventario.ui.insumos;

import javafx.beans.property.*;

public class FilaCreacion {
    private final StringProperty nombre = new SimpleStringProperty();
    private final StringProperty proveedor = new SimpleStringProperty();
    private final ObjectProperty<Double> precio = new SimpleObjectProperty<>();
    private final ObjectProperty<Double> contenido = new SimpleObjectProperty<>();
    private final StringProperty unidad = new SimpleStringProperty();
    private final ObjectProperty<Integer> stockMin = new SimpleObjectProperty<>();
    private final StringProperty ubicacion = new SimpleStringProperty();
    private final StringProperty estado = new SimpleStringProperty();

    public FilaCreacion(String n, String p, Double pr, Double c, String u, Integer min, String ub) {
        setNombre(n);
        setProveedor(p);
        setPrecio(pr);
        setContenido(c);
        setUnidad(u);
        setStockMin(min);
        setUbicacion(ub);
        setEstado("Pendiente");
    }

    // --- Properties y Getters (Necesarios para TableView editable) ---

    public String getNombre() { return nombre.get(); }
    public void setNombre(String v) { nombre.set(v); }
    public StringProperty nombreProperty() { return nombre; }

    public String getProveedor() { return proveedor.get(); }
    public void setProveedor(String v) { proveedor.set(v); }
    public StringProperty proveedorProperty() { return proveedor; }

    public Double getPrecio() { return precio.get(); }
    public void setPrecio(Double v) { precio.set(v); }
    public ObjectProperty<Double> precioProperty() { return precio; }

    public Double getContenido() { return contenido.get(); }
    public void setContenido(Double v) { contenido.set(v); }
    public ObjectProperty<Double> contenidoProperty() { return contenido; }

    public String getUnidad() { return unidad.get(); }
    public void setUnidad(String v) { unidad.set(v); }
    public StringProperty unidadProperty() { return unidad; }

    public Integer getStockMin() { return stockMin.get(); }
    public void setStockMin(Integer v) { stockMin.set(v); }
    public ObjectProperty<Integer> stockMinProperty() { return stockMin; }

    public String getUbicacion() { return ubicacion.get(); }
    public void setUbicacion(String v) { ubicacion.set(v); }
    public StringProperty ubicacionProperty() { return ubicacion; }

    public String getEstado() { return estado.get(); }
    public void setEstado(String v) { estado.set(v); }
    public StringProperty estadoProperty() { return estado; }
}