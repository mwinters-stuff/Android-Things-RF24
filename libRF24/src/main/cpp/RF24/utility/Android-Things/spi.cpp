#include "spi.h"
#include <pthread.h>
#include <stdlib.h>

static pthread_mutex_t spiMutex = PTHREAD_MUTEX_INITIALIZER;
ASpiDevice *SPI::spiDevice;
APeripheralManagerClient *SPI::peripheralManagerClient;
AGpio* SPI::gpioPins[2];
int SPI::gpioPinNumbers[2];


/*
 * initialiseEpoch:
 *	Initialise our start-of-time variable to be the current unix
 *	time in milliseconds and microseconds.
 *********************************************************************************
 */
// Time for easy calculations

static uint64_t epochMilli, epochMicro ;

static void initialiseEpoch (void)
{
  struct timeval tv ;

  gettimeofday (&tv, NULL) ;
  epochMilli = (uint64_t)tv.tv_sec * (uint64_t)1000    + (uint64_t)(tv.tv_usec / 1000) ;
  epochMicro = (uint64_t)tv.tv_sec * (uint64_t)1000000 + (uint64_t)(tv.tv_usec) ;
}

SPI::SPI() {
  initialiseEpoch();
}

void SPI::begin(int busNo) {
  peripheralManagerClient = APeripheralManagerClient_new();
  int num_spi_buses;
  char **spiBuses = APeripheralManagerClient_listSpiBuses(peripheralManagerClient, &num_spi_buses);
  APeripheralManagerClient_openSpiDevice(peripheralManagerClient, spiBuses[busNo], &spiDevice);
  free(spiBuses);
}

void SPI::beginTransaction(SPISettings settings) {
  pthread_mutex_lock(&spiMutex);
  setBitOrder(settings.border);
  setDataMode(settings.dmode);
  setClockDivider(settings.clck);
}

void SPI::endTransaction() {
  pthread_mutex_unlock(&spiMutex);
}

void SPI::setBitOrder(uint8_t bit_order) {
  ASpiDevice_setBitJustification(spiDevice, (ASpiBitJustification) bit_order);
}

void SPI::setDataMode(uint8_t data_mode) {
  ASpiDevice_setMode(spiDevice, (ASpiMode) data_mode);
}

void SPI::setClockDivider(uint32_t spi_speed) {
  ASpiDevice_setFrequency(spiDevice, spi_speed);
}

void SPI::chipSelect(int /*csn_pin*/) {
//	bcm2835_spi_chipSelect(csn_pin);
//	delayMicroseconds(5);
}

void SPI::internalDigitalWrite(int pin, int value) {
  AGpio_setValue(getGpioPin(pin),value);
}

void SPI::internalPinMode(int pin, int value) {
  AGpio_setDirection(getGpioPin(pin), (AGpioDirection) value);
}

AGpio *SPI::getGpioPin(int pin){
  if(pin == gpioPinNumbers[0]){
    return gpioPins[0];
  }
  if(pin == gpioPinNumbers[1]){
    return gpioPins[1];
  }
  char name[20];
  sprintf(name,"BCM%d",pin);
  AGpio *aGpio;
  APeripheralManagerClient_openGpio(peripheralManagerClient,name,&aGpio);
  int index = 0;
  if(gpioPinNumbers[index] != 0){
    index++;
  }
  gpioPinNumbers[index] = pin;
  gpioPins[index] = aGpio;
  return aGpio;
}

SPI::~SPI() {
  gpioPinNumbers[0] = 0;
  gpioPinNumbers[1] = 0;
}

void SPI::end() {
  if(gpioPins[0]){
    AGpio_delete(gpioPins[0]);
  }
  if(gpioPins[1]){
    AGpio_delete(gpioPins[1]);
  }
  ASpiDevice_delete(spiDevice);
  APeripheralManagerClient_delete(peripheralManagerClient);
}




/*
 * delay:
 *	Wait for some number of milliseconds
 *********************************************************************************
 */

void delay (unsigned int howLong)
{
  struct timespec sleeper, dummy ;

  sleeper.tv_sec  = (time_t)(howLong / 1000) ;
  sleeper.tv_nsec = (long)(howLong % 1000) * 1000000 ;

  nanosleep (&sleeper, &dummy) ;
}


/*
 * delayMicroseconds:
 *	This is somewhat intersting. It seems that on the Pi, a single call
 *	to nanosleep takes some 80 to 130 microseconds anyway, so while
 *	obeying the standards (may take longer), it's not always what we
 *	want!
 *
 *	So what I'll do now is if the delay is less than 100uS we'll do it
 *	in a hard loop, watching a built-in counter on the ARM chip. This is
 *	somewhat sub-optimal in that it uses 100% CPU, something not an issue
 *	in a microcontroller, but under a multi-tasking, multi-user OS, it's
 *	wastefull, however we've no real choice )-:
 *
 *      Plan B: It seems all might not be well with that plan, so changing it
 *      to use gettimeofday () and poll on that instead...
 *********************************************************************************
 */

void delayMicrosecondsHard (unsigned int howLong)
{
  struct timeval tNow, tLong, tEnd ;

  gettimeofday (&tNow, NULL) ;
  tLong.tv_sec  = howLong / 1000000 ;
  tLong.tv_usec = howLong % 1000000 ;
  timeradd (&tNow, &tLong, &tEnd) ;

  while (timercmp (&tNow, &tEnd, <))
    gettimeofday (&tNow, NULL) ;
}

void delayMicroseconds (unsigned int howLong)
{
  struct timespec sleeper ;
  unsigned int uSecs = howLong % 1000000 ;
  unsigned int wSecs = howLong / 1000000 ;

  /**/ if (howLong ==   0)
    return ;
  else if (howLong  < 100)
    delayMicrosecondsHard (howLong) ;
  else
  {
    sleeper.tv_sec  = wSecs ;
    sleeper.tv_nsec = (long)(uSecs * 1000L) ;
    nanosleep (&sleeper, NULL) ;
  }
}


/*
 * millis:
 *	Return a number of milliseconds as an unsigned int.
 *********************************************************************************
 */

unsigned int millis (void)
{
  struct timeval tv ;
  uint64_t now ;

  gettimeofday (&tv, NULL) ;
  now  = (uint64_t)tv.tv_sec * (uint64_t)1000 + (uint64_t)(tv.tv_usec / 1000) ;

  return (uint32_t)(now - epochMilli) ;
}


/*
 * micros:
 *	Return a number of microseconds as an unsigned int.
 *********************************************************************************
 */

unsigned int micros (void)
{
  struct timeval tv ;
  uint64_t now ;

  gettimeofday (&tv, NULL) ;
  now  = (uint64_t)tv.tv_sec * (uint64_t)1000000 + (uint64_t)tv.tv_usec ;

  return (uint32_t)(now - epochMicro) ;
}


void SPI::internalDelay(uint32_t _delay) {
  delay(_delay);
}

void SPI::internalDelayMicroseconds(uint32_t _delay) {
  delayMicrosecondsHard(_delay);
}

uint32_t SPI::internalMillis() {
  return millis();
}
