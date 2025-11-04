import React from 'react';
import type { Producto } from '../types';
import { ShoppingCart, MapPin, Tag } from 'lucide-react';
import { useCart } from '../context/CartContext';
import { useAuth } from '../context/AuthContext';

interface ProductCardProps {
  producto: Producto;
}

const ProductCard: React.FC<ProductCardProps> = ({ producto }) => {
  const { addToCart } = useCart();
  const { user } = useAuth();

  const handleAddToCart = () => {
    addToCart(producto, 1);
    // Mostrar notificación (puede implementarse con toast)
  };

  const formatPrice = (price: number) => {
    return new Intl.NumberFormat('es-CO', {
      style: 'currency',
      currency: 'COP',
      minimumFractionDigits: 0,
    }).format(price);
  };

  return (
    <div className="product-card">
      <div className="product-image">
        <img src={producto.imagen} alt={producto.nombre} />
        {!producto.disponible && (
          <div className="product-overlay">No disponible</div>
        )}
        <div className="product-category">{producto.categoria}</div>
      </div>

      <div className="product-content">
        <h3 className="product-name">{producto.nombre}</h3>
        <p className="product-description">{producto.descripcion}</p>

        <div className="product-producer">
          <MapPin size={14} />
          <span>{producto.productorNombre}</span>
        </div>

        <div className="product-tags">
          {producto.etiquetas.slice(0, 3).map((tag, index) => (
            <span key={index} className="tag">
              <Tag size={12} />
              {tag}
            </span>
          ))}
        </div>

        <div className="product-footer">
          <div className="product-price">
            <span className="price">{formatPrice(producto.precio)}</span>
            <span className="unit">/ {producto.unidad}</span>
          </div>

          {user?.role === 'cliente' && producto.disponible && (
            <button
              className="btn-add-cart"
              onClick={handleAddToCart}
            >
              <ShoppingCart size={18} />
              Agregar
            </button>
          )}
        </div>

        {producto.stock < 10 && producto.disponible && (
          <div className="product-stock-warning">
            ¡Solo quedan {producto.stock} {producto.unidad}!
          </div>
        )}
      </div>
    </div>
  );
};

export default ProductCard;
