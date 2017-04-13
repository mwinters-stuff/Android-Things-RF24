package nz.org.winters.android.things.RF24;

import android.support.annotation.IntRange;

/**
 * @author MWinters
 */
@SuppressWarnings("unused")
public class NativeRF24Network {
  static {
    System.loadLibrary("RF24");
  }

  /**
   * Internal defines for handling internal payloads - prevents reading additional data from the radio
   * when buffers are full
   */
  public static final int FLAG_HOLD_INCOMING = 1;
  /**
   * FLAG_BYPASS_HOLDS is mainly for use with RF24Mesh as follows:
   * a: Ensure no data in radio buffers, else exit
   * b: Address is changed to multicast address for renewal
   * c: Holds Cleared (bypass flag is set)
   * d: Address renewal takes place and is set
   * e: Holds Enabled (bypass flag off)
   */
  public static final int FLAG_BYPASS_HOLDS = 2;
  public static final int FLAG_FAST_FRAG = 4;
  public static final int FLAG_NO_POLL = 8;


  @SuppressWarnings("WeakerAccess")
  public class RF24NetworkHeader {

    public int from_node;
    public int to_node;
    public int id;
    public int type;
    public int reserved;
    public int next_id;

    RF24NetworkHeader(int to, @IntRange(from = 0, to = 127) int type) {
      this.to_node = to;
      this.type = type;
    }

    RF24NetworkHeader(int to) {
      this.to_node = to;
      this.type = 0;
    }
  }

  public NativeRF24Network() {
    init();
  }

  public native void init();

  public native void begin(int nodeAddress);

  public native int update();

  public native boolean available();

  public native int peek(RF24NetworkHeader header);

  public native int read(RF24NetworkHeader header, byte[] message, int maxLen);

  public native boolean write(RF24NetworkHeader header, byte[] message, int maxLen);

  public native void multicastLevel(@IntRange(from = 1, to = 6) int level);

  public native void setMulticastRelay(boolean enable);

  public native void setTxTimeout(int timeout);

  public native int getTxTimeout();

  public native void setRouteTimeout(int timeout);

  public native int getRouteTimeout();

  public native boolean multicast(RF24NetworkHeader header, byte[] message, int maxLen, int level);

  public native boolean write(RF24NetworkHeader header, byte[] message, int maxLen, int writeDirect);

  public native int parent();

  public native int addressOfPipe(int node, int pipeNo);

  public native boolean isValidAddress(int node);

  public native void begin(int channel, int node_address);

  public native void setNetworkFlags(int flags);

  public native int getNetworkFlags();


}
