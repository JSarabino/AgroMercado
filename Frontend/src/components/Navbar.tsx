import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useCart } from '../context/CartContext';
import {
  Sprout,
  ShoppingCart,
  Bell,
  User,
  LogOut,
  Package,
  LayoutDashboard,
  Menu,
  X
} from 'lucide-react';

const Navbar: React.FC = () => {
  const { user, logout } = useAuth();
  const { itemCount } = useCart();
  const navigate = useNavigate();
  const [isMenuOpen, setIsMenuOpen] = useState(false);
  const [showUserMenu, setShowUserMenu] = useState(false);

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  return (
    <nav className="navbar">
      <div className="navbar-container">
        <Link to={user?.role === 'admin' ? '/admin' : user?.role === 'productor' ? '/productor' : '/catalogo'} className="navbar-logo">
          <Sprout size={28} />
          <span>AgroMercado</span>
        </Link>

        <button
          className="navbar-toggle"
          onClick={() => setIsMenuOpen(!isMenuOpen)}
        >
          {isMenuOpen ? <X size={24} /> : <Menu size={24} />}
        </button>

        <div className={`navbar-menu ${isMenuOpen ? 'active' : ''}`}>
          {user?.role === 'cliente' && (
            <>
              <Link to="/catalogo" className="navbar-link">
                Catálogo
              </Link>
              <Link to="/pedidos" className="navbar-link">
                Mis Pedidos
              </Link>
              <Link to="/productores" className="navbar-link">
                Productores
              </Link>
            </>
          )}

          {user?.role === 'productor' && (
            <>
              <Link to="/productor" className="navbar-link">
                <LayoutDashboard size={18} />
                Dashboard
              </Link>
              <Link to="/productor/productos" className="navbar-link">
                <Package size={18} />
                Mis Productos
              </Link>
              <Link to="/productor/pedidos" className="navbar-link">
                <ShoppingCart size={18} />
                Pedidos
              </Link>
            </>
          )}

          {user?.role === 'admin' && (
            <>
              <Link to="/admin" className="navbar-link">
                <LayoutDashboard size={18} />
                Dashboard
              </Link>
              <Link to="/admin/productores" className="navbar-link">
                Productores
              </Link>
              <Link to="/admin/productos" className="navbar-link">
                Productos
              </Link>
            </>
          )}
        </div>

        <div className="navbar-actions">
          <Link to="/notificaciones" className="navbar-icon">
            <Bell size={22} />
            <span className="badge">3</span>
          </Link>

          {user?.role === 'cliente' && (
            <Link to="/carrito" className="navbar-icon cart-icon">
              <ShoppingCart size={22} />
              {itemCount > 0 && <span className="badge">{itemCount}</span>}
            </Link>
          )}

          <div className="navbar-user">
            <button
              className="user-button"
              onClick={() => setShowUserMenu(!showUserMenu)}
            >
              {user?.avatar ? (
                <img src={user.avatar} alt={user.nombre} />
              ) : (
                <User size={22} />
              )}
            </button>

            {showUserMenu && (
              <div className="user-menu">
                <div className="user-menu-header">
                  <p className="user-name">{user?.nombre}</p>
                  <p className="user-role">{user?.role}</p>
                </div>
                <Link to="/perfil" className="user-menu-item">
                  <User size={18} />
                  Mi Perfil
                </Link>
                <button onClick={handleLogout} className="user-menu-item logout">
                  <LogOut size={18} />
                  Cerrar Sesión
                </button>
              </div>
            )}
          </div>
        </div>
      </div>
    </nav>
  );
};

export default Navbar;
