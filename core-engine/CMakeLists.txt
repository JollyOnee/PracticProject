cmake_minimum_required(VERSION 3.22.1)

# Имя проекта пусть будет как в репозитории
project("math_solver_core")

include_directories(include)

# Называем библиотеку правильно
add_library(math_solver_core SHARED 
    src/native-lib.cpp 
    src/testKMP.cpp
)

find_library(log-lib log)

# Здесь тоже меняем имя
target_link_libraries(math_solver_core ${log-lib})