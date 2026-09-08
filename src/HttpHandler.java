@FunctionalInterface
public interface HttpHandler {
  Response handle(Request request);
}
