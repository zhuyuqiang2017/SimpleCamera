
set(ProjectToPath F:/workspace/android/Project/SimpleCamera)

set(OpenCVToPath F:/workspace/android/OpenCV-android-sdk)

cmake_minimum_required(VERSION 3.4.1)
set(CMAKE_VERBOSE_MAKEFILE on)
#set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=gnu++11")

add_library(native-lib SHARED src/main/cpp/native-lib.cpp )

#add_library( jpegutil SHARED IMPORTED )
#set_target_properties(jpegutil PROPERTIES IMPORTED_LOCATION ${ProjectToPath}/app/jniLibs/${ANDROID_ABI}/libjni_jpegutil.so)

#add_library( tinyplanet SHARED IMPORTED )
#set_target_properties(tinyplanet PROPERTIES IMPORTED_LOCATION ${ProjectToPath}/app/jniLibs/${ANDROID_ABI}/libjni_tinyplanet.so)

find_library(log-lib log )

target_link_libraries( native-lib log)