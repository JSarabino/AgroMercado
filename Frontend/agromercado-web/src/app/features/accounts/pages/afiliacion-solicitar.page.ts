import { Component, inject } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { AccountsApiService } from '../services/accounts-api.service';
import { AfiliacionResponse } from '../models/accounts.models';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'app-afiliacion-solicitar-page',
  standalone: true,
  imports: [ReactiveFormsModule],
  template: `
    <div class="container py-4">
      <h1 class="h4 mb-3">Solicitar afiliación de zona</h1>

      <form class="row g-3" [formGroup]="form" (ngSubmit)="solicitar()">
        <div class="col-12 col-md-6">
          <label class="form-label">Zona Id</label>
          <input class="form-control" formControlName="zonaId" placeholder="ZONA-...">
          @if (form.controls.zonaId.touched && form.controls.zonaId.invalid) {
            <div class="text-danger small mt-1">Zona Id es requerido</div>
          }
        </div>

        <div class="col-12 col-md-6">
          <label class="form-label">Nombre vereda</label>
          <input class="form-control" formControlName="nombreVereda" placeholder="El Mirador 1">
        </div>

        <div class="col-12 col-md-6">
          <label class="form-label">Municipio</label>
          <input class="form-control" formControlName="municipio" placeholder="Popayán">
        </div>

        <div class="col-12 col-md-6">
          <label class="form-label">Representante - Nombre</label>
          <input class="form-control" formControlName="representanteNombre" placeholder="Ana Ruiz">
        </div>

        <div class="col-12 col-md-6">
          <label class="form-label">Representante - Documento</label>
          <input class="form-control" formControlName="representanteDocumento" placeholder="CC-112233">
        </div>

        <div class="col-12 col-md-6">
          <label class="form-label">Representante - Correo</label>
          <input type="email" class="form-control" formControlName="representanteCorreo" placeholder="ana@correo.com">
        </div>

        <div class="col-12">
          <button class="btn btn-primary" type="submit" [disabled]="form.invalid || loading">
            {{ loading ? 'Enviando...' : 'Enviar solicitud' }}
          </button>
        </div>
      </form>

      @if (ok)  { <div class="alert alert-success mt-3">{{ msg }}</div> }
      @if (err) { <div class="alert alert-danger  mt-3">{{ err }}</div> }
    </div>
  `,
})
export class AfiliacionSolicitarPage {
  private fb = inject(FormBuilder);
  private api = inject(AccountsApiService);

  ok = false;
  msg = '';
  err = '';
  loading = false;

  form = this.fb.nonNullable.group({
    zonaId:              ['', Validators.required],
    nombreVereda:        [''],
    municipio:           [''],
    representanteNombre: [''],
    representanteDocumento: [''],
    representanteCorreo: ['', Validators.email],
  });

  async solicitar() {
    this.ok = false; this.err = ''; this.msg = ''; this.loading = true;
    try {
      const req = this.form.getRawValue();
      const res: AfiliacionResponse = await firstValueFrom(this.api.solicitarAfiliacion(req));
      this.ok = true;
      this.msg = `Solicitud enviada. Afiliación ${res.afiliacionId} (estado inicial: PENDIENTE).`;
      // this.form.reset();
    } catch (e: any) {
      this.err = e?.message ?? 'Error enviando solicitud';
    } finally {
      this.loading = false;
    }
  }
}
