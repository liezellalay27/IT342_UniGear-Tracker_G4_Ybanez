describe('Auth pages smoke', () => {
  it('loads login page and shows form controls', () => {
    cy.visit('/login');
    cy.contains('Welcome Back');
    cy.get('input[name="email"]').should('exist');
    cy.get('input[name="password"]').should('exist');
    cy.contains('button', 'Login').should('exist');
  });

  it('loads register page', () => {
    cy.visit('/register');
    cy.contains(/register|create/i);
  });
});
