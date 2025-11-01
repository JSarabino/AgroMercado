import React from 'react';
import { mockEstadisticas } from '../data/mockData';
import { Users, ShoppingBag, DollarSign, Activity } from 'lucide-react';

const AdminDashboard: React.FC = () => {
  const stats = mockEstadisticas;

  const formatPrice = (price: number) => {
    return new Intl.NumberFormat('es-CO', {
      style: 'currency',
      currency: 'COP',
      minimumFractionDigits: 0,
    }).format(price);
  };

  return (
    <div className="dashboard-page admin-dashboard">
      <div className="dashboard-header">
        <h1>Panel Administrativo</h1>
        <div className="admin-actions">
          <button className="btn-secondary">Exportar Reportes</button>
          <button className="btn-primary">Configuración</button>
        </div>
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
