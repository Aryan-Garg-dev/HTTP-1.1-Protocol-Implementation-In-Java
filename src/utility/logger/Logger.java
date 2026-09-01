package utility.logger;

import java.lang.reflect.Array;
import java.time.LocalDateTime;

public class Logger {
  private static final String RESET = "\u001B[0m";
  private static final String DEBUG = "\u001B[34m"; // Blue
  private static final String INFO  = "\u001B[36m"; // Cyan
  private static final String WARN  = "\u001B[33m"; // Yellow
  private static final String ERROR = "\u001B[31m"; // Red

  enum Level {
    LOG, WARN, ERROR, DEBUG, INFO
  }


  public static volatile LoggerConfig config = new LoggerConfig();

  private final Level currentLevel;
  private final String prefix;
  private boolean prefixEmitted = false;
  private final LoggerConfig instanceConfig;

  Logger() {
    this(Level.LOG, config);
  }

  Logger(Level level){
    this(level, config);
  }

  Logger(Level level, LoggerConfig config){
    this.currentLevel = level;
    this.instanceConfig = config;
    this.prefix = buildPrefix(level);
  }


  public static Logger log()   { return new Logger(Level.LOG); }
  public static Logger error() { return new Logger(Level.ERROR); }
  public static Logger warn()  { return new Logger(Level.WARN); }
  public static Logger info()  { return new Logger(Level.INFO); }
  public static Logger debug() { return new Logger(Level.DEBUG); }

  private String colorize(String message) {
    if (!instanceConfig.getSink().supportsColor()) return message;
    return switch (currentLevel) {
      case WARN -> WARN + message + RESET;
      case ERROR -> ERROR + message + RESET;
      case DEBUG -> DEBUG + message + RESET;
      case INFO -> INFO + message + RESET;
      default -> message;
    };
  }

  private String buildPrefix(Level level) {
    StringBuilder sb = new StringBuilder();
    if (instanceConfig.canShowTimeStamp()) {
      sb.append('[').append(
          LocalDateTime.now().format(instanceConfig.getDateTimeFormat())
      ).append("] ");
    }
    if (instanceConfig.canShowLogLevel()){
      sb.append('[').append(level.toString()).append("] ");
    }
    if (instanceConfig.canShowCaller()) {
      sb.append('[').append(resolveCaller()).append("] ");
    }
    if (!instanceConfig.getPrefix().isBlank()){
      sb.append(instanceConfig.getPrefix()).append(" ");
    }
    return sb.toString();
  }

  private String resolveCaller() {
    return StackWalker.getInstance()
      .walk(frames -> frames
        .dropWhile(f ->
          f.getClassName().equals(Logger.class.getName()) ||
          f.getClassName().equals(LoggerConfig.class.getName())
        ).findFirst())
      .map(f -> {
        String cls = f.getClassName();
        String simple = cls.substring(cls.lastIndexOf('.') + 1);
        return simple + "." + f.getMethodName();
      })
      .orElse("unknown");
  }

  private static void deepArrayToStringRecursive(Object array, StringBuilder sb) {
    int length = Array.getLength(array);
    sb.append("[");
    for (int i = 0; i < length; i++) {
      Object elem = Array.get(array, i);
      if (elem == null) {
        sb.append("null");
      } else if (elem.getClass().isArray()) {
        deepArrayToStringRecursive(elem, sb);
      } else {
        sb.append(elem);
      }
      if (i < length - 1) sb.append(", ");
    }
    sb.append("]");
  }

  private static String stringify(Object obj) {
    if (obj == null) return "null";

    if (!obj.getClass().isArray()) return obj.toString();

    StringBuilder sb = new StringBuilder();
    deepArrayToStringRecursive(obj, sb);
    return sb.toString();
  }

  private String withPrefixIfNotEmitted(String body) {
    if (!prefixEmitted) {
      prefixEmitted = true;
      return prefix + body;
    }
    return body;
  }

  public Logger print(Object message) {
    instanceConfig.getSink()
      .write(colorize(
        withPrefixIfNotEmitted(stringify(message))
      ));
    return this;
  }

  public Logger println(Object message) {
    instanceConfig.getSink()
      .write(
        colorize(withPrefixIfNotEmitted(stringify(message))) +
          System.lineSeparator()
      );
    return this;
  }

  public Logger println() {
    instanceConfig.getSink()
      .write(System.lineSeparator());
    return this;
  }

  public Logger printf(String format, Object... args) {
    String formatted = withPrefixIfNotEmitted(String.format(format, args));
    instanceConfig.getSink()
      .write(colorize(formatted));
    return this;
  }
}

