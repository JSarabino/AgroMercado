import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import { CartProvider } from './context/CartContext';
import Navbar from './components/Navbar';
import Login from './pages/Login';
import Catalogo from './pages/Catalogo';
import Carrito from './pages/Carrito';
import Pedidos from './pages/Pedidos';
import Productores from './pages/Productores';
import Notificaciones from './pages/Notificaciones';
import ProductorDashboard from './pages/ProductorDashboard';
import AdminDashboard from './pages/AdminDashboard';
import './App.css';

// Componente para proteger rutas
const ProtectedRoute: React.FC<{
  children: React.ReactNode;
  allowedRoles: string[]
}> = ({ children, allowedRoles }) => {
  const { user, isLoading } = useAuth();

  if (isLoading) {
    return (
      <div className="loading-screen">
        <div className="spinner"></div>
        <p>Cargando...</p>
      </div>
    );
  }

  if (!user) {
    return <Navigate to="/" replace />;
  }

  if (!allowedRoles.includes(user.role)) {
    return <Navigate to="/" replace />;
  }

  return <>{children}</>;
};

// Layout principal con Navbar
const MainLayout: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { user } = useAuth();

  if (!user) {
    return <>{children}</>;
  }

  return (
    <div className="app-layout">
      <Navbar />
      <main className="main-content">
        {children}
      </main>
    </div>
  );
};

function AppRoutes() {
  const { isAuthenticated, user } = useAuth();

  return (
    <Routes>
      {/* Ruta de login */}
      <Route
        path="/"
        element={
          isAuthenticated ? (
            user?.role === 'admin' ? (
              <Navigate to="/admin" replace />
            ) : user?.role === 'productor' ? (
              <Navigate to="/productor" replace />
            ) : (
              <Navigate to="/catalogo" replace />
            )
          ) : (
            <Login />
          )
        }
      />

      {/* Rutas para clientes */}
      <Route
        path="/catalogo"
        element={
          <ProtectedRoute allowedRoles={['cliente', 'admin']}>
            <MainLayout>
              <Catalogo />
            </MainLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/carrito"
        element={
          <ProtectedRoute allowedRoles={['cliente']}>
            <MainLayout>
              <Carrito />
            </MainLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/pedidos"
        element={
          <ProtectedRoute allowedRoles={['cliente', 'admin']}>
            <MainLayout>
              <Pedidos />
            </MainLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/productores"
        element={
          <ProtectedRoute allowedRoles={['cliente', 'admin']}>
            <MainLayout>
              <Productores />
            </MainLayout>
          </ProtectedRoute>
        }
      />

      {/* Rutas para productores */}
      <Route
        path="/productor"
        element={
          <ProtectedRoute allowedRoles={['productor']}>
            <MainLayout>
              <ProductorDashboard />
            </MainLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/productor/productos"
        element={
          <ProtectedRoute allowedRoles={['productor']}>
            <MainLayout>
              <ProductorDashboard />
            </MainLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/productor/pedidos"
        element={
          <ProtectedRoute allowedRoles={['productor']}>
            <MainLayout>
              <Pedidos />
            </MainLayout>
          </ProtectedRoute>
        }
      />

      {/* Rutas para admin */}
      <Route
        path="/admin"
        element={
          <ProtectedRoute allowedRoles={['admin']}>
            <MainLayout>
              <AdminDashboard />
            </MainLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/admin/productores"
        element={
          <ProtectedRoute allowedRoles={['admin']}>
            <MainLayout>
              <Productores />
            </MainLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/admin/productos"
        element={
          <ProtectedRoute allowedRoles={['admin']}>
            <MainLayout>
              <Catalogo />
            </MainLayout>
          </ProtectedRoute>
        }
      />

      {/* Rutas compartidas */}
      <Route
        path="/notificaciones"
        element={
          <ProtectedRoute allowedRoles={['cliente', 'productor', 'admin']}>
            <MainLayout>
              <Notificaciones />
            </MainLayout>
          </ProtectedRoute>
        }
      />

      {/* Ruta 404 */}
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}

function App() {
  return (
    <Router>
      <AuthProvider>
        <CartProvider>
          <AppRoutes />
        </CartProvider>
      </AuthProvider>
    </Router>
  );
}

export default App;
