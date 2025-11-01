import { jwtDecode } from 'jwt-decode';
import apiService from './api.service';
import { API_CONFIG, API_ENDPOINTS } from '../config/api.config';
import type { UserRole } from '../types';

interface JWTPayload {
  sub: string; // userId
  roles: string[]; // ['ADMIN_GLOBAL', 'ADMIN_ZONA', 'PRODUCTOR']
  iat: number;
  exp: number;
}

interface DevTokenRequest {
  userId: string;
  roles: string[];
  ttlSeconds?: number;
}

interface DevTokenResponse {
  access_token: string;
  token_type: string;
  expires_in: number;
}

export interface DecodedToken {
  userId: string;
  roles: string[];
  exp: number;
}

class AuthService {
  /**
   * Genera un token de desarrollo usando el endpoint /auth/dev/token
   */
  async generateDevToken(userId: string, roles: string[]): Promise<string> {
    try {
      const response = await apiService.post<DevTokenResponse>(
        API_ENDPOINTS.AUTH_DEV_TOKEN,
        {
          userId,
          roles,
          ttlSeconds: 3600, // 1 hora
        } as DevTokenRequest
      );

      return response.access_token;
    } catch (error) {
      console.error('Error generando token:', error);
      throw new Error('Error al generar token de autenticación');
    }
  }

  /**
   * Guarda el token en localStorage
   */
  saveToken(token: string): void {
    localStorage.setItem(API_CONFIG.TOKEN_KEY, token);
  }

  /**
   * Obtiene el token del localStorage
   */
  getToken(): string | null {
    return localStorage.getItem(API_CONFIG.TOKEN_KEY);
  }

  /**
   * Decodifica el JWT y extrae la información del usuario
   */
  decodeToken(token: string): DecodedToken | null {
    try {
      const decoded = jwtDecode<JWTPayload>(token);
      return {
        userId: decoded.sub,
        roles: decoded.roles || [],
        exp: decoded.exp,
      };
    } catch (error) {
      console.error('Error decodificando token:', error);
      return null;
    }
  }

  /**
   * Verifica si el token es válido y no ha expirado
   */
  isTokenValid(token: string): boolean {
    const decoded = this.decodeToken(token);
    if (!decoded) return false;

    const now = Date.now() / 1000;
    return decoded.exp > now;
  }

  /**
   * Obtiene los datos del usuario desde el token
   */
  getUserFromToken(): DecodedToken | null {
    const token = this.getToken();
    if (!token) return null;

    if (!this.isTokenValid(token)) {
      this.logout();
      return null;
    }

    return this.decodeToken(token);
  }

  /**
   * Mapea roles del backend a roles del frontend
   */
  mapRoleToUserRole(roles: string[]): UserRole {
    if (roles.includes('ADMIN_GLOBAL')) return 'admin';
    if (roles.includes('ADMIN_ZONA')) return 'productor';
    if (roles.includes('PRODUCTOR')) return 'productor';
    return 'cliente';
  }

  /**
   * Verifica si el usuario tiene un rol específico
   */
  hasRole(role: string): boolean {
    const userData = this.getUserFromToken();
    if (!userData) return false;
    return userData.roles.includes(role);
  }

  /**
   * Limpia la sesión
   */
  logout(): void {
    localStorage.removeItem(API_CONFIG.TOKEN_KEY);
    localStorage.removeItem(API_CONFIG.USER_KEY);
  }

  /**
   * Login simulado para desarrollo - genera token con roles específicos
   * En producción, esto debería llamar a un endpoint de login real
   */
  async loginDev(email: string, role: UserRole): Promise<string> {
    // Mapear rol del frontend a roles del backend
    let backendRoles: string[] = [];

    switch (role) {
      case 'admin':
        backendRoles = ['ADMIN_GLOBAL'];
        break;
      case 'productor':
        backendRoles = ['PRODUCTOR', 'ADMIN_ZONA'];
        break;
      case 'cliente':
        backendRoles = [];
        break;
    }

    // Generar un userId simulado basado en el email
    const userId = `user-${email.split('@')[0]}-${Date.now()}`;

    // Generar token
    const token = await this.generateDevToken(userId, backendRoles);

    // Guardar token
    this.saveToken(token);

    return token;
  }
}

export default new AuthService();
