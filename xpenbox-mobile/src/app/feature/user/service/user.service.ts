import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { EnvService } from '../../common/service/env.service';
import { ApiResponseDTO } from '../../common/model/api.response.dto';
import { UserResponseDTO } from '../model/user.response.dto';
import { Observable } from 'rxjs';
import { userState } from './user.state';
import { NotificationService } from '../../common/service/notification.service';

@Injectable({
  providedIn: 'root',
})
export class UserService {
  private apiUrl: string;

  constructor(
    private http: HttpClient,
    private envService: EnvService,
    private notificationService: NotificationService
  ) {
    this.apiUrl = `${this.envService.getApiUrl()}/user`;
  }

  getUserLoggedIn(): Observable<ApiResponseDTO<UserResponseDTO>> {
    return this.http.get<ApiResponseDTO<UserResponseDTO>>(`${this.apiUrl}/me`, { withCredentials: true });
  }

  loadUserLoggedIn(): void {
    userState.isLoading.set(true);
    this.getUserLoggedIn().subscribe({
      next: (response: ApiResponseDTO<UserResponseDTO>) => {
        userState.isLoading.set(false);
        if (response.success) {
          userState.userLogged.set(response.data);
        } else {
          userState.error.set(response.message);
          this.notificationService.warning(response.message);
        }
      },
      error: (error) => {
        userState.isLoading.set(false);
        userState.error.set('Error fetching user data.');
        this.notificationService.error('Error al obtener los datos del usuario.');
        console.error('Error fetching user data:', error);
      }
    });
  }
}
