import apiService from './api.service';
import { API_ENDPOINTS } from '../config/api.config';
import type { Producto } from '../types';

export interface ProductoDTO {
  id: number;
  nombre: string;
  descripcion: string;
  precio: number;
  categoria: string;
  imagen?: string;
}

class ProductosService {
  /**
   * Obtiene todos los productos
   * Requiere pasar por el Gateway (header X-Gateway-Passed: true)
   */
  async listarProductos(): Promise<ProductoDTO[]> {
    try {
      // Intenta obtener desde el gateway
      return await apiService.getFromGateway<ProductoDTO[]>(API_ENDPOINTS.PRODUCTOS);
    } catch {
      console.warn('Gateway no disponible, usando datos mock');
      // Si falla, retorna array vacío o podrías usar los datos mock
      return [];
    }
  }

  /**
   * Obtiene un producto por ID
   */
  async obtenerProducto(id: number): Promise<ProductoDTO> {
    try {
      return await apiService.getFromGateway<ProductoDTO>(
        API_ENDPOINTS.PRODUCTOS_BY_ID(id)
      );
    } catch (error) {
      console.error('Error obteniendo producto:', error);
      throw error;
    }
  }

  /**
   * Convierte ProductoDTO del backend a Producto del frontend
   */
  mapToFrontendProduct(dto: ProductoDTO): Producto {
    // Mapear categoría del backend a categoría del frontend
    const categoriaMap: Record<string, Producto['categoria']> = {
      'frutas': 'frutas',
      'verduras': 'verduras',
      'tuberculos': 'tubérculos',
      'lacteos': 'lácteos',
      'granos': 'granos',
    };

    return {
      id: dto.id.toString(),
      nombre: dto.nombre,
      descripcion: dto.descripcion,
      precio: dto.precio,
      unidad: 'kg', // Por defecto, ajustar según tu lógica
      categoria: categoriaMap[dto.categoria.toLowerCase()] || 'otros',
      imagen: dto.imagen || 'https://via.placeholder.com/400x300',
      productorId: 'p1', // Asignar según tu lógica
      productorNombre: 'Productor', // Asignar según tu lógica
      disponible: true,
      stock: 50,
      temporada: ['todo el año'],
      etiquetas: ['fresco', 'local'],
    };
  }
}

export default new ProductosService();
