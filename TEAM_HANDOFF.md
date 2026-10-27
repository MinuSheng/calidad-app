## Corrección Maestra v4.0.31 - Horas Editables, Recálculo en Paros y Orden Cronológico de Turno en Cestas
- **Fecha y Hora:** 2026-10-27 13:45:00
- **Versión:** v4.0.31 (versionCode 7)
- **Problemas Erradicados y Solución Implementada:**
  1. **Desbloqueo de Horas en Ventana Flotante Overlay:** Se erradicó la dependencia de `type="time"` que disparaba diálogos nativos bloqueados por Android en `TYPE_APPLICATION_OVERLAY`. Se reemplazó por inputs táctiles formateados "HH:MM" con `inputmode="numeric"` y máscara reactiva en Cestas y Paros.
  2. **Reordenamiento Cronológico Inteligente por Turno en Cestas:** Implementación de cálculo de minutos transcurridos relativo al inicio del turno oficial: `(minutosRegistro - minutosInicioTurno + 1440) % 1440` (Turno 1 / A: 07:00, Turno 2 / B: 15:00, Turno 3 / C: 23:00 con cruce de medianoche, o 00:00-23:59 sin turno). Re-renderizado instantáneo de la tabla y totales acumulados.
  3. **Edición Completa y Recálculo en Tiempo Real de Paros:** En "Ver/Editar Paros", campos editables para Hora Inicio y Hora Fin con recálculo automático de la duración en minutos (`Math.round((fin - inicio) / 60000)`), soporte para cruce de medianoche, persistencia en `localStorage` / Firebase y ordenamiento cronológico.
  4. **Panel Resumen de Tiempo Muerto:** Inclusión de banner con cálculo acumulado de tiempo muerto total (minutos y horas) y cantidad de paros en el historial.
  5. **Preservación de Firma Permanente:** Se conserva intacta la firma fija con `calidad.keystore` en `android-bubble-app/app/build.gradle` elevando a `versionCode 7` y `versionName "4.0.31"`.
  6. **Sincronización Total de Assets Offline:** Actualización de `android-bubble-app/app/src/main/assets/index.html` con el código monolito unificado.


## Corrección Visual y de Flujo v4.0.30 - Selector Acordeón In-Flow y Cero Colisiones en Modales
- **Fecha y Hora:** 2026-10-23 17:50:37
- **Versión:** v4.0.30 (versionCode 6)
- **Problema Erradicado y Solución Arquitectónica:**
  1. **Erradicación de Superposición por `position: absolute`:** En v4.0.29, el menú desplegable absoluto causaba colisiones visuales con los botones de acción (.swal2-actions), tapando opciones inferiores como "Posimat", "Hared Ruiz" y "Daniel".
  2. **Motor de Acordeón In-Flow (Flujo Natural):** Se estableció `.custom-select-dropdown` con `position: relative !important; width: 100% !important; margin-top: 8px !important; margin-bottom: 8px !important; z-index: 10 !important;`. Al desplegarse en flujo estático/relativo, empuja físicamente los botones y campos siguientes hacia abajo, garantizando que NINGÚN BOTÓN PUEDA TAPAR LAS OPCIONES.
  3. **Estilo Neón y Scroll Táctil:** Contenedor de opciones con borde cian neón (`1.5px solid #00F2FE`), fondo `#0F172A`, sombra luminosa `0 4px 15px rgba(0,242,254,0.25)` y scroll táctil suave (`max-height: 190px`).
  4. **Búsqueda Reactiva (>4 Opciones):** Buscador táctil en cabecera con autofocus y filtrado en vivo para selectores de más de 4 opciones.
  5. **Escudo de Apilamiento y Scroll en Modales:** Reglas globales para `.swal2-popup` (`overflow-y: auto !important; max-height: 90vh !important;`), `.swal2-html-container` (`overflow: visible !important; position: relative !important; z-index: 5 !important;`) y `.swal2-actions` (`position: relative !important; z-index: 1 !important; margin-top: 14px !important;`). Fijación estricta de scroll en `document.body`.
  6. **Preservación de Firma Permanente:** Se mantiene la firma con `calidad.keystore` en `build.gradle` elevando a `versionCode 6` y `versionName "4.0.30"`.


## Corrección Crítica v4.0.29 - Motor de Selectores Adheridos In-Place y Bloqueo de Scroll de Fondo
- **Fecha y Hora:** 2026-10-23 17:31:03
- **Versión:** v4.0.29 (versionCode 5)
- **Problemas Erradicados y Solución Implementada:**
  1. **Erradicación del Bottom-Sheet Anterior:** Se eliminó por completo el modal flotante en `document.body` de la v4.0.28 que quedaba atrapado detrás de SweetAlert2 o destruía modales al detectar eventos fuera del contenedor en "Añadir Compañero" y "Entregar Turno".
  2. **Motor In-Place Adherido al DOM:** Cada `<select>` se oculta de forma accesible y se sustituye por un contenedor `.custom-select-container` insertado en su mismo nodo padre. El menú `.custom-select-dropdown` se despliega posicionado de forma absoluta directamente debajo del disparador con `z-index: 999999`, borde neón `#38BDF8`, esquinas redondeadas y scroll táctil interno (`max-height: 210px`).
  3. **Búsqueda Reactiva (>5 Opciones):** Se integra automáticamente un campo de búsqueda en vivo con autofocus (`🔍 Buscar...`) cuando el selector supera 5 elementos.
  4. **Reactividad Dinámica (MutationObserver):** Se detecta la apertura de modales nuevos e inyecciones dinámicas de `<option>` (como la actualización de Motivos al cambiar de Máquina en "Iniciar Nuevo Paro") y se sincroniza la visibilidad con `style.display`.
  5. **Bloqueo Estricto del Scroll de Fondo:** Al abrir menús o modales SweetAlert2 (`body.swal2-shown`), se bloquea el deslizamiento de la página principal (`overflow: hidden; touch-action: none;`) y se cancelan eventos `touchmove` no dirigidos a la lista de opciones.
  6. **Preservación de Firma:** Se mantiene intacta la firma fija con `calidad.keystore` en `android-bubble-app/app/build.gradle` elevando a `versionCode 5` y `versionName "4.0.29"`.


## Refactorización Maestra v4.0.28 - Firma Permanente, MediaStore, Select Táctil y Monitor Pack
- **Fecha y Hora:** 2026-10-23 17:08:50
- **Versión:** v4.0.28
- **Objetivos Implementados:**
  1. **Keystore Fija Permanente:** Generación de `android-bubble-app/app/calidad.keystore` (alias `calidad`, pass `sndrlabs`, RSA 2048, 10000 días) configurada en `signingConfigs.calidadKey` tanto para debug como para release. Permite actualizar el APK sobre versiones previas sin desinstalar ni perder datos locales.
  2. **Puente Nativo MediaStore.Downloads:** Exposición de `@JavascriptInterface guardarArchivo(base64Data, nombreArchivo, mimeType)` en `BubbleService.kt`, escribiendo en la carpeta pública `Downloads` mediante `MediaStore.Downloads` (Android 10+) o fallback directo, con notificación interactiva `Intent.ACTION_VIEW` (`FLAG_GRANT_READ_URI_PERMISSION`).
  3. **Monitor Horario de Production Pack:** Bucle en Kotlin dentro de `BubbleService.kt` (cada 30s) que evalúa si el reloj marca hora en punto (`:00`, excluyendo 07:00, 15:00 y 23:00) y si el pack no ha sido anotado, emite vibración nativa `[0, 500, 250, 500]` ms en segundo plano con la burbuja cerrada. Exposición de `confirmarPackRegistrado(hora)` en `AndroidBridge`.
  4. **Polyfill Universal Táctil para `<select>`:** Interceptor en fase de captura sobre eventos `pointerdown` / `click` en cualquier `<select>` (estático o dentro de SweetAlert2 en Paros, Peso, Compañeros, Máquinas), desplegando una hoja modal flotante Dark Slate / Neón (`#0F172A`, `#00F2FE`, z-index 999999999) con filtrado instantáneo y botones táctiles grandes, resolviendo bloqueos del sistema en ventanas flotantes.
  5. **Corrección SweetAlert2 y Sincronización:** Eliminación de advertencias de consola por `backdrop: false` en toasts de SweetAlert2, bump general a `v4.0.28`, actualización de assets offline y workflow de CI.

# TEAM HANDOFF - Calidad App

## Auditoría Inicial por Antigravity
- **Fecha y Hora:** 2026-10-21 19:58:13
- **Directorio de Trabajo:** `C:\Users\Ssnaydher\Documents\Calidad App`
- **Rama Activa:** `No detectada`
- **Último Commit:** `Sin commits`

### 1. Estado de Git
```text
Limpio (Clean working tree)
```

### 2. Inventario de Archivos y Métricas
| Archivo | Líneas | Tamaño (bytes) |
|---|---|---|
| `index.html` | 6634 | 546596 |

### 3. Stack Técnico y CDNs Detectados
- **Librerías identificadas:** SweetAlert2, SheetJS (XLSX)
- **Recursos externos en index.html:**
  - `https://cdnjs.cloudflare.com/ajax/libs/html2canvas/1.4.1/html2canvas.min.js`
  - `manifest.json`
  - `https://cdnjs.cloudflare.com/ajax/libs/exceljs/4.4.0/exceljs.min.js`
  - `https://cdn.jsdelivr.net/npm/sweetalert2@11`

### 4. Estructura del DOM y Secciones Principales
- **Título de la aplicación:** SNDR LABS - Gestión de Calidad
- **Total de IDs identificados:** 199
  - `div#loading-overlay`
  - `div#loader-spinner`
  - `div#loading-text`
  - `div#security-overlay`
  - `h2#security-box-title`
  - `p#security-box-msg`
  - `strong#display-device-id`
  - `div#login-error-msg`
  - `input#clave-acceso`
  - `button#btn-cerrar-security-in-app`
  - `div#operator-login-overlay`
  - `div#operario-login-error`
  - `input#operario-email`
  - `input#operario-password`
  - `div#perfil-overlay`
  - `strong#perfil-nombre`
  - `strong#perfil-correo`
  - `strong#perfil-device-id`
  - `strong#perfil-licencia`
  - `div#peso-preview-overlay`
  - `div#peso-preview-scroll`
  - `div#peso-preview-contenido`
  - `div#admin-panel-overlay`
  - `p#admin-panel-subtitulo`
  - `div#admin-panel-tabla-container`
  - `div#sincronizacion-overlay`
  - `strong#sync-resumen`
  - `div#sync-lista-container`
  - `button#sync-btn-todos`
  - `div#nombre-overlay`
  - `div#nombre-operario-error`
  - `input#nombre-operario-input`
  - `div#sidebar-overlay`
  - `div#sidebar`
  - `span#sidebar-header-nombre`
  - `span#dark-mode-icon`
  - `span#dark-mode-text`
  - `div#menu-panel-admin`
  - `input#file-import`
  - `div#main-app-container`
  - `div#current-time-block`
  - `div#banner-edicion`
  - `span#banner-fecha`
  - `button#btn-tab-yogurt`
  - `button#btn-tab-cestas`
  - `button#btn-tab-caducidad`
  - `button#btn-tab-peso`
  - `div#tab-yogurt`
  - `div#card-inicio-turno`
  - `div#dd-turno`
  - `div#lbl-turno`
  - `input#select-turno`
  - `input#verificador`
  - `div#timer-banner`
  - `div#missing-pack-banner`
  - `button#btn-lavado-toggle`
  - `div#dd-producto`
  - `div#lbl-producto`
  - `input#producto`
  - `input#producto-custom`
  - `input#lote`
  - `div#dd-etiqueta`
  - `div#lbl-etiqueta`
  - `input#etiqueta`
  - `input#estado-turno-lbl`
  - `button#btn-aplicar-cambio-lote`
  - `span#lbl-malSellado`
  - `span#lbl-sobrellenado`
  - `span#lbl-volumenBajo`
  - `div#contenedor-tabla-registros`
  - `tbody#tabla-cuerpo`
  - `td#tot-pack`
  - `td#tot-mal`
  - `td#tot-sobre`
  - `td#tot-bajo`
  - `div#conversion-box`
  - `div#merma-box`
  - `button#btn-completar-turno`
  - `div#tab-cestas`
  - `input#obs-cestas`
  - `span#lbl-cestas-rev`
  - `span#lbl-cestas-rech`
  - `button#btn-detener-revision-cestas`
  - `tbody#tabla-cuerpo-cestas`
  - `td#tot-ces-rev`
  - `td#tot-ces-rech`
  - `td#tot-ces-suc`
  - `td#tot-ces-que`
  - `div#tab-caducidad`
  - `input#input-lote-caducidad`
  - `div#dd-caducidad`
  - `div#lbl-caducidad`
  - `input#select-cat-caducidad`
  - `div#resultado-caducidad`
  - `div#tab-peso`
  - `div#peso-cargando`
  - `div#peso-principal`
  - `button#btn-peso-sub-uht`
  - `button#btn-peso-sub-derivados`
  - `div#peso-setup`
  - `div#dd-peso-maquina`
  - `div#lbl-peso-maquina`
  - `div#opciones-peso-maquina`
  - `input#select-peso-maquina`
  - `div#dd-peso-producto`
  - `div#lbl-peso-producto`
  - `div#opciones-peso-producto`
  - `input#select-peso-producto`
  - `input#input-peso-lote`
  - `input#input-peso-vence`
  - `input#input-peso-bocas`
  - `div#peso-trabajo`
  - `div#peso-equipo-banner`
  - `div#peso-recibir-cta`
  - `p#peso-info-actual`
  - `div#peso-control-30`
  - `span#peso-proximo-control`
  - `div#peso-estado-paro`
  - `div#peso-botones-equipo`
  - `div#peso-botones-edicion`
  - `button#btn-peso-paro-reinicio`
  - `button#btn-peso-papelera`
  - `div#peso-tabla-container`
  - `input#swal-hora-inicio`
  - `input#swal-maquina`
  - `input#swal-motivo`
  - `input#swal-minutos`
  - `select#dd-paro-maquina`
  - `div#container-motivo`
  - `select#swal-select-error`
  - `input#paro-motivo-manual`
  - `textarea#reporte-final-whatsapp`
  - `clipPath#baldeClip`
  - `rect#balde-fill-rect`
  - `div#balde-porcentaje-label`
  - `input#edit-cambio-hora`
  - `input#edit-cambio-producto`
  - `input#edit-cambio-etiqueta`
  - `input#edit-cambio-lote`
  - `input#edit-esp-hora`
  - `input#edit-esp-lote`
  - `input#edit-cab-hora`
  - `input#edit-cab-producto`
  - `input#edit-cab-etiqueta`
  - `input#edit-cab-lote`
  - `button#btn-arch-descargar`
  - `button#btn-arch-compartir`
  - `input#edit-c-hora`
  - `input#edit-c-rev`
  - `input#edit-c-rech`
  - `input#edit-c-suc`
  - `input#edit-c-que`
  - `input#edit-c-obs`
  - `div#peso-formato-visual`
  - `div#peso-tabla-trabajo`
  - `select#peso-nuevolote-bloque`
  - `select#peso-nuevolote-producto`
  - `input#peso-nuevolote-lote`
  - `input#peso-nuevolote-vence`
  - `input#peso-rev-b`
  - `input#pp-hora`
  - `input#pp-causa`
  - `input#pr-hora`
  - `input#pr-obs`
  - `select#peso-rev-bloque`
  - `input#peso-rev-hora`
  - `input#peso-rev-paro`
  - `div#peso-rev-causa-box`
  - `input#peso-rev-causa`
  - `input#peso-rev-accion`
  - `input#peso-rev-tecnico`
  - `div#peso-rev-pesos`
  - `div#peso-rev-pesos-container`
  - `input#peso-rev-fuera`
  - `div#peso-rev-ajuste-box`
  - `textarea#peso-rev-ajuste-obs`
  - `div#peso-rev-post-container`
  - `input#peso-edit-fin-hora`
  - `input#peso-edit-hora`
  - `input#peso-edit-paro`
  - `div#peso-edit-causa-box`
  - `input#peso-edit-causa`
  - `input#peso-edit-accion`
  - `input#peso-edit-tecnico`
  - `div#peso-edit-pesos`
  - `input#peso-edit-b`
  - `select#peso-editbl-maquina`
  - `input#peso-editbl-bocas`
  - `input#peso-editbl-producto`
  - `input#peso-editbl-lote`
  - `select#peso-fin-bloque`
  - `input#peso-fin-hora`
  - `select#peso-cambio-maquina`
  - `select#peso-cambio-producto`
  - `input#peso-cambio-lote`
  - `input#peso-cambio-vence`
  - `input#peso-cambio-bocas`
  - `select#peso-colab-select`
  - `select#peso-entrega-select`

### 5. Catálogo de Funciones JavaScript
- **Total funciones declaradas:** 230
  - `_fechaDeLote()`
  - `categoriaVidaUtilPeso()`
  - `obtenerDiasProducto()`
  - `switchTab()`
  - `descargarImagenReporte()`
  - `toggleDropdown()`
  - `setDropdown()`
  - `sincronizarDropdownPorValor()`
  - `toggleSidebar()`
  - `esTemaOscuro()`
  - `initDarkMode()`
  - `toggleDarkMode()`
  - `aplicarTema()`
  - `actualizarBotonDarkMode()`
  - `verAcercaDeSNDR()`
  - `verHistorialVersiones()`
  - `calcularCaducidad()`
  - `exportarRespaldoLocal()`
  - `procesarDescargaRespaldo()`
  - `importarRespaldoLocal()`
  - `guardarTurnoPasado()`
  - `sincronizarTurnoConNube()`
  - `marcarSincronizacionTurno()`
  - `reintentarSincronizacion()`
  - `abrirSincronizacion()`
  - `cerrarSincronizacion()`
  - `renderizarPanelSincronizacion()`
  - `sincronizarTodosPendientes()`
  - `reintentarSincronizacionesPendientes()`
  - `verTurnosPasados()`
  - `mostrarDetalleTurnoPasado()`
  - `borrarTurnoPasado()`
  - `reanudarTurno()`
  - `guardarEdicionRespaldo()`
  - `actualizarRespaldoExistente()`
  - `restaurarTurnoTemporal()`
  - `salirModoEdicion()`
  - `btnDown()`
  - `btnUp()`
  - `btnLeave()`
  - `btnMinusDown()`
  - `btnMinusUp()`
  - `btnMinusLeave()`
  - `obtenerNombreDefecto()`
  - `abrirInputNumerico()`
  - `abrirInputRestar()`
  - `guardarEnMemoria()`
  - `cargarConfiguracionPrevia()`
  - `ordenarRegistrosCronologicamente()`
  - `actualizarLabelEstadoTurno()`
  - `detectarCambioManual()`
  - `aplicarCambioDatosLoteAhora()`
  - `getProductName()`
  - `increment()`
  - `decrement()`
  - `inyectarTurnoSimuladoDePrueba()`
  - `mostrarBannerSimulacion()`
  - `salirModoSimulacion()`
  - `obtenerFechaOperativaTurno()`
  - `inicializarNuevoTurnoOficial()`
  - `alternarModoLavadoParo()`
  - `actualizarBotonLavadoUI()`
  - `actualizarBotonParoUI()`
  - `verHistorialParos()`
  - `editarParo()`
  - `eliminarParo()`
  - `guardarParosEnMemoria()`
  - `abrirModalRegistrarParo()`
  - `copiarReporteParos()`
  - `registrarMermaInmediata()`
  - `seleccionarFraccionBalde()`
  - `actualizarVisualBalde()`
  - `actualizarPantallaContadores()`
  - `esHoraMayor()`
  - `ejecutarRelojYAlertas()`
  - `revisarFaltaProduccionPack()`
  - `ejecutarCorteAutomaticoExacto()`
  - `confirmarCorteManual()`
  - `cambiarProductionPackFila()`
  - `alternarAlmuerzoFila()`
  - `abrirMenuOpcionesFila()`
  - `abrirVentanaEdicionEspecial()`
  - `abrirVentanaEdicionManualFila()`
  - `_firmaYogurt()`
  - `_actualizarTextoCambioDatos()`
  - `corregirSeccionYogurt()`
  - `editarCabeceraSeccion()`
  - `descripcionCambioAplicado()`
  - `renderizarTabla()`
  - `_esc()`
  - `_dataUrlABlob()`
  - `_esIOS()`
  - `_entregarArchivo()`
  - `_descargarBlob()`
  - `arrayBufferToBase64()`
  - `descargarExcelDirecto()`
  - `verDetalleMerma()`
  - `limpiarTodoHistorialManual()`
  - `guardarCestas()`
  - `inicializarTurnoCestas()`
  - `incrementarCestas()`
  - `deshacerUltimoCestas()`
  - `detenerRevisionCestas()`
  - `actualizarPantallaCestas()`
  - `ejecutarCorteCestas()`
  - `confirmarCorteCestasManual()`
  - `abrirMenuOpcionesFilaCestas()`
  - `renderizarTablaCestas()`
  - `limpiarHistorialCestas()`
  - `descargarExcelCestas()`
  - `verificarInterruptorMaestro()`
  - `bloquearAppTotalmente()`
  - `obtenerOcrearDeviceID()`
  - `chequearSeguridadMensual()`
  - `mostrarPantallaSeguridad()`
  - `validarClaveMensual()`
  - `activarModoEmergencia()`
  - `iniciarAplicacionNormal()`
  - `manejarEstadoAuthOperario()`
  - `iniciarSesionOperarioUI()`
  - `guardarNombreOperarioUI()`
  - `cerrarSesionOperarioUI()`
  - `abrirPanelAdmin()`
  - `cerrarPanelAdmin()`
  - `abrirPerfil()`
  - `cerrarPerfil()`
  - `abrirAjustes()`
  - `mostrarAppPrincipal()`
  - `recuperarSesionRevisionAbandonada()`
  - `avisarSiHuboActualizacion()`
  - `verificarEstadoEmergencia()`
  - `actualizarCuentaRegresivaEmergencia()`
  - `verificarActualizacionAlAbrir()`
  - `calcularVenceParaPeso()`
  - `previsualizarVencePeso()`
  - `inicializarTabPeso()`
  - `conectarListenersPeso()`
  - `cambiarVistaPeso()`
  - `_pesoEstadoPermiso()`
  - `evaluarPantallaPeso()`
  - `seleccionarMaquinaPeso()`
  - `filtrarOpcionesDropdown()`
  - `iniciarHojaPeso()`
  - `renderizarTablaPeso()`
  - `generarHtmlFormatoOficialPeso()`
  - `_formatoPesoValor()`
  - `_formatoHoraCelda()`
  - `_finYaMarcado()`
  - `generarHtmlFormatoPeso()`
  - `_normalizarHora()`
  - `_horaAMinutos()`
  - `_marcarEliminadoPeso()`
  - `_bloquesVisibles()`
  - `_segmentoActualBloque()`
  - `_agruparRevisionesPorLote()`
  - `abrirModalNuevoLotePeso()`
  - `_actualizarSelectorNuevoLote()`
  - `_previsualizarVenceNuevoLote()`
  - `_generarCamposBocasHtml()`
  - `_actualizarCamposRevisionPeso()`
  - `_pesoRevisionesActivas()`
  - `_pesoParoActivo()`
  - `_pesoProximaMediaHora()`
  - `actualizarIndicadorPeso30()`
  - `iniciarTimerPeso30()`
  - `_camposPesosHtmlPrefijo()`
  - `registrarParoReinicioPeso()`
  - `abrirModalRevisionPeso()`
  - `editarRevisionPeso()`
  - `editarBloquePeso()`
  - `abrirModalFinProduccionPeso()`
  - `abrirModalCambiarProductoPeso()`
  - `actualizarProductosCambioPeso()`
  - `previsualizarVenceCambioPeso()`
  - `abrirPapeleraPeso()`
  - `restaurarBloquePeso()`
  - `restaurarRevisionPeso()`
  - `cerrarHojaPeso()`
  - `abrirModalAgregarColaboradorPeso()`
  - `abrirModalEntregarTurnoPeso()`
  - `confirmarRecibirTurnoPeso()`
  - `abrirPrevisualizacionPeso()`
  - `cerrarPrevisualizacionPeso()`
  - `cambiarZoomPeso()`
  - `_avisarDescargaPeso()`
  - `_base64AArrayBufferPeso()`
  - `descargarExcelPeso()`
  - `_rellenarPlantillaOficialPeso()`
  - `fijarAlto()`
  - `escribirAnotacionVertical()`
  - `_planificarPaginasPeso()`
  - `_fijarLogoLalaPeso()`
  - `_descargarPesoOficialPaginado()`
  - `descargarExcelPesoConPlantillaUht()`
  - `descargarExcelPesoConPlantillaDerivados()`
  - `_pesosSimulados()`
  - `_generarHorasCada30()`
  - `inyectarSimulacionPeso()`
  - `borrarSimulacionPeso()`
  - `_coleccionPeso()`
  - `_coleccionPesoHistorial()`
  - `ponerCabecera()`
  - `ponerCausa()`
  - `suscribirseAuthOperario()`
  - `onload()`
  - `_pesoNuevoLoteBloques()`
  - `_pesoRevBloques()`
  - `_pesoFinBloques()`
  - `_catalogoTipoTemp()`
  - `guardarTurnoEnNube()`
  - `iniciarSesionOperario()`
  - `cerrarSesionOperario()`
  - `actualizarNombreOperario()`
  - `esAdminActual()`
  - `obtenerUsuarioActual()`
  - `obtenerTurnosParaAdmin()`
  - `suscribirseHojaPeso()`
  - `registrarAnalistaDirectorio()`
  - `obtenerDirectorioAnalistas()`
  - `crearHojaPeso()`
  - `agregarColaboradorPeso()`
  - `entregarTurnoPeso()`
  - `recibirTurnoPeso()`
  - `agregarBloquePeso()`
  - `agregarRevisionPeso()`
  - `actualizarRevisionesPeso()`
  - `actualizarBloquesPeso()`
  - `cerrarHojaPesoNube()`
  - `borrarHojaPesoPrueba()`
  - `borrarHistorialPesoPrueba()`

### 6. Persistencia y Eventos
- **Claves de almacenamiento local:** lala_corte_cestas, lala_theme_mode, lala_etiqueta, lala_prod_cust, lala_contadores, lala_prod_val, lala_seguridad, lala_turno_temporal, lala_emergencia_usada, lala_lote, lala_fecha_inicio_turno, lala_paros, lala_device_id, lala_emergencia_activa, lala_cont_cestas, lala_dark_mode, lala_turno_activo, lala_sesion_activa, lala_verificador, lala_modo_lavado, lala_registros, lala_ultimo_bloque, lala_reg_cestas, lala_turnos_pasados, lala_ultima_version_vista
- **Tipos de eventos capturados:** change, click, contextmenu, input, load, online, pointerdown, pointerleave, pointerup

### 7. Puntos Clave para Refactoring y Próximos Pasos
- **Separación de Responsabilidades:** Extraer scripts y estilos embebidos en `index.html` hacia módulos dedicados (`/js`, `/css`).
- **Modularización:** Dividir las funciones en módulos (e.g. `storage.js`, `ui.js`, `reports.js`, `charts.js`).
- **Seguridad y Validación:** Verificar esquemas de datos guardados en `localStorage` antes de consumirlos.
- **Compatibilidad Offline / PWA:** Evaluar service worker y manifest para auditorías en campo sin conexión.

---

## Extracción Técnica de Módulos Críticos (Target Analysis)

### 1. MÓDULO CADUCIDAD (Vida Útil)
#### HTML de la Pestaña Caducidad (Líneas 825 - 857):
```html
        <div class="card">
            <h2>📅 Aseguramiento de Calidad: Vida Útil</h2>
            <p style="font-size: 0.85rem; color: #6c757d; margin-top:0;">Calculadora automática de fecha de vencimiento según lote y categoría de producto.</p>
            
            <div class="form-group" style="margin-bottom: 15px;">
                <label>Lote de Producción (DDMM)</label>
                <input type="number" id="input-lote-caducidad" placeholder="Ej. 1507 (Para 15 de Julio)" style="font-size:1.1rem; text-align:center; letter-spacing:2px; font-weight:bold;">
            </div>

            <div class="form-group" style="margin-bottom: 15px;">
                <label>Categoría del Producto</label>
                <!-- CUSTOM SELECT CADUCIDAD -->
                <div class="custom-dropdown" id="dd-caducidad" onclick="toggleDropdown('dd-caducidad', event)">
                    <div class="dropdown-selected" id="lbl-caducidad">Yogurt Normal (62 días)</div>
                    <div class="dropdown-options">
                        <div class="drop-opt" onclick="setDropdown('dd-caducidad', 'crema45', 'Crema Dulce (45 días)')">Crema Dulce (45 días)</div>
                        <div class="drop-opt" onclick="setDropdown('dd-caducidad', 'griego46', 'Yogurt Griego (46 días)')">Yogurt Griego (46 días)</div>
                        <div class="drop-opt" onclick="setDropdown('dd-caducidad', 'crema50', 'Crema LP & Eskimo 8kg (50 días)')">Crema LP & Eskimo 8kg (50 días)</div>
                        <div class="drop-opt active" onclick="setDropdown('dd-caducidad', 'yogurt62', 'Yogurt Normal (62 días)')">Yogurt Normal (62 días)</div>
                        <div class="drop-opt" onclick="setDropdown('dd-caducidad', 'bolsa90', 'Bolsa (90 días)')">Bolsa (90 días)</div>                        <div class="drop-opt" onclick="setDropdown('dd-caducidad', 'leche120', 'Nutri Lety / Foremst Deslact / Descremada Eskimo (120 días)')">Nutri Lety / Foremst Deslact / Descremada Eskimo (120 días)</div>
                        <div class="drop-opt" onclick="setDropdown('dd-caducidad', 'leche150', 'Caja Lala / OH Lala / Mantequilla LP y Eskimo (150 días)')">Caja Lala / OH Lala / Mantequilla LP y Eskimo (150 días)</div>
                        <div class="drop-opt" onclick="setDropdown('dd-caducidad', 'caja180', 'Caja Lechelado / Caja Eskimo Mi Sabor / Mantequilla Granel (180 días)')">Caja Lechelado / Caja Eskimo Mi Sabor / Mantequilla Granel (180 días)</div>
                    </div>
                    <input type="hidden" id="select-cat-caducidad" value="yogurt62">
                </div>
            </div>

            <button class="btn-action" style="background-color: var(--info); font-size:1.1rem;" onclick="calcularCaducidad()">🔍 CALCULAR VENCIMIENTO</button>

            <div id="resultado-caducidad" class="resultado-caducidad">
                <span style="color:#6c757d;">Ingresa los datos arriba y presiona calcular.</span>
            </div>
        </div>
```

#### Funciones JS de Cálculo de Fechas y Vencimiento:
- **`_fechaDeLote()`** (Líneas 1076 - 1092):
```javascript
    function _fechaDeLote(loteStr) {
        if (!loteStr) return null;
        loteStr = String(loteStr).trim();
        if (loteStr.length < 3 || loteStr.length > 4) return null;
        loteStr = loteStr.padStart(4, '0');
        var day = parseInt(loteStr.substring(0, 2), 10);
        var month = parseInt(loteStr.substring(2, 4), 10) - 1;
        var hoy = new Date();
        var prodDate = new Date(hoy.getFullYear(), month, day);
        if (isNaN(prodDate.getTime()) || prodDate.getMonth() !== month || prodDate.getDate() !== day) return null;
        var margenMs = 2 * 24 * 60 * 60 * 1000;
        if (prodDate.getTime() - hoy.getTime() > margenMs) {
            prodDate = new Date(hoy.getFullYear() - 1, month, day);
            if (prodDate.getMonth() !== month) return null; // 29/02 de un año no bisiesto
        }
        return prodDate;
    }
```

- **`obtenerDiasProducto()`** (Líneas 1120 - 1125):
```javascript
    function obtenerDiasProducto(tipo, maquina, producto) {
        var info = catalogoMaquinasPeso[tipo] && catalogoMaquinasPeso[tipo][maquina];
        if (!info || !info.productos.some(function(p){ return p.nombre === producto; })) return null;
        var categoria = categoriaVidaUtilPeso(tipo, maquina, producto);
        return categoria ? VIDA_UTIL_OFICIAL_OCT2026[categoria] : null;
    }
```

- **`categoriaVidaUtilPeso()`** (Líneas 1100 - 1119):
```javascript
    function categoriaVidaUtilPeso(tipo, maquina, producto) {
        var n = String(producto || '').toLowerCase();
        var m = String(maquina || '').toUpperCase();
        if (tipo === 'derivados') {
            if (n.includes('griego')) return 'griego46';
            if (n.includes('crema dulce')) return 'crema45';
            if (n.includes('crema')) return 'crema50';
            if (n.includes('mantequilla a granel')) return 'caja180';
            if (n.includes('mantequilla')) return 'leche150';
            if (n.includes('yogurt') || ['ERCA','PRIMO','SERAC','IS6-7'].includes(m)) return 'yogurt62';
        }
        if (tipo === 'uht') {
            if (m === 'THIMONIER') return 'bolsa90';
            if (n.includes('lechelado')) return 'caja180';
            if (n.includes('nutrilety') || n.includes('deslactosada eskimo') || n.includes('descremada-deslactosada')) return 'leche120';
            if (n.includes('lala') || n.includes('oh lala')) return 'leche150';
            if (m === 'A3FLEX' || m === 'COMPACT') return 'caja180';
        }
        return null;
    }
```

- **`calcularCaducidad()`** (Líneas 1727 - 1770):
```javascript
    function calcularCaducidad() {
        let loteStr = document.getElementById('input-lote-caducidad').value;
        let catVal = document.getElementById('select-cat-caducidad').value;
        let catText = document.getElementById('lbl-caducidad').innerText;

        if(!loteStr || loteStr.length < 3 || loteStr.length > 4) {
            return Swal.fire('Error de Lote', 'Ingresa el lote en formato de 3 o 4 dígitos (Ej. 107 para 1 de julio, o 1507 para 15 de julio).', 'warning');
        }

        let prodDate = _fechaDeLote(loteStr);
        if (!prodDate) {
            return Swal.fire('Fecha Inválida', 'El lote ingresado no corresponde a una fecha real del calendario.', 'error');
        }

        let daysToAdd = categoryDays[catVal];
        let expDate = new Date(prodDate);
        expDate.setDate(expDate.getDate() + daysToAdd);

        let today = new Date();
        today.setHours(0,0,0,0);
        let checkDate = new Date(expDate);
        checkDate.setHours(0,0,0,0);

        let fDay = String(expDate.getDate()).padStart(2, '0');
        let fMonth = String(expDate.getMonth() + 1).padStart(2, '0');
        let fYear = expDate.getFullYear();

        let diffTime = checkDate - today;
        let diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));

        let resultHTML = `<div style="font-size:1.1rem; color:var(--text-color); margin-bottom:5px;"><b>${catText}</b></div>`;
        resultHTML += `<div style="font-size:1rem; color:var(--info);">Fecha de Vencimiento:</div>`;
        resultHTML += `<div style="font-size:2.2rem; font-weight:900; color:var(--text-color); letter-spacing:1px;">${fDay}/${fMonth}/${fYear}</div>`;

        if (diffDays < 0) {
            resultHTML += `<div style="margin-top:15px; padding:10px; background-color:#f8d7da; color:#842029; border-radius:8px; border:1px solid #f5c2c7; text-align:left;">⚠️ <b>¡ALERTA DE SEGURIDAD!</b><br>Este producto se encuentra <b>VENCIDO</b> desde hace ${Math.abs(diffDays)} días.</div>`;
        } else if (diffDays <= 5) {
            resultHTML += `<div style="margin-top:15px; padding:10px; background-color:#fff3cd; color:#664d03; border-radius:8px; border:1px solid #ffe69c; text-align:left;">⏳ <b>ATENCIÓN:</b><br>El producto está próximo a vencer en <b>${diffDays} días</b>.</div>`;
        } else {
             resultHTML += `<div style="margin-top:15px; padding:10px; background-color:#d1e7dd; color:#0f5132; border-radius:8px; border:1px solid #badbcc; text-align:left;">✅ <b>ESTADO ÓPTIMO:</b><br>Producto dentro de su vida útil. Faltan ${diffDays} días para su caducidad.</div>`;
        }

        document.getElementById('resultado-caducidad').innerHTML = resultHTML;
    }
```

### 2. BOTONES MERMA Y CORTE MANUAL (HTML + CSS Neón)
#### HTML de Botón 'REGISTRAR 1 BALDE DE MERMA AHORA':
- Líneas 661 - 667:
```html
                <label style="font-size: 0.8rem; font-weight: bold; color: var(--purple); display: block; margin-bottom: 6px;">🗑️ REGISTRO INMEDIATO DE MERMA</label>
                <div class="counter-row">
                    <button class="btn-counter btn-merma-color" onclick="registrarMermaInmediata()">
                        <span>🪣 REGISTRAR 1 BALDE DE MERMA AHORA</span><span class="counter-badge" style="font-size:1.2rem;">+1</span>
                    </button>
                </div>
            </div>
```

#### HTML de Botón 'REGISTRAR CORTE MANUAL AHORA':
- Líneas 666 - 672:
```html
                </div>
            </div>

            <button class="btn-action btn-corte-manual" onclick="confirmarCorteManual()">💾 REGISTRAR CORTE MANUAL AHORA</button>
        </div>

        <div class="card">
```

#### Reglas CSS (Estilos Neón y Acciones):
- Líneas 45 - 59:
```css
        body.dark-mode .tab-btn { background: #1f2024; color: #9a9ba3; border-color: #34353a; }
        body.dark-mode .tab-btn.active { background: var(--info); color: white; border-color: var(--info); }
        body.dark-mode .btn-corte-manual { background-color: #7c93e8 !important; color: white; }
        body.dark-mode th, body.dark-mode td { border-color: #34353a; }
        
        /* DIFUMINADO MODALES Y ALERTAS */
        .swal2-container.swal2-center { backdrop-filter: blur(8px) !important; background: rgba(0, 0, 0, 0.7) !important; transition: 0.25s ease-out; }
        .swal2-container { z-index: 999999999 !important; }
        .swal-pack-popup { margin-top: 6vh !important; }
        .swal-no-autoresize.swal2-popup { overflow: visible !important; }
        .swal-no-autoresize .swal2-html-container { overflow: visible !important; }
        body.dark-mode .swal2-popup { background: #222327; color: #e6e6ea; border: 1px solid #34353a; box-shadow: 0 10px 25px rgba(0,0,0,0.5); }
        body.dark-mode .swal2-title { color: #f0f0f3; }
        body.dark-mode .swal2-html-container { color: #9a9ba3; text-align: left; }
        body.dark-mode .swal2-input, body.dark-mode .swal2-textarea { color: #e6e6ea; background: #26272c; border-color: #3a3b41; }
```
- Líneas 92 - 106:
```css
        .btn-minus { border: 1px solid var(--input-border); background-color: var(--bg-color); color: var(--text-color); font-size: 1.3rem; font-weight: bold; padding: 15px; border-radius: 14px; cursor: pointer; min-width: 55px; text-align: center; box-shadow: 0 2px 4px rgba(0,0,0,0.05); transition: transform 0.1s, background-color 0.25s, color 0.25s; }
        .btn-counter:active, .btn-minus:active, .btn-action:active { transform: scale(0.96); filter: brightness(0.9); }
        .btn-danger { background-color: var(--danger); } .btn-info { background-color: var(--info); } .btn-warning { background-color: var(--success); } .btn-merma-color { background-color: var(--purple); } 
        .counter-badge { background: rgba(0, 0, 0, 0.25); padding: 4px 12px; border-radius: 10px; font-size: 1.6rem; min-width: 40px; text-align: center; pointer-events: none; }
        .btn-action { width: 100%; background-color: var(--dark); color: white; border: none; padding: 14px; font-size: 1rem; font-weight: bold; border-radius: 10px; margin-top: 8px; cursor: pointer; transition: 0.25s; }
        .btn-start-shift { background-color: #0d6efd; margin-bottom: 10px; font-size: 1.05rem; box-shadow: 0 4px 6px rgba(13,110,253,0.2); }
        .btn-lavado { background-color: #6c757d; color: white; font-size: 1.1rem; padding: 16px; margin-bottom: 5px; box-shadow: 0 4px 6px rgba(0,0,0,0.15); }
        .btn-lavado.activo { background-color: #d63384; animation: parpadeo-soft 2s infinite; }
        @keyframes parpadeo-soft { 50% { background-color: #a31d5d; } }
        .btn-group-actions { display: flex; gap: 6px; width: auto; flex-wrap: wrap; }
        .btn-excel { background-color: #198754; margin-top: 0; } .btn-preview { background-color: #0d6efd; margin-top: 0; } .btn-photo { background-color: #6f42c1; margin-top: 0; }
        .btn-clear { background-color: #6c757d; font-size: 0.85rem; padding: 8px; margin-top: 15px; }
        .timer-alert { background-color: #fff3cd; color: #664d03; padding: 8px; border-radius: 8px; text-align: center; font-weight: bold; font-size: 0.85rem; margin-bottom: 8px; border: 1px solid #ffe69c; transition: 0.25s;}
        .alert-missing { background-color: #f8d7da !important; color: #842029 !important; border: 1px solid #f5c2c7 !important; animation: parpadeo 1.5s infinite; margin-bottom: 5px; }
        @keyframes parpadeo { 50% { opacity: 0.6; } }
```
- Líneas 177 - 191:
```css
           el tema elegido).
           ============================================================ */
        body.theme-neon {
            --bg-color: #12131c; --text-color: #f0f0f5; --card-bg: #1a1b26; --border-color: #2e2f42; --input-bg: #1f2030; --input-border: #3a3b52; --table-header: #181926;
            --danger: #dc2626; --info: #22d3ee; --success: #34d399; --warning: #f59e0b; --purple: #a855f7; --dark: #1a1b26;
        }
        body.theme-neon .header-app { background: var(--card-bg); color: var(--text-color); box-shadow: 0 4px 15px rgba(217,70,239,0.15); }
        body.theme-neon .header-app h1 { color: #f0f0f5; }
        body.theme-neon .time-display { background: #181926; color: #9a9bc0; border: 1px solid #2e2f42; }
        body.theme-neon h2 { color: #ffffff; border-bottom: 2px solid var(--border-color); }
        body.theme-neon input, body.theme-neon select { color: #f0f0f5; }
        body.theme-neon .change-row { background-color: #1a2b3d; color: #67e8f9; border-color: #2e2f42; }
        body.theme-neon .lunch-row { background-color: #3d2e10 !important; color: #fbbf24 !important; }
        body.theme-neon .lavado-row { background-color: #3d1530 !important; color: #f9a8d4 !important; border-color: #2e2f42; }
        body.theme-neon .merma-row { background-color: #2a1a3d !important; color: #d8b4fe !important; border-color: #2e2f42; }
```
- Líneas 192 - 206:
```css
        body.theme-neon .total-row { background-color: #181926 !important; color: #f0f0f5 !important;}
        body.theme-neon .btn-minus { background-color: #1f2030; color: #f0f0f5; border-color: #3a3b52; }
        body.theme-neon .calc-box { background: #1f2030; color: #f0f0f5; border: 1px solid #3a3b52; }
        body.theme-neon .merma-summary { background-color: #2a1a3d; color: #d8b4fe; border-color: #5b21b6; }
        body.theme-neon .timer-alert { background-color: #3d2e10; color: #fbbf24; border-color: #92650a; }
        body.theme-neon .tab-btn { background: #181926; color: #9a9bc0; border-color: #2e2f42; }
        body.theme-neon .tab-btn.active { background: linear-gradient(135deg, #d946ef, #7c3aed); color: white; border-color: transparent; box-shadow: 0 4px 15px rgba(217,70,239,0.4); }
        body.theme-neon .btn-corte-manual { background: linear-gradient(135deg, #d946ef, #7c3aed) !important; color: white; }
        body.theme-neon th, body.theme-neon td { border-color: #2e2f42; }
        body.theme-neon .swal2-popup { background: #1a1b26; color: #f0f0f5; border: 1px solid #2e2f42; box-shadow: 0 10px 30px rgba(0,0,0,0.6); }
        body.theme-neon .swal2-title { color: #ffffff; }
        body.theme-neon .swal2-html-container { color: #9a9bc0; text-align: left; }
        body.theme-neon .swal2-input, body.theme-neon .swal2-textarea { color: #f0f0f5; background: #1f2030; border-color: #3a3b52; }
        body.theme-neon .swal-table th, body.theme-neon .swal-table td { border-color: #2e2f42; }
        body.theme-neon .dropdown-options { box-shadow: 0 5px 20px rgba(0,0,0,0.6); }
```
- Líneas 207 - 221:
```css
        body.theme-neon .security-box { background: #191a26; box-shadow: 13px 13px 25px #0c0d14, -13px -13px 25px #262838; color: #9a9bc0; }
        body.theme-neon .security-box h2 { color: #ffffff; }
        body.theme-neon .security-box p { color: #9a9bc0; }
        body.theme-neon .device-id-box { background: #191a26; box-shadow: inset 8px 8px 15px #0c0d14, inset -8px -8px 15px #262838; }
        body.theme-neon .device-id-box span { color: #9a9bc0; }
        body.theme-neon .device-id-box strong { color: #d946ef; }
        body.theme-neon .security-box input { background: #191a26; color: #f0f0f5; box-shadow: inset 8px 8px 15px #0c0d14, inset -8px -8px 15px #262838; }
        body.theme-neon .security-box .btn-action { background: #191a26; color: #d946ef; box-shadow: 8px 8px 15px #0c0d14, -8px -8px 15px #262838; }
        body.theme-neon .security-box .btn-action:active { box-shadow: inset 8px 8px 15px #0c0d14, inset -8px -8px 15px #262838; }

        /* Silencia/vibra los colores fijos escritos directo en botones/insignias,
           igual que en Modo Oscuro pero con la paleta magenta/cian. */
        body.theme-neon [style*="background-color: #0d6efd"], body.theme-neon [style*="background-color:#0d6efd"], body.theme-neon [style*="background: #0d6efd"] { background-color: #22d3ee !important; color: #0a0a12 !important; }
        body.theme-neon [style*="background-color: #198754"], body.theme-neon [style*="background-color:#198754"], body.theme-neon [style*="background: #198754"] { background-color: #34d399 !important; }
        body.theme-neon [style*="background-color: #28a745"] { background-color: #34d399 !important; }
```
- Líneas 222 - 236:
```css
        body.theme-neon [style*="background-color: #20c997"], body.theme-neon [style*="background-color:#20c997"] { background-color: #2dd4bf !important; }
        body.theme-neon [style*="background-color: #842029"] { background-color: #9f1239 !important; }
        body.theme-neon [style*="background-color: #a31d5d"] { background-color: #be185d !important; }
        body.theme-neon [style*="background-color: #d63384"] { background-color: #ec4899 !important; }
        body.theme-neon [style*="background-color: #dc3545"], body.theme-neon [style*="background-color:#dc3545"], body.theme-neon [style*="background: #dc3545"] { background-color: #dc2626 !important; }
        body.theme-neon [style*="background-color: #fd7e14"], body.theme-neon [style*="background-color:#fd7e14"] { background-color: #f59e0b !important; }
        body.theme-neon [style*="background-color: #ff8c00"] { background-color: #f59e0b !important; }
        body.theme-neon [style*="background-color: #ffc107"], body.theme-neon [style*="background: #ffc107"] { background-color: #f59e0b !important; }
        body.theme-neon [style*="background-color: #e7f1ff"] { background-color: #182236 !important; }
        body.theme-neon [style*="background-color: #f3e8ff"] { background-color: #241a38 !important; }
        body.theme-neon [style*="background-color: #f8d7da"], body.theme-neon [style*="background-color:#f8d7da"] { background-color: #341824 !important; }
        body.theme-neon [style*="background-color: #fff3cd"], body.theme-neon [style*="background-color:#fff3cd"], body.theme-neon [style*="background:#fff3cd"] { background-color: #342a14 !important; }
        body.theme-neon [style*="color:#664d03"], body.theme-neon [style*="color: #664d03"] { color: #fde68a !important; }
        body.theme-neon [style*="background-color:#d1e7dd"] { background-color: #142c22 !important; }
        body.theme-neon [style*="background-color: #e2e3e5"] { background-color: #1f2030 !important; }
```

### 3. ALERTA PRODUCTION PACK, TURNOS E HISTORIAL OFICIAL
#### Elemento DOM Alerta '¡ALERTA: Falta registrar datos de Production Pack!':
- Líneas 567 - 571:
```html

        <div id="timer-banner" class="timer-alert">⏳ Próximo corte automático en: --m --s</div>
        <div id="missing-pack-banner" class="timer-alert alert-missing" style="display: none;">⚠️ ¡ALERTA: Falta registrar datos de Production Pack!</div>

        <button id="btn-lavado-toggle" class="btn-action btn-lavado" onclick="alternarModoLavadoParo()">🧼 PASAR MÁQUINA A MODO LAVADO / PARO</button>
```

#### Funciones JS de Evaluación Horaria y Alertas de Pack:
- **`revisarFaltaProduccionPack()`** (Líneas 3169 - 3178):
```javascript
    function revisarFaltaProduccionPack() {
        var falta = false;
        for (var i = 0; i < registros.length; i++) {
            if (!registros[i].tipoEspecial && (registros[i].pack === "—" || registros[i].pack === "")) {
                var hParts = registros[i].hora.split(':'); var hM = parseInt(hParts[1]);
                if (hM === 0) { falta = true; break; }
            }
        }
        document.getElementById('missing-pack-banner').style.display = falta ? 'block' : 'none';
    }
```

- **`ejecutarRelojYAlertas()`** (Líneas 3135 - 3167):
```javascript
    function ejecutarRelojYAlertas() {
        var ahora = new Date(); var hrs = ahora.getHours(); var mins = ahora.getMinutes(); var segs = ahora.getSeconds();
        var timeEl = document.getElementById('current-time-block');
        if(timeEl) timeEl.innerText = String(hrs).padStart(2, '0') + ":" + String(mins).padStart(2, '0') + ":" + String(segs).padStart(2, '0');

        // Si estamos revisando un turno pasado (Nevera), los cortes automáticos
        // deben quedar en pausa por completo — no le pertenecen a ese turno.
        if (editandoRespaldoId) {
            var tb = document.getElementById('timer-banner'); if (tb) tb.innerText = "⏸️ Cortes automáticos en pausa (Modo Revisión)";
            var mpb = document.getElementById('missing-pack-banner'); if (mpb) mpb.style.display = 'none';
            return;
        }

        // --- 1. LÓGICA DE YOGURT (CADA 30 MIN) ---
        if (modoLavadoActivo) {
            document.getElementById('timer-banner').innerText = "⏸️ Cronómetro pausado (Máquina en Lavado)"; document.getElementById('missing-pack-banner').style.display = 'none';
        } else {
            var minFaltantes = mins < 30 ? 29 - mins : 59 - mins; var segFaltantes = 59 - segs; // TAREA 3: Interacción Lavado vs. Corte Automático
            document.getElementById('timer-banner').innerText = "⏳ Próximo corte automático en: " + minFaltantes + "m " + segFaltantes + "s";

            if (turnoActivo !== "" && ultimoBloqueProcesado !== "") {
                var marcaPasada = (mins >= 30) ? String(hrs).padStart(2, '0') + ":30" : String(hrs).padStart(2, '0') + ":00";
                if (esHoraMayor(marcaPasada, ultimoBloqueProcesado)) { ejecutarCorteAutomaticoExacto(marcaPasada); }
            }
            revisarFaltaProduccionPack();
        }

        // --- 2. LÓGICA DE CESTAS (CADA 1 HORA EXACTA :00) ---
        if (ultimoCorteCestas !== "") {
            var horaCorteCestas = String(hrs).padStart(2, '0') + ":00";
            if (esHoraMayor(horaCorteCestas, ultimoCorteCestas)) { ejecutarCorteCestas(horaCorteCestas); }
        }
    }
```

#### Lógica de Inicio y Configuración de Turnos (07:00, 15:00, 23:00):
- Selector de Turnos HTML (Líneas 546 - 563):
```html
                <div class="form-group">
                    <label>Seleccionar Turno</label>
                    <div class="custom-dropdown" id="dd-turno" onclick="toggleDropdown('dd-turno', event)">
                        <div class="dropdown-selected" id="lbl-turno">-- Seleccione Turno --</div>
                        <div class="dropdown-options">
                            <div class="drop-opt" onclick="setDropdown('dd-turno', 'A', 'Turno A (07:00 - 15:00)', 'turno')">Turno A (07:00 - 15:00)</div>
                            <div class="drop-opt" onclick="setDropdown('dd-turno', 'B', 'Turno B (15:00 - 23:00)', 'turno')">Turno B (15:00 - 23:00)</div>
                            <div class="drop-opt" onclick="setDropdown('dd-turno', 'C', 'Turno C (23:00 - 07:00)', 'turno')">Turno C (23:00 - 07:00)</div>
                            <div class="drop-opt" onclick="setDropdown('dd-turno', 'A+', 'Turno A+ (07:00 - 19:00)', 'turno')">Turno A+ (07:00 - 19:00)</div>
                            <div class="drop-opt" onclick="setDropdown('dd-turno', 'A+B', 'Turno A+B (07:00 - 23:00)', 'turno')">Turno A+B (07:00 - 23:00)</div>
                        </div>
                        <input type="hidden" id="select-turno" value="">
                    </div>
                </div>
                <div class="form-group">
                    <label>Verificador</label>
                    <input type="text" id="verificador" value="Snayder">
                </div>
```
- Selector de Turnos HTML (Líneas 1331 - 1348):
```html
        }
    }

    function sincronizarDropdownPorValor(dropId, val) {
        var textos = {
            'dd-turno': { 'A': 'Turno A (07:00 - 15:00)', 'B': 'Turno B (15:00 - 23:00)', 'C': 'Turno C (23:00 - 07:00)', 'A+': 'Turno A+ (07:00 - 19:00)', 'A+B': 'Turno A+B (07:00 - 23:00)' },
            'dd-etiqueta': { 'LALA': 'Lala', 'LP': 'La Perfecta' }
        };
        var dd = document.getElementById(dropId);
        if (!dd) return;
        var hidden = dd.querySelector('input[type="hidden"]');
        if (hidden) hidden.value = val || '';
        var lbl = dd.querySelector('.dropdown-selected');
        if (lbl) {
            if (val && textos[dropId] && textos[dropId][val]) lbl.innerText = textos[dropId][val];
            else if (dropId === 'dd-etiqueta') lbl.innerText = '-- Seleccione --';
            else if (dropId === 'dd-turno') lbl.innerText = '-- Seleccione Turno --';
        }
```
- Función JS **`inicializarNuevoTurnoOficial()`** (Líneas 2729 - 2774):
```javascript
    function inicializarNuevoTurnoOficial() {
        // Sin esta validación, si nadie elegía turno quedaba turnoActivo = ""
        // y la app se comportaba como si NO hubiera turno iniciado (contadores
        // bloqueados, sin cortes automáticos), pero ya había escrito la primera fila.
        var turnoElegido = document.getElementById('select-turno').value;
        if (!turnoElegido) {
            return Swal.fire('Falta el Turno', 'Selecciona primero tu turno (A, B, C, A+ o A+B) antes de iniciar.', 'warning');
        }
        Swal.fire({
            title: '¿Iniciar Turno Oficial?', text: "Se registrará la primera fila con los datos actuales.", icon: 'info', showCancelButton: true, confirmButtonText: 'Sí, Iniciar', cancelButtonText: 'Cancelar'
        }).then(function(result) {
            if(result.isConfirmed) {
                registros = [];
                contadoresActuales = { malSellado: 0, sobrellenado: 0, volumenBajo: 0 };
                modoLavadoActivo = false;
                turnoActivo = document.getElementById('select-turno').value;
                fechaInicioTurno = obtenerFechaOperativaTurno(turnoActivo, new Date());
                parosRegistrados = [];
                guardarParosEnMemoria();

                // Lee los valores de la pantalla pero NO los borra.
                var prodSel = getProductName();
                var etiSel = document.getElementById('etiqueta').value || "—";
                var loteSel = document.getElementById('lote').value || "—";

                var ahora = new Date(); var hrsIni = ahora.getHours(); var minsIni = ahora.getMinutes();
                var horaStr = String(hrsIni).padStart(2, '0') + ":" + String(minsIni).padStart(2, '0');
                registros.push({ hora: horaStr, producto: prodSel, etiqueta: etiSel, lote: loteSel, pack: "—", malSellado: 0, sobrellenado: 0, volumenBajo: 0, almuerzo: false });

                // CORRECCIÓN v2.7.0: se marca el bloque de 30 min en el que arrancó el turno
                // (en vez de dejarlo vacío) para que el PRIMER corte automático después de
                // iniciar turno sí se dispare solo. Antes, al quedar en "", el reloj global
                // no ejecutaba ningún corte hasta que se hacía uno manual.
                ultimoBloqueProcesado = (minsIni < 30) ? (String(hrsIni).padStart(2, '0') + ":00") : (String(hrsIni).padStart(2, '0') + ":30");

                actualizarBotonParoUI();
                actualizarBotonLavadoUI();
                actualizarLabelEstadoTurno();
                guardarEnMemoria();
                actualizarPantallaContadores();
                renderizarTabla();

                Swal.fire('¡Listo!', 'Turno ' + turnoActivo + ' iniciado. Tus datos se han conservado.', 'success');
            }
        });
    }
```

#### Contenedor 'Historial Oficial' y Zona de Defectos (Mal Sellado, Sobrellenado, Vol. Bajo):
- Contenedor Historial Oficial (Líneas 670 - 699):
```html
        </div>

        <div class="card">
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px;">
                <h2 style="margin: 0; border: none;">📋 Historial Oficial</h2>
                <div class="btn-group-actions">
                    <!-- BOTÓN CAPTURA DE REPORTE -->
                    <button class="btn-action btn-photo" style="width: auto; padding: 6px 12px; font-size: 0.8rem;" onclick="descargarImagenReporte()">Captura Reporte 📸</button>
                    <button class="btn-action btn-excel" style="width: auto; padding: 6px 12px; font-size: 0.8rem; background-color: #198754;" onclick="descargarExcelDirecto()">Descargar Excel Oficial 📊</button>
                </div>
            </div>
            
            <div class="table-responsive" id="contenedor-tabla-registros">
                <table>
                    <thead>
                        <tr>
                            <th style="width: 14%;">Hora</th>
                            <th style="width: 14%;">Lote</th>
                            <th style="width: 20%;">Pack</th>
                            <th style="width: 18%;">Mal Sellado</th>
                            <th style="width: 18%;">Sobrellenado</th>
                            <th style="width: 16%;">Vol. Bajo</th>
                        </tr>
                    </thead>
                    <tbody id="tabla-cuerpo"></tbody>
                    <tfoot>
                        <tr class="total-row">
                            <td colspan="2">TOTAL</td>
                            <td id="tot-pack">0</td>
                            <td id="tot-mal">0</td>
```
- Zona de Defectos (Líneas 629 - 653):
```html
            <div class="counter-row">
                <button class="btn-counter btn-danger" onpointerdown="btnDown('malSellado')" onpointerup="btnUp('malSellado')" onpointerleave="btnLeave()" oncontextmenu="return false;">
                    <span>⚠️ MAL SELLADO</span><span class="counter-badge" id="lbl-malSellado">0</span>
                </button>
                <button class="btn-minus" onpointerdown="btnMinusDown('malSellado')" onpointerup="btnMinusUp('malSellado')" onpointerleave="btnMinusLeave()" oncontextmenu="return false;">-</button>
            </div>

            <div class="counter-row">
                <button class="btn-counter btn-info" onpointerdown="btnDown('sobrellenado')" onpointerup="btnUp('sobrellenado')" onpointerleave="btnLeave()" oncontextmenu="return false;">
                    <span>⚖️ SOBRELLENADO</span><span class="counter-badge" id="lbl-sobrellenado">0</span>
                </button>
                <button class="btn-minus" onpointerdown="btnMinusDown('sobrellenado')" onpointerup="btnMinusUp('sobrellenado')" onpointerleave="btnMinusLeave()" oncontextmenu="return false;">-</button>
            </div>

            <div class="counter-row">
                <button class="btn-counter btn-warning" onpointerdown="btnDown('volumenBajo')" onpointerup="btnUp('volumenBajo')" onpointerleave="btnLeave()" oncontextmenu="return false;">
                    <span>📉 VOLUMEN BAJO</span><span class="counter-badge" id="lbl-volumenBajo">0</span>
                </button>
                <button class="btn-minus" onpointerdown="btnMinusDown('volumenBajo')" onpointerup="btnMinusUp('volumenBajo')" onpointerleave="btnMinusLeave()" oncontextmenu="return false;">-</button>
            </div>

            <div style="margin-top: 15px; border-top: 2px dashed var(--border-color); padding-top: 12px;">
                <label style="font-size: 0.8rem; font-weight: bold; color: var(--info); display: block; margin-bottom: 6px;">⏸️ BITÁCORA DE PAROS</label>                <div class="counter-row" style="flex-wrap: wrap; justify-content: center; gap: 4px; width: 100%; box-sizing: border-box;">
                <button class="btn-counter" style="width: 100%; margin-bottom: 5px; background-color: #ff8c00;" onclick="abrirModalRegistrarParo()">
                    <span>🛑 REGISTRAR PARO</span>
```
- Zona de Defectos (Líneas 687 - 711):
```html
                            <th style="width: 14%;">Lote</th>
                            <th style="width: 20%;">Pack</th>
                            <th style="width: 18%;">Mal Sellado</th>
                            <th style="width: 18%;">Sobrellenado</th>
                            <th style="width: 16%;">Vol. Bajo</th>
                        </tr>
                    </thead>
                    <tbody id="tabla-cuerpo"></tbody>
                    <tfoot>
                        <tr class="total-row">
                            <td colspan="2">TOTAL</td>
                            <td id="tot-pack">0</td>
                            <td id="tot-mal">0</td>
                            <td id="tot-sobre">0</td>
                            <td id="tot-bajo">0</td>
                        </tr>
                    </tfoot>
                </table>
            </div>

            <div id="conversion-box" class="calc-box">📦 Equivalencia de producción: 0 paquetes (0 polines)</div>
            
            <div id="merma-box" class="calc-box merma-summary" onclick="verDetalleMerma()">
                <span>🪣 Total merma en el turno: <b>0 baldes</b></span>
                <span style="font-size: 0.75rem; text-decoration: underline; color:var(--purple);">Ver detalles 🔍</span>
```
- Zona de Defectos (Líneas 2429 - 2453):
```html

    function obtenerNombreDefecto(tipo) {
        if(tipo === 'malSellado') return 'Mal Sellado';
        if(tipo === 'sobrellenado') return 'Sobrellenado';
        if(tipo === 'volumenBajo') return 'Volumen Bajo';
        return tipo;
    }

    function abrirInputNumerico(tipo) {
        Swal.fire({
            title: 'Añadir ' + obtenerNombreDefecto(tipo),
            text: "Ingresa la cantidad exacta a sumar:",
            input: 'text',
            inputAttributes: { inputmode: 'numeric' },
            showCancelButton: true,
            confirmButtonText: 'Sumar',
            cancelButtonText: 'Cancelar',
            position: 'top',
            didOpen: () => { Swal.getInput().blur(); },
            inputValidator: function(value) {
                if (!value || isNaN(value) || Number(value) <= 0) return 'Debes ingresar un número válido mayor a 0';
            }
        }).then(function(result) {
            if (result.isConfirmed) {
                var cantidad = parseInt(result.value);
```
- Zona de Defectos (Líneas 3337 - 3361):
```html
            ['edit-hora', 'Hora (HH:MM)', r.hora, 'text', 5],
            ['edit-lote', 'Lote', r.lote, 'text', 12],
            ['edit-mal', 'Mal Sellado', r.malSellado, 'number', 4],
            ['edit-sobre', 'Sobrellenado', r.sobrellenado, 'number', 4],
            ['edit-bajo', 'Volumen Bajo', r.volumenBajo, 'number', 4]
        ];
        var htmlCont = '<div class="yogurt-edit-grid">' + campos.map(function(c) {
            var valor = String(c[2] == null ? '' : c[2]).replace(/&/g,'&amp;').replace(/"/g,'&quot;').replace(/</g,'&lt;');
            return '<div class="yogurt-edit-field"><label for="'+c[0]+'">'+c[1]+'</label>' +
                '<input id="'+c[0]+'" type="'+c[3]+'" '+(c[3]==='number'?'min="0" step="1" inputmode="numeric"':'')+' maxlength="'+c[4]+'" value="'+valor+'"></div>';
        }).join('') + '</div>';
        Swal.fire({ title: 'Modificar Datos Manualmente', html: htmlCont, showCancelButton: true, confirmButtonText: 'Guardar Cambios'
        }).then(function(res) {
            if(res.isConfirmed) {
                registros[index].hora = document.getElementById('edit-hora').value; registros[index].lote = document.getElementById('edit-lote').value; registros[index].malSellado = parseInt(document.getElementById('edit-mal').value) || 0; registros[index].sobrellenado = parseInt(document.getElementById('edit-sobre').value) || 0; registros[index].volumenBajo = parseInt(document.getElementById('edit-bajo').value) || 0;
                ordenarRegistrosCronologicamente(); guardarEnMemoria(); renderizarTabla(); Swal.fire('Guardado', 'Los datos se actualizaron correctamente.', 'success');
            }
        });
    }

    // Editar/eliminar la cabecera de un cambio de sabor. Antes se redefinía
    // dentro de renderizarTabla(), o sea que se volvía a crear en cada
    // repintado de la tabla sin ninguna necesidad.
    // v4.0.2 — Corrige una sección existente sin crear un cambio nuevo.
    function _firmaYogurt(r) {
```

---
## Reporte de Mejoras Quirúrgicas Aplicadas (Upgrades v4.0.24)
- **Respaldo creado:** `index.backup.html` antes de iniciar transformaciones.

### 1. Módulo Caducidad Multi-Producto (Máx 4 Slots)
- **HTML (`#tab-caducidad`):** Agregado botón `#btn-fijar-caducidad` y contenedor `<details open id="details-caducidad-slots">` con resumen dinámico `(X/4)`. Generación de grid responsive con tarjetas temáticas.
- **Persistencia:** Almacenamiento en `localStorage['lala_caducidad_slots']` conteniendo `{id, lote, prodKey, prodLabel, dias, fechaVenc, diffDays, estadoHtml}`.
- **Funciones implementadas:**
  - `autoGuardarSlotCaducidad(item)`: Registra automáticamente el cálculo si no existe el par Lote+Producto (hasta 4 items).
  - `fijarSlotCaducidadActual()`: Permite al usuario fijar o actualizar el cálculo actual en un slot de memoria.
  - `eliminarSlotCaducidad(id)`: Remueve individualmente un slot sin alterar los demás.
  - `verSlotCaducidad(id)`: Carga el lote y producto en la calculadora y ejecuta la consulta con scroll suave.
  - `renderizarSlotsCaducidad(slots)`: Muestra tarjetas compactas con badges de estado (Óptimo / Próximo / Vencido).

### 2. Diferenciación Visual en Modo Neón (Merma vs Corte Manual)
- **Botón Merma (`.theme-neon .btn-merma-color`):**
  `background: linear-gradient(135deg, #f43f5e, #be185d) !important;` (Rosa Fresa / Carmesí Neón con resplandor armonizado).
- **Botón Corte Manual (`.theme-neon .btn-corte-manual`):**
  `background: linear-gradient(135deg, #4338ca, #6d28d9) !important;` (Gradiente Índigo / Violeta frío con borde sutil).
- **Resultado:** Contraste instantáneo y distinción clara entre merma y corte manual.

### 3. Reubicación y Lógica de Turno en Alerta de Production Pack
- **Reubicación DOM:** Removido de cabecera e insertado directamente encima de `#contenedor-tabla-registros` (Historial Oficial).
- **Exclusión de Inicio de Turno:** En `revisarFaltaProduccionPack()` se excluye el registro base inicial (`i === 0`) y la hora de arranque oficial (`07:00` en Turno A/A+/A+B, `15:00` en Turno B, `23:00` en Turno C). La primera exigencia de pack ocurre a la siguiente hora en punto (`08:00`, `16:00`, `00:00`).
- **Texto Detallado y Acción Táctil:** El banner indica las horas faltantes exactas (`⚠️ ¡ALERTA: Falta registrar datos de Production Pack (Hora: XX:00)! 👆 Toca aquí para ir al registro`) y al hacer clic invoca `irARegistroPack()` haciendo scroll y abriendo la edición rápida de pack.

### 4. Verificación de Integridad
- Tags `<script>`, `<style>` y `<div>` verificados y balanceados.
- Persistencia verificada y compatible con temas Claro, Oscuro y Neón.

---
## Registro de Versión v4.0.25 (Version Bump)
- **Versión activa:** `4.0.25` (`var APP_VERSION = "4.0.25";`).
- **Mecanismo de Notificación Automática:**
  - Función `avisarSiHuboActualizacion()` compara `localStorage.getItem('lala_ultima_version_vista')` con `APP_VERSION`.
  - Al detectar la discrepancia en recarga, guarda la clave y lanza tras 900ms el modal `verHistorialVersiones()`.
- **Historial de Novedades (v4.0.25 Actual):**
  1. *Memoria multi-producto en Caducidad:* Hasta 4 slots en memoria local (`lala_caducidad_slots`) para consultar o alternar productos sin perder datos.
  2. *Alerta inteligente de Production Pack:* Reubicada sobre Historial Oficial, excluye arranque oficial (07:00, 15:00, 23:00) y enfoca/abre el registro faltante con un clic.
  3. *Diferenciación Neón:* Balde de Merma en gradiente rosa fresa / carmesí para evitar confusiones con Corte Manual (índigo/violeta).
- **Versión previa:** `v4.0.24` conservada en el registro cronológico del modal.


---

## Módulo Android Bubble App & CI/CD Cloud Build (v4.0.25)
- **Fecha de Implementación:** 2026-10-21
- **Componentes Creados:**
  * `android-bubble-app/`: Arquitectura nativa Android en Kotlin (SDK 34, minSdk 26, Java 17).
  * `MainActivity.kt`: Panel de control Neón con verificación de permisos de superposición (`Settings.canDrawOverlays`), botones de activación de burbuja y detención.
  * `BubbleService.kt`: Servicio en Foreground (`SYSTEM_ALERT_WINDOW` y `FOREGROUND_SERVICE_SPECIAL_USE` con propiedad `floating_quality_control_bubble` compatible con Android 14/15).
    - **Modo Colapsado (Burbuja 62dp):** Gradiente Neón púrpura/violeta, arrastre suave con el dedo y atracción magnética al borde más cercano al soltar.
    - **Modo Expandido (Panel Messenger):** Ventana flotante (92% ancho, 82% alto), barra superior con botones de Minimizar/Recargar/Cerrar, y WebView con JavaScript, DOM Storage, fallback offline a `file:///android_asset/index.html` y online a `https://minusheng.github.io/calidad-app/`.
    - **Gestión de Foco:** Configuración dinámica de `FLAG_NOT_TOUCH_MODAL` (sin `FLAG_NOT_FOCUSABLE` en modo expandido para escribir lotes con el teclado nativo).
  * `.github/workflows/build-apk.yml`: Compilación automatizada en la nube con GitHub Actions (Java 17, Gradle 8.5, tarea `assembleDebug` y subida del artefacto `SNDR-Calidad-Bubble-APK`).


---

## Corrección Crítica: Android 14/15 One UI & Samsung WebView Crash (v4.0.25)
- **Fecha:** 2026-10-21
- **Diagnóstico del Crash:**
  1. En Samsung con Android 14/15 (One UI), la instanciación de un `WebView` dentro de un `Service` context crudo o mediante `LayoutInflater.from(service)` provocaba un `AndroidRuntimeException` o crash inmediato al levantar `BubbleService`.
  2. En Android 14 (API 34), el `ForegroundService` con `specialUse` requería `POST_NOTIFICATIONS` solicitado en runtime y un `smallIcon` garantizado en la notificación persistente.
  3. Falta de verificación de `Settings.canDrawOverlays(this)` previa a `windowManager.addView` podía disparar `BadTokenException`.
- **Solución Implementada:**
  - **ContextThemeWrapper:** Se envuelve tanto el `LayoutInflater` como la creación del `WebView` con `ContextThemeWrapper(this, android.R.style.Theme_DeviceDefault)`, y se desacopla el WebView del XML a un contenedor dinámico `FrameLayout` con inicialización lazy en el hilo principal (`Handler(Looper.getMainLooper())`).
  - **Foreground Service Seguro:** `startForeground(1001, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)` en API 34+ con `android.R.drawable.ic_dialog_info` como icono de notificación infalible.
  - **Permiso POST_NOTIFICATIONS:** Verificación y solicitud en `MainActivity.kt` (Android 13+).
  - **Escudo Anti-Crash Global:** Implementado `Thread.setDefaultUncaughtExceptionHandler` y bloques `try/catch (Throwable)` en todos los ciclos de vida y métodos de `BubbleService` y `MainActivity`, mostrando cualquier anomalía en Toast sin cerrar la aplicación.

---

## Corrección Definitiva: UI 100% Programática en Kotlin & Lazy WebView en BubbleService (v4.0.25)
- **Fecha:** 2026-10-22
- **Diagnóstico del Crash en Samsung One UI:**
  * El escudo Anti-Crash capturó en el dispositivo: `Binary XML file line #45 in com.sndrlabs.calidadbubble...`.
  * Causa raíz: `LayoutInflater` intentando inflar recursos XML (`layout_bubble_panel.xml`) desde el contexto sin tema visual de un `Service`, provocando fallo fatal en atributos de tema (`?attr/selectableItemBackgroundBorderless` o drawables).
- **Solución Implementada:**
  1. **Cero Dependencia de XML en BubbleService:** Se eliminó por completo `LayoutInflater` y cualquier referencia a `R.layout.*` en el servicio.
  2. **Burbuja Flotante 100% Programática:**
     * `FrameLayout` de 62dp x 62dp creado en Kotlin puro.
     * `GradientDrawable` circular (`OVAL`) con degradado neón `#D946EF` a `#6D28D9` y borde cian `#22D3EE` de 2dp.
     * `TextView` centrado a 26sp con icono `"⚡"` sin recursos XML externos.
     * Adición inmediata y segura al `WindowManager` al arrancar el servicio.
  3. **Panel Expandido Programático:**
     * Contenedor vertical `LinearLayout` con fondo `#0F172A`, esquinas redondeadas (16dp) y borde `#7C3AED` (2dp).
     * Barra superior con título `"⚡ Calidad App v4.0.25"` y botones de texto planos `"🔄"`, `"➖"` y `"❌"` con `GradientDrawable` redondeado (`#334155`).
     * Arrastre suave del panel en pantalla.
  4. **WebView Lazy Init y Fallback Samsung:**
     * Cero instanciación de WebView durante el arranque (`onCreate()`).
     * Carga diferida únicamente cuando el usuario expande el panel por primera vez.
     * Doble capa de protección: primero intenta `ContextThemeWrapper(applicationContext, android.R.style.Theme_DeviceDefault_NoActionBar)`, y si la ROM de Samsung lanza excepción, recurre automáticamente a `WebView(applicationContext)` directo.

---

## Actualización v4.0.26: Cyber Cyan UI, Icono Oficial SNDR, File Picker Bridge & Alerta Háptica
- **Fecha:** 2026-10-22
- **Objetivos y Resultados:**
  1. **Remodelación Visual Completa (Cero Rosa):**
     * Eliminados por completo los colores magenta/rosa (#D946EF, #7C3AED, #BE185D).
     * Paleta unificada 100% Neón Cian (#00F2FE, #00D2FF), Cyber Azul (#0077FF) y Dark Slate (#0F172A, #1E293B).
     * `MainActivity`: Header con contenedor circular y borde cian para el logo oficial SNDR, preview circular superior derecho con borde cian neón 2dp, tarjeta de permisos con fondo `#0F172A` y borde cian `#00F2FE`.
     * Botón "ACTIVAR BURBUJA FLOTANTE": Gradiente Cian Neón (#00D2FF a #0077FF), esquinas 12dp, texto blanco nítido en negrita y sombra cian.
     * Botón "DETENER BURBUJA": Grafito oscuro `#1E293B`, borde carmesí `#EF4444` (1.5dp) y texto coral `#F87171`.
  2. **Iconos Oficiales de la Aplicación:**
     * Procesado `icono-app.png` con Pillow generando `ic_launcher.png` e `ic_launcher_round.png` en densidades `mdpi`, `hdpi`, `xhdpi`, `xxhdpi` y `xxxhdpi`.
     * Generado `res/drawable/app_logo.png` optimizado (512x512).
     * En `BubbleService.kt`: Reemplazado el TextView por `ImageView` centrado con padding de 8dp dentro del círculo con borde cian neón.
  3. **Puente Nativo para Restaurar Backups (File Picker en WebView):**
     * Creada `FilePickerActivity.kt`: lanza `Intent.ACTION_GET_CONTENT` (*/* y JSON) y canaliza la Uri seleccionada hacia el callback `ValueCallback<Array<Uri>>`.
     * Registrada en `AndroidManifest.xml` con tema transparente `Theme.Translucent.NoTitleBar`.
     * Integrado `onShowFileChooser(...)` en `WebChromeClient` dentro de `BubbleService.kt`, habilitando la restauración completa de backups desde memoria.
  4. **Alerta Háptica de Falta de Pack:**
     * Permiso `android.permission.VIBRATE` en `AndroidManifest.xml`.
     * Implementada interfaz `@JavascriptInterface` `AndroidBridge.vibrateAlert()` con patrón `[0, 400, 200, 400] ms` para API 26 a 34+.
     * Invocación automática en `revisarFaltaProduccionPack()` de `index.html`.
     * Elevada versión oficial a `v4.0.26` en `index.html`, `build.gradle` y `BubbleService`.
     * Sincronizado el archivo `android-bubble-app/app/src/main/assets/index.html` para soporte offline idéntico.

---

## Actualización v4.0.27: Visor Nativo de Logs en MainActivity y Blindaje del Selector de Archivos (HyperOS / Xiaomi / Android)
- **Fecha de Implementación:** 2026-10-23
- **Diagnóstico y Causa Raíz:**
  1. En dispositivos Xiaomi / HyperOS / MIUI, el callback `ValueCallback<Array<Uri>>` en `onShowFileChooser` quedaba bloqueado si una intención previa no se resolvía o cancelaba de inmediato al reabrir el selector.
  2. Al seleccionar archivos `.TXT` o `.JSON` de respaldo desde proveedores de almacenamiento (`content://`), faltaba la concesión explícita de `Intent.FLAG_GRANT_READ_URI_PERMISSION` y la habilitación completa de `allowContentAccess` y `allowFileAccess` en `WebSettings`.
  3. No existía visibilidad de los eventos JS en consola ni de las transiciones del servicio nativo en ejecución, dificultando la auditoría de fallos en producción.
- **Soluciones Implementadas:**
  1. **Motor de Logs Nativo (`AppLogManager.kt`):**
     * Singleton en Kotlin con buffer circular de 150 slots (`HH:mm:ss`, etiquetas `[WEB]`, `[SERVICE]`, `[PICKER]`, `[ERROR]`, etc.).
     * Sistema de listeners con despacho al MainLooper para sincronización en tiempo real con la UI.
     * Métodos de exportación de log en texto plano (`exportLogs()`) y limpieza (`clearLogs()`).
  2. **Consola de Diagnóstico en `MainActivity`:**
     * Tarjeta colapsable con diseño Cyberpunk / Dark Slate (`#0F172A`, borde cian `#00F2FE`), scroll independiente (`ScrollView` con `nestedScrollingEnabled="true"`) y tipografía monoespaciada.
     * Coloreado dinámico de líneas (`SpannableStringBuilder`): Cian para eventos de servicio, Rojo para errores/cancelaciones, Verde para éxitos, Azul para selector y Violeta para eventos web.
     * Botones nativos: `📋 Copiar Registro` (hacia portapapeles) y `🗑️ Limpiar`.
  3. **Blindaje de `BubbleService.kt` y `FilePickerActivity.kt`:**
     * `WebSettings`: `allowContentAccess = true`, `allowFileAccess = true`, `allowFileAccessFromFileURLs = true`, `allowUniversalAccessFromFileURLs = true`.
     * `WebChromeClient.onConsoleMessage`: redirección de `console.log`, `console.warn` y `console.error` hacia `AppLogManager.log(...)`.
     * `onShowFileChooser`: verificación y cancelación segura (`fileUploadCallback?.onReceiveValue(null)`) de cualquier callback pendiente previo antes de instanciar un nuevo intent.
     * `FilePickerActivity`: configuración de `Intent.FLAG_GRANT_READ_URI_PERMISSION`, tipo `*/*` y resolución inmediata con `null` en cancelación o destrucción (`onDestroy`).
  4. **Bump de Versión Oficial v4.0.27:**
     * `build.gradle` (app): `versionCode 3`, `versionName "4.0.27"`.
     * `index.html`: `APP_VERSION = "4.0.27"` e inclusión en `verHistorialVersiones()`.
     * Sincronización idéntica de `android-bubble-app/app/src/main/assets/index.html`.
     * Header y preview en `MainActivity` y `BubbleService` elevados a `v4.0.27`.

---

## Corrección CI/CD: Resolución de Atributos AAPT2 e Imports Kotlin en Android App (v4.0.27)
- **Fecha:** 2026-10-23
- **Diagnóstico del Fallo en GitHub Actions (Run #39944574805):**
  * El paso `Build Debug APK` falló durante `assembleDebug` debido al atributo `android:nestedScrollingEnabled="true"` en `<ScrollView>` dentro de `activity_main.xml`. En AAPT2 de Android SDK, `nestedScrollingEnabled` en el namespace `android:` sobre `android.widget.ScrollView` no es reconocido por el compilador de recursos XML.
  * En `FilePickerActivity.kt` faltaba la importación explícita de `android.webkit.ValueCallback`.
  * La declaración de `fileUploadCallback` en `BubbleService.companion object` requería tipado canónico explícito `android.webkit.ValueCallback<Array<android.net.Uri>>?`.
- **Solución Quirúrgica:**
  1. `activity_main.xml`: Se removió el atributo problemático `android:nestedScrollingEnabled="true"` de la consola de logs.
  2. `MainActivity.kt`: Se delegó la habilitación de nested scrolling a nivel de código Kotlin con `scrollLogViewer.isNestedScrollingEnabled = true` en API 21+, garantizando 100% de compatibilidad con AAPT2. Se verificaron todos los imports de `Spannable`, `ClipboardManager` y vistas.
  3. `BubbleService.kt`: Declaración canónica explícita de `fileUploadCallback: android.webkit.ValueCallback<Array<android.net.Uri>>?` en `companion object`, con imports completos de WebChromeClient, Uri, Handler, Looper y ConsoleMessage.
  4. `FilePickerActivity.kt`: Imports explícitos de `Activity`, `Intent`, `Uri`, `Bundle` y `ValueCallback`.
  5. `AppLogManager.kt`: Imports verificados de `Handler`, `Looper`, `SimpleDateFormat`, `Date` y `Locale`.

---

## Corrección CI/CD: Reparación de Sintaxis Kotlin e Imports en BubbleService (v4.0.27)
- **Fecha:** 2026-10-23
- **Diagnóstico del Fallo en GitHub Actions (Run #39944574805 / #39945533939):**
  * `BubbleService.kt:151:13 Expecting member declaration`: Durante la inyección de logs, una llamada a `AppLogManager.log(...)` se concatenó accidentalmente con la declaración del método `private fun startForegroundServiceWithNotification()`, corrompiendo la definición de la función antes de su bloque `{`.
  * `FilePickerActivity.kt`: Se añadió import explícito de `android.webkit.ValueCallback`.
  * `activity_main.xml`: Se removió el atributo `android:nestedScrollingEnabled` en `<ScrollView>` y se trasladó su habilitación limpia a Kotlin (`isNestedScrollingEnabled = true` en API 21+).
- **Corrección Quirúrgica:**
  1. Restaurada la firma limpia de `private fun startForegroundServiceWithNotification() {` en `BubbleService.kt`.
  2. Tipado canónico `android.webkit.ValueCallback<Array<android.net.Uri>>?` verificado en `companion object`.
  3. Verificación de todos los imports en `AppLogManager.kt`, `MainActivity.kt` y `FilePickerActivity.kt`.
