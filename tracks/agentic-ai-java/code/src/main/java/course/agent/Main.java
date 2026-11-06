package course.agent;

import java.net.URI;
import java.time.*;
import java.util.*;

public class Main {
  public static void main(String[] args) throws Exception {
    boolean live = args.length > 0 && args[0].equals("ollama");
    Agent.Model model;
    if (live) {
      String name = System.getenv("OLLAMA_MODEL");
      if (name == null || name.isBlank())
        throw new IllegalArgumentException(
            "Set OLLAMA_MODEL to a model already installed in Ollama");
      model =
          new OllamaModel(
              URI.create("http://127.0.0.1:11434/api/chat"), name, Duration.ofSeconds(20));
    } else {
      var steps =
          new ArrayDeque<>(
              List.of(
                  Agent.Decision.tool("lookupOrder", Map.of("orderId", "A100")),
                  Agent.Decision.tool("searchPolicy", Map.of("query", "delayed order")),
                  Agent.Decision.finish(
                      "Order A100 is delayed [orders/A100]. Policy v1 requires support review; no"
                          + " refund has been issued [policy/v1].")));
      model = messages -> steps.remove();
    }
    Agent agent =
        new Agent(
            model, new SupportTools().registry(), 5, Clock.systemUTC(), Duration.ofSeconds(60));
    var result =
        agent.run(
            new Agent.Context("red", "alice"), "Where is order A100 and what can support do?");
    System.out.println(
        live
            ? "LOCAL MODEL MODE"
            : "SCRIPTED MODE: deterministic loop demonstration, no LLM inference");
    result.trace().stream()
        .filter(m -> m.text().startsWith("UNTRUSTED"))
        .forEach(m -> System.out.println(m.text()));
    System.out.println(result.answer());
    System.out.println("Model calls: " + result.modelCalls());
    var store =
        new Retriever(
            List.of(
                new Retriever.Chunk("red-1", "red", "Delayed order support review"),
                new Retriever.Chunk("blue-1", "blue", "Private blue refund policy")));
    System.out.println("Tenant filtered retrieval: " + store.search("red", "delayed order", 3));
  }
}
