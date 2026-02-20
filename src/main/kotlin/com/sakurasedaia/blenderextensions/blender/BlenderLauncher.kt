package com.sakurasedaia.blenderextensions.blender

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.OSProcessHandler
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.exists

@Service(Service.Level.PROJECT)
class BlenderLauncher(private val project: Project) {
    private val logger = BlenderLogger.getInstance(project)

    fun startBlenderProcess(
        blenderPath: String,
        scriptPath: Path,
        additionalArgs: String? = null,
        isSandboxed: Boolean = false
    ): OSProcessHandler? {
        val blenderFile = Path.of(blenderPath)
        if (!blenderFile.exists()) {
            logger.log("Blender executable does not exist: $blenderPath")
            return null
        }

        val commandLine = GeneralCommandLine(blenderPath)
            .withParameters("--python", scriptPath.absolutePathString())
        
        if (isSandboxed) {
            setupSandbox(commandLine)
        }
        
        if (!additionalArgs.isNullOrBlank()) {
            commandLine.addParameters(additionalArgs.split(" "))
        }
        
        logger.log("Executing command: ${commandLine.commandLineString}")
        return OSProcessHandler(commandLine)
    }

    private fun setupSandbox(commandLine: GeneralCommandLine) {
        logger.log("Using sandboxed mode")
        val projectPath = project.basePath ?: return
        val sandboxDir = Path.of(projectPath, ".blender_sandbox")
        val configDir = sandboxDir.resolve("config")
        val scriptsDir = sandboxDir.resolve("scripts")
        
        Files.createDirectories(configDir)
        Files.createDirectories(scriptsDir)
        
        // Create a simple app template
        val templatesDir = scriptsDir.resolve("startup/bl_app_templates/blender_extensions_dev")
        Files.createDirectories(templatesDir)
        val initFile = templatesDir.resolve("__init__.py")
        if (!initFile.exists()) {
            Files.writeString(initFile, "def register():\n    pass\n\ndef unregister():\n    pass\n")
        }

        handleSandboxSplashScreen(templatesDir)
        
        commandLine.withEnvironment("BLENDER_USER_CONFIG", configDir.absolutePathString())
        commandLine.withEnvironment("BLENDER_USER_SCRIPTS", scriptsDir.absolutePathString())
        commandLine.addParameters("--app-template", "blender_extensions_dev")
    }

    private fun handleSandboxSplashScreen(templatesDir: Path) {
        val projectPath = project.basePath ?: return
        val projectSplash = Path.of(projectPath, "splash.png")
        val targetSplash = templatesDir.resolve("splash.png")
        
        if (projectSplash.exists()) {
            try {
                Files.copy(projectSplash, targetSplash, java.nio.file.StandardCopyOption.REPLACE_EXISTING)
                logger.log("Copied project-specific splash screen to sandboxed app template")
            } catch (e: Exception) {
                logger.log("Failed to copy project splash screen: ${e.message}")
            }
        } else {
            // Try to copy the default splash from plugin resources
            try {
                this.javaClass.getResourceAsStream("/splash.png")?.use { input ->
                    Files.copy(input, targetSplash, java.nio.file.StandardCopyOption.REPLACE_EXISTING)
                }
                logger.log("Copied default splash screen from plugin resources to sandboxed app template")
            } catch (e: Exception) {
                logger.log("Failed to copy default splash screen: ${e.message}")
            }
        }
    }

    companion object {
        fun getInstance(project: Project): BlenderLauncher = project.getService(BlenderLauncher::class.java)
    }
}
