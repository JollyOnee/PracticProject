package org.infa252.project

import java.io.File

actual fun saveSvgFile(fileName: String, content: String): String {
    return try {
        val folder = File("GraphExports")

        if (!folder.exists()) {
            folder.mkdirs()
        }

        val file = File(folder, fileName)
        file.writeText(content)

        "SVG сохранён: ${file.absolutePath}"
    } catch (e: Exception) {
        "Ошибка SVG: ${e.message}"
    }
}