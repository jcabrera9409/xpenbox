import { Injectable } from '@angular/core';
import { EnvService } from '../../common/service/env.service';
import { HttpClient } from '@angular/common/http';
import { CategoryResponseDTO } from '../model/category.response.dto';
import { ApiResponseDTO } from '../../common/model/api.response.dto';
import { CategoryRequestDTO } from '../model/category.request.dto';
import { GenericService } from '../../common/service/generic.service';
import { categoryState } from './category.state';
import { CategoryBudgetUsageRequestDTO } from '../model/categorybudgetusage.request.dto';
import { Observable } from 'rxjs';

/**
 * Service for managing categories, including creating, updating, and fetching category data.
 * This service interacts with the backend API to perform CRUD operations on categories.
 * It also updates the category state to reflect the current list of categories.
 */
@Injectable({
  providedIn: 'root',
})
export class CategoryService extends GenericService<CategoryRequestDTO, CategoryResponseDTO> {
  
  constructor(
    protected override http: HttpClient,
    protected envService: EnvService
  ) {
    super ( 
      http,
      `${envService.getApiUrl()}/category`
    )
  }

  /**
   * Loads the budget usage information for categories and returns it as an observable.
   * This method is used to fetch the usage count and budget used for each category.
   * @returns An observable of the API response containing a list of CategoryBudgetUsageRequestDTO.
   */
  loadBudgetUsage(): void {
    categoryState.isLoadingGetBudgetUsage.set(true);
    categoryState.errorGetBudgetUsage.set(null);

    this.http.get<ApiResponseDTO<CategoryBudgetUsageRequestDTO[]>>(`${this.apiUrl}/budget-usage`,
      { withCredentials: true }
    ).subscribe({
      next: (response) => {
        categoryState.categoriesBudgetUsage.set(response.data || []);
        categoryState.isLoadingGetBudgetUsage.set(false);
      },
      error: (error) => {
        console.error('Error fetching category budget usage:', error);
        categoryState.errorGetBudgetUsage.set(error.message || 'Error fetching category budget usage');
        categoryState.isLoadingGetBudgetUsage.set(false);
      }
    });
  }

  /**
   * Refreshes the budget usage information by calling the loadBudgetUsage method.
   * This can be used to update the budget usage data after changes to categories or budgets.
   */
  refreshBudgetUsage(): void {
    this.loadBudgetUsage();
  }

  /**
   * Loads categories and updates the category state with the fetched data.
   * Handles loading and error states.
   */
  override load(): void {
    categoryState.isLoadingGetList.set(true);
    categoryState.errorGetList.set(null);

    this.getAll().subscribe({
      next: (response: ApiResponseDTO<CategoryResponseDTO[]>) => {
        categoryState.categories.set(response.data || []);
        categoryState.isLoadingGetList.set(false);
      },
      error: (error) => {
        console.error('Error fetching categories:', error);
        categoryState.errorGetList.set(error.message || 'Error fetching categories');
        categoryState.isLoadingGetList.set(false);
      }
    })
  }
}
