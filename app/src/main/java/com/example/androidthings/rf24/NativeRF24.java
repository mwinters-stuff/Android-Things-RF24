package com.example.androidthings.rf24;

import android.support.annotation.IntDef;

import java.io.Closeable;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by mathew on 10/04/17.
 */

@SuppressWarnings({"unused", "WeakerAccess"})
public class NativeRF24 implements Closeable{
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
    System.loadLibrary("native-lib");
  }

  public NativeRF24(int cePin, int spiFrequency, int spiBus) {
    init(cePin, spiFrequency, spiBus);
  }

  @Override
  public void close() throws IOException {
    closeDevices();
  }

  public native void closeDevices();

  public native void init(int cePin, int spiFrequency, int spiBus);

  public native void begin();

  public native boolean available();

  public native int availablePipe();

  public native void enableAckPayload();

  public native void enableDynamicPayloads();

  public native void disableDynamicPayloads();

  public native void enableDynamicAck();

  public native void setAutoAck(int pipe, boolean enable);

  public native void setAutoAck(boolean enable);

  public native void writeAckPayload(int pipe, byte[] buf, int len);

  public native boolean isAckPayloadAvailable();

  public native boolean txStandBy(long timeout, boolean startTx);

  public native boolean txStandBy();

  public native boolean writeBlocking(byte[] buffer, int len, long timeout);

  public native boolean writeFast(byte[] buffer, int len, boolean multicast);

  public native boolean writeFast(byte[] buffer, int len);

  public native boolean write(byte[] buffer, int len, boolean multicast);

  public native boolean write(byte[] buffer, int len);


  public native void powerUp();

  public native void powerDown();

  public native boolean rxFifoFull();


  public native boolean isPVariant();

  public native int getDynamicPayloadSize();

  public native int getPayloadSize();

  public native void setPayloadSize(int size);

  public native int getChannel();

  public native void setChannel(int channel);

  public native void setRetries(int delay, int count);

  public native boolean testRPD();

  public native boolean testCarrier();

  public native int flush_tx();

  public native void reUseTx();


  public native void setAddressWidth(int a_width);

  public native void openReadingPipeStr(int pipe, String address);

  public native void openReadingPipe(int pipe, long address);

  public native void closeReadingPipe(int pipe);

  public native void openWritingPipe(long address);

  public native void openWritingPipeStr(String address);

  public native void startWrite(byte[] buf, int len, boolean multicast);

  public native void startFastWrite(byte[] buf, int len, boolean multicast, boolean startTx);


  public native void startListening();

  public native void stopListening();

  public native byte[] read(int length);

  public native void disableCRC();

  public native int getCRCLength();

  public native void setCRCLength(int length);

  public native int getDataRate();

  public native void setDataRate(int speed);

  public native int getPALevel();

  public native void setPALevel(int level);

  public native void printDetails();

}
