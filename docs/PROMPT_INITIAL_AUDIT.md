# Preparación del MVP antes de recibir el contrato de API

Este documento sustituye el prompt inicial de investigación de proveedores. La
investigación/implementación descrita en ese prompt queda fuera de alcance. Las llamadas futuras
serán a una API propia y sus especificaciones se facilitarán más adelante.

## Estado actual

- Preparación documental, no ejecución de implementación.
- No se han recibido el contrato, endpoints, autenticación, modelos de datos ni condiciones
  operativas de la API. No deducirlos ni probar servicios terceros como sustituto.
- La documentación funcional expresa objetivos; la disponibilidad de operaciones/campos remotos solo
  puede confirmarse contra el contrato recibido.

## Próxima revisión, cuando se entregue la especificación

1. Leer el contrato y separar hechos documentados, preguntas abiertas y decisiones recomendadas.
2. Confirmar entornos, base URL, operaciones, parámetros, esquemas, errores, autenticación,
   secretos, límites, versionado y reglas de uso.
3. Mapear capacidades confirmadas a catálogo, búsqueda, detalle, Room, frescura/caché y estados de
   UI; señalar incompatibilidades sin inventar soluciones.
4. Proponer ejemplos de prueba/fixtures y pruebas de red autorizadas. No afirmar que funciona sin
   evidencia verificable.
5. Actualizar `SPECIFICATION.md`, `IMPLEMENTATION_PLAN.md` y `REQUIREMENTS_CHECKLIST.md`; presentar
   decisiones técnicas o funcionales no resueltas y detenerse para aprobación.

## Restricciones de preparación

- No modificar código de producción, dependencias ni versiones durante esta preparación.
- Mantener los requisitos de Android y académicos como objetivos sujetos a verificación, no como
  hechos completados.
- Respetar la configuración Gradle/Kotlin/AGP/minSdk existente al llegar a implementación.
- No registrar ni versionar credenciales. No modificar archivos fuera de `docs/`, instrucciones de
  agentes y skills en `.junie` para esta tarea.
