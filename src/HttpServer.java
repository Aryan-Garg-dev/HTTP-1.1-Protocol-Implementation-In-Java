import utility.logger.Logger;

import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpServer implements Closeable {
  private volatile boolean running;
  private ServerSocket serverSocket;

  private final int port;
  private final HttpHandler handler;
  private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

  public HttpServer(int port, HttpHandler handler){
    this.port = port;
    this.handler = handler;
  }

  public void listen() throws IOException {
    serverSocket = new ServerSocket(port);
    running = true;

    try {
      while (running) {
        Socket socket = serverSocket.accept();
        executor.execute(new HttpConnection(socket, handler));
      }
    } catch (IOException e) {
      if (running) throw e;
    }
  }

  @Override
  public void close(){
    if (!running) return;
    running = false;
    try {
      serverSocket.close();
    } catch (IOException e) {
      Logger.error().println(e);
    }
    executor.shutdown();
  }

  public static void main(String[] args) {
    try(
      HttpServer server = new HttpServer(42069, (request) -> new Response());
    ){

      Runtime.getRuntime().addShutdownHook(
        new Thread(server::close)
      );

      server.listen();

    } catch (Exception e){
      Logger.error().print(e);
    }
  }
}
