import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { mockProductos, mockPedidos } from '../data/mockData';
import afiliacionesService from '../services/afiliaciones.service';
import type { SolicitudProductorZona } from '../services/afiliaciones.service';
import productosService from '../services/productos.service';
import type { CrearProductoRequest } from '../services/productos.service';
import { TrendingUp, Package, ShoppingCart, DollarSign, Plus, FileText, Activity, MapPin } from 'lucide-react';

interface ZonaConInfo extends SolicitudProductorZona {
  nombreVereda?: string;
  municipio?: string;
}

const ProductorDashboard: React.FC = () => {
  const navigate = useNavigate();
  const { user } = useAuth();
  const [showAddProduct, setShowAddProduct] = useState(false);
  const [zonasAfiliadas, setZonasAfiliadas] = useState<ZonaConInfo[]>([]);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);

  // Estado del formulario de producto
  const [formData, setFormData] = useState({
    zonaId: '',
    nombre: '',
    descripcion: '',
    precio: '',
    unidadMedida: 'kg',
    categoria: 'frutas',
    stock: '',
    imagenUrl: ''
  });

  // Datos del productor (simulados)
  const misProductos = mockProductos.filter(p => p.productorId === 'p1');
  const misPedidos = mockPedidos.filter(p => p.productores.includes('p1'));

  const cargarZonasAfiliadas = useCallback(async () => {
    if (!user?.id) {
      setLoading(false);
      return;
    }

    try {
      const solicitudes = await afiliacionesService.listarMisZonasAfiliadas(user.id);

      // Enriquecer cada solicitud con información de la zona
      const zonasEnriquecidas: ZonaConInfo[] = await Promise.all(
        solicitudes.map(async (solicitud) => {
          try {
            // Obtener información de la zona desde las afiliaciones
            const afiliacionesZona = await afiliacionesService.listarAfiliacionesZona(solicitud.zonaId);
            const afiliacionZona = afiliacionesZona.find(a => a.zonaId === solicitud.zonaId);

            return {
              ...solicitud,
              nombreVereda: afiliacionZona?.nombreVereda,
              municipio: afiliacionZona?.municipio,
            };
          } catch (error) {
            console.error(`Error obteniendo info de zona ${solicitud.zonaId}:`, error);
            return solicitud as ZonaConInfo;
          }
        })
      );

      setZonasAfiliadas(zonasEnriquecidas);
    } catch (error) {
      console.error('Error cargando zonas afiliadas:', error);
    } finally {
      setLoading(false);
    }
  }, [user?.id]);

  useEffect(() => {
    cargarZonasAfiliadas();
  }, [cargarZonasAfiliadas]);

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const resetForm = () => {
    setFormData({
      zonaId: '',
      nombre: '',
      descripcion: '',
      precio: '',
      unidadMedida: 'kg',
      categoria: 'frutas',
      stock: '',
      imagenUrl: ''
    });
  };

  const handleSubmitProduct = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!user?.id) {
      alert('Error: Usuario no autenticado');
      return;
    }

    if (!formData.zonaId) {
      alert('Por favor selecciona una zona productiva');
      return;
    }

    if (!formData.nombre || !formData.precio || !formData.stock) {
      alert('Por favor completa todos los campos obligatorios');
      return;
    }

    try {
      setSubmitting(true);

      const productoData: CrearProductoRequest = {
        idProductor: user.id,
        zonaId: formData.zonaId,
        nombre: formData.nombre.trim(),
        categoria: formData.categoria,
        descripcion: formData.descripcion.trim() || undefined,
        stockDisponible: parseInt(formData.stock),
        unidadMedida: formData.unidadMedida,
        precioUnitario: parseFloat(formData.precio),
        imagenUrl: formData.imagenUrl.trim() || undefined
      };

      await productosService.crearProducto(productoData);

      alert('✅ Producto creado exitosamente!');
      resetForm();
      setShowAddProduct(false);

      // Recargar la página o actualizar la lista de productos
      window.location.reload();
    } catch (error) {
      console.error('Error creando producto:', error);
      const err = error as { response?: { data?: { message?: string } } };
      alert(`❌ Error al crear producto: ${err.response?.data?.message || 'Error desconocido'}`);
    } finally {
      setSubmitting(false);
    }
  };

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
        <div style={{ display: 'flex', gap: '1rem' }}>
          <button className="btn-secondary" onClick={() => navigate('/productor/solicitar-afiliacion')}>
            <FileText size={20} />
            Solicitar Afiliación a Zona
          </button>
          <button className="btn-secondary" onClick={() => navigate('/productor/mis-solicitudes')}>
            <Activity size={20} />
            Ver Mis Solicitudes
          </button>
          <button className="btn-primary" onClick={() => setShowAddProduct(true)}>
            <Plus size={20} />
            Agregar Producto
          </button>
        </div>
      </div>

      {/* Alerta condicional basada en afiliaciones */}
      {!loading && zonasAfiliadas.length === 0 && (
        <div className="alert alert-info" style={{ marginBottom: '2rem' }}>
          <strong>⚠️ Importante:</strong> Para poder publicar productos, primero debes estar afiliado a una zona productiva.
          <button
            onClick={() => navigate('/productor/solicitar-afiliacion')}
            style={{
              marginLeft: '1rem',
              padding: '0.5rem 1rem',
              background: 'var(--primary)',
              color: 'white',
              border: 'none',
              borderRadius: '0.5rem',
              cursor: 'pointer',
              fontWeight: 600
            }}
          >
            Solicitar Ahora
          </button>
        </div>
      )}

      {!loading && zonasAfiliadas.length > 0 && (
        <div className="alert alert-success" style={{ marginBottom: '2rem', background: '#d1fae5', border: '1px solid #10b981' }}>
          <strong>✅ Estás afiliado a {zonasAfiliadas.length} zona{zonasAfiliadas.length > 1 ? 's' : ''}:</strong> Puedes publicar productos en tus zonas.
          <button
            onClick={() => navigate('/productor/mis-zonas')}
            style={{
              marginLeft: '1rem',
              padding: '0.5rem 1rem',
              background: '#10b981',
              color: 'white',
              border: 'none',
              borderRadius: '0.5rem',
              cursor: 'pointer',
              fontWeight: 600
            }}
          >
            <MapPin size={16} style={{ display: 'inline', marginRight: '0.5rem' }} />
            Ver Mis Zonas
          </button>
        </div>
      )}

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
            <div className="modal-header">
              <h2>Agregar Nuevo Producto</h2>
              <button className="btn-close" onClick={() => setShowAddProduct(false)}>
                ×
              </button>
            </div>
            <div className="modal-body">
              <form id="product-form" className="product-form" onSubmit={handleSubmitProduct}>
              {zonasAfiliadas.length > 0 && (
                <div className="form-group">
                  <label>
                    <MapPin size={16} style={{ display: 'inline', marginRight: '0.5rem' }} />
                    Zona Productiva <span className="required">*</span>
                  </label>
                  <select
                    name="zonaId"
                    value={formData.zonaId}
                    onChange={handleInputChange}
                    required
                  >
                    <option value="">Selecciona una zona</option>
                    {zonasAfiliadas.map((zona) => (
                      <option key={zona.zonaId} value={zona.zonaId}>
                        {zona.nombreVereda || zona.zonaId} {zona.municipio ? `- ${zona.municipio}` : ''}
                      </option>
                    ))}
                  </select>
                  <small style={{ color: '#718096', fontSize: '0.875rem' }}>
                    El producto se publicará en la zona seleccionada
                  </small>
                </div>
              )}

              {zonasAfiliadas.length === 0 && (
                <div className="alert alert-warning" style={{ marginBottom: '1rem', background: '#fef3c7', border: '1px solid #f59e0b', padding: '1rem', borderRadius: '0.5rem' }}>
                  <strong>⚠️ No estás afiliado a ninguna zona.</strong>
                  <p style={{ marginTop: '0.5rem', fontSize: '0.875rem' }}>
                    Debes estar afiliado a al menos una zona para publicar productos.
                  </p>
                  <button
                    type="button"
                    onClick={() => {
                      setShowAddProduct(false);
                      navigate('/productor/solicitar-afiliacion');
                    }}
                    style={{
                      marginTop: '0.5rem',
                      padding: '0.5rem 1rem',
                      background: '#f59e0b',
                      color: 'white',
                      border: 'none',
                      borderRadius: '0.5rem',
                      cursor: 'pointer',
                      fontWeight: 600
                    }}
                  >
                    Solicitar Afiliación
                  </button>
                </div>
              )}

              <div className="form-group">
                <label>Nombre del Producto <span className="required">*</span></label>
                <input
                  type="text"
                  name="nombre"
                  value={formData.nombre}
                  onChange={handleInputChange}
                  placeholder="Ej: Aguacate Hass"
                  required
                  disabled={zonasAfiliadas.length === 0}
                />
              </div>
              <div className="form-group">
                <label>Descripción</label>
                <textarea
                  name="descripcion"
                  value={formData.descripcion}
                  onChange={handleInputChange}
                  placeholder="Descripción detallada del producto"
                  rows={3}
                  disabled={zonasAfiliadas.length === 0}
                />
              </div>
              <div className="form-row">
                <div className="form-group">
                  <label>Precio (COP) <span className="required">*</span></label>
                  <input
                    type="number"
                    name="precio"
                    value={formData.precio}
                    onChange={handleInputChange}
                    placeholder="3500"
                    min="0"
                    step="0.01"
                    required
                    disabled={zonasAfiliadas.length === 0}
                  />
                </div>
                <div className="form-group">
                  <label>Unidad de Medida <span className="required">*</span></label>
                  <select
                    name="unidadMedida"
                    value={formData.unidadMedida}
                    onChange={handleInputChange}
                    disabled={zonasAfiliadas.length === 0}
                    required
                  >
                    <option value="kg">Kilogramo (kg)</option>
                    <option value="unidad">Unidad</option>
                    <option value="litro">Litro</option>
                    <option value="libra">Libra</option>
                    <option value="arroba">Arroba</option>
                    <option value="bulto">Bulto</option>
                  </select>
                </div>
              </div>
              <div className="form-group">
                <label>Categoría <span className="required">*</span></label>
                <select
                  name="categoria"
                  value={formData.categoria}
                  onChange={handleInputChange}
                  disabled={zonasAfiliadas.length === 0}
                  required
                >
                  <option value="frutas">Frutas</option>
                  <option value="verduras">Verduras</option>
                  <option value="tubérculos">Tubérculos</option>
                  <option value="lácteos">Lácteos</option>
                  <option value="granos">Granos</option>
                  <option value="otros">Otros</option>
                </select>
              </div>
              <div className="form-group">
                <label>Stock Disponible <span className="required">*</span></label>
                <input
                  type="number"
                  name="stock"
                  value={formData.stock}
                  onChange={handleInputChange}
                  placeholder="50"
                  min="0"
                  required
                  disabled={zonasAfiliadas.length === 0}
                />
              </div>
              <div className="form-group">
                <label>URL de Imagen (opcional)</label>
                <input
                  type="url"
                  name="imagenUrl"
                  value={formData.imagenUrl}
                  onChange={handleInputChange}
                  placeholder="https://ejemplo.com/imagen.jpg"
                  disabled={zonasAfiliadas.length === 0}
                />
                <small style={{ color: '#718096', fontSize: '0.875rem' }}>
                  Si no se proporciona, se usará una imagen por defecto
                </small>
              </div>
            </form>
            </div>
            <div className="modal-footer">
              <button
                type="button"
                className="btn-secondary"
                onClick={() => {
                  setShowAddProduct(false);
                  resetForm();
                }}
                disabled={submitting}
              >
                Cancelar
              </button>
              <button
                type="button"
                className="btn-primary"
                onClick={(e) => {
                  e.preventDefault();
                  const form = document.getElementById('product-form') as HTMLFormElement;
                  if (form) {
                    form.requestSubmit();
                  }
                }}
                disabled={zonasAfiliadas.length === 0 || submitting}
              >
                {submitting ? 'Creando...' : 'Agregar Producto'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default ProductorDashboard;
