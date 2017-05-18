//
// Created by mathew on 5/18/17.
//

#include <android/log.h>
#include <stdio.h>
#include <string.h>

#define BUFFER_SIZE 200

char bigbuffer[BUFFER_SIZE * 5];

void log_print(const char *tag, const char* format, ...){
  char buffer[BUFFER_SIZE];
  va_list args;
  va_start(args,format);
  vsnprintf(buffer,BUFFER_SIZE,format, args);
  va_end(args);
  if(strchr(buffer,'\n') > 0){
    __android_log_print(ANDROID_LOG_INFO,tag,"%s", bigbuffer);
    bigbuffer[0] = 0;
  }else{
    strcat(bigbuffer,buffer);
  }

}