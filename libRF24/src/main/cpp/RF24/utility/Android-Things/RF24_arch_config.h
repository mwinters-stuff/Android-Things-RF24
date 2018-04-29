
#ifndef __ARCH_CONFIG_H__
#define __ARCH_CONFIG_H__

#define RF24_LINUX

#include <stdint.h>
#include <stdio.h>
#include <time.h>
#include <string.h>
#include <sys/time.h>
#include <stddef.h>
#include <pio/gpio.h>



  //#include "bcm2835.h"
  #include "spi.h"
  #define _SPI spi
	
  #if defined SPI_HAS_TRANSACTION && !defined SPI_UART && !defined SOFTSPI
    #define RF24_SPI_TRANSACTIONS
  #endif	
  // GCC a Arduino Missing
  #define _BV(x) (1<<(x))
  #define pgm_read_word(p) (*(p))
  #define pgm_read_byte(p) (*(p))

#define  LOG_TAG    "RF24"
void log_print(const char *tag, const char* format, ...);
//#define  ALOG(...)  log_print(LOG_TAG,__VA_ARGS__)

  //typedef uint16_t prog_uint16_t;
  #define PSTR(x) (x)
  #define printf_P(...) log_print(LOG_TAG,__VA_ARGS__)
  #define printf(...) log_print(LOG_TAG,__VA_ARGS__)
  #define strlen_P strlen
  #define PROGMEM
  #define PRIPSTR "%s"

  #ifdef SERIAL_DEBUG
	#define IF_SERIAL_DEBUG(x) ({x;})
  #else
	#define IF_SERIAL_DEBUG(x)
  #endif
  
  #define digitalWrite(pin, value) SPI::internalDigitalWrite(pin,value)
  #define pinMode(pin,value) SPI::internalPinMode(pin,value)
  #define OUTPUT AGPIO_DIRECTION_OUT_INITIALLY_LOW
#define LOW 0
#define HIGH 1

#define delay(time) SPI::internalDelay(time)
#define delayMicroseconds(time) SPI::internalDelayMicroseconds(time)
#define millis() SPI::internalMillis()


#endif