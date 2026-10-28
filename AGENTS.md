# Instrucciones permanentes para Junie

## Misión

Desarrollar un MVP Android llamado provisionalmente Ballet Reference: catálogo de obras de ballet
clásico, búsqueda, detalle, favoritos, notas personales y visualización de texto original/traducido.

## Restricciones no negociables

- Usar Kotlin, Android Views, XML y ViewBinding.
- No usar Jetpack Compose.
- Mantener el proyecto compatible con su configuración actual de Gradle, Kotlin, AGP y minSdk.
  Inspeccionar versiones antes de proponer cambios.
- No añadir dependencias ni actualizar versiones sin justificarlo y pedir aprobación.
- No inventar APIs, endpoints, propiedades, datos de obras ni condiciones de uso.
- No modificar archivos fuera del alcance de la tarea.
- No crear capas, clases, interfaces o recursos XML sin una necesidad demostrable.
- No introducir credenciales en el código ni en Git. Si una fuente requiere clave, documentar el
  procedimiento de configuración y usar configuración local no versionada cuando corresponda.
- Los textos visibles deben residir en recursos `strings.xml`; colores, dimensiones y estilos deben
  usar recursos XML.
- Mantener los textos originales recibidos de la fuente. La traducción de ML Kit es una
  representación de presentación, no una sustitución del dato original.
- Mantener notas del usuario sin traducir automáticamente ni alterar su contenido.
- Toda operación de red o persistencia debe evitar bloquear el hilo principal.
- Tratar errores de red, ausencia de resultados y modelos de traducción no descargados como estados
  normales y recuperables.

## Arquitectura objetivo

MVVM sencilla con separación razonable entre UI, ViewModel, repositorio, Room/DAO y cliente Retrofit
de la API propia, una vez recibido y aprobado su contrato. No aplicar abstracciones ceremoniales.
Las decisiones finales deben respetar el proyecto existente. No asumir endpoints, autenticación,
campos, límites ni errores antes de recibir las especificaciones.

## Persistencia

- Room sobre SQLite: obras, favoritos, notas y metadatos de consulta/caché.
- SharedPreferences: preferencias sencillas y al menos un valor persistente requerido por la
  práctica, por ejemplo `last_viewed_work_id` o idioma de visualización.
- No guardar listas de obras ni notas en SharedPreferences.
- No confundir preferencia persistente con sesión temporal en memoria.
- Los datos personales no deben caducar automáticamente.

## Requisitos académicos obligatorios

- Al menos tres Activities funcionales.
- Pasar un extra de Intent entre Activities, recuperarlo y utilizarlo.
- Al menos un AlertDialog en respuesta a una acción del usuario.
- Guardar al menos un valor con SharedPreferences.
- Tabla (s) SQLite gestionadas mediante Room.
- Llamadas REST mediante Retrofit.
- RecyclerView con al menos un evento de clic por celda implementado con lambda/callback.
- Mejoras: menú de AppBar, recursos localizados (español como idioma inicial) y ViewBinding.
- Usar componentes de texto Material cuando sean compatibles con el proyecto y aporten valor.

## Control del trabajo

- Trabajar por fases y tareas pequeñas.
- Antes de una fase con cambios relevantes, explicar el alcance y los archivos previstos.
- Si una decisión funcional o técnica importante no está resuelta, presentar opciones y detenerse
  para aprobación.
- No considerar una tarea terminada hasta compilar y realizar las pruebas aplicables.
- Al final de cada fase informar: archivos cambiados, decisiones, pruebas ejecutadas y resultados,
  pendientes y riesgos.
- No afirmar que una API funciona sin haber ejecutado una prueba real o aportado evidencia
  verificable.
- La integración remota está bloqueada mientras no se faciliten y aprueben las especificaciones de
  la API propia; no sustituirlas por investigación de proveedores externos ni por endpoints
  imaginados.
- No borrar ni sobrescribir datos de usuario durante precarga o actualización de catálogo.
- Mantener `docs/REQUIREMENTS_CHECKLIST.md` actualizado cuando se valide un requisito.
