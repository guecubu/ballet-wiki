# Especificación funcional y técnica — Ballet Reference

## 1. Objetivo y alcance

Aplicación Android nativa de referencia para consultar obras de ballet clásico, encontrar
información adicional, guardar favoritos y notas personales, y leer los textos en su idioma
original, traducidos al idioma preferido o en ambos idiomas.

Objetivo de planificación: MVP de alcance reducido. La agenda/calendario es futura y no debe
desplazar el catálogo, el detalle, los favoritos y las notas. No se estima duración hasta cerrar el
contrato de la API y aprobar el alcance.

### Estado de implementación (Fase 2 — refinamiento y aprobación)

El estado de trabajo comunicado por el usuario es **Fase 2**, en un momento de refinamiento y
aprobación. El repositorio ya contiene pantallas y funciones de fases posteriores; esto describe
avance de código, no que las fases estén cerradas ni que QA se haya ejecutado. Ver
`IMPLEMENTATION_PLAN.md` y `REQUIREMENTS_CHECKLIST.md` para el inventario estático actual.

- Implementado en código: catálogo/detalle con Retrofit, tres Activities, favoritos/última obra en
  SharedPreferences, notas CRUD parcial (crear/listar/eliminar) en Room, consultas adicionales a
  Open Opus y Europeana y traducción ML Kit parcial.
- Limitaciones observadas: Room solo contiene notas; no hay catálogo offline. Las notas no se
  editan. La búsqueda es local sobre el catálogo recibido. El detalle no ofrece selector
  Original/Traducido/Ambos ni idioma preferido; algunas traducciones tienen origen inglés
  codificado.
- Verificación: esta revisión fue estática; no se ejecutaron compilación, app ni pruebas. No se
  afirma que las APIs funcionen o hayan sido validadas en ejecución.
- Requisito actualizado de interfaz: inglés como idioma predeterminado (`values/strings.xml`),
  español (`values-es/strings.xml`) y francés (`values-fr/strings.xml`). Debe cubrir todas las
  cadenas actuales y futuras. Aún no está implementado: actualmente solo aparece
  `values/strings.xml` en español y hay textos visibles literales en Kotlin.
- Seguridad pendiente: `app/build.gradle.kts` contiene un valor fallback literal para la clave de
  Europeana. La protección de la credencial y la ausencia de secretos en Git no se dan por
  verificadas.

### Decisiones y alcance documentados (no equivalen a estado implementado)

- La descripción siguiente registra decisiones/requisitos previos y parte de ella está
  desactualizada respecto del código; para el estado actual prevalece la sección anterior.
- El origen de datos está unificado en la API propia:
  - Listado de obras: `GET https://ballet-api.vercel.app/api/ballets`
  - Detalle de obra: `GET https://ballet-api.vercel.app/api/ballets/{id}`
- Se descarta la búsqueda remota fuera de catálogo; el listado completo se obtiene de la API y la búsqueda es local en la ActionBar (por título) sobre dicho listado.
- La persistencia principal se estructura de forma ligera:
  - **SharedPreferences:** mediante una clase `SessionManager` en el paquete `utils`, guardará el conjunto de IDs favoritos (cumpliendo el requisito de SharedPreferences).
  - **Room (SQLite):** gestionará la tabla de notas personales asociadas a cada obra (`work_id`).
  - **Caché offline de obras en Room:** queda pospuesta como tarea opcional al final del proyecto si hay tiempo.
- **Sin ViewModel:** Para adecuarse al nivel del curso y no sobrecargar la arquitectura, las Activities gestionarán su ciclo de vida y llamarán directamente al repositorio usando corrutinas (`lifecycleScope.launch`).
- **Traducción con ML Kit:** Se implementará mediante funciones de extensión sobre `TextView` (en `TranslationUtils.kt`), gestionando descargas por Wi-Fi y solicitando confirmación con `AlertDialog` si se usan datos móviles. Permite traducir títulos (mostrando original entre paréntesis) y textos de detalle.
- **Estructura de paquetes:** `activities`, `adapters`, `data` (`local`, `remote`, `repository`), `utils`.
- **Serialización:** Los DTOs de Retrofit usarán anotaciones `@SerializedName` de Gson para mapear explícitamente los campos JSON (snake_case) a las propiedades Kotlin (camelCase).

## 2. Requisitos funcionales

### RF-01 — Catálogo inicial

- Incluir unas 20 obras de ballet clásico reconocidas.
- Poder abrir y consultar el catálogo sin conexión después de la instalación y la precarga local.
- Mostrar título y los datos que estén realmente disponibles.
- Mostrar el catálogo mediante RecyclerView y responder al clic de cada celda mediante
  lambda/callback.
- Ofrecer búsqueda local por títulos y alias conocidos cuando existan.
- No inventar datos bibliográficos.

### RF-02 — Búsqueda remota (requisitos pendientes de contrato)

- La API propia será la fuente remota de la aplicación y se consumirá mediante Retrofit, una vez
  recibidas y aprobadas sus especificaciones.
- Se desea permitir buscar obras fuera del catálogo local; confirmar en el contrato los recursos y
  operaciones que lo soportan.
- Mostrar estados de carga, resultados vacíos y fallos recuperables. Los estados de autenticación,
  cuotas, rate limiting y errores HTTP se definirán según el contrato.
- No inventar endpoints, campos, límites, atribución, licencias, autenticación ni comportamiento de
  servidor.
- Conservar procedencia e identificadores remotos si el contrato los proporciona.

### RF-03 — Detalle de obra

Mostrar, cuando existan en la fuente:

- Título original y alias/títulos alternativos.
- Descripción o argumento.
- Compositor.
- Coreógrafo.
- Fecha de estreno.
- Otros datos pertinentes, fuente y enlace de procedencia.

Los campos ausentes se omiten o se presentan como no disponibles. No completar con suposiciones.

### RF-04 — Favoritos

- Marcar/desmarcar una obra como favorita.
- Persistir el estado entre sesiones.
- Ofrecer acceso a la lista de favoritos.
- Evitar duplicados.
- Preferentemente almacenar `isFavorite` en la entidad de obra, salvo que la investigación
  justifique una tabla de relación.

### RF-05 — Notas

- Crear, editar y eliminar notas de texto asociadas a una obra.
- Mostrar las notas de una obra en una lista.
- Confirmar la eliminación mediante AlertDialog.
- Las notas se guardan en Room y no caducan.
- Se puede facilitar copiar/seleccionar texto del detalle para crear una nota, pero el MVP puede
  usar un flujo sencillo de copiar/pegar.
- No implementar aún recortes visuales, OCR, anotaciones sobre imágenes ni edición enriquecida.
- No traducir las notas automáticamente. Una acción explícita de traducción podría estudiarse en una
  fase futura.

### RF-06 — Preferencias

- Usar SharedPreferences para guardar al menos un valor persistente requerido por la práctica.
- Valores adecuados: idioma preferido de visualización, modo Original/Traducido/Ambos o última obra
  consultada.
- SharedPreferences no es una base de datos de obras ni una sesión temporal en memoria.
- Al recuperar una preferencia, comprobar que sus valores siguen siendo válidos.

## 3. Navegación y Activities

Como mínimo:

1. `MainActivity`: catálogo, búsqueda y acceso a favoritos.
2. `DetailActivity`: datos de la obra, favorito, selector de idioma de texto y acceso a notas.
3. `NotesActivity`: listado y gestión de notas asociadas a una obra.

Flujo principal: `MainActivity` → `DetailActivity` → `NotesActivity`.

- Pasar `work_id` mediante extra de Intent.
- Recuperar el extra en la Activity de destino y utilizarlo para cargar la obra/notas.
- Manejar un identificador ausente o inválido sin cerrar la aplicación inesperadamente.
- Incluir menú de AppBar.
- Mantener navegación simple, sin introducir Navigation Component salvo que ya esté en el proyecto y
  sea útil.

## 4. Datos y persistencia

### Room / SQLite

Room es la capa de persistencia sobre SQLite.

Entidades iniciales sugeridas:

#### `WorkEntity`

- `id`: identificador local estable.
- `sourceId`: identificador remoto opcional.
- `source`: identificador de la fuente.
- `originalTitle`.
- `originalLanguage`: código BCP-47 o ISO cuando se conozca.
- `descriptionOriginal`: nullable.
- `composer`: nullable.
- `choreographer`: nullable.
- `premiereDate`: nullable.
- `sourceUrl`: nullable.
- `aliases`: formato simple que se pueda consultar o modelado separado si es necesario.
- `isFavorite`.
- `lastFetchedAt`: fecha de la última consulta correcta/actualización del recurso.
- `expiresAt` o política de caducidad equivalente, solo si la estrategia de caché lo necesita.
- `updatedAt` local, si aporta valor.

No incluir campos vacíos o duplicados sin justificación. Separar una entidad de alias solo si las
consultas reales lo requieren.

#### `NoteEntity`

- `id`.
- `workId`, con relación a la obra.
- `text`.
- `createdAt` y `updatedAt` si no complican el MVP.

#### Caché

- Primera opción: utilizar los datos y `lastFetchedAt` de `WorkEntity` para controlar la frescura
  por obra.
- Crear `CacheEntry` solo si se necesitan metadatos de caché para consultas que no representan una
  obra, o si la validación técnica demuestra que simplifica la solución.
- Registrar únicamente consultas exitosas como última consulta correcta.
- No renovar la fecha de éxito tras un error de red.
- Definir caducidad por fuente/recurso, no una caducidad universal inventada.
- Definir manejo de límites, reintentos y errores HTTP después de conocer el contrato de la API
  propia; no presuponer códigos o cabeceras.
- Los favoritos, notas y obras iniciales no se eliminan por caducidad.

### Precarga

- Incorporar aproximadamente 20 obras iniciales de forma idempotente.
- La precarga no debe sobrescribir favoritos ni notas.
- Documentar el origen y la licencia/procedencia de los datos precargados.
- El catálogo inicial debe ser utilizable sin conexión; la búsqueda remota requiere conectividad.

### SharedPreferences

- Guardar una preferencia sencilla, por ejemplo `preferred_display_language` o
  `last_viewed_work_id`.
- No usar SharedPreferences como caché por artículo ni almacenar listas de obras/notas allí.
- No guardar claves API privadas en preferencias compartidas sin protección; preferir configuración
  segura apropiada y no versionada cuando proceda.

## 5. Contrato de API propia y estructura de datos (análisis preliminar)

Se han facilitado y consultado los endpoints base:
- **Base URL:** `https://ballet-api.vercel.app/`
- **Listado:** `GET /api/ballets`
- **Detalle:** `GET /api/ballets/{id}`

### Estructura JSON identificada (catálogo de 60 obras)

Tanto el listado general como el detalle devuelven los mismos campos con tipos consistentes:

```json
{
  "id": 1,                                  // Int (identificador numérico)
  "title": "Swan Lake",                     // String
  "composer": "Pyotr Ilyich Tchaikovsky",   // String
  "original_choreographer": "Julius Reisinger", // String
  "premiere_year": 1877,                    // Int
  "premiere_location": "Bolshoi Theatre, Moscow", // String
  "acts": 4,                                // Int
  "synopsis": "...",                        // String
  "history_and_evolution": "...",           // String
  "characters": [                           // List<String>
    "Odette/Odile",
    "Prince Siegfried"
  ],
  "key_variations": [                       // List<String>
    "White Swan Pas de Deux"
  ],
  "historical_performances": [              // List<Object>
    {
      "year": 1895,                         // Int
      "company": "Imperial Ballet",         // String
      "theater": "Mariinsky Theatre",       // String
      "choreographer": "Marius Petipa and Lev Ivanov", // String
      "relevance": "..."                    // String
    }
  ],
  "prominent_companies": [                  // List<Object>
    {
      "name": "Bolshoi Ballet",             // String
      "country": "Russia",                  // String
      "official_url": "https://bolshoi.ru"  // String
    }
  ],
  "multimedia": {                           // Object
    "cover_image_url": "https://commons.wikimedia.org/...", // String
    "europeana_search_term": "...",         // String
    "youtube_query": "..."                  // String
  }
}
```

*Nota:* En el elemento 54 ("Afternoon of a Faun"), se detectó un campo adicional redundante `acts1: 1` junto con `acts: 1`.

### Aspectos pendientes de revisión con el usuario
- Confirmar si se persiste toda la estructura en Room (entidades anidadas, TypeConverters para listas y objetos complejos, o modelo aplanado para el MVP).
- Revisar si el listado soporta parámetros de búsqueda (`?search=`, `?q=`) o si se trabajará con filtrado local del catálogo obtenido.
- Revisar si existen requisitos de cabeceras o autenticación.
- Alinear el nuevo origen de datos con los requerimientos académicos y el plan general del proyecto.

Principios independientes del proveedor:

1. Preservar el texto introducido por el usuario y el texto original entregado por la fuente.
2. No convertir una traducción automática en título oficial ni deduplicar únicamente por títulos
   traducidos.
3. Guardar identificadores/procedencia solo si la API los define.
4. ML Kit es una función de presentación propuesta, no un mecanismo de consulta remota; no usar
   traducción como requisito de búsqueda hasta decisión explícita.

## 6. Traducción con ML Kit

### Idiomas objetivo

Español, inglés y francés. El idioma de la interfaz y el idioma de presentación del contenido son
conceptos separados.

### Requisitos

- Usar la integración oficial de ML Kit Translation para Android.
- Revisar dependencias existentes antes de añadir otra.
- Gestionar explícitamente disponibilidad y descarga de los modelos.
- No asumir que los tres modelos están instalados por defecto.
- Descargar bajo demanda o proponer descarga anticipada con Wi-Fi; permitir reintentar.
- No bloquear la interfaz mientras se descargan modelos o se traduce texto.
- Cerrar los objetos `Translator` conforme al ciclo de vida recomendado.
- Si falta el modelo o falla la traducción, mostrar el original y permitir seguir utilizando la
  aplicación.
- Traducir los textos al mostrarlos; no sustituir el texto original almacenado en Room.

### Selector en DetailActivity

Ofrecer tres modos:

- `ORIGINAL`: solo texto de la fuente.
- `TRANSLATED`: texto en el idioma preferido.
- `BOTH`: original y traducción, con etiquetas claras de idioma.

El modo elegido se puede guardar en SharedPreferences. Si el texto ya está en el idioma destino,
evitar traducción redundante. El idioma original debe mostrarse como desconocido si no se conoce,
sin adivinarlo.

### Caché de traducciones

- Evitar traducir de nuevo cada vez que una vista se redibuja o se abre.
- MVP: caché en memoria indexada por texto/identificador y par de idiomas.
- No persistir traducciones en la entidad original salvo que una prueba demuestre que es necesario.
- No traducir nombres propios o títulos oficiales de manera indiscriminada; mantener original
  visible y usar alias de la fuente para búsqueda.
- No traducir automáticamente las notas personales.

## 7. Arquitectura técnica

Arquitectura directa y ligera basada en componentes estándar de Android:

- **UI / Activities (`com.gf.ballet_wiki.activities`):** `MainActivity`, `DetailActivity`, `NotesActivity`. Gestionan el ciclo de vida y la interacción con vistas mediante ViewBinding.
- **Adapters (`com.gf.ballet_wiki.adapters`):** `BalletListAdapter` (usando `ListAdapter` de RecyclerView) y `NotesAdapter`.
- **Data Layer (`com.gf.ballet_wiki.data`):**
  - **`local`:** Base de datos Room (`AppDatabase`), DAO de notas (`NoteDao`), entidad `NoteEntity`.
  - **`remote`:** Cliente Retrofit, interfaz `BalletApiService`, DTOs con anotaciones `@SerializedName` (`BalletItemDto`, `BalletDetailDto`, etc.).
  - **`repository`:** `BalletRepository` que unifica las llamadas a la API remota y el acceso a la base de datos local de notas.
- **Utils (`com.gf.ballet_wiki.utils`):**
  - `SessionManager`: Manejo de SharedPreferences para persistencia de favoritos (`Set<String>`).
  - `TranslationUtils`: Funciones de extensión sobre `TextView` y gestor de traducción ML Kit.

Sin capas de ViewModel ni Domain complejas para mantener el código claro y ajustado a los conocimientos del curso. Las operaciones asíncronas se ejecutan con `lifecycleScope.launch` en el hilo de IO.

## 8. UI y accesibilidad

- Kotlin, XML, Android Views y ViewBinding.
- RecyclerView/ListAdapter cuando corresponda.
- Material Components para campos de texto, si son compatibles con las dependencias existentes.
- Textos visibles en `strings.xml`.
- Colores, dimensiones y estilos en recursos XML.
- Inglés como idioma predeterminado de interfaz, con recursos completos equivalentes en español y
  francés. Aplicar a cadenas actuales y nuevas; requisito pendiente de implementación.
- Estados de carga, vacío, error y modelo de traducción no disponible.
- Interfaz simple, centrada en lectura y consulta. No desarrollar todavía identidad visual avanzada.

## 9. Requisitos de la práctica y trazabilidad

| Requisito de la guía       | Solución prevista                                                        |
|----------------------------|--------------------------------------------------------------------------|
| 3 Activities o más         | MainActivity, DetailActivity, NotesActivity                              |
| Pasar y recuperar un extra | `work_id` en Intent hacia DetailActivity y NotesActivity                 |
| AlertDialog                | Confirmación de eliminación de nota                                      |
| SharedPreferences          | Idioma/modo de visualización o última obra                               |
| SQLite                     | Room sobre SQLite                                                        |
| Retrofit                   | Cliente de API REST propia, tras recibir y aprobar el contrato           |
| RecyclerView + lambda      | Catálogo/lista de notas con callback por celda                           |
| Menú AppBar                | Opciones básicas                                                         |
| Internacionalización       | Inglés predeterminado más español y francés; pendiente de implementación |
| ViewBinding                | Uso obligatorio                                                          |
| TextField Material         | TextInputLayout/TextInputEditText si compatibles                         |

## 10. Fuera de alcance

- Agenda/calendario completo.
- Diseño o implementación del backend/API propia (se integra como servicio externo a la app);
  autenticación de usuario no especificada.
- Sincronización entre dispositivos.
- OCR, recortes visuales de prensa y anotaciones sobre imágenes.
- Traducción automática de notas.
- Relaciones complejas entre compañías, intérpretes y versiones.
- API de pago o cuenta obligatoria sin aprobación explícita.

## 11. Criterios de aceptación

- La aplicación compila y se ejecuta desde Android Studio.
- Las tres Activities funcionan y reciben el identificador correcto.
- Catálogo inicial disponible offline.
- Favoritos y notas sobreviven al reinicio.
- Búsqueda remota real funciona contra la API propia según contrato aprobado y gestiona los errores
  definidos por este.
- Caché evita llamadas innecesarias y respeta la fecha de última consulta correcta.
- La pantalla de detalle permite original/traducido/ambos.
- La traducción no bloquea la UI y falla de forma segura si falta el modelo.
- Se cumplen todos los requisitos de la práctica y el README los explica.
- No hay credenciales, datos inventados ni archivos ajenos a la tarea modificados.
