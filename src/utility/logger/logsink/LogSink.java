package utility.logger.logsink;

public interface LogSink {
  void write(String text);

  default boolean supportsColor() {
    return false;
  }

  default void close() {}
}
