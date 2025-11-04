import apiService from './api.service';
import { API_ENDPOINTS } from '../config/api.config';

export interface RegistrarUsuarioRequest {
  email: string;
  nombre: string;
}

export interface UsuarioResponse {
  usuarioId: string;
  email: string;
  nombre: string;
  mensaje: string;
}

export interface AgregarRolGlobalRequest {
  rolGlobal: 'ADMIN_GLOBAL';
}

class UsuariosService {
  /**
   * Registra un nuevo usuario en el sistema
   */
  async registrarUsuario(data: RegistrarUsuarioRequest): Promise<UsuarioResponse> {
    return await apiService.post<UsuarioResponse>(API_ENDPOINTS.USUARIOS, data);
  }

  /**
   * Agrega un rol global a un usuario (requiere rol ADMIN_GLOBAL)
   */
  async agregarRolGlobal(usuarioId: string, rolGlobal: 'ADMIN_GLOBAL'): Promise<string> {
    await apiService.patch<void>(
      API_ENDPOINTS.USUARIOS_ROLES(usuarioId),
      { rolGlobal } as AgregarRolGlobalRequest
    );
    return 'Rol global agregado exitosamente';
  }

  /**
   * Otorga membresía zonal a un usuario (requiere rol ADMIN_GLOBAL)
   */
  async otorgarMembresia(
    usuarioId: string,
    zonaId: string,
    rolZonal: 'ADMIN_ZONA' | 'PRODUCTOR'
  ): Promise<string> {
    await apiService.post<void>(
      API_ENDPOINTS.USUARIOS_MEMBRESIAS(usuarioId),
      { zonaId, rolZonal }
    );
    return 'Membresía otorgada exitosamente';
  }
}

export default new UsuariosService();
