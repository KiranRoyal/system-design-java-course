# Verification record

Prepared and checked on 6 November 2026 on macOS ARM64 with Java 21.0.11 and Maven. Dependencies were resolved from public repositories using empty course-specific Maven settings.

## Executed checks

- Commerce projects: 21 JUnit tests passed, zero failures or skips: 3 Java domain tests, 9 inventory tests, and 9 order tests.
- Both actual Spring Boot services started and passed 12 HTTP smoke checks.
- Stateful local Postman inventory mock passed 10 contract checks.
- The actual order service against that mock passed 6 failure/recovery checks, including a delayed reply after reservation and retry without a second stock effect.
- Portable Postman collection: 6 requests and 6 assertions passed locally with cloud reporting disabled. The CLI printed a sign-in notice; no sign-in was required for the local run and no results were published.
- Hibernate 7.2.25.Final and H2 2.4.240 were used. Flyway logged that this H2 version is newer than its last verified H2 version; the supplied migrations and tests nevertheless completed successfully.
- PostgreSQL, security integration, broker delivery, scheduled recovery, and production load were not executed. These are explicitly documented extensions.

## Document and media checks

- Word guide rendered and every page visually inspected: 25 pages.
- 24 MP4 files decoded fully without FFmpeg errors; total decoded duration approximately 44.6 minutes.
- Audio present in every file; PCM level analysis found zero clipped samples. Audio is normalized synthesized English narration, not a human recording.
- Captions and full transcripts are included for every lecture. Sentence boundaries are measured; timing inside a long sentence is proportional.
- Video slides were checked for layout and representative slides inspected visually. All lectures have five scenes.

Detailed local evidence is in the adjacent evidence directory. The tests establish the stated teaching behaviors; they do not certify the extensions or production readiness.
