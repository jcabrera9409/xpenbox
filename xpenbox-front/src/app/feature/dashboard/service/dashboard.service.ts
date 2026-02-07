import { Injectable } from '@angular/core';
import { EnvService } from '../../common/service/env.service';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiResponseDTO } from '../../common/model/api.response.dto';
import { DashboardResponseModelDTO } from '../model/dashboard.response.model.dto';
import { PeriodFilterRequestDTO } from '../model/period-filter.request.dto';

/**
 * Service for handling dashboard-related operations.
 * Provides methods to generate dashboard data based on specified period filters.
 */
@Injectable({
  providedIn: 'root',
})
export class DashboardService {
  private apiUrl: string;

  constructor(
    private http: HttpClient,
    private envService: EnvService
  ) {
    this.apiUrl = `${this.envService.getApiUrl()}/dashboard`;
  }

  /**
   * Generate dashboard data based on the specified period filter.
   * @param period - The period filter for generating dashboard data.
   * @returns An Observable containing the API response with dashboard data.
   */
  generateDashboardData(period: PeriodFilterRequestDTO): Observable<ApiResponseDTO<DashboardResponseModelDTO>> {
    return this.http.get<ApiResponseDTO<DashboardResponseModelDTO>>(
      `${this.apiUrl}?period=${period}`,
      { withCredentials: true }
    );
  }
}
