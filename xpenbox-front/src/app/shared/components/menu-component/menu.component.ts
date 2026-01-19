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

  // User profile data - TODO: Obtener desde servicio de autenticación
  userProfile = signal<UserProfile>({
    name: 'Usuario',
    photo: undefined,
  });

  // Notification count - TODO: Obtener desde servicio de notificaciones
  notificationCount = signal<number>(3);

  onQuickExpense(): void {
    // TODO: Implementar lógica para abrir modal de registro rápido de gastos
    console.log('Registrar gasto rápido');
  }

  onLogout(): void {
    // TODO: Implementar lógica de logout
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
