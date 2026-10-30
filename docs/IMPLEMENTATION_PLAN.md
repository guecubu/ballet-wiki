# Plan de implementación por fases (Actualizado)

Estado: Fases 0 a 5 completadas e integradas con arquitectura Shell de Fragments y diseño Figma 1:347.

## Fase 0 — Preparación, auditoría y consenso del diseño (Completada)
- Especificación completa en `DESIGN_WORK_PLAN.md`, `SPECIFICATION.md` y `IMPLEMENTATION_PLAN.md`.
- Conectividad con API `https://ballet-api.vercel.app/api/ballets` y DTOs con Gson.

## Fase 1 — Modelos de Datos, Retrofit y Room (Completada)
- DTOs, `BalletApiService`, `NoteEntity`, `NoteDao`, `AppDatabase` y `BalletRepository`.

## Fase 2 — Catálogo Principal, Tipografía y Navegación Shell (Completada)
- Implementación de `MainActivity` como Shell con `CatalogFragment` y `NotesFragment`.
- Integración de `BottomNavigationView` persistente de 80dp.
- Tipografía exacta (`@font/bodoni_moda` y `@font/plus_jakarta_sans`).
- Tarjetas de ballet con relación de aspecto 3:4, gradiente scrim y badges de época.

## Fase 3 y 4 — Detalle, Favoritos y Notas (Completada)
- `DetailActivity` con soporte de favoritos persistentes en SharedPreferences y gestión de notas personales con Room y `AlertDialog`.

## Fase 5 — Traducción con ML Kit (Completada)
- Integración de `TranslationUtils` con descarga de modelos y diálogo de confirmación en datos móviles.
