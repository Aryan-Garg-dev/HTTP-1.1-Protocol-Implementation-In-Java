import utility.logger.Logger;


public class Main {
  static {
    Logger.config
      .useAsyncConsole()
      .withTimeStamp(true)
      .withCaller(true)
      .withLogLevel(true);
  }

  public static void main(String[] args) {
    try(
      HttpServer server = new HttpServer(42069, (request) -> {
        Logger.info().println(request);

        String target = request.getRequestLine().requestTarget();

        if (target.equals("/")){
          return new Response(Status.OK)
            .html("""
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