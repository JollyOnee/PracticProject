#include <jni.h>
#include <string>
#include <sstream>
#include <iostream>
#include <algorithm>
#include <android/log.h>
#include <mutex>
#include "MyMath.h"

#define LOG_TAG "MathSolver_Native"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

static std::mutex g_mutex;

std::string bigNumberToString(BigNumber num) {
    std::ostringstream oss;
    std::streambuf* oldCout = std::cout.rdbuf(oss.rdbuf());
    num.print(0);
    std::cout.rdbuf(oldCout);
    return oss.str();
}

std::string replaceAll(std::string str, const std::string& from, const std::string& to) {
    size_t start_pos = 0;
    while((start_pos = str.find(from, start_pos)) != std::string::npos) {
        str.replace(start_pos, from.length(), to);
        start_pos += to.length();
    }
    return str;
}

static bool isBigNumberValid(BigNumber& num) {
    std::string val = num.getValue();
    int pt = num.getPoint();
    if (val.empty()) return false;
    if (val == "99") return false;
    if (pt < 0 || pt > (int)val.length() + 10) return false;
    for (char c : val) {
        if (c != '-' && (c < '0' || c > '9')) return false;
    }
    return true;
}

extern "C" JNIEXPORT jstring JNICALL
Java_org_infa252_project_NativeLib_calculateNative(JNIEnv *env, jobject thiz, jstring formula) {
    if (!formula) return env->NewStringUTF("0");
    const char *nativeFormula = env->GetStringUTFChars(formula, nullptr);
    std::string result;
    {
        std::lock_guard<std::mutex> lock(g_mutex);
        try {
            std::string expr(nativeFormula);
            std::string prepared = prepareLatex(expr);

            LOGI("Original: %s", expr.c_str());
            LOGI("Prepared: %s", prepared.c_str());

            if (prepared.find('x') != std::string::npos && prepared.substr(0, 4) != "int(") {
                std::ostringstream devNull;
                std::streambuf* old = std::cout.rdbuf(devNull.rdbuf());
                try {
                    Polynom p = parsePolyExpression(prepared);
                    std::cout.rdbuf(old);
                    std::vector<BigNumber> roots = p.Solve();
                    if (roots.empty()) {
                        result = "Нет корней";
                    } else {
                        result = "x = ";
                        for (int i = 0; i < roots.size(); i++) {
                            if (i > 0) result += ", ";
                            result += bigNumberToString(roots[i]);
                        }
                    }
                } catch (const std::exception& e) {
                    std::cout.rdbuf(old);
                    result = std::string("Ошибка: ") + e.what();
                } catch (...) {
                    std::cout.rdbuf(old);
                    result = "Ошибка вычисления";
                }
            } else {
                BigNumber num = evaluate(expr);
                result = bigNumberToString(num);
            }
        } catch (...) {
            result = "Ошибка";
        }
    }
    env->ReleaseStringUTFChars(formula, nativeFormula);
    return env->NewStringUTF(result.c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_org_infa252_project_NativeLib_calculateWithXNativeJNI(JNIEnv *env, jobject thiz, jstring formula, jstring x_value) {
    if (!formula || !x_value) return env->NewStringUTF("Error");

    const char *nativeFormula = env->GetStringUTFChars(formula, nullptr);
    const char *nativeX = env->GetStringUTFChars(x_value, nullptr);

    std::string expr(nativeFormula);
    std::string xStr(nativeX);
    std::string result;

    std::replace(xStr.begin(), xStr.end(), ',', '.');

    env->ReleaseStringUTFChars(formula, nativeFormula);
    env->ReleaseStringUTFChars(x_value, nativeX);

    {
        std::lock_guard<std::mutex> lock(g_mutex);
        try {
            if (expr.find("x") != std::string::npos) {
                expr = replaceAll(expr, "x", "(" + xStr + ")");
            }
            BigNumber num = evaluate(expr);
            if (!isBigNumberValid(num)) {
                result = "Error";
            } else {
                result = bigNumberToString(num);
                if (result.empty()) result = "Error";
            }
        } catch (...) {
            result = "Error";
        }
    }

    return env->NewStringUTF(result.c_str());
}