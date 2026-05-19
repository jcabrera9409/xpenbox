import { isPlatformBrowser } from "@angular/common";
import { DOCUMENT, inject, Injectable, OnDestroy, OnInit, PLATFORM_ID } from "@angular/core";

@Injectable()
export abstract class ModalGeneric implements OnInit, OnDestroy {
    private readonly document = inject(DOCUMENT);
    private readonly platformId = inject(PLATFORM_ID);
    private scrollPosition = 0;

    ngOnInit(): void {
        if (isPlatformBrowser(this.platformId)) {
            this.scrollPosition = window.scrollY;
            this.document.documentElement.style.setProperty('overflow', 'hidden', 'important');
            this.document.body.style.setProperty('overflow', 'hidden', 'important');
            this.document.body.style.setProperty('position', 'fixed', 'important');
            this.document.body.style.setProperty('top', `-${this.scrollPosition}px`, 'important');
            this.document.body.style.setProperty('width', '100%', 'important');
        }
    }

    ngOnDestroy(): void {
        if (isPlatformBrowser(this.platformId)) {
            this.document.documentElement.style.removeProperty('overflow');
            this.document.body.style.removeProperty('overflow');
            this.document.body.style.removeProperty('position');
            this.document.body.style.removeProperty('top');
            this.document.body.style.removeProperty('width');
            window.scrollTo(0, this.scrollPosition);
        }
    }
}