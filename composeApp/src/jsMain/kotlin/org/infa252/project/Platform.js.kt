package org.infa252.project

import kotlinx.browser.document
import org.w3c.dom.HTMLAnchorElement
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag

class JsPlatform: Platform {
    override val name: String = "Web with Kotlin/JS"
}

actual fun getPlatform(): Platform = JsPlatform()

actual suspend fun saveSvg(fileName: String, content: String) {
    val blob = Blob(arrayOf(content), BlobPropertyBag(type = "image/svg+xml"))
    
    val link = document.createElement("a") as HTMLAnchorElement
    // Используем динамический вызов URL для совместимости
    val url = js("URL.createObjectURL(blob)")
    link.href = url.toString()
    link.download = fileName
    document.body?.appendChild(link)
    link.click()
    document.body?.removeChild(link)
    js("URL.revokeObjectURL(url)")
}
