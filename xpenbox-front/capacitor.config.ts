import type { CapacitorConfig } from '@capacitor/cli';

const config: CapacitorConfig = {
  appId: 'com.firstcode.xpenbox',
  appName: 'XpenBox',
  webDir: 'dist/xpenbox-front/browser',
  android: {
    backgroundColor: '#2b2b2b'
  },
  server: {
    androidScheme: 'http',
    cleartext: true
  },
  plugins: {
    SplashScreen: {
      launchShowDuration: 0,
      launchAutoHide: false,
      backgroundColor: '#000000',
      androidScaleType: 'CENTER_INSIDE',
      splashFullScreen: false
    },
    LocalNotifications: {
      smallIcon: 'ic_stat_notification',
      iconColor: '#2b2b2b'
    },
    PushNotifications: {
      presentationOptions: ['badge', 'sound', 'alert']
    }
  }
};

export default config;
