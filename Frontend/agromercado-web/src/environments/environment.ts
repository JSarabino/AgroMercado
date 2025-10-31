export const environment = {
  production: false,
  apiBase: 'http://localhost:8081',
  // headers multi-tenant derivados del JWT en backend;
  // para llamadas directas puedes forzar header si lo necesitas:
  defaultTenantHeader: 'ZONA-d7585f1a-b6a9-4167-a47d-956209eda6b5'
};