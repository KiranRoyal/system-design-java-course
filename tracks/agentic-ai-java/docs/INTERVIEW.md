# Interview questions with model answers

Answer in your own words and connect each answer to a test or failure scenario. These are practice prompts, not a prediction of a particular company’s interview.

## 01 When would you avoid an agent?

Topic: 01 What makes an application agentic

When a deterministic rule or fixed workflow satisfies the requirement more cheaply and predictably.

## 02 Who executes a tool call?

Topic: 01 What makes an application agentic

The application. Model output is a request for execution, not an authorization grant.

## 03 Why inject Clock?

Topic: 02 Java foundations for AI applications

Expiry and deadlines become deterministic test inputs instead of tests that sleep.

## 04 Does a record make all contained data immutable?

Topic: 02 Java foundations for AI applications

No. Its references are final, but a mutable collection held by a record can still change unless copied or wrapped appropriately.

## 05 Does a large context window solve memory?

Topic: 03 Language models tokens and context

It supplies a larger temporary input budget; persistence, selection, access control, and forgetting remain application responsibilities.

## 06 Why can low temperature still produce an incorrect answer?

Topic: 03 Language models tokens and context

Sampling controls variability, not whether the prompt contains enough evidence or whether the model learned the fact correctly.

## 07 Is a system prompt a security boundary?

Topic: 04 Prompts with evidence and clear boundaries

No. Enforce security in deterministic application code and downstream services.

## 08 What should an explanation expose?

Topic: 04 Prompts with evidence and clear boundaries

Relevant evidence, assumptions, decision criteria, and user-facing rationale; it need not expose private internal deliberation.

## 09 Why validate after schema-constrained generation?

Topic: 05 Structured output and validation

Provider capabilities differ and schemas cannot encode every authorization or business rule.

## 10 Should malformed model JSON produce a successful user response?

Topic: 05 Structured output and validation

No. Return a controlled failure or a bounded retry, and record a parse or validation metric.

## 11 Why avoid a generic SQL tool?

Topic: 06 Tool design and authorization

Its authority is broad, schema exposure can be large, and safely constraining queries and effects is much harder.

## 12 Where should tenant filtering occur?

Topic: 06 Tool design and authorization

At the trusted query or service boundary before data reaches the model, with downstream authorization still enforced.

## 13 Does a circuit breaker replace a deadline?

Topic: 07 Bounded agent loops and stopping conditions

No. A breaker controls admission after failures; each admitted call still needs a timeout.

## 14 What is a correct budget-exhausted result?

Topic: 07 Bounded agent loops and stopping conditions

A controlled incomplete outcome with useful available evidence, not a fabricated successful answer.

## 15 Why retain a plain Java engine in a Spring AI course?

Topic: 08 Spring AI integration in Java

It makes orchestration, validation, and stopping behavior explicit before framework abstractions hide those details.

## 16 Does a tool annotation authorize the caller?

Topic: 08 Spring AI integration in Java

No. It exposes a callable capability; authorization must be implemented separately.

## 17 When is parallelization wrong?

Topic: 09 Workflows routing and parallel tasks

When steps depend on prior results, share an unsafe resource, or overload a constrained dependency.

## 18 Does adding more agents guarantee a better answer?

Topic: 09 Workflows routing and parallel tasks

No. It adds coordination, latency, and correlated error opportunities; compare against a simpler baseline.

## 19 Why keep source versions?

Topic: 10 Retrieval ingestion and chunking

An answer must be traceable to the policy that was active when it was produced, and updates need re-indexing.

## 20 Is chunk overlap always beneficial?

Topic: 10 Retrieval ingestion and chunking

No. It can preserve boundary context but also duplicates evidence and increases index and prompt size.

## 21 Why can vector search miss a product code?

Topic: 11 Embeddings vector search and hybrid retrieval

Embedding similarity may not preserve exact string identity; lexical matching is often useful alongside it.

## 22 Does nearest mean relevant?

Topic: 11 Embeddings vector search and hybrid retrieval

No. An index always has a nearest candidate even when no candidate answers the question; use evaluation and an abstention policy.

## 23 Does RAG eliminate hallucinations?

Topic: 12 Grounded answers with retrieval augmented generation

No. It improves access to relevant evidence but retrieval, interpretation, and generation can still fail.

## 24 When should the assistant abstain?

Topic: 12 Grounded answers with retrieval augmented generation

When authorized evidence cannot support the requested claim or the available sources conflict without a resolvable rule.

## 25 Why not save the entire transcript forever?

Topic: 13 Conversation memory and durable state

It increases cost, privacy exposure, stale instructions, and irrelevant context.

## 26 Can a model-generated summary authorize a payment?

Topic: 13 Conversation memory and durable state

No. Authorization must refer to exact trusted state and the specific permitted action.

## 27 Can another agent’s output be trusted automatically?

Topic: 14 Prompt injection and secure agent boundaries

No. It is another untrusted input unless verified through an explicit contract and authorization boundary.

## 28 What is least privilege for tools?

Topic: 14 Prompt injection and secure agent boundaries

Expose only the smallest capabilities and data scope required by the current authenticated task.

## 29 Why must approval expire?

Topic: 15 Human approval idempotency and actions

The business state and user intent may change; a stale approval should not grant indefinite authority.

## 30 Does synchronized provide crash safety?

Topic: 15 Human approval idempotency and actions

No. It coordinates threads in one process and loses its in-memory state on restart.

## 31 Why is exact string matching often inadequate?

Topic: 16 Evaluation datasets and regression testing

Several phrasings can express the same supported answer; compare required facts and prohibited behavior.

## 32 What does a mocked model test establish?

Topic: 16 Evaluation datasets and regression testing

It establishes orchestration behavior for supplied decisions, not the quality of real model decisions.

## 33 What is the unit of AI cost optimization?

Topic: 17 Resilience budgets and cost control

Cost per successfully completed task under the required quality and safety constraints, not merely cost per token.

## 34 Why can retries make outages worse?

Topic: 17 Resilience budgets and cost control

They multiply traffic when the dependency has the least spare capacity.

## 35 Does MCP solve authorization?

Topic: 18 MCP and cooperation between agents

No. It standardizes interaction; the application and server must enforce access policy.

## 36 What prevents agents from endlessly delegating?

Topic: 18 MCP and cooperation between agents

A coordinator-owned task graph, bounded depth and calls, explicit completion criteria, and deadlines.

## 37 What makes an AI trace useful?

Topic: 19 Observability deployment and operations

It connects a specific outcome to versioned inputs, decisions, evidence, and dependency timings while protecting sensitive data.

## 38 Why can a prompt-only release need rollback?

Topic: 19 Observability deployment and operations

It can alter tool selection, answer quality, token usage, latency, and failure behavior even when Java code is unchanged.

## 39 How would you design an agent for every business use case?

Topic: 20 Capstone projects and interview preparation

Start by rejecting the premise of one universal design. Classify read-only versus state-changing tasks, risk, data access, latency, and correctness needs, then choose suitable workflows.

## 40 What should you show in a portfolio demo?

Topic: 20 Capstone projects and interview preparation

A working happy path and a meaningful failure path, with commands, tests, evaluation evidence, and honest limits.
