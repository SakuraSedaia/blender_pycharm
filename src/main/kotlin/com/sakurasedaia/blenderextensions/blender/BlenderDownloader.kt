package com.sakurasedaia.blenderextensions.blender

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.OSProcessHandler
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.util.io.HttpRequests
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.*

@Service(Service.Level.PROJECT)
class BlenderDownloader(private val project: Project) {
    private val logger = BlenderLogger.getInstance(project)

    fun getOrDownloadBlenderPath(version: String): String? {
        val osName = System.getProperty("os.name").lowercase()
        val arch = System.getProperty("os.arch").lowercase()
        val isWindows = osName.contains("win")
        val isMac = osName.contains("mac")
        val isLinux = !isWindows && !isMac

        val downloadDir = Path.of(PathManager.getSystemPath(), "blender_downloads", version)
        if (!downloadDir.exists()) {
            Files.createDirectories(downloadDir)
        }

        // Check if already downloaded
        val executable = findBlenderExecutable(downloadDir)
        if (executable != null) {
            logger.log("Blender $version already exists at ${executable.absolutePathString()}")
            return executable.absolutePathString()
        }

        // If not, download it
        val downloadUrl = getDownloadUrl(version, isWindows, isMac, isLinux, arch) ?: run {
            logger.log("Could not resolve download URL for Blender $version")
            return null
        }
        
        logger.log("Downloading Blender $version from $downloadUrl")
        val downloadedFile = downloadFile(downloadUrl, downloadDir) ?: run {
            logger.log("Failed to download Blender from $downloadUrl")
            return null
        }
        
        // Extract it
        logger.log("Extracting ${downloadedFile.name}...")
        extractFile(downloadedFile, downloadDir)
        
        val finalExecutable = findBlenderExecutable(downloadDir)
        if (finalExecutable != null) {
            logger.log("Blender $version successfully installed at ${finalExecutable.absolutePathString()}")
        } else {
            logger.log("Failed to find Blender executable after extraction in ${downloadDir.absolutePathString()}")
        }
        return finalExecutable?.absolutePathString()
    }

    private fun findBlenderExecutable(directory: Path): Path? {
        val osName = System.getProperty("os.name").lowercase()
        val isWindows = osName.contains("win")
        val isMac = osName.contains("mac")

        val executableName = if (isWindows) "blender.exe" else "blender"
        
        // Walk the directory to find the executable
        Files.walk(directory).use { stream ->
            return stream.filter { path ->
                if (isMac) {
                    path.name == "Blender" && path.toString().contains("Blender.app/Contents/MacOS")
                } else {
                    path.name == executableName && path.isRegularFile() && (isWindows || Files.isExecutable(path))
                }
            }.findFirst().orElse(null)
        }
    }

    private fun getDownloadUrl(version: String, isWindows: Boolean, isMac: Boolean, isLinux: Boolean, arch: String): String? {
        val baseUrl = "https://download.blender.org/release/Blender$version/"
        val platformSuffix = when {
            isWindows -> "windows-x64\\.zip"
            isMac -> if (arch.contains("aarch64") || arch.contains("arm64")) "macos-arm64\\.dmg" else "macos-x64\\.dmg"
            else -> "linux-x64\\.tar\\.xz"
        }
        val regex = Regex("blender-$version\\.(\\d+)-$platformSuffix")
        try {
            val html = HttpRequests.request(baseUrl).readString()
            val matches = regex.findAll(html).toList()
            val best = matches.maxByOrNull { it.groupValues[1].toIntOrNull() ?: -1 }?.value
            if (best != null) return baseUrl + best
        } catch (e: Exception) {
            logger.log("Error during online version detection: ${e.message}")
        }
        
        // Fallback to a safe default if online detection fails
        val fallbackPatch = FALLBACK_PATCHES[version] ?: "0"
        val suffix = when {
            isWindows -> "windows-x64.zip"
            isMac -> if (arch.contains("aarch64") || arch.contains("arm64")) "macos-arm64.dmg" else "macos-x64.dmg"
            else -> "linux-x64.tar.xz"
        }
        return "${baseUrl}blender-$version.$fallbackPatch-$suffix"
    }

    private fun downloadFile(url: String, targetDir: Path): Path? {
        val fileName = url.substringAfterLast("/")
        val targetFile = targetDir.resolve(fileName)
        
        val indicator = ProgressManager.getInstance().progressIndicator
        indicator?.text = "Downloading Blender..."
        indicator?.text2 = url
        
        try {
            HttpRequests.request(url)
                .connect { request ->
                    request.saveToFile(targetFile, indicator)
                }
            return targetFile
        } catch (e: Exception) {
            if (e is com.intellij.openapi.progress.ProcessCanceledException) {
                logger.log("Download cancelled by user")
            } else {
                logger.log("Download failed: ${e.message}")
            }
            return null
        }
    }

    private fun extractFile(file: Path, targetDir: Path) {
        val indicator = ProgressManager.getInstance().progressIndicator
        indicator?.text = "Extracting Blender..."
        indicator?.text2 = file.name
        indicator?.isIndeterminate = true
        
        val fileName = file.name
        when {
            fileName.endsWith(".zip") -> extractZip(file, targetDir)
            fileName.endsWith(".tar.xz") -> extractTarXz(file, targetDir)
            fileName.endsWith(".dmg") -> extractDmg(file, targetDir)
            else -> logger.log("Unsupported file format for extraction: $fileName")
        }
    }

    private fun extractZip(file: Path, targetDir: Path) {
        val isWindows = System.getProperty("os.name").lowercase().contains("win")
        val commandLine = if (isWindows) {
            GeneralCommandLine("powershell", "Expand-Archive", "-Path", file.absolutePathString(), "-DestinationPath", targetDir.absolutePathString(), "-Force")
        } else {
            GeneralCommandLine("unzip", "-o", file.absolutePathString(), "-d", targetDir.absolutePathString())
        }
        executeExtractionCommand(commandLine)
    }

    private fun extractTarXz(file: Path, targetDir: Path) {
        val commandLine = GeneralCommandLine("tar", "-xf", file.absolutePathString(), "-C", targetDir.absolutePathString())
        executeExtractionCommand(commandLine)
    }

    private fun extractDmg(file: Path, targetDir: Path) {
        if (!System.getProperty("os.name").lowercase().contains("mac")) {
            logger.log("DMG extraction is only supported on macOS")
            return
        }

        val mountPoint = Path.of("/tmp", "blender_mount_${System.currentTimeMillis()}")
        Files.createDirectories(mountPoint)
        try {
            logger.log("Mounting ${file.absolutePathString()} to ${mountPoint.absolutePathString()}")
            executeExtractionCommand(GeneralCommandLine("hdiutil", "attach", file.absolutePathString(), "-mountpoint", mountPoint.absolutePathString(), "-nobrowse", "-readonly"))
            
            Files.list(mountPoint).use { stream ->
                val appFile = stream.filter { it.name == "Blender.app" }.findFirst().orElse(null)
                if (appFile != null) {
                    logger.log("Copying Blender.app to ${targetDir.absolutePathString()}")
                    executeExtractionCommand(GeneralCommandLine("cp", "-R", appFile.absolutePathString(), targetDir.absolutePathString()))
                } else {
                    logger.log("Could not find Blender.app in mounted DMG")
                }
            }
        } catch (e: Exception) {
            logger.log("Error during DMG handling: ${e.message}")
        } finally {
            logger.log("Detaching DMG from ${mountPoint.absolutePathString()}")
            executeExtractionCommand(GeneralCommandLine("hdiutil", "detach", mountPoint.absolutePathString()))
            try {
                Files.deleteIfExists(mountPoint)
            } catch (_: Exception) {}
        }
    }

    private fun executeExtractionCommand(commandLine: GeneralCommandLine) {
        try {
            val handler = OSProcessHandler(commandLine)
            handler.startNotify()
            while (!handler.waitFor(100)) {
                ProgressManager.checkCanceled()
            }
            if (handler.exitCode != 0) {
                logger.log("Extraction command failed with exit code ${handler.exitCode}: ${commandLine.commandLineString}")
            }
        } catch (e: Exception) {
            if (e is com.intellij.openapi.progress.ProcessCanceledException) {
                logger.log("Extraction cancelled by user")
                throw e
            }
            logger.log("Failed to execute extraction command: ${e.message}")
        }
    }

    companion object {
        fun getInstance(project: Project): BlenderDownloader = project.getService(BlenderDownloader::class.java)

        private val FALLBACK_PATCHES = mapOf(
            "4.2" to "18",
            "4.3" to "2",
            "4.4" to "1",
            "4.5" to "0",
            "5.0" to "1"
        )
    }
}
