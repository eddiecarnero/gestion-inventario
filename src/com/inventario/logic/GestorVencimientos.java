package com.inventario.logic;

import com.inventario.config.ConexionBD;
import javafx.scene.control.Alert;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class GestorVencimientos {

    // Método principal que usa la App (conecta a la BD real y muestra alertas)
    public static void verificarYLimpiarVencidos() {
        try (Connection conn = ConexionBD.getConnection()) {
            List<String> eliminados = verificarYLimpiarVencidos(conn);

            if (!eliminados.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Alerta de Vencimiento");
                alert.setHeaderText("Se han eliminado productos vencidos:");
                alert.setContentText(String.join("\n", eliminados));
                alert.showAndWait();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Método Lógico para TEST (Recibe conexión y devuelve lista de lo borrado)
    public static List<String> verificarYLimpiarVencidos(Connection conn) throws SQLException {
        List<String> mensajes = new ArrayList<>();

        // 1. Buscar lotes vencidos (FechaVencimiento < HOY)
        String sqlBuscar = "SELECT l.IdLote, l.IdProducto, l.CantidadActual, p.Tipo_de_Producto " +
                "FROM lotes l " +
                "JOIN producto p ON l.IdProducto = p.IdProducto " +
                "WHERE l.FechaVencimiento IS NOT NULL AND l.FechaVencimiento < ?";

        List<DatosLote> lotesVencidos = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(sqlBuscar)) {
            stmt.setString(1, LocalDate.now().toString());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                lotesVencidos.add(new DatosLote(
                        rs.getInt("IdLote"),
                        rs.getInt("IdProducto"),
                        rs.getDouble("CantidadActual"),
                        rs.getString("Tipo_de_Producto")
                ));
            }
        }

        // Si no hay nada vencido, salimos rápido
        if (lotesVencidos.isEmpty()) return mensajes;

        // Procesar eliminaciones
        for (DatosLote lote : lotesVencidos) {
            // 2. Restar del Stock Total
            try (PreparedStatement psStock = conn.prepareStatement("UPDATE producto SET Stock = Stock - ? WHERE IdProducto = ?")) {
                psStock.setDouble(1, lote.cantidad);
                psStock.setInt(2, lote.idProd);
                psStock.executeUpdate();
            }

            // 3. Registrar en Kardex
            try (PreparedStatement psKar = conn.prepareStatement("INSERT INTO kardex (IdProducto, Fecha, Motivo, TipoMovimiento, IdEmpleado, Cantidad) VALUES (?, ?, ?, ?, ?, ?)")) {
                psKar.setInt(1, lote.idProd);
                psKar.setString(2, LocalDate.now().toString());
                psKar.setString(3, "Vencimiento Lote #" + lote.idLote);
                psKar.setString(4, "Salida (Vencido)");
                psKar.setInt(5, 1);
                psKar.setDouble(6, lote.cantidad);
                psKar.executeUpdate();
            }

            // 4. Eliminar el Lote
            try (PreparedStatement psDel = conn.prepareStatement("DELETE FROM lotes WHERE IdLote = ?")) {
                psDel.setInt(1, lote.idLote);
                psDel.executeUpdate();
            }

            mensajes.add("- " + lote.cantidad + " de " + lote.nombre);
        }

        return mensajes;
    }

    // Clase auxiliar interna para guardar datos temporalmente
    private static class DatosLote {
        int idLote, idProd; double cantidad; String nombre;
        public DatosLote(int l, int p, double c, String n) { idLote=l; idProd=p; cantidad=c; nombre=n; }
    }
}