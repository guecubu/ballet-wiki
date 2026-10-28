---
name: android-views-ui
description: Implement or review the native Ballet Reference UI using Activities, XML, ViewBinding, RecyclerView, and Material Components.
---

# Android Views UI

1. Use Kotlin, Android Views, XML, and ViewBinding. Do not use Jetpack Compose.
2. Inspect existing layouts, themes, strings, and dependencies before creating files.
3. Target Activities: MainActivity, DetailActivity, and NotesActivity.
4. Pass `work_id` via Intent, retrieve it, and validate its presence.
5. Use RecyclerView/ListAdapter as appropriate and explicit callbacks/lambdas for row clicks.
6. Include loading, empty, error, and unavailable data states.
7. Add an AlertDialog to confirm note deletion.
8. Include an AppBar menu and text resources in `strings.xml`.
9. Use Material `TextInputLayout`/`TextInputEditText` only if existing dependencies are compatible.
10. The detail view selector supports Original, Translated, and Both; clearly display languages and
    always preserve the original.
11. Do not automatically translate notes or proper nouns.
12. Do not add redundant layouts/classes. Reuse existing components when reasonable.
