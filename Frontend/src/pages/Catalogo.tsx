import React, { useState } from 'react';
import { mockProductos } from '../data/mockData';
import ProductCard from '../components/ProductCard';
import { Search, Filter, X } from 'lucide-react';

const Catalogo: React.FC = () => {
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedCategory, setSelectedCategory] = useState<string>('todas');
  const [showFilters, setShowFilters] = useState(false);

  const categorias = ['todas', 'frutas', 'verduras', 'tubérculos', 'lácteos', 'granos', 'otros'];

  const filteredProducts = mockProductos.filter(producto => {
    const matchesSearch = producto.nombre.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         producto.descripcion.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesCategory = selectedCategory === 'todas' || producto.categoria === selectedCategory;
    return matchesSearch && matchesCategory;
  });

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
              <ProductCard key={producto.id} producto={producto} />
            ))}
          </div>
        ) : (
          <div className="no-results">
            <p>No se encontraron productos que coincidan con tu búsqueda</p>
          </div>
        )}
      </div>
    </div>
  );
};

export default Catalogo;
