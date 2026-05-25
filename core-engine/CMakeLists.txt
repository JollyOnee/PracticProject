cmake_minimum_required(VERSION 3.22.1)
project("math_solver_lib")

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
    # 16KB page size alignment для Android 15+
    target_link_options(math_solver_lib PRIVATE "-Wl,-z,max-page-size=16384")
endif()