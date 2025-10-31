import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from '../../../../environments/environment';
import {
    RegistrarUsuarioRequest, UsuarioResponse,
    SolicitarAfiliacionRequest, AfiliacionResponse,
    DecisionAfiliacionRequest, AfiliacionResumen
} from '../models/accounts.models';

@Injectable({ providedIn: 'root' })
export class AccountsApiService {
    private base = environment.apiBase;

    constructor(private http: HttpClient) { }

    registrarUsuario(body: RegistrarUsuarioRequest) {
        return this.http.post<UsuarioResponse>(`${this.base}/cmd/usuarios`, body);
    }

    // Dev only: emitir/guardar token desde fuera (usamos AuthDevService para solicitar)
    setToken(token: string) {
        localStorage.setItem('access_token', token);
    }

    // Solicitar afiliación (JWT subject = solicitante)
    solicitarAfiliacion(body: SolicitarAfiliacionRequest) {
        return this.http.post<AfiliacionResponse>(`${this.base}/cmd/afiliaciones/solicitar`, body);
    }

    aprobarAfiliacion(afiliacionId: string, body: DecisionAfiliacionRequest) {
        return this.http.patch<void>(`${this.base}/cmd/afiliaciones/${afiliacionId}/aprobar`, body ?? {});
    }

    rechazarAfiliacion(afiliacionId: string, body: DecisionAfiliacionRequest) {
        return this.http.patch<void>(`${this.base}/cmd/afiliaciones/${afiliacionId}/rechazar`, body ?? {});
    }

    // Endpoint de lectura (si lo publicas del QRY) – opcional:
    listarAfiliacionesZona(zonaId: string) {
        // Ajusta a tu API QRY si la expones (Mongo view)
        return this.http.get<AfiliacionResumen[]>(`${this.base}/qry/afiliaciones?zonaId=${encodeURIComponent(zonaId)}`);
    }
}
