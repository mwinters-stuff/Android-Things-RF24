package nz.org.winters.android.things.RF24;

import android.support.annotation.IntRange;

/**
 * @author MWinters
 */
@SuppressWarnings("unused")
public class NativeRF24Network implements NativeRF24NetworkInterface {
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

  public NativeRF24Network() {
    init();
  }

  @Override
  public native void init();

  @Override
  public native void begin(int nodeAddress);

  @Override
  public native int update();

  @Override
  public native boolean available();

  @Override
  public native int peek(RF24NetworkHeader header);

  @Override
  public native int read(RF24NetworkHeader header, byte[] message, int maxLen);

  @Override
  public native boolean write(RF24NetworkHeader header, byte[] message, int maxLen);

  @Override
  public native void multicastLevel(@IntRange(from = 1, to = 6) int level);

  @Override
  public native void setMulticastRelay(boolean enable);

  @Override
  public native void setTxTimeout(int timeout);

  @Override
  public native int getTxTimeout();

  @Override
  public native void setRouteTimeout(int timeout);

  @Override
  public native int getRouteTimeout();

  @Override
  public native boolean multicast(RF24NetworkHeader header, byte[] message, int maxLen, int level);

  @Override
  public native boolean write(RF24NetworkHeader header, byte[] message, int maxLen, int writeDirect);

  @Override
  public native int parent();

  @Override
  public native int addressOfPipe(int node, int pipeNo);

  @Override
  public native boolean isValidAddress(int node);

  @Override
  public native void begin(int channel, int node_address);

  @Override
  public native void setNetworkFlags(int flags);

  @Override
  public native int getNetworkFlags();


}
