cmake_minimum_required(VERSION 3.22.1)
project("math_solver_lib")

if(ANDROID)
    message(STATUS "Building for Android")
else()
    message(STATUS "Building for Desktop/Windows")
    include_directories("${CMAKE_CURRENT_SOURCE_DIR}/include/jni")
endif()

include_directories("${CMAKE_CURRENT_SOURCE_DIR}/include")

add_library(
        math_solver_lib
        SHARED
        src/Native-lib.cpp
        src/MyMath.cpp
        src/main.cpp
)

target_compile_options(math_solver_lib PRIVATE -std=c++17)

if(ANDROID)
    find_library(log-lib log)
    target_link_libraries(math_solver_lib ${log-lib})
endif()