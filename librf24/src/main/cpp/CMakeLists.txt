#
# Copyright (C) 2017 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

cmake_minimum_required(VERSION 3.4.1)

# Add FindAndroidThings.cmake dir to the module path.
set(CMAKE_MODULE_PATH ${CMAKE_MODULE_PATH} ${PROJECT_DIR}/libandroidthings)
# Resolve Android Things headers and libraries
find_package(AndroidThings REQUIRED)

add_library(RF24 SHARED
            RF24/RF24.cpp
            RF24/utility/Android-Things/spi.cpp)

target_include_directories(RF24 PRIVATE
                           ${ANDROIDTHINGS_INCLUDE_DIRS})

#target_include_directories(${ANDROIDTHINGS_INCLUDE_DIRS})

add_library(androidthings SHARED
            IMPORTED)
set_target_properties(androidthings
                      PROPERTIES IMPORTED_LOCATION
                      ${ANDROIDTHINGS_LIBRARIES})

target_link_libraries(RF24
                      android
                      log
                      androidthings)
