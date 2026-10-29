# Checklist de requisitos y pruebas

Estado: Revisión estática del código. Fase 2 — refinamiento y aprobación. Las marcas indican
presencia observada en el código, no ejecución ni validación funcional; no se revisaron archivos de
prueba ni se ejecutó QA.

## Práctica académica

- [x] Tres Activities presentes: `MainActivity`, `DetailActivity`, `NotesActivity` (presencia en
  código; no verificado en ejecución).
- [x] `work_id` se transmite/recupera en navegación hacia detalle y notas (presencia en código; no
  verificado en ejecución).
- [x] AlertDialog para confirmar borrado y solicitar descarga del modelo de traducción (presencia en
  código).
- [x] SharedPreferences persiste favoritos y última obra consultada (revisión estática; persistencia
  tras reinicio no probada).
- [x] Room/SQLite almacena notas personales. No almacena catálogo ni favoritos.
- [x] Hay servicios Retrofit para catálogo propio, Open Opus y Europeana; llamadas reales y contrato
  no validados en esta revisión.
- [x] `BalletListAdapter` usa `ListAdapter`, `DiffUtil` y callbacks de clic/favorito.
- [x] Búsqueda local por título y compositor; filtro de favoritos presente.
- [ ] Localización trilingüe pendiente: hoy solo existe `values/strings.xml` con español. El
  predeterminado debe ser inglés, con recursos equivalentes en español y francés.
- [ ] Externalizar todos los textos visibles: se observaron textos literales en Kotlin.
- [x] ViewBinding observado en las pantallas.
- [x] Componentes Material están configurados/usados; no se ha validado su comportamiento en
  ejecución.

## Funcionalidad

- [x] Código consulta catálogo propio (`GET /api/ballets`); ejecución/servicio no verificados.
- [x] Búsqueda local por título y compositor en la barra superior con filtrado reactivo.
- [x] Filtro de favoritos mediante Chips (Todos / Favoritos).
- [x] Código consulta detalle mediante `GET /api/ballets/{id}`; ejecución/servicio no verificados.
- [x] Consultas complementarias: Open Opus (datos del compositor) y Europeana (archivos digitales adicionales).
- [x] Estados de carga (`ProgressBar`), vacío y error con botón de reintento.
- [x] Favoritos persistentes en SharedPreferences (`SessionManager`).
- [x] Crear, listar y eliminar notas asociadas a `work_id` en Room; no se observó edición.
- [ ] Caché offline/catálogo local en Room pendiente; ante fallo de red no hay catálogo persistido.

## ML Kit

- [x] Integración ML Kit vía `TranslationUtils`; traducción parcial de títulos y algunos textos de
  detalle.
- [x] Código contempla descarga del modelo y diálogo ante uso de datos móviles; no probado en
  dispositivo.
- [x] Código inicia precarga en MainActivity con Wi-Fi.
- [x] El código mantiene texto original cuando falla; no se ha validado visualmente cada flujo.
- [ ] Selector Original/Traducido/Ambos e idioma preferido pendiente; origen inglés está codificado.
- [ ] Pruebas con modelos descargados/no descargados y cambio de idioma no realizadas.

## Entrega

- [x] Estructura por Activities, adapters, data local/remota/repositorio y utils.
- [ ] Revisar gestión de credencial Europeana: el archivo Gradle incluye un valor fallback literal;
  no afirmar que el secreto está protegido sin corregir y validar esa configuración.
- [ ] Ausencia de secretos en Git no verificada en esta revisión.

## Pendiente de validación

- No se ejecutó compilación, aplicación ni pruebas; esta actualización es una revisión estática del
  código y la documentación.
- La integración remota no se declara funcional ni validada contra un contrato aprobado o evidencia
  de ejecución.
- Confirmar en una tarea posterior la aprobación del alcance y completar las lagunas anteriores
  antes de marcar entrega.
