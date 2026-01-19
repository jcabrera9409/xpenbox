import { signal } from '@angular/core';

export const authState = {
  isLoading: signal<boolean>(false),
  isAuthenticated: signal<boolean>(false),
  error: signal<string | null>(null),
};
