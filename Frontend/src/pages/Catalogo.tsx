import React, { useState, useEffect } from 'react';
import productosService from '../services/productos.service';
import type { Producto } from '../services/productos.service';
import { Search, Filter, X, Loader, ShoppingCart } from 'lucide-react';
import { useNavigate } from 'react-router-dom';

const Catalogo: React.FC = () => {
  const navigate = useNavigate();
  const [productos, setProductos] = useState<Producto[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedCategory, setSelectedCategory] = useState<string>('todas');
  const [showFilters, setShowFilters] = useState(false);

  const categorias = ['todas', 'frutas', 'verduras', 'tubérculos', 'lácteos', 'granos', 'otros'];

  useEffect(() => {
    cargarProductos();
  }, []);

  const cargarProductos = async () => {
    try {
      setLoading(true);
      const data = await productosService.listarProductosDisponibles();
      setProductos(data);
    } catch (error) {
      console.error('Error cargando productos:', error);
      alert('Error al cargar los productos. Intenta de nuevo más tarde.');
    } finally {
      setLoading(false);
    }
  };

  const filteredProducts = productos.filter(producto => {
    const matchesSearch = producto.nombre.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         (producto.descripcion && producto.descripcion.toLowerCase().includes(searchTerm.toLowerCase()));
    const matchesCategory = selectedCategory === 'todas' || producto.categoria === selectedCategory;
    return matchesSearch && matchesCategory;
  });

  const handleAddToCart = (producto: Producto) => {
    // TODO: Implementar lógica de carrito
    alert(`Producto "${producto.nombre}" agregado al carrito`);
  };

  if (loading) {
    return (
      <div className="loading-screen">
        <Loader size={48} className="spinner" />
        <p>Cargando productos...</p>
      </div>
    );
  }

  return (
    <div className="catalogo-page">
      <div className="catalogo-header">
        <div className="header-content">
          <h1>🌾 Catálogo de Productos</h1>
          <p>Productos frescos y agroecológicos directamente del campo</p>
        </div>

        <div className="search-section">
          <div className="search-bar">
            <Search size={20} />
            <input
              type="text"
              placeholder="Buscar productos..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
            />
            {searchTerm && (
              <button onClick={() => setSearchTerm('')} className="clear-search">
                <X size={18} />
              </button>
            )}
          </div>

          <button
            className="filter-toggle"
            onClick={() => setShowFilters(!showFilters)}
          >
            <Filter size={20} />
            Filtros
          </button>
        </div>

        {showFilters && (
          <div className="filters-section">
            <h3>Categorías</h3>
            <div className="category-filters">
              {categorias.map(cat => (
                <button
                  key={cat}
                  className={`category-btn ${selectedCategory === cat ? 'active' : ''}`}
                  onClick={() => setSelectedCategory(cat)}
                >
                  {cat.charAt(0).toUpperCase() + cat.slice(1)}
                </button>
              ))}
            </div>
          </div>
        )}
      </div>

      <div className="catalogo-content">
        <div className="results-info">
          <p>Se encontraron <strong>{filteredProducts.length}</strong> productos</p>
        </div>

        {filteredProducts.length > 0 ? (
          <div className="products-grid">
            {filteredProducts.map(producto => (
              <div key={producto.idProducto} className="product-card">
                <div className="product-image">
                  <img
                    src={producto.imagenUrl || 'https://images.unsplash.com/photo-1574943320219-553eb213f72d?w=400&h=300&fit=crop'}
                    alt={producto.nombre}
                  />
                  {producto.stockDisponible < 10 && producto.stockDisponible > 0 && (
                    <span className="stock-badge low-stock">¡Pocas unidades!</span>
                  )}
                  {producto.stockDisponible === 0 && (
                    <span className="stock-badge out-of-stock">Agotado</span>
                  )}
                </div>

                <div className="product-info">
                  <h3>{producto.nombre}</h3>
                  <p className="product-category">{producto.categoria}</p>
                  {producto.descripcion && (
                    <p className="product-description">{producto.descripcion}</p>
                  )}

                  <div className="product-footer">
                    <div className="product-price">
                      <span className="price">${producto.precioUnitario.toLocaleString('es-CO')}</span>
                      <span className="unit">/ {producto.unidadMedida}</span>
                    </div>
                    <div className="product-stock">
                      <span>{producto.stockDisponible} disponibles</span>
                    </div>
                  </div>

                  <button
                    className="btn-primary btn-block"
                    onClick={() => handleAddToCart(producto)}
                    disabled={producto.stockDisponible === 0}
                  >
                    <ShoppingCart size={18} />
                    {producto.stockDisponible === 0 ? 'Agotado' : 'Agregar al Carrito'}
                  </button>
                </div>
              </div>
            ))}
          </div>
        ) : (
          <div className="no-results">
            <p>No se encontraron productos que coincidan con tu búsqueda</p>
            {productos.length === 0 && (
              <p className="no-products-hint">
                Aún no hay productos disponibles. ¡Vuelve pronto!
              </p>
            )}
          </div>
        )}
      </div>
    </div>
  );
};

export default Catalogo;
