import { Injectable } from "@angular/core";
import { Capacitor } from "@capacitor/core";
import { Preferences } from "@capacitor/preferences";

@Injectable({
    providedIn: 'root'
})
export class CapacitorService {
    
    private readonly REFRESH_TOKEN_KEY = 'refreshToken';

    isNativePlatform(): boolean {
        return Capacitor.isNativePlatform();
    }

    async setRefreshToken(value: string): Promise<void> {
        await Preferences.set({ 
            key: this.REFRESH_TOKEN_KEY, 
            value: value 
        });
    }

    async getRefreshToken(): Promise<string | null> {
        const { value } = await Preferences.get({ key: this.REFRESH_TOKEN_KEY });
        return value;
    }

    async clearRefreshToken(): Promise<void> {
        await Preferences.remove({ key: this.REFRESH_TOKEN_KEY });
    }
}