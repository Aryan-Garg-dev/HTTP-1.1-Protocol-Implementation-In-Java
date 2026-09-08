import utility.Evaluators;
import utility.logger.Logger;
import utility.logger.LoggerConfig;
import utility.logger.logsink.LogStreams;

public class Main {
  static {
    Logger.config
      .withTimeStamp(true)
      .withPrefix("[TESTING]")
      .withCaller(true)
      .withLogLevel(true);
  }
  public static void main(String[] args) {
    int WARM_UP = 1000;
    int TEST = 50_000;

    Logger.config.useConsole();
    for (int i = 0; i < WARM_UP; i++) Logger.info().println("Sync Log WARM-UP");

    Logger.config.useAsyncConsole();
    for (int i = 0; i < WARM_UP; i++) Logger.info().println("Async Log WARM-UP");
    Logger.config.closeSink();

    Logger.config.useFile("sync-logs.txt");
    for (int i = 0; i < WARM_UP; i++) Logger.info().println("async Log WARM-UP");

    Logger.config.useFile("async-logs.txt");
    for (int i = 0; i < WARM_UP; i++) Logger.info().println("Async Log WARM-UP");
    Logger.config.closeSink();


    Logger.config.useConsole();
    long syncTimeElapsed = Evaluators.time(() -> {
      for (int i = 0; i < TEST; i++) Logger.info().println("Sync Log TEST");
    });

    Logger.config.useAsyncConsole();
    long asyncFullTimeElapsed = Evaluators.time(() -> {
      for (int i = 0; i < TEST; i++) Logger.info().println("Async Log TEST");
      Logger.config.closeSink();
    });

    Logger.config.useAsyncConsole();
    long asyncTimeElapsed = Evaluators.time(() -> {
      for (int i = 0; i < TEST; i++) Logger.info().println("Async Log TEST");
    });
    Logger.config.closeSink();

    Logger.config.useFile("sync-logs.txt");
    long syncFileTimeElapsed = Evaluators.time(() -> {
      for (int i = 0; i < TEST; i++) Logger.info().println("async Log TEST");
    });

    Logger.config.useAsyncFile("async-logs.txt");
    long asyncFileFullTimeElapsed = Evaluators.time(() -> {
      for (int i = 0; i < TEST; i++) Logger.info().println("Async Log TEST");
      Logger.config.closeSink();
    });

    Logger.config.useAsyncFile("async-logs.txt");
    long asyncFileTimeElapsed = Evaluators.time(() -> {
      for (int i = 0; i < TEST; i++) Logger.info().println("Async Log TEST");
    });
    Logger.config.closeSink();

    Evaluators.report("SYNC Console", syncTimeElapsed, TEST);
    Evaluators.report("ASYNC Console", asyncTimeElapsed, TEST);
    Evaluators.report("ASYNC Full Console", asyncFullTimeElapsed, TEST);
    Evaluators.report("SYNC-FILE Console", syncFileTimeElapsed, TEST);
    Evaluators.report("ASYNC-FILE Console", asyncFileTimeElapsed, TEST);
    Evaluators.report("ASYNC-FILE Full Console", asyncFileFullTimeElapsed, TEST);
  }
}