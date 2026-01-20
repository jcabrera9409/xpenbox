import { Component, signal } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';

interface MenuItem {
  label: string;
  icon: string;
  route: string;
}

interface UserProfile {
  name: string;
  photo?: string;
}

@Component({
  selector: 'app-menu-component',
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './menu.component.html',
  styleUrl: './menu.component.css',
})
export class MenuComponent {
  menuItems: MenuItem[] = [
    { label: 'Dashboard', icon: 'dashboard', route: '/dashboard' },
    { label: 'Cuentas', icon: 'account_balance_wallet', route: '/cuentas' },
    { label: 'Categorías', icon: 'category', route: '/categorias' },
    { label: 'Configuración', icon: 'settings', route: '/configuracion' },
  ];

  userProfile = signal<UserProfile>({
    name: 'Usuario',
    photo: undefined,
  });

  notificationCount = signal<number>(3);

  onQuickExpense(): void {
    console.log('Registrar gasto rápido');
  }

  onLogout(): void {
    console.log('Cerrando sesión...');
  }

  getUserInitials(name: string): string {
    return name
      .split(' ')
      .map((n) => n[0])
      .join('')
      .toUpperCase()
      .slice(0, 2);
  }
}
