import React, { useState } from 'react';
import { useCart } from '../context/CartContext';
import { useNavigate } from 'react-router-dom';
import { Trash2, Plus, Minus, ShoppingBag, ArrowLeft } from 'lucide-react';

const Carrito: React.FC = () => {
  const { items, removeFromCart, updateQuantity, total, clearCart } = useCart();
  const navigate = useNavigate();
  const [direccion, setDireccion] = useState('');
  const [metodoPago, setMetodoPago] = useState('efectivo');
  const [showCheckout, setShowCheckout] = useState(false);

  const formatPrice = (price: number) => {
    return new Intl.NumberFormat('es-CO', {
      style: 'currency',
      currency: 'COP',
      minimumFractionDigits: 0,
    }).format(price);
  };

  const handleCheckout = () => {
    if (!direccion) {
      alert('Por favor ingresa tu dirección de entrega');
      return;
    }

    // Simular creación de pedido
    alert('¡Pedido realizado con éxito! Te notificaremos cuando sea procesado.');
    clearCart();
    navigate('/pedidos');
  };

  if (items.length === 0) {
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
      </div>

      <div className="carrito-content">
        <div className="carrito-items">
          {items.map(item => (
            <div key={item.producto.id} className="cart-item">
              <img src={item.producto.imagen} alt={item.producto.nombre} />

              <div className="item-details">
                <h3>{item.producto.nombre}</h3>
                <p className="item-producer">{item.producto.productorNombre}</p>
                <p className="item-price">{formatPrice(item.producto.precio)} / {item.producto.unidad}</p>
              </div>

              <div className="item-quantity">
                <button onClick={() => updateQuantity(item.producto.id, item.cantidad - 1)}>
                  <Minus size={16} />
                </button>
                <span>{item.cantidad}</span>
                <button onClick={() => updateQuantity(item.producto.id, item.cantidad + 1)}>
                  <Plus size={16} />
                </button>
              </div>

              <div className="item-subtotal">
                {formatPrice(item.producto.precio * item.cantidad)}
              </div>

              <button
                className="item-remove"
                onClick={() => removeFromCart(item.producto.id)}
              >
                <Trash2 size={18} />
              </button>
            </div>
          ))}
        </div>

        <div className="carrito-summary">
          <h2>Resumen del Pedido</h2>

          <div className="summary-line">
            <span>Subtotal</span>
            <span>{formatPrice(total)}</span>
          </div>
          <div className="summary-line">
            <span>Envío</span>
            <span>{formatPrice(3000)}</span>
          </div>
          <div className="summary-line total">
            <span>Total</span>
            <span>{formatPrice(total + 3000)}</span>
          </div>

          {!showCheckout ? (
            <button
              className="btn-primary btn-block"
              onClick={() => setShowCheckout(true)}
            >
              Proceder al Pago
            </button>
          ) : (
            <div className="checkout-form">
              <div className="form-group">
                <label>Dirección de Entrega</label>
                <textarea
                  value={direccion}
                  onChange={(e) => setDireccion(e.target.value)}
                  placeholder="Ingresa tu dirección completa"
                  rows={3}
                />
              </div>

              <div className="form-group">
                <label>Método de Pago</label>
                <select value={metodoPago} onChange={(e) => setMetodoPago(e.target.value)}>
                  <option value="efectivo">Efectivo</option>
                  <option value="transferencia">Transferencia</option>
                  <option value="tarjeta">Tarjeta (Mock)</option>
                </select>
              </div>

              <button
                className="btn-primary btn-block"
                onClick={handleCheckout}
              >
                Confirmar Pedido
              </button>
              <button
                className="btn-secondary btn-block"
                onClick={() => setShowCheckout(false)}
              >
                Cancelar
              </button>
            </div>
          )}

          <div className="summary-info">
            <p>🚚 Entrega en 24-48 horas</p>
            <p>📦 {items.length} productos de {new Set(items.map(i => i.producto.productorId)).size} productores</p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Carrito;
