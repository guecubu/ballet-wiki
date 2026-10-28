---
name: ballet-api-research
description: Review and validate the user's own API contract for Ballet Reference before implementing a Retrofit client. Use when assessing documented endpoints, schemas, authentication, limits, or test evidence.
---

# Ballet Reference API contract review

1. Read `docs/SPECIFICATION.md` and the user's supplied API documentation. Do not search for or
   recommend third-party providers as substitutes: the intended remote service is the user's own
   API.
2. If the API specification has not been supplied, report that API research/integration is blocked
   and list the contract information needed. Do not invent base URLs, routes, fields,
   authentication, rate limits, or terms.
3. Once supplied, distinguish documented facts, open questions, and recommendations. Record
   endpoints, parameters, schemas, identifiers, language/provenance, errors, authentication,
   environments, limits, and operational constraints only as evidenced by the contract.
4. Map confirmed capabilities to product requirements; identify unsupported or ambiguous
   requirements rather than implying they are available.
5. Propose tests grounded in documented examples or fixtures, and real requests only when explicitly
   authorized and safely configured. Do not claim the API works without verifiable execution
   evidence.
6. Keep credentials out of source control and document approved local configuration without exposing
   secret values.
7. Provide a concise contract-to-requirements matrix, unresolved questions, risks, and a
   recommendation. Do not implement or add dependencies during specification review unless
   explicitly authorized.
