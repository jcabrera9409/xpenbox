import type { CapacitorConfig } from '@capacitor/cli';

const config: CapacitorConfig = {
  appId: 'com.firstcode.xpenbox',
  appName: 'xpenbox',
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
      splashFullScreen: true
    }
  }
};

export default config;
