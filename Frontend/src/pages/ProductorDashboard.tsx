import React, { useState } from 'react';
import { mockProductos, mockPedidos } from '../data/mockData';
import { TrendingUp, Package, ShoppingCart, DollarSign, Plus } from 'lucide-react';

const ProductorDashboard: React.FC = () => {
  const [showAddProduct, setShowAddProduct] = useState(false);

  // Datos del productor (simulados)
  const misProductos = mockProductos.filter(p => p.productorId === 'p1');
  const misPedidos = mockPedidos.filter(p => p.productores.includes('p1'));

  const totalVentas = 2450000;
  const ventasMes = 850000;
  const pedidosActivos = 5;

  const formatPrice = (price: number) => {
    return new Intl.NumberFormat('es-CO', {
      style: 'currency',
      currency: 'COP',
      minimumFractionDigits: 0,
    }).format(price);
  };

  return (
    <div className="dashboard-page">
      <div className="dashboard-header">
        <h1>Panel de Productor</h1>
        <button className="btn-primary" onClick={() => setShowAddProduct(true)}>
          <Plus size={20} />
          Agregar Producto
        </button>
      </div>

      <div className="stats-grid">
        <div className="stat-card">
          <div className="stat-icon green">
            <DollarSign size={24} />
          </div>
          <div className="stat-content">
            <p className="stat-label">Ventas Totales</p>
            <p className="stat-value">{formatPrice(totalVentas)}</p>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon blue">
            <TrendingUp size={24} />
          </div>
          <div className="stat-content">
            <p className="stat-label">Este Mes</p>
            <p className="stat-value">{formatPrice(ventasMes)}</p>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon orange">
            <ShoppingCart size={24} />
          </div>
          <div className="stat-content">
            <p className="stat-label">Pedidos Activos</p>
            <p className="stat-value">{pedidosActivos}</p>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon purple">
            <Package size={24} />
          </div>
          <div className="stat-content">
            <p className="stat-label">Mis Productos</p>
            <p className="stat-value">{misProductos.length}</p>
          </div>
        </div>
      </div>

      <div className="dashboard-content">
        <div className="dashboard-section">
          <h2>Mis Productos</h2>
          <div className="products-list">
            {misProductos.map(producto => (
              <div key={producto.id} className="product-item">
                <img src={producto.imagen} alt={producto.nombre} />
                <div className="product-info">
                  <h3>{producto.nombre}</h3>
                  <p className="price">{formatPrice(producto.precio)} / {producto.unidad}</p>
                </div>
                <div className="product-stock">
                  <span className={producto.stock > 20 ? 'stock-good' : 'stock-low'}>
                    Stock: {producto.stock}
                  </span>
                </div>
                <div className="product-status">
                  <span className={producto.disponible ? 'status-active' : 'status-inactive'}>
                    {producto.disponible ? 'Disponible' : 'No disponible'}
                  </span>
                </div>
                <button className="btn-secondary btn-sm">Editar</button>
              </div>
            ))}
          </div>
        </div>

        <div className="dashboard-section">
          <h2>Pedidos Recientes</h2>
          <div className="orders-list">
            {misPedidos.map(pedido => (
              <div key={pedido.id} className="order-item">
                <div className="order-header">
                  <span className="order-id">#{pedido.id}</span>
                  <span className={`order-status status-${pedido.estado}`}>
                    {pedido.estado.replace('_', ' ')}
                  </span>
                </div>
                <div className="order-details">
                  <p>📦 {pedido.items.length} productos</p>
                  <p>💰 {formatPrice(pedido.total)}</p>
                  <p>📅 {new Date(pedido.fecha).toLocaleDateString('es-CO')}</p>
                </div>
                <button className="btn-link">Ver detalles</button>
              </div>
            ))}
          </div>
        </div>
      </div>

      {showAddProduct && (
        <div className="modal-overlay" onClick={() => setShowAddProduct(false)}>
          <div className="modal" onClick={(e) => e.stopPropagation()}>
            <h2>Agregar Nuevo Producto</h2>
            <form className="product-form">
              <div className="form-group">
                <label>Nombre del Producto</label>
                <input type="text" placeholder="Ej: Aguacate Hass" />
              </div>
              <div className="form-group">
                <label>Descripción</label>
                <textarea placeholder="Descripción detallada" rows={3} />
              </div>
              <div className="form-row">
                <div className="form-group">
                  <label>Precio</label>
                  <input type="number" placeholder="3500" />
                </div>
                <div className="form-group">
                  <label>Unidad</label>
                  <select>
                    <option>kg</option>
                    <option>unidad</option>
                    <option>litro</option>
                  </select>
                </div>
              </div>
              <div className="form-group">
                <label>Categoría</label>
                <select>
                  <option>frutas</option>
                  <option>verduras</option>
                  <option>tubérculos</option>
                  <option>lácteos</option>
                  <option>granos</option>
                </select>
              </div>
              <div className="form-group">
                <label>Stock Disponible</label>
                <input type="number" placeholder="50" />
              </div>
              <div className="form-actions">
                <button type="button" className="btn-secondary" onClick={() => setShowAddProduct(false)}>
                  Cancelar
                </button>
                <button type="submit" className="btn-primary">
                  Agregar Producto
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default ProductorDashboard;
