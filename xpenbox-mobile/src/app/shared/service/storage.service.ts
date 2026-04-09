import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class StorageService {

  private readonly STORAGE_HAS_LOGGED_BEFORE = 'hasLoggedBefore';

  public setHasLoggedBefore(value: boolean): void {
    localStorage.setItem(this.STORAGE_HAS_LOGGED_BEFORE, JSON.stringify(value));
  }

  public getHasLoggedBefore(): boolean {
    const value = localStorage.getItem(this.STORAGE_HAS_LOGGED_BEFORE);
    return value ? JSON.parse(value) : false;
  }

  public clearStorage(): void {
    localStorage.clear();
  }
}
