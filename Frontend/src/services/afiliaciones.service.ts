import apiService from './api.service';
import { API_ENDPOINTS } from '../config/api.config';

export interface SolicitarAfiliacionRequest {
  nombreVereda: string;
  municipio: string;
  telefono?: string;
  correo?: string;
  representanteNombre: string;
  representanteDocumento: string;
  representanteCorreo: string;
}

export interface SolicitarAfiliacionProductorRequest {
  zonaId: string;
  nombreProductor: string;
  documento: string;
  telefono?: string;
  correo: string;
  direccion?: string;
  tipoProductos?: string;
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
   * Solicita una nueva afiliación a una zona (Admin Zona solicita crear zona)
   */
  async solicitarAfiliacion(data: SolicitarAfiliacionRequest): Promise<AfiliacionResponse> {
    return await apiService.post<AfiliacionResponse>(
      API_ENDPOINTS.AFILIACIONES_SOLICITAR,
      data
    );
  }

  /**
   * Solicita afiliación de productor a una zona existente
   */
  async solicitarAfiliacionProductor(data: SolicitarAfiliacionProductorRequest): Promise<AfiliacionResponse> {
    return await apiService.post<AfiliacionResponse>(
      API_ENDPOINTS.AFILIACIONES_PRODUCTOR_SOLICITAR,
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
   * Aprueba una solicitud de productor (usado por ADMIN_ZONA)
   */
  async aprobarProductor(
    afiliacionId: string,
    observaciones?: string
  ): Promise<void> {
    await apiService.patch<void>(
      API_ENDPOINTS.AFILIACIONES_PRODUCTOR_APROBAR(afiliacionId),
      { observaciones } as DecisionAfiliacionRequest
    );
  }

  /**
   * Rechaza una solicitud de productor (usado por ADMIN_ZONA)
   */
  async rechazarProductor(
    afiliacionId: string,
    observaciones?: string
  ): Promise<void> {
    await apiService.patch<void>(
      API_ENDPOINTS.AFILIACIONES_PRODUCTOR_RECHAZAR(afiliacionId),
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

  /**
   * Lista las solicitudes de afiliación de un usuario (solicitante)
   */
  async listarMisSolicitudes(usuarioId: string): Promise<AfiliacionResumen[]> {
    return await apiService.get<AfiliacionResumen[]>(
      `${API_ENDPOINTS.AFILIACIONES_QUERY}?solicitante=${encodeURIComponent(usuarioId)}`
    );
  }

  /**
   * Lista solicitudes de afiliación según el rol del usuario:
   * - ADMIN_GLOBAL: obtiene TODAS las solicitudes
   * - PRODUCTOR/CLIENTE: obtiene solo las APROBADAS (para seleccionar zona)
   */
  async listarTodasLasSolicitudes(): Promise<AfiliacionResumen[]> {
    // El backend filtra automáticamente según el rol del usuario
    return await apiService.get<AfiliacionResumen[]>(
      `${API_ENDPOINTS.AFILIACIONES_QUERY}`
    );
  }

  /**
   * Lista solo las zonas aprobadas (para que productores puedan seleccionar)
   * El backend ya devuelve solo APROBADAS para PRODUCTOR/CLIENTE,
   * pero agregamos un filtro adicional por seguridad
   */
  async listarZonasAprobadas(): Promise<AfiliacionResumen[]> {
    try {
      const solicitudes = await this.listarTodasLasSolicitudes();

      // Filtrar solo las que están aprobadas y tienen zona asignada
      const zonasAprobadas = solicitudes.filter(
        (afiliacion) =>
          afiliacion.estado === 'APROBADA' &&
          afiliacion.zonaId &&
          afiliacion.zonaId.trim() !== ''
      );

      return zonasAprobadas;
    } catch (error) {
      console.error('Error listando zonas aprobadas:', error);
      return [];
    }
  }
}

export default new AfiliacionesService();
