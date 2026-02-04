import { signal } from "@angular/core";


export const genericState = {
    // Receipt Modal State
    showReceiptModal: signal<boolean>(false),
    titleReceiptModal: signal<string>(''),
    contentReceiptModal: signal<string>(''),
    buttonTextReceiptModal: signal<string>(''),
    buttonIconReceiptModal: signal<string>(''),
};