# AlojamientosHosped

Plataforma web para gestión de alojamientos turísticos. Permite a anfitriones publicar propiedades y a huéspedes buscar, reservar y comentar estadías.

---

## Tecnologías

| Capa | Tecnología |
|---|---|
| Frontend | Angular 17 |
| Backend | Spring Boot 3 · Java 17 |
| Base de datos | MySQL 8 |
| Autenticación | JWT |
| Documentación API | Swagger / OpenAPI 3 |

---

## Requisitos previos

- **Java 17+**
- **Node.js 18+** y **npm**
- **Angular CLI** (`npm install -g @angular/cli`)
- **MySQL 8** corriendo localmente
- **Maven 3.8+** (o usar el wrapper `mvnw` si está disponible)

---

## Configuración de la base de datos

1. Crea la base de datos y el usuario en MySQL:

```sql
CREATE DATABASE alojamientos_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'alojamientos_user'@'localhost' IDENTIFIED BY 'tu_password';
GRANT ALL PRIVILEGES ON alojamientos_db.* TO 'alojamientos_user'@'localhost';
FLUSH PRIVILEGES;
```

2. Edita `src/main/resources/application-dev.properties` con tus credenciales:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/alojamientos_db
spring.datasource.username=alojamientos_user
spring.datasource.password=tu_password
```

> Las tablas se crean automáticamente al iniciar el backend (`ddl-auto=update`).

---

## Ejecución en desarrollo

### Backend (Spring Boot)

```bash
# Desde la carpeta raíz del backend
cd backend

# Con Maven instalado
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# O con el wrapper (si existe)
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

El backend queda disponible en: `http://localhost:8080/alojamientos`

Swagger UI: `http://localhost:8080/alojamientos/swagger-ui.html`

### Frontend (Angular)

```bash
# Desde la carpeta raíz del frontend
cd frontend

# Instalar dependencias (solo la primera vez)
npm install

# Levantar servidor de desarrollo
ng serve
```

El frontend queda disponible en: `http://localhost:4200`

> El frontend apunta al backend en `http://localhost:8080/alojamientos/api` (configurado en `src/environments/environment.ts`).

---

## Variables de entorno sensibles

Estas variables **no deben subirse al repositorio**. Configúralas localmente antes de correr el backend:

| Variable | Descripción |
|---|---|
| `MAIL_USERNAME` | Correo Gmail para envío de notificaciones |
| `MAIL_PASSWORD` | Contraseña de aplicación de Gmail |

En Linux/Mac:
```bash
export MAIL_USERNAME=tu_correo@gmail.com
export MAIL_PASSWORD=tu_app_password
```

En Windows (PowerShell):
```powershell
$env:MAIL_USERNAME="tu_correo@gmail.com"
$env:MAIL_PASSWORD="tu_app_password"
```

---

## Perfiles disponibles

| Perfil | Uso | Activación |
|---|---|---|
| `dev` | Desarrollo local con MySQL local | Por defecto |
| `docker` | Contenedores Docker, variables por env | `--spring.profiles.active=docker` |
| `prod` | Producción | `--spring.profiles.active=prod` |
| `test` | Tests automatizados | Automático con `mvn test` |

---

## Estructura del proyecto

```
backend/
└── src/main/java/com/example/Alojamientos/
    ├── presentationLayer/controller/   # Controladores REST
    ├── businessLayer/
    │   ├── service/                    # Lógica de negocio
    │   └── dto/                        # Data Transfer Objects
    ├── persistenceLayer/
    │   ├── entity/                     # Entidades JPA
    │   ├── repository/                 # Repositorios Spring Data
    │   └── mapper/                     # MapStruct mappers
    ├── securityLayer/                  # JWT, filtros de seguridad
    └── config/                         # Configuración general

frontend/
└── src/app/
    ├── components/ad/
    │   ├── atoms/                      # Componentes base (button, input, star-rating…)
    │   ├── molecules/                  # Componentes compuestos (comentario-card…)
    │   ├── organisms/                  # Secciones completas
    │   ├── templates/                  # Layouts de página
    │   └── pages/                      # Vistas completas (login, detalle, mis-reservas…)
    ├── models/                         # Interfaces TypeScript
    └── services/                       # Servicios HTTP
```

---

## Endpoints principales

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/auth/login` | Iniciar sesión |
| POST | `/api/auth/register` | Registro de usuario |
| GET | `/api/alojamientos` | Listar alojamientos |
| GET | `/api/alojamientos/{id}` | Detalle de alojamiento |
| POST | `/api/reservas` | Crear reserva |
| GET | `/api/comentarios/alojamiento/{id}` | Comentarios de un alojamiento |
| POST | `/api/comentarios` | Crear comentario |
| POST | `/api/respuestas-comentarios` | Responder comentario (anfitrión) |

Documentación completa disponible en Swagger UI una vez levantado el backend.

---

## Ejecución con Docker

> Requiere Docker y Docker Compose instalados.

```bash
# Desde la raíz del proyecto
docker-compose up --build
```

El perfil `docker` usa variables de entorno definidas en el `docker-compose.yml`.

---

## Equipo

Proyecto académico — Ingeniería de Software 3.
