# Java Spring Boot Hibernate and Microservices

Edition prepared 6 November 2026. Examples use Java 21; Spring examples pin Boot 4.0.9. The optional AI framework adapter pins Spring AI 2.0.2. These are tested course versions, not a claim that they are the newest releases.

The original videos use synthesized English narration with captions. They are concise topic lectures, not recordings of live coding. Each module includes a worked scenario and an implementation or design exercise. Some advanced integrations are deliberately assigned as extensions; the code README identifies what is implemented.

# 01 Java syntax values and methods

## Explanation

A Java program organizes behavior into classes and methods. Variables have declared types, and the compiler checks many mistakes before the program runs. Primitive types represent values such as integers and booleans; reference types refer to objects and may be null. Choose types that express the domain instead of passing every value as a string.

For commerce calculations, decide whether money is represented as integer minor units or BigDecimal with an explicit rounding policy. Binary floating-point cannot exactly represent many decimal fractions. Construct BigDecimal from decimal strings when exact decimal input matters. Validate quantities and prices at the domain boundary, not only in a web form.

## Worked example

JavaBasics.Line contains a stock-keeping unit, quantity, and unit price. Its constructor rejects an empty SKU, nonpositive quantity, or negative price. Three items priced at 0.10 produce exactly 0.30 in the supplied test. A valid domain object makes later calculations easier to reason about.

```text
var price = new BigDecimal("0.10");
var line = new Line("book", 3, price);
System.out.println(line.total()); // 0.30
```

## Practice

Implement subtotal, tax, and total for a basket. Choose a rounding rule and test zero items, a fractional tax, a large quantity, and invalid input. Compile with Java 21.

## Model answer and acceptance criteria

Keep arithmetic separate from printing and HTTP. State when rounding occurs, such as once per invoice or once per line, because the totals can differ. Test expected decimal values rather than formatted strings alone.

## Interview check

**Why not use double for exact currency arithmetic?**

Many decimal amounts cannot be represented exactly in binary floating-point; use minor units or decimal arithmetic with a stated rounding rule.

**What is the difference between a compile error and an exception?**

A compile error prevents valid bytecode generation; an exception occurs while a compiled program is executing.

## Further reading

[Primary documentation or original author](https://dev.java/learn/language-basics/)

## Video

[Lecture](../videos/01.mp4) · [Transcript](../videos/01.txt) · [Captions](../videos/01.srt)

# 02 Objects interfaces records and exceptions

## Explanation

A class combines state and behavior. Encapsulation protects rules by limiting how state can change. An interface describes a capability, allowing implementations to vary without changing callers. Prefer composition when one object uses another capability; inheritance is useful only when the subtype genuinely satisfies the parent contract.

Records provide concise data carriers and generated equality, accessors, and string representation. They are useful for request and response DTOs, but JPA entities have different lifecycle and proxy requirements. Exceptions should represent meaningful failure categories. Catch an exception only where you can recover, translate it into a useful boundary contract, or add context without losing the cause.

## Worked example

JavaBasics.Discount is an interface whose implementation can calculate a discount without knowing about Spring. InventoryService has domain-specific Missing and Conflict failures. The HTTP layer translates these into 404 and 409, preserving the distinction between an absent product and insufficient stock.

```text
interface Discount {
  BigDecimal apply(BigDecimal amount);
}
record ProductView(long id, String name, int stock) {}
```

## Practice

Implement fixed and percentage discount strategies. Add a maximum discount rule and tests that a discount cannot make the total negative. Explain why a request DTO should not be the same object as a persistence entity.

## Model answer and acceptance criteria

Inject the selected strategy into a calculation service. DTOs expose only the intended public contract; entities represent database state and may include internal fields, relationships, and mutable lifecycle. Keeping them separate prevents accidental API changes when mappings change.

## Interview check

**What is dependency inversion?**

High-level policy depends on an abstraction rather than a concrete infrastructure detail.

**When should you use a record?**

For a value-like data carrier whose shallow immutability and generated equality fit the intended semantics.

## Further reading

[Primary documentation or original author](https://dev.java/learn/classes-objects/)

## Video

[Lecture](../videos/02.mp4) · [Transcript](../videos/02.txt) · [Captions](../videos/02.srt)

# 03 Collections generics streams and equality

## Explanation

Collections represent groups of values. A List preserves an ordered sequence, a Set represents uniqueness, and a Map associates keys with values. Equality and hash-code contracts matter when objects become keys. Mutating a field used in hashCode after insertion can make a key effectively unreachable in a hash-based collection.

Generics express element types and reduce unsafe casts. Streams describe transformations such as filtering, mapping, grouping, and reduction. They are not automatically faster than loops, and a stream pipeline should avoid hidden side effects. Parallel streams use shared execution resources and are usually a poor default for blocking database or HTTP work.

## Worked example

JavaBasics.quantities groups basket lines by SKU and sums quantities. Two lines for SKU a with quantities two and three become a single entry with quantity five. The total function reduces line totals with BigDecimal.ZERO. These operations are pure enough to test without starting Spring.

```text
lines.stream()
  .collect(groupingBy(Line::sku,
      summingInt(Line::quantity)));
// {a=5}
```

## Practice

Build a report of the three highest-selling SKUs. Define tie ordering, null handling, and whether returned collections may be modified. Compare a loop implementation with a stream implementation for readability.

## Model answer and acceptance criteria

Use deterministic sorting, such as descending quantity followed by SKU. Reject invalid domain data early instead of scattering null checks through the pipeline. Return a copied or immutable collection when callers should not mutate the report.

## Interview check

**Why must equals and hashCode agree?**

Equal objects must have equal hash codes so hash-based lookup can find an equal key.

**When is a loop preferable to a stream?**

When early exits, complex branching, or explicit state make the loop easier to understand and verify.

## Further reading

[Primary documentation or original author](https://dev.java/learn/api/collections-framework/)

## Video

[Lecture](../videos/03.mp4) · [Transcript](../videos/03.txt) · [Captions](../videos/03.srt)

# 04 Concurrency virtual threads and the JVM

## Explanation

Concurrency allows work to overlap; parallelism means work executes at the same time on multiple processing resources. Shared mutable state requires coordination. A compound check followed by an update is not atomic merely because individual field reads are safe. Locks, atomic operations, immutable data, and message passing solve different coordination problems.

Java 21 virtual threads reduce the cost of many blocking tasks, but they do not create unlimited database connections, sockets, or provider quota. Bound access to scarce dependencies separately. The JVM manages heap allocation and garbage collection; memory leaks still occur when objects remain reachable unnecessarily. Measure allocation, pauses, thread activity, and pool waits before tuning.

## Worked example

InventoryTest launches twenty buyers using virtual threads. Database row locking, rather than the thread type, prevents overselling ten units. Replacing virtual threads with platform threads should preserve the business result. The experiment separates concurrency implementation from correctness authority.

```text
try (var pool = Executors.newVirtualThreadPerTaskExecutor()) {
  Future<Result> result = pool.submit(task);
  // Apply a deadline and bound downstream concurrency.
}
```

## Practice

Run 100 blocking read tasks with a semaphore allowing ten outbound calls. Record peak active calls and completion time. Add cancellation and ensure interrupted tasks restore or propagate interruption.

## Model answer and acceptance criteria

The semaphore limits dependency pressure while virtual threads make waiting economical. A test should verify the peak active count, not only completion. Do not hold a database transaction while waiting for unrelated remote work.

## Interview check

**Do virtual threads prevent races?**

No. Shared-state correctness still requires appropriate synchronization or transactional coordination.

**Why can a service be slow with low CPU?**

It may be waiting on connections, locks, queues, disk, or downstream calls rather than executing CPU work.

## Further reading

[Primary documentation or original author](https://docs.oracle.com/en/java/javase/21/core/virtual-threads.html)

## Video

[Lecture](../videos/04.mp4) · [Transcript](../videos/04.txt) · [Captions](../videos/04.srt)

# 05 HTTP REST and API contracts

## Explanation

HTTP is a request-response protocol with methods, headers, status codes, and representations. A REST-style API exposes resources and uses method semantics consistently. GET should be safe; POST commonly creates or executes an operation. Idempotence means repeated equivalent requests have the same intended effect, not necessarily the same response bytes.

Define the contract before implementation: path, method, input schema, authentication, pagination, success responses, and errors. Distinguish 400 for invalid input, 404 for missing accessible resources, 409 for a state conflict, and 202 for accepted work whose outcome is pending. A timeout does not tell the client whether the server committed.

## Worked example

The inventory API accepts POST /reservations with an Idempotency-Key and a product and quantity. The orders API returns a PENDING order with 202 when inventory’s outcome is unknown. A repeated logical checkout reuses the same key. The provided contract files describe these semantics for the local learning services.

```text
POST /orders
Idempotency-Key: checkout-001
Content-Type: application/json

{"productId":1,"quantity":2}
```

## Practice

Write an API contract for cancelling an order, including already-cancelled, already-shipped, unknown-order, and unauthorized cases. Decide which responses may safely be retried.

## Model answer and acceptance criteria

Make cancellation idempotent for the same intent, validate ownership, and return a conflict when shipment makes cancellation impossible. Define whether inaccessible and absent resources share a 404 response. A client should preserve the original logical request identity on retries.

## Interview check

**Is HTTP 200 proof that the business task succeeded?**

Only if the documented response semantics say so; a response may contain a pending or rejected business state.

**What does a client timeout imply?**

The result is unknown. The server may have committed before the connection failed.

## Further reading

[Primary documentation or original author](https://www.rfc-editor.org/rfc/rfc9110)

## Video

[Lecture](../videos/05.mp4) · [Transcript](../videos/05.txt) · [Captions](../videos/05.srt)

# 06 Spring beans dependency injection and proxies

## Explanation

Spring creates and connects application objects called beans. Constructor injection declares required dependencies and supports ordinary unit testing. A service should express business behavior without looking up its dependencies from a global context. Bean scope controls instance lifetime; singleton means one instance per application context, not one instance across all deployments.

Several Spring features use proxies that intercept calls, including declarative transactions. With the usual proxy-based transaction model, a method calling another method on the same object does not pass through the proxy. This explains why moving a transactional method to a separate injected service can change behavior.

## Worked example

OrderService orchestrates the checkout while OrderTransactions owns short database transactions. Calls from OrderService to the injected OrderTransactions bean cross the proxy. The HTTP inventory call happens after create returns and before complete begins, avoiding a database transaction held open during network waiting.

```text
OrderService(OrderTransactions tx, InventoryClient client) {
  this.tx = tx;
  this.inventory = client;
}
// The transaction boundary is another injected bean.
```

## Practice

Draw the bean graph for both services. Write a unit test that constructs a pure domain service directly. Demonstrate why calling this.transactionalMethod() differs from calling a separate proxied bean.

## Model answer and acceptance criteria

Constructor injection reveals the graph and prevents partially initialized required dependencies. Verify transaction activity or rollback behavior in the proxy demonstration rather than relying on the presence of an annotation. Keep infrastructure details out of pure calculations.

## Interview check

**Does a singleton bean make its fields thread-safe?**

No. Concurrent requests may call the same instance, so mutable fields need appropriate coordination or should be avoided.

**Why can self-invocation bypass a transaction annotation?**

The call stays inside the target object and does not pass through the transaction proxy.

## Further reading

[Primary documentation or original author](https://docs.spring.io/spring-framework/reference/core/beans/dependencies/factory-collaborators.html)

## Video

[Lecture](../videos/06.mp4) · [Transcript](../videos/06.txt) · [Captions](../videos/06.srt)

# 07 Spring Boot configuration and application startup

## Explanation

Spring Boot assembles a Spring application using starters, auto-configuration, and external configuration. Auto-configuration reacts to classes, properties, and existing beans; it is not magic and can be inspected when behavior is unexpected. Use the dependency management supplied by the chosen Boot release instead of independently mixing framework versions.

Keep environment-specific values outside source code. Profiles can select local and PostgreSQL settings, while environment variables supply connection details. Validate required configuration at startup. Secrets should come from an appropriate secret store or injected environment, never a committed repository file.

## Worked example

Both services pin Boot 4.0.9 and Java 21. The default profile binds only to 127.0.0.1 and uses an in-memory H2 database. The postgres profile reads DB_URL, DB_USER, and DB_PASSWORD. Flyway creates the schema, while Hibernate validates that the mappings match it. Restarting the default profile resets its data.

```text
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.open-in-view=false
server.address=127.0.0.1
# postgres profile uses environment-provided credentials
```

## Practice

Run a service with a different port supplied on the command line. Then intentionally provide an invalid database URL and inspect the startup failure. Explain which settings should vary between development and production.

## Model answer and acceptance criteria

Ports, database endpoints, credentials, timeouts, and observability destinations vary by environment. Business invariants should not silently change through profiles. Keep a documented configuration contract and avoid copying production credentials into a local properties file.

## Interview check

**What does a starter provide?**

A coordinated dependency set and configuration integration for a capability such as web MVC or JPA.

**Why pin a compatible version family?**

It reduces untested combinations of Spring, Hibernate, Jackson, and other managed dependencies.

## Further reading

[Primary documentation or original author](https://docs.spring.io/spring-boot/4.0/reference/using/auto-configuration.html)

## Video

[Lecture](../videos/07.mp4) · [Transcript](../videos/07.txt) · [Captions](../videos/07.srt)

# 08 REST controllers DTOs and pagination

## Explanation

Controllers translate HTTP requests into application operations and translate results back into the API contract. Keep business rules in services, persistence in repositories, and request validation at the boundary. DTOs prevent persistence details from accidentally becoming public JSON fields. They also avoid serializing lazy relationships or recursive entity graphs.

Pagination must be bounded and ordered. A page without a stable sort can repeat or omit records as the database changes. Offset pagination is simple for small datasets; keyset pagination can work better for deep sequential browsing. Define maximum page size so one request cannot fetch the entire table.

## Worked example

InventoryController maps products to ProductView records and accepts page sizes from one to one hundred. It sorts by product ID. OrderController exposes only ID, product, quantity, and status. Internal idempotency keys and JPA version fields are not automatically serialized from the order entity.

```text
products.findAll(PageRequest.of(page, size, Sort.by("id")))
  .stream()
  .map(p -> new ProductView(
      p.getId(), p.getName(), p.getStock(), p.getVersion()));
```

## Practice

Add GET /products/{id} and a product search endpoint. Return explicit DTOs, validate pagination limits, and test the empty page and unknown product cases over HTTP.

## Model answer and acceptance criteria

Map an unknown product to the documented 404 response. Keep search inputs parameterized through repository APIs. Use a deterministic sort with a tie-breaker and state whether the endpoint returns a total count or only a next-page indicator.

## Interview check

**Why avoid returning JPA entities from controllers?**

It couples the public API to persistence structure and can expose internal fields or trigger unexpected lazy loads.

**What is keyset pagination?**

Fetching rows after a stable ordered cursor, such as a last-seen ID, instead of skipping a growing offset.

## Further reading

[Primary documentation or original author](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller.html)

## Video

[Lecture](../videos/08.mp4) · [Transcript](../videos/08.txt) · [Captions](../videos/08.srt)

# 09 Validation error handling and problem details

## Explanation

Validation occurs at several layers. Bean Validation checks request shape and ranges, domain methods enforce business invariants, and database constraints protect persisted state under concurrency. None completely replaces the others. A request may pass field validation but still conflict with current stock or a previously used idempotency key.

Return stable error categories without leaking SQL, stack traces, or credentials. Problem Details gives an interoperable structure for HTTP errors. Include useful client information such as which field is invalid and a correlation identifier, while detailed internal diagnostics stay in protected logs.

## Worked example

ReserveRequest requires a positive product ID and a quantity between one and one thousand. InventoryService rechecks its own boundary because it can also be called outside HTTP. A database check constraint prevents negative stock. Conflicts become 409; missing products become 404; invalid arguments become 400.

```text
record ReserveRequest(
  @Positive long productId,
  @Min(1) @Max(1000) int quantity) {}

check (stock >= 0)
```

## Practice

Send malformed JSON, quantity zero, an unknown product, and a repeated key with a different quantity. Write down the expected status and verify it with the provided HTTP smoke test or Postman collection.

## Model answer and acceptance criteria

Malformed or invalid input should return 400, unknown product 404, and a conflicting key 409. Confirm that failed requests do not alter stock or create orders. Avoid a catch-all handler that converts every programming defect into a misleading client error.

## Interview check

**Why validate again in a service?**

Services may be invoked by jobs, tests, or other code that bypasses the HTTP validator.

**Why keep database constraints?**

They enforce invariants at the durable shared boundary even when concurrent callers or another application bypass application checks.

## Further reading

[Primary documentation or original author](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-ann-rest-exceptions.html)

## Video

[Lecture](../videos/09.mp4) · [Transcript](../videos/09.txt) · [Captions](../videos/09.srt)

# 10 Testing from domain rules to HTTP boundaries

## Explanation

Choose tests by the risk they address. Pure unit tests verify calculations and branching quickly. Persistence tests verify mappings, transactions, constraints, and queries. HTTP integration tests verify serialization, validation, status codes, and the actual client-server contract. End-to-end tests verify a small number of important complete workflows.

A mock proves how a caller handles a supplied response; it does not prove the real dependency behaves that way. Match mocks to the contract and separately exercise the real service. Concurrency and crash-recovery claims need tests that reproduce the relevant interleavings and durability boundaries.

## Worked example

The project tests exact decimal arithmetic, stale entity versions, transactional rollback, concurrent stock purchases, duplicate reservations, order idempotency, and inventory timeouts. OrderTest uses a real local HTTP fixture so RestClient’s network and timeout behavior is exercised. Separate smoke tests start both actual Boot services.

```text
Domain test -> no application context
JPA test -> real database transaction
HTTP fixture -> real client boundary
Two-service smoke -> complete checkout path
```

## Practice

Break one invariant deliberately: remove the stock lock or change timeout handling from PENDING to REJECTED. Identify which existing test fails and add a missing test only if the behavior is not covered.

## Model answer and acceptance criteria

Tests should fail for the behavior they are intended to protect. Avoid duplicating implementation line by line. H2 success is useful but does not establish PostgreSQL lock timing, query plans, or dialect compatibility; run the target database before making those claims.

## Interview check

**What does an integration test add over a unit test?**

Evidence that multiple real components cooperate through their actual boundaries and configuration.

**Why not test everything end to end?**

Such tests are slower and less diagnostic; use focused tests for most rules and a few complete-path checks.

## Further reading

[Primary documentation or original author](https://docs.spring.io/spring-boot/4.0/reference/testing/index.html)

## Video

[Lecture](../videos/10.mp4) · [Transcript](../videos/10.txt) · [Captions](../videos/10.srt)

# 11 SQL schema design indexes and transactions

## Explanation

A relational schema expresses entities, keys, relationships, and constraints. A transaction groups changes that must commit or roll back together. Isolation controls what concurrent transactions can observe; it does not automatically enforce every business rule. A read-then-write sequence can race unless protected by an appropriate lock, version check, conditional update, or constraint.

Indexes speed selected reads and constraint checks but cost storage and write work. Design them for actual query predicates and ordering, then inspect execution plans on representative data. Avoid assuming that adding an index to every column improves performance. Keep transactions short to reduce lock duration and connection occupancy.

## Worked example

The inventory schema has products and reservations. Stock cannot be negative, reservation quantity must be positive, and request_key is unique. The order schema records orders and outbox events in a separate database. An order ID in the inventory request is a business reference, not a cross-database foreign key.

```text
create table reservations (
  request_key varchar(80) primary key,
  product_id bigint not null references products(id),
  quantity integer not null check (quantity > 0)
);
```

## Practice

Design an index for finding a user’s newest orders and explain the ordering of columns. Write a transaction that updates stock and inserts a reservation, then force the insert to fail and verify rollback.

## Model answer and acceptance criteria

A candidate index begins with the equality-filtered user or tenant columns and continues with the time and stable tie-breaker used for ordering. Verify the plan with realistic cardinality. The failed reservation insert must leave stock unchanged because both changes share one transaction.

## Interview check

**Does a transaction automatically prevent overselling?**

No. The isolation level and read-write pattern must enforce the stock invariant under concurrency.

**Why can an unused index be harmful?**

It consumes space and increases insert, update, delete, and maintenance work without helping the workload.

## Further reading

[Primary documentation or original author](https://www.postgresql.org/docs/current/transaction-iso.html)

## Video

[Lecture](../videos/11.mp4) · [Transcript](../videos/11.txt) · [Captions](../videos/11.srt)

# 12 JPA Hibernate entity lifecycle and dirty checking

## Explanation

JPA is a persistence specification; Hibernate is an implementation. An entity moves through transient, managed, detached, and removed states. A persistence context tracks managed entities and their identities. Changes to managed state can be detected and synchronized to SQL during flush. Flush sends changes to the database but does not necessarily commit the transaction.

A detached entity is no longer tracked. Merging detached state copies it into a managed instance; callers should use the returned instance. Entity identity, generated IDs, equality, and lazy proxies require deliberate design. Use protected no-argument constructors and mapping annotations consistent with the selected access strategy.

## Worked example

InventoryService loads a managed Product under a lock and calls reserve, changing its stock field. It does not need an explicit product save for dirty checking to persist that change at transaction completion. The reservation insert and product update participate in the same transaction. The rollback test proves a failed transaction discards a renamed product.

```text
@Entity
class Product {
  @Id private Long id;
  @Version private long version;
  protected Product() {}
}
```

## Practice

Load an entity inside a transaction, modify it, flush, then roll back. Repeat with an entity loaded outside the transaction and explain the difference. Observe SQL with logging enabled only for the local lab.

## Model answer and acceptance criteria

A managed change is synchronized on flush but remains subject to rollback. A detached object’s changes are not tracked until explicitly reattached through an appropriate operation. Do not equate save, flush, and commit; they have different meanings.

## Interview check

**What is the first-level cache?**

The persistence context’s identity map and managed entity state, scoped to that context.

**Does flush make a change durable?**

Not by itself. Durability follows successful transaction commit according to the database guarantees.

## Further reading

[Primary documentation or original author](https://docs.hibernate.org/orm/7.2/introduction/html_single/)

## Video

[Lecture](../videos/12.mp4) · [Transcript](../videos/12.txt) · [Captions](../videos/12.srt)

# 13 Entity relationships ownership and aggregates

## Explanation

Relationships map object references to foreign keys or join tables. The owning side determines which mapping writes the relationship. For a typical one-to-many and many-to-one pair, the many-to-one side owns the foreign key, while mappedBy identifies the inverse collection. Keep both object sides consistent through helper methods.

Cascade controls which entity operations propagate; it is not database foreign-key cascading and does not mean every relationship should use CascadeType.ALL. Orphan removal deletes children removed from an owned collection and should match aggregate semantics. Avoid cascading removal from an order to a shared product.

## Worked example

The supplied checkout starts with one product per order to keep distributed behavior visible. The relationship extension adds OrderLine children owned by PurchaseOrder, with each line storing a product identifier and price snapshot. Product remains owned by inventory and is not a JPA relationship across services.

```text
@OneToMany(mappedBy="order", cascade=CascadeType.ALL,
           orphanRemoval=true)
List<OrderLine> lines;
// OrderLine owns @ManyToOne and the order_id column.
```

## Practice

Implement OrderLine with quantity and price snapshot. Add a helper that sets both order.lines and line.order. Test adding a line, removing a line, and deleting an order without deleting any catalog product.

## Model answer and acceptance criteria

Use aggregate ownership for cascade decisions. A line belongs to one order and may be deleted with it; a product is shared and independently managed. Add migrations and test foreign-key behavior. Do not expose the bidirectional graph directly as JSON.

## Interview check

**What does mappedBy mean?**

It names the property on the owning side of a bidirectional relationship; it is not the database column name.

**Why store a price snapshot on an order line?**

The order must preserve the agreed purchase price even when the catalog price later changes.

## Further reading

[Primary documentation or original author](https://docs.hibernate.org/orm/7.2/introduction/html_single/)

## Video

[Lecture](../videos/13.mp4) · [Transcript](../videos/13.txt) · [Captions](../videos/13.srt)

# 14 Fetching N plus one queries and performance

## Explanation

Lazy fetching defers data access until needed, while eager fetching requests related data as part of loading policy. Eager does not guarantee one SQL join or good performance. The N plus one pattern occurs when one query loads parents and subsequent accesses issue one additional query per parent. Measure query count and row volume rather than guessing from annotations.

Choose a fetch plan for each use case: a DTO projection, entity graph, fetch join, or batch fetching. Joining several collections can multiply rows dramatically. Combining collection fetch joins with pagination can be unsafe or inefficient depending on the query and provider behavior. Keep the persistence boundary explicit with Open Session in View disabled.

## Worked example

The product list maps simple product fields to DTOs, so it has no relationship traversal. After adding order lines, a dashboard that loads orders and then accesses each line collection can trigger many queries. A two-step paged query or projection can fetch only the data required by the dashboard.

```text
1 query: load 50 orders
50 queries: initialize each lines collection
Total: 51 queries
Fix the use-case fetch plan and measure again.
```

## Practice

Create fifty orders with several lines each. Record SQL count for a lazy traversal, a DTO projection, and a bounded batch approach. Compare transferred rows and latency as well as query count.

## Model answer and acceptance criteria

One enormous join is not automatically better than several bounded queries. Prevent accidental lazy access after the transaction ends by assembling the DTO inside the intended boundary. Verify the chosen plan on the target database and realistic data size.

## Interview check

**Does EAGER solve N plus one?**

Not reliably. It specifies when data must be available, not an optimal SQL strategy.

**Why disable Open Session in View?**

It makes database access boundaries explicit and reduces unexpected queries during response rendering.

## Further reading

[Primary documentation or original author](https://docs.hibernate.org/orm/7.2/userguide/html_single/Hibernate_User_Guide.html)

## Video

[Lecture](../videos/14.mp4) · [Transcript](../videos/14.txt) · [Captions](../videos/14.srt)

# 15 Optimistic locking pessimistic locking and stock

## Explanation

Optimistic locking detects conflicting updates using a version or equivalent condition. It works well when conflicts are uncommon and retries are feasible. Pessimistic locking acquires a database lock before modifying a row, trading waiting and possible deadlocks for serialized access. Neither strategy removes the need for short transactions and clear retry behavior.

For stock, another option is a conditional update that decrements only when enough units remain and checks the affected-row count. The correct strategy depends on contention, transaction scope, and database behavior. A JVM synchronized method alone does not coordinate several service replicas.

## Worked example

Product has a version field, and a stale detached update fails in the test. Reservation uses a pessimistic row lock so concurrent buyers inspect and update stock serially. Twenty one-unit requests against ten units produce exactly ten successes. The result is verified on H2; PostgreSQL remains a separate deployment verification step.

```text
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("select p from Product p where p.id=:id")
Optional<Product> lock(long id);

// Alternative: UPDATE ... WHERE stock >= quantity
```

## Practice

Replace row locking with a conditional update and compare the implementations under contention. Add a test for two different products and a reused reservation key. Explain how deadlocks should be retried.

## Model answer and acceptance criteria

Check the affected-row count and keep reservation insertion in the same transaction. Retrying a deadlock must restart the whole transaction with bounded attempts. Reusing a key with different request data must conflict rather than consume stock again.

## Interview check

**When is optimistic locking attractive?**

When conflicts are relatively rare and the application can detect and retry or report them safely.

**Why is a version field insufficient if updates bypass it?**

Every competing write must participate in the version check or use another compatible concurrency control.

## Further reading

[Primary documentation or original author](https://docs.hibernate.org/orm/7.2/userguide/html_single/Hibernate_User_Guide.html)

## Video

[Lecture](../videos/15.mp4) · [Transcript](../videos/15.txt) · [Captions](../videos/15.srt)

# 16 Migrations PostgreSQL and data evolution

## Explanation

Schema migrations are versioned changes applied in a known order. Once a migration has been applied in a shared environment, add a new migration rather than editing history. Hibernate schema validation checks mapping compatibility; automatic schema update is not a replacement for reviewed production migrations.

Use expand-and-contract changes for rolling deployments. First add a compatible field or table, then deploy code that can handle both states, backfill safely, switch reads, and finally remove obsolete structures after old code is gone. Large backfills need progress tracking, bounded batches, and operational monitoring.

## Worked example

Flyway creates each service’s schema from its own migration directory. The default H2 profile makes the course easy to run, while the PostgreSQL profile accepts an external database connection. The two services must use separate databases or isolated ownership boundaries; sharing table access would undermine the service split.

```text
V1__orders.sql -> initial schema
V2__add_customer_reference.sql -> compatible addition
Backfill in bounded batches
Later migration -> enforce new constraint
```

## Practice

Add a nullable customer reference to orders, backfill synthetic rows, then make it required in a later migration. Test both a fresh database and an upgrade from the previous schema.

## Model answer and acceptance criteria

A fresh-install test alone misses upgrade failures. Preserve old migration checksums, test against production-like data volume, and plan rollback or forward repair. A destructive migration may not be reversible by simply redeploying old Java code.

## Interview check

**Why test migrations on PostgreSQL if H2 passes?**

SQL dialect, locking, types, indexes, and execution plans can differ.

**What makes a rolling schema change safe?**

Old and new application versions can both operate correctly during the transition.

## Further reading

[Primary documentation or original author](https://documentation.red-gate.com/flyway)

## Video

[Lecture](../videos/16.mp4) · [Transcript](../videos/16.txt) · [Captions](../videos/16.srt)

# 17 Service boundaries and domain ownership

## Explanation

A microservice owns a coherent business capability and its data. Splitting by technical layer, such as controller service and database service, often creates excessive coupling. Begin with domain boundaries, team ownership, consistency requirements, and deployment needs. A modular monolith can be a better starting point when independent deployment is not yet valuable.

A service boundary introduces network failure, latency, versioning, and operational overhead. Keep contracts explicit and avoid cross-service database joins. Share identifiers and events rather than entity classes. A small shared library may reduce repetition, but a shared domain model can force services to upgrade together.

## Worked example

Inventory owns products, stock, and reservations. Orders owns checkout intent, order status, and outbox events. Orders calls the inventory API with its order ID as the reservation identity. It does not query the inventory database or assume a distributed transaction spans both databases.

```text
Client -> Orders -> Inventory
          |          |
       Orders DB   Inventory DB
Each service owns its schema and transaction boundary.
```

## Practice

Design a commerce boundary map including catalog, inventory, orders, payment, and shipping. Identify which capabilities could remain modules in one application and which need independent scaling or ownership.

## Model answer and acceptance criteria

Use concrete reasons for each split: independent release cadence, different availability needs, data ownership, or measured scaling pressure. Document the added failure modes and communication contracts. Do not create a service merely because a domain noun exists.

## Interview check

**Why is a shared database risky for microservices?**

It allows hidden coupling and independent code changes to violate another service’s assumptions.

**When would you prefer a modular monolith?**

When domain boundaries are still evolving or the team cannot justify the operational cost of independent services.

## Further reading

[Primary documentation or original author](https://spring.io/projects/spring-modulith)

## Video

[Lecture](../videos/17.mp4) · [Transcript](../videos/17.txt) · [Captions](../videos/17.srt)

# 18 HTTP clients timeouts retries and resilience

## Explanation

Every remote call can fail before sending, during processing, or after the server commits but before the reply arrives. Configure connection and response timeouts and fit them within a total request deadline. A retry is safe only when the operation’s semantics and identity support it. Use bounded attempts with backoff and jitter for genuinely retryable failures.

Circuit breakers reduce calls to a failing dependency, while bulkheads limit how much capacity one dependency can occupy. Neither creates business correctness. Avoid holding database locks or transactions across slow network calls. Record unknown outcomes explicitly instead of guessing that a timeout means rejection.

## Worked example

InventoryClient uses a one-second connection timeout and a two-second read timeout. A 409 or 404 becomes a known rejection; transport failures and server errors remain UNKNOWN. OrderService leaves the order PENDING and can reconcile by repeating the same reservation identity. A test exercises an actual delayed HTTP response.

```text
RESERVED -> CONFIRMED
Known business rejection -> REJECTED
Timeout or server failure -> PENDING
Retry uses the same order ID at inventory.
```

## Practice

Run the inventory mock in success, outage, and delayed scenarios. Start orders against that endpoint. Verify that a timeout produces PENDING and that retrying after recovery confirms the same order.

## Model answer and acceptance criteria

Preserve the idempotency key and order ID across attempts. Do not translate a timeout into insufficient stock. Measure the elapsed time and verify that the HTTP client’s timeout is active. The mock demonstrates caller behavior; the real inventory smoke test verifies the normal integration.

## Interview check

**What is a retry storm?**

Many callers repeatedly retrying a failing dependency, multiplying load and delaying recovery.

**Why keep the order pending after a timeout?**

Inventory may have reserved stock even though the response was lost; reconciliation is required.

## Further reading

[Primary documentation or original author](https://docs.spring.io/spring-framework/reference/integration/rest-clients.html)

## Video

[Lecture](../videos/18.mp4) · [Transcript](../videos/18.txt) · [Captions](../videos/18.srt)

# 19 Idempotency and concurrent request handling

## Explanation

Idempotency preserves one logical intent across repeated delivery. A client-generated key must remain stable for the same intent, and the server must bind it to relevant request fields. The same key with changed data should conflict. Persist the key and business result with a unique constraint so multiple instances share the same authority.

A check-then-insert without a database constraint can race. Handle the losing concurrent insert outside its failed transaction, then read and compare the winning record. Define retention: deleting old keys can allow an old retry to create a new effect. Scope keys by account in a multi-tenant system.

## Worked example

Orders stores request_key with a unique constraint. A matching retry returns the same order; a changed quantity conflicts. The downstream reservation uses the stable order ID, so retries after an unknown inventory outcome cannot consume stock twice. The local demo has no authenticated accounts; tenant-scoped keys are an explicit production extension.

```text
same key + same request -> same logical order
same key + different request -> conflict
new key -> new intent
Database uniqueness resolves concurrent creation.
```

## Practice

Launch twenty concurrent requests using one checkout key, then repeat with different quantities. Verify one order, one reservation, and a conflict for mismatched intent. Design a key-retention policy.

## Model answer and acceptance criteria

Count durable business effects rather than HTTP responses alone. A policy should cover the maximum expected retry window and any legal or business retention requirements. Hash a canonical request representation if the request grows beyond a few fields, and version the canonicalization rule.

## Interview check

**Is a random key generated on every retry useful?**

No. It turns each retry into a new logical intent and defeats deduplication.

**Can an in-memory map enforce idempotency across replicas?**

Not by itself. Replicas need shared durable coordination or a routing and recovery design that provides equivalent guarantees.

## Further reading

[Primary documentation or original author](https://www.postgresql.org/docs/current/ddl-constraints.html)

## Video

[Lecture](../videos/19.mp4) · [Transcript](../videos/19.txt) · [Captions](../videos/19.srt)

# 20 Transactional outbox messaging and consumers

## Explanation

A transactional outbox stores a business change and an event record in the same local transaction. A relay later publishes the event to a broker. This closes the loss window where a database commit succeeds but event publication never happens. It does not eliminate duplicates: a relay may publish successfully and crash before recording completion.

Consumers must make duplicate delivery safe. Store the event ID and local effect in one transaction where possible. Partition ordering applies within the broker’s ordering boundary, not globally across all messages. Design event schemas for compatibility and keep event payloads focused on stable business facts.

## Worked example

OrderTransactions.complete changes a pending order to CONFIRMED or REJECTED and inserts an outbox event in the same transaction. Repeating completion does not add a second event. The supplied project stores the outbox but does not ship a broker relay; implementing and crash-testing that relay is the advanced messaging exercise.

```text
Transaction: order status + outbox row
Relay: publish event -> record progress
Crash between publish and progress -> duplicate
Consumer: event ID + local effect atomically
```

## Practice

Implement a relay with bounded batches and a deduplicating consumer. Inject a crash after publish but before acknowledgement storage. Restart and demonstrate that the event may repeat while the consumer’s business effect remains single.

## Model answer and acceptance criteria

Use stable event IDs and durable consumer deduplication. If the consumer calls an external provider, local deduplication alone is insufficient; use provider idempotency and reconciliation. State the exact effect and failure model before using the phrase exactly once.

## Interview check

**What does the outbox guarantee?**

The local business change and event record commit together, assuming the database transaction succeeds.

**Why can outbox delivery repeat?**

Publishing and recording relay progress are separate operations, leaving a crash window.

## Further reading

[Primary documentation or original author](https://debezium.io/documentation/reference/stable/transformations/outbox-event-router.html)

## Video

[Lecture](../videos/20.mp4) · [Transcript](../videos/20.txt) · [Captions](../videos/20.srt)

# 21 Sagas compensation and workflow recovery

## Explanation

A saga coordinates a business process through multiple local transactions. Each step commits independently, and failures trigger retries, compensation, or manual resolution. Compensation is a business operation, not a database rollback across services. It can also fail and may not fully reverse an external effect.

Define explicit states, allowed transitions, stable step identifiers, deadlines, and recovery ownership. Persist progress so a worker can resume after a crash. Distinguish known rejection from unknown outcome. A payment timeout, for example, requires querying the provider before deciding whether to retry or compensate.

## Worked example

The learning checkout contains a small two-service workflow: create PENDING order, reserve inventory, then mark a terminal status. It has no payment step, automatic reconciliation scheduler, cancellation, or stock-release compensation. The advanced capstone adds these capabilities without pretending that the initial demo is a complete commerce saga.

```text
PENDING -> INVENTORY_RESERVED -> PAYMENT_AUTHORIZED
        -> CONFIRMED
Failure -> release reservation or reconcile payment
Every step has a durable identity and state.
```

## Practice

Extend the state machine with payment authorization and stock release using synthetic providers. Create a failure matrix covering timeouts before and after each remote effect and crashes before each local state update.

## Model answer and acceptance criteria

For each row, specify how to discover the actual outcome and which idempotency key to reuse. Persist compensation attempts and alert on exhausted recovery. A manual-review state is valid when automatic resolution cannot safely determine the next action.

## Interview check

**Why is compensation different from rollback?**

Earlier service transactions have already committed, and reversing their business effects requires new operations.

**What must survive a coordinator restart?**

Workflow state, step identities, known outcomes, retry metadata, and enough information to reconcile unknown effects.

## Further reading

[Primary documentation or original author](https://learn.microsoft.com/en-us/azure/architecture/patterns/saga)

## Video

[Lecture](../videos/21.mp4) · [Transcript](../videos/21.txt) · [Captions](../videos/21.srt)

# 22 Authentication authorization and service security

## Explanation

Authentication establishes identity; authorization decides whether that identity may perform an operation on a resource. Validate token signatures, issuer, audience, and expiry using maintained security libraries. Do not merely decode a JWT and trust its claims. Role checks alone may be insufficient when ownership or tenant boundaries matter.

Enforce authorization in the service that owns the data, even when a gateway already checked the request. Protect secrets, restrict CORS deliberately, validate input, and use TLS outside a local environment. CSRF concerns depend on how credentials are attached by browsers; disabling it without understanding the authentication model is unsafe.

## Worked example

The supplied commerce services are explicitly local learning apps, bound to loopback, with synthetic data and no authentication. Before exposing them, add an OAuth2 resource-server configuration, account ownership, tenant-scoped idempotency, and tests for cross-account reads and writes. The AI track separately demonstrates trusted tenant checks in its tool boundary.

```text
Validate token -> trusted principal
Authorize principal against order ownership
Pass scoped identity to application services
Do not accept tenant authority from request JSON.
```

## Practice

Add a secured profile using a local identity provider or test JWT decoder. Test missing token, wrong issuer, wrong audience, expired token, insufficient role, and another user’s order. Keep the default local demo limitations documented.

## Model answer and acceptance criteria

Use Spring Security’s resource-server support and explicit authorization rules. Derive tenant identity from validated claims mapped by trusted configuration. Test resource ownership independently of role membership. Do not commit credentials or signing keys to the course repository.

## Interview check

**What is the difference between 401 and 403?**

401 indicates missing or invalid authentication; 403 indicates the authenticated caller lacks permission, subject to deliberate resource-hiding policies.

**Why is gateway-only authorization insufficient?**

Internal calls, misconfiguration, or alternate routes can bypass the gateway; the data-owning service must enforce its own policy.

## Further reading

[Primary documentation or original author](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html)

## Video

[Lecture](../videos/22.mp4) · [Transcript](../videos/22.txt) · [Captions](../videos/22.srt)

# 23 Observability containers and production delivery

## Explanation

Logs explain discrete events, metrics summarize behavior over time, and traces connect work across service boundaries. Use correlation identifiers and structured fields while avoiding credentials and unnecessary customer data. Measure request rates, errors, latency distributions, pool waits, database contention, and business outcomes. Averages alone can hide slow-tail behavior.

Containerization packages runtime dependencies but does not make an application operationally complete. Configure resource limits, health checks, graceful shutdown, secret injection, database backups, and deployment rollback. A successful build is different from a successful rollout. Readiness should reflect whether the instance can serve its intended traffic.

## Worked example

The demo exposes health and info through Actuator and binds to loopback. PostgreSQL configuration and migrations prepare the next deployment step, but production authentication, broker delivery, telemetry export, and load-tested capacity remain explicit extensions. A pending-order count and age metric would reveal stuck reconciliation better than HTTP status counts alone.

```text
Observe: p95 latency, errors, DB pool waits
Business: pending order age, stock conflicts
Deploy: health, limits, secrets, migrations
Recover: rollback, backups, reconciliation
```

## Practice

Create a container image, run each service with a separate PostgreSQL database, and load-test a fixed workload. Record hardware, dataset size, concurrency, duration, throughput, error rate, and latency percentiles.

## Model answer and acceptance criteria

Compare measurements under the same conditions and avoid claiming production capacity from a laptop test. Include dependency and database limits in the explanation. Verify restore procedures and schema compatibility as part of recovery, not only that a backup file exists.

## Interview check

**Why is p95 useful?**

It describes the latency below which 95 percent of measured requests fall, revealing tail behavior hidden by the mean.

**What should a deployment rollback consider besides code?**

Schema compatibility, configuration, external effects, queued events, and data written by the newer version.

## Further reading

[Primary documentation or original author](https://docs.spring.io/spring-boot/4.0/reference/actuator/index.html)

## Video

[Lecture](../videos/23.mp4) · [Transcript](../videos/23.txt) · [Captions](../videos/23.srt)

# 24 Commerce capstone and interview practice

## Explanation

Build the capstone in milestones. First run the two local services and explain their contracts. Then add order lines and query-performance tests, run PostgreSQL migrations, add authentication and ownership checks, implement the outbox relay and consumer, and extend the workflow with payment and compensation. Finish with observability, load measurement, and a failure demonstration.

In interviews, begin with functional requirements, scale assumptions, consistency needs, and failure tolerance. Draw the data ownership and transaction boundaries. Trace one request, one concurrent conflict, one timeout, and one restart. Explain why you chose a design and what evidence would cause you to change it. Do not present every extension as already implemented.

## Worked example

A strong portfolio demonstration shows two clients buying the last item, a duplicate checkout returning the same order, an inventory timeout leaving a pending order, and a recovered retry confirming it without reserving twice. Include test output, commands, schema migrations, and a concise architecture decision record.

```text
Milestones
1. Local run and invariant tests
2. JPA relationships and PostgreSQL
3. Security and durable event delivery
4. Recovery, measurements, interview explanation
```

## Practice

Run a 45-minute mock interview: five minutes requirements, ten architecture and schema, ten concurrency and transactions, ten distributed failures, and ten security, operations, and tradeoffs.

## Model answer and acceptance criteria

Score correctness, clarity, data ownership, failure handling, and evidence from one to five. Any answer claiming that a timeout proves failure or that an outbox prevents all duplicates needs revision. Use a runnable experiment to resolve your weakest assumption.

## Interview check

**How do you choose between a monolith and microservices?**

Compare team ownership, deployment independence, scaling needs, domain stability, and operational cost; choose the simplest architecture that satisfies the constraints.

**What makes an advanced answer convincing?**

Precise invariants, explicit transaction and failure boundaries, measured tradeoffs, and a recovery story supported by tests.

## Further reading

[Primary documentation or original author](https://docs.spring.io/spring-boot/4.0/index.html)

## Video

[Lecture](../videos/24.mp4) · [Transcript](../videos/24.txt) · [Captions](../videos/24.srt)
