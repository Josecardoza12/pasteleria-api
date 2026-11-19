# pasteleria-api

API REST de Pastelería Mil Sabores (DSY1104 Desarrollo Fullstack II - Evaluación Parcial 3).

Spring Boot 4.1 · Java 21 · MySQL · Spring Security + JWT · Swagger (springdoc)

## Requisitos

- Java 21 (IntelliJ IDEA con Maven en "Bundled (Maven 3)")
- MySQL en `localhost:3306` (Laragon o XAMPP), usuario `root` sin contraseña

## Cómo levantar la API

1. Encender MySQL en Laragon o XAMPP.
2. Abrir el proyecto en IntelliJ y ejecutar `PasteleriaApiApplication`.
3. La base `pasteleria_db` y sus tablas se crean solas al arrancar.

| Recurso | URL |
|---|---|
| API | http://localhost:8080/api/v1 |
| Swagger | http://localhost:8080/swagger-ui.html |

Si tu MySQL tiene contraseña, crea la variable de entorno `DB_PASSWORD` (y `DB_USER` si no usas `root`).

## Pruebas

```
.\mvnw.cmd test
```

Las pruebas usan H2 en memoria, así que no necesitan MySQL encendido.

## Estructura

Paquete base: `com.pasteleriamilsabores.pasteleria.api`

| Paquete | Contenido |
|---|---|
| model | Entidades JPA (cada @Entity es una tabla) |
| repository | Repositorios JpaRepository |
| dto | Objetos que entran y salen como JSON |
| service | Lógica de negocio |
| controller | Controladores REST bajo /api/v1 |
| security | Spring Security, JWT y roles |
| exception | Manejo global de errores (ErrorApi) |
| config | Swagger y datos iniciales |

## Equipo

- José Cardoza Miranda
- Juan Núñez Sánchez
- Catalina Díaz Moraga
