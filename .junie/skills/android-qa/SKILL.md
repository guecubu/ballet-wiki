---
name: android-qa
description: Verify academic requirements, build status, and functional flows of the Android MVP before concluding a phase or delivering.
---

# Android QA

1. Read `docs/REQUIREMENTS_CHECKLIST.md` and the academic guide.
2. Mark a requirement as complete only after verifying it; distinguish executed tests from pending
   tests.
3. Run the appropriate Gradle build according to the existing project and report the command and
   result.
4. Verify all three Activities, Intent extra, AlertDialog, SharedPreferences, Room/SQLite, Retrofit
   against the approved API contract, and RecyclerView with callback/lambda.
5. Verify persistence of favorites/notes after closing and reopening the app.
6. Test offline mode and ensure network errors do not prevent opening local data.
7. Test caching: recent resources do not trigger unnecessary requests; network errors do not update
   `lastFetchedAt`; verify rate limits, retries, and HTTP errors as defined by the approved API
   contract.
8. Test ML Kit with available and unavailable models; switching between Original/Translated/Both;
   translation failures; language changes; unblocked UI.
9. Check that original text is not overwritten, notes are not automatically translated, and no API
   keys are stored in the repository.
10. Update checklist and README only with verified results.
11. Do not claim the remote integration is validated until tests use the approved API contract and
    provide real or otherwise verifiable evidence. If the contract is unavailable, record remote QA
    as blocked rather than infer behavior.
12. Report defects and do not declare delivery ready while critical bugs exist.
