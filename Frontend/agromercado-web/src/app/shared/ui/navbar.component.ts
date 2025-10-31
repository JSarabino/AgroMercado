import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [RouterLink],
  template: `
  <nav class="navbar navbar-expand-lg navbar-dark bg-dark">
    <div class="container">
      <a routerLink="/" class="navbar-brand">AgroMercado</a>
      <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#nav">
        <span class="navbar-toggler-icon"></span>
      </button>
      <div id="nav" class="collapse navbar-collapse">
        <ul class="navbar-nav me-auto">
          <li class="nav-item"><a routerLink="/accounts/register" class="nav-link">Registro</a></li>
          <li class="nav-item"><a routerLink="/accounts/login" class="nav-link">Login</a></li>
          <li class="nav-item"><a routerLink="/accounts/afiliaciones/solicitar" class="nav-link">Solicitar afiliación</a></li>
          <li class="nav-item"><a routerLink="/accounts/afiliaciones/admin" class="nav-link">Admin Afiliaciones</a></li>
        </ul>
      </div>
    </div>
  </nav>
  `
})
export class NavbarComponent {}
