#include <jni.h>
#include <string>
#include "MyMath.h"

// Обертка для логики
std::string internal_solve(JNIEnv *env, jstring formula) {
    if (!formula) return "0";
    const char *nativeFormula = env->GetStringUTFChars(formula, nullptr);
    std::string result;
    try {
        result = MyMath::solve(std::string(nativeFormula));
    } catch (...) {
        result = "Error";
    }
    env->ReleaseStringUTFChars(formula, nativeFormula);
    return result;
}

// Функции с жесткой привязкой имен для JNI
extern "C" JNIEXPORT jstring JNICALL
Java_org_infa252_project_NativeLib_calculate(JNIEnv *env, jobject thiz, jstring formula) {
    return env->NewStringUTF(internal_solve(env, formula).c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_org_infa252_project_NativeLib_calculateNative(JNIEnv *env, jobject thiz, jstring formula) {
    return env->NewStringUTF(internal_solve(env, formula).c_str());
}