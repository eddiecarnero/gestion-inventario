package com.inventario.ui.ventas;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FilaCargaTest {

    @Test
    void constructorDebeAsignarValoresCorrectamente() {
        // Simulamos una fila leída desde el Excel
        // Producto: "Galletas", Cantidad: 10, Fecha: "2023-10-01", Cliente: "Juan", Estado: "Pendiente", ID: 50
        FilaCarga fila = new FilaCarga("Galletas", 10, "2023-10-01", "Juan", "Pendiente", 50);

        assertAll("Verificando integridad de la fila de carga",
                () -> assertEquals("Galletas", fila.getNombreProducto()),
                () -> assertEquals(10, fila.getCantidad()),
                () -> assertEquals("Juan", fila.getCliente()),
                () -> assertEquals(50, fila.getIdProducto())
        );
    }

    @Test
    void estadoDebeSerModificable() {
        // Este test es importante porque tu lógica de "Procesar Carga Masiva"
        // cambia el estado de "Pendiente" a "Registrado" o "Error".

        FilaCarga fila = new FilaCarga("Soda", 5, "2023-10-01", "Ana", "Pendiente", 1);

        // Simulamos que el proceso fue exitoso
        fila.setEstado("Registrado");

        assertEquals("Registrado", fila.getEstado(), "El estado debe poder actualizarse tras procesar la venta");
    }
}