#include <jni.h>
#include <string>
#include <android/log.h>
#include <math.h>

extern "C"
{
    JNIEXPORT jstring JNICALL
    Java_com_appends_JniUtils_JniTest_stringComeFromJNI(
            JNIEnv *env,
            jobject /* this */) {
        std::string hello = "Hello from C++";
        return env->NewStringUTF(hello.c_str());
    }

    JNIEXPORT jstring JNICALL
    Java_com_appends_JniUtils_EffectUtil_stringFromJNI(
            JNIEnv *env,
            jobject /* this */) {
        std::string hello = "EffectUtil Hello from C++";
        __android_log_print(ANDROID_LOG_INFO,"zyq",  "clazz = %s", "EffectUtil Hello from C++");
        jclass clazz = env->FindClass("com/android/camera/Storage");
        __android_log_print(ANDROID_LOG_INFO,"zyq",  "clazz = %d", clazz);
        jmethodID id = env->GetStaticMethodID(clazz,"printMessage","()V");
        __android_log_print(ANDROID_LOG_INFO,"zyq",  "id = %d", id);
        jvalue* values = new jvalue[0];
        env->CallStaticVoidMethodA(clazz,id, values);
        return env->NewStringUTF(hello.c_str());
    }

    JNIEXPORT jintArray JNICALL Java_com_appends_JniUtils_EffectUtil_getResultFromJni
        (JNIEnv *env, jclass, jintArray piels, jint source_width, jint source_height, jint center_x, jint center_y, jint radius){
        jint *pixels = env->GetIntArrayElements(piels, NULL);
        int centerX = center_x;
        int centerY = center_y;
        int x_start = (centerX-radius)<0?(-centerX):(-radius);
        int x_end = (centerX+radius)>source_width?(source_width-centerX):(radius);
        int y_start = (centerY-radius)<0?(-centerY):(-radius);
        int y_end = (centerY+radius)>source_height?(source_height-centerY):(radius);
        __android_log_print(ANDROID_LOG_INFO,"zyq",  "centerX = %d", centerX);
        __android_log_print(ANDROID_LOG_INFO,"zyq",  "centerY = %d", centerY);
        __android_log_print(ANDROID_LOG_INFO,"zyq",  "source_width = %d", source_width);
        __android_log_print(ANDROID_LOG_INFO,"zyq",  "source_height = %d", source_height);
        __android_log_print(ANDROID_LOG_INFO,"zyq",  "radius = %d", radius);
        for(int i = x_start;i<x_end;i++){
            for(int j = y_start;j<y_end;j++){
                if(((int)fabsf(i)>(radius-255))||((int)fabsf(j)>(radius-255))){
                int alpha = (255-((radius-(int)(fmaxf(fabsf(i),fabsf(j))))));
                    *(pixels+(source_width*(centerY+j))+(centerX+i)) = *(pixels+(source_width*(centerY+j))+(centerX+i)) & (0x00ffffff |(alpha<<24));
                }else{
                    *(pixels+(source_width*(centerY+j))+(centerX+i)) = *(pixels+(source_width*(centerY+j))+(centerX+i)) & (0x00ffffff);
                }
            }

        }
        int size = source_width * source_height;
        jintArray result = env->NewIntArray(size);
        env->SetIntArrayRegion(result, 0, size, pixels);
        env->ReleaseIntArrayElements(piels, pixels, 0);
        return result;
    }
}
