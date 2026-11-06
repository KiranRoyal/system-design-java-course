# Capstone projects and assessment

Keep synthetic data and personal accounts throughout. Record which features are implemented, tested, measured, or only proposed.

## Project 1 Order support assistant

1. Run the deterministic agent and all tests. Explain every validation boundary.
2. Use the optional local Ollama adapter with an installed model. Record model, prompt, and evaluation dataset versions.
3. Add a policy corpus and compare lexical, vector, and hybrid retrieval on 30 labeled questions.
4. Add bounded per-user memory with expiry and deletion tests.
5. Persist proposals and simulated approval decisions. Prove that changed amounts and another user’s approval are rejected.
6. Add traces, cost estimates, deadlines, and a provider outage demonstration.

Acceptance: zero cross-account disclosures in the adversarial set, bounded calls in every run, supported source IDs for factual claims, and a reproducible evaluation report. Choose and justify a task-success target before testing. A scripted result is not evidence of live-model quality.

## Project 2 Catalog review assistant

Read synthetic product descriptions, flag missing attributes, and propose corrected text in a validated schema. A person approves the exact edit. Measure false positives and missed defects on a labeled set. No direct production catalog writes.

## Project 3 Read only incident assistant

Retrieve synthetic logs and runbooks, summarize observed evidence, distinguish hypotheses from facts, and propose next diagnostic steps. Do not grant shell access. Evaluate unsupported root-cause claims and source correctness.

## Scoring rubric

Score each category from 0 to 5: domain correctness, failure recovery, test evidence, security boundaries, observability, and explanation. Target at least 24 of 30 with no zero in correctness or security. This is a learning rubric, not an external certification.

## Interview routine

Twice a week, explain one design aloud without notes. Ask a partner to introduce a timeout, duplicate request, concurrent update, or malicious input. Rewrite your weakest answer and prove the revised behavior with a test or measurement. Keep a short decision log describing alternatives and tradeoffs.
