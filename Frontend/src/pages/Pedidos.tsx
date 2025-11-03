import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import pedidosService, { type Pedido } from '../services/pedidos.service';
import { Package, Truck, CheckCircle, XCircle, Clock, Loader, CreditCard, ShoppingBag } from 'lucide-react';

const Pedidos: React.FC = () => {
  const navigate = useNavigate();
  const [pedidos, setPedidos] = useState<Pedido[]>([]);
  const [loading, setLoading] = useState(true);
  const [filtroEstado, setFiltroEstado] = useState<string>('todos');

  useEffect(() => {
    cargarPedidos();
  }, []);

  const cargarPedidos = async () => {
    try {
      setLoading(true);
      const data = await pedidosService.listarMisPedidos();
      setPedidos(data);
    } catch (error) {
      console.error('Error al cargar pedidos:', error);
      alert('Error al cargar tus pedidos');
    } finally {
      setLoading(false);
    }
  };

  const getStatusIcon = (estado: string) => {
    switch (estado) {
      case 'PENDIENTE':
        return <Clock size={20} color="#f39c12" />;
      case 'PAGADO':
      case 'EN_PREPARACION':
        return <Package size={20} color="#3498db" />;
      case 'ENVIADO':
        return <Truck size={20} color="#9b59b6" />;
      case 'ENTREGADO':
        return <CheckCircle size={20} color="#27ae60" />;
      case 'CANCELADO':
        return <XCircle size={20} color="#e74c3c" />;
      default:
        return <Package size={20} />;
    }
  };

  const getStatusColor = (estado: string) => {
    switch (estado) {
      case 'PENDIENTE':
        return '#f39c12';
      case 'PAGADO':
      case 'EN_PREPARACION':
        return '#3498db';
      case 'ENVIADO':
        return '#9b59b6';
      case 'ENTREGADO':
        return '#27ae60';
      case 'CANCELADO':
        return '#e74c3c';
      default:
        return '#95a5a6';
    }
  };

  const pedidosFiltrados = filtroEstado === 'todos'
    ? pedidos
    : pedidos.filter(p => p.estado === filtroEstado);

  if (loading) {
    return (
      <div className="loading-screen">
        <Loader size={48} className="spinner" />
        <p>Cargando tus pedidos...</p>
      </div>
    );
  }

  return (
    <div className="pedidos-page">
      <div className="pedidos-header">
        <h1>📦 Mis Pedidos</h1>
        <p>Historial completo de tus compras</p>
      </div>

      <div className="pedidos-filters">
        <button
          className={filtroEstado === 'todos' ? 'active' : ''}
          onClick={() => setFiltroEstado('todos')}
        >
          Todos ({pedidos.length})
        </button>
        <button
          className={filtroEstado === 'PENDIENTE' ? 'active' : ''}
          onClick={() => setFiltroEstado('PENDIENTE')}
        >
          Pendientes ({pedidos.filter(p => p.estado === 'PENDIENTE').length})
        </button>
        <button
          className={filtroEstado === 'EN_PREPARACION' ? 'active' : ''}
          onClick={() => setFiltroEstado('EN_PREPARACION')}
        >
          En Preparación ({pedidos.filter(p => p.estado === 'EN_PREPARACION').length})
        </button>
        <button
          className={filtroEstado === 'ENVIADO' ? 'active' : ''}
          onClick={() => setFiltroEstado('ENVIADO')}
        >
          Enviados ({pedidos.filter(p => p.estado === 'ENVIADO').length})
        </button>
        <button
          className={filtroEstado === 'ENTREGADO' ? 'active' : ''}
          onClick={() => setFiltroEstado('ENTREGADO')}
        >
          Entregados ({pedidos.filter(p => p.estado === 'ENTREGADO').length})
        </button>
      </div>

      <div className="pedidos-list">
        {pedidosFiltrados.length > 0 ? (
          pedidosFiltrados.map(pedido => (
            <div key={pedido.id} className="pedido-card">
              <div className="pedido-header-section">
                <div className="pedido-id">
                  <strong>Pedido #{pedido.numeroPedido}</strong>
                  <span className="pedido-fecha">
                    {pedido.fechaCreacion && new Date(pedido.fechaCreacion).toLocaleDateString('es-CO', {
                      day: 'numeric',
                      month: 'long',
                      year: 'numeric',
                      hour: '2-digit',
                      minute: '2-digit',
                    })}
                  </span>
                </div>
                <div className="pedido-estados">
                  <div
                    className="pedido-estado"
                    style={{ backgroundColor: `${getStatusColor(pedido.estado)}20`, color: getStatusColor(pedido.estado) }}
                  >
                  {getStatusIcon(pedido.estado)}
                    <span>{pedidosService.traducirEstado(pedido.estado)}</span>
                  </div>
                  {pedido.estadoPago && (
                    <div className="pedido-estado-pago">
                      💳 {pedidosService.traducirEstadoPago(pedido.estadoPago)}
                    </div>
                  )}
                </div>
              </div>

              <div className="pedido-items">
                {pedido.detalles.map((detalle, index) => (
                  <div key={index} className="pedido-item">
                    <div className="item-info">
                      <p className="item-name"><strong>{detalle.productoNombre}</strong></p>
                      {detalle.productorNombre && (
                        <p className="item-producer">🌾 {detalle.productorNombre}</p>
                      )}
                      <p className="item-quantity">
                        Cantidad: {detalle.cantidad} {detalle.unidadMedida}
                      </p>
                    </div>
                    <div className="item-price">
                      {pedidosService.formatearPrecio(detalle.subtotal)}
                    </div>
                  </div>
                ))}
              </div>

              <div className="pedido-footer-section">
                <div className="pedido-info-column">
                  {pedido.direccionEntrega && (
                  <p>📍 {pedido.direccionEntrega}</p>
                  )}
                  {pedido.metodoPago && (
                    <p>💳 {pedidosService.traducirMetodoPago(pedido.metodoPago)}</p>
                  )}
                  {pedido.telefonoContacto && (
                    <p>📞 {pedido.telefonoContacto}</p>
                  )}
                </div>
                <div className="pedido-total-column">
                  <div className="total-breakdown">
                    <div className="total-line">
                      <span>Subtotal:</span>
                      <span>{pedidosService.formatearPrecio(pedido.subtotal || 0)}</span>
                    </div>
                    <div className="total-line">
                      <span>Impuestos:</span>
                      <span>{pedidosService.formatearPrecio(pedido.impuestos || 0)}</span>
                    </div>
                    <div className="total-line grand-total">
                  <span>Total:</span>
                      <strong>{pedidosService.formatearPrecio(pedido.total)}</strong>
                    </div>
                  </div>
                </div>
              </div>

              {pedido.estado === 'PENDIENTE' && pedido.estadoPago === 'PENDIENTE' && (
                <div className="pedido-actions">
                  <button
                    className="btn-primary"
                    onClick={() => navigate(`/pago/${pedido.id}`)}
                  >
                    <CreditCard size={18} />
                    Procesar Pago
                  </button>
                </div>
              )}

              {pedido.estado === 'ENVIADO' && (
                <div className="pedido-tracking">
                  <div className="tracking-bar">
                    <div className="tracking-progress" style={{ width: '75%' }} />
                  </div>
                  <p>🚚 Tu pedido está en camino. Llegará pronto.</p>
                </div>
              )}

              {pedido.transaccionPagoId && (
                <div className="pedido-transaction">
                  <small>ID de Transacción: {pedido.transaccionPagoId}</small>
                </div>
              )}
            </div>
          ))
        ) : (
          <div className="no-pedidos">
            <ShoppingBag size={64} />
            <h2>No tienes pedidos {filtroEstado !== 'todos' && 'con este estado'}</h2>
            <p>Realiza tu primera compra en nuestro catálogo</p>
            <button className="btn-primary" onClick={() => navigate('/catalogo')}>
              Ir al Catálogo
            </button>
          </div>
        )}
      </div>
    </div>
  );
};

export default Pedidos;
