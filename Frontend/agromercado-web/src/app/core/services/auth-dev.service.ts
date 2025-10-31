// emisión de token dev /auth/dev/token
import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';
import { DevTokenRequest as DevTokenReq, DevTokenRes } from '../../features/accounts/models/accounts.models';

@Injectable({ providedIn: 'root' })
export class AuthDevService {
  constructor(private http: HttpClient) {}

  issueDevToken(req: DevTokenReq) {
    return this.http.post<DevTokenRes>(`${environment.apiBase}/auth/dev/token`, req);
  }
}