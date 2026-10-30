# Diseño de Caché Multimedia y Metadatos de Proveedores (Room)

## Propósito

Este documento describe la arquitectura de persistencia local en Room diseñada para almacenar
recursos multimedia (imágenes, videos, audios) y metadatos detallados de proveedores externos
(Europeana y Open Opus), garantizando atribuciones legales, soporte offline y la futura creación de
galerías separadas por tipo en la pantalla de detalle (`DetailActivity`).

---

## 1. Estructura de Base de Datos (Room v2)

### A. Tabla `europeana_records`

Almacena los metadatos completos y legales devueltos por la API de Europeana para asegurar
atribución y derechos de autor.

* **Entidad:** `EuropeanaEntity`
* **DAO:** `EuropeanaDao`
* **Campos principales:**
    * `europeana_id` (Primary Key): Identificador único del registro en Europeana.
    * `work_id`: Clave lógica asociada a la obra de ballet.
    * `title`: Título del recurso.
    * `creator`: Autor o creador.
    * `data_provider`: Institución proveedora (ej. *Deutsche Fotothek*).
    * `rights_statement`: Declaración o enlace de derechos de autor.
    * `is_shown_at_url`: Enlace web a la página original del museo/institución.
    * `is_shown_by_url`: Enlace directo al archivo multimedia en alta resolución.
    * `preview_url`: URL de la miniatura.
    * `media_type`: Tipo de medio (`IMAGE`, `VIDEO`, etc.).

### B. Tabla `composer_records`

Almacena la información biográfica y visual del compositor obtenida de Open Opus, incluyendo su
retrato.

* **Entidad:** `ComposerEntity`
* **DAO:** `ComposerDao`
* **Campos principales:**
    * `composer_id` (Primary Key): ID de Open Opus.
    * `name` / `complete_name`: Nombres del compositor.
    * `birth` / `death`: Fechas vitales.
    * `epoch`: Época musical (ej. *Romantic*).
    * `portrait`: URL al retrato oficial del compositor (`https://assets.openopus.org/...`).

### C. Tabla `multimedia_items`

Tabla centralizada y normalizada para alimentar las galerías independientes (Imágenes, Videos,
Audio) en la interfaz de detalle.

* **Entidad:** `MultimediaEntity`
* **DAO:** `MultimediaDao`
* **Campos principales:**
    * `id` (Primary Key Auto-generate): Identificador interno.
    * `work_id`: Obra asociada.
    * `media_type`: Categoría (`IMAGE`, `VIDEO`, `AUDIO`).
    * `title`: Título visible en la galería.
    * `media_url`: URL del recurso multimedia.
    * `thumbnail_url`: Miniatura opcional.
    * `provider_ref_id`: Referencia cruzada opcional al ID de Europeana o proveedor origen para
      consultar atribuciones y derechos.

---

## 2. Configuración en `AppDatabase`

* Versión de la base de datos: `2`
* Estrategia de migración: `.fallbackToDestructiveMigration()`
* Nuevos DAOs expuestos: `europeanaDao()`, `composerDao()`, `multimediaDao()`.

---

## 3. Plan de Uso Futuro

1. **Repositorio (`BalletRepository`)**: Poblar estas tablas al consultar los endpoints remotos,
   evitando llamadas repetitivas y permitiendo navegación offline.
2. **UI (`DetailActivity`)**: Consultar `getMediaByWorkAndType(workId, "IMAGE")`,
   `getMediaByWorkAndType(workId, "VIDEO")`, etc., para mostrar galerías organizadas por pestañas o
   secciones con sus respectivas atribuciones legales.
3. **Sección de Compositor**: Cargar el retrato del compositor usando `ComposerEntity.portrait` con
   Coil.
