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
public interface NativeRF24Interface {
   void closeDevices();

   void init(int cePin, int spiFrequency, int spiBus);

   void begin();

   boolean available();

   int availablePipe();

   void enableAckPayload();

   void enableDynamicPayloads();

   void disableDynamicPayloads();

   void enableDynamicAck();

   void setAutoAck(int pipe, boolean enable);

   void setAutoAck(boolean enable);

   void writeAckPayload(int pipe, byte[] buf, int len);

   boolean isAckPayloadAvailable();

   boolean txStandBy(long timeout, boolean startTx);

   boolean txStandBy();

   boolean writeBlocking(byte[] buffer, int len, long timeout);

   boolean writeFastEx(byte[] buffer, int len, boolean multicast);

   boolean writeFast(byte[] buffer, int len);

   boolean write(byte[] buffer, int len);

   boolean writeEx(byte[] buffer, int len, boolean multicast);


   void powerUp();

   void powerDown();

   boolean rxFifoFull();


   boolean isPVariant();

   int getDynamicPayloadSize();

   int getPayloadSize();

   void setPayloadSize(int size);

   int getChannel();

   void setChannel(int channel);

   void setRetries(int delay, int count);

   boolean testRPD();

   boolean testCarrier();

   int flush_tx();

   void reUseTx();


   void setAddressWidth(int a_width);

   void openReadingPipeStr(int pipe, String address);

   void openReadingPipe(int pipe, long address);

   void closeReadingPipe(int pipe);

   void openWritingPipe(long address);

   void openWritingPipeStr(String address);

   void startWrite(byte[] buf, int len, boolean multicast);

   void startFastWrite(byte[] buf, int len, boolean multicast, boolean startTx);


   void startListening();

   void stopListening();

   byte[] read(int length);

   void disableCRC();

   int getCRCLength();

   void setCRCLength(int length);

   int getDataRate();

   void setDataRate(int speed);

   int getPALevel();

   void setPALevel(int level);

   void printDetails();

}
