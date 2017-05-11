package nz.org.winters.android.things.RF24;

import android.support.annotation.IntRange;

/**
 * @author MWinters
 */
@SuppressWarnings("unused")
public interface NativeRF24NetworkInterface {

   void init();

   void begin(int nodeAddress);

   int update();

   boolean available();

   int peek(RF24NetworkHeader header);

   int read(RF24NetworkHeader header, byte[] message, int maxLen);

   boolean write(RF24NetworkHeader header, byte[] message, int maxLen);

   void multicastLevel(@IntRange(from = 1, to = 6) int level);

   void setMulticastRelay(boolean enable);

   void setTxTimeout(int timeout);

   int getTxTimeout();

   void setRouteTimeout(int timeout);

   int getRouteTimeout();

   boolean multicast(RF24NetworkHeader header, byte[] message, int maxLen, int level);

   boolean write(RF24NetworkHeader header, byte[] message, int maxLen, int writeDirect);

   int parent();

   int addressOfPipe(int node, int pipeNo);

   boolean isValidAddress(int node);

   void begin(int channel, int node_address);

   void setNetworkFlags(int flags);

   int getNetworkFlags();

}
