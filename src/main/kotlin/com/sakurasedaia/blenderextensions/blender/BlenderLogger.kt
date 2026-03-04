package com.sakurasedaia.blenderextensions.blender

import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.application.PathManager
import java.nio.file.Path
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.io.path.appendText
import kotlin.io.path.exists

@Service(Service.Level.PROJECT)
class BlenderLogger(private val project: Project) {
    private val platformLogger = Logger.getInstance(BlenderLogger::class.java)

    fun log(message: String) {
        // Platform logging
        platformLogger.info(message)

        // Create Log Directory
        val scratchPath = Path.of(PathManager.getConfigPath(), "scratches")
        
        val logPath = if (scratchPath.exists()) {
            scratchPath.resolve(".logs")
        } else {
            Path.of("/home/sakura/.config/JetBrains/IntelliJIdea2025.3/scratches/", ".logs")
        }
        
        if (!logPath.exists()) { 
            java.nio.file.Files.createDirectories(logPath)
        }

        // Custom file logging in project root (as requested by user in README)
        val date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val logFile = logPath.resolve("blender_plugin_$date.log")
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        try {
            logFile.appendText("[$timestamp] $message\n")
        } catch (_: Exception) {
            // Silently ignore logging errors
        }
    }

    // TODO: Properly integrate to throw an error when one crops up.
    fun error(message: String, e: Throwable? = null) {
        platformLogger.error(message, e)
        log("ERROR: $message" + (if (e != null) " - ${e.message}" else ""))
    }

    companion object {
        fun getInstance(project: Project): BlenderLogger = project.getService(BlenderLogger::class.java)
    }
}
