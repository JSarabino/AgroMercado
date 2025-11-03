import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Package, Search, Eye, CheckCircle, XCircle, Clock, Truck } from 'lucide-react';
import pedidosService, { type Pedido } from '../services/pedidos.service';
import afiliacionesService from '../services/afiliaciones.service';
import { useAuth } from '../context/AuthContext';
import '../styles/pedidos.css';

const AdminPedidos = () => {
  const navigate = useNavigate();
  const { user } = useAuth();
  const [pedidos, setPedidos] = useState<Pedido[]>([]);
  const [filteredPedidos, setFilteredPedidos] = useState<Pedido[]>([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [filterEstado, setFilterEstado] = useState<string>('TODOS');
  const [zonaId, setZonaId] = useState<string | null>(null);

  useEffect(() => {
    obtenerZonaAprobada();
  }, [user]);

  useEffect(() => {
    if (zonaId) {
      cargarPedidos();
    }
  }, [zonaId]);

  useEffect(() => {
    filtrarPedidos();
  }, [pedidos, searchTerm, filterEstado]);

  const obtenerZonaAprobada = async () => {
    try {
      if (!user) {
        setError('No hay usuario autenticado');
        setCargando(false);
        return;
      }

      console.log('Usuario actual:', user);

      // Obtener las solicitudes de afiliación del usuario (admin de zona)
      const afiliaciones = await afiliacionesService.listarMisSolicitudes(user.id);
      console.log('Afiliaciones encontradas:', afiliaciones);

      const zonaAprobada = afiliaciones.find(a => a.estado === 'APROBADA');
      console.log('Zona aprobada encontrada:', zonaAprobada);

      if (zonaAprobada && zonaAprobada.zonaId) {
        console.log('ZonaId asignado:', zonaAprobada.zonaId);
        setZonaId(zonaAprobada.zonaId);
      } else {
        setError('No tienes una zona aprobada asignada');
        setCargando(false);
      }
    } catch (err: any) {
      console.error('Error al obtener zona:', err);
      setError('Error al obtener la zona asignada');
      setCargando(false);
    }
  };

  const cargarPedidos = async () => {
    try {
      setCargando(true);
      setError(null);

      if (!zonaId) {
        setError('No tienes una zona asignada');
        return;
      }

      console.log('Cargando pedidos para zona:', zonaId);
      const pedidosZona = await pedidosService.listarPedidosPorZona(zonaId);
      console.log('Pedidos recibidos:', pedidosZona);
      console.log('Cantidad de pedidos:', pedidosZona.length);

      setPedidos(pedidosZona);
    } catch (err: any) {
      console.error('Error al cargar pedidos:', err);
      setError(err.response?.data?.message || 'Error al cargar pedidos');
    } finally {
      setCargando(false);
    }
  };

  const filtrarPedidos = () => {
    let filtered = pedidos;

    // Filtrar por estado
    if (filterEstado !== 'TODOS') {
      filtered = filtered.filter(p => p.estado === filterEstado);
    }

    // Filtrar por búsqueda
    if (searchTerm) {
      const search = searchTerm.toLowerCase();
      filtered = filtered.filter(p =>
        p.numeroPedido?.toLowerCase().includes(search) ||
        p.clienteNombre?.toLowerCase().includes(search) ||
        p.clienteEmail?.toLowerCase().includes(search)
      );
    }

    setFilteredPedidos(filtered);
  };

  const actualizarEstadoPedido = async (pedidoId: number, nuevoEstado: string) => {
    try {
      await pedidosService.actualizarEstado(pedidoId, nuevoEstado);
      await cargarPedidos();
    } catch (err: any) {
      console.error('Error al actualizar estado:', err);
      alert(err.response?.data?.message || 'Error al actualizar el estado del pedido');
    }
  };

  const verDetallePedido = (pedidoId: number) => {
    navigate(`/admin/pedidos/${pedidoId}`);
  };

  const getEstadoColor = (estado: string) => {
    const colors: Record<string, string> = {
      'PENDIENTE': 'warning',
      'PAGADO': 'info',
      'EN_PREPARACION': 'primary',
      'ENVIADO': 'success',
      'ENTREGADO': 'success',
      'CANCELADO': 'danger'
    };
    return colors[estado] || 'secondary';
  };

  const getEstadoPagoIcon = (estadoPago: string) => {
    switch (estadoPago) {
      case 'APROBADO': return <CheckCircle size={16} className="text-success" />;
      case 'RECHAZADO': return <XCircle size={16} className="text-danger" />;
      case 'PROCESANDO': return <Clock size={16} className="text-warning" />;
      default: return <Clock size={16} className="text-secondary" />;
    }
  };

  if (cargando) {
    return (
      <div className="container-fluid mt-4">
        <div className="text-center">
          <div className="spinner-border text-primary" role="status">
            <span className="visually-hidden">Cargando...</span>
          </div>
          <p className="mt-2">Cargando pedidos...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="container-fluid mt-4">
        <div className="alert alert-danger">
          <strong>Error:</strong> {error}
        </div>
      </div>
    );
  }

  return (
    <div className="container-fluid mt-4">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <div>
          <h2><Package className="me-2" />Gestión de Pedidos</h2>
          <p className="text-muted">Administra los pedidos de tu zona</p>
        </div>
        <button className="btn btn-secondary" onClick={() => navigate('/dashboard')}>
          Volver al Dashboard
        </button>
      </div>

      {/* Filtros */}
      <div className="card mb-4">
        <div className="card-body">
          <div className="row g-3">
            <div className="col-md-6">
              <div className="input-group">
                <span className="input-group-text">
                  <Search size={18} />
                </span>
                <input
                  type="text"
                  className="form-control"
                  placeholder="Buscar por número de pedido, cliente..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                />
              </div>
            </div>
            <div className="col-md-6">
              <select
                className="form-select"
                value={filterEstado}
                onChange={(e) => setFilterEstado(e.target.value)}
              >
                <option value="TODOS">Todos los estados</option>
                <option value="PENDIENTE">Pendientes</option>
                <option value="PAGADO">Pagados</option>
                <option value="EN_PREPARACION">En Preparación</option>
                <option value="ENVIADO">Enviados</option>
                <option value="ENTREGADO">Entregados</option>
                <option value="CANCELADO">Cancelados</option>
              </select>
            </div>
          </div>
        </div>
      </div>

      {/* Lista de Pedidos */}
      {filteredPedidos.length === 0 ? (
        <div className="alert alert-info">
          <p className="mb-0">
            No hay pedidos {filterEstado !== 'TODOS' ? `con estado "${filterEstado}"` : ''} para la zona {zonaId || 'asignada'}
          </p>
          <small className="text-muted d-block mt-2">
            Los pedidos aparecerán aquí cuando los clientes confirmen sus compras de productos de tu zona.
            {pedidos.length > 0 && filterEstado !== 'TODOS' && ' (Prueba cambiando el filtro de estado)'}
          </small>
        </div>
      ) : (
        <div className="table-responsive">
          <table className="table table-hover">
            <thead>
              <tr>
                <th>Nº Pedido</th>
                <th>Cliente</th>
                <th>Fecha</th>
                <th>Items</th>
                <th>Total</th>
                <th>Estado</th>
                <th>Pago</th>
                <th>Acciones</th>
              </tr>
            </thead>
            <tbody>
              {filteredPedidos.map((pedido) => (
                <tr key={pedido.id}>
                  <td>
                    <strong>{pedido.numeroPedido || `#${pedido.id}`}</strong>
                  </td>
                  <td>
                    <div>{pedido.clienteNombre}</div>
                    <small className="text-muted">{pedido.clienteEmail}</small>
                  </td>
                  <td>
                    <small>{pedido.fechaConfirmacion
                      ? new Date(pedido.fechaConfirmacion).toLocaleDateString('es-CO')
                      : new Date(pedido.fechaCreacion!).toLocaleDateString('es-CO')
                    }</small>
                  </td>
                  <td>{pedido.detalles?.length || 0}</td>
                  <td><strong>{pedidosService.formatearPrecio(pedido.total)}</strong></td>
                  <td>
                    <span className={`badge bg-${getEstadoColor(pedido.estado)}`}>
                      {pedidosService.traducirEstado(pedido.estado)}
                    </span>
                  </td>
                  <td>
                    <div className="d-flex align-items-center gap-1">
                      {getEstadoPagoIcon(pedido.estadoPago)}
                      <small>{pedidosService.traducirEstadoPago(pedido.estadoPago)}</small>
                    </div>
                  </td>
                  <td>
                    <div className="btn-group btn-group-sm">
                      <button
                        className="btn btn-outline-primary"
                        onClick={() => verDetallePedido(pedido.id!)}
                        title="Ver detalles"
                      >
                        <Eye size={14} />
                      </button>
                      {pedido.estado === 'PAGADO' && (
                        <button
                          className="btn btn-outline-success"
                          onClick={() => actualizarEstadoPedido(pedido.id!, 'EN_PREPARACION')}
                          title="Marcar en preparación"
                        >
                          <Clock size={14} />
                        </button>
                      )}
                      {pedido.estado === 'EN_PREPARACION' && (
                        <button
                          className="btn btn-outline-info"
                          onClick={() => actualizarEstadoPedido(pedido.id!, 'ENVIADO')}
                          title="Marcar como enviado"
                        >
                          <Truck size={14} />
                        </button>
                      )}
                      {pedido.estado === 'ENVIADO' && (
                        <button
                          className="btn btn-outline-success"
                          onClick={() => actualizarEstadoPedido(pedido.id!, 'ENTREGADO')}
                          title="Marcar como entregado"
                        >
                          <CheckCircle size={14} />
                        </button>
                      )}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* Resumen */}
      <div className="row mt-4">
        <div className="col-md-3">
          <div className="card text-center">
            <div className="card-body">
              <h3 className="text-primary">{pedidos.length}</h3>
              <p className="text-muted mb-0">Total Pedidos</p>
            </div>
          </div>
        </div>
        <div className="col-md-3">
          <div className="card text-center">
            <div className="card-body">
              <h3 className="text-warning">{pedidos.filter(p => p.estado === 'PENDIENTE' || p.estado === 'PAGADO').length}</h3>
              <p className="text-muted mb-0">Pendientes</p>
            </div>
          </div>
        </div>
        <div className="col-md-3">
          <div className="card text-center">
            <div className="card-body">
              <h3 className="text-info">{pedidos.filter(p => p.estado === 'EN_PREPARACION' || p.estado === 'ENVIADO').length}</h3>
              <p className="text-muted mb-0">En Proceso</p>
            </div>
          </div>
        </div>
        <div className="col-md-3">
          <div className="card text-center">
            <div className="card-body">
              <h3 className="text-success">{pedidos.filter(p => p.estado === 'ENTREGADO').length}</h3>
              <p className="text-muted mb-0">Entregados</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default AdminPedidos;
