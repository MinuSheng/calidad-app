# MEMORY MAP - Calidad App

## 1. Propósito y Arquitectura
- Sistema de control de calidad ('SNDR LABS').
- Monolito frontend SPA web y Wrapper Nativo Android con Burbuja Flotante Messenger.
- Versión activa: v4.0.27 (Native Diagnostics Log Viewer & File Picker Stability Fix).

## 2. Estructura y Módulos
- Web SPA: `index.html` (monolito v4.0.27), `index.backup.html`.
- Android App: `android-bubble-app/` (Kotlin, SDK 34, MinSdk 26, System Alert Window).
  * `AppLogManager.kt`: Singleton de logs en memoria (150 slots) y dispatcher a UI.
  * `MainActivity.kt`: Dashboard Cyber Cyan, visor nativo colapsable con código de color.
  * `BubbleService.kt`: Servicio flotante, captura de consola web y puente háptico.
  * `FilePickerActivity.kt`: Selector transparente blindado con FLAG_GRANT_READ_URI_PERMISSION.
  * `assets/index.html`: Fallback offline sincronizado con versión web v4.0.27.
- CI/CD: `.github/workflows/build-apk.yml` (GitHub Actions assembleDebug & artifact).

## 3. Stack y Dependencias
- Web: HTML5, CSS3 (Neón/Oscuro/Claro), JS Vanilla, SweetAlert2, SheetJS.
- Android: Kotlin 1.9.22, AGP 8.2.2, AndroidX, Material Components, Gradle 8.5.
- Persistencia: localStorage (26 claves: lala_caducidad_slots, lala_registros, etc.).

## 4. Funciones Críticas y Flujo de Datos
- Caducidad & Pack: `_fechaDeLote`, `calcularCaducidad`, `revisarFaltaProduccionPack`.
- Turnos & Merma: `inicializarNuevoTurnoOficial`, `confirmarCorteManual`, `registrarMermaInmediata`.
- Android Bridge: `AndroidBridge.vibrateAlert()`, `onShowFileChooser` protegido anti-freeze.
