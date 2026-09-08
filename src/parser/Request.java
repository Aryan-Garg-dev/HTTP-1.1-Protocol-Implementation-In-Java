package parser;

import java.util.HashMap;
import java.util.Map;

public class Request {
  public record RequestLine(
    String method,
    String requestTarget,
    String httpVersion
  ) {}

  RequestLine requestLine;
  Map<String, String> headers = new HashMap<>();
  String body = "";

  public RequestLine getRequestLine() {
    return requestLine;
  }

  public Map<String, String> getHeaders() {
    return headers;
  }

  public String getBody() {
    return body;
  }

  @Override
  public String toString() {
    return "parser.Request{" +
      "requestLine=" + requestLine +
      ", headers=" + headers +
      ", body=" + body +
      '}';
  }
}
