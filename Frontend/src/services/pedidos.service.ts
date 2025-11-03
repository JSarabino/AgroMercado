import apiService from './api.service';

// ============= TIPOS =============

export interface DetallePedido {
  id?: number;
  productoId: number;
  productoNombre: string;
  productoDescripcion?: string;
  productorId?: string;
  productorNombre?: string;
  cantidad: number;
  precioUnitario: number;
  subtotal: number;
  unidadMedida?: string;
}

export interface Pedido {
  id?: number;
  clienteId?: string;
  clienteNombre?: string;
  clienteEmail?: string;
  zonaId?: string;
  numeroPedido?: string;
  estado: 'CARRITO' | 'PENDIENTE' | 'PAGADO' | 'EN_PREPARACION' | 'ENVIADO' | 'ENTREGADO' | 'CANCELADO';
  estadoPago: 'PENDIENTE' | 'PROCESANDO' | 'APROBADO' | 'RECHAZADO' | 'REEMBOLSADO';
  metodoPago?: 'TARJETA_CREDITO' | 'TARJETA_DEBITO' | 'TRANSFERENCIA' | 'EFECTIVO' | 'PSE';
  subtotal?: number;
  impuestos?: number;
  total: number;
  direccionEntrega?: string;
  telefonoContacto?: string;
  notas?: string;
  fechaCreacion?: string;
  fechaConfirmacion?: string;
  fechaPago?: string;
  fechaEntrega?: string;
  transaccionPagoId?: string;
  detalles: DetallePedido[];
}

export interface AgregarProductoRequest {
  productoId: number;
  cantidad: number;
}

export interface ConfirmarPedidoRequest {
  direccionEntrega: string;
  telefonoContacto: string;
  metodoPago: 'TARJETA_CREDITO' | 'TARJETA_DEBITO' | 'TRANSFERENCIA' | 'EFECTIVO' | 'PSE';
  notas?: string;
}

export interface ProcesarPagoRequest {
  pedidoId: number;
  metodoPago: 'TARJETA_CREDITO' | 'TARJETA_DEBITO' | 'TRANSFERENCIA' | 'EFECTIVO' | 'PSE';
  numeroTarjeta?: string;
  nombreTitular?: string;
  fechaVencimiento?: string;
  cvv?: string;
}

export interface PagoResponse {
  transaccionId: string;
  aprobado: boolean;
  mensaje: string;
  monto: number;
  metodoPago: string;
  pedidoId: number;
  numeroPedido: string;
}

// ============= SERVICIO =============

class PedidosService {
  // ========== CARRITO ==========

  /**
   * Obtiene el carrito actual del cliente
   */
  async obtenerCarrito(): Promise<Pedido> {
    try {
      const response = await apiService.get<Pedido>('/carrito');
      return response;
    } catch (error) {
      console.error('Error al obtener carrito:', error);
      throw error;
    }
  }

  /**
   * Agrega un producto al carrito
   */
  async agregarProductoAlCarrito(productoId: number, cantidad: number): Promise<Pedido> {
    try {
      const request: AgregarProductoRequest = { productoId, cantidad };
      const response = await apiService.post<Pedido>('/carrito/agregar', request);
      return response;
    } catch (error) {
      console.error('Error al agregar producto al carrito:', error);
      throw error;
    }
  }

  /**
   * Elimina un producto del carrito
   */
  async eliminarProductoDelCarrito(detalleId: number): Promise<Pedido> {
    try {
      const response = await apiService.delete<Pedido>(`/carrito/items/${detalleId}`);
      return response;
    } catch (error) {
      console.error('Error al eliminar producto del carrito:', error);
      throw error;
    }
  }

  /**
   * Vacía completamente el carrito
   */
  async vaciarCarrito(): Promise<void> {
    try {
      await apiService.delete('/carrito');
    } catch (error) {
      console.error('Error al vaciar carrito:', error);
      throw error;
    }
  }

  // ========== PEDIDOS ==========

  /**
   * Confirma el carrito como un pedido
   */
  async confirmarPedido(datos: ConfirmarPedidoRequest): Promise<Pedido> {
    try {
      const response = await apiService.post<Pedido>('/pedidos/confirmar', datos);
      return response;
    } catch (error) {
      console.error('Error al confirmar pedido:', error);
      throw error;
    }
  }

  /**
   * Obtiene un pedido específico por ID
   */
  async obtenerPedido(id: number): Promise<Pedido> {
    try {
      const response = await apiService.get<Pedido>(`/pedidos/${id}`);
      return response;
    } catch (error) {
      console.error('Error al obtener pedido:', error);
      throw error;
    }
  }

  /**
   * Lista todos los pedidos del usuario actual
   */
  async listarMisPedidos(): Promise<Pedido[]> {
    try {
      const response = await apiService.get<Pedido[]>('/pedidos/mis-pedidos');
      return response;
    } catch (error) {
      console.error('Error al listar pedidos:', error);
      throw error;
    }
  }

  /**
   * Lista pedidos de una zona específica (para admin de zona)
   */
  async listarPedidosPorZona(zonaId: string): Promise<Pedido[]> {
    try {
      const response = await apiService.get<Pedido[]>(`/pedidos/zona/${zonaId}`);
      return response;
    } catch (error) {
      console.error('Error al listar pedidos por zona:', error);
      throw error;
    }
  }

  /**
   * Actualiza el estado de un pedido
   */
  async actualizarEstado(pedidoId: number, nuevoEstado: string): Promise<Pedido> {
    try {
      const response = await apiService.patch<Pedido>(`/pedidos/${pedidoId}/estado?estado=${nuevoEstado}`);
      return response;
    } catch (error) {
      console.error('Error al actualizar estado del pedido:', error);
      throw error;
    }
  }

  // ========== PAGOS ==========

  /**
   * Procesa el pago de un pedido
   */
  async procesarPago(datos: ProcesarPagoRequest): Promise<PagoResponse> {
    try {
      const response = await apiService.post<PagoResponse>('/pagos/procesar', datos);
      return response;
    } catch (error) {
      console.error('Error al procesar pago:', error);
      throw error;
    }
  }

  // ========== UTILIDADES ==========

  /**
   * Formatea un precio en pesos colombianos
   */
  formatearPrecio(precio: number): string {
    return new Intl.NumberFormat('es-CO', {
      style: 'currency',
      currency: 'COP',
      minimumFractionDigits: 0,
    }).format(precio);
  }

  /**
   * Traduce el estado del pedido al español
   */
  traducirEstado(estado: string): string {
    const traducciones: Record<string, string> = {
      'CARRITO': 'En Carrito',
      'PENDIENTE': 'Pendiente',
      'PAGADO': 'Pagado',
      'EN_PREPARACION': 'En Preparación',
      'ENVIADO': 'Enviado',
      'ENTREGADO': 'Entregado',
      'CANCELADO': 'Cancelado',
    };
    return traducciones[estado] || estado;
  }

  /**
   * Traduce el estado de pago al español
   */
  traducirEstadoPago(estadoPago: string): string {
    const traducciones: Record<string, string> = {
      'PENDIENTE': 'Pendiente',
      'PROCESANDO': 'Procesando',
      'APROBADO': 'Aprobado',
      'RECHAZADO': 'Rechazado',
      'REEMBOLSADO': 'Reembolsado',
    };
    return traducciones[estadoPago] || estadoPago;
  }

  /**
   * Traduce el método de pago al español
   */
  traducirMetodoPago(metodoPago: string): string {
    const traducciones: Record<string, string> = {
      'TARJETA_CREDITO': 'Tarjeta de Crédito',
      'TARJETA_DEBITO': 'Tarjeta de Débito',
      'TRANSFERENCIA': 'Transferencia Bancaria',
      'EFECTIVO': 'Efectivo',
      'PSE': 'PSE',
    };
    return traducciones[metodoPago] || metodoPago;
  }
}

// Exportación por defecto del servicio
export default new PedidosService();
