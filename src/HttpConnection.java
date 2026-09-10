import parser.Request;
import parser.RequestReader;
import utility.logger.Logger;

import java.io.Closeable;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class HttpConnection implements Runnable, Closeable {
  private final Socket socket;
  private final HttpHandler handler;

  HttpConnection(
    Socket socket,
    HttpHandler handler
  ){
    this.handler = handler;
    this.socket = socket;
  }

  @Override
  public void run(){
    try (socket){
      InputStream in = socket.getInputStream();
      OutputStream out = socket.getOutputStream();
      RequestReader requestReader = new RequestReader(in);
      while (true){
        Request request = requestReader.readRequest();
        if (request == null) break;
        Response response = handler.handle(request);
        response.writeTo(out);
        // keep-alive check
      }
    } catch (InterruptedException e){
      Thread.currentThread().interrupt();
      Logger.error().println(e);
    } catch (Exception e){
      Logger.error().println(e);
    }
  }

  @Override
  public void close(){
   try {
     socket.close();
   } catch (Exception e){
     Logger.error().println(e);
   }
  }
}
