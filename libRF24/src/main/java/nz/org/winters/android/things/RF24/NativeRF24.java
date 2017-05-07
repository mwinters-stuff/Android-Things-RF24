package nz.org.winters.android.things.RF24;

import android.support.annotation.IntDef;

import java.io.Closeable;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by mathew on 10/04/17.
 */

@SuppressWarnings({"unused", "WeakerAccess"})
public class NativeRF24 implements NativeRF24Interface, Closeable{
  public static final int RF24_PA_MIN = 0;
  public static final int RF24_PA_LOW = 1;
  public static final int RF24_PA_HIGH = 2;
  public static final int RF24_PA_MAX = 3;
  public static final int RF24_PA_ERROR = 4;


  @Retention(RetentionPolicy.SOURCE)
  @IntDef({RF24_PA_MIN, RF24_PA_LOW, RF24_PA_HIGH, RF24_PA_MAX, RF24_PA_ERROR})
  public @interface Rf24Pa {
  }

  public static final int RF24_CRC_DISABLED = 0;
  public static final int RF24_CRC_8 = 1;
  public static final int RF24_CRC_16 = 2;

  @Retention(RetentionPolicy.SOURCE)
  @IntDef({RF24_CRC_DISABLED, RF24_CRC_8, RF24_CRC_16})
  public @interface Rf24Crc {
  }

  public static final int RF24_DATA_RATE_1MBPS = 0;
  public static final int RF24_DATA_RATE_2MBPS = 1;
  public static final int RF24_DATA_RATE_250KBPS = 2;

  @Retention(RetentionPolicy.SOURCE)
  @IntDef({RF24_DATA_RATE_1MBPS, RF24_DATA_RATE_2MBPS, RF24_DATA_RATE_250KBPS})
  public @interface Rf24DataRate {
  }


  static {
    System.loadLibrary("RF24");
  }

  public NativeRF24(int cePin, int spiFrequency, int spiBus) {
    init(cePin, spiFrequency, spiBus);
  }

  @Override
  public void close() throws IOException {
    closeDevices();
  }

  @Override
  public native void closeDevices();

  @Override
  public native void init(int cePin, int spiFrequency, int spiBus);

  @Override
  public native void begin();

  @Override
  public native boolean available();

  @Override
  public native int availablePipe();

  @Override
  public native void enableAckPayload();

  @Override
  public native void enableDynamicPayloads();

  @Override
  public native void disableDynamicPayloads();

  @Override
  public native void enableDynamicAck();

  @Override
  public native void setAutoAck(int pipe, boolean enable);

  @Override
  public native void setAutoAck(boolean enable);

  @Override
  public native void writeAckPayload(int pipe, byte[] buf, int len);

  @Override
  public native boolean isAckPayloadAvailable();

  @Override
  public native boolean txStandBy(long timeout, boolean startTx);

  @Override
  public native boolean txStandBy();

  @Override
  public native boolean writeBlocking(byte[] buffer, int len, long timeout);

  @Override
  public native boolean writeFastEx(byte[] buffer, int len, boolean multicast);

  @Override
  public native boolean writeFast(byte[] buffer, int len);

  @Override
  public native boolean write(byte[] buffer, int len);

  @Override
  public native boolean writeEx(byte[] buffer, int len, boolean multicast);


  @Override
  public native void powerUp();

  @Override
  public native void powerDown();

  @Override
  public native boolean rxFifoFull();


  @Override
  public native boolean isPVariant();

  @Override
  public native int getDynamicPayloadSize();

  @Override
  public native int getPayloadSize();

  @Override
  public native void setPayloadSize(int size);

  @Override
  public native int getChannel();

  @Override
  public native void setChannel(int channel);

  @Override
  public native void setRetries(int delay, int count);

  @Override
  public native boolean testRPD();

  @Override
  public native boolean testCarrier();

  @Override
  public native int flush_tx();

  @Override
  public native void reUseTx();

  @Override
  public native void setAddressWidth(int a_width);

  @Override
  public native void openReadingPipeStr(int pipe, String address);

  @Override
  public native void openReadingPipe(int pipe, long address);

  @Override
  public native void closeReadingPipe(int pipe);

  @Override
  public native void openWritingPipe(long address);

  @Override
  public native void openWritingPipeStr(String address);

  @Override
  public native void startWrite(byte[] buf, int len, boolean multicast);

  @Override
  public native void startFastWrite(byte[] buf, int len, boolean multicast, boolean startTx);


  @Override
  public native void startListening();

  @Override
  public native void stopListening();

  @Override
  public native byte[] read(int length);

  @Override
  public native void disableCRC();

  @Override
  public native int getCRCLength();

  @Override
  public native void setCRCLength(int length);

  @Override
  public native int getDataRate();

  @Override
  public native void setDataRate(int speed);

  @Override
  public native int getPALevel();

  @Override
  public native void setPALevel(int level);

  @Override
  public native void printDetails();

}
