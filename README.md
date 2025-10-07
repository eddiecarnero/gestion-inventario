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

## 🧱 Estructura del Código

### 🧩 1. `com.inventario.config`
**Propósito:** configuración general del sistema.  

- **ConexionBD** → Clase encargada de crear la conexión a la base de datos **MySQL** (usando JDBC).  
  - Define la URL, usuario y contraseña.  
  - Retorna un objeto `Connection` para ser usado en los DAO.

---

### 🗂️ 2. `com.inventario.dao` *(Data Access Object)*
**Propósito:** comunicación directa con la base de datos.  

Cada DAO ejecuta consultas **SQL** específicas (`SELECT`, `INSERT`, `UPDATE`, `DELETE`).  

- **KardexDAO** → Maneja los movimientos del inventario (entradas y salidas).  
- **OrdenCompraDAO** → Registra y obtiene las órdenes de compra a proveedores.  
- **ProductoDAO** → Administra productos e insumos.  
- **UsuarioDAO** → Valida login y gestiona usuarios.  
- **VentaDAO** → Registra y consulta las ventas cargadas desde Excel.

---

### 🧱 3. `com.inventario.model`
**Propósito:** representa las **entidades del sistema** (tablas de la base de datos).  

Estas clases definen los **objetos del dominio** que se manipulan en la aplicación.  

- **DetalleCompra** → Detalle de cada producto en una orden de compra.  
- **Kardex** → Registro de movimientos del inventario.  
- **OrdenCompra** → Datos de una orden (proveedor, fecha, estado).  
- **Producto** → Información de productos e insumos.  
- **Usuario** → Datos del usuario (nombre, rol, contraseña, etc).

---

### ⚙️ 4. `com.inventario.service`
**Propósito:** capa intermedia entre el **DAO** y la **interfaz gráfica**.  

Contiene la **lógica de negocio**, como:  
- Validaciones.  
- Cálculos de stock.  
- Reglas antes de insertar o actualizar datos.  

**Clases principales:**  
- **KardexService** → Controla entradas y salidas del inventario.  
- **OrdenCompraService** → Procesa las órdenes antes de guardarlas.  
- **ProductoService** → Gestiona actualizaciones y búsquedas de productos.  
- **UsuarioService** → Controla el login, roles y registro de usuarios.  
- **VentaService** → Calcula y descuenta stock al cargar ventas desde Excel.

---

### 🖥️ 5. `com.inventario.ui` *(User Interface)*
**Propósito:** interfaz gráfica con la que interactúa el usuario.  

Implementada con **Swing** o **JavaFX**.  

- **RegisterPage** → Registro de nuevos usuarios.  
- **MainDashboard** → Ventana principal o panel de control.  
- **KardexPage** → Vista del kardex (entradas/salidas).  
- **OrdenesPage** → Gestión de órdenes de compra.  
- **ProductosPage** → Administración de productos e insumos.  
- **ReportesPage** → Generación de reportes y consultas.

---

### 🔧 6. `com.inventario.util`
**Propósito:** clases de **utilidades generales** o herramientas auxiliares.  

- **ConexionDB** → Métodos reutilizables para abrir y cerrar conexiones *(posiblemente duplicado de `ConexionBD`; se recomienda unificarlos)*.

---

### 🚀 7. `Main.java`
**Propósito:** punto de entrada del programa.  

Contiene el método `main()`, que inicia la aplicación:  
- Llama a la ventana principal (**LoginPage** o **MainDashboard**).  

---

### 💾 8. `resources/`
**Propósito:** archivos externos necesarios para la aplicación.  

- **schema.sql** → Script para crear las tablas de la base de datos.  
- **data.sql** → Script con datos iniciales (usuarios, productos, etc).

---
