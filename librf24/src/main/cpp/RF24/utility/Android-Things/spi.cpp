#include "spi.h"
#include <pio/peripheral_manager_client.h>
#include <pthread.h>

static pthread_mutex_t spiMutex = PTHREAD_MUTEX_INITIALIZER;
ASpiDevice *SPI::spiDevice;
APeripheralManagerClient* SPI::peripheralManagerClient;

SPI::SPI() {

}


void SPI::begin( int busNo ) {
	peripheralManagerClient = APeripheralManagerClient_new();
    int num_spi_buses;
    char** spiBuses = APeripheralManagerClient_listSpiBuses(peripheralManagerClient,&num_spi_buses);
    APeripheralManagerClient_openSpiDevice(peripheralManagerClient,spiBuses[busNo],&spiDevice);
    free(spiBuses);
}

void SPI::beginTransaction(SPISettings settings){
   	pthread_mutex_lock (&spiMutex);
	setBitOrder(settings.border);
	setDataMode(settings.dmode);
	setClockDivider(settings.clck);
}

void SPI::endTransaction() {
	pthread_mutex_unlock (&spiMutex);
}

void SPI::setBitOrder(uint8_t bit_order) {
    ASpiDevice_setBitJustification(spiDevice,(ASpiBitJustification)bit_order);
}

void SPI::setDataMode(uint8_t data_mode) {
   ASpiDevice_setMode(spiDevice,(ASpiMode)data_mode);
}

void SPI::setClockDivider(uint32_t spi_speed) {
    ASpiDevice_setFrequency(spiDevice,spi_speed);
}

void SPI::chipSelect(int /*csn_pin*/){
//	bcm2835_spi_chipSelect(csn_pin);
//	delayMicroseconds(5);
}

SPI::~SPI() {

}