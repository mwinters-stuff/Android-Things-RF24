package com.example.androidthings.rf24.rf24;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManagerService;
import com.google.android.things.pio.SpiDevice;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.AdditionalMatchers;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

/**
 * Created by mathew on 31/01/17.
 * Copyright 2017 Mathew Winters
 */
@RunWith(MockitoJUnitRunner.class)
public class RF24Test {

  @Mock
  public PeripheralManagerService peripheralManagerServiceMock;

  @Mock
  public SpiDevice spiDeviceMock;

  @Mock
  public Gpio cePinMock;

  private List<String> spiDevices = new ArrayList<>();

  @Before
  public void setUp() throws Exception {
    spiDevices.add("SPI0");
    spiDevices.add("SPI1");
  }

  private byte[] createArray(){
    byte[] b = new byte[33];
    for(int i =0; i < 33; i++){
      b[i] = (byte) 0;
    }
    return b;

  }

  @After
  public void tearDown() throws Exception {

  }

  @Test
  public void ce() throws Exception {

  }

  @Test
  public void beginTransaction() throws Exception {

  }

  @Test
  public void readRegister() throws Exception {

  }

  @Test
  public void readRegister1() throws Exception {

  }

  @Test
  public void writeRegister() throws Exception {

  }

  @Test
  public void writeRegister1() throws Exception {

  }

  @Test
  public void writePayload() throws Exception {

  }

  @Test
  public void readPayload() throws Exception {

  }

  @Test
  public void flushRx() throws Exception {

  }

  @Test
  public void flushTx() throws Exception {

  }

  @Test
  public void transfer() throws Exception {

  }

  @Test
  public void spiTrans() throws Exception {

  }

  @Test
  public void getStatus() throws Exception {
    when(peripheralManagerServiceMock.getSpiBusList()).thenReturn(spiDevices);
    when(peripheralManagerServiceMock.openSpiDevice("SPI0")).thenReturn(spiDeviceMock);

    byte[] bb_in = createArray();
    byte[] bb_out = createArray();
    bb_in[0] = (byte)0xff;

    doAnswer(new Answer() {
      @Override
      public Object answer(InvocationOnMock invocation) throws Throwable {
        byte[] array = invocation.getArgument(1);
        array[0] = (byte)0x10;
        return null;
      }
    }).when(spiDeviceMock).transfer(aryEq(bb_in), aryEq(bb_out),eq(1));


//            .thenAnswer(new Answer<byte[], byte[], Integer>(){
//
//      @Override
//      public byte[] answer(InvocationOnMock invocation) throws Throwable {
//        return new byte[0];
//      }
//
//      void answer(byte[] tx, byte[] rx, Integer len){
//        rx[0] = (byte) 10;
//      }
//    });

    InOrder inOrder = inOrder(spiDeviceMock, cePinMock);

    RF24 radio = new RF24(peripheralManagerServiceMock,cePinMock);
    assertEquals(0x10,radio.getStatus());

    // constructor calls.
    inOrder.verify(cePinMock).setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);

    //Log.d(TAG,"construct");
    inOrder.verify(spiDeviceMock).setMode(SpiDevice.MODE0);
    inOrder.verify(spiDeviceMock).setFrequency(16000000);     // 16MHz
    inOrder.verify(spiDeviceMock).setBitsPerWord(8);          // 8 BPW
    inOrder.verify(spiDeviceMock).setBitJustification(false); // MSB first


    // begin transaction
    inOrder.verify(spiDeviceMock).setMode(SpiDevice.MODE0);
    inOrder.verify(spiDeviceMock).setFrequency(16000000);     // 16MHz
    inOrder.verify(spiDeviceMock).setBitsPerWord(8);

    byte[] b_in = createArray();
    b_in[0] = (byte)0xff;
    byte[] b_out = createArray();
    b_out[0] = (byte) 0x10;
    inOrder.verify(spiDeviceMock).transfer(aryEq(b_in), aryEq(b_out), eq(1) );

    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void printStatus() throws Exception {

  }

  @Test
  public void printObserveTx() throws Exception {

  }

  @Test
  public void printByteRegister() throws Exception {

  }

  @Test
  public void printAddressRegister() throws Exception {

  }

  @Test
  public void min() throws Exception {

  }

  @Test
  public void setChannel() throws Exception {

  }

  @Test
  public void getPayloadSize() throws Exception {

  }

  @Test
  public void setPayloadSize() throws Exception {

  }

  @Test
  public void printDetails() throws Exception {

  }

  @Test
  public void _BV() throws Exception {

  }

  @Test
  public void begin() throws Exception {

  }

  @Test
  public void startListening() throws Exception {

  }

  @Test
  public void stopListening() throws Exception {

  }

  @Test
  public void powerDown() throws Exception {

  }

  @Test
  public void powerUp() throws Exception {

  }

  @Test
  public void millis() throws Exception {

  }

  @Test
  public void write() throws Exception {

  }

  @Test
  public void write1() throws Exception {

  }

  @Test
  public void writeBlocking() throws Exception {

  }

  @Test
  public void writeFast() throws Exception {

  }

  @Test
  public void writeFast1() throws Exception {

  }

  @Test
  public void startWrite() throws Exception {

  }

  @Test
  public void rxFifoFull() throws Exception {

  }

  @Test
  public void txStandBy() throws Exception {

  }

  @Test
  public void txStandBy1() throws Exception {

  }

  @Test
  public void maskIRQ() throws Exception {

  }

  @Test
  public void getDynamicPayloadSize() throws Exception {

  }

  @Test
  public void available() throws Exception {

  }

  @Test
  public void available1() throws Exception {

  }

  @Test
  public void read() throws Exception {

  }

  @Test
  public void whatHappened() throws Exception {

  }

  @Test
  public void openWritingPipe() throws Exception {

  }

  @Test
  public void openWritingPipe1() throws Exception {

  }

  @Test
  public void openReadingPipe() throws Exception {

  }

  @Test
  public void setAddressWidth() throws Exception {

  }

  @Test
  public void openReadingPipe1() throws Exception {

  }

  @Test
  public void closeReadingPipe() throws Exception {

  }

  @Test
  public void enableDynamicPayloads() throws Exception {

  }

  @Test
  public void enableAckPayload() throws Exception {

  }

  @Test
  public void enableDynamicAck() throws Exception {

  }

  @Test
  public void writeAckPayload() throws Exception {

  }

  @Test
  public void isAckPayloadAvailable() throws Exception {

  }

  @Test
  public void setAutoAck() throws Exception {

  }

  @Test
  public void setAutoAck1() throws Exception {

  }

  @Test
  public void testCarrier() throws Exception {

  }

  @Test
  public void testRPD() throws Exception {

  }

  @Test
  public void getPALevel() throws Exception {

  }

  @Test
  public void setPALevel() throws Exception {

  }

  @Test
  public void setCRCLength() throws Exception {

  }

  @Test
  public void disableCRC() throws Exception {

  }

  @Test
  public void setRetries() throws Exception {

  }

}