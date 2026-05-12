package org.infa252.project

class NativeLib {
    init {

        java.lang.System.loadLibrary("math_solver_lib")
    }

    external fun calculate(expression: String): String
}