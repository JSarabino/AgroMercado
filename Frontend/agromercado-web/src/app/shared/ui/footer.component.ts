import { Component } from '@angular/core';

@Component({
  selector: 'app-footer',
  standalone: true,
  template: `
    <footer class="bg-light border-top mt-5">
      <div class="container py-3 text-center text-muted">
        <small>© {{year}} AgroMercado Local · MVP</small>
      </div>
    </footer>
  `
})
export class FooterComponent {
  year = new Date().getFullYear();
}