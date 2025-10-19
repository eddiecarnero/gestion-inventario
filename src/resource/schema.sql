-- ===============================================
-- CREACIÓN DE TABLAS - Foco en Inventario
-- Base de Datos: mamatania
-- ===============================================

-- OPCIONAL: Eliminar la tabla Productos si ya existe para empezar limpio
DROP TABLE IF EXISTS Productos;


-- 1. Crear la tabla de Productos
CREATE TABLE Productos (
    idProducto INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    descripcion VARCHAR(255),
    precio DECIMAL(10, 2) NOT NULL,

    -- Campos de Inventario
    stockActual INT NOT NULL DEFAULT 0,  -- Cantidad de unidades en almacén
    stockMinimo INT NOT NULL DEFAULT 0   -- Nivel de alerta para bajo stock
);


-- DATOS DE PRUEBA INICIALES
INSERT INTO Productos (nombre, descripcion, precio, stockActual, stockMinimo) VALUES
    ('Tostadas de Maíz', 'Paquete de 12 unidades', 2.50, 15, 10),
    ('Harina de Trigo (1kg)', 'Para pasteles y pan', 3.00, 5, 15),    -- ESTARÁ EN BAJO STOCK
    ('Jarabe de Vainilla', 'Ingrediente para café', 8.00, 20, 5),
    ('Leche Deslactosada', 'Galón de leche', 4.50, 8, 8);             -- ESTARÁ AL LÍMITE