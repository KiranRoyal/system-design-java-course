# Run the Java Spring Boot Hibernate and microservices labs

Requirements: JDK 21 and Maven 3.6.3 or newer. These are synthetic local learning services bound to `127.0.0.1`; the default configuration has no authentication and resets its H2 data on restart. Keep it local. Complete the security and persistence extensions before exposing an instance.

`mvn-course.sh` uses empty course settings and a local dependency cache, avoiding reliance on office Maven credentials. The initial build downloads public dependencies.

```sh
sh mvn-course.sh test
sh mvn-course.sh package
```

Start two terminals from this directory:

```sh
java -jar inventory/target/inventory-1.0.0.jar
```

```sh
java -jar orders/target/orders-1.0.0.jar
```

Then:

```sh
curl -s http://127.0.0.1:18081/products
curl -s -X POST http://127.0.0.1:18082/orders \
  -H 'Content-Type: application/json' -H 'Idempotency-Key: checkout-001' \
  -d '{"productId":1,"quantity":2}'
```

Expected order status: CONFIRMED. Repeating the same request returns the same order and leaves stock at eight. Reusing the key with quantity three returns 409. Stop inventory and submit a new checkout: its status remains PENDING. Restart inventory and repeat that same checkout to reconcile. Because H2 resets on restart, use this only to study the flow, not to claim durable recovery.

## Automated HTTP smoke test

After packaging, run `python3 scripts/http_smoke.py`. It starts and stops its own two instances on temporary ports and checks the public HTTP contract, duplicate checkout, conflict, invalid quantity, unknown product, stock exhaustion, and pagination. It refuses to substitute existing processes.

## Inventory dependency simulation

The Postman mock is entirely local and needs no account:

```sh
postman mock run postman/mocks/inventory --port 18083 --no-history
```

Start orders with `--inventory.base-url=http://127.0.0.1:18083`. The mock supports stateful reservations and a local `PUT /__scenario` control accepting `{"mode":"success"}`, `{"mode":"outage"}`, or `{"mode":"delay"}`. That control is a test harness extension, not part of the real inventory API. Run `python3 scripts/mock_contract.py` to check the mock contract. Run `python3 scripts/simulation_smoke.py` to exercise the real order service against it, including timeout and recovery. Do not deploy this test mock.

## PostgreSQL extension

Provision separate local PostgreSQL databases for inventory and orders. In each service's terminal set DB_URL, DB_USER, and DB_PASSWORD, then add `--spring.profiles.active=postgres` to its Java command. Do not put credentials in source control. The provided migrations and profile are included, but PostgreSQL execution is not claimed by the H2 test results.

## Implemented and extension boundaries

Implemented: DTO validation, bounded pagination, JPA persistence, Flyway migrations, stock locking, optimistic versioning, database-backed idempotency, two-service HTTP calls, timeout-to-pending behavior, and transactional outbox storage.

Advanced exercises: order-line relationship mapping, real PostgreSQL verification, authentication, tenant ownership, outbox relay and broker consumer, scheduled reconciliation, payment saga and compensation, tracing export, container deployment, and capacity testing. The outbox storage alone is not event delivery. No billing or real payments occur.
