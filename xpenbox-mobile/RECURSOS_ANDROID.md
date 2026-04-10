# Guía de Configuración de Recursos Android - xpenbox

## ✅ ¿Qué se configuró?

Se han generado y configurado los siguientes recursos para tu aplicación Android:

### 1. **Íconos de Aplicación** 
- **Íconos adaptativos**: Compatible con Android 8.0+ (API 26+)
  - `ic_launcher_foreground.png` - Logo principal
  - `ic_launcher_background.png` - Fondo del ícono
- **Íconos tradicionales**: Para versiones anteriores de Android
  - `ic_launcher.png` - Ícono cuadrado
  - `ic_launcher_round.png` - Ícono circular
- **Densidades generadas**: ldpi, mdpi, hdpi, xhdpi, xxhdpi, xxxhdpi

### 2. **Splash Screens (Pantallas de Carga)**
- **Orientaciones**: Portrait (vertical) y Landscape (horizontal)
- **Temas**: Modo claro y modo oscuro
- **Todas las densidades**: Para diferentes tamaños de pantalla

### 3. **Configuración de Capacitor**
Actualizado [capacitor.config.ts](capacitor.config.ts) con:
- Background color para Android (#ffffff)
- Configuración del Splash Screen:
  - Duración: 2 segundos
  - Sin spinner de carga
  - Pantalla completa e inmersiva

---

## 🔄 Cómo actualizar los recursos

### Opción 1: Actualizar todo automáticamente

1. Reemplaza las imágenes en la carpeta `resources/`:
   - `icon.png` (1024x1024 px) - Tu nuevo logo
   - `splash.png` (2732x2732 px) - Tu nueva pantalla de carga

2. Ejecuta el comando:
   ```bash
   npm run resources
   ```

3. Sincroniza los cambios:
   ```bash
   npx cap sync android
   ```

### Opción 2: Personalización avanzada

Si necesitas íconos adaptativos personalizados:

1. Crea dos archivos separados:
   - `resources/icon-foreground.png` (1024x1024 px) - Solo el logo
   - `resources/icon-background.png` (1024x1024 px) - Color o patrón de fondo

2. Ejecuta los mismos comandos del método anterior

---

## 🎨 Recomendaciones de diseño

### Para el ícono (`icon.png`):
- **Tamaño**: 1024x1024 px
- **Formato**: PNG
- **Contenido**: Centrado, con padding de ~20% para evitar recortes
- **Colores**: Que contrasten bien con fondos claros y oscuros
- **Evitar**: Texto pequeño, detalles finos

### Para el splash (`splash.png`):
- **Tamaño**: 2732x2732 px
- **Área segura**: Contenido importante en el centro (1200x1200 px)
- **Fondo**: Color sólido o gradiente simple
- **Logo**: Centrado y dimensionado apropiadamente

---

## 📁 Estructura generada

\`\`\`
android/app/src/main/res/
├── mipmap-*/              # Íconos en diferentes densidades
│   ├── ic_launcher.png
│   ├── ic_launcher_round.png
│   ├── ic_launcher_foreground.png
│   └── ic_launcher_background.png
├── mipmap-anydpi-v26/     # Configuración de íconos adaptativos
│   ├── ic_launcher.xml
│   └── ic_launcher_round.xml
├── drawable*/             # Splash screens
│   └── splash.png
├── drawable-land-*/       # Splash landscape
│   └── splash.png
├── drawable-port-*/       # Splash portrait
│   └── splash.png
└── drawable-*-night*/     # Splash modo oscuro
    └── splash.png
\`\`\`

---

## 🛠️ Scripts disponibles

| Comando | Descripción |
|---------|-------------|
| `npm run resources` | Genera todos los íconos y splash screens |
| `npx cap sync android` | Sincroniza cambios con el proyecto Android |
| `npx cap run android` | Compila y ejecuta en dispositivo/emulador |
| `npx cap open android` | Abre Android Studio |

---

## 🔍 Verificar los cambios

1. **Compilar la app**:
   \`\`\`bash
   npx cap run android
   \`\`\`

2. **Revisar**:
   - El ícono aparece correctamente en el launcher
   - El splash screen se muestra al abrir la app
   - Los colores y tamaños lucen bien

---

## 💡 Tips adicionales

- **Cambiar nombre de la app**: Edita `appName` en [capacitor.config.ts](capacitor.config.ts)
- **Cambiar ID de la app**: Edita `appId` (ej. `com.tuempresa.xpenbox`)
- **Colores del splash**: Modifica `backgroundColor` en la configuración del SplashScreen
- **Duración del splash**: Ajusta `launchShowDuration` (en milisegundos)

---

## 📚 Recursos útiles

- [Documentación de Capacitor Assets](https://github.com/ionic-team/capacitor-assets)
- [Guías de diseño de Android](https://developer.android.com/guide/practices/ui_guidelines/icon_design_adaptive)
- [Generador de íconos online](https://romannurik.github.io/AndroidAssetStudio/)
