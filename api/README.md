# Teaching API contract

`openapi.json` is authoritative. The portable Postman v2.1 collection is a curated sequence derived from its operations, with additional behavioral assertions. Update both when changing the API; the sequence is not an independently versioned contract.

Start the server, import `course.postman_collection.json`, and run all 15 requests in order. Disable **Automatically follow redirects** in Postman. The collection also requests this per request. The examples use only `127.0.0.1`. They do not need authentication, call a provider, or upload results to a workspace.

With a compatible Postman CLI:

```sh
postman collection run api/course.postman_collection.json --no-report-events --ignore-redirects
```

The first request creates a fresh idempotency key. Avoid concurrent manual mutations during the sequence. The local stores have a 1,000-item teaching limit; restart to reset. `GET /limited` is best explored manually because time-sensitive quota assertions would make the sequential collection brittle. Its atomic behavior is tested with a fake clock in SelfTest.

The full Java test suite passed all 49 checks, including local HTTP integration, after listening sockets became available on 6 November 2026. Run `sh scripts/test.sh` locally to reproduce it. JSON syntax and operation mapping were also validated. The Postman collection itself has not been executed.
