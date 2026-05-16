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
import { ModalButtonsUi } from '../../../shared/ui/modal-buttons-ui/modal-buttons.ui';
import { upgradeProModalState } from '../../subscription/state/upgrade-pro.modal.state';
import { IconComponent } from '../../../shared/components/icon.component/icon.component';

@Component({
  selector: 'app-category-edition-modal',
  imports: [CommonModule, ReactiveFormsModule, LoadingUi, RetryComponent, ModalButtonsUi, IconComponent],
  templateUrl: './category-edition.modal.html',
  styleUrl: './category-edition.modal.css',
})
export class CategoryEditionModal implements OnInit {

  resourceCodeSelected = input<string | null>();
  close = output<void>();

  categoryState = categoryState;
  categoryData = signal<CategoryResponseDTO | null>(null);

  formCategory!: FormGroup;

  hasBudget = signal<boolean>(false);

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
      }, error: (error) => {
        this.categoryState.isLoadingSendingCategory.set(false);
        if (error.status === 403) {
          if (error.error && error.error.featureCode) {
            this.showUpgradeProModal();
          } else {
            this.categoryState.errorSendingCategory.set('No tienes permiso para realizar esta acción. Por favor, contacta con soporte.');
          }
        } else {
          this.categoryState.errorSendingCategory.set('Error guardando la categoría. Por favor, inténtalo de nuevo más tarde.');
        }
      }
    });
  }

  onClose(): void {
    this.close.emit();
  }

  onChangeHasBudget(): void {
    this.hasBudget.set(!this.hasBudget());

    if (!this.hasBudget()) {
      this.formCategory.get('budget')?.clearValidators();
      this.formCategory.get('budget')?.updateValueAndValidity();
    } else {
      this.formCategory.get('budget')?.setValidators([Validators.required, Validators.min(1)]);
      this.formCategory.get('budget')?.updateValueAndValidity();
    }
  }

  private showUpgradeProModal(): void {
    upgradeProModalState.title.set('Alcanzaste el límite de categorías');
    upgradeProModalState.htmlMessage.set('Tu plan Free permite hasta 3 categorías. ' +
            'Actualiza a <strong>Pro</strong> y gestiona todas tus categorías sin restricciones.');
    upgradeProModalState.open.set(true);
  }

  private buildCategoryData(): CategoryRequestDTO {
    const formValues = this.formCategory.value;
    const categoryName = formValues['name'];
    const categoryColor = formValues['color'];
    const categoryHasBudget = this.hasBudget();
    const categoryBudget = this.hasBudget() ? formValues['budget'] : 0;

    return new CategoryRequestDTO(categoryName, categoryColor, categoryHasBudget, categoryBudget);
  }

  retryLoadCategoryData(): void {
    this.loadCategoryData();
  }

  private initForms(): void {
    this.formCategory = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(25)]],
      color: [this.generateRandomHexColor(), [Validators.required, Validators.pattern(/^#([0-9A-Fa-f]{6}|[0-9A-Fa-f]{3})$/)]],
      budget: [0]
    });
  }

  private loadCategoryData(): void {
    if (!this.isEditMode) return; 

    this.categoryState.isLoadingGetCategory.set(true);

    this.categoryService.getByResourceCode(this.resourceCodeSelected()!).subscribe({
      next: (response: ApiResponseDTO<CategoryResponseDTO>) => {
        if (response.success && response.data) {
          this.categoryData.set(response.data);
          this.hasBudget.set(response.data.hasBudget);
          this.formCategory.patchValue({
            name: response.data.name,
            color: response.data.color,
            budget: response.data.budget
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
