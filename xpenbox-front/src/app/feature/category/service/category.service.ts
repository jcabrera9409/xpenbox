import { Injectable } from '@angular/core';
import { EnvService } from '../../common/service/env.service';
import { HttpClient } from '@angular/common/http';
import { CategoryResponseDTO } from '../model/category.response.dto';
import { ApiResponseDTO } from '../../common/model/api.response.dto';
import { CategoryRequestDTO } from '../model/category.request.dto';
import { GenericService } from '../../common/service/generic.service';
import { categoryState } from './category.state';

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
   * Loads categories and updates the category state with the fetched data.
   * Handles loading and error states.
   */
  override load(): void {
    categoryState.isLoading.set(true);
    categoryState.error.set(null);

    this.getAll().subscribe({
      next: (response: ApiResponseDTO<CategoryResponseDTO[]>) => {
        categoryState.categories.set(response.data || []);
        categoryState.isLoading.set(false);
      },
      error: (error) => {
        console.error('Error fetching categories:', error);
        categoryState.error.set(error.message || 'Error fetching categories');
        categoryState.isLoading.set(false);
      }
    })
  }
}
