// cypress/support/e2e.ts
// Archivo de configuración global para todos los tests E2E

import './Commands';

// Ignorar errores de Angular en consola que no afectan los tests
Cypress.on('uncaught:exception', (err) => {
  // Ignorar errores de Angular/RxJS que no son relevantes para los E2E
  if (
    err.message.includes('ResizeObserver') ||
    err.message.includes('NG0') ||
    err.message.includes('ExpressionChangedAfterItHasBeenCheckedError')
  ) {
    return false;
  }
  return true;
});