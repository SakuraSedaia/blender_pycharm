package com.sakurasedaia.blenderextensions.blender

import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
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
        val logPath = "${project.basePath}/.logs/" ?: return
        if (!Path.of(logPath).exists()) { Path.of(logPath).toFile().mkdirs() }

        // Custom file logging in project root (as requested by user in README)
        val date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val logFile = Path.of(logPath, "blender_plugin_$date.log")
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        try {
            logFile.appendText("[$timestamp] $message\n")
        } catch (_: Exception) {
            // Silently ignore logging errors
        }
    }

    fun error(message: String, e: Throwable? = null) {
        platformLogger.error(message, e)
        log("ERROR: $message" + (if (e != null) " - ${e.message}" else ""))
    }

    companion object {
        fun getInstance(project: Project): BlenderLogger = project.getService(BlenderLogger::class.java)
    }
}
