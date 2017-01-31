//
// Created by mathew on 28/01/17.
//

#include <jni.h>
#include <string>

extern "C"
jbyteArray
Java_com_example_androidthings_rf24_MainActivity_longToCByteArray(
        JNIEnv *env,
        jobject /* this */,
        jlong value) {

    unsigned long ulong = (unsigned long)value;
    jbyteArray buffer = env->NewByteArray(4);
    jbyte * elements = env->GetByteArrayElements(buffer,NULL);
    memcpy(elements, &ulong,4);
    env->ReleaseByteArrayElements(buffer,elements,JNI_COMMIT);
    return buffer;
}

extern "C"
jlong
Java_com_example_androidthings_rf24_MainActivity_byteArrayToClong(
        JNIEnv *env,
        jobject /* this */,
        jbyteArray buffer) {

    unsigned long ulong = 0;
    jbyte * elements = env->GetByteArrayElements(buffer,NULL);
    memcpy(&ulong, elements,4);
    env->ReleaseByteArrayElements(buffer,elements,JNI_ABORT);
    return (jlong) ulong;
}
