package course.springai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.context.annotation.*;

@SpringBootApplication
public class Application {
  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

  @Bean
  @Profile("live-demo")
  CommandLineRunner demo(ChatClient.Builder builder) {
    return args -> {
      var client =
          builder
              .defaultSystem(
                  "Use the read-only tool for order facts. This is a local synthetic-data teaching"
                      + " demo. Do not invent facts or claim to issue refunds.")
              .build();
      System.out.println(
          client
              .prompt()
              .user("What is the status of A100?")
              .tools(new ReadOnlyTools())
              .call()
              .content());
    };
  }

  public static class ReadOnlyTools {
    @Tool(description = "Read the status of the only synthetic order in this local demo")
    public String orderStatus(String orderId) {
      return "A100".equals(orderId) ? "source=demo/A100; status=DELAYED" : "No accessible order";
    }
  }
}
