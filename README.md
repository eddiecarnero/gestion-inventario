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
- **Framework principal:**Java Swing para la interfaz y JDBC para la persistencia
- **Persistencia:** Spring Data JPA (con MySQL)
- **Control de versiones:** Git + GitHub

## Flujo de trabajo Git
- Ramas: `main`, `develop`, `feature/<nombre>`, `hotfix/<nombre>`
- Protecciones: PRs obligatorios en `main`; revisión por el líder.
- Issues: usar GitHub Issues para tareas y bugs.

## Roles del equipo
- Eddie Carnero : Jefe de Proyecto
- Yesenia Angeles : Programadora de Interfaz
- Xiomara Coronado : Programadora de Controladores
- Cristhian Gonzales : Programador de Persistencia
- Romel Ortiz : Tester
- Moises Sinche : Documentador

## Fases de Desarrollo

### Fase 1
<img src="https://github.com/eddiecarnero/gestion-inventario/blob/main/imagenes-readme/fase1.jpg?" width="50%" height="50%">

### Fase 2
<img src="https://github.com/eddiecarnero/gestion-inventario/blob/main/imagenes-readme/fase2.jpg?" width="50%" height="50%">

### Fase 3
<img src="https://github.com/eddiecarnero/gestion-inventario/blob/main/imagenes-readme/fase3.jpg?" width="50%" height="50%">

## Estructura del codigo
### 🧩 1. com.inventario.config
Propósito: configuración general del sistema.

ConexionBD → Clase encargada de crear la conexión a la base de datos MySQL (usando JDBC).

Ejemplo: define la URL, usuario y contraseña, y retorna un Connection.

### 🗂️ 2. com.inventario.dao (Data Access Object)

**Propósito:** comunicación directa con la base de datos.

Cada DAO ejecuta consultas SQL específicas (SELECT, INSERT, UPDATE, DELETE).

**KardexDAO** → Maneja los movimientos de inventario (entradas y salidas).

**OrdenCompraDAO** → Registra y obtiene las órdenes de compra a proveedores.

**ProductoDAO** → Administra productos e insumos.

**UsuarioDAO** → Valida login, registra usuarios, etc.

**VentaDAO** → Registra y consulta las ventas cargadas desde el Excel.

### 🧱 3. com.inventario.model

**Propósito:** contiene las clases que representan las entidades del sistema (tablas de la base de datos).

Son los objetos que se manipulan en el programa.

**DetalleCompra** → Detalle de cada producto en una orden de compra.

**Kardex **→ Registro de movimientos de inventario.

**OrdenCompra** → Datos de una orden de compra (proveedor, fecha, estado).

**Producto** → Datos de los productos e insumos.

**Usuario** → Información de los usuarios (nombre, rol, contraseña, etc).

### ⚙️ 4. com.inventario.service

**Propósito:** capa intermedia entre el DAO y la interfaz.

Contiene la lógica de negocio, por ejemplo:

- Validaciones.

- Cálculos de stock.

- Reglas antes de insertar datos.

**KardexService** → Controla entradas/salidas del inventario.

**OrdenCompraService** → Procesa las órdenes antes de guardarlas.

**ProductoService** → Maneja actualizaciones de productos, búsquedas, etc.

**UsuarioService** → Controla login, roles, y registro.

**VentaService** → Calcula y descuenta stock al subir el Excel de ventas.

### 🖥️ 5. com.inventario.ui (User Interface)

**Propósito:** Interfaz gráfica (pantallas) con las que el usuario interactúa.

Se implementa normalmente con Swing o JavaFX.

**LoginPage** → Pantalla de inicio de sesión.

**RegisterPage** → Registro de nuevos usuarios.

**MainDashboard** → Ventana principal (menú o panel central).

**KardexPage** → Vista del kardex (entradas/salidas).

**OrdenesPage** → Manejo de órdenes de compra.

**ProductosPage** → Gestión de productos e insumos.

**ReportesPage** → Generación de reportes o consultas.

### 🔧 6. com.inventario.util

**Propósito:** Clases de utilidades generales o herramientas.

**ConexionDB (posiblemente duplicado de ConexionBD, puedes unificarlo)** → métodos reutilizables para abrir/cerrar conexiones.

### 🚀 7. Main.java

**Propósito:** Punto de entrada del programa.

Aquí se ejecuta el método main().

Llama a la ventana principal (LoginPage o MainDashboard).

### 💾 8. resource

**Propósito:** archivos externos necesarios para la aplicación.

**schema.sql** → Script para crear las tablas de la base de datos.

**data.sql**→ Script con datos iniciales (usuarios, productos, etc).
