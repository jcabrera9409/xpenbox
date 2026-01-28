import { Component, input, OnInit, output, signal } from '@angular/core';
import { CategoryResponseDTO } from '../../../feature/category/model/category.response.dto';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { CategoryService } from '../../../feature/category/service/category.service';
import { ApiResponseDTO } from '../../../feature/common/model/api.response.dto';
import { CategoryRequestDTO } from '../../../feature/category/model/category.request.dto';
import { CommonModule } from '@angular/common';
import { NotificationService } from '../../../feature/common/service/notification.service';
import { categoryState } from '../../../feature/category/service/category.state';
import { LoadingUi } from '../../../shared/ui/loading-ui/loading.ui';
import { RetryComponent } from '../../../shared/components/retry-component/retry.component';

@Component({
  selector: 'app-category-edition-modal',
  imports: [CommonModule, ReactiveFormsModule, LoadingUi, RetryComponent],
  templateUrl: './category-edition.modal.html',
  styleUrl: './category-edition.modal.css',
})
export class CategoryEditionModal implements OnInit {

  resourceCodeSelected = input<string | null>();
  close = output<void>();

  categoryState = categoryState;
  categoryData = signal<CategoryResponseDTO | null>(null);

  formCategory!: FormGroup;

  constructor(
    private fb: FormBuilder,
    private categoryService: CategoryService,
    private notificationService: NotificationService
  ) { }

  ngOnInit(): void {
    this.categoryState.isLoadingSendingCategory.set(false);
    this.categoryState.errorSendingCategory.set(null);

    this.loadCategoryData()
    this.initForms();
  }

  get isEditMode(): boolean {
    return this.resourceCodeSelected() !== null;
  }

  onSubmit() {
    if (this.formCategory.invalid) return;

    this.categoryState.isLoadingSendingCategory.set(true);
    this.categoryState.errorSendingCategory.set(null);

    const categoryData = this.buildCategoryData();

    const observable = this.isEditMode
      ? this.categoryService.update(this.resourceCodeSelected()!, categoryData)
      : this.categoryService.create(categoryData);

    observable.subscribe({
      next: (response: ApiResponseDTO<CategoryResponseDTO>) => {
        this.categoryState.isLoadingSendingCategory.set(false);

        if (response.success && response.data) {
          this.notificationService.success(`Categoria ${this.isEditMode ? 'actualizada' : 'creada'} con éxito.`);
          this.categoryService.refresh();
          this.close.emit();
        } else {
          this.categoryState.errorSendingCategory.set(response.message);
        }
      }, error: () => {
        this.categoryState.errorSendingCategory.set('Error guardando la categoría. Por favor, inténtalo de nuevo más tarde.');
        this.categoryState.isLoadingSendingCategory.set(false);
      }
    });
  }

  onClose(): void {
    this.close.emit();
  }

  private buildCategoryData(): CategoryRequestDTO {
    const formValues = this.formCategory.value;
    const categoryName = formValues['name'];
    const categoryColor = formValues['color'];

    return new CategoryRequestDTO(categoryName, categoryColor);
  }

  retryLoadCategoryData(): void {
    this.loadCategoryData();
  }

  private initForms(): void {
    this.formCategory = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(25)]],
      color: [this.generateRandomHexColor(), [Validators.required, Validators.pattern(/^#([0-9A-Fa-f]{6}|[0-9A-Fa-f]{3})$/)]],
    });
  }

  private loadCategoryData(): void {
    if (!this.isEditMode) return; 

    this.categoryState.isLoadingGetCategory.set(true);

    this.categoryService.getByResourceCode(this.resourceCodeSelected()!).subscribe({
      next: (response: ApiResponseDTO<CategoryResponseDTO>) => {
        if (response.success && response.data) {
          this.categoryData.set(response.data);
          this.formCategory.patchValue({
            name: response.data.name,
            color: response.data.color
          });
        } else {
          this.categoryState.errorGetCategory.set(response.message);
        }
        this.categoryState.isLoadingGetCategory.set(false);
      }, error: () => {
        this.categoryState.errorGetCategory.set('Error cargando los datos de la categoría. Por favor, inténtalo de nuevo más tarde.');
        this.categoryState.isLoadingGetCategory.set(false);
      }
    });
  }

  private generateRandomHexColor(): string {
    const hex = Math.floor(Math.random() * 0xffffff).toString(16).padStart(6, '0');
    return `#${hex.toUpperCase()}`;
  }

}
