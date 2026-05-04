# Recursos para Aplicación Móvil Android/iOS

Esta carpeta contiene las imágenes fuente para generar íconos y splash screens.

## Archivos necesarios:

### 1. icon.png
- **Tamaño recomendado**: 1024x1024 px
- **Formato**: PNG con fondo transparente o sólido
- **Uso**: Ícono de la aplicación que aparece en el teléfono

### 2. splash.png  
- **Tamaño recomendado**: 2732x2732 px
- **Formato**: PNG
- **Uso**: Pantalla de carga que se muestra al abrir la app
- **Nota**: El contenido importante debe estar en el centro (1200x1200px) para evitar recortes

### 3. icon-foreground.png (opcional)
- **Tamaño**: 1024x1024 px
- **Uso**: Parte del ícono adaptativo de Android (solo el logo)

### 4. icon-background.png (opcional)
- **Tamaño**: 1024x1024 px
- **Uso**: Fondo del ícono adaptativo de Android

## Cómo generar los recursos:

Una vez que coloques tus imágenes `icon.png` y `splash.png` en esta carpeta, ejecuta:

```bash
npm run resources
```

Esto generará automáticamente todos los tamaños necesarios para Android e iOS.

## Notas:
- Puedes usar las imágenes de la carpeta `public/` como base
- Asegúrate de que el logo sea visible en fondos claros y oscuros
