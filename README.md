# Android-Things-RF24
Android-Things port of RF24 for the nRF24L01(+) 2.4GHz Wireless Transceiver

This is a (working) try at using RF24 with Android-Things on a Raspberry Pi 3.

This is based off the work from [https://github.com/TMRh20/RF24]() and is a pure
java implementation.

All the basic raspberry pi examples work.

## Configuration
The current configuration is the CE is CE0, and the CSN pin is GPIO22 or Pin 15.

Currently there is no GUI, change the example by changing what is enabled in the background thread
only one at a time will work so don't bother enabling more.


