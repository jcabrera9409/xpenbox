import type { CapacitorConfig } from '@capacitor/cli';

const config: CapacitorConfig = {
  appId: 'com.firstcode',
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
      launchShowDuration: 2000,
      backgroundColor: '#2b2b2b',
      androidScaleType: 'CENTER_CROP',
      showSpinner: false,
      androidSpinnerStyle: 'large',
      spinnerColor: '#ffffff00',
      splashFullScreen: true,
      splashImmersive: true
    }
  }
};

export default config;
