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

  override load(): void {
    incomeState.isLoading.set(true);
    incomeState.error.set(null);

    this.getAll().subscribe({
      next: (response: ApiResponseDTO<IncomeResponseDTO[]>) => {
        incomeState.incomes.set(response.data || []);
        incomeState.isLoading.set(false);
      },
      error: (error) => {
        console.error('Error fetching incomes:', error);
        incomeState.error.set(error.message || 'Error fetching incomes');
        incomeState.isLoading.set(false);
      }
    })
  }
}
