# MEMORY MAP - Calidad App

## 1. Propósito y Arquitectura
- Sistema de gestión de control de calidad ('SNDR LABS').
- Monolito frontend SPA web y Wrapper Nativo Android con Burbuja Flotante Messenger.
- Versión activa: v4.0.26 (Cyber Cyan UI, File Picker Bridge & Haptic Alerts).

## 2. Estructura y Módulos
- Web SPA: `index.html` (monolito activo v4.0.26), `index.backup.html`.
- Android App: `android-bubble-app/` (Kotlin, SDK 34, MinSdk 26, System Alert Window).
  * `MainActivity.kt`: Dashboard Cyber Cyan, permisos y preview de burbuja flotante.
  * `BubbleService.kt`: Servicio flotante en primer plano con icono oficial, JavaScriptInterface y FileChooser.
  * `FilePickerActivity.kt`: Actividad transparente que resuelve `onShowFileChooser` para backups.
  * `assets/index.html`: Fallback offline sincronizado con versión web v4.0.26.
- CI/CD: `.github/workflows/build-apk.yml` (GitHub Actions assembleDebug & artifact).

## 3. Stack y Dependencias
- Web: HTML5, CSS3 (Neón/Oscuro/Claro), JS Vanilla, SweetAlert2, SheetJS.
- Android: Kotlin 1.9.22, AGP 8.2.2, AndroidX, Material Components, Gradle 8.5.
- Persistencia: localStorage (26 claves: lala_caducidad_slots, lala_registros, etc.).

## 4. Funciones Críticas y Flujo de Datos
- Caducidad & Pack: `_fechaDeLote`, `calcularCaducidad`, `revisarFaltaProduccionPack` (con vibración háptica).
- Turnos & Merma: `inicializarNuevoTurnoOficial`, `confirmarCorteManual`, `registrarMermaInmediata`.
- Android Bridge: `AndroidBridge.vibrateAlert()`, `onShowFileChooser` con `FilePickerActivity`.
