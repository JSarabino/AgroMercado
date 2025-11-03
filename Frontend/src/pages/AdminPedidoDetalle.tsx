import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { ArrowLeft, Package, User, MapPin, Phone, Calendar, CreditCard, FileText } from 'lucide-react';
import pedidosService, { type Pedido } from '../services/pedidos.service';
import '../styles/pedidos.css';

const AdminPedidoDetalle = () => {
  const { pedidoId } = useParams<{ pedidoId: string }>();
  const navigate = useNavigate();
  const [pedido, setPedido] = useState<Pedido | null>(null);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (pedidoId) {
      cargarPedido();
    }
  }, [pedidoId]);

  const cargarPedido = async () => {
    try {
      setCargando(true);
      setError(null);
      const pedidoData = await pedidosService.obtenerPedido(Number(pedidoId));
      setPedido(pedidoData);
    } catch (err: any) {
      console.error('Error al cargar pedido:', err);
      setError(err.response?.data?.message || 'Error al cargar el pedido');
    } finally {
      setCargando(false);
    }
  };

  if (cargando) {
    return (
      <div className="container-fluid mt-4">
        <div className="text-center">
          <div className="spinner-border text-primary" role="status">
            <span className="visually-hidden">Cargando...</span>
          </div>
          <p className="mt-2">Cargando detalle del pedido...</p>
        </div>
      </div>
    );
  }

  if (error || !pedido) {
    return (
      <div className="container-fluid mt-4">
        <div className="alert alert-danger">
          <strong>Error:</strong> {error || 'Pedido no encontrado'}
        </div>
        <button className="btn btn-secondary" onClick={() => navigate('/admin/pedidos')}>
          <ArrowLeft size={20} className="me-2" />
          Volver a Pedidos
        </button>
      </div>
    );
  }

  const getEstadoClass = (estado: string) => {
    const classes: Record<string, string> = {
      'PENDIENTE': 'warning',
      'PAGADO': 'info',
      'EN_PREPARACION': 'primary',
      'ENVIADO': 'success',
      'ENTREGADO': 'success',
      'CANCELADO': 'danger'
    };
    return classes[estado] || 'secondary';
  };

  const getEstadoPagoClass = (estadoPago: string) => {
    const classes: Record<string, string> = {
      'PENDIENTE': 'warning',
      'PROCESANDO': 'info',
      'APROBADO': 'success',
      'RECHAZADO': 'danger',
      'REEMBOLSADO': 'secondary'
    };
    return classes[estadoPago] || 'secondary';
  };

  return (
    <div className="container-fluid mt-4">
      {/* Header */}
      <div className="d-flex justify-content-between align-items-center mb-4">
        <div>
          <h2>
            <Package className="me-2" />
            Detalle del Pedido
          </h2>
          <p className="text-muted">{pedido.numeroPedido || `#${pedido.id}`}</p>
        </div>
        <button className="btn btn-secondary" onClick={() => navigate('/admin/pedidos')}>
          <ArrowLeft size={20} className="me-2" />
          Volver
        </button>
      </div>

      <div className="row">
        {/* Información del Pedido */}
        <div className="col-md-8">
          {/* Estados */}
          <div className="card mb-3">
            <div className="card-body">
              <h5 className="card-title">Estados</h5>
              <div className="d-flex gap-3 mt-3">
                <div>
                  <small className="text-muted d-block">Estado del Pedido</small>
                  <span className={`badge bg-${getEstadoClass(pedido.estado)} mt-1`}>
                    {pedidosService.traducirEstado(pedido.estado)}
                  </span>
                </div>
                <div>
                  <small className="text-muted d-block">Estado del Pago</small>
                  <span className={`badge bg-${getEstadoPagoClass(pedido.estadoPago)} mt-1`}>
                    {pedidosService.traducirEstadoPago(pedido.estadoPago)}
                  </span>
                </div>
              </div>
            </div>
          </div>

          {/* Productos */}
          <div className="card mb-3">
            <div className="card-body">
              <h5 className="card-title">Productos</h5>
              <div className="table-responsive mt-3">
                <table className="table">
                  <thead>
                    <tr>
                      <th>Producto</th>
                      <th>Cantidad</th>
                      <th>Precio Unit.</th>
                      <th className="text-end">Subtotal</th>
                    </tr>
                  </thead>
                  <tbody>
                    {pedido.detalles?.map((detalle) => (
                      <tr key={detalle.id}>
                        <td>
                          <div>
                            <strong>{detalle.productoNombre}</strong>
                            {detalle.productoDescripcion && (
                              <small className="text-muted d-block">
                                {detalle.productoDescripcion}
                              </small>
                            )}
                            {detalle.productorNombre && (
                              <small className="text-success d-block">
                                Productor: {detalle.productorNombre}
                              </small>
                            )}
                          </div>
                        </td>
                        <td>
                          {detalle.cantidad} {detalle.unidadMedida || 'und'}
                        </td>
                        <td>{pedidosService.formatearPrecio(detalle.precioUnitario)}</td>
                        <td className="text-end">
                          <strong>{pedidosService.formatearPrecio(detalle.subtotal)}</strong>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          </div>

          {/* Notas */}
          {pedido.notas && (
            <div className="card mb-3">
              <div className="card-body">
                <h5 className="card-title">
                  <FileText size={20} className="me-2" />
                  Notas del Cliente
                </h5>
                <p className="mt-2">{pedido.notas}</p>
              </div>
            </div>
          )}
        </div>

        {/* Información del Cliente y Resumen */}
        <div className="col-md-4">
          {/* Cliente */}
          <div className="card mb-3">
            <div className="card-body">
              <h5 className="card-title">
                <User size={20} className="me-2" />
                Cliente
              </h5>
              <div className="mt-3">
                <p className="mb-2"><strong>{pedido.clienteNombre}</strong></p>
                <p className="mb-2 text-muted">{pedido.clienteEmail}</p>
                {pedido.telefonoContacto && (
                  <p className="mb-0">
                    <Phone size={16} className="me-2" />
                    {pedido.telefonoContacto}
                  </p>
                )}
              </div>
            </div>
          </div>

          {/* Dirección de Entrega */}
          {pedido.direccionEntrega && (
            <div className="card mb-3">
              <div className="card-body">
                <h5 className="card-title">
                  <MapPin size={20} className="me-2" />
                  Dirección de Entrega
                </h5>
                <p className="mt-3 mb-0">{pedido.direccionEntrega}</p>
              </div>
            </div>
          )}

          {/* Totales */}
          <div className="card mb-3">
            <div className="card-body">
              <h5 className="card-title">Resumen</h5>
              <div className="mt-3">
                <div className="d-flex justify-content-between mb-2">
                  <span>Subtotal:</span>
                  <span>{pedidosService.formatearPrecio(pedido.subtotal || 0)}</span>
                </div>
                <div className="d-flex justify-content-between mb-2">
                  <span>Impuestos (19%):</span>
                  <span>{pedidosService.formatearPrecio(pedido.impuestos || 0)}</span>
                </div>
                <hr />
                <div className="d-flex justify-content-between">
                  <strong>Total:</strong>
                  <strong className="text-success fs-5">
                    {pedidosService.formatearPrecio(pedido.total)}
                  </strong>
                </div>
              </div>
            </div>
          </div>

          {/* Método de Pago */}
          {pedido.metodoPago && (
            <div className="card mb-3">
              <div className="card-body">
                <h5 className="card-title">
                  <CreditCard size={20} className="me-2" />
                  Método de Pago
                </h5>
                <p className="mt-2 mb-0">{pedidosService.traducirMetodoPago(pedido.metodoPago)}</p>
                {pedido.transaccionPagoId && (
                  <small className="text-muted d-block mt-2">
                    ID Transacción: {pedido.transaccionPagoId}
                  </small>
                )}
              </div>
            </div>
          )}

          {/* Fechas */}
          <div className="card">
            <div className="card-body">
              <h5 className="card-title">
                <Calendar size={20} className="me-2" />
                Fechas
              </h5>
              <div className="mt-3">
                {pedido.fechaCreacion && (
                  <p className="mb-2">
                    <small className="text-muted d-block">Creado</small>
                    {new Date(pedido.fechaCreacion).toLocaleString('es-CO')}
                  </p>
                )}
                {pedido.fechaConfirmacion && (
                  <p className="mb-2">
                    <small className="text-muted d-block">Confirmado</small>
                    {new Date(pedido.fechaConfirmacion).toLocaleString('es-CO')}
                  </p>
                )}
                {pedido.fechaPago && (
                  <p className="mb-2">
                    <small className="text-muted d-block">Pagado</small>
                    {new Date(pedido.fechaPago).toLocaleString('es-CO')}
                  </p>
                )}
                {pedido.fechaEntrega && (
                  <p className="mb-0">
                    <small className="text-muted d-block">Entregado</small>
                    {new Date(pedido.fechaEntrega).toLocaleString('es-CO')}
                  </p>
                )}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default AdminPedidoDetalle;
