import { Component, input, OnInit, output, signal, ViewChild, ElementRef, AfterViewInit, HostListener, effect } from '@angular/core';
import { TabObjectDTO } from '../../dto/tab-object.dto';
import { CommonModule } from '@angular/common';
import { IconComponent } from '../../components/icon.component/icon.component';

@Component({
  selector: 'app-tab-ui',
  imports: [CommonModule, IconComponent],
  templateUrl: './tab.ui.html',
  styleUrl: './tab.ui.css',
})
export class TabUi implements OnInit, AfterViewInit {
  @ViewChild('tabsContainer', { static: false }) tabsContainer!: ElementRef<HTMLDivElement>;

  tabsList = input<TabObjectDTO[]>();

  activeTabId = signal<string>('');
  showLeftArrow = signal<boolean>(false);
  showRightArrow = signal<boolean>(false);
  hasOverflow = signal<boolean>(false);
  canScrollLeft = signal<boolean>(false);
  canScrollRight = signal<boolean>(false);

  animationDirection = output<'left' | 'right'>();
  activeTabIdChange = output<string>();
  
  constructor() {
    // Recalcular cuando cambie la lista de tabs
    effect(() => {
      if (this.tabsList()) {
        setTimeout(() => this.updateArrowsVisibility(), 100);
      }
    });
  }

  ngOnInit(): void {
    this.activeTabId.set(this.tabsList()?.[0]?.id || '');
    this.activeTabIdChange.emit(this.activeTabId());
    this.animationDirection.emit('right');
  }

  ngAfterViewInit(): void {
    setTimeout(() => this.updateArrowsVisibility(), 100);
  }

  @HostListener('window:resize')
  onResize(): void {
    this.updateArrowsVisibility();
  }

  switchTab(tabId: string) {
    const currentIndex = this.tabsList()?.findIndex(tab => tab.id === this.activeTabId());
    const newIndex = this.tabsList()?.findIndex(tab => tab.id === tabId);

    if (currentIndex !== undefined && newIndex !== undefined) {
      this.animationDirection.emit(newIndex > currentIndex ? 'right' : 'left');
    }
    this.activeTabId.set(tabId);
    this.activeTabIdChange.emit(tabId);
  }

  scrollLeft(): void {
    if (this.tabsContainer) {
      const container = this.tabsContainer.nativeElement;
      container.scrollBy({ left: -200, behavior: 'smooth' });
      setTimeout(() => this.updateArrowsVisibility(), 300);
    }
  }

  scrollRight(): void {
    if (this.tabsContainer) {
      const container = this.tabsContainer.nativeElement;
      container.scrollBy({ left: 200, behavior: 'smooth' });
      setTimeout(() => this.updateArrowsVisibility(), 300);
    }
  }

  updateArrowsVisibility(): void {
    if (!this.tabsContainer) return;

    const container = this.tabsContainer.nativeElement;
    const hasOverflow = container.scrollWidth > container.clientWidth;

    // Agregar/remover clase para CSS responsive en mobile
    if (hasOverflow) {
      container.classList.remove('no-overflow');
    } else {
      container.classList.add('no-overflow');
    }

    this.hasOverflow.set(hasOverflow);

    if (!hasOverflow) {
      this.showLeftArrow.set(false);
      this.showRightArrow.set(false);
      this.canScrollLeft.set(false);
      this.canScrollRight.set(false);
      return;
    }

    // Si hay overflow, siempre mostrar las flechas
    this.showLeftArrow.set(true);
    this.showRightArrow.set(true);

    // Determinar si se puede hacer scroll en cada dirección
    const scrollLeft = container.scrollLeft;
    const maxScroll = container.scrollWidth - container.clientWidth;

    this.canScrollLeft.set(scrollLeft > 5);
    this.canScrollRight.set(scrollLeft < maxScroll - 5);
  }

  onScroll(): void {
    this.updateArrowsVisibility();
  }
}
