package com.sakurasedaia.blenderextensions.blender

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.OSProcessHandler
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import kotlin.io.path.*

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
        val blenderFile = Paths.get(blenderPath)
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
            setupSandbox(commandLine, importUserConfig, blenderVersion, blenderCommand)
        }
        
        if (!additionalArgs.isNullOrBlank()) {
            commandLine.addParameters(additionalArgs.split(" "))
        }
        
        logger.log("Executing command: ${commandLine.commandLineString}")
        return OSProcessHandler(commandLine)
    }

    private fun setupSandbox(
        commandLine: GeneralCommandLine,
        importUserConfig: Boolean,
        blenderVersion: String?,
        blenderCommand: String?
    ) {
        logger.log("Using sandboxed environment")
        val projectPath = project.basePath ?: return
        val sandboxDir = Paths.get(projectPath, ".blender-sandbox")
        val configDir = sandboxDir.resolve("config")
        val scriptsDir = sandboxDir.resolve("scripts")
        
        configDir.createDirectories()
        scriptsDir.createDirectories()
        
        if (importUserConfig) {
            importBlenderConfig(configDir, blenderVersion)
        }

        // Create a simple app template
        val templatesDir = scriptsDir.resolve("startup/bl_app_templates/pycharm")
        templatesDir.createDirectories()
        val initFile = templatesDir.resolve("__init__.py")
        if (!initFile.exists()) {
            initFile.writeText("\"\"\"\n# Blender Extension Development App Template\n# Created by the Blender Extension Development for PyCharm plugin.\n\"\"\"\ndef register():\n    pass\n\ndef unregister():\n    pass\n")
        }

        handleSandboxSplashScreen(templatesDir)
        
        commandLine.withEnvironment("BLENDER_USER_CONFIG", configDir.absolutePathString())
        commandLine.withEnvironment("BLENDER_USER_SCRIPTS", scriptsDir.absolutePathString())

        val isExtensionCommand = blenderCommand?.contains("extension") == true

        if (!isExtensionCommand) {
            commandLine.addParameters("--app-template", "pycharm")
        } else {
            logger.log("Extension command detected, skipping --app-template")
        }
    }

    private fun importBlenderConfig(targetConfigDir: Path, version: String?) {
        val versionToUse = version ?: "5.0" // Fallback to 5.0
        val sourceConfigDir = findSystemBlenderConfigDir(versionToUse)
        
        if (sourceConfigDir == null || !sourceConfigDir.exists()) {
            logger.log("Could not find system Blender config directory for version $versionToUse")
            return
        }

        logger.log("Importing user config from: ${sourceConfigDir.absolutePathString()}")
        val filesToCopy = listOf("userpref.blend", "startup.blend", "bookmarks.txt", "recent-files.txt", "recent-searches.txt")
        
        for (fileName in filesToCopy) {
            val sourceFile = sourceConfigDir.resolve(fileName)
            if (sourceFile.exists()) {
                try {
                    sourceFile.copyTo(targetConfigDir.resolve(fileName), overwrite = true)
                    logger.log("Imported $fileName")
                } catch (e: Exception) {
                    logger.log("Failed to import $fileName: ${e.message}")
                }
            }
        }
        
        // Copy special directories if they exist (pycharm, sedaia)
        // TODO: Change the loop to dynamically detect and copy special directories, instead of the directories being hard-coded.
        for (dirName in listOf("pycharm", "sedaia")) {
            val sourceDir = sourceConfigDir.resolve(dirName)
            if (sourceDir.exists() && sourceDir.isDirectory()) {
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
        source.walk().forEach { sourcePath ->
            val targetPath = target.resolve(source.relativize(sourcePath))
            if (sourcePath.isDirectory()) {
                targetPath.createDirectories()
            } else {
                sourcePath.copyTo(targetPath, overwrite = true)
            }
        }
    }

    private fun findSystemBlenderConfigDir(version: String): Path? {
        val osName = System.getProperty("os.name").lowercase()
        val userHome = System.getProperty("user.home")
        
        return when {
            osName.contains("win") -> {
                val appData = System.getenv("APPDATA")
                if (appData != null) Paths.get(appData, "Blender Foundation", "Blender", version, "config") else null
            }
            osName.contains("mac") -> {
                Paths.get(userHome, "Library", "Application Support", "Blender", version, "config")
            }
            else -> { // Linux/Unix
                Paths.get(userHome, ".config", "blender", version, "config")
            }
        }
    }

    private fun handleSandboxSplashScreen(templatesDir: Path) {
        val projectPath = project.basePath ?: return
        val projectSplash = Paths.get(projectPath, "images/splash.png")
        val targetSplash = templatesDir.resolve("splash.png")
        
        if (projectSplash.exists()) {
            try {
                projectSplash.copyTo(targetSplash, overwrite = true)
                logger.log("Copied project-specific splash screen to sandboxed app template")
            } catch (e: Exception) {
                logger.log("Failed to copy project splash screen: ${e.message}")
            }
        } else {
            // Try to copy the default splash from plugin resources
            try {
                this.javaClass.getResourceAsStream("/images/sandbox_splash.png")?.use { input ->
                    Files.copy(input, targetSplash, StandardCopyOption.REPLACE_EXISTING)
                }
                logger.log("Copied default splash screen from plugin resources to sandboxed app template")
            } catch (e: Exception) {
                logger.log("Failed to copy default splash screen: ${e.message}")
            }
        }
    }

    companion object {
        fun getInstance(project: Project): BlenderLauncher = project.service()
    }
}
