# 🏋️ Coach Gym 360

**Plataforma integral para la transformación digital y modernización de la gestión de Coach Gym.**

Sistema web de gestión administrativa y control de acceso para gimnasios, desarrollado con **Spring Boot** bajo arquitectura **MVC en capas** (Controller → Service → Repository) y base de datos **MySQL**. Permite administrar clientes, cobrar y renovar membresías, controlar la entrada al gimnasio con **códigos QR**, reservar clases grupales con control de cupo y separar los permisos entre **Administración** y **Recepción**.

> Proyecto académico — Universidad Francisco Gavidia, Facultad de Ingeniería y Sistemas.

---

## ✨ Funcionalidades

### 🔐 Inicio de sesión y roles
- Autenticación por sesión HTTP con dos perfiles: **Administrador** y **Recepción**.
- Contraseñas **encriptadas con BCrypt** (en la base de datos nunca se guardan en texto plano).
- Un interceptor (`RolInterceptor`) protege las rutas: si Recepción intenta entrar por URL a una sección de Administración, el sistema lo bloquea y muestra un aviso de acceso denegado.

### 📊 Dashboard
- Métricas del día: **clientes activos**, **membresías por vencer** (5 días o menos), **membresías vencidas** y **asistencias de hoy**.
- Gráfica de dona con el estado de las membresías (Chart.js).
- Bitácora en vivo de los últimos accesos registrados en recepción.

### 👥 Clientes
- Registro, **edición**, búsqueda por nombre o código y eliminación.
- Código único correlativo generado automáticamente (`CG-0001`, `CG-0002`…).
- Estado calculado según la fecha de vencimiento: `ACTIVO`, `POR VENCER` o `VENCIDO`.
- **Código QR** individual para cada cliente, listo para mostrarse en pantalla o guardarse.
- Un cliente con pagos, asistencias o reservas **no se puede eliminar**, para conservar el historial.

### 💳 Pagos y membresías
- Cobro de planes configurables (Mensual, Trimestral, Anual…) con método de pago: Efectivo, Tarjeta o Transferencia.
- **Regla de renovación:** si el cliente paga antes de vencer, los días nuevos se suman desde su fecha de vencimiento actual (no pierde días). Si ya estaba vencido, el nuevo período inicia el día del pago.
- Historial de pagos con el período que cubre cada uno.

### 📷 Control de asistencia con QR
- Escáner con la **cámara web** (html5-qrcode) y opción de ingreso manual del código.
- Validación inmediata: **✅ Bienvenido** si la membresía está vigente, **❌ Membresía vencida** si no.
- Se registran tanto los accesos permitidos como los denegados, con fecha y hora, para auditoría.
- Si el cliente ya registró su entrada ese día, no se duplica el registro.

### 📅 Reservas de clases
- Horario semanal de clases con instructor, hora y cupo máximo.
- Cada reserva valida **5 reglas de negocio**:
  1. La fecha no puede haber pasado.
  2. La fecha debe coincidir con el día de la semana en que se imparte la clase.
  3. El cliente debe tener membresía vigente para esa fecha.
  4. El cliente no puede reservar la misma clase dos veces el mismo día.
  5. No se puede superar el cupo máximo de la clase.
- Listado de **próximas reservas** con opción de **cancelar**.

### ⚙️ Administración (solo Administrador)
- Reporte de **ingresos totales** registrados.
- Creación de **planes** y **clases**.
- Gestión de **usuarios del personal** (crear y eliminar), sin permitir nombres de usuario repetidos.

### 🎨 Extras
- Interfaz con **tema oscuro y acentos amarillos**, diseño responsive con Bootstrap 5.
- **Funciona sin internet**: todas las librerías de estilos, gráficas y QR están incluidas en el proyecto.
- Página de error personalizada.
- **Datos de prueba** automáticos para demostración (15 clientes con pagos y asistencias).

---

## 👤 Permisos por perfil

| Acción | Administrador | Recepción |
|---|:---:|:---:|
| Ver dashboard | ✅ | ✅ |
| Registrar y editar clientes | ✅ | ✅ |
| Eliminar clientes | ✅ | ❌ |
| Registrar pagos | ✅ | ✅ |
| Control de asistencia (QR) | ✅ | ✅ |
| Reservar y cancelar clases | ✅ | ✅ |
| Ver ingresos totales | ✅ | ❌ |
| Crear planes y clases | ✅ | ❌ |
| Crear y eliminar usuarios | ✅ | ❌ |

---

## 🛠️ Tecnologías

| Capa | Tecnología |
|---|---|
| Lenguaje | Java 21 |
| Framework | Spring Boot 4 (Spring Web MVC) |
| Vistas | Thymeleaf |
| Persistencia | Spring Data JPA / Hibernate |
| Base de datos | MySQL / MariaDB (XAMPP) |
| Seguridad | BCrypt (`spring-security-crypto`) + interceptor de roles |
| Interfaz | Bootstrap 5.3 + CSS propio (`coachgym.css`) |
| Gráficas | Chart.js 4 |
| Códigos QR | QRCode.js (generar) y html5-qrcode (escanear) |
| Dependencias | Maven (incluido con `mvnw`) |

---

## 🧱 Arquitectura (MVC en capas)

```text
Navegador ──► Controller ──► Service ──► Repository ──► MySQL
                  │
                  ▼
           Vista Thymeleaf (HTML)
```

- **Model:** entidades JPA que representan las tablas.
- **View:** plantillas HTML con Thymeleaf.
- **Controller:** recibe las peticiones del navegador y decide qué vista mostrar.
- **Service:** reglas de negocio (renovaciones, validación de reservas y de acceso).
- **Repository:** acceso a datos con Spring Data JPA (las consultas se generan a partir del nombre del método).

```text
src/main/java/com/coachgym/coachgym/
├── config/        DataInitializer (datos iniciales), RolInterceptor, WebConfig, SeguridadConfig
├── controller/    AuthController, GymController, OperacionController, AdminController
├── model/         Usuario, Plan, Cliente, Pago, Asistencia, Clase, Reserva
├── repository/    Interfaces de Spring Data JPA
└── service/       ClienteService, PagoService, AsistenciaService, ReservaService

src/main/resources/
├── templates/     login, dashboard, clientes, pagos, asistencia, reservas, admin, error, fragments
├── static/        css (Bootstrap + tema propio) y js (Bootstrap, Chart.js, QR)
└── application.properties
```

---

## 🗄️ Base de datos

Hibernate crea las tablas automáticamente al iniciar el sistema.

| Tabla | Contenido |
|---|---|
| `usuarios` | Usuario, contraseña encriptada y rol (ADMIN / RECEPCION) |
| `planes` | Nombre, precio y duración en días |
| `clientes` | Código, nombre, teléfono, plan actual y fecha de vencimiento |
| `pagos` | Cliente, monto, método, fecha y período cubierto |
| `asistencias` | Cliente, fecha, hora y si se le permitió la entrada |
| `clases` | Nombre, instructor, día, hora y cupo |
| `reservas` | Cliente, clase, fecha y estado (CONFIRMADA / CANCELADA) |

---

## 🚀 Instalación y ejecución

### Requisitos
- [XAMPP](https://www.apachefriends.org/) (MySQL y phpMyAdmin)
- [IntelliJ IDEA](https://www.jetbrains.com/idea/) (u otro IDE con soporte para Maven)
- JDK 21 (IntelliJ puede descargarlo desde *File → Project Structure → SDK → Download JDK*)

### Pasos
1. Abre XAMPP y enciende **Apache** y **MySQL**.
2. Clona el repositorio:
   ```bash
      git clone https://github.com/mark2504-a/CoachGym360.git
   ```
3. Ábrelo en IntelliJ y espera a que Maven descargue las dependencias.
4. Revisa el puerto de MySQL en `src/main/resources/application.properties` (`3306` por defecto en XAMPP; este proyecto viene con `3307`):
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3307/coach_gym?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=America/El_Salvador&allowPublicKeyRetrieval=true
   ```
   La base de datos `coach_gym` se crea sola si no existe.
5. Ejecuta `CoachgymApplication.java`.
6. Abre **http://localhost:8080**

### 🔑 Usuarios de prueba

| Perfil | Usuario | Contraseña |
|---|---|---|
| Administrador | `admin` | `admin123` |
| Recepción | `recepcion` | `recep123` |

Al iniciar por primera vez también se crean 3 planes, 2 clases y 15 clientes de ejemplo.

---

## 🔭 Trabajo futuro
- Integración con pasarela de pagos en línea.
- Aplicación móvil para clientes.
- Recordatorios de vencimiento por WhatsApp o correo.
- Sitio web público con planes, horarios e inscripción en línea.

---

## 👨‍💻 Integrantes

- Jaime Fabricio Aparicio Portillo — AP100216
- Carlos Edenilson Echeverría Leiva — EL100224
- Mark Dylan Hernández Escobar — HE100204
- Josué Daniel Méndez Guillén — MG102606
- Christian Vladimir Reyes Paradas — RP100224
