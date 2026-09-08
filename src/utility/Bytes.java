package utility;

import java.util.Arrays;

public class Bytes {
  public static int indexOf(byte[] buffer, byte[] target){
    if (target.length == 0) return 0;

    outer:
    for (int i = 0; i <= (buffer.length - target.length); i++){
      for (int j = 0; j < target.length; j++){
        if (buffer[i + j] != target[j]) continue outer;
      }
      return i;
    }
    return -1;
  }

  public static int indexOf(byte[] buffer, byte[] target, int offset){
    if (offset < 0 || offset > buffer.length)
      throw new IllegalArgumentException("Invalid offset");

    if (target.length == 0)
      return offset;

    outer:
    for (int i = offset; i <= (buffer.length - target.length); i++){
      for (int j = 0; j < target.length; j++){
        if (buffer[i + j] != target[j]) continue outer;
      }
      return i;
    }

    return -1;
  }
}
