package com.inventario.ui;

import com.inventario.config.ConexionBD;
import javafx.scene.control.Alert;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class GestorVencimientos {

    public static void verificarYLimpiarVencidos() {
        List<String> mensajes = new ArrayList<>();
        Connection conn = null;

        try {
            conn = ConexionBD.getConnection();
            conn.setAutoCommit(false);

            // 1. Buscar lotes vencidos (FechaVencimiento < HOY)
            String sqlBuscar = "SELECT l.IdLote, l.IdProducto, l.CantidadActual, p.Tipo_de_Producto " +
                    "FROM lotes l " +
                    "JOIN producto p ON l.IdProducto = p.IdProducto " +
                    "WHERE l.FechaVencimiento IS NOT NULL AND l.FechaVencimiento < ?";

            try (PreparedStatement stmt = conn.prepareStatement(sqlBuscar)) {
                stmt.setString(1, LocalDate.now().toString());
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    int idLote = rs.getInt("IdLote");
                    int idProd = rs.getInt("IdProducto");
                    double cantidad = rs.getDouble("CantidadActual");
                    String nombre = rs.getString("Tipo_de_Producto");

                    // 2. Restar del Stock Total (Tabla producto)
                    try (PreparedStatement psStock = conn.prepareStatement("UPDATE producto SET Stock = Stock - ? WHERE IdProducto = ?")) {
                        psStock.setDouble(1, cantidad);
                        psStock.setInt(2, idProd);
                        psStock.executeUpdate();
                    }

                    // 3. Registrar en Kardex (Salida por Vencimiento)
                    try (PreparedStatement psKar = conn.prepareStatement("INSERT INTO kardex (IdProducto, Fecha, Motivo, TipoMovimiento, IdEmpleado, Cantidad) VALUES (?, ?, ?, ?, ?, ?)")) {
                        psKar.setInt(1, idProd);
                        psKar.setString(2, LocalDate.now().toString());
                        psKar.setString(3, "Vencimiento Lote #" + idLote);
                        psKar.setString(4, "Salida (Vencido)");
                        psKar.setInt(5, 1);
                        psKar.setDouble(6, cantidad);
                        psKar.executeUpdate();
                    }

                    // 4. Eliminar el Lote
                    try (PreparedStatement psDel = conn.prepareStatement("DELETE FROM lotes WHERE IdLote = ?")) {
                        psDel.setInt(1, idLote);
                        psDel.executeUpdate();
                    }

                    mensajes.add("- " + cantidad + " de " + nombre);
                }
            }
            conn.commit();

            if (!mensajes.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Alerta de Vencimiento");
                alert.setHeaderText("Se han eliminado productos vencidos:");
                alert.setContentText(String.join("\n", mensajes));
                alert.showAndWait();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) try {
                conn.rollback();
            } catch (Exception ex) {
            }
        } finally {
            if (conn != null) try {
                conn.setAutoCommit(true);
                conn.close();
            } catch (Exception ex) {
            }
        }
    }
}