// manejo del token (front)
import { Injectable } from '@angular/core';
import { StorageService } from './storage.service';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly tokenKey = 'access_token';
  constructor(private storage: StorageService) {}

  setToken(token: string) {
    this.storage.setItem(this.tokenKey, token);
  }
  getToken(): string | null {
    return this.storage.getItem(this.tokenKey);
  }
  clear() {
    this.storage.removeItem(this.tokenKey);
  }
  isAuthenticated(): boolean {
    return !!this.getToken();
  }
}
