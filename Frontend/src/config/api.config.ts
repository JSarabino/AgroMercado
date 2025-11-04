// Configuración de API y endpoints
export const API_CONFIG = {
  BASE_URL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  GATEWAY_URL: import.meta.env.VITE_GATEWAY_URL || 'http://localhost:8080',
  DEFAULT_TENANT: 'ZONA-d7585f1a-b6a9-4167-a47d-956209eda6b5',
  TOKEN_KEY: 'access_token',
  USER_KEY: 'user_data',
};

export const API_ENDPOINTS = {
  // Auth
  AUTH_DEV_TOKEN: '/auth/dev/token',
  AUTH_ME: '/auth/me',

  // Usuarios
  USUARIOS: '/cmd/usuarios',
  USUARIOS_BY_ID: (id: string) => `/cmd/usuarios/${id}`,
  USUARIOS_ROLES: (id: string) => `/cmd/usuarios/${id}/roles-globales`,
  USUARIOS_MEMBRESIAS: (id: string) => `/cmd/usuarios/${id}/membresias`,

  // Afiliaciones de Zona (Líder → Admin Global)
  AFILIACIONES_SOLICITAR: '/cmd/afiliaciones/solicitar',
  AFILIACIONES_APROBAR: (id: string) => `/cmd/afiliaciones/${id}/aprobar`,
  AFILIACIONES_RECHAZAR: (id: string) => `/cmd/afiliaciones/${id}/rechazar`,
  AFILIACIONES_QUERY: '/qry/afiliaciones',

  // Solicitudes de Productor a Zona (Productor → Admin Zona)
  SOLICITUDES_PRODUCTOR_SOLICITAR: '/cmd/solicitudes-productor/solicitar',
  SOLICITUDES_PRODUCTOR_APROBAR: (id: string) => `/cmd/solicitudes-productor/${id}/aprobar`,
  SOLICITUDES_PRODUCTOR_RECHAZAR: (id: string) => `/cmd/solicitudes-productor/${id}/rechazar`,
  SOLICITUDES_PRODUCTOR_QUERY: '/qry/solicitudes-productor',
  SOLICITUDES_PRODUCTOR_ZONA_PENDIENTES: (zonaId: string) => `/qry/solicitudes-productor/zona/${zonaId}/pendientes`,

  // Productos (a través del microservicio productos-service)
  PRODUCTOS: '/productos',
  PRODUCTOS_BY_ID: (id: number) => `/productos/${id}`,
  PRODUCTOS_PRODUCTOR: (idProductor: string) => `/productos/productor/${idProductor}`,
  PRODUCTOS_ZONA: (zonaId: string) => `/productos/zona/${zonaId}`,
  PRODUCTOS_DISPONIBLES: '/productos/disponibles',
  PRODUCTOS_DISPONIBILIDAD: (id: number) => `/productos/${id}/disponibilidad`,
};
