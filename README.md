# System Design with Java

A 16-week learning package for building systems, interview preparation, and production design reviews.

Start with `docs/System_Design_with_Java.docx`. Watch `videos/System_Design_Java_Sample.mp4`, then use `videos/VIDEO_GUIDE.txt` for topic-specific lessons from original publishers.

## Included

- 24 explained core modules, 8 worked system designs, exercises, model answers, glossary, and a capstone rubric.
- 10 runnable Java labs and one HTTP teaching capstone.
- OpenAPI contract, portable Postman collection with 15 requests and 25 assertions, SQL reference schema, and test evidence.
- An original narrated video, individual topic clips, transcript, and subtitles.

## Prerequisites and quick start

Use JDK 21 or later with Java 21 language support. No Maven, Gradle, Docker, or third-party Java dependency is needed for the supplied code. Run from this folder:

```sh
sh scripts/build.sh
java -cp build/classes course.Labs all
sh scripts/test.sh
sh scripts/run.sh 18080
```

Windows PowerShell: `powershell -ExecutionPolicy Bypass -File scripts/build.ps1` builds and runs the tests. Start the server with `java --add-modules jdk.httpserver -cp build/classes course.CourseServer 18080`.

Open a second terminal:

```sh
curl -i http://127.0.0.1:18080/health
curl -i -H 'Content-Type: text/plain' --data 'https://example.com/guide' http://127.0.0.1:18080/links
curl -i -H 'Content-Type: text/plain' -H 'Idempotency-Key: checkout-1' --data '12500' http://127.0.0.1:18080/orders
```

Repeat the order request: expect 200 and `replayed: true` with the same order id. Change the amount using the same key: expect 409. Stop with Ctrl+C. Restarting resets every in-memory record.

## Labs

Run `java -cp build/classes course.Labs NAME`:

| Name | Source | Experiment |
|---|---|---|
| capacity | Labs.java | Demand, concurrency, storage units |
| cache | TtlCache.java | TTL boundary, LRU, invalidation |
| limiter | TokenBucket.java | Burst budget and fractional refill |
| hashing | HashRing.java | Key movement when a node joins |
| breaker | CircuitBreaker.java | Open, reject, and one recovery probe |
| outbox | OrderStore.java | Retry deduplication and repeated delivery |
| replication | ReplicationDemo.java | Stale follower and session visibility |
| bloom | BloomFilter.java | Approximate membership and false positives |
| queue | Labs.java | Bounded backlog and rejection |
| bulkhead | Labs.java | Virtual threads with finite resource permits |

Use `docs/LAB_WORKBOOK.txt` for expected observations and extensions. The random UUIDs and scheduling-dependent values may differ between runs.

## Scope and verification

The capstone is a loopback-only learning server with in-memory state and no authentication. It implements URL creation/redirect/deletion, toy idempotent orders, a local outbox consumer, a token bucket endpoint, and basic counters. It does not implement a production checkout, Kafka, distributed Redis quota, database persistence, payments, or a complete chat/feed service. Those are explicit extension projects.

JDK 21 compilation, all ten lab demonstrations, and 30 core behavior checks passed. Tests cover concurrent idempotency, atomic quotas, cache expiry, hash placement, circuit-breaker concurrency, event deduplication, and replica version handling. Network integration could not run in the authoring sandbox because listening sockets are blocked. Run `sh scripts/test.sh` locally for the full suite; `sh scripts/test.sh --unit` runs only the core checks. SQL examples were reviewed but not run against PostgreSQL.

The Postman collection is supplied but was not executed here. The installed Postman CLI's status command also failed with a macOS keychain error. No Postman workspace, account, provider, or cloud deployment is required for this package.

## Troubleshooting

- `javac` missing: install a JDK, not only a runtime; check PATH.
- `release version 21 not supported`: the compiler is older than 21.
- Port in use: pass another port to run.sh and change the collection's baseUrl.
- Redirect request reaches example.com: disable automatic redirect following in Postman; use curl without `-L` for this exercise.
- Store full: the demo retains at most 1,000 orders and 1,000 links. Restart to reset it.
- HTTP tests fail with a bind permission error: the environment disallows listeners; run core mode there and the full suite in a permitted local environment.

All explanations, exercises, and code in this package were prepared for this course. External readings and videos remain with their original publishers. They are linked, not redistributed.
