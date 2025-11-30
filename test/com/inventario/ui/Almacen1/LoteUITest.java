package com.inventario.ui.Almacen1;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

import com.inventario.ui.almacen1.LoteUI;

class LoteUITest {

    @Test
    void debeDetectarLoteVencido() {
        // Creamos una fecha en el PASADO (Ayer)
        String fechaAyer = LocalDate.now().minusDays(1).toString();

        LoteUI lote = new LoteUI(1, 50.0, "2023-01-01", fechaAyer);

        assertEquals("VENCIDO", lote.getEstado(), "Si la fecha ya pasó, debe decir VENCIDO");
    }

    @Test
    void debeDetectarPorVencer() {
        // Creamos una fecha FUTURA cercana (dentro de 3 días)
        // La lógica dice: si es antes de hoy + 7 días
        String fechaCercana = LocalDate.now().plusDays(3).toString();

        LoteUI lote = new LoteUI(2, 50.0, "2023-01-01", fechaCercana);

        assertEquals("Por Vencer", lote.getEstado(), "Si vence en menos de 7 días, debe avisar");
    }

    @Test
    void debeDetectarEstadoOk() {
        // Creamos una fecha MUY FUTURA (el próximo mes)
        String fechaLejana = LocalDate.now().plusMonths(1).toString();

        LoteUI lote = new LoteUI(3, 50.0, "2023-01-01", fechaLejana);

        assertEquals("Ok", lote.getEstado(), "Si falta mucho para vencer, debe decir Ok");
    }

    @Test
    void debeManejarFechasNulasOInválidas() {
        // Caso sin fecha
        LoteUI loteSinFecha = new LoteUI(4, 10, "2023-01-01", null);
        assertEquals("No Vence", loteSinFecha.getEstado());

        // Caso fecha basura
        LoteUI loteError = new LoteUI(5, 10, "2023-01-01", "fecha-rara");
        assertEquals("Error Fecha", loteError.getEstado());
    }
}