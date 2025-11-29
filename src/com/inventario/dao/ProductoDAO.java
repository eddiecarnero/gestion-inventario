package com.inventario.dao;

import com.inventario.config.ConexionBD;
import com.inventario.model.Producto;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAO {

    // 1. Método para obtener todos los productos
    public List<Producto> obtenerTodosLosProductos() {
        List<Producto> productos = new ArrayList<>();
        // --- CAMBIO: Agregamos Contenido ---
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
                // --- NUEVO ---
                double cont = rs.getDouble("Contenido");
                p.setContenido(cont <= 0 ? 1 : cont); // Evitar división por cero

                productos.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return productos;
    }

    // 2. Método para agregar un producto nuevo
    public boolean agregarProducto(Producto producto) {
        // --- CAMBIO: Agregamos Contenido ---
        String sql = "INSERT INTO producto (Tipo_de_Producto, Stock_Minimo, Unidad_de_medida, Ubicacion, IdProveedor, PrecioUnitario, Contenido) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, producto.getTipoDeProducto());
            pstmt.setInt(2, producto.getStockMinimo());
            pstmt.setString(3, producto.getUnidadDeMedida());
            pstmt.setString(4, producto.getUbicacion());
            pstmt.setInt(5, producto.getIdProveedor());
            pstmt.setDouble(6, producto.getPrecioUnitario());
            pstmt.setDouble(7, producto.getContenido()); // Nuevo

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 3. Método para actualizar el stock actual
    // (Este método no usa el objeto Producto, así que ya estaba bien)
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

    // 4. Método para eliminar un producto por ID
    // (Este método no usa el objeto Producto, así que ya estaba bien)
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

    // 5. Método para obtener un producto por ID (¡NUEVO Y NECESARIO!)
    public Producto obtenerProductoPorId(int idProducto) {
        // Usamos todas las columnas que definimos en el modelo
        String sql = "SELECT IdProducto, Tipo_de_Producto, Stock, Stock_Minimo, Stock_Maximo, Estado_Stock, Fecha_de_caducidad, Unidad_de_medida, Ubicacion FROM producto WHERE IdProducto = ?";

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
                    p.setStockMaximo(rs.getInt("Stock_Maximo"));
                    p.setEstadoStock(rs.getString("Estado_Stock"));
                    p.setFechaDeCaducidad(rs.getDate("Fecha_de_caducidad"));
                    p.setUnidadDeMedida(rs.getString("Unidad_de_medida"));
                    p.setUbicacion(rs.getString("Ubicacion"));
                    return p; // Devuelve el producto encontrado
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener producto por ID: " + e.getMessage());
        }
        return null; // No encontrado
    }

    // 6. Método para actualizar Stock Y Estado (¡NUEVO Y RECOMENDADO!)
//    Esto soluciona el problema de que el estado no se actualizaba.
    public boolean actualizarStockYEstado(int idProducto, int nuevoStock, String nuevoEstado) {
        String sql = "UPDATE producto SET Stock = ?, Estado_Stock = ? WHERE IdProducto = ?";
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, nuevoStock);
            pstmt.setString(2, nuevoEstado);
            pstmt.setInt(3, idProducto);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al actualizar stock y estado: " + e.getMessage());
            return false;
        }
    }

    public boolean actualizarProductoCompleto(Producto producto) {
        String sql = "UPDATE producto SET Tipo_de_Producto=?, Stock_Minimo=?, Unidad_de_medida=?, Ubicacion=?, IdProveedor=?, PrecioUnitario=?, Contenido=? WHERE IdProducto=?";
        try (Connection conn = ConexionBD.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, producto.getTipoDeProducto());
            pstmt.setInt(2, producto.getStockMinimo());
            pstmt.setString(3, producto.getUnidadDeMedida());
            pstmt.setString(4, producto.getUbicacion());
            pstmt.setInt(5, producto.getIdProveedor());
            pstmt.setDouble(6, producto.getPrecioUnitario());
            pstmt.setDouble(7, producto.getContenido());
            pstmt.setInt(8, producto.getIdProducto());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}