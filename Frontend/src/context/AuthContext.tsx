import React, { createContext, useContext, useState, useEffect } from 'react';
import type { User, UserRole } from '../types';
import authService from '../services/auth.service';
import { API_CONFIG } from '../config/api.config';

interface AuthContextType {
  user: User | null;
  login: (email: string, password: string) => Promise<void>;
  logout: () => void;
  isAuthenticated: boolean;
  isLoading: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    // Verificar si hay token válido y reconstruir usuario
    const token = authService.getToken();
    if (token && authService.isTokenValid(token)) {
      const tokenData = authService.getUserFromToken();
      if (tokenData) {
        // Intentar obtener datos guardados del usuario
        const savedUserData = localStorage.getItem(API_CONFIG.USER_KEY);
        if (savedUserData) {
          setUser(JSON.parse(savedUserData));
        } else {
          // Crear usuario básico desde el token
          const userRole = authService.mapRoleToUserRole(tokenData.roles);
          const basicUser: User = {
            id: tokenData.userId,
            nombre: tokenData.userId.split('-')[1] || 'Usuario',
            email: `${tokenData.userId}@agromercado.com`,
            role: userRole,
            avatar: `https://api.dicebear.com/7.x/avataaars/svg?seed=${tokenData.userId}`,
          };
          setUser(basicUser);
          localStorage.setItem(API_CONFIG.USER_KEY, JSON.stringify(basicUser));
        }
      }
    }
    setIsLoading(false);
  }, []);

  const login = async (email: string, password: string) => {
    setIsLoading(true);

    try {
      // Login real con el backend
      const response = await authService.login(email, password);

      // Mapear tipoUsuario del backend a UserRole del frontend
      const tipoUsuarioMap: Record<string, UserRole> = {
        'CLIENTE': 'cliente',
        'PRODUCTOR': 'productor',
        'ADMIN_ZONA': 'admin_zona',
        'ADMIN_GLOBAL': 'admin_global'
      };

      const userRole = tipoUsuarioMap[response.tipoUsuario] || 'cliente';

      const newUser: User = {
        id: response.usuarioId,
        nombre: response.nombre,
        email: response.email,
        role: userRole,
        avatar: `https://api.dicebear.com/7.x/avataaars/svg?seed=${email}`,
      };

      setUser(newUser);
      localStorage.setItem(API_CONFIG.USER_KEY, JSON.stringify(newUser));
      setIsLoading(false);
    } catch (error) {
      setIsLoading(false);
      console.error('Error en login:', error);
      throw error;
    }
  };

  const logout = () => {
    setUser(null);
    authService.logout();
  };

  return (
    <AuthContext.Provider value={{
      user,
      login,
      logout,
      isAuthenticated: !!user,
      isLoading
    }}>
      {children}
    </AuthContext.Provider>
  );
};

// eslint-disable-next-line react-refresh/only-export-components
export function useAuth() {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth debe usarse dentro de AuthProvider');
  }
  return context;
}
