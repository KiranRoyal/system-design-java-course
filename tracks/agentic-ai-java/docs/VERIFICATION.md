# Verification record

Prepared and checked on 6 November 2026 on macOS ARM64 with Java 21.0.11 and Maven. Dependencies were resolved from public repositories using empty course-specific Maven settings.

## Executed checks

- Agent core: 16 JUnit tests passed, zero failures or skips.
- Optional Spring AI 2.0.2 / Boot 4.0.9 adapter: Maven package succeeded.
- The core adapter tests use actual local HTTP fixtures for a valid model response and a 503 failure.
- No real language model was installed or queried. Live model answer quality, tool selection, and embedding quality remain for the learner to evaluate.

## Document and media checks

- Word guide rendered and every page visually inspected: 21 pages.
- 20 MP4 files decoded fully without FFmpeg errors; total decoded duration approximately 38.3 minutes.
- Audio present in every file; PCM level analysis found zero clipped samples. Audio is normalized synthesized English narration, not a human recording.
- Captions and full transcripts are included for every lecture. Sentence boundaries are measured; timing inside a long sentence is proportional.
- Video slides were checked for layout and representative slides inspected visually. All lectures have five scenes.

Detailed local evidence is in the adjacent evidence directory. The tests establish the stated teaching behaviors; they do not certify the extensions or production readiness.
