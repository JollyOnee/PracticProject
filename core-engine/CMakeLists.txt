cmake_minimum_required(VERSION 3.22.1)
project("math_solver_lib")

# Универсальное подключение JNI
if(ANDROID)
    # На Android NDK сам знает, где лежат заголовки
    message(STATUS "Building for Android")
else()
    # На Windows используем твои подготовленные файлы (image_2bc8c3.png)
    message(STATUS "Building for Desktop/Windows")
    include_directories("${CMAKE_CURRENT_SOURCE_DIR}/include/jni")
endif()

# Общие папки для обеих платформ
include_directories("${CMAKE_CURRENT_SOURCE_DIR}/include")

# Создание библиотеки
add_library(
    math_solver_lib
    SHARED
    src/Native-lib.cpp
    src/MyMath.cpp
)

# Специфичные настройки для Android
if(ANDROID)
    find_library(log-lib log)
    target_link_libraries(math_solver_lib ${log-lib})
endif()