package course.orders;

import java.net.http.HttpClient;
import java.time.*;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.*;

@Component
public class InventoryClient {
  public enum Outcome {
    RESERVED,
    REJECTED,
    UNKNOWN
  }

  private final RestClient client;

  public InventoryClient(@Value("${inventory.base-url}") String base) {
    var factory =
        new JdkClientHttpRequestFactory(
            HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(1)).build());
    factory.setReadTimeout(Duration.ofSeconds(2));
    client = RestClient.builder().baseUrl(base).requestFactory(factory).build();
  }

  public Outcome reserve(String orderId, long product, int quantity) {
    try {
      client
          .post()
          .uri("/reservations")
          .header("Idempotency-Key", orderId)
          .body(Map.of("productId", product, "quantity", quantity))
          .retrieve()
          .toBodilessEntity();
      return Outcome.RESERVED;
    } catch (HttpClientErrorException e) {
      return e.getStatusCode().value() == 409 || e.getStatusCode().value() == 404
          ? Outcome.REJECTED
          : Outcome.UNKNOWN;
    } catch (RestClientException e) {
      return Outcome.UNKNOWN;
    }
  }
}
