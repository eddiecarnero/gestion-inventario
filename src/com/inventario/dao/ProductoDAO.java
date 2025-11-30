package com.inventario.dao;

import com.inventario.model.Producto;
import com.inventario.config.ConexionBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAO {

    // =========================================================================
    // SECCIÓN 1: EL NUEVO "CEREBRO" (Lógica Centralizada para Tests)
    // =========================================================================

    // Este es el método maestro. 'agregar' y 'actualizar' ahora usan esto por debajo.
    public boolean guardarProducto(Connection conn, Producto p, boolean esNuevo) {
        String sql = esNuevo
                ? "INSERT INTO producto (Tipo_de_Producto, Stock_Minimo, Unidad_de_medida, Ubicacion, IdProveedor, PrecioUnitario, Contenido, Stock) VALUES (?, ?, ?, ?, ?, ?, ?, 0)"
                : "UPDATE producto SET Tipo_de_Producto=?, Stock_Minimo=?, Unidad_de_medida=?, Ubicacion=?, IdProveedor=?, PrecioUnitario=?, Contenido=? WHERE IdProducto=?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, p.getTipoDeProducto());
            pstmt.setInt(2, p.getStockMinimo());
            pstmt.setString(3, p.getUnidadDeMedida());
            pstmt.setString(4, p.getUbicacion());
            pstmt.setInt(5, p.getIdProveedor());
            pstmt.setDouble(6, p.getPrecioUnitario());
            pstmt.setDouble(7, p.getContenido());

            if (!esNuevo) {
                pstmt.setInt(8, p.getIdProducto());
            }

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Puente para la UI (Abre la conexión automáticamente)
    public boolean guardarProducto(Producto p, boolean esNuevo) {
        try (Connection conn = ConexionBD.getConnection()) {
            return guardarProducto(conn, p, esNuevo);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // =========================================================================
    // SECCIÓN 2: TUS MÉTODOS ANTIGUOS (Compatibilidad Total)
    // =========================================================================

    // [REDIRIGIDO] Antes tenía SQL propio, ahora usa el método maestro. Funciona IGUAL.
    public boolean agregarProducto(Producto producto) {
        return guardarProducto(producto, true); // true = es nuevo
    }

    // [REDIRIGIDO] Antes tenía SQL propio, ahora usa el método maestro. Funciona IGUAL.
    public boolean actualizarProductoCompleto(Producto producto) {
        return guardarProducto(producto, false); // false = es edición
    }

    // [MANTENIDO] Este método es específico, así que se queda con su lógica original.
    public boolean actualizarStock(int idProducto, int nuevoStock) {
        String sql = "UPDATE producto SET Stock = ? WHERE IdProducto = ?";
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, nuevoStock);
            pstmt.setInt(2, idProducto);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar stock: " + e.getMessage());
            return false;
        }
    }

    // [MANTENIDO] Lógica original de eliminación.
    public boolean eliminarProducto(int idProducto) {
        String sql = "DELETE FROM producto WHERE IdProducto = ?";
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idProducto);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al eliminar producto: " + e.getMessage());
            return false;
        }
    }

    // [MANTENIDO] Lógica original de lectura (con la corrección del campo 'Contenido').
    public List<Producto> obtenerTodosLosProductos() {
        List<Producto> productos = new ArrayList<>();
        String sql = "SELECT IdProducto, Tipo_de_Producto, Stock, Stock_Minimo, Stock_Maximo, Estado_Stock, Fecha_de_caducidad, Unidad_de_medida, Ubicacion, PrecioUnitario, IdProveedor, Contenido FROM producto";

        try (Connection conn = ConexionBD.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Producto p = new Producto();
                p.setIdProducto(rs.getInt("IdProducto"));
                p.setTipoDeProducto(rs.getString("Tipo_de_Producto"));
                p.setStock(rs.getInt("Stock"));
                p.setStockMinimo(rs.getInt("Stock_Minimo"));
                p.setStockMaximo(rs.getInt("Stock_Maximo"));
                p.setEstadoStock(rs.getString("Estado_Stock"));
                p.setFechaDeCaducidad(rs.getDate("Fecha_de_caducidad"));
                p.setUnidadDeMedida(rs.getString("Unidad_de_medida"));
                p.setUbicacion(rs.getString("Ubicacion"));
                p.setPrecioUnitario(rs.getDouble("PrecioUnitario"));
                p.setIdProveedor(rs.getInt("IdProveedor"));

                double cont = rs.getDouble("Contenido");
                p.setContenido(cont <= 0 ? 1 : cont);

                productos.add(p);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return productos;
    }

    // [MANTENIDO] Lógica original de buscar por ID.
    public Producto obtenerProductoPorId(int idProducto) {
        String sql = "SELECT * FROM producto WHERE IdProducto = ?";
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idProducto);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Producto p = new Producto();
                    p.setIdProducto(rs.getInt("IdProducto"));
                    p.setTipoDeProducto(rs.getString("Tipo_de_Producto"));
                    p.setStock(rs.getInt("Stock"));
                    p.setStockMinimo(rs.getInt("Stock_Minimo"));
                    // ... mapeo rápido ...
                    p.setPrecioUnitario(rs.getDouble("PrecioUnitario"));
                    return p;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener producto: " + e.getMessage());
        }
        return null;
    }

    // [MANTENIDO] Método específico para estado y stock.
    public boolean actualizarStockYEstado(int idProducto, int nuevoStock, String nuevoEstado) {
        String sql = "UPDATE producto SET Stock = ?, Estado_Stock = ? WHERE IdProducto = ?";
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, nuevoStock);
            pstmt.setString(2, nuevoEstado);
            pstmt.setInt(3, idProducto);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }
}