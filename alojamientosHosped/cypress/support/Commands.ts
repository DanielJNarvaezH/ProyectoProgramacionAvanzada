// cypress/support/commands.ts
// Comandos personalizados para los tests E2E de Hosped

/**
 * cy.loginAnfitrion() — Inicia sesión como anfitrión y guarda el token en localStorage.
 * Usa la API directamente para evitar depender de la UI del login en cada test.
 */
Cypress.Commands.add('loginAnfitrion', () => {
  const email    = Cypress.env('anfitrion_email');
  const password = Cypress.env('anfitrion_password');
  const apiUrl   = Cypress.env('apiUrl');

  cy.request({
    method: 'POST',
    url: `${apiUrl}/auth/login`,
    body: { email, password },
    failOnStatusCode: true,
  }).then((response) => {
    const { token, userId, name, email: correo, rol } = response.body;

    // Guardar en localStorage igual que hace AuthService.guardarSesion()
    window.localStorage.setItem('hosped_token', token);
    window.localStorage.setItem('hosped_user', JSON.stringify({
      id:    userId,
      name:  name,
      email: correo,
      role:  rol,
    }));
  });
});

/**
 * cy.logout() — Limpia el localStorage de sesión.
 */
Cypress.Commands.add('logout', () => {
  window.localStorage.removeItem('hosped_token');
  window.localStorage.removeItem('hosped_user');
  window.localStorage.removeItem('hosped_refresh_token');
});

// TypeScript: declarar los comandos para que el compilador los reconozca
declare global {
  namespace Cypress {
    interface Chainable {
      loginAnfitrion(): Chainable<void>;
      logout(): Chainable<void>;
    }
  }
}

export {};