package com.inventario.dao;

import com.inventario.model.Proveedor;
import java.util.ArrayList;
import java.util.List;

//Por ahora vamos a simular para probar

public class ProveedorDAO {
    private static final List<Proveedor> listaProveedores = new ArrayList<>();

    static {
        // Simulación de base de datos
        listaProveedores.add(new Proveedor(1, "Distribuidora El Frío S.A.", "20123456789", "Alimentos",
                "987654321", "contacto@elfrio.com", "Av. Los Helados 123"));
        listaProveedores.add(new Proveedor(2, "Lácteos del Sur", "20876543210", "Lácteos",
                "912345678", "ventas@lacteosdelsur.pe", "Jr. Central 456"));
        listaProveedores.add(new Proveedor(3, "DulceProve S.A.C.", "20567890123", "Jarabes",
                "934567890", "info@dulceprove.com", "Av. Sabores 789"));
    }

    public List<Proveedor> listarProveedores() {
        return listaProveedores;
    }

    public Proveedor buscarPorId(int id) {
        for (Proveedor p : listaProveedores) {
            if (p.getIdProveedor() == id) return p;
        }
        return null;
    }
}