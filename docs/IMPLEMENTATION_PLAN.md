# Plan de implementación por fases

Estado: preparación y especificación; no se autoriza todavía la ejecución de fases de
implementación. La API propia y su contrato están pendientes. No estimar calendario hasta conocer el
contrato y acordar el alcance.

## Fase 0 — Preparación y auditoría del proyecto (actual)

**Entregable:** documentación coherente, inventario de configuración actual y lista de decisiones
pendientes. Sin cambios de producción ni validación de API.

- Revisar la especificación, el plan, el checklist, las instrucciones de agentes y skills.
- Inspeccionar versiones Gradle/Kotlin/AGP/minSdk y dependencias existentes antes de proponer
  cambios técnicos.
- Retirar el prompt inicial y las hipótesis de proveedores terceros de la planificación.
- Tener en cuenta que existe código de prototipo Wikidata; no considerarlo contrato/validación de la
  API propia y no modificarlo durante esta fase documental.
- Conservar objetivos de producto y requisitos académicos como objetivos por verificar, no como
  implementación ya existente.

**Puerta:** completar preparación documental. La integración API no puede empezar sin el contrato de
la API propia.

## Fase 0.5 — Recepción y análisis del contrato (pendiente; bloqueante)

**Entregable:** contrato revisado, preguntas resueltas, diseño de integración aprobado y pruebas
contractuales acordadas.

- Recibir especificaciones oficiales: entornos/base URL, recursos/endpoints, métodos, parámetros,
  modelos/campos, errores, autenticación, límites y reglas operativas.
- Confirmar catálogo, búsqueda, detalle, identificadores, idioma, procedencia, paginación y
  operaciones realmente disponibles.
- Definir gestión de secretos/configuración sin credenciales versionadas, DTO, mapeo a Room, caché y
  estados recuperables conforme al contrato.
- Acordar pruebas reproducibles y autorización de llamadas reales; no inventar respuestas ni afirmar
  validación sin evidencia.
- Actualizar especificación, checklist y fases; presentar decisiones no resueltas al usuario y
  detenerse para aprobación.

**Puerta:** contrato comprendido y aprobado por el usuario. Hasta entonces no implementar ni añadir
dependencias para la integración.

## Fase 1 — Esqueleto y persistencia

- Revisar las versiones existentes y dependencias.
- Definir entidades Room, DAO y base de datos.
- Implementar precarga idempotente de las obras iniciales.
- Preservar favoritos y notas durante cualquier actualización.
- Añadir SharedPreferences para una preferencia útil.
- Compilar y probar persistencia/reinicio.

**Aceptación:** catálogo local disponible; datos personales persistentes; compilación correcta.

## Fase 2 — Navegación, catálogo y detalle

- Implementar MainActivity, DetailActivity y NotesActivity.
- RecyclerView con callback/lambda por elemento.
- Pasar `work_id` por Intent y validar el extra.
- Añadir AppBar/menu.
- Usar ViewBinding y recursos de strings.
- Añadir estados vacío/carga/error.
- Compilar y probar los flujos.

**Aceptación:** navegación completa y requisitos académicos de Activities/Intent/RecyclerView
satisfechos.

## Fase 3 — Favoritos y notas

- Favoritos en Room.
- CRUD de notas asociadas a `work_id`.
- AlertDialog al eliminar.
- Validación de entradas y estados vacíos.
- Pruebas de persistencia y asociación correcta.

**Aceptación:** favoritos/notas persisten y pertenecen a la obra correcta.

## Fase 4 — API propia y caché (bloqueada hasta completar Fase 0.5)

- Implementar Retrofit conforme al contrato aprobado, sin asumir rutas ni estructura.
- Mapear DTO a modelos locales utilizando únicamente campos contractuales.
- Implementar operaciones remotas realmente disponibles y estados/errores definidos.
- Registrar `lastFetchedAt` solo tras una respuesta válida y según política aprobada.
- Definir frescura y reintentos basados en evidencia/contrato, sin peticiones redundantes.
- Probar autenticación, límites, timeout, resultados vacíos, falta de red y errores pertinentes al
  contrato.
- Documentar procedencia y atribución solo cuando correspondan.

**Aceptación:** prueba real de red y recuperación desde Room sin conexión.

## Fase 5 — ML Kit y visualización multilingüe

- Integrar el servicio de traducción oficial.
- Gestionar modelos español/inglés/francés de forma explícita.
- No asumir que todos están descargados.
- Implementar Original / Traducido / Ambos en detalle.
- Mantener el texto original en Room.
- Cachear traducciones en memoria por contenido e idiomas.
- No usar ML Kit para construir consultas remotas salvo decisión explícita posterior; preservar su
  función de presentación.
- Probar falta de modelo, error de traducción y cambio de idioma.

**Aceptación:** interfaz fluida; el original permanece disponible; la traducción es opcional y
recuperable.

## Fase 6 — QA y entrega

- Ejecutar compilación limpia.
- Probar modo sin conexión.
- Probar los flujos de catálogo, detalle, favoritos, notas y traducción.
- Revisar todos los requisitos académicos.
- Completar README y checklist.
- Revisar dependencias, strings, recursos y credenciales.
- Presentar lista de cambios y limitaciones.

**Aceptación:** checklist completo y README permite reproducir la ejecución.

## Regla de alcance

No comenzar una fase posterior si la anterior no compila o tiene defectos críticos. Cualquier
ampliación fuera del MVP requiere aprobación.
