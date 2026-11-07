#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring JNICALL
Java_org_mcremote_launcher_MainActivity_nativeRuntimeInfo(JNIEnv* env, jobject /* this */) {
#if defined(__aarch64__)
    const char* abi = "arm64-v8a";
#elif defined(__x86_64__)
    const char* abi = "x86_64";
#else
    const char* abi = "unknown ABI";
#endif
    const std::string info = std::string("Device ABI: ") + abi + ". JVM backend is not bundled.";
    return env->NewStringUTF(info.c_str());
}
