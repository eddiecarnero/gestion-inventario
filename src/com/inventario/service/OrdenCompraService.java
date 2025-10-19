package com.inventario.service;

import com.inventario.dao.OrdenCompraDAO;
import com.inventario.model.OrdenCompra;

import java.util.Date;
import java.util.List;

public class OrdenCompraService {
    private final OrdenCompraDAO ordenDAO = new OrdenCompraDAO();

    public void crearOrden(int idProveedor, int idEmpleado, String tipoCompra,
                           double precioTotal, Date fechaCompra, Date fechaEntrega, String observacion) {

        if (precioTotal <= 0) {
            System.out.println("⚠️ Error: el precio total no puede ser 0 o negativo.");
            return;
        }

        if (fechaCompra == null) fechaCompra = new Date();

        OrdenCompra nueva = new OrdenCompra(0, idProveedor, idEmpleado, tipoCompra,
                precioTotal, fechaCompra, fechaEntrega, observacion);

        ordenDAO.registrarOrden(nueva);
    }

    public List<OrdenCompra> listarOrdenes() {
        return ordenDAO.listarOrdenes();
    }
}