/*
 * Copyright 2016, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.androidthings.rf24;

import android.app.Activity;
import android.os.SystemClock;
import android.util.Log;
import android.widget.EditText;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManagerService;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.io.IOException;
import java.util.Locale;


/**
 * Skeleton of the main Android Things activity. Implement your device's logic
 * in this class.
 * <p>
 * Android Things peripheral APIs are accessible through the class
 * PeripheralManagerService. For example, the snippet below will open a GPIO pin and
 * set it to HIGH:
 * <p>
 * <pre>{@code
 * PeripheralManagerService service = new PeripheralManagerService();
 * mLedGpio = service.openGpio("BCM6");
 * mLedGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
 * mLedGpio.setValue(true);
 * }</pre>
 * <p>
 * For more complex peripherals, look for an existing user-space driver, or implement one if none
 * is available.
 */

@EActivity(R.layout.main_activity)
public class MainActivity extends Activity {
  private static final String TAG = MainActivity.class.getSimpleName();

  static {
    System.loadLibrary("native-lib");
  }

  @Pref
  AppPrefs_ appPrefs;

  private static final int cePin = 25;
  private static final int spiSpeed = 8000000;
  private Gpio ledPinRed;

  boolean stop = false;
  String[] pipes_strs = {"1Node", "2Node"};

  private static int radioNumber = 0;

  @ViewById(R.id.editTextLog)
  EditText editTextLog;


  @AfterViews
  protected void onAfterViews() {

    try {
      PeripheralManagerService peripheralManagerService = new PeripheralManagerService();


      //cePin = peripheralManagerService.openGpio("BCM22");
      ledPinRed = peripheralManagerService.openGpio("BCM13");
      ledPinRed.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
      ledPinRed.setValue(false);

      //    pingRadioThread(peripheralManagerService);

    } catch (Exception e) { // NOSONAR
      Log.d("ERROR", "Exception: " + e.getMessage());
    }
  }


  @Background(serial = "RF24")
  void pingOutCallResponse(){
    stop = false;
    try (NativeRF24 radio = new NativeRF24(cePin, spiSpeed, 1)) {
      radio.begin();
      radio.enableAckPayload();
      radio.enableDynamicPayloads();

      if (radioNumber == 1) {
        radio.openWritingPipeStr(pipes_strs[1]);
        radio.openReadingPipeStr(1, pipes_strs[0]);
      } else {
        radio.openWritingPipeStr(pipes_strs[0]);
        radio.openReadingPipeStr(1, pipes_strs[1]);
      }


      radio.printDetails();

      byte counter = 1;

      radio.startListening();
      radio.writeAckPayload((byte) 1, new byte[]{counter}, 1);

      while (!stop) {
        radio.stopListening();
        log( String.format("Now sending %d as payload.", counter));
        long time = SystemClock.uptimeMillis();
        if (radio.write(new byte[]{counter}, 1)) {
          if (!radio.available()) {
            log( String.format("Got blank response. round trip delay: %d", SystemClock.uptimeMillis() - time));
          } else {
            while (radio.available()) {
              byte[] buffer = radio.read(1);
              log( String.format(Locale.getDefault(),"Got response %d. round-trip delay %d", buffer[0], SystemClock.uptimeMillis() - time));
              counter++;
            }
          }
        } else {
          log( "Sending Failed.");
        }
        Thread.sleep(1000);
      }
    } catch (Exception e) {
      log(e.toString());

    }
  }


  static final String send_payload_str = "ABCDEFGHIJKLMNOPQRSTUVWXYZ789012";
  static long[] pipes = {0xF0F0F0F0E1L, 0xF0F0F0F0D2L};
//  static final byte[][] dyn_pipes = {{(byte)0xF0,(byte)0xF0,(byte)0xF0,(byte)0xF0,(byte)0xE1}, {(byte)0xF0,(byte)0xF0,(byte)0xF0,(byte)0xF0,(byte)0xD2}};

  @Background(serial = "RF24")
  void dynPairPong() {
    stop = false;
    try (NativeRF24 radio = new NativeRF24(cePin, spiSpeed, 1)) {

      radio.begin();
      radio.enableDynamicPayloads();
      radio.setRetries((byte) 5, (byte) 15);
//radio.setChannel(0x60);

      radio.openWritingPipe(pipes[1]);
      radio.openReadingPipe((byte) 1, pipes[0]);
      //Log.d(TAG, radio.printDetails())
      radio.printDetails();
      radio.startListening();

//      int nextPayloadSize = 4;

//      byte[] send_payload = send_payload_str.getBytes();

      while (!stop) {

        if (radio.available()) {
          flipLED();
          int len = 0;
          byte[] receive_payload = {};
          while (radio.available()) {
            len = radio.getDynamicPayloadSize();
            receive_payload = radio.read(len);

            log( String.format(Locale.getDefault(),"Got payload size %d value %s", len, new String(receive_payload)));
          }
          radio.stopListening();
          if (len > 0) {
            radio.write(receive_payload, len);
            log( "Sent Response");
          }
          radio.startListening();
          flipLED();
        }


      }
    } catch (Exception e) {
      log(e.toString());
    }
  }


  @Background(serial = "RF24")
  void dynPairPing() {
    stop = false;
    try (NativeRF24 radio = new NativeRF24(cePin, spiSpeed, 1)) {
      radio.begin();
      radio.enableDynamicPayloads();
      radio.setRetries((byte) 5, (byte) 15);


      radio.openWritingPipe(pipes[0]);
      radio.openReadingPipe((byte) 1, pipes[1]);
      radio.printDetails();

      int nextPayloadSize = 4;

      byte[] send_payload = send_payload_str.getBytes();

      while (!stop) {
        radio.stopListening();
        log( String.format(Locale.getDefault(),"Now Sending length %d", nextPayloadSize));

        radio.write(send_payload, nextPayloadSize);

        radio.startListening();

        long started_waiting_at = SystemClock.uptimeMillis();
        boolean timeout = false;
        while (!radio.available() && !timeout) {
          if (SystemClock.uptimeMillis() - started_waiting_at > 500) {
            timeout = true;
          }
        }

        if (timeout) {
          log( "Failed, response timeout,");
        } else {
          int len = radio.getDynamicPayloadSize();
          if (len > 0) {
            byte[] receive_payload = radio.read(len);
            // receive_payload[len] = 0;
            log( String.format(Locale.getDefault(),"got response size %d value=%s", len, new String(receive_payload)));
          } else {
            log( "Dynamic payload size = 0");
          }
        }
        nextPayloadSize += 1;
        if (nextPayloadSize > 32) {
          nextPayloadSize = 4;
        }
        Thread.sleep(100);


      }
    } catch (Exception e) {
      log(e.toString());
    }
  }


  @Background(serial = "RF24")
  void pongBackCallResponse() {
    stop = false;
    try (NativeRF24 radio = new NativeRF24(cePin, spiSpeed, 1)) {

      radio.begin();
      radio.enableAckPayload();
      radio.enableDynamicPayloads();

      if (radioNumber == 1) {
        radio.openWritingPipeStr(pipes_strs[1]);
        radio.openReadingPipeStr(1, pipes_strs[0]);
      } else {
        radio.openWritingPipeStr(pipes_strs[0]);
        radio.openReadingPipeStr(1, pipes_strs[1]);
      }
      radio.printDetails();

      byte counter = 1;

      radio.startListening();
      radio.writeAckPayload((byte) 1, new byte[]{counter}, 1);

      while (!stop) {
        int pipeNo = radio.availablePipe();
        if (pipeNo > -1) {
          byte[] buffer = radio.read(1);
          buffer[0] += 1;
          radio.writeAckPayload(pipeNo, buffer, 1);
          log( String.format(Locale.getDefault(),"Loaded next response for pipe %d response %d", pipeNo, buffer[0]));
          Thread.sleep(900);
        } else {
          log( "No available");
          //        Thread.sleep(1000);
        }
        //      radio.flushRx();
        //      radio.flushTx();

      }
    } catch (Exception e) {
      log(e.toString());
    }

  }


  @Background(serial = "RF24")
  void pongBack() {
    stop = false;
    try(NativeRF24 radio = new NativeRF24(cePin, spiSpeed, 1)) {
      radio.begin();
      radio.setPALevel(NativeRF24.RF24_PA_LOW);
      //radio.setRetries((byte) 15, (byte) 15);

      if (radioNumber == 1) {
        radio.openWritingPipeStr(pipes_strs[1]);
        radio.openReadingPipeStr(1, pipes_strs[0]);
      } else {
        radio.openWritingPipeStr(pipes_strs[0]);
        radio.openReadingPipeStr(1, pipes_strs[1]);
      }

      radio.startListening();
      radio.printDetails();
      while (!stop) {
        if (radio.available()) {
          long got_time = 0;
          while (radio.available()) {
            byte[] got_buffer = radio.read(4);
            got_time = byteArrayToClong(got_buffer);
          }
          radio.stopListening();
          radio.write(longToCByteArray(got_time), 4);

          radio.startListening();

          log( String.format(Locale.getDefault(),"Got payload %d...", got_time));

          Thread.sleep(925);
        }
        //stop = true;
      }
    } catch (Exception e) {
      log(e.toString());
    }
  }

  @Background(serial = "RF24")
  void pingOut()  {
    stop = false;
    try (NativeRF24 radio = new NativeRF24(cePin, spiSpeed, 1)) {
      radio.begin();

      radio.setRetries((byte) 15, (byte) 15);

      if (radioNumber == 1) {
        radio.openWritingPipeStr(pipes_strs[1]);
        radio.openReadingPipeStr(1, pipes_strs[0]);
      } else {
        radio.openWritingPipeStr(pipes_strs[0]);
        radio.openReadingPipeStr(1, pipes_strs[1]);
      }

      radio.printDetails();

      radio.startListening();
      while (!stop) {
        radio.stopListening();
        log( "Sending...");
        long time = SystemClock.uptimeMillis();
        byte[] buffer = longToCByteArray(time);
//            Longs.toByteArray(time);
//        for(int x = 0; x < 4; x++){
//          buffer[x] = buffer[x+4];
//        }
        boolean ok = radio.write(buffer, 4);

        if (!ok) {
          Log.e(TAG, "Send Failed");
        }
        radio.startListening();
        long started_waiting_at = SystemClock.uptimeMillis();
        boolean timeout = false;
        while (!radio.available() && !timeout) {
          if (SystemClock.uptimeMillis() - started_waiting_at > 200) {
            timeout = true;
          }
        }

        if (timeout) {
          Log.e(TAG, "Response timed out");
        } else {
          byte[] got_buffer = radio.read(4);
          long got_time = byteArrayToClong(got_buffer);

          log( String.format(Locale.getDefault(),"GOT Response %d, sent %d, trip delay %d", got_time, time, SystemClock.uptimeMillis() - time));
        }

        Thread.sleep(1000);

      }
    } catch (Exception e) {
      log(e.toString());
    }
  }

  @Override
  public void onPause() {
    stop = true;
    super.onPause();
  }


  @Override
  protected void onDestroy() {
    super.onDestroy();

    try {
      ledPinRed.setValue(false);
    } catch (IOException ignore) {
    }
  }


  private void flipLED() throws IOException {
    if (ledPinRed != null) {
      ledPinRed.setValue(!ledPinRed.getValue());
    }
  }

  public native byte[] longToCByteArray(long value);

  public native long byteArrayToClong(byte[] array);

  @Click(R.id.buttonStop)
  void onClickStop() {
    stop = true;
    setTitle(String.format(Locale.getDefault(), getString(R.string.title_plus), getString(R.string.stopped)));
  }

  @Click(R.id.buttonPongBack)
  void onClickPongBack() {
    stop = true;
    setTitle(String.format(Locale.getDefault(), getString(R.string.title_plus), getString(R.string.basic_pong_back)));
    pongBack();
  }

  @Click(R.id.buttonPingOut)
  void onClickPingOut() {
    stop = true;
    setTitle(String.format(Locale.getDefault(), getString(R.string.title_plus), getString(R.string.basic_ping_out)));
    pingOut();
  }

  @Click(R.id.buttonCRPingOut)
  void onClickCRPingOut(){
    stop = true;
    setTitle(String.format(Locale.getDefault(), getString(R.string.title_plus), getString(R.string.call_response_ping_out)));
    pingOutCallResponse();
  }

  @Click(R.id.buttonCRPongBack)
  void onClickCRPongBack(){
    stop = true;
    setTitle(String.format(Locale.getDefault(), getString(R.string.title_plus), getString(R.string.call_response_pong_back)));
    pongBackCallResponse();
  }

  @Click(R.id.buttonPPDPingOut)
  void onClickPPDPingOut(){
    stop = true;
    setTitle(String.format(Locale.getDefault(), getString(R.string.title_plus), getString(R.string.dyn_pair_ping_out)));
    dynPairPing();
  }

  @Click(R.id.buttonPPDPongBack)
  void onClickPPDPongBack(){
    stop = true;
    setTitle(String.format(Locale.getDefault(), getString(R.string.title_plus), getString(R.string.dyn_pair_pong_back)));
    dynPairPong();
  }

  @Click(R.id.buttonClearLog)
  void onClickClearLog(){
    editTextLog.setText("");
  }

  void log(String value){
    editTextLog.getText().append(value + '\n');
  }

}
