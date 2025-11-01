import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import type { UserRole } from '../types';
import { Sprout, Mail, Lock, User } from 'lucide-react';

const Login: React.FC = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [role, setRole] = useState<UserRole>('cliente');
  const [error, setError] = useState('');
  const { login, isLoading } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    if (!email || !password) {
      setError('Por favor completa todos los campos');
      return;
    }

    try {
      await login(email, password, role);
      // Redirigir según el rol
      if (role === 'admin') {
        navigate('/admin');
      } else if (role === 'productor') {
        navigate('/productor');
      } else {
        navigate('/catalogo');
      }
    } catch {
      setError('Error al iniciar sesión');
    }
  };

  return (
    <div className="login-container">
      <div className="login-background">
        <div className="floating-element element-1"></div>
        <div className="floating-element element-2"></div>
        <div className="floating-element element-3"></div>
      </div>

      <div className="login-content">
        <div className="login-card">
          <div className="login-header">
            <div className="logo-container">
              <Sprout size={48} className="logo-icon" />
              <h1>AgroMercado</h1>
            </div>
            <p className="subtitle">Conectando campo y ciudad</p>
          </div>

          <form onSubmit={handleSubmit} className="login-form">
            {error && <div className="error-message">{error}</div>}

            <div className="form-group">
              <label htmlFor="email">
                <Mail size={18} />
                Correo Electrónico
              </label>
              <input
                id="email"
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="tu@email.com"
                disabled={isLoading}
              />
            </div>

            <div className="form-group">
              <label htmlFor="password">
                <Lock size={18} />
                Contraseña
              </label>
              <input
                id="password"
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="••••••••"
                disabled={isLoading}
              />
            </div>

            <div className="form-group">
              <label htmlFor="role">
                <User size={18} />
                Tipo de Usuario
              </label>
              <select
                id="role"
                value={role}
                onChange={(e) => setRole(e.target.value as UserRole)}
                disabled={isLoading}
              >
                <option value="cliente">Cliente (Comprador)</option>
                <option value="productor">Productor (Vendedor)</option>
                <option value="admin">Administrador</option>
              </select>
            </div>

            <button
              type="submit"
              className="btn-primary"
              disabled={isLoading}
            >
              {isLoading ? 'Iniciando sesión...' : 'Ingresar'}
            </button>

            <div className="login-footer">
              <p>¿No tienes cuenta? <a href="#registro">Regístrate aquí</a></p>
            </div>
          </form>

          <div className="demo-info">
            <h4>🌱 Demo - Acceso rápido:</h4>
            <div className="demo-cards">
              <div className="demo-card" onClick={() => { setEmail('cliente@demo.com'); setRole('cliente'); }}>
                <strong>Cliente</strong>
                <small>Compra productos</small>
              </div>
              <div className="demo-card" onClick={() => { setEmail('productor@demo.com'); setRole('productor'); }}>
                <strong>Productor</strong>
                <small>Vende productos</small>
              </div>
              <div className="demo-card" onClick={() => { setEmail('admin@demo.com'); setRole('admin'); }}>
                <strong>Admin</strong>
                <small>Gestiona plataforma</small>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Login;
