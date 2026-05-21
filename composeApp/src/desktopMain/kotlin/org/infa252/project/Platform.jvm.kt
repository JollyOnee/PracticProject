package org.infa252.project

import java.io.File

class JVMPlatform: Platform {
    override val name: String = "Java ${System.getProperty("java.version")}"
}

actual fun getPlatform(): Platform = JVMPlatform()

actual fun saveSvg(filename: String, content: String) {
    try {
        val file = File(System.getProperty("user.home"), filename)
        file.writeText(content)
        println("SVG сохранён: ${file.absolutePath}")
    } catch (e: Exception) {
        println("Ошибка сохранения SVG: ${e.message}")
    }
}