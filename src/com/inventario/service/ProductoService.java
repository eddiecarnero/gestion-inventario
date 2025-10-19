package com.inventario.service;

import com.inventario.dao.ProductoDAO;
import com.inventario.model.Producto;

import java.util.List;

public class ProductoService {
    private ProductoDAO productoDAO;

    public ProductoService() {
        this.productoDAO = new ProductoDAO();
    }

    // 🔴 1. MÉTODO FALTANTE: agregarNuevoProducto
    public boolean agregarNuevoProducto(Producto producto) {
        // Validación básica
        if (producto.getNombre() == null || producto.getNombre().trim().isEmpty()) {
            return false;
        }
        return productoDAO.agregarProducto(producto);
    }

    // 🔴 2. MÉTODO FALTANTE: obtenerTodosLosProductos
    public List<Producto> obtenerTodosLosProductos() {
        return productoDAO.obtenerTodosLosProductos();
    }

    // 🔴 3. MÉTODO FALTANTE: aumentarStockProducto
    public boolean aumentarStockProducto(int idProducto, int cantidadAAgregar) {
        if (cantidadAAgregar <= 0) {
            return false;
        }

        // Obtener el producto actual para saber su stock anterior
        List<Producto> productos = obtenerTodosLosProductos();
        Producto productoEncontrado = null;
        for (Producto p : productos) {
            if (p.getId() == idProducto) {
                productoEncontrado = p;
                break;
            }
        }

        if (productoEncontrado != null) {
            int nuevoStock = productoEncontrado.getStockActual() + cantidadAAgregar;
            return productoDAO.actualizarStock(idProducto, nuevoStock);
        } else {
            return false; // Producto no encontrado en la base de datos
        }
    }

    public boolean eliminarProducto(int idProducto) {
        // Aquí puedes agregar lógica de negocio (ej: no permitir eliminar si el stock es > 0)
        // Por ahora, solo llamamos al DAO
        return productoDAO.eliminarProducto(idProducto);
    }
}