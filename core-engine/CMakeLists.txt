cmake_minimum_required(VERSION 3.22.1)

project("math_solver_lib")

# 1. Добавляем поиск JNI для ПК (на Android NDK делает это автоматически)
if(NOT ANDROID)
    find_package(JNI REQUIRED)
    include_directories(${JNI_INCLUDE_DIRS})
endif()

include_directories(include)

add_library(
    math_solver_lib
    SHARED
    src/Native-lib.cpp
    src/MyMath.cpp
)

# 2. Условное подключение Android-логгера
if(ANDROID)
    find_library(log-lib log)
    target_link_libraries(math_solver_lib ${log-lib})
else()
    # На ПК (Windows) библиотека log отсутствует, поэтому здесь ничего не линкуем
    # Если на ПК нужны логи, можно использовать стандартный iostream
endif()