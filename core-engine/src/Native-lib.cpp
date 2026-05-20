#include <jni.h>
#include <string>
#include <sstream>
#include <iostream>
#include <algorithm>
#include <android/log.h>
#include "MyMath.h"

#define LOG_TAG "MathSolver_Native"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

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

extern "C" JNIEXPORT jstring JNICALL
Java_org_infa252_project_NativeLib_calculateNative(JNIEnv *env, jobject thiz, jstring formula) {
    if (!formula) return env->NewStringUTF("0");
    const char *nativeFormula = env->GetStringUTFChars(formula, nullptr);
    std::string result;
    try {
        std::string expr(nativeFormula);
        std::string prepared = prepareLatex(expr);

        if (prepared.find('x') != std::string::npos && prepared.substr(0, 4) != "int(") {
            // Многочлен — глушим мусорный вывод из Solve()
            std::ostringstream devNull;
            std::streambuf* old = std::cout.rdbuf(devNull.rdbuf());
            Polynom p = parsePolyExpression(prepared);
            std::vector<BigNumber> roots = p.Solve();
            std::cout.rdbuf(old);

            if (roots.empty()) {
                result = "Нет корней";
            } else {
                result = "x = ";
                for (int i = 0; i < roots.size(); i++) {
                    if (i > 0) result += ", ";
                    result += bigNumberToString(roots[i]);
                }
            }
        } else {
            BigNumber num = evaluate(expr);
            result = bigNumberToString(num);
        }
    } catch (...) {
        result = "Ошибка";
    }
    env->ReleaseStringUTFChars(formula, nativeFormula);
    return env->NewStringUTF(result.c_str());
}

// ВОТ ТУТ МЫ ИСПРАВИЛИ ИМЯ: добавили JNI на конец, чтобы оно совпало с Kotlin
extern "C" JNIEXPORT jstring JNICALL
Java_org_infa252_project_NativeLib_calculateWithXNativeJNI(JNIEnv *env, jobject thiz, jstring formula, jstring x_value) {
    if (!formula || !x_value) return env->NewStringUTF("0");

    const char *nativeFormula = env->GetStringUTFChars(formula, nullptr);
    const char *nativeX = env->GetStringUTFChars(x_value, nullptr);

    std::string expr(nativeFormula);
    std::string xStr(nativeX);
    std::string result;

    std::replace(xStr.begin(), xStr.end(), ',', '.');

    // Выведем лог, чтобы на 100% убедиться, что вызов из графика дошел сюда!
    __android_log_print(ANDROID_LOG_INFO, "MathSolver_Native", "Успешный JNI вызов! Считаем x=%s", nativeX);

    try {
        if (expr.find("x") != std::string::npos) {
            expr = replaceAll(expr, "x", "(" + xStr + ")");
        }
        BigNumber num = evaluate(expr);
        result = bigNumberToString(num);
    } catch (...) {
        result = "Error";
    }

    env->ReleaseStringUTFChars(formula, nativeFormula);
    env->ReleaseStringUTFChars(x_value, nativeX);
    return env->NewStringUTF(result.c_str());
}