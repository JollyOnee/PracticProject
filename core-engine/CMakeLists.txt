cmake_minimum_required(VERSION 3.22.1)
project("math_solver_lib")

include_directories("${CMAKE_CURRENT_SOURCE_DIR}/include")

if(ANDROID)
    add_library(
        math_solver_lib
        SHARED
        src/Native-lib.cpp
        src/MyMath.cpp
        src/main.cpp
    )
    find_library(log-lib log)
    target_link_libraries(math_solver_lib ${log-lib})
    target_link_options(math_solver_lib PRIVATE "-Wl,-z,max-page-size=16384")
else()
    # Находим JDK автоматически
    find_package(JNI REQUIRED)
    include_directories(${JNI_INCLUDE_DIRS})

    add_library(
        math_solver_lib
        SHARED
        src/DesktopNativeLib.cpp
        src/MyMath.cpp
        src/main.cpp
    )
endif()

target_compile_options(math_solver_lib PRIVATE $<IF:$<BOOL:${ANDROID}>,-std=c++17,/std:c++17>)