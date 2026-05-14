import { Injectable } from '@angular/core';
import {
  PushNotifications,
  Token,
  PushNotificationSchema,
  ActionPerformed
} from '@capacitor/push-notifications';
import { DevicetokenService } from '../../device/service/devicetoken.service';
import { CapacitorService } from './capacitor.service';


@Injectable({
  providedIn: 'root',
})
export class PushService {
  constructor(
    private deviceTokenService: DevicetokenService,
    private capacitorService: CapacitorService
  ) {

  }

  async initialize() {
    const permission = await PushNotifications.requestPermissions();

    if (permission.receive !== 'granted') {
      console.log('Push notification permission not granted');
      return;
    }

    await PushNotifications.register();
    await PushNotifications.removeAllListeners();

    PushNotifications.addListener('registration', async (token: Token) => {
      console.log('Push registration success, token: ' + token.value);
      
      await this.capacitorService.setFcmToken(token.value);
      
      const platform = this.capacitorService.getPlatform();
      
      // Save the device token using the DevicetokenService
      this.deviceTokenService.create({ token: token.value, platform: platform }).subscribe({
        next: async () => {
          console.log('Device token saved successfully for platform: ' + platform);
        },
        error: (error) => {
          console.error('Error saving device token:', error);
        }
      });
    });

    PushNotifications.addListener('pushNotificationReceived', async (notification: PushNotificationSchema) => {
      // Cuando la notificación llega:
      // - En segundo plano: Firebase ya la muestra automáticamente
      // - En primer plano: No mostramos nada, el usuario ya está usando la app
      console.log('Push notification received:', notification.title);
      
      // Si necesitas manejar la notificación en primer plano (actualizar UI, etc.)
      // puedes agregar lógica aquí sin crear notificaciones locales
    });

    PushNotifications.addListener('pushNotificationActionPerformed', (notification: ActionPerformed) => {
      console.log(notification);
    });
  }
}
