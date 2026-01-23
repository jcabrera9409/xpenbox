import { Component, input, OnInit, output, signal } from '@angular/core';
import { CategoryResponseDTO } from '../../feature/category/model/categoryResponseDTO';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { CategoryService } from '../../feature/category/service/category.service';
import { ApiResponseDTO } from '../../feature/common/model/apiResponseDTO';
import { CategoryRequestDTO } from '../../feature/category/model/categoryRequestDTO';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-category-edition-modal',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './category-edition.modal.html',
  styleUrl: './category-edition.modal.css',
})
export class CategoryEditionModal implements OnInit {

  resourceCodeSelected = input<string | null>();
  close = output<void>();

  categoryData = signal<CategoryResponseDTO | null>(null);

  formCategory!: FormGroup;
  loading = signal<boolean>(false);
  sendingForm = signal<boolean>(false)
  errorMessage = signal<string | null>(null);

  constructor(
    private fb: FormBuilder,
    private categoryService: CategoryService
  ) { }

  ngOnInit(): void {
    this.loadCategoryData()
    this.initForms();
  }

  get isEditMode(): boolean {
    return this.resourceCodeSelected() !== null;
  }

  onSubmit() {
    if (this.formCategory.invalid) return;

    this.sendingForm.set(true);
    this.errorMessage.set(null);

    const categoryData = this.buildCategoryData();

    const observable = this.isEditMode
      ? this.categoryService.update(this.resourceCodeSelected()!, categoryData)
      : this.categoryService.create(categoryData);

    observable.subscribe({
      next: (response: ApiResponseDTO<CategoryResponseDTO>) => {
        this.sendingForm.set(false);

        if (response.success && response.data) {
          this.categoryService.refresh();
          this.close.emit();
        } else {
          console.error('Error saving category:', response);
          this.errorMessage.set('Error saving category: ' + response.message);
        }
      }, error: (error) => {
        console.error('Error saving category:', error);
        this.errorMessage.set('Error saving category: ' + error.message);
        this.sendingForm.set(false);
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

  private initForms(): void {
    this.formCategory = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(25)]],
      color: [this.generateRandomHexColor(), [Validators.required, Validators.pattern(/^#([0-9A-Fa-f]{6}|[0-9A-Fa-f]{3})$/)]],
    });
  }

  private loadCategoryData(): void {
    if (!this.isEditMode) return; 

    this.loading.set(true);

    this.categoryService.getByResourceCode(this.resourceCodeSelected()!).subscribe({
      next: (response: ApiResponseDTO<CategoryResponseDTO>) => {
        if (response.success && response.data) {
          this.categoryData.set(response.data);
          this.formCategory.patchValue({
            name: response.data.name,
            color: response.data.color
          });
        } else {
          console.error('Error loading category data:', response);
          this.errorMessage.set('Error loading category data: ' + response.message);
        }
        this.loading.set(false);
      }, error: (error) => {
        console.error('Error loading category data:', error);
        this.errorMessage.set('Error loading category data: ' + error.message);
        this.loading.set(false);
      }
    });
  }

  private generateRandomHexColor(): string {
    const hex = Math.floor(Math.random() * 0xffffff).toString(16).padStart(6, '0');
    return `#${hex.toUpperCase()}`;
  }

}
