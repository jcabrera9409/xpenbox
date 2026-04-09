import { isPlatformBrowser } from "@angular/common";
import { DOCUMENT, inject, Injectable, OnDestroy, OnInit, PLATFORM_ID } from "@angular/core";

@Injectable()
export abstract class ModalGeneric implements OnInit, OnDestroy {
    private readonly document = inject(DOCUMENT);
    private readonly platformId = inject(PLATFORM_ID);

    ngOnInit(): void {
        if (isPlatformBrowser(this.platformId)) {
        this.document.body.style.overflow = 'hidden';
        }
    }

    ngOnDestroy(): void {
        if (isPlatformBrowser(this.platformId)) {
        this.document.body.style.overflow = '';
        }
    }
}