package utility;

import java.util.function.Supplier;

public class Evaluators {
  public static long time(Runnable task) {
    long start = System.nanoTime();
    task.run();
    return System.nanoTime() - start;
  }

  public static <T> T time(Supplier<T> task, long[] outElapsedNanos) {
    long start = System.nanoTime();
    T result = task.get();
    outElapsedNanos[0] = System.nanoTime() - start;
    return result;
  }

  public static void report(String label, long elapsedNanos, int iterations) {
    double ms = elapsedNanos / 1_000_000.0;
    double perCallMicros = (elapsedNanos / 1000.0) / iterations;
    System.out.printf(
      "%-28s total=%.2fms  calls=%d  avg=%.2fus/call%n",
      label, ms, iterations, perCallMicros);
  }
}
