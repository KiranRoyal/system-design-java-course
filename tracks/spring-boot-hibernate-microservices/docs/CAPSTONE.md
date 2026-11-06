# Capstone projects and assessment

Keep synthetic data and personal accounts throughout. Record which features are implemented, tested, measured, or only proposed.

## Project 1 Commerce services

1. Build and run inventory and orders. Pass unit, persistence, and real HTTP smoke tests.
2. Add order lines, mapping tests, and query-count measurements.
3. Run both services with separate PostgreSQL databases; test fresh install and schema upgrades.
4. Add resource-server authentication, ownership checks, and account-scoped idempotency.
5. Implement an outbox relay and durable deduplicating consumer. Demonstrate duplicate delivery after a relay crash.
6. Add a synthetic payment provider, compensation, and a reconciliation worker with a durable state machine.
7. Add metrics, traces, container packaging, and a documented load experiment.

Acceptance: no overselling, one logical order per idempotency key and request, no cross-account access, recoverable unknown outcomes, and durable event recovery demonstrated under the stated failure model.

## Project 2 Appointment booking

Model providers, appointment slots, and bookings. Prevent two users from booking the same slot using a database constraint or appropriate concurrency control. Add cancellation and an asynchronous reminder with outbox delivery.

## Project 3 Subscription billing simulator

Use synthetic payments only. Model invoice creation, retryable collection attempts, provider idempotency, and reconciliation after timeouts. Explain why an unknown payment outcome must not immediately trigger another charge with a new key.

## Scoring rubric

Score each category from 0 to 5: domain correctness, failure recovery, test evidence, security boundaries, observability, and explanation. Target at least 24 of 30 with no zero in correctness or security. This is a learning rubric, not an external certification.

## Interview routine

Twice a week, explain one design aloud without notes. Ask a partner to introduce a timeout, duplicate request, concurrent update, or malicious input. Rewrite your weakest answer and prove the revised behavior with a test or measurement. Keep a short decision log describing alternatives and tradeoffs.
