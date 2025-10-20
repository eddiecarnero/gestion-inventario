package com.inventario.dao;

import com.inventario.model.Producto;
import com.inventario.config.ConexionBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAO {

    // 1. Método para obtener todos los productos
    public List<Producto> obtenerTodosLosProductos() {
        List<Producto> productos = new ArrayList<>();
        // La consulta SQL sigue siendo la misma, usando los nombres de columna de la BD
        String sql = "SELECT IdProducto, Tipo_de_Producto, Stock, Stock_Minimo, Stock_Maximo, Estado_Stock, Fecha_de_caducidad, Unidad_de_medida, Ubicacion FROM producto";

        try (Connection conn = ConexionBD.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Producto p = new Producto();

                // *** CORRECCIÓN ***
                // Mapeamos las columnas de la BD (String) a los setters de Java (camelCase)
                p.setIdProducto(rs.getInt("IdProducto"));
                p.setTipoDeProducto(rs.getString("Tipo_de_Producto")); // Corregido
                p.setStock(rs.getInt("Stock"));
                p.setStockMinimo(rs.getInt("Stock_Minimo"));           // Corregido
                p.setStockMaximo(rs.getInt("Stock_Maximo"));           // Corregido
                p.setEstadoStock(rs.getString("Estado_Stock"));        // Corregido
                p.setFechaDeCaducidad(rs.getDate("Fecha_de_caducidad"));// Corregido
                p.setUnidadDeMedida(rs.getString("Unidad_de_medida"));  // Corregido
                p.setUbicacion(rs.getString("Ubicacion"));

                productos.add(p);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener productos: " + e.getMessage());
        }
        return productos;
    }

    // 2. Método para agregar un producto nuevo
    public boolean agregarProducto(Producto producto) {
        // La consulta SQL sigue siendo la misma
        String sql = "INSERT INTO producto (Tipo_de_Producto, Stock, Stock_Minimo, Stock_Maximo, Estado_Stock, Fecha_de_caducidad, Unidad_de_medida, Ubicacion) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // *** CORRECCIÓN ***
            // Usamos los getters de Java (camelCase) para poblar la consulta
            pstmt.setString(1, producto.getTipoDeProducto()); // Corregido
            pstmt.setInt(2, producto.getStock());
            pstmt.setInt(3, producto.getStockMinimo());        // Corregido
            pstmt.setInt(4, producto.getStockMaximo());        // Corregido
            pstmt.setString(5, producto.getEstadoStock());     // Corregido
            pstmt.setDate(6, producto.getFechaDeCaducidad()); // Corregido
            pstmt.setString(7, producto.getUnidadDeMedida()); // Corregido
            pstmt.setString(8, producto.getUbicacion());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al agregar producto: " + e.getMessage());
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
}