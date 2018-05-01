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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;
import android.util.Log;
import android.widget.TextView;

import com.fastaccess.permission.base.PermissionHelper;
import com.fastaccess.permission.base.callback.OnPermissionCallback;
import com.google.android.things.device.TimeManager;
import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManager;

import java.io.IOException;
import java.util.Locale;

import nz.org.winters.android.things.RF24.NativeRF24;


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

public class MainActivity extends Activity implements OnPermissionCallback {
  private static final String TAG = MainActivity.class.getSimpleName();

  static {
    System.loadLibrary("native-lib");
  }

  private static final int cePin = 25;
  private static final int spiSpeed = 8000000;
  private static final int spiBus = 0;
  private Gpio ledPinRed;

  boolean stop = false;
  String[] pipes_strs = {"1Node", "2Node"};

  private static int radioNumber = 0;

//  @ViewById(R.id.editTextLog)
  TextView editTextLog;
  private PermissionHelper permissionHelper;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main_activity);


//    permissionHelper = PermissionHelper.getInstance(this, this);
//    permissionHelper.setForceAccepting(true).request("com.google.android.things.permission.MODIFY_SCREEN_SETTINGS");
//    permissionHelper.setForceAccepting(true).request("com.google.android.things.permission.CHANGE_TIME");

    TimeManager timeManager = TimeManager.getInstance();
    timeManager.setTimeZone("Pacific/Auckland");

    editTextLog = findViewById(R.id.editTextLog);

    try {
      PeripheralManager peripheralManagerService = PeripheralManager.getInstance();


      //cePin = peripheralManagerService.openGpio("BCM22");
      ledPinRed = peripheralManagerService.openGpio("BCM13");
      ledPinRed.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
      ledPinRed.setValue(false);

      //    pingRadioThread(peripheralManagerService);

    } catch (Exception e) { // NOSONAR
      Log.d("ERROR", "Exception: " + e.getMessage());
    }

    connectButtons();
  }




  static final String send_payload_str = "ABCDEFGHIJKLMNOPQRSTUVWXYZ789012";
  static long[] pipes = {0xF0F0F0F0E1L, 0xF0F0F0F0D2L};
  static final byte[][] dyn_pipes = {{(byte)0xF0,(byte)0xF0,(byte)0xF0,(byte)0xF0,(byte)0xE1}, {(byte)0xF0,(byte)0xF0,(byte)0xF0,(byte)0xF0,(byte)0xD2}};

  @WorkerThread
  void dynPairPong() {
    stop = false;
    try (NativeRF24 radio = new NativeRF24(cePin, spiSpeed, spiBus)) {

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


@WorkerThread
  void dynPairPing() {
    stop = false;
    try (NativeRF24 radio = new NativeRF24(cePin, spiSpeed, spiBus)) {
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


  @WorkerThread
  void pingOutCallResponse(){
    stop = false;
    try (NativeRF24 radio = new NativeRF24(cePin, spiSpeed, spiBus)) {
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
        log( String.format(Locale.getDefault(),"Now sending %d as payload.", counter));
        long time = SystemClock.uptimeMillis();
        if (radio.write(new byte[]{counter}, 1)) {
          if (!radio.available()) {
            log( String.format(Locale.getDefault(),"Got blank response. round trip delay: %d", SystemClock.uptimeMillis() - time));
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

  @WorkerThread
  void pongBackCallResponse() {
    stop = false;
    try (NativeRF24 radio = new NativeRF24(cePin, spiSpeed, spiBus)) {

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
          Thread.sleep(1000);
        }
        //      radio.flushRx();
        //      radio.flushTx();

      }
    } catch (Exception e) {
      log(e.toString());
    }

  }


  @WorkerThread
  void pongBack() {
    stop = false;
    try(NativeRF24 radio = new NativeRF24(cePin, spiSpeed, spiBus)) {
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

  @WorkerThread
  void pingOut()  {
    stop = false;
    try (NativeRF24 radio = new NativeRF24(cePin, spiSpeed, spiBus)) {
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
          log("Send Failed");
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
          log( "Response timed out");
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

  public static final long[] ack_pipes = { 0xABCDABCD71L, 0x544d52687CL };              // Radio pipe addresses for the 2 nodes to communicate.

  @WorkerThread
  void ackPingOut()  {
    stop = false;
    try (NativeRF24 radio = new NativeRF24(cePin, spiSpeed, spiBus)) {
      radio.begin();

      radio.setAutoAck(true);
      radio.enableAckPayload();
      radio.setRetries((byte) 0, (byte) 15);
      radio.setPayloadSize(1);

      radio.openWritingPipe(ack_pipes[0]);
      radio.openReadingPipe(1, ack_pipes[1]);

      radio.startListening();
      radio.printDetails();

      byte counter = 1;

      while (!stop) {
        radio.stopListening();
        log(String.format(Locale.getDefault(),"Now sending %d as payload.",counter));
        byte gotByte;

        long time = SystemClock.uptimeMillis();

        boolean ok = radio.write(new byte[]{counter}, 0);

        if (!ok) {
          log("Send Failed");
        }else{
          if(!radio.available()){
            log("Blank Payload Received");
          }
          while (radio.available()) {
            long tim = SystemClock.uptimeMillis();
            gotByte = radio.read(1)[0];
            log(String.format(Locale.getDefault(),"Got response %d, round-trip delay: %d microseconds.",gotByte,tim-time));
            counter++;
          }
        }
        Thread.sleep(1000);
      }
    } catch (Exception e) {
      log(e.toString());
    }
  }
  @WorkerThread
  void ackPongBack()  {
    stop = false;
    try (NativeRF24 radio = new NativeRF24(cePin, spiSpeed, spiBus)) {
      radio.begin();
      radio.setAutoAck(true);
      radio.enableAckPayload();
      radio.setRetries((byte) 0, (byte) 15);
      radio.setPayloadSize(1);

      radio.openWritingPipe(ack_pipes[1]);
      radio.openReadingPipe(1, ack_pipes[0]);

      radio.startListening();
      radio.printDetails();

      while (!stop) {
        int pipe = radio.availablePipe();
        if(pipe >=0){
          byte[] got = radio.read(1);
          radio.writeAckPayload(pipe,got,1);
          log(String.format(Locale.getDefault(),"Got and Ack'd %d",got[0]));
        }
      }
    } catch (Exception e) {
      log(e.toString());
    }
  }
//
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

  void connectButtons() {
    findViewById(R.id.buttonStop).setOnClickListener(v -> {
      stop = true;
      setTitle(String.format(Locale.getDefault(), getString(R.string.title_plus), getString(R.string.stopped)));
    });

    findViewById(R.id.buttonPongBack).setOnClickListener(v -> {
      stop = true;
      setTitle(String.format(Locale.getDefault(), getString(R.string.title_plus), getString(R.string.basic_pong_back)));
      new Thread(this::pongBack).start();
    });

    findViewById(R.id.buttonPingOut).setOnClickListener(v -> {
    stop = true;
    setTitle(String.format(Locale.getDefault(), getString(R.string.title_plus), getString(R.string.basic_ping_out)));
      new Thread(this::pingOut).start();
    });

    findViewById(R.id.buttonCRPingOut).setOnClickListener(v -> {
    stop = true;
    setTitle(String.format(Locale.getDefault(), getString(R.string.title_plus), getString(R.string.call_response_ping_out)));
      new Thread(this::pingOutCallResponse).start();
    });

    findViewById(R.id.buttonCRPongBack).setOnClickListener(v -> {
    stop = true;
    setTitle(String.format(Locale.getDefault(), getString(R.string.title_plus), getString(R.string.call_response_pong_back)));
      new Thread(this::pongBackCallResponse).start();
    });

    findViewById(R.id.buttonPPDPingOut).setOnClickListener(v -> {
    stop = true;
    setTitle(String.format(Locale.getDefault(), getString(R.string.title_plus), getString(R.string.dyn_pair_ping_out)));
      new Thread(this::dynPairPing).start();
    });

    findViewById(R.id.buttonPPDPongBack).setOnClickListener(v -> {
    stop = true;
    setTitle(String.format(Locale.getDefault(), getString(R.string.title_plus), getString(R.string.dyn_pair_pong_back)));
      new Thread(this::dynPairPong).start();
    });

    findViewById(R.id.buttonAckPingOut).setOnClickListener(v -> {
    setTitle(String.format(Locale.getDefault(), getString(R.string.title_plus), getString(R.string.ack_ping_out)));
    stop = true;
      new Thread(this::ackPingOut).start();
    });

    findViewById(R.id.buttonAckPongBack).setOnClickListener(v -> {
    setTitle(String.format(Locale.getDefault(), getString(R.string.title_plus), getString(R.string.ack_pong_back)));
    stop = true;
      new Thread(this::ackPongBack).start();
    });

    findViewById(R.id.buttonClearLog).setOnClickListener(v -> {
      editTextLog.setText("");
    });
  }


  @SuppressLint("SetTextI18n")
  @WorkerThread
  void log(String value){
    runOnUiThread(() -> editTextLog.setText(editTextLog.getText() + value + '\n'));
  }


  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                         @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    permissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
  }

  @Override
  public void onPermissionGranted(@NonNull String[] permissionName) {
    Log.d(TAG, "onPermissionGranted: " + permissionName[0]);
  }

  @Override
  public void onPermissionDeclined(@NonNull String[] permissionName) {
    Log.d(TAG, "onPermissionDeclined: " + permissionName[0]);
  }

  @Override
  public void onPermissionPreGranted(@NonNull String permissionsName) {
    Log.d(TAG, "onPermissionPreGranted: " + permissionsName);
  }

  @Override
  public void onPermissionNeedExplanation(@NonNull String permissionName) {
    Log.d(TAG, "onPermissionNeedExplanation: " + permissionName);
  }

  @Override
  public void onPermissionReallyDeclined(@NonNull String permissionName) {
    Log.d(TAG, "onPermissionReallyDeclined: " + permissionName);
  }

  @Override
  public void onNoPermissionNeeded() {
    Log.d(TAG, "onNoPermissionNeeded");
  }

}
