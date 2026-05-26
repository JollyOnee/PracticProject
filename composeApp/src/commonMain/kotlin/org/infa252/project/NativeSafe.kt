package org.infa252.project

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

object NativeSafe {
    private val mutex = Mutex()

    suspend fun calculate(nativeLib: NativeLib, formula: String): String {
        return mutex.withLock {
            nativeLib.calculate(formula)
        }
    }

    suspend fun calculateWithX(
        nativeLib: NativeLib,
        formula: String,
        xValue: String
    ): String {
        return mutex.withLock {
            nativeLib.calculateWithXNative(formula, xValue)
        }
    }
}