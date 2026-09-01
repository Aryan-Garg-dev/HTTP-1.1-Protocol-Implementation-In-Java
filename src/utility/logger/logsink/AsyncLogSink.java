package utility.logger.logsink;

import java.io.PrintStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public final class AsyncLogSink implements LogSink {
  private static final String POISON_PILL = "\0__LOGGER_CLOSE__\0";

  private final BlockingQueue<String> queue = new LinkedBlockingDeque<>();
  private final Thread worker;
  private volatile boolean closed = false;
  private final PrintStream stream;
  private final boolean colorSupport;

  public AsyncLogSink(PrintStream stream) {
    this(stream, stream == System.out);
  }

  public AsyncLogSink(PrintStream stream, boolean colorSupport){
    this.stream = stream;
    this.colorSupport = colorSupport;
    this.worker = new Thread(() -> runLoop(stream), "async-logger-sink");
    this.worker.setDaemon(true);
    this.worker.start();
    Runtime.getRuntime().addShutdownHook(new Thread(this::close, "async-logger-sink-shutdown"));
  }

  private void runLoop(PrintStream stream){
    try {
      while (true){
        String line = queue.take();
        if (line.equals(POISON_PILL)) break;
        stream.print(line);
        //TODO: can create flush policy, if immediate flush is bottleneck (for extensive file logging)
        stream.flush(); // durable -> flushed immediately
      }
    } catch (InterruptedException e){
      Thread.currentThread().interrupt();
    } catch (Exception e){
      System.err.println(e.getMessage());
    }
  }

  @Override
  public void write(String text) {
    synchronized (this) {
      if (closed) return;
      queue.offer(text);
    }
  }

  @Override
  public boolean supportsColor() {
    return colorSupport;
  }

  @Override
  public void close() {
    synchronized (this) {
      if (closed) return;
      closed = true;
      queue.offer(POISON_PILL);
    }
    try {
      worker.join(2000);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    if (stream != System.out && stream != System.err) {
      stream.close();
    }
  }
}

