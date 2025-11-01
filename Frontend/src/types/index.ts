// Tipos y interfaces para toda la aplicación

export type UserRole = 'cliente' | 'productor' | 'admin';

export interface User {
  id: string;
  nombre: string;
  email: string;
  role: UserRole;
  telefono?: string;
  direccion?: string;
  avatar?: string;
}

export interface Productor extends User {
  role: 'productor';
  ubicacion: {
    lat: number;
    lng: number;
    direccion: string;
  };
  practicasCultivo: string[];
  calificacion: number;
  verificado: boolean;
  descripcion: string;
}

export interface Producto {
  id: string;
  nombre: string;
  descripcion: string;
  precio: number;
  unidad: string;
  categoria: 'frutas' | 'verduras' | 'tubérculos' | 'lácteos' | 'granos' | 'otros';
  imagen: string;
  productorId: string;
  productorNombre: string;
  disponible: boolean;
  stock: number;
  temporada: string[];
  etiquetas: string[];
}

export interface CartItem {
  producto: Producto;
  cantidad: number;
}

export interface Pedido {
  id: string;
  clienteId: string;
  items: Array<{
    producto: Producto;
    cantidad: number;
    subtotal: number;
  }>;
  total: number;
  estado: 'pendiente' | 'confirmado' | 'en_transito' | 'entregado' | 'cancelado';
  fecha: string;
  direccionEntrega: string;
  metodoPago: string;
  productores: string[];
}

export interface Notificacion {
  id: string;
  tipo: 'pedido' | 'entrega' | 'sistema' | 'recordatorio';
  titulo: string;
  mensaje: string;
  fecha: string;
  leida: boolean;
  accion?: string;
}

export interface Estadistica {
  productosMasVendidos: Array<{ producto: string; ventas: number }>;
  ventasPorMes: Array<{ mes: string; total: number }>;
  zonasMasDemanda: Array<{ zona: string; pedidos: number }>;
  totalVentas: number;
  totalProductores: number;
  totalClientes: number;
}
