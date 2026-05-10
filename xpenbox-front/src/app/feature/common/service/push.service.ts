import { Injectable } from '@angular/core';
import {
  PushNotifications,
  Token,
  PushNotificationSchema,
  ActionPerformed
} from '@capacitor/push-notifications';
import { DevicetokenService } from '../../device/service/devicetoken.service';


@Injectable({
  providedIn: 'root',
})
export class PushService {
  constructor(
    private deviceTokenService: DevicetokenService
  ) {

  }

  async initialize() {
    const permission = await PushNotifications.requestPermissions();

    if (permission.receive === 'granted') {
      return;
    }

    await PushNotifications.register();

    PushNotifications.addListener('registration', async (token: Token) => {
      console.log('Push registration success, token: ' + token.value);

      // Save the device token using the DevicetokenService
      this.deviceTokenService.create({ token: token.value, platform: 'ANDROID' }).subscribe({
        next: () => {
          console.log('Device token saved successfully');
        },
        error: (error) => {
          console.error('Error saving device token:', error);
        }
      });
    });

    PushNotifications.addListener('pushNotificationReceived', (notification: PushNotificationSchema) => {
      console.log(notification);
    });

    PushNotifications.addListener('pushNotificationActionPerformed', (notification: ActionPerformed) => {
      console.log(notification);
    });
  }
}
