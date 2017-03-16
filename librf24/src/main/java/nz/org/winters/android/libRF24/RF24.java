package nz.org.winters.android.libRF24;

import android.os.SystemClock;
import android.support.annotation.NonNull;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManagerService;
import com.google.android.things.pio.SpiDevice;
import com.google.common.primitives.Longs;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.Locale;


/**
 * Created by mathew on 20/01/17.
 * Copyright 2017 Mathew Winters
 */

@SuppressWarnings({"WeakerAccess", "unused", "MismatchedReadAndWriteOfArray", "squid:S1068",
    "squid:CommentedOutCodeLine", "squid:S00116", "squid:S00117", "squid:S00100", "squid:S1226"})
public class RF24 implements Closeable {
  private static final String TAG = RF24.class.getName();
  private final Gpio cePin;
  private final SpiDevice device;

  /****************************************************************************/
  byte[] child_pipe =
      {
          nRF24L01.RX_ADDR_P0.i(), nRF24L01.RX_ADDR_P1.i(), nRF24L01.RX_ADDR_P2.i(), nRF24L01.RX_ADDR_P3.i(), nRF24L01.RX_ADDR_P4.i(), nRF24L01.RX_ADDR_P5.i()
      };
  byte[] child_payload_size =
      {
          nRF24L01.RX_PW_P0.i(), nRF24L01.RX_PW_P1.i(), nRF24L01.RX_PW_P2.i(), nRF24L01.RX_PW_P3.i(), nRF24L01.RX_PW_P4.i(), nRF24L01.RX_PW_P5.i()
      };
  private int addrWidth;
  private byte[] spi_rxbuff = new byte[32 + 1]; //SPI receive buffer (payload max 32 bytes)
  private byte[] spi_txbuff = new byte[32 + 1]; //SPI transmit buffer (payload max 32 bytes + 1 byte for the command)
  private String[] rf24DataRateStr = {"1MBPS", "2MBPS", "250KBPS"};
  private String[] rf24ModelStr = {"nRF24L01", "nRF24L01+"};
  private String[] rf24CrcLengthStr = {"Disabled", "8 bits", "16 bits"};
  private String[] rf24PaDbmStr = {"PA_MIN", "PA_LOW", "PA_HIGH", "PA_MAX"};
  private String[] rf24CsnStr = {"CE0 (PI Hardware Driven)", "CE1 (PI Hardware Driven)", "CE2 (PI Hardware Driven)", "Custom GPIO Software Driven"};
  private byte[] childPipeEnable = {nRF24L01.ERX_P0.i(), nRF24L01.ERX_P1.i(), nRF24L01.ERX_P2.i(), nRF24L01.ERX_P3.i(), nRF24L01.ERX_P4.i(), nRF24L01.ERX_P5.i()};

  private int payloadSize;
  private boolean dynamicPayloadsEnabled = false;
  private boolean pVariant;
  private byte[] pipe0ReadingAddress = new byte[5];
  private int txRxDelay;
  private int spiFrequency;


  /**
   * < Last address set on pipe 0 for reading.
   */

  public RF24(@NonNull PeripheralManagerService manager, Gpio cePin, int spiBus) throws IOException {
    this(manager, cePin, 16, spiBus, 32);
  }

  public RF24(@NonNull PeripheralManagerService manager, Gpio cePin) throws IOException {
    this(manager, cePin, 16, 0, 32);
  }


  public RF24(@NonNull PeripheralManagerService manager, Gpio cePin, int spiFrequency, int spiBus, int payloadSize) throws IOException {
    this.cePin = cePin;
//    this.csPin = csPin;
    this.payloadSize = payloadSize;
    this.addrWidth = 5;
    this.pVariant = false;
    this.spiFrequency = spiFrequency;
    this.dynamicPayloadsEnabled = false;
    pipe0ReadingAddress[0] = 0;

    List<String> spiBusList = manager.getSpiBusList();
    if (spiBusList.isEmpty() || spiBusList.size() < spiBus) {
      throw new UnsupportedOperationException("No SPI Bus");
    }

    device = manager.openSpiDevice(spiBusList.get(spiBus));
    if (device == null) {
      throw new IOException("SpiDevice open failed");
    }

    cePin.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);

    //Log.d(TAG,"construct");
    device.setMode(SpiDevice.MODE0);
    device.setFrequency(spiFrequency * 1000000);     // 16MHz
    device.setBitsPerWord(8);          // 8 BPW
    device.setBitJustification(false); // MSB first
    delayMicroseconds(5);

  }

  @Override
  public void close() throws IOException {
    if (device != null) {
      device.close();
    }
  }

  public void delay(long milliseconds) {
    try {
      Thread.sleep(milliseconds);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }

  }

  public void delayMicroseconds(int microseconds) {
    try {
      Thread.sleep(0, microseconds * 1000);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  public void ce(boolean level) throws IOException {
    cePin.setValue(level);
  }

  public void beginTransaction() throws IOException {
    //csn(false);
    device.setMode(SpiDevice.MODE0);
    device.setFrequency(spiFrequency * 1000000);     // 16MHz
    device.setBitsPerWord(8);          // 8 BPW
  }

  public ReturnBuffer readRegister(byte reg, int len) throws IOException {
    beginTransaction();

    int ptx = 0;
    spi_txbuff[ptx++] = (byte) (nRF24L01.R_REGISTER.i() | (nRF24L01.REGISTER_MASK.i() & reg));
    int size = len + 1;
    while (len-- != 0) {
      spi_txbuff[ptx++] = nRF24L01.NOP.i();
    }

    transfer(spi_txbuff, spi_rxbuff, size);

    ReturnBuffer rv = new ReturnBuffer();
    rv.status = spi_rxbuff[0];
    rv.copyBuffer(spi_rxbuff, 1, size);
    ////Log.d(TAG,String.format("readRegisterB(%02x,%02x,%02x,%02x)",reg,spi_rxbuff[1],spi_txbuff[0],spi_rxbuff[0]));
    return rv;
  }

  public byte readRegister(byte reg) throws IOException {
    beginTransaction();

    int pos = 0;
    spi_txbuff[pos++] = (byte) (nRF24L01.R_REGISTER.i() | (nRF24L01.REGISTER_MASK.i() & reg));
    spi_txbuff[pos] = nRF24L01.NOP.i();

    transfer(spi_txbuff, spi_rxbuff, 2);
    ////Log.d(TAG,String.format("readRegister(%02x,%02x,%02x,%02x)",reg,spi_rxbuff[1],spi_txbuff[0],spi_rxbuff[0]));
    return spi_rxbuff[1];
  }

  public byte writeRegister(byte reg, byte[] buf, int len) throws IOException {
    beginTransaction();

    int ptx = 0;
    int size = len + 1;
    int bx = 0;
    spi_txbuff[ptx++] = (byte) (nRF24L01.W_REGISTER.i() | (nRF24L01.REGISTER_MASK.i() & reg));
    while (len-- != 0) {
      spi_txbuff[ptx++] = buf[bx++];
    }
    spi_txbuff[ptx] = nRF24L01.NOP.i();

    transfer(spi_txbuff, spi_rxbuff, size);
    return spi_rxbuff[0];
  }

  public byte writeRegister(byte reg, byte value) throws IOException {
    beginTransaction();

    int ptx = 0;
    spi_txbuff[ptx++] = (byte) (nRF24L01.W_REGISTER.i() | (nRF24L01.REGISTER_MASK.i() & reg));
    spi_txbuff[ptx] = value;

    transfer(spi_txbuff, spi_rxbuff, 2);
    // //Log.d(TAG,String.format("writeRegister(%02x,%02x,%02x,%02x)",reg,value,spi_txbuff[0],spi_rxbuff[0]));
    return spi_rxbuff[0];
  }

  public byte writePayload(byte[] buffer, int dataLen, byte writeType) throws IOException {
    //Log.d(TAG,"writePayload");
    int current = 0;
    dataLen = Math.min(dataLen, payloadSize);
    int blankLen = dynamicPayloadsEnabled ? 0 : payloadSize - dataLen;

    beginTransaction();

    int ptx = 0;
    int size = dataLen + blankLen + 1;

    spi_txbuff[ptx++] = writeType;
    while (dataLen-- > 0) {
      spi_txbuff[ptx++] = buffer[current++];
    }
    while (blankLen-- > 0) {
      spi_txbuff[ptx++] = 0;
    }

    transfer(spi_txbuff, spi_rxbuff, size);
    return spi_rxbuff[0];
  }

//  void endTransaction() throws IOException {
////    csn(true);
//  }

  public ReturnBuffer readPayload(int dataLen) throws IOException {
    //Log.d(TAG,"readPayload");
    ReturnBuffer status = new ReturnBuffer();

    //int current = 0;
    if (dataLen > payloadSize) {
      dataLen = payloadSize;
    }
    int blankLen = dynamicPayloadsEnabled ? 0 : payloadSize - dataLen;

    beginTransaction();

    int prx = 0;
    int ptx = 0;
    int size = dataLen + blankLen + 1;

    spi_txbuff[ptx++] = nRF24L01.R_RX_PAYLOAD.i();
    while (--size > 0) {
      spi_txbuff[ptx++] = nRF24L01.NOP.i();
    }

    size = dataLen + blankLen + 1;

    transfer(spi_txbuff, spi_rxbuff, size);
    status.status = spi_rxbuff[prx++];
    status.copyBuffer(spi_rxbuff, prx, dataLen);

    return status;
  }

  public byte flushRx() throws IOException {
    return spiTrans(nRF24L01.FLUSH_RX.i());
  }

  public byte flushTx() throws IOException {
    return spiTrans(nRF24L01.FLUSH_TX.i());
  }

  public void transfer(byte[] txbuf, byte[] rxbuf, int len) throws IOException {
//    device.write(txbuf,len);
//    device.read(txbuf,len);
    device.transfer(txbuf, rxbuf, len);
  }

  byte spiTrans(byte command) throws IOException {
    beginTransaction();

    spi_txbuff[0] = command;
    transfer(spi_txbuff, spi_rxbuff, 1);
    return spi_rxbuff[0];
  }

  public byte getStatus() throws IOException {
    return spiTrans(nRF24L01.NOP.i());
  }

  public String printStatus(byte status) {
    return String.format(Locale.getDefault(), "STATUS\t\t = 0x%02x RX_DR=%x TX_DS=%x MAX_RT=%x RX_P_NO=%x TX_FULL=%x",
        status,
        (status & (1 << (nRF24L01.RX_DR.i()) & 0xff) & 0xff) & 0xff,
        (status & (1 << (nRF24L01.TX_DS.i()) & 0xff) & 0xff) & 0xff,
        (status & (1 << (nRF24L01.MAX_RT.i()) & 0xff) & 0xff) & 0xff,
        ((status >> nRF24L01.RX_P_NO.i()) & 0b111) & 0xff,
        (status & (1 << (nRF24L01.TX_FULL.i()) & 0xff) & 0xff) & 0xff
    );
  }

  String printObserveTx(byte value) {
    return String.format(Locale.getDefault(), "OBSERVE_TX=%02x: POLS_CNT=%x ARC_CNT=%x",
        value,
        (value >> nRF24L01.PLOS_CNT.i()) & 0b1111,
        (value >> nRF24L01.ARC_CNT.i()) & 0b1111
    );
  }

  String printByteRegister(String name, byte reg, int qty) throws IOException {
    StringBuilder stringBuilder = new StringBuilder(String.format(Locale.getDefault(), "%s\t =", name));

    while (qty-- > 0) {
      stringBuilder.append(String.format(Locale.getDefault(), " 0x%02x", readRegister(reg++)));
    }
    stringBuilder.append("\n");
    return stringBuilder.toString();
  }

  String printAddressRegister(String name, byte reg, int qty) throws IOException {
    StringBuilder stringBuilder = new StringBuilder(String.format(Locale.getDefault(), "%s\t =", name));

    while (qty-- > 0) {

      ReturnBuffer buffer = readRegister(reg++, addrWidth);

      stringBuilder.append(" 0x");

      int bufptr = addrWidth;
      while (--bufptr >= 0) {
        stringBuilder.append(String.format(Locale.getDefault(), "%02X", buffer.buffer[bufptr]));
      }
    }
    stringBuilder.append("\n");
    return stringBuilder.toString();
  }

  byte min(byte a, byte b) {
    return (a <= b) ? a : b;
  }

  public void setChannel(byte channel) throws IOException {
    writeRegister(nRF24L01.RF_CH.i(), min(channel, (byte) 127));
  }

  public int getPayloadSize() {
    return payloadSize;
  }

  public void setPayloadSize(int size) {
    payloadSize = size;
  }

  public String printDetails() throws IOException {
    @SuppressWarnings("StringBufferReplaceableByString")
    StringBuilder stringBuilder = new StringBuilder();

    stringBuilder.append(printStatus(getStatus()));
    stringBuilder.append("\n");
    stringBuilder.append(printAddressRegister("RX_ADDR_P0-1", nRF24L01.RX_ADDR_P0.i(), 2));
    stringBuilder.append(printByteRegister("RX_ADDR_P2-5", nRF24L01.RX_ADDR_P2.i(), 4));
    stringBuilder.append(printAddressRegister("TX_ADDR\t", nRF24L01.TX_ADDR.i(), 1));

    stringBuilder.append(printByteRegister("RX_PW_P0-6", nRF24L01.RX_PW_P0.i(), 6));
    stringBuilder.append(printByteRegister("EN_AA\t", nRF24L01.EN_AA.i(), 1));
    stringBuilder.append(printByteRegister("EN_RXADDR", nRF24L01.EN_RXADDR.i(), 1));
    stringBuilder.append(printByteRegister("RF_CH\t", nRF24L01.RF_CH.i(), 1));
    stringBuilder.append(printByteRegister("RF_SETUP", nRF24L01.RF_SETUP.i(), 1));
    stringBuilder.append(printByteRegister("CONFIG\t", nRF24L01.CONFIG.i(), 1));
    stringBuilder.append(printByteRegister("DYNPD/FEATURE", nRF24L01.DYNPD.i(), 2));

    stringBuilder.append("\nData Rate\t = ");
    stringBuilder.append(rf24DataRateStr[getDataRate().ordinal()]);
    stringBuilder.append("\nModel\t\t = ");
    stringBuilder.append(rf24ModelStr[isPVariant() ? 1 : 0]);
    stringBuilder.append("\nCRC Length = ");
    stringBuilder.append(rf24CrcLengthStr[getCRCLength().ordinal()]);
    stringBuilder.append("\nPA Power\t = ");
    stringBuilder.append(rf24PaDbmStr[getPALevel()]);

    return stringBuilder.toString();
  }

  byte _BV(byte bit) {
    return (byte) (1 << bit);
  }

  public void begin() throws IOException {
    //int setup;
    //Log.d(TAG,"begin");
    // spi.begin

    ce(false);
    delay(100);
    delay(5);

    // Reset CONFIG and enable 16-bit CRC.
    writeRegister(nRF24L01.CONFIG.i(), (byte) 0b00001100);
    // Set 1500uS (minimum for 32B payload in ESB@250KBPS) timeouts, to make testing a little easier
    // WARNING: If this is ever lowered, either 250KBS mode with AA is broken or maximum packet
    // sizes must never be used. See documentation for a more complete explanation.
    setRetries((byte) 5, (byte) 15);

    // check for connected module and if this is a p nRF24l01 variant
    //
    if (setDataRate(rf24DataRate.RF24_250KBPS)) {
      pVariant = true;
    }
    // Then set the data rate to the slowest (and most reliable) speed supported by all
    // hardware.

    setDataRate(rf24DataRate.RF24_1MBPS);

    toggleFeatures();

    // Disable dynamic payloads, to match dynamic_payloads_enabled setting - Reset value is 0
    writeRegister(nRF24L01.FEATURE.i(), (byte) 0);
    writeRegister(nRF24L01.DYNPD.i(), (byte) 0);

    // Reset current status
    // Notice reset and flush is the last thing we do
    writeRegister(nRF24L01.NRF_STATUS.i(), (byte) (_BV(nRF24L01.RX_DR.i()) | _BV(nRF24L01.TX_DS.i()) | _BV(nRF24L01.MAX_RT.i())));


    // Set up default configuration.  Callers can always change it later.
    // This channel should be universally safe and not bleed over into adjacent
    // spectrum.
    setChannel((byte) 76);

    // Flush buffers
    flushRx();
    flushTx();

    powerUp(); //Power up by default when begin() is called

    // Enable PTX, do not write CE high so radio will remain in standby I mode ( 130us max to transition to RX or TX instead of 1500us from powerUp )
    // PTX should use only 22uA of power
    writeRegister(nRF24L01.CONFIG.i(), (byte) ((readRegister(nRF24L01.CONFIG.i())) & ~_BV(nRF24L01.PRIM_RX.i())));


  }

  public void startListening() throws IOException {
    powerUp();
    //Log.d(TAG,"startListening");
    writeRegister(nRF24L01.CONFIG.i(), (byte) (readRegister(nRF24L01.CONFIG.i()) | _BV(nRF24L01.PRIM_RX.i())));

    writeRegister(nRF24L01.NRF_STATUS.i(), (byte) (_BV(nRF24L01.RX_DR.i()) | _BV(nRF24L01.TX_DS.i()) | _BV(nRF24L01.MAX_RT.i())));
    ce(true);
    // Restore the pipe0 adddress, if exists
    if (pipe0ReadingAddress[0] > 0) {
      writeRegister(nRF24L01.RX_ADDR_P0.i(), pipe0ReadingAddress, addrWidth);
    } else {
      closeReadingPipe((byte) 0);
    }

    // Flush buffers
    //flush_rx();
    if ((readRegister(nRF24L01.FEATURE.i()) & _BV(nRF24L01.EN_ACK_PAY.i())) != 0) {
      flushTx();
    }
  }

  public void stopListening() throws IOException {
    //Log.d(TAG,"stopListening()");
    ce(false);

    delayMicroseconds(txRxDelay);

    if ((readRegister(nRF24L01.FEATURE.i()) & _BV(nRF24L01.EN_ACK_PAY.i())) != 0) {
      delayMicroseconds(txRxDelay); //200
      flushTx();
    }
    //flush_rx();
    writeRegister(nRF24L01.CONFIG.i(), (byte) ((readRegister(nRF24L01.CONFIG.i())) & ~_BV(nRF24L01.PRIM_RX.i())));

    writeRegister(nRF24L01.EN_RXADDR.i(), (byte) ((readRegister(nRF24L01.EN_RXADDR.i())) | _BV(childPipeEnable[0])));

  }

  public void powerDown() throws IOException {
    //Log.d(TAG,"powerDown");
    ce(false); // Guarantee CE is low on powerDown
    writeRegister(nRF24L01.CONFIG.i(), (byte) ((readRegister(nRF24L01.CONFIG.i())) & ~_BV(nRF24L01.PWR_UP.i())));
  }

  //Power up now. Radio will not power down unless instructed by MCU for config changes etc.
  public void powerUp() throws IOException {
    //Log.d(TAG,"PowerUp");
    byte cfg = readRegister(nRF24L01.CONFIG.i());

    // if not powered up then power up and wait for the radio to initialize
    if ((cfg & _BV(nRF24L01.PWR_UP.i())) == 0) {
      byte w = (byte) (cfg | _BV(nRF24L01.PWR_UP.i()));
      //Log.d(TAG,String.format("%02x %02x",cfg,w));
      writeRegister(nRF24L01.CONFIG.i(), w);

      // For nRF24L01+ to go from power down mode to TX or RX mode it must first pass through stand-by mode.
      // There must be a delay of Tpd2stby (see Table 16.) after the nRF24L01+ leaves power down mode before
      // the CEis set high. - Tpd2stby can be up to 5ms per the 1.0 datasheet
      delay(5);
    }
  }

  public long millis() {
    return SystemClock.uptimeMillis();
  }

  //Similar to the previous write, clears the interrupt flags
  public boolean write(byte[] buf, int len, boolean multicast) throws IOException {
    //Start Writing
    startFastWrite(buf, len, multicast, true);

    //Wait until complete or failed
    long timer = millis();

    while ((getStatus() & (_BV(nRF24L01.TX_DS.i()) | _BV(nRF24L01.MAX_RT.i()))) == 0) {
      if (millis() - timer > 85) {
        return false;
      }
    }

    ce(false);

    byte status = writeRegister(nRF24L01.NRF_STATUS.i(), (byte) (_BV(nRF24L01.RX_DR.i()) | _BV(nRF24L01.TX_DS.i()) | _BV(nRF24L01.MAX_RT.i())));

    //Max retries exceeded
    if ((status & _BV(nRF24L01.MAX_RT.i())) != 0) {
      flushTx(); //Only going to be 1 packet int the FIFO at a time using this method, so just flush
      return false;
    }
    //TX OK 1 or 0
    return true;
  }

  public boolean write(byte[] buf, int len) throws IOException {
    return write(buf, len, false);
  }

  //For general use, the interrupt flags are not important to clear
  public boolean writeBlocking(byte[] buf, int len, long timeout) throws IOException {
    //Block until the FIFO is NOT full.
    //Keep track of the MAX retries and set auto-retry if seeing failures
    //This way the FIFO will fill up and allow blocking until packets go through
    //The radio will auto-clear everything in the FIFO as long as CE remains high

    long timer = millis();                //Get the time that the payload transmission started

    while ((getStatus() & (_BV(nRF24L01.TX_FULL.i()))) != 0) {      //Blocking only if FIFO is full. This will loop and block until TX is successful or timeout

      if ((getStatus() & _BV(nRF24L01.MAX_RT.i())) != 0) {            //If MAX Retries have been reached
        reUseTX();                      //Set re-transmit and clear the MAX_RT interrupt flag
        if (millis() - timer > timeout) {
          return false;
        }      //If this payload has exceeded the user-defined timeout, exit and return 0
      }
      if (millis() - timer > (timeout + 85)) {
        return false;
      }


    }

    //Start Writing
    startFastWrite(buf, len, false, true);                  //Write the payload if a buffer is clear

    return true;                          //Return 1 to indicate successful transmission
  }

  private void reUseTX() throws IOException {
    writeRegister(nRF24L01.NRF_STATUS.i(), _BV(nRF24L01.MAX_RT.i()));        //Clear max retry flag
    spiTrans(nRF24L01.REUSE_TX_PL.i());
    ce(false);                      //Re-Transfer packet
    ce(true);
  }

  public boolean writeFast(byte[] buf, int len, boolean multicast) throws IOException {
    //Block until the FIFO is NOT full.
    //Keep track of the MAX retries and set auto-retry if seeing failures
    //Return 0 so the user can control the retrys and set a timer or failure counter if required
    //The radio will auto-clear everything in the FIFO as long as CE remains high

    long timer = millis();

    while ((getStatus() & (_BV(nRF24L01.TX_FULL.i()))) != 0) {        //Blocking only if FIFO is full. This will loop and block until TX is successful or fail

      if ((getStatus() & _BV(nRF24L01.MAX_RT.i())) != 0) {
        //reUseTX();										  //Set re-transmit
        writeRegister(nRF24L01.NRF_STATUS.i(), _BV(nRF24L01.MAX_RT.i()));        //Clear max retry flag
        return false;                      //Return 0. The previous payload has been retransmitted
        //From the user perspective, if you get a 0, just keep trying to send the same payload
      }
      if (millis() - timer > 85) {
        return false;
      }
    }
    //Start Writing
    startFastWrite(buf, len, multicast, true);

    return true;
  }

  public boolean writeFast(byte[] buf, int len) throws IOException {
    return writeFast(buf, len, false);
  }

  private void startFastWrite(byte[] buf, int len, boolean multicast, boolean startTx) throws IOException {
    writePayload(buf, len, multicast ? nRF24L01.W_TX_PAYLOAD_NO_ACK.i() : nRF24L01.W_TX_PAYLOAD.i());
    if (startTx) {
      ce(true);
    }
  }

  //Added the original startWrite back in so users can still use interrupts, ack payloads, etc
//Allows the library to pass all tests
  void startWrite(byte[] buf, int len, boolean multicast) throws IOException {

    // Send the payload

    //write_payload( buf, len );
    writePayload(buf, len, multicast ? nRF24L01.W_TX_PAYLOAD_NO_ACK.i() : nRF24L01.W_TX_PAYLOAD.i());
    ce(true);
    ce(false);
  }

  public boolean rxFifoFull() throws IOException {
    return (readRegister(nRF24L01.FIFO_STATUS.i()) & _BV(nRF24L01.RX_FULL.i())) > 0;
  }

  public boolean txStandBy() throws IOException {
    long timeout = millis();
    while (0 == (readRegister(nRF24L01.FIFO_STATUS.i()) & _BV(nRF24L01.TX_EMPTY.i()))) {
      if ((getStatus() & _BV(nRF24L01.MAX_RT.i())) > 0) {
        writeRegister(nRF24L01.NRF_STATUS.i(), _BV(nRF24L01.MAX_RT.i()));
        ce(false);
        flushTx();    //Non blocking, flush the data
        return false;
      }

      if (millis() - timeout > 85) {
        return false;
      }
    }

    ce(false);         //Set STANDBY-I mode
    return true;
  }

  public boolean txStandBy(long timeout, boolean startTx) throws IOException {

    if (startTx) {
      stopListening();
      ce(true);
    }
    long start = millis();

    while (0 == (readRegister(nRF24L01.FIFO_STATUS.i()) & _BV(nRF24L01.TX_EMPTY.i()))) {
      if ((getStatus() & _BV(nRF24L01.MAX_RT.i())) > 0) {
        writeRegister(nRF24L01.NRF_STATUS.i(), _BV(nRF24L01.MAX_RT.i()));
        ce(false);                      //Set re-transmit
        ce(true);
        if (millis() - start >= timeout) {
          ce(false);
          flushTx();
          return false;
        }
      }
      if (millis() - start > (timeout + 85)) {
        return false;
      }
    }


    ce(false);           //Set STANDBY-I mode
    return false;
  }

  void maskIRQ(boolean tx, boolean fail, boolean rx) throws IOException {
    writeRegister(nRF24L01.CONFIG.i(), (byte) ((readRegister(nRF24L01.CONFIG.i()) & 0xff) | ((fail ? 1 : 0) << nRF24L01.MASK_MAX_RT.i()) | ((tx ? 1 : 0) << nRF24L01.MASK_TX_DS.i()) | ((rx ? 1 : 0) << nRF24L01.MASK_RX_DR.i())));
  }

  public byte getDynamicPayloadSize() throws IOException {
    spi_txbuff[0] = nRF24L01.R_RX_PL_WID.i();
    spi_rxbuff[1] = (byte) 0xff;
    beginTransaction();
    transfer(spi_txbuff, spi_rxbuff, 2);
    byte result = spi_rxbuff[1];


    if (result > 32) {
      flushRx();
      delay(2);
      return 0;
    }
    return result;
  }

  public boolean available() throws IOException {
    return available(false) > 0;
  }

  /****************************************************************************/

  public byte available(boolean pipe_num) throws IOException {
    byte r = readRegister(nRF24L01.FIFO_STATUS.i());

    if ((r & _BV(nRF24L01.RX_EMPTY.i())) == 0) {// || (r & _BV(nRF24L01.RX_FULL.i())) == 0){
//      Log.d(TAG,"FIFO available");

      // If the caller wants the pipe number, include that
      if (pipe_num) {
        byte status = getStatus();
        return (byte) ((status >> nRF24L01.RX_P_NO.i()) & 0x07);
      }
      return 1;
    }
//    if((r & _BV(nRF24L01.TX_FULL.i())) == 0){
//      Log.d(TAG,"FIFO TX_FULL");
//    }
//    if((r & _BV(nRF24L01.TX_EMPTY.i())) == 0){
//      Log.d(TAG,"FIFO TX_EMPTY");
//    }
//
//    if((r & _BV(nRF24L01.RX_FULL.i())) == 0){
//      Log.d(TAG,"FIFO RX_FULL");
//    }
//
//    if((r & _BV(nRF24L01.RX_EMPTY.i())) == 0){
//      Log.d(TAG,"FIFO RX_EMPTY");
//    }

    return -1;
  }

  public byte[] read(int len) throws IOException {
    //Log.d(TAG,"read");
    // Fetch the payload
    ReturnBuffer rv = readPayload(len);

    //Clear the two possible interrupt flags with one command
    writeRegister(nRF24L01.NRF_STATUS.i(), (byte) (_BV(nRF24L01.RX_DR.i()) | _BV(nRF24L01.MAX_RT.i()) | _BV(nRF24L01.TX_DS.i())));
    return rv.buffer;
  }

  public WhatHappenedResult whatHappened() throws IOException {
    // Read the status & reset the status in one easy call
    // Or is that such a good idea?
    byte status = writeRegister(nRF24L01.NRF_STATUS.i(), (byte) (_BV(nRF24L01.RX_DR.i()) | _BV(nRF24L01.TX_DS.i()) | _BV(nRF24L01.MAX_RT.i())));
    WhatHappenedResult result = new WhatHappenedResult();
    // Report to the user what happened
    result.tx_ok = (status & _BV(nRF24L01.TX_DS.i())) == 1;
    result.tx_fail = (status & _BV(nRF24L01.MAX_RT.i())) == 1;
    result.rx_ready = (status & _BV(nRF24L01.RX_DR.i())) == 1;
    return result;
  }

  public void openWritingPipe(long value) throws IOException {
    //Log.d(TAG,"openWiritingPipe");
    // Note that AVR 8-bit uC's store this LSB first, and the NRF24L01(+)
    // expects it LSB first too, so we're good.
    byte[] buffer = Longs.toByteArray(value);
    writeRegister(nRF24L01.RX_ADDR_P0.i(), buffer, addrWidth);
    writeRegister(nRF24L01.TX_ADDR.i(), buffer, addrWidth);


    //const uint8_t max_payload_size = 32;
    //write_register(RX_PW_P0,rf24_min(payload_size,max_payload_size));
    writeRegister(nRF24L01.RX_PW_P0.i(), (byte) payloadSize);
  }

  /****************************************************************************/

  public void openWritingPipe(byte[] address) throws IOException {
    //Log.d(TAG,"openWritingPipe");
    // Note that AVR 8-bit uC's store this LSB first, and the NRF24L01(+)
    // expects it LSB first too, so we're good.

    writeRegister(nRF24L01.RX_ADDR_P0.i(), address, addrWidth);
    writeRegister(nRF24L01.TX_ADDR.i(), address, addrWidth);

    //const uint8_t max_payload_size = 32;
    //write_register(RX_PW_P0,rf24_min(payload_size,max_payload_size));
    writeRegister(nRF24L01.RX_PW_P0.i(), (byte) payloadSize);
  }

  public void openReadingPipe(byte child, long address) throws IOException {
    //Log.d(TAG,"openReadingPipe");
    // If this is pipe 0, cache the address.  This is needed because
    // openWritingPipe() will overwrite the pipe 0 address, so
    // startListening() will have to restore it.
    if (child == 0) {
      pipe0ReadingAddress = Longs.toByteArray(address);
    }

    if (child <= 6) {
      // For pipes 2-5, only write the LSB
      if (child < 2) {
        writeRegister(child_pipe[child], Longs.toByteArray(address), addrWidth);
      } else {
        writeRegister(child_pipe[child], Longs.toByteArray(address), 1);
      }

      writeRegister(child_payload_size[child], (byte) payloadSize);

      // Note it would be more efficient to set all of the bits for all open
      // pipes at once.  However, I thought it would make the calling code
      // more simple to do it this way.
      writeRegister(nRF24L01.EN_RXADDR.i(), (byte) (readRegister(nRF24L01.EN_RXADDR.i()) | _BV(childPipeEnable[child])));
    }
  }

  public void setAddressWidth(byte a_width) throws IOException {
    //Log.d(TAG,"setAddressWidth");
    a_width -= 2;
    if (a_width > 0) {
      writeRegister(nRF24L01.SETUP_AW.i(), (byte) (a_width % 4));
      addrWidth = (a_width % 4) + 2;
    }

  }

  public void openReadingPipe(byte child, byte[] address) throws IOException {
    //Log.d(TAG,"openReadingPipe");
    // If this is pipe 0, cache the address.  This is needed because
    // openWritingPipe() will overwrite the pipe 0 address, so
    // startListening() will have to restore it.
    if (child == 0) {
      pipe0ReadingAddress = address;
    }
    if (child <= 6) {
      // For pipes 2-5, only write the LSB
      if (child < 2) {
        writeRegister(child_pipe[child], address, addrWidth);
      } else {
        writeRegister(child_pipe[child], address, 1);
      }
      writeRegister(child_payload_size[child], (byte) payloadSize);

      // Note it would be more efficient to set all of the bits for all open
      // pipes at once.  However, I thought it would make the calling code
      // more simple to do it this way.
      writeRegister(nRF24L01.EN_RXADDR.i(), (byte) (readRegister(nRF24L01.EN_RXADDR.i()) | _BV(childPipeEnable[child])));

    }
  }

  /****************************************************************************/

  public void closeReadingPipe(byte pipe) throws IOException {
    //Log.d(TAG,"closeReadingPipe");
    writeRegister(nRF24L01.EN_RXADDR.i(), (byte) (readRegister(nRF24L01.EN_RXADDR.i()) & ~_BV(childPipeEnable[pipe])));
  }

  private void toggleFeatures() throws IOException {
    beginTransaction();
    byte[] buffer = new byte[1];
    buffer[0] = nRF24L01.ACTIVATE.i();
    device.write(buffer, 1);
    buffer[0] = 0x73;
    device.write(buffer, 1);

  }

  public void enableDynamicPayloads() throws IOException {
    // Enable dynamic payload throughout the system

    //toggle_features();
    writeRegister(nRF24L01.FEATURE.i(), (byte) (readRegister(nRF24L01.FEATURE.i()) | _BV(nRF24L01.EN_DPL.i())));


    // Enable dynamic payload on all pipes
    //
    // Not sure the use case of only having dynamic payload on certain
    // pipes, so the library does not support it.
    writeRegister(nRF24L01.DYNPD.i(), (byte) (readRegister(nRF24L01.DYNPD.i()) | _BV(nRF24L01.DPL_P5.i()) | _BV(nRF24L01.DPL_P4.i()) | _BV(nRF24L01.DPL_P3.i())
        | _BV(nRF24L01.DPL_P2.i()) | _BV(nRF24L01.DPL_P1.i()) | _BV(nRF24L01.DPL_P0.i())));

    dynamicPayloadsEnabled = true;
  }

  public void enableAckPayload() throws IOException {
    //
    // enable ack payload and dynamic payload features
    //

    //toggle_features();
    writeRegister(nRF24L01.FEATURE.i(), (byte) (readRegister(nRF24L01.FEATURE.i()) | _BV(nRF24L01.EN_ACK_PAY.i()) | _BV(nRF24L01.EN_DPL.i())));

    //
    // Enable dynamic payload on pipes 0 & 1
    //
    writeRegister(nRF24L01.DYNPD.i(), (byte) (readRegister(nRF24L01.DYNPD.i()) | _BV(nRF24L01.DPL_P1.i()) | _BV(nRF24L01.DPL_P0.i())));

    dynamicPayloadsEnabled = true;
  }

  /****************************************************************************/

  public void enableDynamicAck() throws IOException {
    //
    // enable dynamic ack features
    //
    //toggle_features();
    writeRegister(nRF24L01.FEATURE.i(), (byte) (readRegister(nRF24L01.FEATURE.i()) | _BV(nRF24L01.EN_DYN_ACK.i())));
  }

  public void writeAckPayload(byte pipe, byte[] buf, int len) throws IOException {
    int current = 0;

    int data_len = min((byte) len, (byte) 32);

    beginTransaction();
    int ptx = 0;
    int size = data_len + 1; // Add register value to transmit buffer
    spi_txbuff[ptx++] = (byte) (nRF24L01.W_ACK_PAYLOAD.i() | (pipe & 0b111));
    while (data_len-- > 0) {
      spi_txbuff[ptx++] = buf[current++];
    }

    transfer(spi_txbuff, null, size);

  }

  public boolean isAckPayloadAvailable() throws IOException {
    return 1 != (readRegister(nRF24L01.FIFO_STATUS.i()) & _BV(nRF24L01.RX_EMPTY.i()));
  }

  public void setAutoAck(boolean enable) throws IOException {
    writeRegister(nRF24L01.EN_AA.i(), (byte) (enable ? 0b111111 : 0));
  }

  public void setAutoAck(byte pipe, boolean enable) throws IOException {
    if (pipe <= 6) {
      byte en_aa = readRegister(nRF24L01.EN_AA.i());
      if (enable) {
        en_aa |= _BV(pipe);
      } else {
        en_aa &= ~_BV(pipe);
      }
      writeRegister(nRF24L01.EN_AA.i(), en_aa);
    }
  }

  public boolean testCarrier() throws IOException {
    return (readRegister(nRF24L01.CD.i()) & 1) > 0;
  }

  public boolean testRPD() throws IOException {
    return (readRegister(nRF24L01.RPD.i()) & 1) > 0;
  }

  /****************************************************************************/

  public byte getPALevel() throws IOException {

    return (byte) ((readRegister(nRF24L01.RF_SETUP.i()) & (_BV(nRF24L01.RF_PWR_LOW.i()) | _BV(nRF24L01.RF_PWR_HIGH.i()))) >> 1);
  }

  public void setPALevel(byte level) throws IOException {
    //Log.d(TAG,"setPALevel");
    byte setup = (byte) (readRegister(nRF24L01.RF_SETUP.i()) & 0b11111000);

    if (level > 3) {              // If invalid level, go to max PA
      level = (byte) ((rf24PaDBM.RF24_PA_MAX.ordinal() << 1) + 1);    // +1 to support the SI24R1 chip extra bit
    } else {
      level = (byte) ((level << 1) + 1);      // Else set level as requested
    }


    writeRegister(nRF24L01.RF_SETUP.i(), (byte) (setup | level));  // Write it to the chip
  }

  public boolean setDataRate(rf24DataRate speed) throws IOException {
    boolean result = false;
    byte setup = readRegister(nRF24L01.RF_SETUP.i());

    // HIGH and LOW '00' is 1Mbs - our default
    setup &= ~(_BV(nRF24L01.RF_DR_LOW.i()) | _BV(nRF24L01.RF_DR_HIGH.i()));

    txRxDelay = 250;

    if (speed == rf24DataRate.RF24_250KBPS) {
      // Must set the RF_DR_LOW to 1; RF_DR_HIGH (used to be RF_DR) is already 0
      // Making it '10'.
      setup |= _BV(nRF24L01.RF_DR_LOW.i());
      txRxDelay = 450;
    } else {
      // Set 2Mbs, RF_DR (RF_DR_HIGH) is set 1
      // Making it '01'
      if (speed == rf24DataRate.RF24_2MBPS) {
        setup |= _BV(nRF24L01.RF_DR_HIGH.i());
        txRxDelay = 190;
      }
    }
    writeRegister(nRF24L01.RF_SETUP.i(), setup);

    // Verify our result
    if (readRegister(nRF24L01.RF_SETUP.i()) == setup) {
      result = true;
    }
    return result;
  }

  public rf24DataRate getDataRate() throws IOException {
    byte dr = (byte) (readRegister(nRF24L01.RF_SETUP.i()) & (_BV(nRF24L01.RF_DR_LOW.i()) | _BV(nRF24L01.RF_DR_HIGH.i())));

    // switch uses RAM (evil!)
    // Order matters in our case below
    if (dr == _BV(nRF24L01.RF_DR_LOW.i())) {
      // '10' = 250KBPS
      return rf24DataRate.RF24_250KBPS;
    } else if (dr == _BV(nRF24L01.RF_DR_HIGH.i())) {
      // '01' = 2MBPS
      return rf24DataRate.RF24_2MBPS;
    } else {
      // '00' = 1MBPS
      return rf24DataRate.RF24_1MBPS;
    }
  }

  public rf24CRCLen getCRCLength() throws IOException {
    byte config = (byte) (readRegister(nRF24L01.CONFIG.i()) & (_BV(nRF24L01.CRCO.i()) | _BV(nRF24L01.EN_CRC.i())));
    byte AA = readRegister(nRF24L01.EN_AA.i());

    if (((config & _BV(nRF24L01.EN_CRC.i())) > 0) || (AA > 0)) {
      if ((config & _BV(nRF24L01.CRCO.i())) > 0)
        return rf24CRCLen.RF24_CRC_16;
      else
        return rf24CRCLen.RF24_CRC_8;
    }

    return rf24CRCLen.RF24_CRC_DISABLED;

  }

  public void setCRCLength(rf24CRCLen length) throws IOException {
    byte config = (byte) (readRegister(nRF24L01.CONFIG.i()) & ~(_BV(nRF24L01.CRCO.i()) | _BV(nRF24L01.EN_CRC.i())));

    // switch uses RAM (evil!)
    if (length == rf24CRCLen.RF24_CRC_DISABLED) {
      // Do nothing, we turned it off above.
    } else if (length == rf24CRCLen.RF24_CRC_8) {
      config |= _BV(nRF24L01.EN_CRC.i());
    } else {
      config |= _BV(nRF24L01.EN_CRC.i());
      config |= _BV(nRF24L01.CRCO.i());
    }
    writeRegister(nRF24L01.CONFIG.i(), config);
  }

  public void disableCRC() throws IOException {
    byte disable = (byte) (readRegister(nRF24L01.CONFIG.i()) & ~_BV(nRF24L01.EN_CRC.i()));
    writeRegister(nRF24L01.CONFIG.i(), disable);
  }

  public void setRetries(byte delay, byte count) throws IOException {
    //Log.d(TAG,"setRetries");
    writeRegister(nRF24L01.SETUP_RETR.i(), (byte) ((delay & 0xf) << nRF24L01.ARD.i() | (count & 0xf) << nRF24L01.ARC.i()));
  }

  public boolean isPVariant() {
    return pVariant;
  }

  public enum rf24PaDBM {
    RF24_PA_MIN, RF24_PA_LOW, RF24_PA_HIGH, RF24_PA_MAX, RF24_PA_ERROR
  }

  public enum rf24CRCLen {RF24_CRC_DISABLED, RF24_CRC_8, RF24_CRC_16}

  public enum rf24DataRate {RF24_1MBPS, RF24_2MBPS, RF24_250KBPS}

  public class WhatHappenedResult {
    boolean tx_ok;
    boolean tx_fail;
    boolean rx_ready;
  }

  public class ReturnBuffer {
    public int status;
    public byte[] buffer;
    public int bufferLen;

    public void copyBuffer(byte[] in, int start, int len) {
      buffer = new byte[len];
      int y = 0;
      for (int i = start; i < len + start; i++) {
        buffer[y++] = in[i];
      }
    }
  }

  /**
   * < Var for adjusting delays depending on datarate
   */


}