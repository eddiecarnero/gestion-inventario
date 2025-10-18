package com.inventario.dao;

import com.inventario.model.OrdenCompra;
import java.util.ArrayList;
import java.util.List;


public class OrdenCompraDAO {
    private static final List<OrdenCompra> ordenes = new ArrayList<>();
    private static int contadorId = 1;

    public void registrarOrden(OrdenCompra orden) {
        orden.setIdCompra(contadorId++);
        ordenes.add(orden);
        System.out.println("Orden registrada: ID " + orden.getIdCompra());
    }

    public List<OrdenCompra> listarOrdenes() {
        return ordenes;
    }
}
