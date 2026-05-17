package org.infa252.project

import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class JVMPlatform: Platform {
    override val name: String = "Java ${System.getProperty("java.version")}"
}

actual fun getPlatform(): Platform = JVMPlatform()

actual suspend fun saveSvg(fileName: String, content: String) {
    withContext(Dispatchers.IO) {
        val chooser = JFileChooser().apply {
            dialogTitle = "Сохранить график как SVG"
            fileFilter = FileNameExtensionFilter("SVG файлы", "svg")
            selectedFile = File(fileName)
        }
        
        val result = chooser.showSaveDialog(null)
        if (result == JFileChooser.APPROVE_OPTION) {
            var file = chooser.selectedFile
            if (!file.name.lowercase().endsWith(".svg")) {
                file = File(file.absolutePath + ".svg")
            }
            file.writeText(content)
        }
    }
}
