# Practice workbook

Attempt each task before reading the model answer. Implementation exercises require runnable code and evidence; a written answer alone is not completion.

## 01 What makes an application agentic

**Task**
Run Main in scripted mode. Draw the request path and identify every place the application can reject a decision. Compare an agent with a simple if statement for checking order status.

**Model answer and acceptance criteria**
The application validates input, action type, tool name, arguments, trusted identity, output size, number of calls, and elapsed time. For a known order ID and a single status lookup, direct code is simpler. An agent helps when the user describes a problem requiring several information sources.

## 02 Java foundations for AI applications

**Task**
Find every mutable collection in Agent and ApprovalLedger. Add a test that a model cannot add a message to its input history. Explain why synchronized is used in the in-memory ledger.

**Model answer and acceptance criteria**
The engine owns its ArrayList and gives the model an immutable snapshot. The ledger synchronizes a compound check-and-record operation so concurrent approvals produce one receipt. This protects one JVM only; durable cross-instance coordination requires a database transaction or equivalent authority.

## 03 Language models tokens and context

**Task**
Measure prompt length before and after ten retrieved documents are added. Estimate cost for 10,000 requests using a price sheet you verify on the day of measurement. Record both average and worst-case token usage.

**Model answer and acceptance criteria**
Use the actual model tokenizer or provider usage counters, not a fixed words-to-tokens rule. Multiply separately by input and output rates, add repeated agent calls, and state whether caching or batch discounts apply. Keep costs as assumptions until measured.

## 04 Prompts with evidence and clear boundaries

**Task**
Write five support questions: normal status, missing order, another customer’s order, ambiguous request, and prompt injection. Record the expected facts and forbidden claims before trying a model.

**Model answer and acceptance criteria**
Expected answers should be judged against stored facts and policy. The cross-account case must reveal no private order data. The ambiguous case should ask for an order ID. An injection attempt must not add tool permissions. Save exact model and prompt versions with outcomes.

## 05 Structured output and validation

**Task**
Add test inputs for a numeric orderId, a missing type, an unknown tool, an excessively large answer, and an extra tenant argument. Decide which validation layer should reject each.

**Model answer and acceptance criteria**
The adapter rejects non-string argument values. The engine rejects missing or unknown action types and oversized final answers. The tool rejects the extra tenant field. Add a schema for exact top-level fields if you need strict compatibility and version it before changing the contract.

## 06 Tool design and authorization

**Task**
Design a delivery-estimate tool. Specify allowed inputs, the trusted identity source, maximum response size, timeout, and behavior when the delivery service is unavailable.

**Model answer and acceptance criteria**
Accept an order identifier, then resolve the authorized shipment server-side. Return an estimate with timestamp and source, or a typed unavailable result. Reject arbitrary URLs and customer IDs. Use a finite deadline and do not replace missing delivery data with an invented date.

## 07 Bounded agent loops and stopping conditions

**Task**
Make a model return the same tool request forever. Then implement a repeated-action detector and compare it with the absolute call limit. Explain why both may be useful.

**Model answer and acceptance criteria**
The call limit guarantees a finite number of decisions. A repeated-action detector can stop waste earlier, but identical calls may legitimately produce changed results. Include arguments and relevant state in the detector and retain the absolute budget as the reliable outer bound.

## 08 Spring AI integration in Java

**Task**
Compile the optional adapter. If you already have Ollama and a suitable tool-capable model, run it locally. Compare the tool trace with the explicit engine. Add an application deadline and a tool-loop limit before broadening its capabilities.

**Model answer and acceptance criteria**
Use framework hooks documented for the pinned release, and test the limit with a model that repeatedly requests a tool. Preserve trusted account context outside model arguments. Treat compile success, successful inference, and safe behavior under adversarial inputs as three different results.

## 09 Workflows routing and parallel tasks

**Task**
Implement two bounded read tasks using Java 21 virtual threads. Give one task a delayed response and record total latency. Define which failures permit a partial response.

**Model answer and acceptance criteria**
Use a deadline shared by both futures and cancel unfinished work when the response is no longer useful. Restore interrupt status when handling interruption. Limit outbound concurrency independently of the number of virtual threads. A test should verify the partial-result contract, not only that two tasks started.

## 10 Retrieval ingestion and chunking

**Task**
Create ten policy chunks and five questions with known relevant chunk IDs. Measure recall at three: the fraction of relevant chunks found among the first three results. Add a synonym-only question that the lexical baseline misses.

**Model answer and acceptance criteria**
Store relevance labels before changing the ranking function. Report per-question results and aggregate recall, including empty-result cases. Do not improve a metric by removing hard questions. Keep tenant isolation as a binary invariant independent of relevance quality.

## 11 Embeddings vector search and hybrid retrieval

**Task**
Implement a vector-backed retriever using a local embedding model. Store model identifier, dimension, chunk version, and tenant with each vector. Rebuild the index when the model changes.

**Model answer and acceptance criteria**
Use actual embedding outputs rather than random vectors to claim semantic quality. Compare lexical recall, vector recall, hybrid recall, latency, and storage. Test wrong dimensions and cross-tenant near-duplicates. A model migration normally needs separate indexes until the new corpus is ready.

## 12 Grounded answers with retrieval augmented generation

**Task**
Create a 20-question evaluation set with five deliberately unanswerable questions. Grade source correctness, claim support, and appropriate abstention separately. Include one document containing malicious instructions.

**Model answer and acceptance criteria**
Use a human-reviewed answer key for important facts. Automated model graders can assist but need calibration against that key. Count unsupported claims even when the answer is otherwise useful. Preserve the adversarial document as data and verify that it does not change tool permissions.

## 13 Conversation memory and durable state

**Task**
Design a memory record with tenant, user, conversation ID, creation time, expiry, and source. Write tests for two users with the same conversation ID in different tenants and for expired entries.

**Model answer and acceptance criteria**
Use a composite namespace or equivalent authorization predicate. Expired entries must not appear in assembled prompts. Recheck source data before a state-changing action. A deletion request must cover indexes, summaries, and backups according to the chosen retention policy.

## 14 Prompt injection and secure agent boundaries

**Task**
Threat-model a document assistant with file upload and URL fetching. List assets, untrusted inputs, possible tool effects, and an abuse test for each boundary. Explain how redirects and internal IP addresses affect a URL allowlist.

**Model answer and acceptance criteria**
A safe fetcher validates resolved destinations and every redirect, blocks prohibited networks, caps response size and time, and uses narrow egress rules. Uploaded content is parsed in an isolated pipeline. A malicious document never gains tool permissions simply by being retrieved.

## 15 Human approval idempotency and actions

**Task**
Extend the ledger with a database schema for proposal and execution states. Define how to recover after a crash between provider success and local receipt storage. Include a changed-amount attack in the tests.

**Model answer and acceptance criteria**
Persist the exact proposal, human identity, approval version, and provider idempotency key. After restart, reconcile an IN_PROGRESS or UNKNOWN execution with the provider. Never create a new key merely because the previous HTTP call timed out. Eligibility checks and amount limits remain separate from approval storage.

## 16 Evaluation datasets and regression testing

**Task**
Build a CSV with 30 cases and columns for tool accuracy, groundedness, access violations, calls, elapsed milliseconds, and tokens. Define a release threshold before running the experiment.

**Model answer and acceptance criteria**
Treat any access violation as a release blocker rather than averaging it away. Report task success with numerator and denominator, and keep cost and latency distributions. A threshold such as 90 percent success is a project choice, not a universal industry standard.

## 17 Resilience budgets and cost control

**Task**
Simulate a slow model endpoint and a 503 response. Add at most one retry for a read-only model request, with a remaining-budget check. Measure how retry traffic behaves under ten concurrent users.

**Model answer and acceptance criteria**
Stop when the remaining budget cannot support another attempt. Avoid retrying validation errors or unauthorized requests. Compare success rate against added traffic and latency. A successful retry policy should improve useful outcomes without amplifying an outage into uncontrolled load.

## 18 MCP and cooperation between agents

**Task**
Write an MCP tool contract for read-only order lookup and a threat model for an untrusted server. Then compare a single-agent answer with a three-stage workflow on the same evaluation cases.

**Model answer and acceptance criteria**
Record extra model calls, latency, task success, and failure modes. Keep credentials out of prompts and distinguish transport authentication from business authorization. If the multi-agent version is slower without a measurable quality gain, retain the simpler workflow.

## 19 Observability deployment and operations

**Task**
Design a dashboard and three alerts for the support assistant. Create a deployment checklist including model rollout, evaluation gates, rollback, retention, and emergency tool disablement.

**Model answer and acceptance criteria**
Alert on a sustained increase in failed tasks or unsupported claims, exhausted provider quota, and authorization violations. Choose thresholds from a baseline and business impact. Tool disablement should stop risky effects without requiring every prompt to be rewritten.

## 20 Capstone projects and interview preparation

**Task**
Give a 15-minute design explanation: five minutes for requirements and architecture, five for failure handling, and five for evaluation and tradeoffs. Have another person introduce a malicious document and a provider timeout.

**Model answer and acceptance criteria**
A strong response preserves tenant isolation, rejects invented actions, bounds work, reports uncertainty, and explains recovery. Score yourself out of five on correctness, evidence, failure handling, clarity, and tradeoffs. Redo the weakest section with a concrete test or measurement.
