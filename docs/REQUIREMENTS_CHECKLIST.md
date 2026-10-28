# Checklist de requisitos y pruebas

Estado: Implementación completada conforme a los acuerdos del proyecto.

## Práctica académica

- [x] Al menos tres Activities funcionales (`MainActivity`, `DetailActivity`, `NotesActivity`).
- [x] Extra de Intent transmitido, recuperado y usado (`work_id`, `work_title`).
- [x] AlertDialog activado por una acción del usuario (confirmación de eliminación de nota y aviso de descarga ML Kit).
- [x] Valor persistente mediante SharedPreferences (`SessionManager` para favoritos y última obra consultada).
- [x] Room sobre SQLite con datos relevantes (`NoteEntity`, `NoteDao`, `AppDatabase` para notas personales).
- [x] Llamada REST real mediante Retrofit (`BalletApiService` contra `ballet-api.vercel.app`, `EuropeanaApiService` y `OpenOpusApiService`).
- [x] RecyclerView con evento de clic por celda implementado mediante lambda/callback (`BalletListAdapter` con `ListAdapter` + `DiffUtil`).
- [x] Menú / Barra superior con logo personalizado (48dp x 48dp) y búsqueda local por título.
- [x] Textos externalizados y recursos en español en `strings.xml`.
- [x] ViewBinding; sin `findViewById` en código de UI.
- [x] Componentes Material Design 3 (CardView, TextInputLayout, TextInputEditText, MaterialButton, Chip, ExtendedFAB).

## Funcionalidad

- [x] Catálogo cargado desde la API propia (`GET /api/ballets`).
- [x] Búsqueda local por título y compositor en la barra superior con filtrado reactivo.
- [x] Filtro de favoritos mediante Chips (Todos / Favoritos).
- [x] Detalle completo de la obra cargado desde `GET /api/ballets/{id}`.
- [x] Consultas complementarias: Open Opus (datos del compositor) y Europeana (archivos digitales adicionales).
- [x] Estados de carga (`ProgressBar`), vacío y error con botón de reintento.
- [x] Favoritos persistentes en SharedPreferences (`SessionManager`).
- [x] CRUD completo de notas personales en Room asociadas a la obra (`work_id`).
- [ ] (Opcional) Caché offline de obras en Room si da tiempo al final.

## ML Kit

- [x] Integración con `TranslationUtils` (extensiones `TextView.setTranslatedText`).
- [x] Modelos descargables y comprobación de Wi-Fi vs. datos móviles con `AlertDialog`.
- [x] Precarga silenciosa en segundo plano en `MainActivity` cuando hay Wi-Fi.
- [x] Texto original preservado y mostrado entre paréntesis en el catálogo.
- [x] Fallback si falta modelo o falla la traducción.

## Entrega

- [x] Estructura de paquetes limpia (`activities`, `adapters`, `data.local`, `data.remote`, `data.repository`, `utils`).
- [x] Clave de Europeana protegida en `local.properties` y `BuildConfig`.
- [x] Sin credenciales ni secretos en Git.
