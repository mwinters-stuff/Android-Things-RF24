package nz.org.winters.android.things.RF24;

import android.support.annotation.IntRange;

/**
 * Created by mathew on 14/04/17.
 */

public class RF24NetworkHeader {
  public int from_node;
  public int to_node;
  public int id;
  public int type;
  public int reserved;
  public int next_id;

  public RF24NetworkHeader(){

  }

  public RF24NetworkHeader(int to, @IntRange(from = 0, to = 127) int type) {
    this.to_node = to;
    this.type = type;
  }

  public RF24NetworkHeader(int to) {
    this.to_node = to;
    this.type = 0;
  }

}
