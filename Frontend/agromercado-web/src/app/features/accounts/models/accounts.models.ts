// src/app/features/accounts/models/accounts.models.ts
export type RegistrarUsuarioRequest = { email: string; nombre: string; };
export type UsuarioResponse = { usuarioId: string; email: string; nombre: string; mensaje: string; };

export type DevTokenRequest = { userId: string; roles: string[]; ttlSeconds?: number };
// ✅ NUEVO
export type DevTokenRes = { access_token: string; token_type: string; expires_in: number };

export type SolicitarAfiliacionRequest = {
  nombreVereda: string;
  municipio: string;
  representanteNombre: string;
  representanteDocumento: string;
  representanteCorreo: string;
};

export type AfiliacionResponse = {
  afiliacionId: string;
  zonaId: string | null;
  mensaje: string;
};

export type DecisionAfiliacionRequest = { observaciones?: string | null };

export type AfiliacionResumen = {
  afiliacionId: string;
  estado: 'PENDIENTE' | 'APROBADA' | 'RECHAZADA';
  zonaId: string;
  solicitanteUsuarioId: string;
  nombreVereda: string;
  municipio: string;
  updatedAt: string;
};
