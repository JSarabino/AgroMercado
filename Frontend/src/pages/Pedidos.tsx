import React, { useState } from 'react';
import { mockPedidos } from '../data/mockData';
import { Package, Truck, CheckCircle, XCircle, Clock } from 'lucide-react';

const Pedidos: React.FC = () => {
  const [filtroEstado, setFiltroEstado] = useState<string>('todos');

  const formatPrice = (price: number) => {
    return new Intl.NumberFormat('es-CO', {
      style: 'currency',
      currency: 'COP',
      minimumFractionDigits: 0,
    }).format(price);
  };

  const getStatusIcon = (estado: string) => {
    switch (estado) {
      case 'pendiente':
        return <Clock size={20} />;
      case 'confirmado':
        return <Package size={20} />;
      case 'en_transito':
        return <Truck size={20} />;
      case 'entregado':
        return <CheckCircle size={20} />;
      case 'cancelado':
        return <XCircle size={20} />;
      default:
        return <Package size={20} />;
    }
  };

  const pedidosFiltrados = filtroEstado === 'todos'
    ? mockPedidos
    : mockPedidos.filter(p => p.estado === filtroEstado);

  return (
    <div className="pedidos-page">
      <div className="pedidos-header">
        <h1>📦 Mis Pedidos</h1>
      </div>

      <div className="pedidos-filters">
        <button
          className={filtroEstado === 'todos' ? 'active' : ''}
          onClick={() => setFiltroEstado('todos')}
        >
          Todos
        </button>
        <button
          className={filtroEstado === 'pendiente' ? 'active' : ''}
          onClick={() => setFiltroEstado('pendiente')}
        >
          Pendientes
        </button>
        <button
          className={filtroEstado === 'en_transito' ? 'active' : ''}
          onClick={() => setFiltroEstado('en_transito')}
        >
          En Tránsito
        </button>
        <button
          className={filtroEstado === 'entregado' ? 'active' : ''}
          onClick={() => setFiltroEstado('entregado')}
        >
          Entregados
        </button>
      </div>

      <div className="pedidos-list">
        {pedidosFiltrados.length > 0 ? (
          pedidosFiltrados.map(pedido => (
            <div key={pedido.id} className="pedido-card">
              <div className="pedido-header">
                <div className="pedido-id">
                  <strong>Pedido #{pedido.id}</strong>
                  <span className="pedido-fecha">
                    {new Date(pedido.fecha).toLocaleDateString('es-CO', {
                      day: 'numeric',
                      month: 'long',
                      year: 'numeric',
                    })}
                  </span>
                </div>
                <div className={`pedido-estado estado-${pedido.estado}`}>
                  {getStatusIcon(pedido.estado)}
                  <span>{pedido.estado.replace('_', ' ')}</span>
                </div>
              </div>

              <div className="pedido-items">
                {pedido.items.map((item, index) => (
                  <div key={index} className="pedido-item">
                    <img src={item.producto.imagen} alt={item.producto.nombre} />
                    <div className="item-info">
                      <p className="item-name">{item.producto.nombre}</p>
                      <p className="item-quantity">Cantidad: {item.cantidad}</p>
                    </div>
                    <div className="item-price">
                      {formatPrice(item.subtotal)}
                    </div>
                  </div>
                ))}
              </div>

              <div className="pedido-footer">
                <div className="pedido-info">
                  <p>📍 {pedido.direccionEntrega}</p>
                  <p>💳 {pedido.metodoPago}</p>
                </div>
                <div className="pedido-total">
                  <span>Total:</span>
                  <strong>{formatPrice(pedido.total)}</strong>
                </div>
              </div>

              {pedido.estado === 'en_transito' && (
                <div className="pedido-tracking">
                  <div className="tracking-bar">
                    <div className="tracking-progress" style={{ width: '60%' }} />
                  </div>
                  <p>🚚 Tu pedido llegará en aproximadamente 2-4 horas</p>
                </div>
              )}
            </div>
          ))
        ) : (
          <div className="no-pedidos">
            <Package size={64} />
            <p>No tienes pedidos con este estado</p>
          </div>
        )}
      </div>
    </div>
  );
};

export default Pedidos;
