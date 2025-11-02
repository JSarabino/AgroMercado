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

export interface SolicitudProductorZona {
  solicitudId: string;
  zonaId: string;
  productorUsuarioId: string;
  nombreProductor: string;
  documento: string;
  telefono?: string;
  correo?: string;
  direccion?: string;
  tipoProductos?: string;
  estado: 'PENDIENTE' | 'APROBADA' | 'RECHAZADA';
  observaciones?: string;
  aprobadaPor?: string;
  fechaDecision?: string;
  createdAt: string;
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
      API_ENDPOINTS.SOLICITUDES_PRODUCTOR_SOLICITAR,
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
    solicitudId: string,
    observaciones?: string
  ): Promise<void> {
    await apiService.post<void>(
      API_ENDPOINTS.SOLICITUDES_PRODUCTOR_APROBAR(solicitudId),
      { observaciones } as DecisionAfiliacionRequest
    );
  }

  /**
   * Rechaza una solicitud de productor (usado por ADMIN_ZONA)
   */
  async rechazarProductor(
    solicitudId: string,
    observaciones?: string
  ): Promise<void> {
    await apiService.post<void>(
      API_ENDPOINTS.SOLICITUDES_PRODUCTOR_RECHAZAR(solicitudId),
      { observaciones } as DecisionAfiliacionRequest
    );
  }

  /**
   * Lista las solicitudes de productor para una zona (usado por ADMIN_ZONA)
   */
  async listarSolicitudesProductorZona(zonaId: string, estado?: string): Promise<SolicitudProductorZona[]> {
    let url = `${API_ENDPOINTS.SOLICITUDES_PRODUCTOR_QUERY}?zonaId=${encodeURIComponent(zonaId)}`;
    if (estado) {
      url += `&estado=${encodeURIComponent(estado)}`;
    }
    return await apiService.get<SolicitudProductorZona[]>(url);
  }

  /**
   * Lista las solicitudes pendientes de productor para una zona específica
   */
  async listarSolicitudesPendientesZona(zonaId: string): Promise<SolicitudProductorZona[]> {
    return await apiService.get<SolicitudProductorZona[]>(
      API_ENDPOINTS.SOLICITUDES_PRODUCTOR_ZONA_PENDIENTES(zonaId)
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

  /**
   * Obtiene las zonas a las que el productor está afiliado (solicitudes aprobadas)
   */
  async listarMisZonasAfiliadas(productorId: string): Promise<SolicitudProductorZona[]> {
    try {
      // Obtener las solicitudes aprobadas del productor desde el nuevo endpoint
      const solicitudes = await apiService.get<SolicitudProductorZona[]>(
        `${API_ENDPOINTS.SOLICITUDES_PRODUCTOR_QUERY}?productorId=${encodeURIComponent(productorId)}&estado=APROBADA`
      );
      return solicitudes;
    } catch (error) {
      console.error('Error listando zonas afiliadas:', error);
      return [];
    }
  }
}

export default new AfiliacionesService();
