package utility.logger;

import utility.logger.logsink.*;

import java.io.IOException;
import java.io.PrintStream;
import java.time.format.DateTimeFormatter;

public class LoggerConfig {
  private LogSink sink = new SyncLogSink(LogStreams.console());
  private boolean showCaller = false;
  private boolean showTimeStamp = false;
  private DateTimeFormatter dateTimeFormat =
    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
  private boolean showLogLevel = false;
  private String prefix = "";

  LogSink getSink(){
    return this.sink;
  }

  boolean canShowCaller(){
    return this.showCaller;
  }

  boolean canShowLogLevel(){
    return this.showLogLevel;
  }

  boolean canShowTimeStamp(){
    return this.showTimeStamp;
  }

  DateTimeFormatter getDateTimeFormat(){
    return this.dateTimeFormat;
  }

  String getPrefix(){
    return this.prefix;
  }

  private void setSink(LogSink newSink){
    LogSink old = sink;
    sink = newSink;
    if (old != null) old.close();
  }

  public LoggerConfig useConsole(){
    setSink(new SyncLogSink(LogStreams.console()));
    return this;
  }

  public LoggerConfig useFile(String filePath){
    try {
      setSink(new SyncLogSink(LogStreams.file(filePath)));
    } catch (IOException e) {
      System.err.println(e.getMessage());
      System.err.println("Falling back to console sink.");
      useConsole();
    }
    return this;
  }

  public LoggerConfig useAsync(PrintStream stream){
    setSink(new AsyncLogSink(stream));
    return this;
  }

  public LoggerConfig useAsyncConsole(){
    setSink(new AsyncLogSink(LogStreams.console()));
    return this;
  }

  public LoggerConfig useAsyncFile(String filePath){
    try {
      setSink(new AsyncLogSink(LogStreams.file(filePath)));
    } catch (IOException e) {
      System.err.println(e.getMessage());
      System.err.println("Falling back to console sink.");
      useConsole(); // "falling back" should actually fall back
    }
    return this;
  }

  public LoggerConfig withPrefix(String prefix){ this.prefix = prefix; return this; }
  public LoggerConfig withCaller(boolean enabled){ showCaller = enabled; return this; }
  public LoggerConfig withTimeStamp(boolean enabled){ showTimeStamp = enabled; return this; }
  public LoggerConfig withLogLevel(boolean enabled){ showLogLevel = enabled; return this; }

  public LoggerConfig useDateTimeFormat(String pattern){
    this.dateTimeFormat = DateTimeFormatter.ofPattern(pattern);
    return this;
  }

  public Logger log()   { return new Logger(Logger.Level.LOG, this); }
  public Logger error() { return new Logger(Logger.Level.ERROR, this); }
  public Logger warn()  { return new Logger(Logger.Level.WARN, this); }
  public Logger info()  { return new Logger(Logger.Level.INFO, this); }
  public Logger debug() { return new Logger(Logger.Level.DEBUG, this); }


  //TODO: temp for testing
  public void closeSink(){
    this.sink.close();
  }
}