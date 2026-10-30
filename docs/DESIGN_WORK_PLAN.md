# Plan de Diseño e Implementación UI: MainActivity (Ballet Catalog)

## Visión General y Alcance
Este documento contiene la especificación completa del sistema de diseño (extraída de Figma Node `1:3` — *Color Palette & Asset Design System Board* y Node `1:347` — *Ballet Catalog — Dark Mode*), la arquitectura de navegación implementada con Shell de Fragments y persistencia de selección, y el protocolo de verificación visual.

---

## 1. Matriz Consolidada del Sistema de Diseño (Design Tokens & M3)

A partir del tablero de especificaciones de Figma (Nodo `1:3`), se documenta la matriz de tokens de Material 3 para los modos Claro y Oscuro (Obsidian Théâtrale):

### Matriz de Tokens M3

| Token Semántico | Modo Oscuro (Obsidian) | Mapeo de Rol y Componente UI |
| :--- | :--- | :--- |
| `ballet_window_background` | `#121113` | Fondo principal de la ventana y raíz del canvas. |
| `ballet_surface_container_lowest` | `#121114` | Fondo de límites de tarjetas y contenedores base. |
| `ballet_surface_container` | `#1C1A1E` | Superficie de tarjetas `MaterialCardView`, Toolbar y `BottomNavigationView`. |
| `ballet_surface_variant` | `#2A262D` | Fondo de campos de búsqueda inactivos y chips de filtro. |
| `ballet_text_high_emphasis` | `#F8F3ED` | Títulos principales de ballets (fuente Serif Bodoni Moda, 100% contraste). |
| `ballet_text_medium_emphasis` | `#B8B0B5` | Nombres de compositores, fechas de estreno, actos y subtítulos. |
| `ballet_gold_primary` | `#E2A96B` | Iconos de pestañas activas, acentos de actos y botones flotantes. |
| `ballet_rose_accent` | `#E88096` | Corazones de favoritos y acentos de tutú. |
| `ballet_stroke_outline` | `#38323C` | Bordes de tarjetas (stroke 1dp), divisores y contornos de píldora. |

---

### Sistema Tipográfico (Serif & Sans con .TTF locales)

| Estilo de Escala | Fuente Asignada | Aplicación en la UI |
| :--- | :--- | :--- |
| **TitleMediumSerif** | `@font/bodoni_moda` (15sp, Bold) | Títulos de tarjetas de ballets. |
| **BodyMedium** | `@font/plus_jakarta_sans` (11sp - 14sp) | Compositores, sinopsis, buscadores y metadatos. |

---

## 2. Inventario de Vectores y Assets

| Nombre del Asset XML | Descripción y Rol en la UI |
| :--- | :--- |
| `ic_ballet_logo.xml` | **[PROTEGIDO / NO TOCAR]** Logo de marca administrado exclusivamente por el usuario. |
| `ic_profile.xml` | Icono de usuario dentro del botón de perfil. |
| `ic_search.xml` | Lupa del campo de búsqueda estilo píldora. |
| `ic_filter_book.xml` | Icono de libro para la pestaña de filtro "Tous". |
| `ic_filter_heart.xml` | Icono de corazón para la pestaña de filtro "Favoris". |
| `ic_arrow_right.xml` | Flecha indicadora de navegación hacia la pantalla de detalle. |
| `ic_nav_repertoire.xml` | Icono para la pestaña "Répertoire" de la barra inferior. |
| `ic_nav_notes.xml` | Icono para la pestaña "Notes" de la barra inferior. |

---

## 3. Arquitectura Implementada: Shell con FragmentContainerView (Opción A)

Se implementó con éxito la arquitectura de **Shell con Fragments** (`MainActivity` conteniendo `CatalogFragment` y `NotesFragment` junto con un `BottomNavigationView` persistente de 80dp), garantizando que la barra de navegación inferior permanezca fija y no se recargue ni parpadee al cambiar de vista.
