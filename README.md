# Proyecto de Gestion de Inventario para MamaTania
## Descripción
Sistema de gestión de inventario para una heladería/cafetería. Gestiona insumos, lotes, fechas de caducidad, preparación de productos intermedios y finales, control de stock, alertas por stock mínimo y registro de mermas. No incluye registro de ventas.
![Logo de MamaTania](https://linktr.ee/og/image/mamatania.pe.jpg)

## Decisiones de diseño
- **Arquitectura:** En capas (Presentación, Lógica de negocio, Acceso a datos).
- **BD:** MySQL (soporta integridad referencial y transacciones). Para desarrollo local se puede usar SQLite con SQLAlchemy.
- **Modelo de datos:** Tablas principales: `Producto`, `Proveedor`, `OrdenCompra`, `DetalleCompra`, `Kardex`, `Empleado`.

## Lenguaje y frameworks
- **Lenguaje:** Java (JDK 17 recomendado)
- **Framework principal:** Spring Boot con Spring MVC
- **Persistencia:** Spring Data JPA (con MySQL)
- **Control de versiones:** Git + GitHub

## Flujo de trabajo Git
- Ramas: `main`, `develop`, `feature/<nombre>`, `hotfix/<nombre>`
- Protecciones: PRs obligatorios en `main`; revisión por el líder.
- Issues: usar GitHub Issues para tareas y bugs.

## Roles del equipo
- Ángeles Reyes, Yesenia Alessandra – Facilitadora de Organización
- Coronado Lucano, Xiomara Elizabeth – Encargada de Requerimientos
- Ortiz Villegas, Romel – Responsable de Diseño y Arquitectura
- Carnero Olivares, Eddie Carlos Daniel – Encargado de Comunicación y Control de Versiones
- Gonzales Flores, Cristhian Fabián – Responsable de Documentación
- Sinche Vega, Moisés Pedro – Encargado de Calidad y Pruebas
