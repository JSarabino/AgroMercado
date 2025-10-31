import { Component, inject } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { AccountsApiService } from '../services/accounts-api.service';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'app-afiliaciones-admin-page',
  standalone: true,
  imports: [ReactiveFormsModule],
  template: `
    <div class="container py-4">
      <h1 class="h4 mb-3">Administrar solicitudes de afiliación</h1>

      <div class="alert alert-warning">
        Necesitas rol <strong>ADMIN_GLOBAL</strong> para aprobar/rechazar.
      </div>

      <form class="row g-3" [formGroup]="form">
        <div class="col-12 col-md-6">
          <label class="form-label">Afiliación Id</label>
          <input class="form-control" formControlName="afiliacionId" placeholder="AFI-...">
        </div>

        <div class="col-12">
          <label class="form-label">Observaciones (opcional)</label>
          <textarea class="form-control" formControlName="observaciones" rows="2"></textarea>
        </div>

        <div class="col-12">
          <button class="btn btn-success me-2" type="button" (click)="aprobar()" [disabled]="form.invalid || loading">
            {{ loadingAprob ? 'Aprobando...' : 'Aprobar' }}
          </button>
          <button class="btn btn-outline-danger" type="button" (click)="rechazar()" [disabled]="form.invalid || loading">
            {{ loadingRech ? 'Rechazando...' : 'Rechazar' }}
          </button>
        </div>
      </form>

      @if (ok)  { <div class="alert alert-success mt-3">{{ msg }}</div> }
      @if (err) { <div class="alert alert-danger  mt-3">{{ err }}</div> }
    </div>
  `,
})
export class AfiliacionesAdminPage {
  private fb = inject(FormBuilder);
  private api = inject(AccountsApiService);

  ok = false;
  msg = '';
  err = '';
  loading = false;
  loadingAprob = false;
  loadingRech = false;

  form = this.fb.nonNullable.group({
    afiliacionId: ['', Validators.required],
    observaciones: [''],
  });

  async aprobar() {
    this.resetState(); this.loading = true; this.loadingAprob = true;
    try {
      const { afiliacionId, observaciones } = this.form.getRawValue();
      await firstValueFrom(this.api.aprobarAfiliacion(afiliacionId, { observaciones: observaciones || undefined }));
      this.ok = true;
      this.msg = `Afiliación ${afiliacionId} aprobada. Se otorgará membresía al solicitante (ADMIN_ZONA).`;
    } catch (e: any) {
      this.err = e?.message ?? 'Error aprobando afiliación';
    } finally {
      this.loading = false; this.loadingAprob = false;
    }
  }

  async rechazar() {
    this.resetState(); this.loading = true; this.loadingRech = true;
    try {
      const { afiliacionId, observaciones } = this.form.getRawValue();
      await firstValueFrom(this.api.rechazarAfiliacion(afiliacionId, { observaciones: observaciones || undefined }));
      this.ok = true;
      this.msg = `Afiliación ${afiliacionId} rechazada.`;
    } catch (e: any) {
      this.err = e?.message ?? 'Error rechazando afiliación';
    } finally {
      this.loading = false; this.loadingRech = false;
    }
  }

  private resetState() {
    this.ok = false; this.err = ''; this.msg = '';
  }
}
