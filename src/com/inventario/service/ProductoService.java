package com.inventario.service;

import com.inventario.dao.ProductoDAO;
import com.inventario.model.Producto;

import java.util.List;

public class ProductoService {
    private ProductoDAO productoDAO;

    public ProductoService() {
        this.productoDAO = new ProductoDAO();
    }

    // 1. Método agregarNuevoProducto (Corregido)
    public boolean agregarNuevoProducto(Producto producto) {
        // Validación básica (Corregida: usa getTipoDeProducto)
        if (producto.getTipoDeProducto() == null || producto.getTipoDeProducto().trim().isEmpty()) {
            return false;
        }
        // Este producto ya debe venir con el 'EstadoStock' calculado desde la UI
        return productoDAO.agregarProducto(producto);
    }

    // 2. Método obtenerTodosLosProductos (Estaba bien)
    public List<Producto> obtenerTodosLosProductos() {
        return productoDAO.obtenerTodosLosProductos();
    }

    // 3. Método aumentarStockProducto (Corregido y Optimizado)
    public boolean aumentarStockProducto(int idProducto, int cantidadAAgregar) {
        if (cantidadAAgregar <= 0) {
            return false; // No se puede agregar 0 o menos
        }

        // --- OPTIMIZACIÓN ---
        // Ya no cargamos la lista entera. Buscamos solo el que necesitamos.
        // (Usa el método 5 que agregamos al DAO)
        Producto productoActual = productoDAO.obtenerProductoPorId(idProducto);

        if (productoActual != null) {
            // Corregido: usa getStock()
            int nuevoStock = productoActual.getStock() + cantidadAAgregar;

            // --- LÓGICA DE NEGOCIO (NUEVA) ---
            // Recalculamos el estado basado en el nuevo stock
            String nuevoEstado;
            if (nuevoStock <= productoActual.getStockMinimo()) {
                nuevoEstado = "BAJO_STOCK";
            } else {
                nuevoEstado = "EN_STOCK";
            }
            // (Puedes añadir lógica para "SOBRE_STOCK" si comparas con getStockMaximo)

            // Usamos el nuevo método del DAO (método 6) para actualizar AMBAS columnas
            return productoDAO.actualizarStockYEstado(idProducto, nuevoStock, nuevoEstado);

        } else {
            return false; // Producto no encontrado
        }
    }

    // 4. Método eliminarProducto (Estaba bien)
    public boolean eliminarProducto(int idProducto) {
        // Lógica de negocio (ej: no eliminar si stock > 0) podría ir aquí.
        // Por ahora, solo llamamos al DAO.
        return productoDAO.eliminarProducto(idProducto);
    }
}