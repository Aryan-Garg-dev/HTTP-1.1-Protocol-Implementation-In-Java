package utility.logger.logsink;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.file.Path;

public class LogStreams {
  public static PrintStream console(){
    return System.out;
  }

  public static PrintStream file(File file) throws FileNotFoundException {
    return new PrintStream(new FileOutputStream(file, /* append */ true));
  }

  public static PrintStream file(String filePath) throws FileNotFoundException {
    return file(new File(filePath));
  }

  public static PrintStream file(Path path) throws FileNotFoundException {
    return file(path.toFile());
  }
}