package org.infa252.project

import android.os.Environment
import java.io.File

actual fun saveSvgFile(fileName: String, content: String): String {

    return try {

        val folder = File(
            Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS
            ),
            "GraphExports"
        )

        if (!folder.exists()) {
            folder.mkdirs()
        }

        val file = File(folder, fileName)

        file.writeText(content)

        file.absolutePath

    } catch (e: Exception) {

        e.message ?: "Ошибка"
    }
}