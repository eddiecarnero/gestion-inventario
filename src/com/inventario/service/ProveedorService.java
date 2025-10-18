package com.inventario.service;

import com.inventario.dao.ProveedorDAO;
import com.inventario.model.Proveedor;
import java.util.List;

public class ProveedorService {
    private final ProveedorDAO proveedorDAO = new ProveedorDAO();

    public List<Proveedor> obtenerProveedores() {
        return proveedorDAO.listarProveedores();
    }

    public Proveedor obtenerPorId(int id) {
        return proveedorDAO.buscarPorId(id);
    }
}
