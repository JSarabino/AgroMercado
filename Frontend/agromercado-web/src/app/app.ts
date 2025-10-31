import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';

// Shared UI (que definimos antes en shared/ui)
import { NavbarComponent } from './shared/ui/navbar.component';
import { FooterComponent } from './shared/ui/footer.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, NavbarComponent, FooterComponent],
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App {
  protected readonly title = signal('agromercado-web');
}
