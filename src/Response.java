import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class Response {

  private final Status status;
  private final Map<String, String> headers;
  private byte[] body;

  public Response(Status status) {
    this.status = status;
    this.headers = new LinkedHashMap<>();
    this.body = new byte[0];
  }

  public Response header(String key, String value) {
    if (!key.isBlank() && !value.isBlank()) {
      headers.put(key, value);
    }
    return this;
  }

  public Response body(byte[] body) {
    this.body = body;
    return this;
  }

  public Response text(String body){
    this.body = body.getBytes(StandardCharsets.UTF_8);
    return this;
  }

  public Response html(String html){
    this.headers.put("Content-Type", "text/html");
    this.headers.put("charset", "utf-8");
    this.body = html.getBytes(StandardCharsets.UTF_8);
    return this;
  }

  @Override
  public String toString() {
    return "Response{" +
      "status=" + status +
      ", headers=" + headers +
      ", body=" + Arrays.toString(body) +
    '}';
  }

  public void writeTo(OutputStream out) throws IOException {
    headers.put("Content-Length", String.valueOf(body.length));

    String statusLine = "HTTP/1.1 " + status.code() + " " + status.reason() + "\r\n";

    out.write(statusLine.getBytes(StandardCharsets.US_ASCII));
    for (Map.Entry<String, String> header : headers.entrySet()) {
      String line = header.getKey() + ": " + header.getValue() + "\r\n";
      out.write(line.getBytes(StandardCharsets.US_ASCII));
    }
    out.write("\r\n".getBytes(StandardCharsets.US_ASCII));
    out.write(body);

    out.flush();
  }
}