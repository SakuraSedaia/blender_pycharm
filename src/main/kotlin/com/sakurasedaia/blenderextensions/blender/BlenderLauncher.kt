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
        scriptPath: Path? = null,
        additionalArgs: String? = null,
        isSandboxed: Boolean = false,
        blenderCommand: String? = null,
        importUserConfig: Boolean = false,
        blenderVersion: String? = null
    ): OSProcessHandler? {
        val blenderFile = Path.of(blenderPath)
        if (!blenderFile.exists()) {
            logger.log("Blender executable does not exist: $blenderPath")
            return null
        }

        val commandLine = GeneralCommandLine(blenderPath)
        
        if (!blenderCommand.isNullOrBlank()) {
            commandLine.addParameters("--command")
            commandLine.addParameters(blenderCommand.split(" "))
        } else if (scriptPath != null) {
            commandLine.addParameters("--python", scriptPath.absolutePathString())
        }
        
        if (isSandboxed) {
            setupSandbox(commandLine, importUserConfig, blenderVersion)
        }
        
        if (!additionalArgs.isNullOrBlank()) {
            commandLine.addParameters(additionalArgs.split(" "))
        }
        
        logger.log("Executing command: ${commandLine.commandLineString}")
        return OSProcessHandler(commandLine)
    }

    private fun setupSandbox(commandLine: GeneralCommandLine, importUserConfig: Boolean, blenderVersion: String?) {
        logger.log("Using sandboxed mode")
        val projectPath = project.basePath ?: return
        val sandboxDir = Path.of(projectPath, ".blender_sandbox")
        val configDir = sandboxDir.resolve("config")
        val scriptsDir = sandboxDir.resolve("scripts")
        
        Files.createDirectories(configDir)
        Files.createDirectories(scriptsDir)
        
        if (importUserConfig) {
            importBlenderConfig(configDir, blenderVersion)
        }

        // Create a simple app template
        val templatesDir = scriptsDir.resolve("startup/bl_app_templates/blender_extensions_dev")
        Files.createDirectories(templatesDir)
        val initFile = templatesDir.resolve("__init__.py")
        if (!initFile.exists()) {
            Files.writeString(initFile, "\"\"\"\nBlender Extension Development App Template\nCreated by the Blender Extension Development for PyCharm plugin.\n\"\"\"\ndef register():\n    pass\n\ndef unregister():\n    pass\n")
        }

        handleSandboxSplashScreen(templatesDir)
        
        commandLine.withEnvironment("BLENDER_USER_CONFIG", configDir.absolutePathString())
        commandLine.withEnvironment("BLENDER_USER_SCRIPTS", scriptsDir.absolutePathString())
        commandLine.addParameters("--app-template", "blender_extensions_dev")
    }

    private fun importBlenderConfig(targetConfigDir: Path, version: String?) {
        val versionToUse = version ?: "5.0" // Fallback to 5.0
        val sourceConfigDir = findSystemBlenderConfigDir(versionToUse)
        
        if (sourceConfigDir == null || !Files.exists(sourceConfigDir)) {
            logger.log("Could not find system Blender config directory for version $versionToUse")
            return
        }

        logger.log("Importing user config from: ${sourceConfigDir.absolutePathString()}")
        val filesToCopy = listOf("userpref.blend", "startup.blend", "bookmarks.txt", "recent-files.txt", "recent-searches.txt")
        
        for (fileName in filesToCopy) {
            val sourceFile = sourceConfigDir.resolve(fileName)
            if (Files.exists(sourceFile)) {
                try {
                    Files.copy(sourceFile, targetConfigDir.resolve(fileName), java.nio.file.StandardCopyOption.REPLACE_EXISTING)
                    logger.log("Imported $fileName")
                } catch (e: Exception) {
                    logger.log("Failed to import $fileName: ${e.message}")
                }
            }
        }
        
        // Copy special directories if they exist (pycharm, sedaia)
        for (dirName in listOf("pycharm", "sedaia")) {
            val sourceDir = sourceConfigDir.resolve(dirName)
            if (Files.exists(sourceDir) && Files.isDirectory(sourceDir)) {
                try {
                    copyDirectory(sourceDir, targetConfigDir.resolve(dirName))
                    logger.log("Imported $dirName folder")
                } catch (e: Exception) {
                    logger.log("Failed to import $dirName folder: ${e.message}")
                }
            }
        }
    }

    private fun copyDirectory(source: Path, target: Path) {
        Files.walk(source).use { stream ->
            stream.forEach { sourcePath ->
                val targetPath = target.resolve(source.relativize(sourcePath))
                if (Files.isDirectory(sourcePath)) {
                    Files.createDirectories(targetPath)
                } else {
                    Files.copy(sourcePath, targetPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING)
                }
            }
        }
    }

    private fun findSystemBlenderConfigDir(version: String): Path? {
        val osName = System.getProperty("os.name").lowercase()
        val userHome = System.getProperty("user.home")
        
        return when {
            osName.contains("win") -> {
                val appData = System.getenv("APPDATA")
                if (appData != null) Path.of(appData, "Blender Foundation", "Blender", version, "config") else null
            }
            osName.contains("mac") -> {
                Path.of(userHome, "Library", "Application Support", "Blender", version, "config")
            }
            else -> { // Linux/Unix
                Path.of(userHome, ".config", "blender", version, "config")
            }
        }
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
