#include "spi.h"
#include <pthread.h>
#include <unistd.h>
#include <math.h>

static pthread_mutex_t spiMutex = PTHREAD_MUTEX_INITIALIZER;
ASpiDevice *SPI::spiDevice;
APeripheralManagerClient *SPI::peripheralManagerClient;
AGpio* SPI::gpioPins[2];
int SPI::gpioPinNumbers[2];

SPI::SPI() {

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

void SPI::internalDelay(uint32_t delay) {
  usleep(delay * 1000);
}

void SPI::internalDelayMicroseconds(uint32_t delay) {
  usleep(delay);
}

uint32_t SPI::internalMillis() {
  struct timespec spec;

  clock_gettime(CLOCK_MONOTONIC, &spec);
  return (uint32_t) round(spec.tv_nsec / 1.0e6);
}
