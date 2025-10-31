import { Component, inject } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { AccountsApiService } from '../services/accounts-api.service';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'app-register-page',
  standalone: true,
  imports: [ReactiveFormsModule],
  template: `
    <div class="container py-4">
      <h1 class="h4 mb-3">Registrar usuario</h1>

      <form class="row g-3" [formGroup]="form" (ngSubmit)="register()">
        <div class="col-12 col-md-6">
          <label class="form-label">Nombre</label>
          <input class="form-control" formControlName="nombre" placeholder="Juan Pérez">
          @if (form.controls.nombre.touched && form.controls.nombre.invalid) {
            <div class="text-danger small mt-1">Nombre es requerido</div>
          }
        </div>

        <div class="col-12 col-md-6">
          <label class="form-label">Email</label>
          <input type="email" class="form-control" formControlName="email" placeholder="juan@mail.com">
          @if (form.controls.email.touched && form.controls.email.invalid) {
            <div class="text-danger small mt-1">Email válido requerido</div>
          }
        </div>

        <div class="col-12">
          <button class="btn btn-primary" type="submit" [disabled]="form.invalid || loading">
            {{ loading ? 'Registrando...' : 'Registrar' }}
          </button>
        </div>
      </form>

      @if (ok)  { <div class="alert alert-success mt-3">{{ msg }}</div> }
      @if (err) { <div class="alert alert-danger  mt-3">{{ err }}</div> }
    </div>
  `,
})
export class RegisterPage {
  private fb = inject(FormBuilder);
  private api = inject(AccountsApiService);

  ok = false;
  msg = '';
  err = '';
  loading = false;

  form = this.fb.nonNullable.group({
    nombre: ['', [Validators.required, Validators.minLength(2)]],
    email: ['', [Validators.required, Validators.email]],
  });

  async register() {
    this.ok = false; this.err = ''; this.msg = ''; this.loading = true;
    try {
      const { nombre, email } = this.form.getRawValue();
      const res = await firstValueFrom(this.api.registrarUsuario({ nombre, email }));
      this.ok = true;
      this.msg = `Usuario creado (id: ${res.usuarioId}).`;
      this.form.reset();
    } catch (e: any) {
      this.err = e?.message ?? 'Error registrando usuario';
    } finally {
      this.loading = false;
    }
  }
}
