#include <jni.h>
#include <string>
#include <sstream>
#include <android/log.h>
#include "MyMath.h"

BigNumber   evaluate(const std::string& expr);
std::string prepareLatex(const std::string& s);

static std::string captureEvaluate(const std::string& expr) {
    std::ostringstream oss;
    std::streambuf* old = std::cout.rdbuf(oss.rdbuf());
    BigNumber result = evaluate(expr);
    std::string printed = oss.str();
    std::cout.rdbuf(old);

    if (!printed.empty()) {
        while (!printed.empty() && (printed.back() == '\n' || printed.back() == '\r' || printed.back() == ' '))
            printed.pop_back();
        return printed;
    }

    std::ostringstream num;
    std::streambuf* old2 = std::cout.rdbuf(num.rdbuf());
    result.print(0);
    std::cout.rdbuf(old2);
    std::string s = num.str();
    while (!s.empty() && (s.back() == '\n' || s.back() == '\r' || s.back() == ' '))
        s.pop_back();
    return s;
}

static std::string fromJString(JNIEnv* env, jstring js) {
    if (!js) return "";
    const char* raw = env->GetStringUTFChars(js, nullptr);
    std::string s(raw ? raw : "");
    env->ReleaseStringUTFChars(js, raw);
    return s;
}

extern "C"
JNIEXPORT jstring JNICALL
Java_org_infa252_project_NativeLib_calculateNative(
        JNIEnv* env, jobject, jstring formula)
{
    try {
        std::string expr = fromJString(env, formula);

        std::string prepared = prepareLatex(expr);
        __android_log_print(ANDROID_LOG_DEBUG, "MATHSOLVER",
                            "INPUT: %s", expr.c_str());
        __android_log_print(ANDROID_LOG_DEBUG, "MATHSOLVER",
                            "PREPARED: %s", prepared.c_str());

        std::string result = captureEvaluate(expr);
        return env->NewStringUTF(result.c_str());
    } catch (const std::exception& e) {
        return env->NewStringUTF((std::string("Ошибка: ") + e.what()).c_str());
    } catch (...) {
        return env->NewStringUTF("Ошибка");
    }
}

extern "C"
JNIEXPORT jstring JNICALL
Java_org_infa252_project_NativeLib_calculateWithXNativeImpl(
        JNIEnv* env, jobject, jstring formula, jstring xValue)
{
    try {
        std::string expr = fromJString(env, formula);
        std::string xStr = fromJString(env, xValue);

        std::string subst;
        subst.reserve(expr.size() * 2);
        for (int i = 0; i < (int)expr.size(); i++) {
            if (expr[i] == 'x') {
                bool prev = (i > 0 && std::isalpha((unsigned char)expr[i-1]));
                bool next = (i+1 < (int)expr.size() && std::isalpha((unsigned char)expr[i+1]));
                subst += (prev || next) ? "x" : ("(" + xStr + ")");
            } else {
                subst += expr[i];
            }
        }

        __android_log_print(ANDROID_LOG_DEBUG, "MATHSOLVER",
                            "INPUT_X: %s", expr.c_str());
        __android_log_print(ANDROID_LOG_DEBUG, "MATHSOLVER",
                            "SUBST_X: %s", subst.c_str());

        std::string result = captureEvaluate(subst);
        return env->NewStringUTF(result.c_str());
    } catch (const std::exception& e) {
        return env->NewStringUTF((std::string("Ошибка: ") + e.what()).c_str());
    } catch (...) {
        return env->NewStringUTF("Ошибка");
    }
}