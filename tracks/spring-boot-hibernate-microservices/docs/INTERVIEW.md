# Interview questions with model answers

Answer in your own words and connect each answer to a test or failure scenario. These are practice prompts, not a prediction of a particular company’s interview.

## 01 Why not use double for exact currency arithmetic?

Topic: 01 Java syntax values and methods

Many decimal amounts cannot be represented exactly in binary floating-point; use minor units or decimal arithmetic with a stated rounding rule.

## 02 What is the difference between a compile error and an exception?

Topic: 01 Java syntax values and methods

A compile error prevents valid bytecode generation; an exception occurs while a compiled program is executing.

## 03 What is dependency inversion?

Topic: 02 Objects interfaces records and exceptions

High-level policy depends on an abstraction rather than a concrete infrastructure detail.

## 04 When should you use a record?

Topic: 02 Objects interfaces records and exceptions

For a value-like data carrier whose shallow immutability and generated equality fit the intended semantics.

## 05 Why must equals and hashCode agree?

Topic: 03 Collections generics streams and equality

Equal objects must have equal hash codes so hash-based lookup can find an equal key.

## 06 When is a loop preferable to a stream?

Topic: 03 Collections generics streams and equality

When early exits, complex branching, or explicit state make the loop easier to understand and verify.

## 07 Do virtual threads prevent races?

Topic: 04 Concurrency virtual threads and the JVM

No. Shared-state correctness still requires appropriate synchronization or transactional coordination.

## 08 Why can a service be slow with low CPU?

Topic: 04 Concurrency virtual threads and the JVM

It may be waiting on connections, locks, queues, disk, or downstream calls rather than executing CPU work.

## 09 Is HTTP 200 proof that the business task succeeded?

Topic: 05 HTTP REST and API contracts

Only if the documented response semantics say so; a response may contain a pending or rejected business state.

## 10 What does a client timeout imply?

Topic: 05 HTTP REST and API contracts

The result is unknown. The server may have committed before the connection failed.

## 11 Does a singleton bean make its fields thread-safe?

Topic: 06 Spring beans dependency injection and proxies

No. Concurrent requests may call the same instance, so mutable fields need appropriate coordination or should be avoided.

## 12 Why can self-invocation bypass a transaction annotation?

Topic: 06 Spring beans dependency injection and proxies

The call stays inside the target object and does not pass through the transaction proxy.

## 13 What does a starter provide?

Topic: 07 Spring Boot configuration and application startup

A coordinated dependency set and configuration integration for a capability such as web MVC or JPA.

## 14 Why pin a compatible version family?

Topic: 07 Spring Boot configuration and application startup

It reduces untested combinations of Spring, Hibernate, Jackson, and other managed dependencies.

## 15 Why avoid returning JPA entities from controllers?

Topic: 08 REST controllers DTOs and pagination

It couples the public API to persistence structure and can expose internal fields or trigger unexpected lazy loads.

## 16 What is keyset pagination?

Topic: 08 REST controllers DTOs and pagination

Fetching rows after a stable ordered cursor, such as a last-seen ID, instead of skipping a growing offset.

## 17 Why validate again in a service?

Topic: 09 Validation error handling and problem details

Services may be invoked by jobs, tests, or other code that bypasses the HTTP validator.

## 18 Why keep database constraints?

Topic: 09 Validation error handling and problem details

They enforce invariants at the durable shared boundary even when concurrent callers or another application bypass application checks.

## 19 What does an integration test add over a unit test?

Topic: 10 Testing from domain rules to HTTP boundaries

Evidence that multiple real components cooperate through their actual boundaries and configuration.

## 20 Why not test everything end to end?

Topic: 10 Testing from domain rules to HTTP boundaries

Such tests are slower and less diagnostic; use focused tests for most rules and a few complete-path checks.

## 21 Does a transaction automatically prevent overselling?

Topic: 11 SQL schema design indexes and transactions

No. The isolation level and read-write pattern must enforce the stock invariant under concurrency.

## 22 Why can an unused index be harmful?

Topic: 11 SQL schema design indexes and transactions

It consumes space and increases insert, update, delete, and maintenance work without helping the workload.

## 23 What is the first-level cache?

Topic: 12 JPA Hibernate entity lifecycle and dirty checking

The persistence context’s identity map and managed entity state, scoped to that context.

## 24 Does flush make a change durable?

Topic: 12 JPA Hibernate entity lifecycle and dirty checking

Not by itself. Durability follows successful transaction commit according to the database guarantees.

## 25 What does mappedBy mean?

Topic: 13 Entity relationships ownership and aggregates

It names the property on the owning side of a bidirectional relationship; it is not the database column name.

## 26 Why store a price snapshot on an order line?

Topic: 13 Entity relationships ownership and aggregates

The order must preserve the agreed purchase price even when the catalog price later changes.

## 27 Does EAGER solve N plus one?

Topic: 14 Fetching N plus one queries and performance

Not reliably. It specifies when data must be available, not an optimal SQL strategy.

## 28 Why disable Open Session in View?

Topic: 14 Fetching N plus one queries and performance

It makes database access boundaries explicit and reduces unexpected queries during response rendering.

## 29 When is optimistic locking attractive?

Topic: 15 Optimistic locking pessimistic locking and stock

When conflicts are relatively rare and the application can detect and retry or report them safely.

## 30 Why is a version field insufficient if updates bypass it?

Topic: 15 Optimistic locking pessimistic locking and stock

Every competing write must participate in the version check or use another compatible concurrency control.

## 31 Why test migrations on PostgreSQL if H2 passes?

Topic: 16 Migrations PostgreSQL and data evolution

SQL dialect, locking, types, indexes, and execution plans can differ.

## 32 What makes a rolling schema change safe?

Topic: 16 Migrations PostgreSQL and data evolution

Old and new application versions can both operate correctly during the transition.

## 33 Why is a shared database risky for microservices?

Topic: 17 Service boundaries and domain ownership

It allows hidden coupling and independent code changes to violate another service’s assumptions.

## 34 When would you prefer a modular monolith?

Topic: 17 Service boundaries and domain ownership

When domain boundaries are still evolving or the team cannot justify the operational cost of independent services.

## 35 What is a retry storm?

Topic: 18 HTTP clients timeouts retries and resilience

Many callers repeatedly retrying a failing dependency, multiplying load and delaying recovery.

## 36 Why keep the order pending after a timeout?

Topic: 18 HTTP clients timeouts retries and resilience

Inventory may have reserved stock even though the response was lost; reconciliation is required.

## 37 Is a random key generated on every retry useful?

Topic: 19 Idempotency and concurrent request handling

No. It turns each retry into a new logical intent and defeats deduplication.

## 38 Can an in-memory map enforce idempotency across replicas?

Topic: 19 Idempotency and concurrent request handling

Not by itself. Replicas need shared durable coordination or a routing and recovery design that provides equivalent guarantees.

## 39 What does the outbox guarantee?

Topic: 20 Transactional outbox messaging and consumers

The local business change and event record commit together, assuming the database transaction succeeds.

## 40 Why can outbox delivery repeat?

Topic: 20 Transactional outbox messaging and consumers

Publishing and recording relay progress are separate operations, leaving a crash window.

## 41 Why is compensation different from rollback?

Topic: 21 Sagas compensation and workflow recovery

Earlier service transactions have already committed, and reversing their business effects requires new operations.

## 42 What must survive a coordinator restart?

Topic: 21 Sagas compensation and workflow recovery

Workflow state, step identities, known outcomes, retry metadata, and enough information to reconcile unknown effects.

## 43 What is the difference between 401 and 403?

Topic: 22 Authentication authorization and service security

401 indicates missing or invalid authentication; 403 indicates the authenticated caller lacks permission, subject to deliberate resource-hiding policies.

## 44 Why is gateway-only authorization insufficient?

Topic: 22 Authentication authorization and service security

Internal calls, misconfiguration, or alternate routes can bypass the gateway; the data-owning service must enforce its own policy.

## 45 Why is p95 useful?

Topic: 23 Observability containers and production delivery

It describes the latency below which 95 percent of measured requests fall, revealing tail behavior hidden by the mean.

## 46 What should a deployment rollback consider besides code?

Topic: 23 Observability containers and production delivery

Schema compatibility, configuration, external effects, queued events, and data written by the newer version.

## 47 How do you choose between a monolith and microservices?

Topic: 24 Commerce capstone and interview practice

Compare team ownership, deployment independence, scaling needs, domain stability, and operational cost; choose the simplest architecture that satisfies the constraints.

## 48 What makes an advanced answer convincing?

Topic: 24 Commerce capstone and interview practice

Precise invariants, explicit transaction and failure boundaries, measured tradeoffs, and a recovery story supported by tests.
