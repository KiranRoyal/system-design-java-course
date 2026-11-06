# Run the Agentic AI Java labs

Requirements: JDK 21 and Maven 3.6.3 or newer. Verify `java -version` and `mvn -version` use the intended JDK. The first build downloads public Maven Central dependencies. `mvn-course.sh` uses empty course settings and a course-local dependency cache, so it does not rely on office Maven credentials.

From this directory:

```sh
sh mvn-course.sh test
sh mvn-course.sh exec:java -Dexec.mainClass=course.agent.Main
```

Expected: SCRIPTED MODE, an authorized A100 lookup, policy evidence, a delayed-order answer, and three model calls. No paid account or live model is required.

## Optional real local model

Install Ollama separately and download a model suitable for JSON output. Choose one your machine can run; model downloads may be large. Start its local server, set the installed model name, and run:

```sh
export OLLAMA_MODEL='your-installed-model-name'
sh mvn-course.sh exec:java -Dexec.mainClass=course.agent.Main -Dexec.args=ollama
```

The adapter calls only loopback HTTP. It requests JSON decisions and uses the same validated engine. Do not interpret scripted-mode tests as a live-model quality evaluation. No model was downloaded or live inference performed while preparing this course.

## Optional Spring AI framework example

From the track directory:

```sh
mvn -s code/settings.xml -Dmaven.repo.local=code/.local-data/m2 -f spring-ai-adapter/pom.xml package
export OLLAMA_MODEL='your-installed-tool-capable-model'
mvn -s code/settings.xml -Dmaven.repo.local=code/.local-data/m2 -f spring-ai-adapter/pom.xml spring-boot:run -Dspring-boot.run.profiles=live-demo
```

This separate example compiles against Spring AI 2.0.2 and Boot 4.0.9. It exposes a single synthetic read-only tool. It does not reuse the custom engine's step budget and is not an example of production authorization. Add and test framework loop limits, timeouts, and account checks before extending it.

## Source map

- `Agent.java`: bounded loop, action validation, history snapshots, deadlines.
- `SupportTools.java`: exact tool argument checks and trusted tenant/owner filtering.
- `OllamaModel.java`: optional local HTTP model adapter and JSON parsing.
- `Retriever.java`: lexical baseline, tenant filtering, cosine arithmetic.
- `ApprovalLedger.java`: exact proposal binding and concurrent simulated approvals.
- `AgentTest.java`: deterministic invariants and real HTTP adapter fixtures.

## Boundaries

The stores are in memory. Approval records are simulated decisions, not refunds. Retrieval is lexical unless you implement the embedding extension. No persistent memory, MCP server, broker, or multi-agent runtime is included. The HTTP response-size check occurs after buffering; add an ingress/streaming cap for hostile providers. Deadline checks between steps do not preempt a blocked custom tool.
