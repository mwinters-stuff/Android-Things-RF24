
# Android-Things-RF24
Android-Things port of RF24 for the nRF24L01(+) 2.4GHz Wireless Transceiver

This is a working implementation of RF24 and RF24Network with Android-Things on a Raspberry Pi 3.

This is based off the work from [https://github.com/TMRh20/RF24]() and is a cpp implementation with changes for ndk and with
an android library wrapper.

# Example
The example is a simple gui for running the PING tests, which can be used to test the RF24 device.

## Configuration
The current configuration is the CE is CE0, and the CSN pin is GPIO22 or Pin 15.

Currently there is no GUI, change the example by changing what is enabled in the background thread
only one at a time will work so don't bother enabling more.

# Installing
Add the repo http://dl.bintray.com/wintersandroid/maven, for example in you base build.gradle
```
allprojects {
    repositories {
       jcenter()
       google()
        maven { url  "http://dl.bintray.com/wintersandroid/maven" }
    }
}
```
Then import the project in your dependancies.

```
    implementation "nz.org.winters:libRF24:{version}"
```

Find the version in the release tags.

# Using RF24 & RF24Network
Essentially the same as what is documented on the RF24 Site, just use NativeRF24 class, and
NativeRF24Network classes.

```java
  static final String send_payload_str = "ABCDEFGHIJKLMNOPQRSTUVWXYZ789012";
  static long[] pipes = {0xF0F0F0F0E1L, 0xF0F0F0F0D2L};

  void test(){
    NativeRF24 radio = new NativeRF24(cePin, spiSpeed, spiBus);

    radio.begin();
    radio.enableDynamicPayloads();
    radio.setRetries((byte) 5, (byte) 15);
    radio.openWritingPipe(pipes[1]);
    radio.openReadingPipe((byte) 1, pipes[0]);

    radio.printDetails();

    radio.write(send_payload_str, send_payload_str.length());

    radio.startListening();

    if(radio.available()) {
       len = radio.getDynamicPayloadSize();
       receive_payload = radio.read(len);
    }
    radio.startListening();

}

```

