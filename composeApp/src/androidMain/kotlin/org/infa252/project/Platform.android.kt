package org.infa252.project

import android.os.Build
import android.os.Environment
import android.content.ContentValues
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

// Нам нужен контекст для работы с MediaStore, но в KMP это сложно.
// Для простоты сохраним в папку Downloads через старый API, 
// который работает на большинстве Android (с учетом разрешений).
actual suspend fun saveSvg(fileName: String, content: String) {
    withContext(Dispatchers.IO) {
        try {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, fileName)
            FileOutputStream(file).use { 
                it.write(content.toByteArray()) 
            }
            println("Файл сохранен: ${file.absolutePath}")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
