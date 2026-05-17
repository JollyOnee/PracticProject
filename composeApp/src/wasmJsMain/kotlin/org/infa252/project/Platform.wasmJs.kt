package org.infa252.project

class WasmPlatform: Platform {
    override val name: String = "Web with Kotlin/Wasm"
}

actual fun getPlatform(): Platform = WasmPlatform()

/**
 * Вспомогательная функция на JS для скачивания файла.
 * В WasmJs параметры передаются в js() через аргументы функции.
 */
@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
private fun triggerDownload(fileName: String, content: String) {
    js("""
        var blob = new Blob([content], { type: 'image/svg+xml' });
        var url = URL.createObjectURL(blob);
        var link = document.createElement('a');
        link.href = url;
        link.download = fileName;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        URL.revokeObjectURL(url);
    """)
}

actual suspend fun saveSvg(fileName: String, content: String) {
    triggerDownload(fileName, content)
}
