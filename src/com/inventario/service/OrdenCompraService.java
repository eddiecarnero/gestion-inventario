package com.inventario.service;

import com.inventario.dao.OrdenCompraDAO;
import com.inventario.model.OrdenCompra;

import java.util.Date;
import java.util.List;

public class OrdenCompraService {
    private final OrdenCompraDAO ordenDAO = new OrdenCompraDAO();

    public void crearOrden(int idProveedor, int idEmpleado, String tipoCompra,
                           double precioTotal, Date fechaCompra, Date fechaEntrega, String observacion) {

        OrdenCompra nueva = new OrdenCompra(0, idProveedor, idEmpleado, tipoCompra,
                precioTotal, fechaCompra, fechaEntrega, observacion);

        // Validaciones simples antes de registrar
        if (precioTotal <= 0) {
            System.out.println("Error: el precio total no puede ser 0 o negativo.");
            return;
        }

        if (fechaCompra == null) fechaCompra = new Date();
        ordenDAO.registrarOrden(nueva);
    }

    public List<OrdenCompra> listarOrdenes() {
        return ordenDAO.listarOrdenes();
    }
}
