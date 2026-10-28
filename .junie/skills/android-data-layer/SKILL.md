---
name: android-data-layer
description: Design or modify Room/SQLite persistence, repositories, Retrofit, and per-resource caching for Ballet Reference.
---

# Android data layer

1. Inspect existing entities, DAOs, database, repositories, and dependencies before editing.
2. Use Retrofit only for the API selected and approved for the project. For Ballet Reference, the
   intended remote service is the user's own API; if its contract has not been provided, stop
   API-specific implementation and request/await its specification.
3. Room is the local source of truth for works, favorites, and notes. SharedPreferences is reserved
   for simple preferences and academic requirements.
4. Maintain a stable local identifier and retain remote identifiers/provenance only when the
   approved contract defines them.
5. Store the original text and its known language; never replace the original with automated
   translation.
6. Work preloading must be idempotent and must never overwrite favorites or notes.
7. Record `lastFetchedAt` only after a successful response. Errors do not renew the success
   timestamp.
8. Do not create a generic cache table if `WorkEntity` fields suffice; justify the need before
   adding one.
9. Define freshness per resource based on the approved contract or documented decisions; do not
   invent a universal expiration.
10. Use separate DTOs when the approved remote structure differs from the local model. Do not invent
    endpoints, properties, response examples, or authentication behavior.
11. Handle lack of network, timeout, empty results, partial data, and contract-defined
    HTTP/rate-limit errors; do not assume a particular status or retry policy.
12. No network or database operation must block the main thread.
13. Add relevant tests and build after structural changes.
