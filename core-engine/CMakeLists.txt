cmake_minimum_required(VERSION 3.22.1)

project("math_solver_lib")

include_directories(include)

add_library(
    math_solver_lib
    SHARED
    src/Native-lib.cpp
    src/MyMath.cpp
)

find_library(log-lib log)

target_link_libraries(math_solver_lib ${log-lib})