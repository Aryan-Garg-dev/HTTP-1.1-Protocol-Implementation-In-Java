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
    Logger.info().print("Server is listening on PORT: ").println(port);
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
      HttpServer server = new HttpServer(42069, (request) -> {
        Logger.info().println(request);

        String target = request.getRequestLine().requestTarget();

        if (target.equals("/")){
          return new Response(Status.OK)
            .header("Content-Type", "text/html")
            .header("charset", "utf-8")
            .text("""
              <html>
                <head>
                  <title>HTTP/1.1</title>
                  <meta charset="UTF-8" />
                  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
                  <script src="https://cdn.jsdelivr.net/npm/@tailwindcss/browser@4"></script>
                  <link rel="preconnect" href="https://fonts.googleapis.com">
                  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
                  <link href="https://fonts.googleapis.com/css2?family=DM+Sans:ital,opsz,wght@0,9..40,100..1000;1,9..40,100..1000&family=Josefin+Sans:ital,wght@0,100..700;1,100..700&family=Outfit:wght@100..900&display=swap" rel="stylesheet">
                </head>
                <style>
                  .font-outfit {
                    font-family: "Outfit", sans-serif;
                    font-optical-sizing: auto;
                    font-style: normal;
                  }
                  .font-sans {
                    font-family: "Josefin Sans", sans-serif;
                    font-optical-sizing: auto;
                    font-style: normal;
                  }
                </style>
                <body class="w-full h-full min-h-screen flex flex-col justify-center items-center font-sans bg-neutral-900 text-neutral-200">
                  <h1 class="text-3xl font-bold">HTTP/1.1 Implementation in Java using TCP Sockets</h1>
                </body>
              </html>
              """);
        }

        String text;
        Status status = Status.OK;

        if (target.endsWith("ping")) text = "pong";
        else if (target.endsWith("health")) text = "healthy";
        else {
          status = Status.NOT_FOUND;
          text = status.reason();
        }

        return new Response(status)
          .header("Content-Type", "text/plain")
          .text(text);
      });
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
