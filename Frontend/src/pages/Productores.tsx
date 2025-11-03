import React, { useState, useEffect } from 'react';
import { MapPin, Package, Loader, ShoppingBag } from 'lucide-react';
import afiliacionesService from '../services/afiliaciones.service';
import type { AfiliacionResumen } from '../services/afiliaciones.service';
import productosService from '../services/productos.service';
import type { Producto } from '../services/productos.service';

interface ZonaConProductos extends AfiliacionResumen {
  productos: Producto[];
  cantidadProductos: number;
}

const Productores: React.FC = () => {
  const [zonas, setZonas] = useState<ZonaConProductos[]>([]);
  const [loading, setLoading] = useState(true);
  const [selectedZona, setSelectedZona] = useState<string | null>(null);

  useEffect(() => {
    cargarZonasYProductos();
  }, []);

  const cargarZonasYProductos = async () => {
    try {
      setLoading(true);

      // Obtener zonas aprobadas
      const zonasAprobadas = await afiliacionesService.listarZonasAprobadas();

      // Para cada zona, obtener sus productos
      const zonasConProductos: ZonaConProductos[] = await Promise.all(
        zonasAprobadas.map(async (zona) => {
          try {
            const productos = await productosService.listarProductosPorZona(zona.zonaId);
            return {
              ...zona,
              productos: productos,
              cantidadProductos: productos.length
            };
          } catch (error) {
            console.error(`Error cargando productos de zona ${zona.zonaId}:`, error);
            return {
              ...zona,
              productos: [],
              cantidadProductos: 0
            };
          }
        })
      );

      // Filtrar solo zonas con productos
      const zonasConProductosDisponibles = zonasConProductos.filter(z => z.cantidadProductos > 0);
      setZonas(zonasConProductosDisponibles);
    } catch (error) {
      console.error('Error cargando zonas:', error);
      alert('Error al cargar las zonas productoras.');
    } finally {
      setLoading(false);
    }
  };

  const handleVerProductos = (zonaId: string) => {
    setSelectedZona(selectedZona === zonaId ? null : zonaId);
  };

  if (loading) {
    return (
      <div className="loading-screen">
        <Loader size={48} className="spinner" />
        <p>Cargando zonas productoras...</p>
      </div>
    );
  }

  return (
    <div className="productores-page">
      <div className="productores-header">
        <h1>🌾 Zonas Productoras</h1>
        <p>Conoce las zonas productivas y sus productos disponibles</p>
      </div>

      {zonas.length === 0 ? (
        <div className="no-results">
          <p>Aún no hay zonas productoras con productos disponibles.</p>
          <p className="no-products-hint">¡Vuelve pronto para ver nuevos productos!</p>
        </div>
      ) : (
        <div className="productores-grid">
          {zonas.map(zona => (
            <div key={zona.afiliacionId} className="productor-card">
              <div className="productor-header">
                <div className="zona-info">
                  <h2>📍 {zona.nombreVereda}</h2>
                  <p className="productor-ubicacion">
                    <MapPin size={16} />
                    {zona.municipio}
                  </p>
                  <div className="zona-stats">
                    <span className="stat-badge">
                      <Package size={16} />
                      {zona.cantidadProductos} producto{zona.cantidadProductos !== 1 ? 's' : ''} disponible{zona.cantidadProductos !== 1 ? 's' : ''}
                    </span>
                    <span className="status-badge status-aprobada">
                      {zona.estado}
                    </span>
                  </div>
                </div>
              </div>

              <div className="productor-content">
                <button
                  className="btn-primary btn-block"
                  onClick={() => handleVerProductos(zona.zonaId)}
                >
                  <ShoppingBag size={18} />
                  {selectedZona === zona.zonaId ? 'Ocultar Productos' : 'Ver Productos'}
                </button>

                {selectedZona === zona.zonaId && (
                  <div className="zona-productos">
                    <h3>Productos Disponibles:</h3>
                    <div className="productos-list">
                      {zona.productos.map(producto => (
                        <div key={producto.idProducto} className="producto-item">
                          <div className="producto-item-image">
                            <img
                              src={producto.imagenUrl || 'https://images.unsplash.com/photo-1574943320219-553eb213f72d?w=200&h=150&fit=crop'}
                              alt={producto.nombre}
                            />
                          </div>
                          <div className="producto-item-info">
                            <h4>{producto.nombre}</h4>
                            <p className="producto-categoria">{producto.categoria}</p>
                            {producto.descripcion && (
                              <p className="producto-descripcion">{producto.descripcion}</p>
                            )}
                            <div className="producto-item-footer">
                              <span className="producto-precio">
                                ${producto.precioUnitario.toLocaleString('es-CO')} / {producto.unidadMedida}
                              </span>
                              <span className="producto-stock">
                                Stock: {producto.stockDisponible}
                              </span>
                            </div>
                          </div>
                        </div>
                      ))}
                    </div>
                  </div>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default Productores;
