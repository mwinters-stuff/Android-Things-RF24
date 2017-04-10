package nz.org.winters.android.libRF24;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManagerService;
import com.google.android.things.pio.SpiDevice;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.AdditionalMatchers;
import org.mockito.ArgumentMatchers;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

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
  private NativeRF24 radio;

  @Before
  public void setUp() throws Exception {
    spiDevices.add("SPI0");
    spiDevices.add("SPI1");


    Mockito.when(peripheralManagerServiceMock.getSpiBusList()).thenReturn(spiDevices);
    Mockito.when(peripheralManagerServiceMock.openSpiDevice("SPI0")).thenReturn(spiDeviceMock);

    InOrder inOrder = Mockito.inOrder(spiDeviceMock, cePinMock);

    radio = new NativeRF24();

    // constructor calls.
    inOrder.verify(cePinMock).setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);

    inOrder.verify(spiDeviceMock).setMode(SpiDevice.MODE0);
    inOrder.verify(spiDeviceMock).setFrequency(16000000);     // 16MHz
    inOrder.verify(spiDeviceMock).setBitsPerWord(8);          // 8 BPW
    inOrder.verify(spiDeviceMock).setBitJustification(false); // MSB first

    inOrder.verifyNoMoreInteractions();
    Mockito.reset(spiDeviceMock);
    Mockito.reset(cePinMock);
  }

  private byte[] createArray(){
    byte[] b = new byte[33];
    for(int i =0; i < 33; i++){
      b[i] = (byte) 0;
    }
    return b;

  }

  private byte[] createArrayNOP(int num){
    byte[] b = createArray();
    for(int i =0; i < num; i++){
      b[i] = (byte) 0xff;
    }
    return b;

  }

  @After
  public void tearDown() throws Exception {

  }

//  @Test
//  public void ce() throws Exception {
//    InOrder inOrder = Mockito.inOrder(cePinMock);
//
//    radio.ce(false);
//    radio.ce(true);
//    radio.ce(true);
//    radio.ce(false);
//
//    inOrder.verify(cePinMock).setValue(false);
//    inOrder.verify(cePinMock, Mockito.times(2)).setValue(true);
//    inOrder.verify(cePinMock).setValue(false);
//
//    inOrder.verifyNoMoreInteractions();
//  }
//
//  @Test
//  public void beginTransaction() throws Exception {
//    InOrder inOrder = Mockito.inOrder(spiDeviceMock);
//
//    radio.beginTransaction();
//
//    // begin transaction
//    inOrder.verify(spiDeviceMock).setMode(SpiDevice.MODE0);
//    inOrder.verify(spiDeviceMock).setFrequency(16000000);     // 16MHz
//    inOrder.verify(spiDeviceMock).setBitsPerWord(8);
//    inOrder.verifyNoMoreInteractions();
//
//  }

//  @Test
//  public void readRegister() throws Exception {
//
//    byte[] bb_in = createArrayNOP(6);
//    byte[] bb_out = createArray();
//    bb_in[0] = (byte) (nRF24L01.R_REGISTER.i() | (nRF24L01.REGISTER_MASK.i() & nRF24L01.RX_ADDR_P0.i()));
//
//    Mockito.doAnswer(new Answer() {
//      @Override
//      public Object answer(InvocationOnMock invocation) throws Throwable {
//        byte[] array = invocation.getArgument(1);
//        array[0] = (byte)0x10;
//        array[1] = (byte)0x11;
//        array[2] = (byte)0x12;
//        array[3] = (byte)0x13;
//        array[4] = (byte)0x14;
//        array[5] = (byte)0x15;
//        return null;
//      }
//    }).when(spiDeviceMock).transfer(AdditionalMatchers.aryEq(bb_in), AdditionalMatchers.aryEq(bb_out), ArgumentMatchers.eq(6));
//
//    InOrder inOrder = Mockito.inOrder(spiDeviceMock, cePinMock);
//
//    RF24.ReturnBuffer returnBuffer = radio.readRegister(nRF24L01.RX_ADDR_P0.i(),5);
//
//    assertEquals(0x10,returnBuffer.status);
//
//    // begin transaction
//    inOrder.verify(spiDeviceMock).setMode(SpiDevice.MODE0);
//    inOrder.verify(spiDeviceMock).setFrequency(16000000);     // 16MHz
//    inOrder.verify(spiDeviceMock).setBitsPerWord(8);
//
//    byte[] b_in = createArrayNOP(6);
//    b_in[0] = (byte)0x0a;
//    byte[] b_out = createArray();
//    b_out[0] = (byte)0x10;
//    b_out[1] = (byte)0x11;
//    b_out[2] = (byte)0x12;
//    b_out[3] = (byte)0x13;
//    b_out[4] = (byte)0x14;
//    b_out[5] = (byte)0x15;
//    inOrder.verify(spiDeviceMock).transfer(AdditionalMatchers.aryEq(b_in), AdditionalMatchers.aryEq(b_out), ArgumentMatchers.eq(6));
//
//    inOrder.verifyNoMoreInteractions();
//
//  }

//  @Test
//  public void readRegister1() throws Exception {
//    byte[] bb_in = createArrayNOP(2);
//    byte[] bb_out = createArray();
//    bb_in[0] = (byte) (nRF24L01.R_REGISTER.i() | (nRF24L01.REGISTER_MASK.i() & nRF24L01.RX_ADDR_P0.i()));
//
//    Mockito.doAnswer(new Answer() {
//      @Override
//      public Object answer(InvocationOnMock invocation) throws Throwable {
//        byte[] array = invocation.getArgument(1);
//        array[0] = (byte)0x10;
//        array[1] = (byte)0x11;
//        return null;
//      }
//    }).when(spiDeviceMock).transfer(AdditionalMatchers.aryEq(bb_in), AdditionalMatchers.aryEq(bb_out), ArgumentMatchers.eq(2));
//
//    InOrder inOrder = Mockito.inOrder(spiDeviceMock, cePinMock);
//
//
//
//    assertEquals(0x11, radio.readRegister(nRF24L01.RX_ADDR_P0.i()));
//
//    // begin transaction
//    inOrder.verify(spiDeviceMock).setMode(SpiDevice.MODE0);
//    inOrder.verify(spiDeviceMock).setFrequency(16000000);     // 16MHz
//    inOrder.verify(spiDeviceMock).setBitsPerWord(8);
//
//    byte[] b_in = createArrayNOP(2);
//    b_in[0] = (byte)0x0a;
//    byte[] b_out = createArray();
//    b_out[0] = (byte)0x10;
//    b_out[1] = (byte)0x11;
//    inOrder.verify(spiDeviceMock).transfer(AdditionalMatchers.aryEq(b_in), AdditionalMatchers.aryEq(b_out), ArgumentMatchers.eq(2));
//
//    inOrder.verifyNoMoreInteractions();
//  }

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

//  @Test
//  public void getStatus() throws Exception {
//
//    byte[] bb_in = createArray();
//    byte[] bb_out = createArray();
//    bb_in[0] = (byte)0xff;
//
//    Mockito.doAnswer(new Answer() {
//      @Override
//      public Object answer(InvocationOnMock invocation) throws Throwable {
//        byte[] array = invocation.getArgument(1);
//        array[0] = (byte)0x10;
//        return null;
//      }
//    }).when(spiDeviceMock).transfer(AdditionalMatchers.aryEq(bb_in), AdditionalMatchers.aryEq(bb_out), ArgumentMatchers.eq(1));
//
//    InOrder inOrder = Mockito.inOrder(spiDeviceMock, cePinMock);
//
//    assertEquals(0x10,radio.getStatus());
//
//    // begin transaction
//    inOrder.verify(spiDeviceMock).setMode(SpiDevice.MODE0);
//    inOrder.verify(spiDeviceMock).setFrequency(16000000);     // 16MHz
//    inOrder.verify(spiDeviceMock).setBitsPerWord(8);
//
//    byte[] b_in = createArray();
//    b_in[0] = (byte)0xff;
//    byte[] b_out = createArray();
//    b_out[0] = (byte) 0x10;
//    inOrder.verify(spiDeviceMock).transfer(AdditionalMatchers.aryEq(b_in), AdditionalMatchers.aryEq(b_out), ArgumentMatchers.eq(1) );
//
//    inOrder.verifyNoMoreInteractions();
//  }

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