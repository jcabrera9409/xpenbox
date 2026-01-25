import { Component, output, signal } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { userState } from '../../../feature/user/service/user.state';
import { CommonModule } from '@angular/common';

interface MenuItem {
  label: string;
  icon: string;
  route: string;
}

@Component({
  selector: 'app-menu-component',
  imports: [CommonModule, RouterLink, RouterLinkActive],
  templateUrl: './menu.component.html',
  styleUrl: './menu.component.css',
})
export class MenuComponent {

  userState = userState;

  openQuickExpense = output<void>();

  notificationCount = signal<number>(3);

  menuItems: MenuItem[] = [
    { label: 'Dashboard', icon: 'dashboard', route: '/landing' },
    { label: 'Cuentas', icon: 'account_balance_wallet', route: '/landing/account' },
    { label: 'Categorías', icon: 'category', route: '/landing/category' },
    { label: 'Configuración', icon: 'settings', route: '/landing/settings' },
  ];

  get userName(): string {
    const name = this.userState.userLogged()?.email || 'Usuario';
    return name.split('@')[0];
  }

  onQuickExpense(): void {
    this.openQuickExpense.emit();
  }

}
