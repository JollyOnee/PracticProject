package org.infa252.project

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

/**
 * Сохраняет SVG файл на диск в зависимости от платформы.
 */
expect suspend fun saveSvg(fileName: String, content: String)
