#include <jni.h>

// Write C++ code here.
//
// Do not forget to dynamically load the C++ library into your application.
//
// For instance,
//
// In MainActivity.java:
//    static {
//       System.loadLibrary("commonMain");
//    }
//
// Or, in MainActivity.kt:
//    companion object {
//      init {
//         System.loadLibrary("commonMain")
//      }
//    }



bool fall_detection_algorithm(float accel1, float accel2, float accel3) {

    return true;
}


//extern "C"
//JNIEXPORT bool JNICALL fall_detection_algorithm(JNIEnv *env, jobject obj, jfloat accel1, jfloat accel2, jfloat accel3) {
//    return fall_detection_algorithm(accel1, accel2, accel3);
//}

extern "C"
JNIEXPORT jboolean JNICALL
Java_pie_activityrecognition_platform_android_MainActivity_00024commonMain_fall_1detection_1algorithm(
        JNIEnv *env, jobject thiz, jfloat accel1, jfloat accel2, jfloat accel3) {
    return fall_detection_algorithm(accel1, accel2, accel3);
}
