import { Injectable } from '@angular/core';
import { EnvService } from '../../common/service/env.service';
import { HttpClient } from '@angular/common/http';
import { LoginRequestDTO } from '../model/loginRequestDTO';
import { authState } from './auth.state';
import { Observable, tap, catchError, throwError } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private apiUrl: string;

  constructor(
    private http: HttpClient,
    private envService: EnvService
  ) {
    this.apiUrl = `${this.envService.getApiUrl()}/auth`;
  }

  login(credentials: LoginRequestDTO): Observable<void> {
    return this.http.post<void>(
      `${this.apiUrl}/login`,
      credentials,
      { withCredentials: true }
    );
  }
}
