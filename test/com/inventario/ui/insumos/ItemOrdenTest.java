package com.inventario.ui.insumos;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ItemOrdenTest {

    @Test
    void debeCalcularTotalConContenidoEstandar() {
        // CASO 1: Compras "Leche" por Litro y el precio es por Litro (Contenido = 1)
        // Precio: $10.00 por Litro
        // Cantidad: 5 Litros
        InsumoUI leche = new InsumoUI(1, 100, "Leche", 10.00, "Litro", 1.0);

        ItemOrden item = new ItemOrden(leche, 5.0);

        // Total esperado: 5 * 10 = $50.00
        assertEquals(50.00, item.getTotal(), 0.01, "El cálculo simple falló");
    }

    @Test
    void debeCalcularTotalConPaquetesGrandes() {
        // CASO 2: Compras "Saco de Arroz" (50kg)
        // El precio ($100) es POR SACO (Unidad de compra).
        // El contenido es 50 (kg).
        // Tú pides 100 kg de arroz.

        // InsumoUI(id, idProv, nombre, precio, unidad, contenido)
        // Precio $100 es por el SACO entero, que trae 50kg.
        InsumoUI sacoArroz = new InsumoUI(2, 100, "Arroz Costeño", 100.00, "Saco 50kg", 50.0);

        // Pedimos 100 kg
        // La fórmula es: (CantidadPedida / Contenido) * PrecioUnitario
        // (100 / 50) * 100 = 2 sacos * $100 = $200
        ItemOrden item = new ItemOrden(sacoArroz, 100.0);

        assertEquals(200.00, item.getTotal(), 0.01, "El cálculo con paquetes grandes falló");
        assertEquals(200.00, item.getPrecioUnitario() * (100.0 / 50.0), 0.01);
    }

    @Test
    void debeCalcularTotalConPaquetesPequeños() {
        // CASO 3: Compras "Mantequilla" (Barra de 200g)
        // Precio: $5.00 por barra.
        // Contenido: 0.2 kg (o 200 si usas gramos, supongamos kg para el ejemplo).
        // Pides: 1 kg de mantequilla.

        InsumoUI mantequilla = new InsumoUI(3, 100, "Mantequilla", 5.00, "Barra", 0.2);

        // Pides 1.0 unidad de medida base (kg)
        // (1.0 / 0.2) = 5 barras
        // 5 barras * $5.00 = $25.00
        ItemOrden item = new ItemOrden(mantequilla, 1.0);

        assertEquals(25.00, item.getTotal(), 0.01, "El cálculo con paquetes pequeños falló");
    }
}