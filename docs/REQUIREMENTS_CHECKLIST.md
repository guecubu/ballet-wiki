# Checklist de requisitos y pruebas

Marcar solo después de comprobarlo; no marcar por haber escrito el código.

Estado de este checklist: requisitos por verificar; no implica implementación. El repositorio tiene
un prototipo anterior de Wikidata, que no valida la API propia. Esta aún no tiene especificación
disponible; no completar su apartado hasta revisar y aprobar el contrato.

## Práctica académica

- [ ] Al menos tres Activities funcionales.
- [ ] Extra de Intent transmitido, recuperado y usado.
- [ ] AlertDialog activado por una acción del usuario.
- [ ] Valor persistente mediante SharedPreferences.
- [ ] Room sobre SQLite con datos relevantes.
- [ ] Llamada REST real mediante Retrofit.
- [ ] RecyclerView con evento de clic por celda implementado mediante lambda/callback.
- [ ] Menú de AppBar.
- [ ] Textos externalizados y recursos en español.
- [ ] ViewBinding; sin `findViewById` en código de UI.
- [ ] Campo de texto Material cuando sea compatible con las dependencias existentes.

## Funcionalidad

- [ ] Catálogo inicial disponible sin red.
- [ ] Precarga idempotente.
- [ ] Búsqueda local.
- [ ] Búsqueda remota con API propia, según contrato recibido y aprobado (bloqueado: contrato
  pendiente).
- [ ] Estados de carga, vacío y error.
- [ ] Favoritos persistentes.
- [ ] Crear/editar/eliminar notas.
- [ ] Notas asociadas a la obra correcta.
- [ ] Fecha de última consulta solo tras éxito.
- [ ] No repetir llamadas cuando el dato está suficientemente reciente.
- [ ] Manejo de errores, timeout, límites y reintentos conforme al contrato de API; falta de
  conexión recuperable.
- [ ] Procedencia de los datos conservada cuando esté disponible.

## ML Kit

- [ ] Modelos descargables y estado comprobado.
- [ ] Idiomas español, inglés y francés soportados en configuración.
- [ ] Selector Original / Traducido / Ambos.
- [ ] Texto original nunca sobrescrito.
- [ ] Traducción asíncrona.
- [ ] Caché en memoria de traducciones.
- [ ] Fallback si falta modelo o falla la traducción.
- [ ] Notas no traducidas automáticamente.
- [ ] El texto original se preserva en cualquier búsqueda; ML Kit no se usa para consulta remota
  salvo decisión explícita.

## Entrega

- [ ] Compilación limpia.
- [ ] Pruebas relevantes ejecutadas.
- [ ] README con configuración, contrato/API propia y límites confirmados.
- [ ] Tabla de cumplimiento de la práctica en README.
- [ ] Sin claves/secretos versionados.
- [ ] Sin archivos ajenos al alcance modificados.
