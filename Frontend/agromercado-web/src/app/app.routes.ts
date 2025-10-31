import { Routes } from '@angular/router';
import { RegisterPage } from './features/accounts/pages/register.page';
import { LoginPage } from './features/accounts/pages/login.page';
import { AfiliacionSolicitarPage } from './features/accounts/pages/afiliacion-solicitar.page';
import { AfiliacionesAdminPage } from './features/accounts/pages/afiliaciones-admin.page';

export const routes: Routes = [
  { path: '', redirectTo: 'accounts/login', pathMatch: 'full' },

  // Accounts (épica 1)
  { path: 'accounts/register', component: RegisterPage, title: 'Registro' },
  { path: 'accounts/login', component: LoginPage, title: 'Inicio de sesión' },
  { path: 'accounts/afiliaciones/solicitar', component: AfiliacionSolicitarPage, title: 'Solicitar Afiliación' },
  { path: 'accounts/afiliaciones/admin', component: AfiliacionesAdminPage, title: 'Aprobar/Rechazar Afiliaciones' },

  // Placeholders para otras áreas (no implementados aún)
  { path: 'products', loadComponent: () => import('./shared/ui/footer.component').then(m => m.FooterComponent) },
  { path: 'orders', loadComponent: () => import('./shared/ui/footer.component').then(m => m.FooterComponent) },
  { path: 'tracking', loadComponent: () => import('./shared/ui/footer.component').then(m => m.FooterComponent) },

  { path: '**', redirectTo: 'accounts/login' }
];
