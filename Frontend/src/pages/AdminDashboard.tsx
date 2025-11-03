import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { mockEstadisticas } from '../data/mockData';
import { Users, ShoppingBag, DollarSign, Activity, FileText, UserCog, Package } from 'lucide-react';
import afiliacionesService from '../services/afiliaciones.service';

const AdminDashboard: React.FC = () => {
  const navigate = useNavigate();
  const { user } = useAuth();
  const stats = mockEstadisticas;
  const [tieneZonaAprobada, setTieneZonaAprobada] = useState(false);
  const [cargandoAfiliaciones, setCargandoAfiliaciones] = useState(true);

  // Verificar si el Admin Zona tiene una afiliación aprobada
  useEffect(() => {
    const verificarAfiliacion = async () => {
      if (user?.role === 'admin_zona' && user?.id) {
        try {
          setCargandoAfiliaciones(true);
          const solicitudes = await afiliacionesService.listarMisSolicitudes(user.id);

          // Verificar si tiene al menos una afiliación aprobada
          const tieneAprobada = solicitudes.some(
            (solicitud) => solicitud.estado === 'APROBADA'
          );

          setTieneZonaAprobada(tieneAprobada);
        } catch (error) {
          console.error('Error verificando afiliaciones:', error);
          setTieneZonaAprobada(false);
        } finally {
          setCargandoAfiliaciones(false);
        }
      } else {
        setCargandoAfiliaciones(false);
      }
    };

    verificarAfiliacion();
  }, [user]);

  const formatPrice = (price: number) => {
    return new Intl.NumberFormat('es-CO', {
      style: 'currency',
      currency: 'COP',
      minimumFractionDigits: 0,
    }).format(price);
  };

  // Debug: ver qué rol tiene el usuario
  console.log('Usuario en AdminDashboard:', user);
  console.log('Rol del usuario:', user?.role);
  console.log('Tiene zona aprobada:', tieneZonaAprobada);

  return (
    <div className="dashboard-page admin-dashboard">
      <div className="dashboard-header">
        <h1>Panel Administrativo</h1>
        <div className="admin-actions">
          <button className="btn-secondary">Exportar Reportes</button>
          <button className="btn-primary">Configuración</button>
        </div>
      </div>

      {/* Accesos rápidos */}
      <div className="quick-access-section">
        {user?.role === 'admin_global' && (
          <div
            className="quick-access-card primary"
            onClick={() => navigate('/admin/afiliaciones')}
          >
            <div className="quick-access-icon">
              <FileText size={32} />
            </div>
            <div className="quick-access-content">
              <h3>Gestionar Solicitudes de Afiliaciones de Zonas</h3>
              <p>Aprobar o rechazar solicitudes de nuevas zonas enviadas por Administradores Zonales</p>
              <span className="quick-access-badge">3 pendientes</span>
            </div>
          </div>
        )}

        {user?.role === 'admin_zona' && (
          <>
            <div
              className="quick-access-card primary"
              onClick={() => navigate('/admin/solicitar-afiliacion')}
            >
              <div className="quick-access-icon">
                <FileText size={32} />
              </div>
              <div className="quick-access-content">
                <h3>Solicitar Afiliación de Zona</h3>
                <p>Enviar solicitud de nueva zona al Administrador Global para su aprobación</p>
              </div>
            </div>

            <div
              className="quick-access-card info"
              onClick={() => navigate('/admin/mis-solicitudes')}
            >
              <div className="quick-access-icon">
                <Activity size={32} />
              </div>
              <div className="quick-access-content">
                <h3>Ver Estado de Solicitudes</h3>
                <p>Revisa el estado de tus solicitudes de afiliación de zonas</p>
              </div>
            </div>

            {/* Solo mostrar si tiene una zona aprobada */}
            {!cargandoAfiliaciones && tieneZonaAprobada && (
              <>
              <div
                className="quick-access-card secondary"
                onClick={() => navigate('/admin/productores')}
              >
                <div className="quick-access-icon">
                  <UserCog size={32} />
                </div>
                <div className="quick-access-content">
                  <h3>Gestión de Productores</h3>
                  <p>Aprobar o rechazar solicitudes de productores</p>
                  <span className="quick-access-badge">2 pendientes</span>
                </div>
              </div>

                <div
                  className="quick-access-card success"
                  onClick={() => navigate('/admin/pedidos')}
                >
                  <div className="quick-access-icon">
                    <Package size={32} />
                  </div>
                  <div className="quick-access-content">
                    <h3>Gestión de Pedidos</h3>
                    <p>Administra los pedidos de tu zona</p>
                    <span className="quick-access-badge">Activo</span>
                  </div>
                </div>
              </>
            )}

            {/* Mensaje cuando no tiene zona aprobada */}
            {!cargandoAfiliaciones && !tieneZonaAprobada && (
              <div className="quick-access-card disabled" style={{ opacity: 0.6, cursor: 'not-allowed' }}>
                <div className="quick-access-icon">
                  <UserCog size={32} />
                </div>
                <div className="quick-access-content">
                  <h3>Gestión de Productores</h3>
                  <p>⚠️ Necesitas tener una zona aprobada para acceder a esta funcionalidad</p>
                </div>
              </div>
            )}
          </>
        )}
      </div>

      <div className="stats-grid">
        <div className="stat-card">
          <div className="stat-icon green">
            <DollarSign size={24} />
          </div>
          <div className="stat-content">
            <p className="stat-label">Ventas Totales</p>
            <p className="stat-value">{formatPrice(stats.totalVentas)}</p>
            <p className="stat-change positive">+12.5% vs mes anterior</p>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon blue">
            <Users size={24} />
          </div>
          <div className="stat-content">
            <p className="stat-label">Clientes</p>
            <p className="stat-value">{stats.totalClientes}</p>
            <p className="stat-change positive">+8.3% este mes</p>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon orange">
            <ShoppingBag size={24} />
          </div>
          <div className="stat-content">
            <p className="stat-label">Productores</p>
            <p className="stat-value">{stats.totalProductores}</p>
            <p className="stat-change positive">+3 nuevos</p>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon purple">
            <Activity size={24} />
          </div>
          <div className="stat-content">
            <p className="stat-label">Sistema</p>
            <p className="stat-value">Óptimo</p>
            <p className="stat-change">Todos los servicios OK</p>
          </div>
        </div>
      </div>

      <div className="dashboard-grid">
        <div className="dashboard-section">
          <h2>📊 Productos Más Vendidos</h2>
          <div className="chart-container">
            {stats.productosMasVendidos.map((item, index) => (
              <div key={index} className="bar-chart-item">
                <div className="bar-label">{item.producto}</div>
                <div className="bar-container">
                  <div
                    className="bar"
                    style={{ width: `${(item.ventas / 150) * 100}%` }}
                  >
                    <span className="bar-value">{item.ventas}</span>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>

        <div className="dashboard-section">
          <h2>📈 Ventas Mensuales</h2>
          <div className="chart-container">
            {stats.ventasPorMes.map((item, index) => (
              <div key={index} className="sales-item">
                <span className="month">{item.mes}</span>
                <span className="amount">{formatPrice(item.total)}</span>
              </div>
            ))}
          </div>
        </div>

        <div className="dashboard-section">
          <h2>🗺️ Zonas con Mayor Demanda</h2>
          <div className="zones-list">
            {stats.zonasMasDemanda.map((zona, index) => (
              <div key={index} className="zone-item">
                <div className="zone-info">
                  <span className="zone-name">{zona.zona}</span>
                  <span className="zone-badge">{zona.pedidos} pedidos</span>
                </div>
                <div className="zone-bar">
                  <div
                    className="zone-progress"
                    style={{ width: `${(zona.pedidos / 250) * 100}%` }}
                  />
                </div>
              </div>
            ))}
          </div>
        </div>

        <div className="dashboard-section">
          <h2>🔧 Estado de Microservicios</h2>
          <div className="services-status">
            {[
              { name: 'Catálogo', status: 'online', uptime: '99.9%' },
              { name: 'Pedidos', status: 'online', uptime: '99.7%' },
              { name: 'Pagos', status: 'online', uptime: '99.8%' },
              { name: 'Notificaciones', status: 'online', uptime: '99.5%' },
              { name: 'Logística', status: 'warning', uptime: '98.2%' },
            ].map((service, index) => (
              <div key={index} className="service-item">
                <div className={`service-status ${service.status}`} />
                <div className="service-info">
                  <span className="service-name">{service.name}</span>
                  <span className="service-uptime">Uptime: {service.uptime}</span>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
};

export default AdminDashboard;
