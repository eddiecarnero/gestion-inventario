package com.inventario.dao;

import com.inventario.model.Producto;
import com.inventario.config.ConexionBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAO {

    // 1. Método para obtener todos los productos (Para revisar stock)
    public List<Producto> obtenerTodosLosProductos() {
        List<Producto> productos = new ArrayList<>();
        // Seleccionamos TODOS los campos, incluyendo los nuevos de stock
        String sql = "SELECT idProducto, nombre, descripcion, precio, stockActual, stockMinimo FROM Productos";

        try (Connection conn = ConexionBD.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Producto p = new Producto(
                        rs.getInt("idProducto"),
                        rs.getString("nombre"),
                        rs.getString("descripcion"),
                        rs.getDouble("precio"),
                        rs.getInt("stockActual"),   // Leemos el stock
                        rs.getInt("stockMinimo")    // Leemos el stock mínimo
                );
                productos.add(p);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener productos: " + e.getMessage());
        }
        return productos;
    }

    // 2. Método para agregar un producto nuevo
    public boolean agregarProducto(Producto producto) {
        String sql = "INSERT INTO Productos (nombre, descripcion, precio, stockActual, stockMinimo) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, producto.getNombre());
            pstmt.setString(2, producto.getDescripcion());
            pstmt.setDouble(3, producto.getPrecio());
            pstmt.setInt(4, producto.getStockActual());
            pstmt.setInt(5, producto.getStockMinimo());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al agregar producto: " + e.getMessage());
            return false;
        }
    }

    // 3. Método para actualizar el stock actual (necesario para la recepción de mercancía)
    public boolean actualizarStock(int idProducto, int nuevoStock) {
        String sql = "UPDATE Productos SET stockActual = ? WHERE idProducto = ?";
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
    public boolean eliminarProducto(int idProducto) {
        // Sentencia SQL para borrar una fila
        String sql = "DELETE FROM Productos WHERE idProducto = ?";

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idProducto);

            // Si la ejecución afecta a 1 o más filas, la eliminación fue exitosa
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            // Este catch atrapará errores como restricciones de clave externa (si el producto está en una orden de compra)
            System.err.println("Error al eliminar producto: " + e.getMessage());
            return false;
        }
    }
}