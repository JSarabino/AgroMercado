import { Component, inject, OnInit } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { AuthDevService } from '../../../core/services/auth-dev.service';
import { AuthService } from '../../../core/services/auth.service';
import { DevTokenRes } from '../models/accounts.models';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'app-login-page',
  standalone: true,
  imports: [ReactiveFormsModule],
  template: `
    <div class="container py-4">
      <h1 class="h4 mb-3">Iniciar sesión (DEV)</h1>

      <form class="row g-3" [formGroup]="form" (ngSubmit)="login()">
        <div class="col-12 col-md-6">
          <label class="form-label">UserId</label>
          <input class="form-control" formControlName="userId" placeholder="USR-... o USR-ADMIN-GLOBAL">
        </div>

        <div class="col-12 col-md-6">
          <label class="form-label">Roles (coma)</label>
          <input class="form-control" formControlName="rolesCsv" placeholder="ADMIN_GLOBAL, AUDITOR">
        </div>

        <div class="col-12 col-md-4">
          <label class="form-label">TTL (segundos)</label>
          <input type="number" class="form-control" formControlName="ttlSeconds" placeholder="3600">
        </div>

        <div class="col-12">
          <button class="btn btn-primary" type="submit" [disabled]="form.invalid || loading">
            {{ loading ? 'Generando...' : 'Obtener token' }}
          </button>
          <button type="button" class="btn btn-outline-secondary ms-2" (click)="logout()">Borrar token</button>
        </div>
      </form>

      @if (ok)  { <div class="alert alert-success mt-3">{{ msg }}</div> }
      @if (err) { <div class="alert alert-danger  mt-3">{{ err }}</div> }

      @if (token) {
        <div class="alert alert-info mt-3">Token guardado en localStorage.</div>
        <pre class="mt-2 small bg-light p-2">{{ token }}</pre>
      }
    </div>
  `,
})
export class LoginPage implements OnInit {
  private fb = inject(FormBuilder);
  private authDev = inject(AuthDevService);
  private auth = inject(AuthService);

  ok = false;
  msg = '';
  err = '';
  token = '';           // ← NO leemos aquí
  loading = false;

  form = this.fb.nonNullable.group({
    userId: ['', [Validators.required]],
    rolesCsv: [''],
    ttlSeconds: [3600 as number],
  });

  ngOnInit() {
    this.token = this.auth.getToken() ?? '';
  }

  async login() {
    this.ok = false; this.err = ''; this.msg = ''; this.loading = true;
    try {
      const roles = this.csvToArray(this.form.value.rolesCsv ?? '');
      const res: DevTokenRes = await firstValueFrom(
        this.authDev.issueDevToken({
          userId: this.form.value.userId!,
          roles,
          ttlSeconds: this.form.value.ttlSeconds ?? undefined,
        })
      );
      this.auth.setToken(res.access_token);
      this.token = res.access_token;
      this.ok = true;
      this.msg = 'Token generado y almacenado correctamente.';
    } catch (e: any) {
      this.err = e?.message ?? 'Error generando token';
    } finally {
      this.loading = false;
    }
  }

  logout() {
    this.auth.clear();
    this.token = '';
    this.ok = false;
    this.msg = 'Token eliminado de localStorage.';
  }

  private csvToArray(csv: string): string[] {
    return csv.split(',').map(s => s.trim()).filter(Boolean);
  }
}
