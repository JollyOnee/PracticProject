#include <jni.h>
#include <string>
#include "MyMath.h"

// Вспомогательная функция — форматирует BigNumber в строку
static std::string formatBigNumber(BigNumber& num) {
    std::string result = "";

    if (num.getSign()) result += "-";

    std::string val = num.getValue();
    int pt = num.getPoint();

    if (pt <= 0) {
        result += val;
        // Если point отрицательный — добавляем нули
        for (int i = 0; i < -pt; i++) result += '0';
    } else {
        if ((int)val.length() > pt) {
            // Вставляем точку внутри числа
            int dotPos = val.length() - pt;
            result += val.substr(0, dotPos);
            result += ".";
            result += val.substr(dotPos);
        } else {
            // Число меньше 1, например 0.0042
            result += "0.";
            for (int i = 0; i < pt - (int)val.length(); i++)
                result += '0';
            result += val;
        }
    }
    return result;
}

extern "C" {

JNIEXPORT jstring JNICALL
Java_org_infa252_project_NativeLib_calculateNative(
        JNIEnv* env, jobject thiz, jstring expression) {

    if (expression == nullptr)
        return env->NewStringUTF("Error: Null expression");

    const char* exp_chars = env->GetStringUTFChars(expression, nullptr);
    if (exp_chars == nullptr)
        return env->NewStringUTF("Error: Out of memory");

    std::string expr(exp_chars);
    env->ReleaseStringUTFChars(expression, exp_chars);

    try {
        BigNumber result = evaluate(expr);
        std::string formatted = formatBigNumber(result);
        return env->NewStringUTF(formatted.c_str());
    } catch (const std::exception& e) {
        return env->NewStringUTF(e.what());
    } catch (...) {
        return env->NewStringUTF("Error in calculation");
    }
}

}