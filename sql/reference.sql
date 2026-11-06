-- PostgreSQL reference schema for the advanced extension. NOT wired into the Java app.
-- Apply only in a new disposable learning database; these names are intentionally prefixed.
-- No real provider calls or payment data belong in this exercise.
CREATE TABLE course_inventory (
    tenant_id text NOT NULL,
    sku text NOT NULL,
    available integer NOT NULL CHECK (available >= 0),
    version bigint NOT NULL DEFAULT 0,
    PRIMARY KEY (tenant_id, sku)
);
CREATE TABLE course_orders (
    id uuid PRIMARY KEY,
    tenant_id text NOT NULL,
    customer_id text NOT NULL,
    amount_cents bigint NOT NULL CHECK (amount_cents > 0),
    currency char(3) NOT NULL,
    status text NOT NULL CHECK (status IN ('ACCEPTED','RESERVED','CONFIRMED','CANCELLED')),
    created_at timestamptz NOT NULL DEFAULT now(),
    UNIQUE (tenant_id, id)
);
CREATE INDEX course_orders_customer_page
    ON course_orders (tenant_id, customer_id, created_at DESC, id DESC);
CREATE TABLE course_idempotency (
    tenant_id text NOT NULL,
    customer_id text NOT NULL,
    operation text NOT NULL,
    request_key text NOT NULL,
    request_fingerprint text NOT NULL,
    order_id uuid NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now(),
    PRIMARY KEY (tenant_id, customer_id, operation, request_key),
    FOREIGN KEY (tenant_id, order_id) REFERENCES course_orders (tenant_id, id)
);
CREATE TABLE course_outbox (
    event_id uuid PRIMARY KEY,
    tenant_id text NOT NULL,
    order_id uuid NOT NULL,
    event_type text NOT NULL,
    schema_version integer NOT NULL,
    payload jsonb NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now(),
    published_at timestamptz,
    FOREIGN KEY (tenant_id, order_id) REFERENCES course_orders (tenant_id, id)
);
CREATE INDEX course_outbox_pending ON course_outbox (created_at, event_id)
    WHERE published_at IS NULL;
CREATE TABLE course_processed_events (
    consumer_name text NOT NULL,
    event_id uuid NOT NULL,
    processed_at timestamptz NOT NULL DEFAULT now(),
    PRIMARY KEY (consumer_name, event_id)
);

-- TRANSACTION RECIPE (pseudocode; ? means a JDBC bound parameter):
-- BEGIN
-- INSERT order, then INSERT idempotency row with tenant/customer/operation/key.
-- If the idempotency unique constraint conflicts, ROLLBACK the entire attempt,
-- then load the committed existing request in a fresh transaction and compare
-- the canonical fingerprint. Return its result or 409 for a different request.
-- UPDATE course_inventory SET available = available - ?, version = version + 1
--   WHERE tenant_id = ? AND sku = ? AND available >= ?;
-- Require affected-row count = 1; otherwise ROLLBACK (out of stock).
-- INSERT course_outbox (...); COMMIT.
-- All order, key, reservation and outbox work must share the same transaction.

-- CURSOR RECIPE:
-- SELECT * FROM course_orders
-- WHERE tenant_id = ? AND customer_id = ?
--   AND (created_at, id) < (?, ?)
-- ORDER BY created_at DESC, id DESC LIMIT ?;
-- First page omits the cursor predicate. Bind values; cap the limit.

-- CONSUMER RECIPE:
-- BEGIN
-- INSERT INTO course_processed_events (consumer_name, event_id) VALUES (?, ?)
--   ON CONFLICT DO NOTHING RETURNING event_id;
-- If no row returned: duplicate; COMMIT without repeating effect.
-- Otherwise apply the local database effect in this SAME transaction; COMMIT.
-- A failure must roll back both the identifier and the effect.

-- RELAY EXERCISE:
-- For a simple learning relay, select a bounded pending batch in a transaction
-- using FOR UPDATE SKIP LOCKED; publish, then mark sent and commit.
-- This can hold database locks during broker latency and still publishes
-- duplicates after interruption. A production extension should evaluate a
-- short claim transaction, expiring ownership plus fencing, or CDC. Do not
-- label a selected row sent before the broker acknowledges its required write.
