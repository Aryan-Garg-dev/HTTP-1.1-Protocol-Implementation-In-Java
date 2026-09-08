import utility.Bytes;
import utility.logger.Logger;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class HeaderParser {
  static {
    Logger.config
      .withLogLevel(true)
      .withCaller(true);
  }

  private static final byte[] CRLF = "\r\n".getBytes();
  private Map<String, String> headers;
  private boolean done;

  private static boolean isTokenValid(String token){
    if (token.isBlank()) return false;
    for (char ch: token.toCharArray()){
      if (Character.isLetterOrDigit(ch)) continue;
      switch (ch){
        case '!', '#', '$', '%', '&', '\'', '*', '+',
             '-', '.', '^', '_', '`', '|', '~':
          continue;
        default: return false;
      }
    }
    return true;
  }

  public HeaderParser(){
    headers = new HashMap<>();
    done = false;
  }

  public Map<String, String> getHeaders() {
    return headers;
  }

  public boolean isDone() {
    return done;
  }

  public String get(String name) {
    return headers.get(name.toLowerCase());
  }

  public String getOrDefault(String name, String defaultValue){
    return headers.getOrDefault(name.toLowerCase(), defaultValue);
  }

  public void set(String name, String value) throws Exception {
    if (!isTokenValid(name)) throw new Exception("Malformed header name");
    headers.compute(name.toLowerCase(), (k, v)->{
      if (v == null) return value;
      return String.format("%s,%s", v, value);
    });
  }

  private String[] parseHeader(byte[] fieldLine) throws Exception {
    String[] parts = new String(fieldLine).split(":", 2);
    if (parts.length != 2) throw new Exception("Malformed Field line");
    String name = parts[0];
    String value = parts[1].trim();
    if (name.endsWith(" ") || name.startsWith(" ")) throw new Exception("Malformed Field name");
    return new String[]{ name, value };
  }

  public int parse(byte[] data) throws Exception {
    int consumed = 0;
    while (true){
      int idx = Bytes.indexOf(data, CRLF, consumed);
      if (idx == -1) break;
      if (idx == consumed){
        done = true;
        consumed += CRLF.length;
        break;
      }
      var keyValue = parseHeader(Arrays.copyOfRange(data, consumed, idx));
      set(keyValue[0], keyValue[1]);
      consumed = idx + CRLF.length;
    }
    return consumed;
  }

  public static void main(String[] args) {
    String[] testHeaders = {
      "Host: localhost:42069\r\nFooFoo: barbar\r\n\r\n",
      "Host: localhost:42069\r\nHost: localhost:42069\r\n\r\n",
      "   Host: localhost:42069      \r\n\r\n",
      "H©st: localhost:42069\r\n\r\n",
    };

    for (var header: testHeaders){
      try {
        var h = new HeaderParser();
        h.parse(header.getBytes());
        if (h.done) Logger.log().println(h.headers);
      } catch (Exception e){
        Logger.error().println(e);
      }
    }
  }
}
