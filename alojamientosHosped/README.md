# AlojamientosHosped

This project was generated using [Angular CLI](https://github.com/angular/angular-cli) version 20.3.7.

## Development server

To start a local development server, run:

```bash
ng serve
```

Once the server is running, open your browser and navigate to `http://localhost:4200/`. The application will automatically reload whenever you modify any of the source files.

## Code scaffolding

Angular CLI includes powerful code scaffolding tools. To generate a new component, run:

```bash
ng generate component component-name
```

For a complete list of available schematics (such as `components`, `directives`, or `pipes`), run:

```bash
ng generate --help
```

## Building

To build the project run:

```bash
ng build
```

This will compile your project and store the build artifacts in the `dist/` directory. By default, the production build optimizes your application for performance and speed.

## Running unit tests

To execute unit tests with the [Karma](https://karma-runner.github.io) test runner, use the following command:

```bash
ng test
```

## Running end-to-end tests (ALOJ-16 — Cypress)

Los tests E2E del CRUD de alojamientos están implementados con Cypress.

### Requisitos

- Backend corriendo en `http://localhost:8080` (`./gradlew bootRun`)
- Frontend corriendo en `http://localhost:4200` (`ng serve`)

### Instalación

```bash
npm install --save-dev cypress
```

### Credenciales de prueba

Configuradas en `cypress.config.ts`:
- Email: `juanjo@gmail.com`
- Password: `Juanjo123!`

### Ejecutar los tests

```bash
# Modo visual (recomendado)
npx cypress open

# Modo headless
npx cypress run

# Solo CRUD alojamientos
npx cypress run --spec cypress/e2e/alojamiento-crud.cy.ts
```

### Tests incluidos (17 tests)

| Suite | Descripción |
|-------|-------------|
| Crear | Validaciones, creación via API, verificar en lista |
| Leer  | Listado, detalle, filtro, paginación, botones del dueño |
| Editar | Precarga datos, actualizar campos, validaciones, desde panel |
| Eliminar | Modal, cancelar, soft delete, toast, acceso post-delete |
| Guards | Protección de rutas sin sesión |

## Additional Resources

For more information on using the Angular CLI, including detailed command references, visit the [Angular CLI Overview and Command Reference](https://angular.dev/tools/cli) page.