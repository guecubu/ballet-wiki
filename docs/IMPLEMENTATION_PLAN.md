# Plan de implementación por fases

Estado: Fase 2 — refinamiento y aprobación. Hay implementación posterior a la Fase 2 en el código;
su presencia no equivale a cierre ni QA validado de fases posteriores.

## Fase 0 — Preparación, auditoría y consenso del diseño (completada parcialmente; documentación desactualizada)

**Estado:** No representa el estado actual del proyecto.
**Entregable:** Documentación consensuada (`SPECIFICATION.md`, `IMPLEMENTATION_PLAN.md`, `REQUIREMENTS_CHECKLIST.md`).
- Origen de datos definido: API propia `https://ballet-api.vercel.app/api/ballets` y `/api/ballets/{id}`.
- Contrato de DTOs: Anotados con `@SerializedName` de Gson para mapeo snake_case a camelCase. Listado extrae: `id`, `title`, `composer`, `premiereYear`, `coverImageUrl`. Detalle extrae la información completa.
- Estructura de paquetes acordada: `activities`, `adapters`, `data` (`local`, `remote`, `repository`), `utils`.
- Estrategia de persistencia acordada:
  - SharedPreferences (`SessionManager` en `utils`): Almacenamiento de IDs favoritos (`Set<String>`).
  - Room (`local`): Gestión de notas de usuario asociadas a cada obra (`work_id`).
  - Actividades directas con `lifecycleScope` (sin capa de `ViewModel`).
  - Traducción ML Kit con extensiones de `TextView` (`TranslationUtils` en `utils`) con diálogo `AlertDialog` de confirmación en datos móviles.

**Nota de estado:** La implementación existente ya incluye catálogo, detalle, notas, favoritos y
traducción parcial. Se conserva Fase 2 como fase de trabajo actual según lo indicado por el usuario;
el código contiene avances adicionales aún en refinamiento y aprobación.

## Fase 1 — Configuración, Modelos de Datos, Retrofit y Room (implementada según revisión estática; QA pendiente)

- Limpiar dependencias y código obsoleto (retirar prototipo Wikidata previo).
- Crear estructura de paquetes (`activities`, `adapters`, `data.local`, `data.remote`, `data.repository`, `utils`).
- Configurar dependencias necesarias en Gradle (Retrofit, Gson, Room, ML Kit Translate, Glide/Coil para imágenes) e inspeccionar compatibilidad.
- Implementar DTOs con `@SerializedName` y `BalletApiService` (Retrofit).
- Implementar Room: `NoteEntity`, `NoteDao` y `AppDatabase`.
- Implementar `SessionManager` con SharedPreferences para favoritos.
- Implementar `BalletRepository` como punto único de acceso para red y base de datos local.
- Compilar y verificar que no haya errores de construcción.

**Aceptación:** Compilación limpia y modelos/acceso a datos listos para ser consumidos por la UI.

## Fase 2 — Catálogo Principal y Navegación (MainActivity & ListAdapter) — fase actual, en refinamiento/aprobación

- Diseñar layout de `MainActivity` con `RecyclerView` en cuadrícula de 2 columnas
  (`GridLayoutManager`), `ListAdapter` y ViewBinding.
- Implementar `BalletListAdapter` con `DiffUtil`, callback/lambda y diseño de tarjeta vertical
  (imagen superior sobre textos).
- Cargar listado desde la API y manejar estados de carga, lista vacía y error de red.
- Implementar filtrado/búsqueda local por título y compositor en la barra superior.
- Revisar diferencias entre el código actual y los criterios de esta fase; no declarar la fase
  cerrada sin revisión y aprobación.
- Navegación hacia `DetailActivity` pasando el extra `work_id` en el Intent.
- Compilar y verificar catálogo.

**Aceptación:** Listado funcional con imágenes, búsqueda por título y navegación con extras.

## Fase 3 — Pantalla de Detalle y Favoritos (implementada parcialmente; requiere refinamiento)

- Implementar `DetailActivity` recibiendo y validando el extra `work_id`.
- Consultar el endpoint de detalle `GET /api/ballets/{id}` mediante `BalletRepository`.
- Mostrar datos completos de la obra (sinopsis, personajes, variaciones, representaciones, etc.).
- Integrar botón/icono de Favorito utilizando `SessionManager` (marcar/desmarcar y persistir).
- Botón de navegación hacia la pantalla de notas de la obra (`NotesActivity`).
- Compilar y probar.

**Aceptación:** Detalle completo y gestión de favoritos persistente en SharedPreferences.

## Fase 4 — Gestión de Notas y AlertDialog (implementada parcialmente; requiere refinamiento)

- Implementar `NotesActivity` y `NotesAdapter` para listar notas de la obra actual.
- Formulario sencillo para añadir nueva nota en Room (`NoteDao`).
- Acción de eliminar nota confirmada mediante `AlertDialog` (creación y borrado; no hay edición).
- Validar persistencia de notas asociadas al `work_id` correcto tras reinicio de app.
- Compilar y probar.

**Aceptación:** Crear, listar y borrar notas en Room con `AlertDialog`; edición de notas aún no
implementada.

## Fase 5 — Traducción con ML Kit (integración parcial; requiere refinamiento)

- Integrar `TranslationUtils.kt` adaptado en el paquete `utils`.
- Añadir traducción dinámica para textos en `DetailActivity` (y opcionalmente títulos en el catálogo).
- Gestión de descarga de modelos y verificación del `AlertDialog` en conexiones no Wi-Fi.
- Modo seguro si falla la traducción o no hay conexión.

**Aceptación:** Traducción funcional y respetuosa con el texto original.

## Fase 6 — QA, Caché Offline (Opcional) y Entrega (pendiente)

- *(Opcional / Si da tiempo)* Implementar caché local de obras en Room para consulta offline.
- Ejecutar compilación y pruebas de los requisitos académicos; esta revisión documental no las
  ejecuta.
- Actualizar `README.md` y `REQUIREMENTS_CHECKLIST.md`.
- Verificación final de recursos XML, strings y entrega.

**Aceptación:** Checklist 100% completado y aplicación lista.

## Estado observado en la revisión documental

- `MainActivity`, `DetailActivity` y `NotesActivity` están implementadas. El catálogo y detalle
  consultan Retrofit; notas se almacenan en Room. Favoritos y última obra vista se guardan mediante
  SharedPreferences.
- Existe integración de `BalletApiService`, `OpenOpusApiService` y `EuropeanaApiService`; esta
  revisión no valida disponibilidad, contrato aprobado ni funcionamiento real de los servicios.
- No existe persistencia/caché del catálogo en Room; el catálogo depende de la red. Room contiene
  únicamente notas.
- El detalle presenta traducción de algunos textos/títulos vía ML Kit, con inglés codificado como
  idioma de origen. No hay selector Original/Traducido/Ambos ni elección de idioma preferido.
- La interfaz está actualmente localizada solo en español en `values/strings.xml`; el requisito
  vigente es inglés por defecto, español y francés para cadenas existentes y futuras. También hay
  textos visibles codificados directamente en Kotlin.
- La arquitectura usa Activities y `lifecycleScope` sin ViewModel. Las notas admiten
  crear/listar/eliminar, no editar.
- Esta revisión fue de código y documentación, sin ejecutar compilación, aplicación ni pruebas. No
  usarla como evidencia de QA funcional.
