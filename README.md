# PuenteAuto – Inventario automotriz

Aplicación móvil Android para gestionar el inventario de un taller mecánico automotriz (caso **Gestión móvil de inventario, ventas y reposición de productos automotrices**).
Esta entrega corresponde a la **Evaluación Parcial 2** de **DSY1105 – Desarrollo de Aplicaciones Móviles** (sección 006D).

> Todos los datos de la aplicación son **ficticios**. No se almacenan datos reales de clientes ni de proveedores.

## Integrantes

| Nombre |
|---|---|
| Dario Corona|
| Sebastian Painemilla |
| D Einstein Sanon |

- Equipo: **Grupo 14**
- Docente: Vicente Zapata

## Funcionalidades implementadas

**Acceso**
- Inicio de sesión con una cuenta ficticia, con validación y mensaje de error.
- Menú principal con navegación hacia Productos y cierre de sesión.

**Productos (CRUD con Room)**
- Registrar y modificar productos con: código, nombre, categoría, cantidad y stock mínimo.
- Validaciones por campo con mensaje, ícono de error y animación: campos obligatorios, números enteros mayores o iguales a 0 y código único.
- Eliminar con confirmación y opción **Deshacer**.
- Los datos se conservan al cerrar y reabrir la app (persistencia local con Room).

**Consulta**
- Resumen con el total de productos y los que tienen stock bajo.
- Búsqueda por nombre o código, filtro por categoría y filtro "Solo stock bajo".
- Indicador visual de stock bajo (color, ícono y texto, sin depender solo del color).

**Recursos nativos del dispositivo**
1. **Cámara (CameraX + ML Kit):** lectura de códigos QR. Se puede escanear desde la lista para buscar un producto (si no existe, ofrece registrarlo con el código cargado) y desde el formulario para completar el campo código. Solicita el permiso de cámara en tiempo de ejecución y informa si se rechaza.
2. **Cliente de correo:** desde la lista, el botón de correo prepara un borrador de **solicitud de reposición** con los productos bajo el mínimo y la cantidad sugerida. El usuario puede revisar y modificar destinatario, asunto y mensaje antes de abrir su app de correo. La app **no envía nada automáticamente**, no pide permisos y no guarda credenciales.

**Animaciones**
- Transiciones deslizantes entre pantallas.
- Los productos se mueven y aparecen de forma animada al agregar, filtrar o eliminar.
- Cambio animado de color en las tarjetas cuando un producto pasa a stock bajo.
- Contadores animados en el resumen.
- Fundido suave en los mensajes de error de los formularios.

## Regla de reposición (académica)

- Un producto está en **stock bajo** cuando `cantidad <= stock mínimo`.
- **Existencia objetivo** = 2 × stock mínimo.
- **Cantidad sugerida** = existencia objetivo − cantidad disponible (mínimo 1 unidad).

## Arquitectura

Patrón **MVVM** con estado reactivo (`StateFlow` + `UiState`). Las validaciones están en los ViewModel, no en los composables.

```
app/src/main/java/com/example/dsy1105_006d_lc7/
├── data/
│   ├── local/        Entity, DAO y base de datos Room
│   ├── model/        Modelos simples (credenciales ficticias)
│   └── repository/   Repositorios (Room y autenticación)
├── ui/
│   ├── login/        Pantalla, ViewModel y UiState del login
│   ├── menu/         Menú principal
│   ├── producto/     Lista, formulario, ViewModel y UiState
│   ├── qr/           Pantallas y ViewModel del lector QR
│   ├── navigation/   NavHost y transiciones
│   └── theme/        Colores, tipografía y tema
└── utils/            Cámara, lector QR y apertura del correo
```

**Tecnologías:** Kotlin 2.0.21, Jetpack Compose (BOM 2024.09.00), Material Design 3, Navigation Compose 2.7.7, Room 2.6.1 (KSP), CameraX 1.4.2, ML Kit Barcode Scanning 17.3.0.

## Cómo ejecutar

1. Clonar el repositorio:
   ```
   git clone https://github.com/Dario-gib/inventario-automotriz-grupo14.git
   ```
2. Abrir la carpeta en **Android Studio** y esperar el *Gradle Sync*.
3. Si aparece un error de JDK, ir a *Settings → Build Tools → Gradle → Gradle JDK* y elegir **JDK 21** (jbr-21).
4. Conectar un celular Android (con depuración USB) o iniciar un emulador, y presionar **Run**.
   Requiere Android 7.0 (API 24) o superior.
5. Iniciar sesión con la cuenta ficticia:
   - Usuario: `admin`
   - Contraseña: `123`

**Para probar el lector QR:** generar códigos QR cuyo contenido sea el código de un producto registrado (por ejemplo `FRE-001`) con cualquier generador online y escanearlos desde la app.

## Alcance

Este proyecto es un MVP académico. No realiza facturación, pagos ni envío automático de pedidos, y no se conecta con sistemas externos.
