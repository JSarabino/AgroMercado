import { Injectable, inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';

@Injectable({ providedIn: 'root' })
export class StorageService {
  private readonly isBrowser = isPlatformBrowser(inject(PLATFORM_ID));
  private mem = new Map<string, string>(); // fallback en SSR

  getItem(key: string): string | null {
    if (this.isBrowser) return window.localStorage.getItem(key);
    return this.mem.get(key) ?? null;
  }

  setItem(key: string, value: string): void {
    if (this.isBrowser) { window.localStorage.setItem(key, value); return; }
    this.mem.set(key, value);
  }

  removeItem(key: string): void {
    if (this.isBrowser) { window.localStorage.removeItem(key); return; }
    this.mem.delete(key);
  }

  clear(): void {
    if (this.isBrowser) { window.localStorage.clear(); return; }
    this.mem.clear();
  }
}
