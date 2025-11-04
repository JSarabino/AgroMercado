import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import pedidosService, { type Pedido } from '../services/pedidos.service';
import { CreditCard, Loader, CheckCircle, XCircle } from 'lucide-react';

const Pago: React.FC = () => {
  const navigate = useNavigate();
  const { pedidoId } = useParams<{ pedidoId: string }>();
  const [pedido, setPedido] = useState<Pedido | null>(null);
  const [loading, setLoading] = useState(true);
  const [procesando, setProcesando] = useState(false);

  // Datos del pago
  const [numeroTarjeta, setNumeroTarjeta] = useState('');
  const [nombreTitular, setNombreTitular] = useState('');
  const [fechaVencimiento, setFechaVencimiento] = useState('');
  const [cvv, setCvv] = useState('');

  useEffect(() => {
    if (pedidoId) {
      cargarPedido();
    }
  }, [pedidoId]);

  const cargarPedido = async () => {
    try {
      setLoading(true);
      const data = await pedidosService.obtenerPedido(Number(pedidoId));
      setPedido(data);
    } catch (error) {
      console.error('Error al cargar pedido:', error);
      alert('Error al cargar el pedido');
      navigate('/pedidos');
    } finally {
      setLoading(false);
    }
  };

  const handleProcesarPago = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!pedido) return;

    // Validar datos según método de pago
    if (pedido.metodoPago !== 'EFECTIVO') {
      if (!numeroTarjeta || !nombreTitular || !fechaVencimiento || !cvv) {
        alert('Por favor completa todos los datos de pago');
        return;
      }
    }

    try {
      setProcesando(true);

      const resultado = await pedidosService.procesarPago({
        pedidoId: pedido.id!,
        metodoPago: pedido.metodoPago!,
        numeroTarjeta,
        nombreTitular,
        fechaVencimiento,
        cvv,
      });

      if (resultado.aprobado) {
        // Pago exitoso
        alert(`✅ ${resultado.mensaje}\n\nTransacción ID: ${resultado.transaccionId}\nNúmero de Pedido: ${resultado.numeroPedido}`);
        navigate('/pedidos');
      } else {
        // Pago rechazado
        alert(`❌ ${resultado.mensaje}\n\nPor favor intenta con otro método de pago.`);
      }
    } catch (error: any) {
      console.error('Error al procesar pago:', error);
      const mensaje = error.response?.data?.message || 'Error al procesar el pago';
      alert(`❌ ${mensaje}`);
    } finally {
      setProcesando(false);
    }
  };

  if (loading) {
    return (
      <div className="loading-screen">
        <Loader size={48} className="spinner" />
        <p>Cargando información del pedido...</p>
      </div>
    );
  }

  if (!pedido) {
    return (
      <div className="error-screen">
        <XCircle size={64} color="#e74c3c" />
        <h2>Pedido no encontrado</h2>
        <button className="btn-primary" onClick={() => navigate('/pedidos')}>
          Ver Mis Pedidos
        </button>
      </div>
    );
  }

  return (
    <div className="pago-page">
      <div className="pago-header">
        <h1>💳 Procesar Pago</h1>
        <p>Pedido #{pedido.numeroPedido}</p>
      </div>

      <div className="pago-content">
        <div className="pedido-summary-section">
          <h2>Resumen del Pedido</h2>

          <div className="pedido-info">
            <div className="info-line">
              <span>Estado:</span>
              <span className="badge">{pedidosService.traducirEstado(pedido.estado)}</span>
            </div>
            <div className="info-line">
              <span>Método de Pago:</span>
              <span>{pedidosService.traducirMetodoPago(pedido.metodoPago!)}</span>
            </div>
            <div className="info-line">
              <span>Dirección de Entrega:</span>
              <span>{pedido.direccionEntrega}</span>
            </div>
          </div>

          <div className="productos-list">
            <h3>Productos</h3>
            {pedido.detalles.map((detalle) => (
              <div key={detalle.id} className="producto-item">
                <div className="producto-info">
                  <strong>{detalle.productoNombre}</strong>
                  <span className="cantidad">x{detalle.cantidad}</span>
                </div>
                <span className="precio">{pedidosService.formatearPrecio(detalle.subtotal)}</span>
              </div>
            ))}
          </div>

          <div className="totales">
            <div className="total-line">
              <span>Subtotal:</span>
              <span>{pedidosService.formatearPrecio(pedido.subtotal || 0)}</span>
            </div>
            <div className="total-line">
              <span>Impuestos (IVA 19%):</span>
              <span>{pedidosService.formatearPrecio(pedido.impuestos || 0)}</span>
            </div>
            <div className="total-line grand-total">
              <span>Total a Pagar:</span>
              <span>{pedidosService.formatearPrecio(pedido.total)}</span>
            </div>
          </div>
        </div>

        <div className="pago-form-section">
          <h2>Información de Pago</h2>

          {pedido.metodoPago === 'EFECTIVO' ? (
            <div className="pago-efectivo">
              <CheckCircle size={64} color="#27ae60" />
              <h3>Pago en Efectivo</h3>
              <p>Pagarás en efectivo al recibir tu pedido.</p>
              <p>Por favor ten el monto exacto disponible: <strong>{pedidosService.formatearPrecio(pedido.total)}</strong></p>

              <button
                className="btn-primary btn-block"
                onClick={handleProcesarPago}
                disabled={procesando}
              >
                {procesando ? (
                  <><Loader size={18} className="spinner" /> Confirmando...</>
                ) : (
                  'Confirmar Pedido'
                )}
              </button>
            </div>
          ) : (
            <form onSubmit={handleProcesarPago} className="tarjeta-form">
              <div className="card-icon">
                <CreditCard size={48} />
              </div>

              <div className="form-group">
                <label>Número de Tarjeta</label>
                <input
                  type="text"
                  value={numeroTarjeta}
                  onChange={(e) => setNumeroTarjeta(e.target.value.replace(/\s/g, '').replace(/(.{4})/g, '$1 ').trim())}
                  placeholder="1234 5678 9012 3456"
                  maxLength={19}
                  required
                />
              </div>

              <div className="form-group">
                <label>Nombre del Titular</label>
                <input
                  type="text"
                  value={nombreTitular}
                  onChange={(e) => setNombreTitular(e.target.value.toUpperCase())}
                  placeholder="COMO APARECE EN LA TARJETA"
                  required
                />
              </div>

              <div className="form-row">
                <div className="form-group">
                  <label>Fecha de Vencimiento</label>
                  <input
                    type="text"
                    value={fechaVencimiento}
                    onChange={(e) => {
                      let valor = e.target.value.replace(/\D/g, '');
                      if (valor.length >= 2) {
                        valor = valor.slice(0, 2) + '/' + valor.slice(2, 4);
                      }
                      setFechaVencimiento(valor);
                    }}
                    placeholder="MM/AA"
                    maxLength={5}
                    required
                  />
                </div>

                <div className="form-group">
                  <label>CVV</label>
                  <input
                    type="text"
                    value={cvv}
                    onChange={(e) => setCvv(e.target.value.replace(/\D/g, ''))}
                    placeholder="123"
                    maxLength={4}
                    required
                  />
                </div>
              </div>

              <div className="pago-info">
                <p>🔒 Tu información está segura y encriptada</p>
                <p>⚠️ Este es un pago simulado para demostración (90% de aprobación)</p>
              </div>

              <button
                type="submit"
                className="btn-primary btn-block"
                disabled={procesando}
              >
                {procesando ? (
                  <><Loader size={18} className="spinner" /> Procesando Pago...</>
                ) : (
                  `Pagar ${pedidosService.formatearPrecio(pedido.total)}`
                )}
              </button>

              <button
                type="button"
                className="btn-secondary btn-block"
                onClick={() => navigate('/pedidos')}
                disabled={procesando}
              >
                Cancelar
              </button>
            </form>
          )}
        </div>
      </div>
    </div>
  );
};

export default Pago;
