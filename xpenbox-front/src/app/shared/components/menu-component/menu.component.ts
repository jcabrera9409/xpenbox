import { Component, output, signal } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { userState } from '../../../feature/user/service/user.state';
import { CommonModule } from '@angular/common';

interface MenuItem {
  label: string;
  icon: string;
  route: string;
  onlyMobile?: boolean;
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
  
  isMobileMenuOpen = signal<boolean>(false);

  private menuItems: MenuItem[] = [
    { label: 'Dashboard', icon: 'dashboard', route: '/landing', onlyMobile: true },
    { label: 'Transacciones', icon: 'receipt_long', route: '/landing/transaction', onlyMobile: true },
    { label: 'Cuentas', icon: 'account_balance_wallet', route: '/landing/account', onlyMobile: true },
    { label: 'Ingresos', icon: 'trending_up', route: '/landing/income', onlyMobile: false },
    { label: 'Categorías', icon: 'category', route: '/landing/category', onlyMobile: false },
    { label: 'Configuración', icon: 'settings', route: '/landing/settings', onlyMobile: true },
  ];

  get menuItemsDesktop(): MenuItem[] {
    return this.menuItems;
  }

  get menuItemsMobile(): MenuItem[] {
    return this.menuItems.filter(item => item.onlyMobile);
  }

  get userName(): string {
    const name = this.userState.userLogged()?.email || 'Usuario';
    return name.split('@')[0];
  }

  onQuickExpense(): void {
    this.openQuickExpense.emit();
  }

  toggleMobileMenu(): void {
    this.isMobileMenuOpen.set(!this.isMobileMenuOpen());
  }

  closeMobileMenu(): void {
    this.isMobileMenuOpen.set(false);
  }

}
