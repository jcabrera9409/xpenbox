import { Component, computed } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../feature/auth/service/auth.service';
import { userState } from '../../feature/user/service/user.state';

@Component({
  selector: 'app-settings-page',
  imports: [],
  templateUrl: './settings.page.html',
  styleUrl: './settings.page.css',
})
export class SettingsPage {

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  userState = userState;

  profileImageUrl = computed(() => {
    const email = userState.userLogged()?.email;
    if (!email) return null;
    
    const username = email.split('@')[0];
    return `https://api.dicebear.com/9.x/identicon/svg?seed=${username}`;
  });

  logout(): void {
    this.authService.logout().subscribe({
      next: () => {
        this.router.navigate(['/login']);
      },
      error: (error) => {
        console.error('Error al cerrar sesión:', error);
      }
    });
  }
}
