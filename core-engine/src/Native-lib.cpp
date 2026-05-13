#include <jni.h>
#include <string>
#include "MyMath.h"

extern "C" JNIEXPORT jstring JNICALL
Java_org_infa252_project_NativeLib_calculateNative(JNIEnv *env, jobject thiz, jstring formula) {
    if (!formula) return env->NewStringUTF("0");

    const char *nativeFormula = env->GetStringUTFChars(formula, nullptr);

    // Вызываем обертку, которую мы добавили в MyMath.h
    std::string result = MyMath::solve(std::string(nativeFormula));

    env->ReleaseStringUTFChars(formula, nativeFormula);
    return env->NewStringUTF(result.c_str());
}