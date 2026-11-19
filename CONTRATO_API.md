# Contrato de la API v1 - Pastelería Mil Sabores

Acuerdo entre backend (`pasteleria-api`) y frontend (`PasteleriaReact`) para la Evaluación Parcial 3.
Si algo de este archivo cambia, se anota primero en **Registro de cambios** (al final) y se avisa al equipo.

| Dato | Valor |
|---|---|
| Versión del contrato | 1.0 |
| URL base (desarrollo) | `http://localhost:8080/api/v1` |
| Swagger | `http://localhost:8080/swagger-ui.html` |
| Frontend permitido (CORS) | `http://localhost:5173` |
| Paquete base Java | `com.pasteleriamilsabores.pasteleria.api` |
| Responsables | José Cardoza (S3), Catalina Díaz (C3), Juan Núñez (J3) |

---

## 1. Convenciones

- Todas las rutas empiezan con `/api/v1`. En la tabla de endpoints se omite ese prefijo.
- JSON en ambos sentidos (`Content-Type: application/json`).
- Nombres de campos en **camelCase**, iguales a los que ya usa el frontend: `codigo`, `nombre`, `precio`, `stock`, `stockCritico`, `imagen`, `categoria`, `run`, `correo`, `rol`.
- Dinero en **pesos chilenos enteros** (`45000`, sin decimales ni puntos).
- Fechas en formato ISO: `2026-11-20` (fecha) y `2026-11-20T15:30:00` (fecha y hora).
- RUN sin puntos ni guion, con dígito verificador: `111111111`, `19011022K`.
- Roles en mayúsculas: `ADMINISTRADOR`, `VENDEDOR`, `CLIENTE`.
- Rutas protegidas: encabezado `Authorization: Bearer <accessToken>`.

### Códigos de respuesta

| Código | Cuándo |
|---|---|
| 200 OK | Lectura o actualización correcta |
| 201 Created | Se creó un recurso (POST) |
| 204 No Content | Se eliminó un recurso (DELETE) |
| 400 Bad Request | Datos inválidos; `campos` trae un mensaje por campo |
| 401 Unauthorized | Sin token, token vencido o inválido, o credenciales incorrectas |
| 403 Forbidden | Hay sesión, pero el rol no tiene permiso |
| 404 Not Found | El recurso no existe |
| 409 Conflict | Choca con los datos actuales: duplicado, sin stock, categoría con productos |

### Formato de error (ErrorApi)

Todos los errores tienen la misma forma. React muestra `error.response.data.mensaje` y, si viene `campos`, pinta cada error bajo su input.

```json
{ "status": 400, "mensaje": "Revisa los datos marcados",
  "campos": { "precio": "El precio debe ser mayor que cero" } }
```
```json
{ "status": 409, "mensaje": "No hay stock suficiente de «Torta Cuadrada de Frutas»: quedan 2 unidades.", "campos": {} }
```

---

## 2. Modelo de datos

| Tabla | Campos y reglas | Relaciones | Dueño |
|---|---|---|---|
| `usuario` | id · run único (7-9, con DV) · nombre máx. 50 · apellidos máx. 100 · correo único, máx. 100, `@duoc.cl`, `@profesor.duoc.cl` o `@gmail.com` · contrasena (hash BCrypt) · fecha_nacimiento opcional, no futura · region · comuna · direccion máx. 300 · rol | 1 usuario → N boletas | José |
| `categoria` | id · slug único · nombre único, máx. 50 | 1 categoría → N productos | Catalina |
| `producto` | id · codigo único, mín. 3 · nombre máx. 100 · descripcion máx. 500 · precio > 0 · stock ≥ 0 · stock_critico ≥ 0 · imagen | N productos → 1 categoría (`categoria_id`) | Catalina |
| `boleta` | id (N° de boleta) · fecha · subtotal · descuento · total · cupon opcional | N boletas → 1 usuario (`usuario_id`) | Juan |
| `detalle_boleta` | id · cantidad ≥ 1 · precio_unitario (copia del precio al comprar) · subtotal | → 1 boleta (`boleta_id`) y → 1 producto (`producto_id`) | Juan |

Las tablas las crea Hibernate a partir de las clases `@Entity` (`ddl-auto=update`). Los datos iniciales los carga `DatosIniciales`.

### Reglas de negocio

1. El precio y el total de una compra los calcula el servidor con los precios de la base de datos, nunca con los que manda el navegador.
2. Una compra descuenta el stock de todas sus líneas o de ninguna (`@Transactional`). Si falta stock: 409.
3. Cupón `FELICES10`: 10 % de descuento sobre el subtotal (no distingue mayúsculas). Un cupón inválido da 400.
4. No se repiten código de producto, RUN, correo ni slug o nombre de categoría (409).
5. No se elimina una categoría con productos, ni un producto que ya aparece en boletas, ni el administrador a sí mismo (409).
6. Stock crítico: `stock <= stockCritico`.
7. Las contraseñas se guardan con BCrypt y **nunca** se devuelven en un JSON.
8. El registro público siempre crea usuarios `CLIENTE`. Solo el administrador crea vendedores y administradores.

---

## 3. Endpoints (24)

| # | Método | Ruta | Acceso | Entrada | Respuestas | Dueño |
|---|---|---|---|---|---|---|
| 1 | POST | `/auth/login` | Pública | LoginRequest | 200 · 400 · 401 | José |
| 2 | POST | `/auth/registro` | Pública | RegistroRequest | 201 · 400 · 409 | José |
| 3 | POST | `/auth/refresh` | Pública | RefreshRequest | 200 · 401 | José |
| 4 | GET | `/auth/me` | Con sesión | - | 200 · 401 | José |
| 5 | GET | `/usuarios?rol=` | ADMINISTRADOR | - | 200 · 401 · 403 | José |
| 6 | GET | `/usuarios/{run}` | ADMINISTRADOR | - | 200 · 404 | José |
| 7 | POST | `/usuarios` | ADMINISTRADOR | UsuarioRequest | 201 · 400 · 409 | José |
| 8 | PUT | `/usuarios/{run}` | ADMINISTRADOR | UsuarioRequest | 200 · 400 · 404 · 409 | José |
| 9 | DELETE | `/usuarios/{run}` | ADMINISTRADOR | - | 204 · 404 · 409 | José |
| 10 | GET | `/categorias` | Pública | - | 200 | Catalina |
| 11 | GET | `/categorias/{id}` | Pública | - | 200 · 404 | Catalina |
| 12 | POST | `/categorias` | ADMINISTRADOR | CategoriaRequest | 201 · 400 · 409 | Catalina |
| 13 | PUT | `/categorias/{id}` | ADMINISTRADOR | CategoriaRequest | 200 · 400 · 404 · 409 | Catalina |
| 14 | DELETE | `/categorias/{id}` | ADMINISTRADOR | - | 204 · 404 · 409 | Catalina |
| 15 | GET | `/productos?categoria=` | Pública | - | 200 | Catalina |
| 16 | GET | `/productos/{codigo}` | Pública | - | 200 · 404 | Catalina |
| 17 | GET | `/productos/stock-critico` | ADMINISTRADOR, VENDEDOR | - | 200 · 401 · 403 | Catalina |
| 18 | POST | `/productos` | ADMINISTRADOR | ProductoRequest | 201 · 400 · 409 | Catalina |
| 19 | PUT | `/productos/{codigo}` | ADMINISTRADOR | ProductoRequest | 200 · 400 · 404 | Catalina |
| 20 | DELETE | `/productos/{codigo}` | ADMINISTRADOR | - | 204 · 404 · 409 | Catalina |
| 21 | POST | `/boletas` | CLIENTE | BoletaRequest | 201 · 400 · 401 · 403 · 404 · 409 | Juan |
| 22 | GET | `/boletas/mias` | CLIENTE | - | 200 · 401 | Juan |
| 23 | GET | `/boletas` | ADMINISTRADOR, VENDEDOR | - | 200 · 401 · 403 | Juan |
| 24 | GET | `/boletas/{id}` | ADMINISTRADOR, VENDEDOR o el cliente dueño | - | 200 · 403 · 404 | Juan |

Cada dueño programa sus endpoints, los documenta en Swagger (`@Tag` y `@Operation`), escribe sus pruebas y entrega sus filas para la planilla de APIs (Anexo 3).

---

## 4. JSON de cada recurso

### 4.1 Autenticación (José)

**LoginRequest** - `POST /auth/login`
```json
{ "correo": "admin@gmail.com", "contrasena": "admin123" }
```

**RegistroRequest** - `POST /auth/registro` (siempre crea un CLIENTE)
```json
{
  "run": "19011022K", "nombre": "Ana", "apellidos": "Pérez Soto",
  "correo": "ana@gmail.com", "contrasena": "ana1234",
  "fechaNacimiento": "2000-05-14",
  "region": "Región Metropolitana de Santiago", "comuna": "Melipilla",
  "direccion": "Av. Ortúzar 123"
}
```

**RefreshRequest** - `POST /auth/refresh`
```json
{ "refreshToken": "eyJhbGciOiJIUzI1NiJ9..." }
```

**AuthResponse** - respuesta de login (200), registro (201) y refresh (200)
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tipo": "Bearer",
  "expiraEn": 900,
  "usuario": {
    "run": "111111111", "nombre": "Administrador", "apellidos": "Mil Sabores",
    "correo": "admin@gmail.com", "fechaNacimiento": null,
    "region": "Región del Biobío", "comuna": "Concepción",
    "direccion": "Av. San Martín 1234", "rol": "ADMINISTRADOR"
  }
}
```
- `expiraEn` está en segundos (900 = 15 minutos).
- El access token dura **15 minutos**. El refresh token dura **7 días** y solo sirve en `/auth/refresh`.
- Login con credenciales incorrectas: `401 { "mensaje": "Correo o contraseña incorrectos" }`.

### 4.2 Usuarios (José, solo ADMINISTRADOR)

**UsuarioDTO** - lo que devuelve la API (nunca incluye la contraseña)
```json
{
  "run": "222222222", "nombre": "Vendedor", "apellidos": "Mil Sabores",
  "correo": "vendedor@gmail.com", "fechaNacimiento": null,
  "region": "Región del Biobío", "comuna": "Concepción",
  "direccion": "Av. San Martín 1234", "rol": "VENDEDOR"
}
```

**UsuarioRequest** - `POST /usuarios` y `PUT /usuarios/{run}`
```json
{
  "run": "222222222", "nombre": "Vendedor", "apellidos": "Mil Sabores",
  "correo": "vendedor@gmail.com", "contrasena": "vend123",
  "fechaNacimiento": null, "region": "Región del Biobío", "comuna": "Concepción",
  "direccion": "Av. San Martín 1234", "rol": "VENDEDOR"
}
```
- En el PUT el RUN no cambia (se toma de la URL) y `contrasena` es opcional: si viene vacía, se mantiene la actual.
- La contraseña debe tener entre 4 y 10 caracteres, igual que en el frontend.

### 4.3 Categorías (Catalina)

**Categoria** - respuesta
```json
{ "id": 1, "slug": "cuadradas", "nombre": "Tortas Cuadradas" }
```

**CategoriaRequest** - `POST` y `PUT`
```json
{ "slug": "cuadradas", "nombre": "Tortas Cuadradas" }
```

Categorías iniciales (las mismas del catálogo de la Ev2):

| slug | nombre |
|---|---|
| cuadradas | Tortas Cuadradas |
| circulares | Tortas Circulares |
| individuales | Postres Individuales |
| sin-azucar | Sin Azúcar |
| tradicional | Tradicional |
| sin-gluten | Sin Gluten |
| veganos | Veganos |
| especiales | Tortas Especiales |

### 4.4 Productos (Catalina)

**ProductoDTO** - respuesta
```json
{
  "id": 1, "codigo": "TC001", "nombre": "Torta Cuadrada de Chocolate",
  "descripcion": "Deliciosa torta de chocolate con capas de ganache y un toque de avellanas.",
  "precio": 45000, "stock": 10, "stockCritico": 3,
  "imagen": "/img/Torta Cuadrada de Chocolate.png",
  "categoria": "cuadradas", "categoriaNombre": "Tortas Cuadradas"
}
```

**ProductoRequest** - `POST` y `PUT` (`categoria` es el slug)
```json
{
  "codigo": "TC001", "nombre": "Torta Cuadrada de Chocolate",
  "descripcion": "Deliciosa torta de chocolate...",
  "precio": 45000, "stock": 10, "stockCritico": 3,
  "imagen": "/img/Torta Cuadrada de Chocolate.png", "categoria": "cuadradas"
}
```
- Filtro: `GET /productos?categoria=cuadradas`. Sin el parámetro devuelve todos.
- Los 16 productos iniciales son los de `src/datos/productos.js` del frontend (TC001, TC002, TT001, TT002, PI001, PI002, PSA001, PSA002, PT001, PT002, PG001, PG002, PV001, PV002, TE001, TE002).
- Las imágenes se quedan en `public/img` del frontend; la API solo guarda la ruta.

### 4.5 Boletas (Juan)

**BoletaRequest** - `POST /boletas` (el precio **no** viaja)
```json
{ "lineas": [ { "codigo": "TC001", "cantidad": 2 },
              { "codigo": "PI001", "cantidad": 1 } ],
  "cupon": "FELICES10" }
```

**BoletaResponse** - respuesta de `POST` (201), `GET /boletas/{id}` y cada elemento de las listas
```json
{
  "id": 15, "fecha": "2026-11-20T15:30:00",
  "cliente": { "run": "333333333", "nombre": "Cliente", "apellidos": "Mil Sabores", "correo": "cliente@gmail.com" },
  "unidades": 3,
  "subtotal": 95000, "descuento": 9500, "total": 85500, "cupon": "FELICES10",
  "detalle": [
    { "codigo": "TC001", "nombre": "Torta Cuadrada de Chocolate", "cantidad": 2, "precioUnitario": 45000, "subtotal": 90000 },
    { "codigo": "PI001", "nombre": "Mousse de Chocolate", "cantidad": 1, "precioUnitario": 5000, "subtotal": 5000 }
  ]
}
```
- La boleta se asocia al usuario del token, nunca a un id que mande el navegador.
- `GET /boletas` devuelve las boletas de la más nueva a la más antigua.
- Un cliente que pide una boleta ajena recibe 403.

---

## 5. Cuentas de demostración

Las carga `DatosIniciales` con la contraseña cifrada (BCrypt). Son solo para la demo.

| Rol | Correo | Contraseña | RUN |
|---|---|---|---|
| ADMINISTRADOR | admin@gmail.com | admin123 | 111111111 |
| VENDEDOR | vendedor@gmail.com | vend123 | 222222222 |
| CLIENTE | cliente@gmail.com | cliente123 | 333333333 |

Las dos primeras son las mismas de la Ev2. La cuenta de cliente es nueva.

---

## 6. Roles en el frontend

| Ruta | Visitante | Cliente | Vendedor | Administrador |
|---|---|---|---|---|
| Tienda: `/`, `/productos`, `/producto/:codigo`, `/blogs`, `/nosotros`, `/contacto` | Sí | Sí | Sí | Sí |
| `/carrito` (ver y modificar) | Sí | Sí | Sí | Sí |
| Pagar en `/carrito` | Va a /login | Sí | No (aviso) | No (aviso) |
| `/mis-compras` | Va a /login | Sí | No | No |
| `/admin` (resumen) | Va a /login | 403 | Va a /admin/productos | Sí |
| `/admin/productos` (lista y detalle) | Va a /login | 403 | Sí, solo lectura | Sí |
| `/admin/productos/nuevo` y `/:codigo/editar` | Va a /login | 403 | 403 | Sí |
| `/admin/categorias` | Va a /login | 403 | 403 | Sí |
| `/admin/ordenes` y `/admin/ordenes/:id` | Va a /login | 403 | Sí | Sí |
| `/admin/usuarios`, `/nuevo`, `/:run`, `/:run/editar` | Va a /login | 403 | 403 | Sí |

- El vendedor solo ve **Productos** y **Órdenes** en el panel, sin los botones Nuevo, Editar ni Eliminar (Anexo 1: "todos los demás accesos no deben aparecer en la vista del vendedor").
- El frontend oculta y el backend bloquea: aunque alguien escriba la URL o cambie su rol en localStorage, la API responde 403.
- En el frontend, el campo `tipoUsuario` ('Administrador', 'Vendedor', 'Cliente') pasa a ser `rol` ('ADMINISTRADOR', 'VENDEDOR', 'CLIENTE'), con las constantes de `src/utils/roles.js`.

### Sesión en el navegador

Se guarda en `localStorage`, clave `sesion`:
```json
{ "accessToken": "...", "refreshToken": "...", "usuario": { "run": "...", "nombre": "...", "rol": "CLIENTE" } }
```
Nunca se guarda la contraseña. Ante un 401, axios intenta una vez `POST /auth/refresh`. Si falla, borra la sesión y lleva a `/login`.

---

## 7. Registro de cambios

| Fecha | Versión | Cambio | Quién |
|---|---|---|---|
| 19-11-2026 | 1.0 | Contrato inicial | José Cardoza |
