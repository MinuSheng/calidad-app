# MEMORY MAP - Calidad App

## 1. Propósito y Arquitectura
- Sistema de control de calidad ('SNDR LABS').
- Monolito frontend SPA web y Wrapper Nativo Android con Burbuja Flotante Messenger.
- Versión activa: v4.0.32 (Puente Nativo Gmail Don Milton, Multi-Adjunto & Anti-Olvidos).

## 2. Estructura y Módulos
- Web SPA: `index.html` (monolito v4.0.32), `index.backup.html`.
- Android App: `android-bubble-app/` (Kotlin, SDK 34, MinSdk 26, System Alert Window).
  * `calidad.keystore`: Keystore permanente (alias calidad, SHA-256 fija para updates).
  * `AppLogManager.kt`: Singleton de logs en memoria (150 slots) y dispatcher a UI.
  * `MainActivity.kt`: Dashboard Cyber Cyan, visor nativo colapsable con código de color.
  * `BubbleService.kt`: Servicio flotante, puente Gmail SEND_MULTIPLE, MediaStore, monitor pack.
  * `FilePickerActivity.kt`: Selector transparente blindado con FLAG_GRANT_READ_URI_PERMISSION.
  * `assets/`: `index.html`, `plantilla.xlsx`, `plantilla_cestas.xlsx` offline sincronizados.
- CI/CD: `.github/workflows/build-apk.yml` (GitHub Actions assembleDebug & artifact).

## 3. Stack y Dependencias
- Web: HTML5, CSS3 (Neón/Oscuro/Claro), JS Vanilla, SweetAlert2, ExcelJS, html2canvas.
- Android: Kotlin 1.9.22, AGP 8.2.2, AndroidX, FileProvider, Gradle 8.5.
- Persistencia: localStorage (27 claves: lala_email_jefe, lala_paros, lala_reg_cestas, etc.), Firebase.

## 4. Funciones Críticas y Flujo de Datos
- Don Milton & Gmail: `enviarReporteDonMilton`, `configurarEmailJefe`, `enviarReporteGmail` (FileProvider URI).
- Muestreo & Reportes: `generarCapturaReporteEnMemoria`, `generarExcelYogurtEnMemoria`, `generarExcelCestasEnMemoria`.
- Paros & Cestas: `verHistorialParos`, `ordenarRegistrosCestas` (módulo 1440 por turno), `guardarCestas`.
- Ciclo de Turno: `inicializarNuevoTurnoOficial` (resetea correo), `limpiarTodoHistorialManual` (anti-olvidos).
