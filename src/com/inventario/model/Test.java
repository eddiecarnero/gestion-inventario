package com.inventario.model;

import com.inventario.dao.OrdenCompraDAO;

import java.util.Date;

public class Test {
    public static void main(String[] args) {
        OrdenCompraDAO dao = new OrdenCompraDAO();

        OrdenCompra orden = new OrdenCompra();
        orden.setIdProveedor(1);
        orden.setIdEmpleado(1);
        orden.setTipoDeCompra("Compra local");
        orden.setPrecioTotal(1234.56);
        orden.setFechaDeCompra(new Date());
        orden.setFechaDeEntrega(new Date());
        orden.setObservacion("Prueba directa");

        dao.registrarOrden(orden);

    }
}
