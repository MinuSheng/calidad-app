# MEMORY MAP - Calidad App

## 1. Propósito y Arquitectura
- Sistema de control de calidad ('SNDR LABS').
- Monolito frontend SPA web y Wrapper Nativo Android con Burbuja Flotante Messenger.
- Versión activa: v4.0.28 (Keystore Fija, Descargas MediaStore, Select Táctil & Monitor Pack).

## 2. Estructura y Módulos
- Web SPA: `index.html` (monolito v4.0.28), `index.backup.html`.
- Android App: `android-bubble-app/` (Kotlin, SDK 34, MinSdk 26, System Alert Window).
  * `calidad.keystore`: Keystore permanente (alias calidad, SHA-256 fija para updates).
  * `AppLogManager.kt`: Singleton de logs en memoria (150 slots) y dispatcher a UI.
  * `MainActivity.kt`: Dashboard Cyber Cyan, visor nativo colapsable con código de color.
  * `BubbleService.kt`: Servicio flotante, puente MediaStore, monitor de pack y háptico.
  * `FilePickerActivity.kt`: Selector transparente blindado con FLAG_GRANT_READ_URI_PERMISSION.
  * `assets/index.html`: Fallback offline sincronizado con versión web v4.0.28.
- CI/CD: `.github/workflows/build-apk.yml` (GitHub Actions assembleDebug & artifact).

## 3. Stack y Dependencias
- Web: HTML5, CSS3 (Neón/Oscuro/Claro), JS Vanilla, SweetAlert2, SheetJS.
- Android: Kotlin 1.9.22, AGP 8.2.2, AndroidX, Material Components, Gradle 8.5.
- Persistencia: localStorage (26 claves: lala_caducidad_slots, lala_registros, etc.).

## 4. Funciones Críticas y Flujo de Datos
- Caducidad & Pack: `_fechaDeLote`, `calcularCaducidad`, `revisarFaltaProduccionPack`, monitor nativo.
- Turnos & Merma: `inicializarNuevoTurnoOficial`, `confirmarCorteManual`, `registrarMermaInmediata`.
- Android Bridge: `guardarArchivo()`, `confirmarPackRegistrado()`, `vibrateAlert()`, FilePicker.
- Polyfill Selector: interceptor táctil universal `#sndr-select-picker-modal` en modales flotantes.
