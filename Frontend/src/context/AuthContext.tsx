import React, { createContext, useContext, useState, useEffect } from 'react';
import type { User, UserRole } from '../types';
import authService from '../services/auth.service';
import { API_CONFIG } from '../config/api.config';

interface AuthContextType {
  user: User | null;
  login: (email: string, password: string, role: UserRole) => Promise<void>;
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

  const login = async (email: string, _password: string, role: UserRole) => {
    setIsLoading(true);

    try {
      // Generar token JWT usando el servicio de auth
      const token = await authService.loginDev(email, role);

      // Decodificar token para obtener información
      const tokenData = authService.decodeToken(token);

      if (!tokenData) {
        throw new Error('Token inválido');
      }

      // Crear objeto de usuario
      const newUser: User = {
        id: tokenData.userId,
        nombre: role === 'admin' ? 'Administrador Global' :
                role === 'productor' ? 'Juan Pérez' :
                'María García',
        email,
        role: authService.mapRoleToUserRole(tokenData.roles),
        telefono: '3001234567',
        direccion: role === 'cliente' ? 'Popayán, Cauca' : 'Vereda El Tablón, Popayán',
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
