# Plan de implementación por fases

Estado: Preparación y especificación de arquitectura y contrato.

## Fase 0 — Preparación, auditoría y consenso del diseño (fase actual en curso)

**Estado:** Fase 0 en curso.
**Entregable:** Documentación consensuada (`SPECIFICATION.md`, `IMPLEMENTATION_PLAN.md`, `REQUIREMENTS_CHECKLIST.md`).
- Origen de datos definido: API propia `https://ballet-api.vercel.app/api/ballets` y `/api/ballets/{id}`.
- Contrato de DTOs: Anotados con `@SerializedName` de Gson para mapeo snake_case a camelCase. Listado extrae: `id`, `title`, `composer`, `premiereYear`, `coverImageUrl`. Detalle extrae la información completa.
- Estructura de paquetes acordada: `activities`, `adapters`, `data` (`local`, `remote`, `repository`), `utils`.
- Estrategia de persistencia acordada:
  - SharedPreferences (`SessionManager` en `utils`): Almacenamiento de IDs favoritos (`Set<String>`).
  - Room (`local`): Gestión de notas de usuario asociadas a cada obra (`work_id`).
  - Actividades directas con `lifecycleScope` (sin capa de `ViewModel`).
  - Traducción ML Kit con extensiones de `TextView` (`TranslationUtils` en `utils`) con diálogo `AlertDialog` de confirmación en datos móviles.

**Puerta de Fase 0:** Aprobación expresa del usuario de esta fase para iniciar la Fase 1.

## Fase 1 — Configuración, Modelos de Datos, Retrofit y Room

- Limpiar dependencias y código obsoleto (retirar prototipo Wikidata previo).
- Crear estructura de paquetes (`activities`, `adapters`, `data.local`, `data.remote`, `data.repository`, `utils`).
- Configurar dependencias necesarias en Gradle (Retrofit, Gson, Room, ML Kit Translate, Glide/Coil para imágenes) e inspeccionar compatibilidad.
- Implementar DTOs con `@SerializedName` y `BalletApiService` (Retrofit).
- Implementar Room: `NoteEntity`, `NoteDao` y `AppDatabase`.
- Implementar `SessionManager` con SharedPreferences para favoritos.
- Implementar `BalletRepository` como punto único de acceso para red y base de datos local.
- Compilar y verificar que no haya errores de construcción.

**Aceptación:** Compilación limpia y modelos/acceso a datos listos para ser consumidos por la UI.

## Fase 2 — Catálogo Principal y Navegación (MainActivity & ListAdapter)

- Diseñar layout de `MainActivity` con `RecyclerView` / `ListAdapter` y ViewBinding.
- Implementar `BalletListAdapter` con `DiffUtil` y callback/lambda para eventos de clic.
- Cargar listado desde la API y manejar estados de carga, lista vacía y error de red.
- Implementar filtrado/búsqueda local por título en la TopAppBar.
- Navegación hacia `DetailActivity` pasando el extra `work_id` en el Intent.
- Compilar y verificar catálogo.

**Aceptación:** Listado funcional con imágenes, búsqueda por título y navegación con extras.

## Fase 3 — Pantalla de Detalle y Favoritos (DetailActivity)

- Implementar `DetailActivity` recibiendo y validando el extra `work_id`.
- Consultar el endpoint de detalle `GET /api/ballets/{id}` mediante `BalletRepository`.
- Mostrar datos completos de la obra (sinopsis, personajes, variaciones, representaciones, etc.).
- Integrar botón/icono de Favorito utilizando `SessionManager` (marcar/desmarcar y persistir).
- Botón de navegación hacia la pantalla de notas de la obra (`NotesActivity`).
- Compilar y probar.

**Aceptación:** Detalle completo y gestión de favoritos persistente en SharedPreferences.

## Fase 4 — Gestión de Notas y AlertDialog (NotesActivity & Room)

- Implementar `NotesActivity` y `NotesAdapter` para listar notas de la obra actual.
- Formulario sencillo para añadir nueva nota en Room (`NoteDao`).
- Acción de eliminar nota confirmada mediante `AlertDialog`.
- Validar persistencia de notas asociadas al `work_id` correcto tras reinicio de app.
- Compilar y probar.

**Aceptación:** CRUD de notas funcional en Room y `AlertDialog` activo.

## Fase 5 — Traducción con ML Kit (TranslationUtils)

- Integrar `TranslationUtils.kt` adaptado en el paquete `utils`.
- Añadir traducción dinámica para textos en `DetailActivity` (y opcionalmente títulos en el catálogo).
- Gestión de descarga de modelos y verificación del `AlertDialog` en conexiones no Wi-Fi.
- Modo seguro si falla la traducción o no hay conexión.

**Aceptación:** Traducción funcional y respetuosa con el texto original.

## Fase 6 — QA, Caché Offline (Opcional) y Entrega

- *(Opcional / Si da tiempo)* Implementar caché local de obras en Room para consulta offline.
- Ejecutar compilación limpia y pruebas de todos los requisitos académicos.
- Actualizar `README.md` y `REQUIREMENTS_CHECKLIST.md`.
- Verificación final de recursos XML, strings y entrega.

**Aceptación:** Checklist 100% completado y aplicación lista.
