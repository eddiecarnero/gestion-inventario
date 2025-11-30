package com.inventario.logic;

import com.inventario.config.ConexionBD;
import com.inventario.model.RecetaSimple; // Asegúrate de que apunte a tu modelo
import com.inventario.util.ConversorUnidades;
import java.sql.*;
import java.time.LocalDate;

public class ProduccionService {

    // --- 1. MÉTODO PÚBLICO (El que llama tu UI) ---
    public boolean procesarProduccionFinal(RecetaSimple receta, double multiplicador, double precioVenta, LocalDate vencimiento) {
        Connection conn = null;
        try {
            conn = ConexionBD.getConnection();
            conn.setAutoCommit(false); // Iniciamos transacción

            // Llamamos a la lógica interna pasando la conexión
            procesarProduccionFinal(conn, receta, multiplicador, precioVenta, vencimiento);

            conn.commit(); // Si todo sale bien, guardamos cambios
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
        }
    }

    // --- 2. LÓGICA INTERNA (Testeable y Transaccional) ---
    // Este método recibe la conexión, permitiendo que un Test le pase una BD en memoria.
    public void procesarProduccionFinal(Connection conn, RecetaSimple receta, double multiplicador, double precioVenta, LocalDate vencimiento) throws SQLException {

        // A. LEER INGREDIENTES DE LA RECETA
        String sqlIng = "SELECT IdProducto, IdIntermedio, cantidad, unidad, tipo_origen FROM ingredientes WHERE receta_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sqlIng)) {
            ps.setInt(1, receta.getId());
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String tipoOrigen = rs.getString("tipo_origen"); // "INSUMO" o "INTERMEDIO"
                double cantReceta = rs.getDouble("cantidad");
                String unidadReceta = rs.getString("unidad");

                // Calculamos cuánto necesitamos en total (ej: 2 pasteles * 0.5kg harina = 1.0kg)
                double cantidadNecesaria = cantReceta * multiplicador;

                if ("INSUMO".equalsIgnoreCase(tipoOrigen)) {
                    int idProducto = rs.getInt("IdProducto");
                    consumirDeAlmacen1(conn, idProducto, cantidadNecesaria, unidadReceta);

                } else if ("INTERMEDIO".equalsIgnoreCase(tipoOrigen)) {
                    int idIntermedio = rs.getInt("IdIntermedio");
                    consumirDeAlmacen2(conn, idIntermedio, cantidadNecesaria, unidadReceta);
                }
            }
        }

        // B. CREAR O ACTUALIZAR EL PRODUCTO TERMINADO (Si no existe o cambio de precio)
        int idTerminado = gestionarProductoTerminado(conn, receta.getNombre(), precioVenta);

        // C. INSERTAR EL NUEVO LOTE EN ALMACÉN 3
        double cantidadFinal = receta.getCantidadBase() * multiplicador;
        int cantFinalInt = (int) Math.ceil(cantidadFinal); // Redondeamos hacia arriba para unidades enteras

        String sqlInsLote = "INSERT INTO lotes_terminados (IdProductoTerminado, CantidadActual, FechaProduccion, FechaVencimiento) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sqlInsLote)) {
            ps.setInt(1, idTerminado);
            ps.setInt(2, cantFinalInt);
            ps.setDate(3, Date.valueOf(LocalDate.now())); // Fecha Producción = Hoy
            ps.setDate(4, Date.valueOf(vencimiento));     // Fecha Vencimiento
            ps.executeUpdate();
        }
    }

    // --- MÉTODOS AUXILIARES PRIVADOS ---

    private int gestionarProductoTerminado(Connection conn, String nombre, double precioVenta) throws SQLException {
        // Verificar si ya existe
        String sqlCheck = "SELECT IdProductoTerminado FROM productos_terminados WHERE Nombre = ?";
        try (PreparedStatement ps = conn.prepareStatement(sqlCheck)) {
            ps.setString(1, nombre);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int id = rs.getInt(1);
                // Si existe, actualizamos el precio si es necesario
                if (precioVenta > 0) {
                    try (PreparedStatement psUpd = conn.prepareStatement("UPDATE productos_terminados SET PrecioVenta=? WHERE IdProductoTerminado=?")) {
                        psUpd.setDouble(1, precioVenta);
                        psUpd.setInt(2, id);
                        psUpd.executeUpdate();
                    }
                }
                return id;
            } else {
                // Si no existe, lo creamos
                String sqlIns = "INSERT INTO productos_terminados (Nombre, PrecioVenta) VALUES (?, ?)";
                try (PreparedStatement psIns = conn.prepareStatement(sqlIns, Statement.RETURN_GENERATED_KEYS)) {
                    psIns.setString(1, nombre);
                    psIns.setDouble(2, precioVenta);
                    psIns.executeUpdate();
                    ResultSet gk = psIns.getGeneratedKeys();
                    if (gk.next()) return gk.getInt(1);
                    else throw new SQLException("No se pudo crear el producto terminado: " + nombre);
                }
            }
        }
    }

    private void consumirDeAlmacen1(Connection conn, int idProducto, double cantidadNecesaria, String unidadReceta) throws SQLException {
        // 1. Obtener unidad del stock para convertir
        String unidadStock = "Unidad";
        try (PreparedStatement ps = conn.prepareStatement("SELECT Unidad_de_medida FROM producto WHERE IdProducto=?")) {
            ps.setInt(1, idProducto);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) unidadStock = rs.getString(1);
        }

        // 2. Convertir unidades (ej. Receta pide Litros, Stock está en ml)
        double cantidadReal = ConversorUnidades.convertir(cantidadNecesaria, unidadReceta, unidadStock);
        double faltante = cantidadReal;

        // 3. Buscar lotes (FEFO: Primero los que vencen antes)
        String sqlLotes = "SELECT IdLote, CantidadActual FROM lotes WHERE IdProducto = ? AND CantidadActual > 0 ORDER BY FechaVencimiento ASC";

        try (PreparedStatement ps = conn.prepareStatement(sqlLotes)) {
            ps.setInt(1, idProducto);
            ResultSet rs = ps.executeQuery();

            while (rs.next() && faltante > 0.001) { // 0.001 margen de error decimal
                int idLote = rs.getInt("IdLote");
                double cantLote = rs.getDouble("CantidadActual");
                double aDescontar = Math.min(cantLote, faltante);

                // Actualizar o Borrar Lote
                if (Math.abs(cantLote - aDescontar) < 0.001) {
                    try (PreparedStatement psDel = conn.prepareStatement("DELETE FROM lotes WHERE IdLote=?")) {
                        psDel.setInt(1, idLote);
                        psDel.executeUpdate();
                    }
                } else {
                    try (PreparedStatement psUpd = conn.prepareStatement("UPDATE lotes SET CantidadActual = CantidadActual - ? WHERE IdLote=?")) {
                        psUpd.setDouble(1, aDescontar);
                        psUpd.setInt(2, idLote);
                        psUpd.executeUpdate();
                    }
                }

                // Actualizar Stock Maestro
                try (PreparedStatement psUpdProd = conn.prepareStatement("UPDATE producto SET Stock = Stock - ? WHERE IdProducto=?")) {
                    psUpdProd.setDouble(1, aDescontar);
                    psUpdProd.setInt(2, idProducto);
                    psUpdProd.executeUpdate();
                }

                faltante -= aDescontar;
            }
        }

        if (faltante > 0.001) {
            throw new SQLException("Stock insuficiente en Almacén 1 para el producto ID " + idProducto);
        }
    }

    private void consumirDeAlmacen2(Connection conn, int idIntermedio, double cantidadNecesaria, String unidadReceta) throws SQLException {
        // Lógica idéntica al almacén 1 pero apuntando a las tablas de intermedios
        String unidadStock = "Unidad";
        try (PreparedStatement ps = conn.prepareStatement("SELECT Unidad_de_medida FROM productos_intermedios WHERE IdProductoIntermedio=?")) {
            ps.setInt(1, idIntermedio);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) unidadStock = rs.getString(1);
        }

        double cantidadReal = ConversorUnidades.convertir(cantidadNecesaria, unidadReceta, unidadStock);
        double faltante = cantidadReal;

        String sqlLotes = "SELECT IdLote, CantidadActual FROM lotes_intermedios WHERE IdProductoIntermedio = ? AND CantidadActual > 0 ORDER BY FechaVencimiento ASC";

        try (PreparedStatement ps = conn.prepareStatement(sqlLotes)) {
            ps.setInt(1, idIntermedio);
            ResultSet rs = ps.executeQuery();

            while (rs.next() && faltante > 0.001) {
                int idLote = rs.getInt("IdLote");
                double cantLote = rs.getDouble("CantidadActual");
                double aDescontar = Math.min(cantLote, faltante);

                if (Math.abs(cantLote - aDescontar) < 0.001) {
                    try (PreparedStatement psDel = conn.prepareStatement("DELETE FROM lotes_intermedios WHERE IdLote=?")) {
                        psDel.setInt(1, idLote);
                        psDel.executeUpdate();
                    }
                } else {
                    try (PreparedStatement psUpd = conn.prepareStatement("UPDATE lotes_intermedios SET CantidadActual = CantidadActual - ? WHERE IdLote=?")) {
                        psUpd.setDouble(1, aDescontar);
                        psUpd.setInt(2, idLote);
                        psUpd.executeUpdate();
                    }
                }
                faltante -= aDescontar;
            }
        }

        if (faltante > 0.001) {
            throw new SQLException("Stock insuficiente en Almacén 2 para el producto intermedio ID " + idIntermedio);
        }
    }
}