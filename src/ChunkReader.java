import java.io.InputStream;

public class ChunkReader extends InputStream {
  private final byte[] data;
  private final int chunkSize;
  private int pos = 0;

  public ChunkReader(byte[] data, int chunkSize) {
    this.data = data;
    this.chunkSize = chunkSize;
  }

  @Override public int read() {
    //  converting it to an unsigned 8-bit integer,
    //  allowing the caller to distinguish between a valid data byte
    //  and the end of the data stream.
    return pos < data.length ? data[pos++] & 0xFF : -1;
  }

  @Override public int read(byte[] b, int off, int len) {
    if (pos >= data.length) return -1;
    int n = Math.min(chunkSize, Math.min(len, data.length - pos));
    System.arraycopy(data, pos, b, off, n);
    pos += n;
    return n;
  }
}