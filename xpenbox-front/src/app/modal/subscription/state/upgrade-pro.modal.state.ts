import { signal } from "@angular/core";

export const upgradeProModalState = {
    open: signal<boolean>(false),
    title: signal<string | null>(null),
    htmlMessage: signal<string | null>(null),
};