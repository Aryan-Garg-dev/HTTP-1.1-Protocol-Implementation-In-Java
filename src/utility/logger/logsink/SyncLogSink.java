package utility.logger.logsink;

import java.io.PrintStream;

public class SyncLogSink implements LogSink {
  private final PrintStream out;
  private final boolean supportsColor;

  public SyncLogSink(PrintStream stream) {
    this(stream, false);
  }

  public SyncLogSink(PrintStream stream, boolean supportsColor) {
    out = stream;
    this.supportsColor = supportsColor;
  }

  @Override
  public void write(String text) {
    out.print(text);
    out.flush();
  }

  @Override
  public boolean supportsColor() {
    return this.supportsColor;
  }
}
