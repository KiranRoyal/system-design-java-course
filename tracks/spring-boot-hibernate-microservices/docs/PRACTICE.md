# Practice workbook

Attempt each task before reading the model answer. Implementation exercises require runnable code and evidence; a written answer alone is not completion.

## 01 Java syntax values and methods

**Task**
Implement subtotal, tax, and total for a basket. Choose a rounding rule and test zero items, a fractional tax, a large quantity, and invalid input. Compile with Java 21.

**Model answer and acceptance criteria**
Keep arithmetic separate from printing and HTTP. State when rounding occurs, such as once per invoice or once per line, because the totals can differ. Test expected decimal values rather than formatted strings alone.

## 02 Objects interfaces records and exceptions

**Task**
Implement fixed and percentage discount strategies. Add a maximum discount rule and tests that a discount cannot make the total negative. Explain why a request DTO should not be the same object as a persistence entity.

**Model answer and acceptance criteria**
Inject the selected strategy into a calculation service. DTOs expose only the intended public contract; entities represent database state and may include internal fields, relationships, and mutable lifecycle. Keeping them separate prevents accidental API changes when mappings change.

## 03 Collections generics streams and equality

**Task**
Build a report of the three highest-selling SKUs. Define tie ordering, null handling, and whether returned collections may be modified. Compare a loop implementation with a stream implementation for readability.

**Model answer and acceptance criteria**
Use deterministic sorting, such as descending quantity followed by SKU. Reject invalid domain data early instead of scattering null checks through the pipeline. Return a copied or immutable collection when callers should not mutate the report.

## 04 Concurrency virtual threads and the JVM

**Task**
Run 100 blocking read tasks with a semaphore allowing ten outbound calls. Record peak active calls and completion time. Add cancellation and ensure interrupted tasks restore or propagate interruption.

**Model answer and acceptance criteria**
The semaphore limits dependency pressure while virtual threads make waiting economical. A test should verify the peak active count, not only completion. Do not hold a database transaction while waiting for unrelated remote work.

## 05 HTTP REST and API contracts

**Task**
Write an API contract for cancelling an order, including already-cancelled, already-shipped, unknown-order, and unauthorized cases. Decide which responses may safely be retried.

**Model answer and acceptance criteria**
Make cancellation idempotent for the same intent, validate ownership, and return a conflict when shipment makes cancellation impossible. Define whether inaccessible and absent resources share a 404 response. A client should preserve the original logical request identity on retries.

## 06 Spring beans dependency injection and proxies

**Task**
Draw the bean graph for both services. Write a unit test that constructs a pure domain service directly. Demonstrate why calling this.transactionalMethod() differs from calling a separate proxied bean.

**Model answer and acceptance criteria**
Constructor injection reveals the graph and prevents partially initialized required dependencies. Verify transaction activity or rollback behavior in the proxy demonstration rather than relying on the presence of an annotation. Keep infrastructure details out of pure calculations.

## 07 Spring Boot configuration and application startup

**Task**
Run a service with a different port supplied on the command line. Then intentionally provide an invalid database URL and inspect the startup failure. Explain which settings should vary between development and production.

**Model answer and acceptance criteria**
Ports, database endpoints, credentials, timeouts, and observability destinations vary by environment. Business invariants should not silently change through profiles. Keep a documented configuration contract and avoid copying production credentials into a local properties file.

## 08 REST controllers DTOs and pagination

**Task**
Add GET /products/{id} and a product search endpoint. Return explicit DTOs, validate pagination limits, and test the empty page and unknown product cases over HTTP.

**Model answer and acceptance criteria**
Map an unknown product to the documented 404 response. Keep search inputs parameterized through repository APIs. Use a deterministic sort with a tie-breaker and state whether the endpoint returns a total count or only a next-page indicator.

## 09 Validation error handling and problem details

**Task**
Send malformed JSON, quantity zero, an unknown product, and a repeated key with a different quantity. Write down the expected status and verify it with the provided HTTP smoke test or Postman collection.

**Model answer and acceptance criteria**
Malformed or invalid input should return 400, unknown product 404, and a conflicting key 409. Confirm that failed requests do not alter stock or create orders. Avoid a catch-all handler that converts every programming defect into a misleading client error.

## 10 Testing from domain rules to HTTP boundaries

**Task**
Break one invariant deliberately: remove the stock lock or change timeout handling from PENDING to REJECTED. Identify which existing test fails and add a missing test only if the behavior is not covered.

**Model answer and acceptance criteria**
Tests should fail for the behavior they are intended to protect. Avoid duplicating implementation line by line. H2 success is useful but does not establish PostgreSQL lock timing, query plans, or dialect compatibility; run the target database before making those claims.

## 11 SQL schema design indexes and transactions

**Task**
Design an index for finding a user’s newest orders and explain the ordering of columns. Write a transaction that updates stock and inserts a reservation, then force the insert to fail and verify rollback.

**Model answer and acceptance criteria**
A candidate index begins with the equality-filtered user or tenant columns and continues with the time and stable tie-breaker used for ordering. Verify the plan with realistic cardinality. The failed reservation insert must leave stock unchanged because both changes share one transaction.

## 12 JPA Hibernate entity lifecycle and dirty checking

**Task**
Load an entity inside a transaction, modify it, flush, then roll back. Repeat with an entity loaded outside the transaction and explain the difference. Observe SQL with logging enabled only for the local lab.

**Model answer and acceptance criteria**
A managed change is synchronized on flush but remains subject to rollback. A detached object’s changes are not tracked until explicitly reattached through an appropriate operation. Do not equate save, flush, and commit; they have different meanings.

## 13 Entity relationships ownership and aggregates

**Task**
Implement OrderLine with quantity and price snapshot. Add a helper that sets both order.lines and line.order. Test adding a line, removing a line, and deleting an order without deleting any catalog product.

**Model answer and acceptance criteria**
Use aggregate ownership for cascade decisions. A line belongs to one order and may be deleted with it; a product is shared and independently managed. Add migrations and test foreign-key behavior. Do not expose the bidirectional graph directly as JSON.

## 14 Fetching N plus one queries and performance

**Task**
Create fifty orders with several lines each. Record SQL count for a lazy traversal, a DTO projection, and a bounded batch approach. Compare transferred rows and latency as well as query count.

**Model answer and acceptance criteria**
One enormous join is not automatically better than several bounded queries. Prevent accidental lazy access after the transaction ends by assembling the DTO inside the intended boundary. Verify the chosen plan on the target database and realistic data size.

## 15 Optimistic locking pessimistic locking and stock

**Task**
Replace row locking with a conditional update and compare the implementations under contention. Add a test for two different products and a reused reservation key. Explain how deadlocks should be retried.

**Model answer and acceptance criteria**
Check the affected-row count and keep reservation insertion in the same transaction. Retrying a deadlock must restart the whole transaction with bounded attempts. Reusing a key with different request data must conflict rather than consume stock again.

## 16 Migrations PostgreSQL and data evolution

**Task**
Add a nullable customer reference to orders, backfill synthetic rows, then make it required in a later migration. Test both a fresh database and an upgrade from the previous schema.

**Model answer and acceptance criteria**
A fresh-install test alone misses upgrade failures. Preserve old migration checksums, test against production-like data volume, and plan rollback or forward repair. A destructive migration may not be reversible by simply redeploying old Java code.

## 17 Service boundaries and domain ownership

**Task**
Design a commerce boundary map including catalog, inventory, orders, payment, and shipping. Identify which capabilities could remain modules in one application and which need independent scaling or ownership.

**Model answer and acceptance criteria**
Use concrete reasons for each split: independent release cadence, different availability needs, data ownership, or measured scaling pressure. Document the added failure modes and communication contracts. Do not create a service merely because a domain noun exists.

## 18 HTTP clients timeouts retries and resilience

**Task**
Run the inventory mock in success, outage, and delayed scenarios. Start orders against that endpoint. Verify that a timeout produces PENDING and that retrying after recovery confirms the same order.

**Model answer and acceptance criteria**
Preserve the idempotency key and order ID across attempts. Do not translate a timeout into insufficient stock. Measure the elapsed time and verify that the HTTP client’s timeout is active. The mock demonstrates caller behavior; the real inventory smoke test verifies the normal integration.

## 19 Idempotency and concurrent request handling

**Task**
Launch twenty concurrent requests using one checkout key, then repeat with different quantities. Verify one order, one reservation, and a conflict for mismatched intent. Design a key-retention policy.

**Model answer and acceptance criteria**
Count durable business effects rather than HTTP responses alone. A policy should cover the maximum expected retry window and any legal or business retention requirements. Hash a canonical request representation if the request grows beyond a few fields, and version the canonicalization rule.

## 20 Transactional outbox messaging and consumers

**Task**
Implement a relay with bounded batches and a deduplicating consumer. Inject a crash after publish but before acknowledgement storage. Restart and demonstrate that the event may repeat while the consumer’s business effect remains single.

**Model answer and acceptance criteria**
Use stable event IDs and durable consumer deduplication. If the consumer calls an external provider, local deduplication alone is insufficient; use provider idempotency and reconciliation. State the exact effect and failure model before using the phrase exactly once.

## 21 Sagas compensation and workflow recovery

**Task**
Extend the state machine with payment authorization and stock release using synthetic providers. Create a failure matrix covering timeouts before and after each remote effect and crashes before each local state update.

**Model answer and acceptance criteria**
For each row, specify how to discover the actual outcome and which idempotency key to reuse. Persist compensation attempts and alert on exhausted recovery. A manual-review state is valid when automatic resolution cannot safely determine the next action.

## 22 Authentication authorization and service security

**Task**
Add a secured profile using a local identity provider or test JWT decoder. Test missing token, wrong issuer, wrong audience, expired token, insufficient role, and another user’s order. Keep the default local demo limitations documented.

**Model answer and acceptance criteria**
Use Spring Security’s resource-server support and explicit authorization rules. Derive tenant identity from validated claims mapped by trusted configuration. Test resource ownership independently of role membership. Do not commit credentials or signing keys to the course repository.

## 23 Observability containers and production delivery

**Task**
Create a container image, run each service with a separate PostgreSQL database, and load-test a fixed workload. Record hardware, dataset size, concurrency, duration, throughput, error rate, and latency percentiles.

**Model answer and acceptance criteria**
Compare measurements under the same conditions and avoid claiming production capacity from a laptop test. Include dependency and database limits in the explanation. Verify restore procedures and schema compatibility as part of recovery, not only that a backup file exists.

## 24 Commerce capstone and interview practice

**Task**
Run a 45-minute mock interview: five minutes requirements, ten architecture and schema, ten concurrency and transactions, ten distributed failures, and ten security, operations, and tradeoffs.

**Model answer and acceptance criteria**
Score correctness, clarity, data ownership, failure handling, and evidence from one to five. Any answer claiming that a timeout proves failure or that an outbox prevents all duplicates needs revision. Use a runnable experiment to resolve your weakest assumption.
