import { Injectable } from '@angular/core';
import {
  PushNotifications,
  Token,
  PushNotificationSchema,
  ActionPerformed
} from '@capacitor/push-notifications';
import { LocalNotifications } from '@capacitor/local-notifications';
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

    await LocalNotifications.requestPermissions();

    await PushNotifications.register();
    await PushNotifications.removeAllListeners();

    PushNotifications.addListener('registration', async (token: Token) => {
      console.log('Push registration success, token: ' + token.value);
      
      await this.capacitorService.setFcmToken(token.value);
      
      // Save the device token using the DevicetokenService
      this.deviceTokenService.create({ token: token.value, platform: 'ANDROID' }).subscribe({
        next: async () => {
          console.log('Device token saved successfully');
        },
        error: (error) => {
          console.error('Error saving device token:', error);
        }
      });
    });

    PushNotifications.addListener('pushNotificationReceived', async (notification: PushNotificationSchema) => {
      await LocalNotifications.schedule({
        notifications: [
          {
            id: Math.floor(Math.random() * 100000),
            title: notification.title || 'Notificación',
            body: notification.body || '',
            schedule: { at: new Date(Date.now() + 150) }
          }
        ]
      });
    });

    PushNotifications.addListener('pushNotificationActionPerformed', (notification: ActionPerformed) => {
      console.log(notification);
    });
  }
}
