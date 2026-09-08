import utility.Bytes;
import utility.logger.Logger;

import java.io.InputStream;
import java.util.Arrays;
import java.util.regex.Pattern;

public class RequestReader {
  private static final int BUFFER_SIZE = 1024;
  private static final byte[] CRLF = "\r\n".getBytes();
  private static final String SUPPORTED_HTTP_VERSION = "1.1";
  private static final Pattern CAP_ALPHA = Pattern.compile("[A-Z]+");


  private static boolean validateRegex(String string, Pattern pattern){
    return string != null && pattern.matcher(string).matches();
  }

  private static boolean validateMethod(String methodName){
    return validateRegex(methodName, CAP_ALPHA);
  }

  private static boolean validateHttpVersion(String httpVersion){
    return httpVersion.equals(SUPPORTED_HTTP_VERSION);
  }

  private boolean hasBody(){
    return Integer.parseInt(headerParser.getOrDefault("Content-Length", "0")) != 0;
  }


  private final HeaderParser headerParser = new HeaderParser();
  private final StringBuilder reqBodyBuilder = new StringBuilder();
  private final InputStream inputStream;

  private ParserState state;
  private Request request;


  public RequestReader(InputStream in){
    inputStream = in;
  }

  public Request readRequest() throws Exception {
    request = new Request();
    state = ParserState.INIT;

    byte[] buffer = new byte[BUFFER_SIZE];
    int bufLen = 0;

    while (state != ParserState.DONE){
      int bytesRead = inputStream.read(buffer, bufLen, BUFFER_SIZE - bufLen);
      if (bytesRead == -1) {
        if (state == ParserState.BODY)
          throw new Exception("Length of body falls shorter than specified Content-Length");
      }

      bufLen += bytesRead;
      int bytesConsumed = this.parse(Arrays.copyOfRange(buffer, 0, bufLen));

      if (bytesConsumed > 0)
        System.arraycopy(buffer, bytesConsumed, buffer, 0, bufLen - bytesConsumed);
      bufLen -= bytesConsumed;
    }

    return request;
  }

  private int parse(byte[] buffer) throws Exception {
    int consumed = 0;

    outer:
    while (true){
      byte[] currentData = Arrays.copyOfRange(buffer, consumed, buffer.length);

      switch (state) {
        case INIT: {
          int bytesConsumed = parseRequestLine(currentData);
          if (bytesConsumed == 0) break outer;
          consumed += bytesConsumed;
          state = ParserState.HEADERS;
          break;
        }

        case HEADERS: {
          int bytesConsumed = headerParser.parse(currentData);
          if (bytesConsumed == 0) break outer;

          if (headerParser.isDone()) {
            request.headers = headerParser.getHeaders();

            if (hasBody()){
              state = ParserState.BODY;
            } else {
              state = ParserState.DONE;
            }
          }

          consumed += bytesConsumed;
          break;
        }

        case BODY: {
          int contentLength = Integer.parseInt(headerParser.getOrDefault("Content-Length", "0"));
          if (contentLength == 0) {
            state = ParserState.DONE;
            break;
          }

          int remaining = Math.min(contentLength - reqBodyBuilder.length(), currentData.length);

          if (contentLength == reqBodyBuilder.length()){
            request.body = reqBodyBuilder.toString();
            state = ParserState.DONE;
            break;
          }

          if (currentData.length == 0) break outer;

          var data = new String(Arrays.copyOfRange(currentData, 0, remaining));
          reqBodyBuilder.append(data);

          consumed += remaining;
          break;
        }

        case DONE:
          if (currentData.length > 0){
            if (!hasBody()) throw new Exception("No Content-Length specified but Body Exists");
            throw new Exception("Length of body exceed specified Content-Length");
          }
          break outer;

        default: throw new Exception();
      }
    }

    return consumed;
  }

  private int parseRequestLine(byte[] buffer) throws Exception {
    int idx = Bytes.indexOf(buffer, CRLF);
    if (idx == -1) return 0;

    String requestLine = new String(Arrays.copyOfRange(buffer, 0, idx));
    int consumed = idx + CRLF.length;

    String[] parts = requestLine.split(" ");
    if (parts.length != 3) throw new Exception("Invalid request line: " + requestLine);

    String methodName = parts[0];
    String requestTarget = parts[1];

    String[] httpVersionParts = parts[2].split("/");
    if (
      !httpVersionParts[0].equals("HTTP") ||
        httpVersionParts.length != 2
    ) throw new Exception("Invalid request line: " + requestLine);

    String httpVersion = httpVersionParts[1];

    if (!validateMethod(methodName)) throw new Exception("Invalid method name: " + methodName);
    if (!validateHttpVersion(httpVersion)) throw new Exception("Http version not supported: " + httpVersion);

    this.request.requestLine = new Request.RequestLine(
      methodName,
      requestTarget,
      httpVersion
    );
    return consumed;
  }

  public static void main(String[] args){
    String[] requests = {
      // no content-length, empty body
      "GET / HTTP/1.1\r\nHost: localhost:42069\r\nUser-Agent: curl/7.81.0\r\nAccept: */*\r\n\r\n",

      // content-length: 0, empty body
      "GET / HTTP/1.1\r\nHost: localhost:42069\r\nUser-Agent: curl/7.81.0\r\nContent-Length: 0\r\nAccept: */*\r\n\r\n",

      // Valid
      "GET /coffee HTTP/1.1\r\nHost: localhost:42069\r\nUser-Agent: curl/7.81.0\r\nContent-Length: 13\r\nAccept: */*\r\n\r\n{\"foo\":\"bar\"}",

      // No content-length, but the body exists
      "GET /coffee HTTP/1.1\r\nHost: localhost:42069\r\nUser-Agent: curl/7.81.0\r\nAccept: */*\r\n\r\n{\"foo\":\"bar\"}",

      // Body > content-length
      "GET /coffee HTTP/1.1\r\nHost: localhost:42069\r\nUser-Agent: curl/7.81.0\r\nContent-Length: 13\r\nAccept: */*\r\n\r\n{\"foo\":\"barbar\"}",

      // Body < content-length
      "GET /coffee HTTP/1.1\r\nHost: localhost:42069\r\nUser-Agent: curl/7.81.0\r\nContent-Length: 16\r\nAccept: */*\r\n\r\n{\"foo\":\"bar\"}",

      // Malformed header name
      "GET / HTTP/1.1\r\nHost localhost:42069\r\n\r\n",

      // No method specified
      "/coffee HTTP/1.1\r\nHost: localhost:42069\r\nUser-Agent: curl/7.81.0\r\nAccept: */*\r\n\r\n"
    };
    for (String request: requests){
      var reader = new RequestReader(new ChunkReader(request.getBytes(), 3));

      Request req;
      try {
        req = reader.readRequest();
        Logger.debug().println(req);
      } catch (Exception e){
        Logger.error().println(e);
      }
    }
  }
}
