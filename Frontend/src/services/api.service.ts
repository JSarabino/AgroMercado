import axios from 'axios';
import type { AxiosInstance, AxiosRequestConfig, InternalAxiosRequestConfig } from 'axios';
import { API_CONFIG } from '../config/api.config';

class ApiService {
  private api: AxiosInstance;

  constructor() {
    this.api = axios.create({
      baseURL: API_CONFIG.BASE_URL,
      headers: {
        'Content-Type': 'application/json',
      },
    });

    // Interceptor para agregar token y headers
    this.api.interceptors.request.use(
      (config: InternalAxiosRequestConfig) => {
        // Agregar token si existe
        const token = localStorage.getItem(API_CONFIG.TOKEN_KEY);
        if (token && config.headers) {
          config.headers.Authorization = `Bearer ${token}`;
        }

        // Agregar tenant header
        if (config.headers) {
          config.headers['X-Tenant-Id'] = API_CONFIG.DEFAULT_TENANT;
        }

        return config;
      },
      (error) => {
        return Promise.reject(error);
      }
    );

    // Interceptor para manejar respuestas de error
    this.api.interceptors.response.use(
      (response) => response,
      (error) => {
        if (error.response?.status === 401) {
          // Token expirado o inválido
          localStorage.removeItem(API_CONFIG.TOKEN_KEY);
          localStorage.removeItem(API_CONFIG.USER_KEY);
          window.location.href = '/';
        }
        return Promise.reject(error);
      }
    );
  }

  // Métodos HTTP
  async get<T>(url: string, config?: AxiosRequestConfig): Promise<T> {
    const response = await this.api.get<T>(url, config);
    return response.data;
  }

  async post<T>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<T> {
    const response = await this.api.post<T>(url, data, config);
    return response.data;
  }

  async patch<T>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<T> {
    const response = await this.api.patch<T>(url, data, config);
    return response.data;
  }

  async delete<T>(url: string, config?: AxiosRequestConfig): Promise<T> {
    const response = await this.api.delete<T>(url, config);
    return response.data;
  }

  // Método especial para Gateway (productos service)
  async getFromGateway<T>(url: string, config?: AxiosRequestConfig): Promise<T> {
    const gatewayApi = axios.create({
      baseURL: API_CONFIG.GATEWAY_URL,
      headers: {
        'Content-Type': 'application/json',
        'X-Gateway-Passed': 'true',
      },
    });

    // Agregar token
    const token = localStorage.getItem(API_CONFIG.TOKEN_KEY);
    if (token) {
      gatewayApi.defaults.headers.common['Authorization'] = `Bearer ${token}`;
    }

    const response = await gatewayApi.get<T>(url, config);
    return response.data;
  }
}

export default new ApiService();
