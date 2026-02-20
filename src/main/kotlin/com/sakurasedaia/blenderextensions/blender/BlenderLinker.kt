package com.sakurasedaia.blenderextensions.blender

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.name

@Service(Service.Level.PROJECT)
class BlenderLinker(private val project: Project) {
    private val logger = BlenderLogger.getInstance(project)

    fun linkExtensionSource(addonSourceDir: String?, addonSymlinkName: String?, isSandboxed: Boolean = false) {
        val userRepoDir = getExtensionsRepoDir(isSandboxed) ?: return
        
        if (!userRepoDir.exists()) {
            Files.createDirectories(userRepoDir)
        }

        val projectPath = project.basePath ?: return
        val sourcePath = if (!addonSourceDir.isNullOrEmpty()) Path.of(addonSourceDir) else Path.of(projectPath)
        
        if (!sourcePath.exists()) {
            logger.log("Source directory does not exist: $sourcePath")
            return
        }

        val symlinkName = if (!addonSymlinkName.isNullOrEmpty()) addonSymlinkName else sourcePath.name
        val targetLink = userRepoDir.resolve(symlinkName)

        if (targetLink.exists()) {
            try {
                Files.delete(targetLink)
            } catch (e: Exception) {
                logger.log("Failed to delete existing link at $targetLink: ${e.message}")
            }
        }

        try {
            Files.createSymbolicLink(targetLink, sourcePath)
            logger.log("Created symbolic link: $targetLink -> $sourcePath")
        } catch (e: Exception) {
            logger.log("Failed to create symbolic link: ${e.message}")
            if (System.getProperty("os.name").lowercase().contains("win")) {
                createWindowsJunction(targetLink, sourcePath)
            }
        }
    }

    private fun createWindowsJunction(target: Path, source: Path) {
        logger.log("Attempting to create a directory junction instead (Windows fallback)...")
        try {
            val commandLine = GeneralCommandLine("cmd", "/c", "mklink", "/J", target.toString(), source.toString())
            val process = commandLine.createProcess()
            val exitCode = process.waitFor()
            if (exitCode == 0) {
                logger.log("Successfully created directory junction.")
            } else {
                logger.log("mklink failed with exit code $exitCode")
            }
        } catch (e: Exception) {
            logger.log("Failed to create junction: ${e.message}")
        }
    }

    fun getExtensionsRepoDir(isSandboxed: Boolean = false): Path? {
        if (isSandboxed) {
            val projectPath = project.basePath ?: return null
            return Path.of(projectPath, ".blender_sandbox", "extensions", "blender_pycharm")
        }
        
        val osName = System.getProperty("os.name").lowercase()
        val userHome = System.getProperty("user.home")
        val blenderConfigDir = when {
            osName.contains("win") -> Path.of(System.getenv("APPDATA"), "Blender Foundation", "Blender")
            osName.contains("mac") -> Path.of(userHome, "Library", "Application Support", "Blender")
            else -> Path.of(userHome, ".config", "blender") // Linux
        }

        if (!blenderConfigDir.exists()) return null

        val versions = Files.list(blenderConfigDir).use { stream ->
            stream.filter { path ->
                Files.isDirectory(path) && path.name.all { it.isDigit() || it == '.' }
            }.toList()
        }
        val latestVersion = versions.maxByOrNull { it.name } ?: return null

        return latestVersion.resolve("extensions").resolve("blender_pycharm")
    }

    companion object {
        fun getInstance(project: Project): BlenderLinker = project.getService(BlenderLinker::class.java)
    }
}
