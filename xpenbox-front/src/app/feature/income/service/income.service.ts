import { Injectable } from '@angular/core';
import { GenericService } from '../../common/service/generic.service';
import { IncomeRequestDTO } from '../model/income.request.dto';
import { IncomeResponseDTO } from '../model/income.response.dto';
import { HttpClient } from '@angular/common/http';
import { EnvService } from '../../common/service/env.service';
import { incomeState } from './income.state';
import { ApiResponseDTO } from '../../common/model/api.response.dto';

@Injectable({
  providedIn: 'root',
})
export class IncomeService extends GenericService<IncomeRequestDTO, IncomeResponseDTO> {
  
  constructor(
    protected override http: HttpClient,
    protected envService: EnvService
  ) {
    super ( 
      http,
      `${envService.getApiUrl()}/income`
    )
  }

  getByDateRange(startDate: Date, endDate: Date) {
    const startTimestamp = startDate.getTime();
    const endTimestamp = endDate.getTime();

    return this.http.get<ApiResponseDTO<IncomeResponseDTO[]>>(
      `${this.apiUrl}/filter?from=${startTimestamp}&to=${endTimestamp}`, 
      { withCredentials: true }
    );
  }

  override load(): void {
    incomeState.isLoading.set(true);
    incomeState.error.set(null);

    const startDate = incomeState.startDate() || new Date(new Date().setDate(1));
    const endDate = incomeState.endDate() || new Date();

    this.getByDateRange(startDate, endDate).subscribe({
      next: (response: ApiResponseDTO<IncomeResponseDTO[]>) => {
        incomeState.incomes.set(response.data || []);
        incomeState.isLoading.set(false);
      },
      error: (error) => {
        console.error('Error fetching incomes:', error);
        incomeState.error.set(error.error.message || 'Error fetching incomes');
        incomeState.isLoading.set(false);
      }
    })
  }
}
