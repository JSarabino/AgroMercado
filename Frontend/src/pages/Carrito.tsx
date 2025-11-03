import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import pedidosService, { type Pedido } from '../services/pedidos.service';
import { Trash2, Loader, ShoppingBag, ArrowLeft, CreditCard } from 'lucide-react';

const Carrito: React.FC = () => {
  const navigate = useNavigate();
  const [carrito, setCarrito] = useState<Pedido | null>(null);
  const [loading, setLoading] = useState(true);
  const [procesando, setProcesando] = useState(false);
  const [showCheckout, setShowCheckout] = useState(false);

  // Datos del checkout
  const [direccion, setDireccion] = useState('');
  const [telefono, setTelefono] = useState('');
  const [metodoPago, setMetodoPago] = useState<'TARJETA_CREDITO' | 'TARJETA_DEBITO' | 'TRANSFERENCIA' | 'EFECTIVO' | 'PSE'>('EFECTIVO');
  const [notas, setNotas] = useState('');

  useEffect(() => {
    cargarCarrito();
  }, []);

  const cargarCarrito = async () => {
    try {
      setLoading(true);
      const data = await pedidosService.obtenerCarrito();
      setCarrito(data);
    } catch (error) {
      console.error('Error al cargar carrito:', error);
    } finally {
      setLoading(false);
    }
  };

  const eliminarProducto = async (detalleId: number) => {
    try {
      const carritoActualizado = await pedidosService.eliminarProductoDelCarrito(detalleId);
      setCarrito(carritoActualizado);
    } catch (error) {
      console.error('Error al eliminar producto:', error);
      alert('Error al eliminar el producto del carrito');
    }
  };

  const vaciarCarrito = async () => {
    if (!confirm('¿Estás seguro de que quieres vaciar el carrito?')) return;

    try {
      await pedidosService.vaciarCarrito();
      await cargarCarrito();
    } catch (error) {
      console.error('Error al vaciar carrito:', error);
      alert('Error al vaciar el carrito');
    }
  };

  const handleConfirmarPedido = async () => {
    if (!direccion || !telefono) {
      alert('Por favor completa todos los campos requeridos');
      return;
    }

    try {
      setProcesando(true);
      const pedidoConfirmado = await pedidosService.confirmarPedido({
        direccionEntrega: direccion,
        telefonoContacto: telefono,
        metodoPago,
        notas,
      });

      alert('✅ Pedido confirmado exitosamente');

      // Redirigir al proceso de pago
      navigate(`/pago/${pedidoConfirmado.id}`);
    } catch (error: any) {
      console.error('Error al confirmar pedido:', error);
      const mensaje = error.response?.data?.message || 'Error al confirmar el pedido';
      alert(`❌ ${mensaje}`);
    } finally {
      setProcesando(false);
    }
  };

  if (loading) {
    return (
      <div className="loading-screen">
        <Loader size={48} className="spinner" />
        <p>Cargando carrito...</p>
      </div>
    );
  }

  if (!carrito || carrito.detalles.length === 0) {
    return (
      <div className="carrito-vacio">
        <ShoppingBag size={64} />
        <h2>Tu carrito está vacío</h2>
        <p>Agrega productos desde nuestro catálogo</p>
        <button className="btn-primary" onClick={() => navigate('/catalogo')}>
          Ir al Catálogo
        </button>
      </div>
    );
  }

  return (
    <div className="carrito-page">
      <div className="carrito-header">
        <button className="btn-back" onClick={() => navigate('/catalogo')}>
          <ArrowLeft size={20} />
          Seguir comprando
        </button>
        <h1>🛒 Mi Carrito</h1>
        <button className="btn-danger-outline" onClick={vaciarCarrito}>
          Vaciar Carrito
        </button>
      </div>

      <div className="carrito-content">
        <div className="carrito-items">
          {carrito.detalles.map(detalle => (
            <div key={detalle.id} className="cart-item">
              <div className="item-details">
                <h3>{detalle.productoNombre}</h3>
                {detalle.productoDescripcion && (
                  <p className="item-description">{detalle.productoDescripcion}</p>
                )}
                {detalle.productorNombre && (
                  <p className="item-producer">🌾 {detalle.productorNombre}</p>
                )}
                <p className="item-price">
                  {pedidosService.formatearPrecio(detalle.precioUnitario)} / {detalle.unidadMedida}
                </p>
              </div>

              <div className="item-quantity">
                <span className="quantity-label">Cantidad:</span>
                <span className="quantity-value">{detalle.cantidad}</span>
              </div>

              <div className="item-subtotal">
                <span className="subtotal-label">Subtotal:</span>
                <span className="subtotal-value">
                  {pedidosService.formatearPrecio(detalle.subtotal)}
                </span>
              </div>

              <button
                className="item-remove"
                onClick={() => eliminarProducto(detalle.id!)}
                title="Eliminar producto"
              >
                <Trash2 size={18} />
              </button>
            </div>
          ))}
        </div>

        <div className="carrito-summary">
          <h2>Resumen del Pedido</h2>

          <div className="summary-line">
            <span>Subtotal ({carrito.detalles.length} productos)</span>
            <span>{pedidosService.formatearPrecio(carrito.subtotal || 0)}</span>
          </div>
          <div className="summary-line">
            <span>Impuestos (IVA 19%)</span>
            <span>{pedidosService.formatearPrecio(carrito.impuestos || 0)}</span>
          </div>
          <div className="summary-line total">
            <span>Total</span>
            <span>{pedidosService.formatearPrecio(carrito.total)}</span>
          </div>

          {!showCheckout ? (
            <button
              className="btn-primary btn-block"
              onClick={() => setShowCheckout(true)}
            >
              <CreditCard size={18} />
              Proceder al Pago
            </button>
          ) : (
            <div className="checkout-form">
              <h3>Información de Entrega</h3>

              <div className="form-group">
                <label>Dirección de Entrega *</label>
                <textarea
                  value={direccion}
                  onChange={(e) => setDireccion(e.target.value)}
                  placeholder="Ingresa tu dirección completa (calle, número, barrio, ciudad)"
                  rows={3}
                  required
                />
              </div>

              <div className="form-group">
                <label>Teléfono de Contacto *</label>
                <input
                  type="tel"
                  value={telefono}
                  onChange={(e) => setTelefono(e.target.value)}
                  placeholder="Ej: 3001234567"
                  required
                />
              </div>

              <div className="form-group">
                <label>Método de Pago *</label>
                <select
                  value={metodoPago}
                  onChange={(e) => setMetodoPago(e.target.value as any)}
                >
                  <option value="EFECTIVO">Efectivo</option>
                  <option value="TARJETA_CREDITO">Tarjeta de Crédito</option>
                  <option value="TARJETA_DEBITO">Tarjeta de Débito</option>
                  <option value="TRANSFERENCIA">Transferencia Bancaria</option>
                  <option value="PSE">PSE</option>
                </select>
              </div>

              <div className="form-group">
                <label>Notas adicionales (opcional)</label>
                <textarea
                  value={notas}
                  onChange={(e) => setNotas(e.target.value)}
                  placeholder="Instrucciones especiales de entrega, preferencias, etc."
                  rows={2}
                />
              </div>

              <button
                className="btn-primary btn-block"
                onClick={handleConfirmarPedido}
                disabled={procesando}
              >
                {procesando ? (
                  <><Loader size={18} className="spinner" /> Procesando...</>
                ) : (
                  'Confirmar Pedido'
                )}
              </button>
              <button
                className="btn-secondary btn-block"
                onClick={() => setShowCheckout(false)}
                disabled={procesando}
              >
                Cancelar
              </button>
            </div>
          )}

          <div className="summary-info">
            <p>🚚 Entrega estimada: 24-48 horas</p>
            <p>📦 {carrito.detalles.length} producto(s) en tu carrito</p>
            <p>💳 Pago seguro y protegido</p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Carrito;
