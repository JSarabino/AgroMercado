import { HttpInterceptorFn } from '@angular/common/http';
import { environment } from '../../../environments/environment';

export const tenantAuthInterceptor: HttpInterceptorFn = (req, next) => {
  let headers = req.headers;

  // Tenant (dev fallback)
  const tenant = environment.defaultTenantHeader;
  if (tenant) headers = headers.set('X-Tenant-Id', tenant);

  // Token (si existe)
  const token = localStorage.getItem('access_token');
  if (token) headers = headers.set('Authorization', `Bearer ${token}`);

  return next(req.clone({ headers }));
};