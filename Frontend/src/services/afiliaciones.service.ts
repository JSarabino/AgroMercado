import apiService from './api.service';
import { API_ENDPOINTS } from '../config/api.config';

export interface SolicitarAfiliacionRequest {
  nombreVereda: string;
  municipio: string;
  representanteNombre: string;
  representanteDocumento: string;
  representanteCorreo: string;
}

export interface AfiliacionResponse {
  afiliacionId: string;
  zonaId: string | null;
  mensaje: string;
}

export interface DecisionAfiliacionRequest {
  observaciones?: string | null;
}

export interface AfiliacionResumen {
  afiliacionId: string;
  estado: 'PENDIENTE' | 'APROBADA' | 'RECHAZADA';
  zonaId: string;
  solicitanteUsuarioId: string;
  nombreVereda: string;
  municipio: string;
  updatedAt: string;
}

class AfiliacionesService {
  /**
   * Solicita una nueva afiliación a una zona
   */
  async solicitarAfiliacion(data: SolicitarAfiliacionRequest): Promise<AfiliacionResponse> {
    return await apiService.post<AfiliacionResponse>(
      API_ENDPOINTS.AFILIACIONES_SOLICITAR,
      data
    );
  }

  /**
   * Aprueba una solicitud de afiliación (requiere rol ADMIN_ZONA o ADMIN_GLOBAL)
   */
  async aprobarAfiliacion(
    afiliacionId: string,
    observaciones?: string
  ): Promise<void> {
    await apiService.patch<void>(
      API_ENDPOINTS.AFILIACIONES_APROBAR(afiliacionId),
      { observaciones } as DecisionAfiliacionRequest
    );
  }

  /**
   * Rechaza una solicitud de afiliación (requiere rol ADMIN_ZONA o ADMIN_GLOBAL)
   */
  async rechazarAfiliacion(
    afiliacionId: string,
    observaciones?: string
  ): Promise<void> {
    await apiService.patch<void>(
      API_ENDPOINTS.AFILIACIONES_RECHAZAR(afiliacionId),
      { observaciones } as DecisionAfiliacionRequest
    );
  }

  /**
   * Lista las afiliaciones de una zona específica
   */
  async listarAfiliacionesZona(zonaId: string): Promise<AfiliacionResumen[]> {
    return await apiService.get<AfiliacionResumen[]>(
      `${API_ENDPOINTS.AFILIACIONES_QUERY}?zonaId=${encodeURIComponent(zonaId)}`
    );
  }
}

export default new AfiliacionesService();
