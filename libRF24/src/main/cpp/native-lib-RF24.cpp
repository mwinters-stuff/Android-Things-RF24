//
// Created by mathew on 10/04/17.
//

#include <jni.h>
#include <string>
#include "RF24/RF24.h"

#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wunused-parameter"

RF24 *radio = NULL;


extern "C"
JNIEXPORT void JNICALL
Java_nz_org_winters_android_things_RF24_NativeRF24_init(JNIEnv *env, jobject instance, jint cePin,
                                                    jint spiFrequency, jint spiBus) {

  radio = new RF24((uint16_t) cePin, (uint16_t) spiBus, (uint32_t) spiFrequency);

}

//extern "C"
//void
//Java_nz_org_winters_android_things_RF24_NativeRF24_init(JNIEnv *env, jobject instance, jint cePin,
//                                                    jint spiFrequency, jint spiBus) {
//    radio = new RF24((uint16_t) cePin, (uint16_t) spiBus, (uint32_t) spiFrequency);
//}

extern "C"
JNIEXPORT jboolean JNICALL
Java_nz_org_winters_android_things_RF24_NativeRF24_available(JNIEnv *env, jobject instance) {
  if (radio) {
    return (jboolean) radio->available();
  }
  return (jboolean) false;
}

extern "C"
JNIEXPORT jint JNICALL
Java_nz_org_winters_android_things_RF24_NativeRF24_availablePipe(JNIEnv *env, jobject instance) {
  if (radio) {
    uint8_t pipe;
    if (radio->available(&pipe)) {
      return pipe;
    }
  }
  return -1;
}

extern "C"
JNIEXPORT void JNICALL
Java_nz_org_winters_android_things_RF24_NativeRF24_setPALevel(JNIEnv *env, jobject instance,
                                                          jint level) {

  if (radio) {
    radio->setPALevel((uint8_t) level);
  }


}

extern "C"
JNIEXPORT jint JNICALL
Java_nz_org_winters_android_things_RF24_NativeRF24_getPALevel(JNIEnv *env, jobject instance) {

  if (radio) {
    return radio->getPALevel();
  }
  return -1;

}

extern "C"
JNIEXPORT void JNICALL
Java_nz_org_winters_android_things_RF24_NativeRF24_setDataRate(JNIEnv *env, jobject instance,
                                                           jint speed) {

  if (radio) {
    radio->setDataRate((rf24_datarate_e) speed);
  }


}

extern "C"
JNIEXPORT jint JNICALL
Java_nz_org_winters_android_things_RF24_NativeRF24_getDataRate(JNIEnv *env, jobject instance) {

  if (radio) {
    return radio->getDataRate();
  }
  return -1;
}

extern "C"
JNIEXPORT void JNICALL
Java_nz_org_winters_android_things_RF24_NativeRF24_setCRCLength(JNIEnv *env, jobject instance,
                                                            jint length) {

  if (radio) {
    radio->setCRCLength((rf24_crclength_e) length);
  }

}

extern "C"
JNIEXPORT jint JNICALL
Java_nz_org_winters_android_things_RF24_NativeRF24_getCRCLength(JNIEnv *env, jobject instance) {

  if (radio) {
    return radio->getCRCLength();
  }
  return -1;

}

extern "C"
JNIEXPORT void JNICALL
Java_nz_org_winters_android_things_RF24_NativeRF24_disableCRC(JNIEnv *env, jobject instance) {

  if (radio) {
    radio->disableCRC();
  }

}

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_nz_org_winters_android_things_RF24_NativeRF24_read(JNIEnv *env, jobject instance, jint length) {


  jbyteArray buffer = env->NewByteArray(length);
  jbyte *elements = env->GetByteArrayElements(buffer, NULL);
  if (radio) {
    radio->read(elements, (uint8_t) length);
  }

  env->ReleaseByteArrayElements(buffer, elements, JNI_COMMIT);
  return buffer;

}

extern "C"
JNIEXPORT void JNICALL
Java_nz_org_winters_android_things_RF24_NativeRF24_stopListening(JNIEnv *env, jobject instance) {

  if (radio) {
    radio->stopListening();
  }

}

extern "C"
JNIEXPORT void JNICALL
Java_nz_org_winters_android_things_RF24_NativeRF24_startListening(JNIEnv *env, jobject instance) {

  if (radio) {
    radio->startListening();
  }

}

extern "C"
JNIEXPORT void JNICALL
Java_nz_org_winters_android_things_RF24_NativeRF24_startFastWrite(JNIEnv *env, jobject instance,
                                                              jbyteArray buf_, jint len,
                                                              jboolean multicast,
                                                              jboolean startTx) {
  jbyte *buf = env->GetByteArrayElements(buf_, NULL);

  if (radio) {
    radio->startFastWrite(buf, (uint8_t) len, multicast, startTx);
  }


  env->ReleaseByteArrayElements(buf_, buf, 0);
}

extern "C"
JNIEXPORT void JNICALL
Java_nz_org_winters_android_things_RF24_NativeRF24_startWrite(JNIEnv *env, jobject instance,
                                                          jbyteArray buf_, jint len,
                                                          jboolean multicast) {
  jbyte *buf = env->GetByteArrayElements(buf_, NULL);

  if (radio) {
    radio->startFastWrite(buf, (uint8_t) len, multicast);
  }

  env->ReleaseByteArrayElements(buf_, buf, 0);
}

extern "C"
JNIEXPORT void JNICALL
Java_nz_org_winters_android_things_RF24_NativeRF24_openWritingPipe(JNIEnv *env, jobject instance,
                                                                  jlong address) {

  if (radio) {
    radio->openWritingPipe((uint64_t) address);
  }


}

extern "C"
JNIEXPORT void JNICALL
Java_nz_org_winters_android_things_RF24_NativeRF24_openWritingPipeStr__Ljava_lang_String_2(JNIEnv *env,
                                                                                    jobject instance,
                                                                                    jstring address_) {

  const char *address = env->GetStringUTFChars(address_, 0);

  if (radio) {
    radio->openWritingPipe((const uint8_t *) address);
  }

  env->ReleaseStringUTFChars(address_, address);
}

extern "C"
JNIEXPORT void JNICALL
Java_nz_org_winters_android_things_RF24_NativeRF24_closeReadingPipe(JNIEnv *env, jobject instance,
                                                                jint pipe) {

  if (radio) {
    radio->closeReadingPipe((uint8_t) pipe);
  }

}

extern "C"
JNIEXPORT void JNICALL
Java_nz_org_winters_android_things_RF24_NativeRF24_openReadingPipe(JNIEnv *env, jobject instance,
                                                                   jint pipe, jlong address) {

  if (radio) {
    radio->openReadingPipe((uint8_t) pipe, (uint64_t) address);
  }

}


extern "C"
JNIEXPORT void JNICALL
Java_nz_org_winters_android_things_RF24_NativeRF24_openReadingPipeStr(JNIEnv *env,
                                                                                     jobject instance,
                                                                                     jint pipe,
                                                                                     jstring address_) {
  const char *address = env->GetStringUTFChars(address_, 0);

  if (radio) {
    radio->openReadingPipe((uint8_t) pipe, (const uint8_t *) address);
  }

  env->ReleaseStringUTFChars(address_, address);

}

extern "C"
JNIEXPORT void JNICALL
Java_nz_org_winters_android_things_RF24_NativeRF24_setAddressWidth(JNIEnv *env, jobject instance,
                                                               jint a_width) {

  if (radio) {
    radio->setAddressWidth((uint8_t) a_width);
  }

}

extern "C"
JNIEXPORT void JNICALL
Java_nz_org_winters_android_things_RF24_NativeRF24_reUseTx(JNIEnv *env, jobject instance) {

  if (radio) {
    radio->reUseTX();
  }


}

extern "C"
JNIEXPORT jint JNICALL
Java_nz_org_winters_android_things_RF24_NativeRF24_flush_1tx(JNIEnv *env, jobject instance) {

  if (radio) {
    return radio->flush_tx();
  }
  return 0;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_nz_org_winters_android_things_RF24_NativeRF24_testCarrier(JNIEnv *env, jobject instance) {

  if (radio) {
    return (jboolean) radio->testCarrier();
  }
  return (jboolean) false;

}

extern "C"
JNIEXPORT jboolean JNICALL
Java_nz_org_winters_android_things_RF24_NativeRF24_testRPD(JNIEnv *env, jobject instance) {

  if (radio) {
    return (jboolean) radio->testRPD();
  }
  return (jboolean) false;

}

extern "C"
JNIEXPORT void JNICALL
Java_nz_org_winters_android_things_RF24_NativeRF24_setRetries(JNIEnv *env, jobject instance, jint delay,
                                                          jint count) {

  if (radio) {
    radio->setRetries((uint8_t) delay, (uint8_t) count);
  }


}

extern "C"
JNIEXPORT void JNICALL
Java_nz_org_winters_android_things_RF24_NativeRF24_setChannel(JNIEnv *env, jobject instance,
                                                          jint channel) {

  if (radio) {
    radio->setChannel((uint8_t) channel);
  }

}

extern "C"
JNIEXPORT jint JNICALL
Java_nz_org_winters_android_things_RF24_NativeRF24_getChannel(JNIEnv *env, jobject instance) {

  if (radio) {
    return (jint) radio->getChannel();
  }
  return (jint) 0;

}

extern "C"
JNIEXPORT void JNICALL
Java_nz_org_winters_android_things_RF24_NativeRF24_setPayloadSize(JNIEnv *env, jobject instance,
                                                              jint size) {

  if (radio) {
    radio->setPayloadSize((uint8_t) size);
  }

}

extern "C"
JNIEXPORT jint JNICALL
Java_nz_org_winters_android_things_RF24_NativeRF24_getPayloadSize(JNIEnv *env, jobject instance) {

  if (radio) {
    return (jint) radio->getPayloadSize();
  }
  return (jint) 0;


}

extern "C"
JNIEXPORT jint JNICALL
Java_nz_org_winters_android_things_RF24_NativeRF24_getDynamicPayloadSize(JNIEnv *env,
                                                                     jobject instance) {

  if (radio) {
    return (jint) radio->getDynamicPayloadSize();
  }
  return (jint) 0;


}

extern "C"
JNIEXPORT jboolean JNICALL
Java_nz_org_winters_android_things_RF24_NativeRF24_isPVariant(JNIEnv *env, jobject instance) {

  if (radio) {
    return (jboolean) radio->isPVariant();
  }
  return (jboolean) false;


}

extern "C"
JNIEXPORT jboolean JNICALL
Java_nz_org_winters_android_things_RF24_NativeRF24_rxFifoFull(JNIEnv *env, jobject instance) {

  if (radio) {
    return (jboolean) radio->rxFifoFull();
  }
  return (jboolean) false;

}

extern "C"
JNIEXPORT void JNICALL
Java_nz_org_winters_android_things_RF24_NativeRF24_powerDown(JNIEnv *env, jobject instance) {

  if (radio) {
    radio->powerDown();
  }
}

extern "C"
JNIEXPORT void JNICALL
Java_nz_org_winters_android_things_RF24_NativeRF24_powerUp(JNIEnv *env, jobject instance) {

  if (radio) {
    radio->powerUp();
  }

}

extern "C"
JNIEXPORT jboolean JNICALL
Java_nz_org_winters_android_things_RF24_NativeRF24_write(JNIEnv *env, jobject instance,
                                                         jbyteArray buffer_, jint len) {
  jbyte *buffer = env->GetByteArrayElements(buffer_, NULL);

  bool rv = false;
  if (radio) {
    rv = radio->write(buffer, (uint8_t) len);
  }

  env->ReleaseByteArrayElements(buffer_, buffer, 0);
  return (jboolean) rv;
}


//extern "C"
//JNIEXPORT jboolean JNICALL
//Java_nz_org_winters_android_things_RF24_NativeRF24_write(JNIEnv *env,
//                                                                     jbyteArray buffer_, jint len) {
//  jbyte *buffer = env->GetByteArrayElements(buffer_, NULL);
//  bool rv = false;
//  if (radio) {
//    rv = radio->write(buffer, (uint8_t) len);
//  }
//
//
//  env->ReleaseByteArrayElements(buffer_, buffer, 0);
//  return (jboolean) rv;
//}

extern "C"
JNIEXPORT jboolean JNICALL
Java_nz_org_winters_android_things_RF24_NativeRF24_writeEx(JNIEnv *env, jobject instance,
                                                                      jbyteArray buffer_, jint len,
                                                                      jboolean multicast) {
  jbyte *buffer = env->GetByteArrayElements(buffer_, NULL);

  bool rv = false;
  if (radio) {
    rv = radio->write(buffer, (uint8_t) len, multicast);
  }

  env->ReleaseByteArrayElements(buffer_, buffer, 0);
  return (jboolean) rv;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_nz_org_winters_android_things_RF24_NativeRF24_writeFast(JNIEnv *env,
                                                                         jobject instance,
                                                                         jbyteArray buffer_,
                                                                         jint len) {
  jbyte *buffer = env->GetByteArrayElements(buffer_, NULL);


  bool rv = false;
  if (radio) {
    rv = radio->writeFast(buffer, (uint8_t) len);
  }


  env->ReleaseByteArrayElements(buffer_, buffer, 0);
  return (jboolean) rv;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_nz_org_winters_android_things_RF24_NativeRF24_writeFastEx(JNIEnv *env,
                                                                          jobject instance,
                                                                          jbyteArray buffer_,
                                                                          jint len,
                                                                          jboolean multicast) {
  jbyte *buffer = env->GetByteArrayElements(buffer_, NULL);

  bool rv = false;
  if (radio) {
    rv = radio->writeFast(buffer, (uint8_t) len, multicast);
  }

  env->ReleaseByteArrayElements(buffer_, buffer, 0);
  return (jboolean) rv;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_nz_org_winters_android_things_RF24_NativeRF24_writeBlocking(JNIEnv *env, jobject instance,
                                                             jbyteArray buffer_, jint len,
                                                             jlong timeout) {
  jbyte *buffer = env->GetByteArrayElements(buffer_, NULL);

  bool rv = false;
  if (radio) {
    rv = radio->writeBlocking(buffer, (uint8_t) len, (uint32_t) timeout);
  }

  env->ReleaseByteArrayElements(buffer_, buffer, 0);
  return (jboolean) rv;

}

extern "C"
JNIEXPORT jboolean JNICALL
Java_nz_org_winters_android_things_RF24_NativeRF24_txStandBy__(JNIEnv *env, jobject instance) {

  if (radio) {
    return (jboolean) radio->txStandBy();
  }
  return (jboolean) false;

}

extern "C"
JNIEXPORT jboolean JNICALL
Java_nz_org_winters_android_things_RF24_NativeRF24_txStandBy__JZ(JNIEnv *env, jobject instance,
                                                             jlong timeout, jboolean startTx) {

  if (radio) {
    return (jboolean) radio->txStandBy((uint32_t) timeout, startTx);
  }
  return (jboolean) false;

}

extern "C"
JNIEXPORT jboolean JNICALL
Java_nz_org_winters_android_things_RF24_NativeRF24_isAckPayloadAvailable(JNIEnv *env,
                                                                     jobject instance) {

  if (radio) {
    return (jboolean) radio->isAckPayloadAvailable();
  }
  return (jboolean) false;

}

extern "C"
JNIEXPORT void JNICALL
Java_nz_org_winters_android_things_RF24_NativeRF24_writeAckPayload(JNIEnv *env, jobject instance,
                                                               jint pipe, jbyteArray buf_,
                                                               jint len) {
  jbyte *buf = env->GetByteArrayElements(buf_, NULL);

  if (radio) {
    radio->writeAckPayload((uint8_t) pipe, buf, (uint8_t) len);
  }

  env->ReleaseByteArrayElements(buf_, buf, 0);
}

extern "C"
JNIEXPORT void JNICALL
Java_nz_org_winters_android_things_RF24_NativeRF24_setAutoAck__Z(JNIEnv *env, jobject instance,
                                                             jboolean enable) {

  if (radio) {
    radio->setAutoAck(enable);
  }

}

extern "C"
JNIEXPORT void JNICALL
Java_nz_org_winters_android_things_RF24_NativeRF24_setAutoAck__IZ(JNIEnv *env, jobject instance,
                                                              jint pipe, jboolean enable) {

  if (radio) {
    radio->setAutoAck((uint8_t) pipe, enable);
  }

}

extern "C"
JNIEXPORT void JNICALL
Java_nz_org_winters_android_things_RF24_NativeRF24_enableDynamicAck(JNIEnv *env, jobject instance) {

  if (radio) {
    radio->enableDynamicAck();
  }

}

extern "C"
JNIEXPORT void JNICALL
Java_nz_org_winters_android_things_RF24_NativeRF24_disableDynamicPayloads(JNIEnv *env,
                                                                      jobject instance) {

  if (radio) {
    radio->disableDynamicPayloads();
  }

}

extern "C"
JNIEXPORT void JNICALL
Java_nz_org_winters_android_things_RF24_NativeRF24_enableDynamicPayloads(JNIEnv *env,
                                                                     jobject instance) {

  if (radio) {
    radio->enableDynamicPayloads();
  }


}

extern "C"
JNIEXPORT void JNICALL
Java_nz_org_winters_android_things_RF24_NativeRF24_enableAckPayload(JNIEnv *env, jobject instance) {

  if (radio) {
    radio->enableAckPayload();
  }


}

extern "C"
JNIEXPORT void JNICALL
Java_nz_org_winters_android_things_RF24_NativeRF24_begin(JNIEnv *env, jobject instance) {

  if (radio) {
    radio->begin();
  }


}

extern "C"
JNIEXPORT void JNICALL
Java_nz_org_winters_android_things_RF24_NativeRF24_closeDevices(JNIEnv *env, jobject instance) {

  if(radio){
    delete radio;
    radio = NULL;
  }

}

extern "C"
JNIEXPORT void JNICALL
Java_nz_org_winters_android_things_RF24_NativeRF24_printDetails(JNIEnv *env, jobject instance) {

  if(radio){
    radio->printDetails();
  }

}

#pragma clang diagnostic pop