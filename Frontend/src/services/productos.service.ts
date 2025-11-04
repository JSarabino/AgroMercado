import apiService from './api.service';

export interface Producto {
  idProducto: number;
  idProductor: string;
  zonaId: string;
  nombre: string;
  categoria: string;
  descripcion?: string;
  stockDisponible: number;
  unidadMedida: string;
  precioUnitario: number;
  imagenUrl?: string;
  disponible: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CrearProductoRequest {
  idProductor: string;
  zonaId: string;
  nombre: string;
  categoria: string;
  descripcion?: string;
  stockDisponible: number;
  unidadMedida: string;
  precioUnitario: number;
  imagenUrl?: string;
}

export interface ActualizarProductoRequest {
  nombre?: string;
  categoria?: string;
  descripcion?: string;
  stockDisponible?: number;
  unidadMedida?: string;
  precioUnitario?: number;
  imagenUrl?: string;
}

class ProductosService {
  private readonly BASE_URL = '/productos';

  /**
   * Obtiene todos los productos disponibles
   */
  async listarProductos(): Promise<Producto[]> {
    return await apiService.get<Producto[]>(this.BASE_URL);
  }

  /**
   * Obtiene un producto por su ID
   */
  async obtenerProducto(idProducto: number): Promise<Producto> {
    return await apiService.get<Producto>(`${this.BASE_URL}/${idProducto}`);
  }

  /**
   * Obtiene productos de un productor específico
   */
  async listarProductosPorProductor(idProductor: string): Promise<Producto[]> {
    return await apiService.get<Producto[]>(`${this.BASE_URL}/productor/${idProductor}`);
  }

  /**
   * Obtiene productos de una zona específica
   */
  async listarProductosPorZona(zonaId: string): Promise<Producto[]> {
    return await apiService.get<Producto[]>(`${this.BASE_URL}/zona/${zonaId}`);
  }

  /**
   * Obtiene solo los productos disponibles
   */
  async listarProductosDisponibles(): Promise<Producto[]> {
    return await apiService.get<Producto[]>(`${this.BASE_URL}/disponibles`);
  }

  /**
   * Crea un nuevo producto
   */
  async crearProducto(data: CrearProductoRequest): Promise<Producto> {
    return await apiService.post<Producto>(this.BASE_URL, data);
  }

  /**
   * Actualiza un producto existente
   */
  async actualizarProducto(idProducto: number, data: ActualizarProductoRequest): Promise<Producto> {
    return await apiService.put<Producto>(`${this.BASE_URL}/${idProducto}`, data);
  }

  /**
   * Elimina un producto
   */
  async eliminarProducto(idProducto: number): Promise<void> {
    return await apiService.delete<void>(`${this.BASE_URL}/${idProducto}`);
  }

  /**
   * Cambia el estado de disponibilidad de un producto
   */
  async toggleDisponibilidad(idProducto: number): Promise<Producto> {
    return await apiService.patch<Producto>(`${this.BASE_URL}/${idProducto}/disponibilidad`);
  }
}

export default new ProductosService();
