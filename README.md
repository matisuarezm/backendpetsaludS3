# Backend Clínica PetSalud – Semana 2 (JWT)

Backend de la Clínica Veterinaria **PetSalud** desarrollado para la asignatura **Desarrollo Backend II (PBY2202)** de Duoc UC.

En esta etapa se reemplazó la autenticación HTTP Basic de la semana 1 por **autenticación sin estado con JWT (JSON Web Token)** y se implementó **autorización basada en roles** con Spring Security.

---

## Tecnologías

| Tecnología | Versión |
|---|---|
| Java | 17 o superior |
| Spring Boot | 3.3.6 |
| Spring Security | 6.3 |
| Spring Data JPA / Hibernate | 6.5 |
| jjwt (Java JWT) | 0.11.5 |
| MySQL | 8 |
| Lombok | — |
| Maven (wrapper incluido) | — |

---

## Requisitos previos

- Java 17 o superior (`java -version`)
- MySQL 8 en ejecución (instalado localmente o en Docker)
- Postman (u otro cliente HTTP) para probar los endpoints

---

## Instalación y ejecución

### 1. Clonar el repositorio

```bash
git clone https://github.com/matisuarezm/backendpetsaludS2.git
cd backendpetsaludS2
```

### 2. Crear la base de datos y el usuario en MySQL

```sql
CREATE DATABASE petsalud_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'petsalud_user' IDENTIFIED BY 'tu_clave';
GRANT ALL PRIVILEGES ON petsalud_db.* TO 'petsalud_user';
FLUSH PRIVILEGES;
```

Las tablas se crean automáticamente al iniciar la aplicación (`spring.jpa.hibernate.ddl-auto=update`).

### 3. Configurar `src/main/resources/application.properties`

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/petsalud_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=petsalud_user
spring.datasource.password=tu_clave

# Clave secreta para firmar los tokens (Base64, mínimo 256 bits)
jwt.secret=tu_clave_secreta_en_base64
```

> En un entorno real, la contraseña de la base de datos y `jwt.secret` deben definirse como variables de entorno y no subirse al repositorio.

### 4. Ejecutar

```bash
# Windows
.\mvnw clean spring-boot:run

# Mac / Linux
chmod +x mvnw
./mvnw clean spring-boot:run
```

La API queda disponible en `http://localhost:8080`.

---

## Cómo funciona la autenticación

1. **Registro:** el usuario crea su cuenta en `POST /api/usuarios/registro`. La contraseña se guarda cifrada con **BCrypt** y el rol asignado siempre es `CLIENTE`.
2. **Login:** el usuario envía email y contraseña a `POST /auth/login` y recibe un **token JWT** firmado (HS256), válido por **1 hora**.
3. **Peticiones protegidas:** el cliente envía el token en el header:
   ```
   Authorization: Bearer <token>
   ```
   El filtro `JwtAuthFilter` valida la firma y la expiración, carga el usuario y su rol desde la base de datos y permite o rechaza el acceso según las reglas de `SecurityConfig`.

| Respuesta | Significado |
|---|---|
| `200 OK` | Token válido y el rol tiene permiso |
| `403 Forbidden` | Sin token, token inválido o el rol no tiene permiso |

---

## Roles y permisos

| Rol | Descripción | Acceso |
|---|---|---|
| `CLIENTE` | Dueño de la mascota | Catálogo de productos, citas, mascotas, compras, notificaciones y recompensas propias |
| `EMPLEADO` | Personal clínico | Lo anterior + expedientes médicos y gestión del estado de las citas |
| `GERENTE` | Administración | Acceso total: usuarios, productos e inventario, reportes de ventas, notificaciones, recompensas y eliminaciones |

---

## Endpoints principales

### Públicos

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/usuarios/registro` | Registrar un usuario (rol CLIENTE) |
| POST | `/auth/login` | Iniciar sesión y obtener el token |

### Protegidos (requieren token)

| Método | Ruta | Roles |
|---|---|---|
| GET | `/api/usuarios` | GERENTE |
| DELETE | `/api/**` | GERENTE |
| POST / PUT | `/api/productos/**` | GERENTE |
| GET | `/api/productos/stock-bajo` | GERENTE |
| GET | `/api/ventas` | GERENTE |
| POST | `/api/notificaciones/**`, `/api/recompensas/**` | GERENTE |
| ALL | `/api/expedientes/**` | EMPLEADO, GERENTE |
| PUT | `/api/citas/{id}/estado` | EMPLEADO, GERENTE |
| GET | `/api/citas/veterinario/**`, `/api/citas/entre-fechas` | EMPLEADO, GERENTE |
| — | Resto de endpoints | Cualquier usuario autenticado |

---

## Ejemplo de uso con Postman

**1. Registro**

```http
POST http://localhost:8080/api/usuarios/registro
Content-Type: application/json

{
  "email": "cliente@petsalud.cl",
  "password": "Cliente123",
  "nombre": "Juan",
  "apellido": "Cliente"
}
```

**2. Login**

```http
POST http://localhost:8080/auth/login
Content-Type: application/json

{
  "email": "cliente@petsalud.cl",
  "password": "Cliente123"
}
```

Respuesta:

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

**3. Petición protegida**

En Postman: pestaña **Authorization → Bearer Token** y pegar el token.

```http
GET http://localhost:8080/api/productos
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

> Para probar los roles EMPLEADO y GERENTE, cambiar el rol del usuario directamente en la base de datos:
> ```sql
> UPDATE petsalud_db.usuarios SET rol = 'GERENTE' WHERE email = 'gerente@petsalud.cl';
> ```

---

## Estructura del proyecto

```
src/main/java/com/petsalud/backendpetsalud/
├── controller/   # Endpoints REST (incluye AuthController para el login)
├── dto/          # LoginRequest y AuthResponse
├── entity/       # Entidades JPA y enum Rol
├── jwt/          # JwtServices (crear/validar tokens) y JwtAuthFilter
├── repository/   # Repositorios Spring Data JPA
├── security/     # SecurityConfig, UsuarioDetails y UsuarioDetailsService
└── service/      # Lógica de negocio
```

---

## Medidas de seguridad implementadas

- Autenticación sin estado con JWT firmado (HS256) y expiración de 1 hora.
- Clave secreta de firma fuera del código Java (`application.properties`).
- Contraseñas cifradas con BCrypt.
- Rol `CLIENTE` forzado en el registro público para evitar escalamiento de privilegios.
- Autorización por rol y método HTTP en `SecurityConfig`.
- Sesiones deshabilitadas (`STATELESS`) y CSRF deshabilitado al no usar cookies.
- El token solo contiene el email y las fechas; no incluye datos sensibles.

## Mejoras futuras

- Responder `401` con un mensaje claro cuando el token esté vencido o alterado.
- HTTPS obligatorio en producción.
- Refresh tokens.
- `@PreAuthorize` para que cada cliente solo acceda a sus propios datos.

---

**Autor:** Matías Suárez – Duoc UC Online, Analista Programador
