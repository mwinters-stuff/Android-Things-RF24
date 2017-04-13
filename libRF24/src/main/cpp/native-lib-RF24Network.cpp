//
// Created by mathew on 10/04/17.
//

#include <jni.h>
#include <string>
#include "RF24/RF24.h"
#include "RF24Network/RF24Network.h"


#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wunused-parameter"

RF24Network *network = NULL;
extern RF24 *radio;

RF24NetworkHeader fromJHeaderToCHeader(JNIEnv *env, jobject jheader) {
  jclass cls = env->GetObjectClass(jheader);
  RF24NetworkHeader networkHeader;
  networkHeader.from_node = (uint16_t) env->GetIntField(jheader,
                                                        env->GetFieldID(cls, "from_node", "I"));
  networkHeader.to_node = (uint16_t) env->GetIntField(jheader,
                                                      env->GetFieldID(cls, "to_node", "I"));
  networkHeader.id = (uint16_t) env->GetIntField(jheader, env->GetFieldID(cls, "id", "I"));
  networkHeader.type = (unsigned char) env->GetIntField(jheader, env->GetFieldID(cls, "type", "I"));
  networkHeader.reserved = (unsigned char) env->GetIntField(jheader,
                                                            env->GetFieldID(cls, "reserved", "I"));
  RF24NetworkHeader::next_id = (uint16_t) env->GetIntField(jheader,
                                                           env->GetFieldID(cls, "next_id", "I"));
  return networkHeader;
}

jobject fromCHeaderToJHeader(JNIEnv *env, jobject jheader, const RF24NetworkHeader &networkHeader) {
  jclass cls = env->GetObjectClass(jheader);

  env->SetIntField(jheader, env->GetFieldID(cls, "from_node", "I"), networkHeader.from_node);
  env->SetIntField(jheader, env->GetFieldID(cls, "to_node", "I"), networkHeader.to_node);
  env->SetIntField(jheader, env->GetFieldID(cls, "id", "I"), networkHeader.id);
  env->SetIntField(jheader, env->GetFieldID(cls, "type", "I"), networkHeader.type);
  env->SetIntField(jheader, env->GetFieldID(cls, "reserved", "I"), networkHeader.reserved);
  env->SetIntField(jheader, env->GetFieldID(cls, "next_id", "I"), RF24NetworkHeader::next_id);
  return jheader;

}

extern "C"
JNIEXPORT jint JNICALL
Java_nz_org_winters_android_things_RF24_NativeRF24Network_getNetworkFlags(JNIEnv *env,
                                                                      jobject instance) {

  if (network) {
    return network->networkFlags;
  }
  return -1;

}

extern "C"
JNIEXPORT void JNICALL
Java_nz_org_winters_android_things_RF24_NativeRF24Network_setNetworkFlags(JNIEnv *env, jobject instance,
                                                                      jint flags) {

  if (network) {
    network->networkFlags = (uint8_t) flags;
  }

}

extern "C"
JNIEXPORT void JNICALL
Java_nz_org_winters_android_things_RF24_NativeRF24Network_begin__II(JNIEnv *env, jobject instance,
                                                                jint channel, jint node_address) {


  if (network) {
    return network->begin((uint16_t) node_address);
  }
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_nz_org_winters_android_things_RF24_NativeRF24Network_isValidAddress(JNIEnv *env, jobject instance,
                                                                     jint node) {

  if (network) {
    return (jboolean) network->is_valid_address((uint16_t) node);
  }
  return (jboolean) false;
}

extern "C"
JNIEXPORT jint JNICALL
Java_nz_org_winters_android_things_RF24_NativeRF24Network_addressOfPipe(JNIEnv *env, jobject instance,
                                                                    jint node, jint pipeNo) {


  if (network) {
    return network->addressOfPipe((uint16_t) node, (uint8_t) pipeNo);
  }
  return -1;
}

extern "C"
JNIEXPORT jint JNICALL
Java_nz_org_winters_android_things_RF24_NativeRF24Network_parent(JNIEnv *env, jobject instance) {

  if (network) {
    return network->parent();
  }
  return -1;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_nz_org_winters_android_things_RF24_NativeRF24Network_write__Lnz_org_winters_android_things_RF24_NativeRF24Network_RF24NetworkHeader_2Lbyte_3_093_2II(
    JNIEnv *env, jobject instance, jobject header, jbyteArray message_, jint maxLen,
    jint writeDirect) {
  jbyte *message = env->GetByteArrayElements(message_, NULL);

  bool rv = false;
  if (network) {
    RF24NetworkHeader networkHeader = fromJHeaderToCHeader(env, header);
    rv = network->write(networkHeader, message, (uint16_t) maxLen, (uint16_t) writeDirect);
    fromCHeaderToJHeader(env, header, networkHeader);
  }
  env->ReleaseByteArrayElements(message_, message, 0);
  return (jboolean) rv;

}

extern "C"
JNIEXPORT jboolean JNICALL
Java_nz_org_winters_android_things_RF24_NativeRF24Network_multicast(JNIEnv *env, jobject instance,
                                                                jobject header, jbyteArray message_,
                                                                jint maxLen, jint level) {
  jbyte *message = env->GetByteArrayElements(message_, NULL);

  bool rv = false;
  if (network) {
    RF24NetworkHeader networkHeader = fromJHeaderToCHeader(env, header);
    rv = network->multicast(networkHeader, message, (uint16_t) maxLen, (uint8_t) level);
    fromCHeaderToJHeader(env, header, networkHeader);
  }
  env->ReleaseByteArrayElements(message_, message, 0);
  return (jboolean) rv;
}

extern "C"
JNIEXPORT jint JNICALL
Java_nz_org_winters_android_things_RF24_NativeRF24Network_getRouteTimeout(JNIEnv *env,
                                                                      jobject instance) {

  if (network) {
    return network->routeTimeout;
  }
  return -1;
}

extern "C"
JNIEXPORT void JNICALL
Java_nz_org_winters_android_things_RF24_NativeRF24Network_setRouteTimeout(JNIEnv *env, jobject instance,
                                                                      jint timeout) {

  if (network) {
    network->routeTimeout = (uint16_t) timeout;
  }

}

extern "C"
JNIEXPORT jint JNICALL
Java_nz_org_winters_android_things_RF24_NativeRF24Network_getTxTimeout(JNIEnv *env, jobject instance) {

  if (network) {
    return network->txTimeout;
  }
  return -1;
}

extern "C"
JNIEXPORT void JNICALL
Java_nz_org_winters_android_things_RF24_NativeRF24Network_setTxTimeout(JNIEnv *env, jobject instance,
                                                                   jint timeout) {

  if (network) {
    network->txTimeout = (uint32_t) timeout;
  }

}

extern "C"
JNIEXPORT void JNICALL
Java_nz_org_winters_android_things_RF24_NativeRF24Network_setMulticastRelay(JNIEnv *env,
                                                                        jobject instance,
                                                                        jboolean enable) {

  if (network) {
    network->multicastRelay = enable;
  }

}

extern "C"
JNIEXPORT void JNICALL
Java_nz_org_winters_android_things_RF24_NativeRF24Network_multicastLevel(JNIEnv *env, jobject instance,
                                                                     jint level) {

  if (network) {
    network->multicastLevel((uint8_t) level);
  }

}




extern "C"
JNIEXPORT jboolean JNICALL
Java_nz_org_winters_android_things_RF24_NativeRF24Network_write__Lnz_org_winters_android_things_RF24_NativeRF24Network_RF24NetworkHeader_2Lbyte_3_093_2I(
    JNIEnv *env, jobject instance, jobject header, jbyteArray message_, jint maxLen) {
  jbyte *message = env->GetByteArrayElements(message_, NULL);
  bool rv = false;
  if (network) {
    RF24NetworkHeader networkHeader = fromJHeaderToCHeader(env, header);
    rv = network->write(networkHeader, message, (uint16_t) maxLen);
    fromCHeaderToJHeader(env, header, networkHeader);
  }
  env->ReleaseByteArrayElements(message_, message, 0);
  return (jboolean) rv;
}

extern "C"
JNIEXPORT jint JNICALL
Java_nz_org_winters_android_things_RF24_NativeRF24Network_read(JNIEnv *env, jobject instance,
                                                           jobject header, jbyteArray message_,
                                                           jint maxLen) {

  jbyte *message = env->GetByteArrayElements(message_, NULL);
  uint16_t rv = 0;
  if (network) {
    RF24NetworkHeader networkHeader = fromJHeaderToCHeader(env, header);
    rv = network->read(networkHeader, message, (uint16_t) maxLen);
    fromCHeaderToJHeader(env, header, networkHeader);
  }
  env->ReleaseByteArrayElements(message_, message, 0);
  return rv;
}


extern "C"
JNIEXPORT jint JNICALL
Java_nz_org_winters_android_things_RF24_NativeRF24Network_peek(JNIEnv *env, jobject instance,
                                                           jobject header) {

  uint16_t rv = 0;
  if (network) {
    RF24NetworkHeader networkHeader = fromJHeaderToCHeader(env, header);
    rv = network->peek(networkHeader);
    fromCHeaderToJHeader(env, header, networkHeader);
  }
  return rv;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_nz_org_winters_android_things_RF24_NativeRF24Network_available(JNIEnv *env, jobject instance) {

  if (network) {
    return (jboolean) network->available();
  }
  return 0;


}

extern "C"
JNIEXPORT jint JNICALL
Java_nz_org_winters_android_things_RF24_NativeRF24Network_update(JNIEnv *env, jobject instance) {

  if (network) {
    return network->update();
  }
  return -1;


}

extern "C"
JNIEXPORT void JNICALL
Java_nz_org_winters_android_things_RF24_NativeRF24Network_begin__I(JNIEnv *env, jobject instance,
                                                               jint nodeAddress) {

  if (network) {
    network->begin((uint16_t) nodeAddress);
  }


}

extern "C"
JNIEXPORT void JNICALL
Java_nz_org_winters_android_things_RF24_NativeRF24Network_init(JNIEnv *env, jobject instance) {

  if (radio) {
    network = new RF24Network(*radio);
  }

}

#pragma clang diagnostic pop