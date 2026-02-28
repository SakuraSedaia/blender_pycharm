package com.sakurasedaia.blenderextensions.blender

import com.intellij.openapi.util.SystemInfo
import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.Collectors
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.name

data class BlenderInstallation(
    val name: String,
    val path: String,
    val version: String,
    val isManaged: Boolean = false,
    val isCustom: Boolean = false,
    val originPath: String? = null
)

object BlenderScanner {
    private var cachedInstallations: List<BlenderInstallation>? = null

    fun scanSystemInstallations(force: Boolean = false, customPaths: Map<String, String> = emptyMap()): List<BlenderInstallation> {
        if (!force && cachedInstallations != null) return cachedInstallations!!
        
        val installations = mutableListOf<BlenderInstallation>()
        
        when {
            SystemInfo.isWindows -> installations.addAll(scanWindows())
            SystemInfo.isMac -> installations.addAll(scanMac())
            SystemInfo.isLinux -> installations.addAll(scanLinux())
        }

        customPaths.forEach { (pathStr, customName) ->
            val path = Path.of(pathStr)
            if (path.exists()) {
                val exe = if (path.isDirectory()) findBlenderExecutable(path) else path
                if (exe != null && exe.exists()) {
                    installations.add(BlenderInstallation(customName, exe.toString(), "Unknown", isCustom = true, originPath = pathStr))
                }
            }
        }
        
        val result = installations.distinctBy { it.path }.map { 
            if (it.version == "Unknown") {
                it.copy(version = tryGetVersion(it.path))
            } else {
                it
            }
        }
        cachedInstallations = result
        return result
    }

    private fun findBlenderExecutable(path: Path): Path? {
        val exeName = if (SystemInfo.isWindows) "blender.exe" else if (SystemInfo.isMac) "Blender.app/Contents/MacOS/Blender" else "blender"
        val exe = path.resolve(exeName)
        if (exe.exists()) return exe

        // Deep search if not immediately found in root
        try {
            Files.walk(path, 3).use { stream ->
                return stream.filter { it.name == (if (SystemInfo.isWindows) "blender.exe" else "blender") && !it.isDirectory() }
                    .findFirst()
                    .orElse(null)
            }
        } catch (e: Exception) {
            return null
        }
    }

    private fun tryGetVersion(path: String): String {
        try {
            val process = ProcessBuilder(path, "--version").start()
            val output = process.inputStream.bufferedReader().readText()
            process.waitFor()
            // Output looks like "Blender 4.2.0\nbuild date: ..."
            val match = Regex("Blender (\\d+\\.\\d+)").find(output)
            return match?.groupValues?.get(1) ?: "Unknown"
        } catch (e: Exception) {
            return "Unknown"
        }
    }

    private fun scanWindows(): List<BlenderInstallation> {
        val paths = mutableListOf<BlenderInstallation>()
        val programFiles = System.getenv("ProgramFiles") ?: "C:\\Program Files"
        val programFilesX86 = System.getenv("ProgramFiles(x86)") ?: "C:\\Program Files (x86)"
        
        listOf(programFiles, programFilesX86).forEach { base ->
            val blenderFoundation = Path.of(base, "Blender Foundation")
            if (blenderFoundation.exists() && blenderFoundation.isDirectory()) {
                Files.list(blenderFoundation).use { stream ->
                    stream.filter { it.isDirectory() && it.name.startsWith("Blender") }
                        .forEach { dir ->
                            val exe = dir.resolve("blender.exe")
                            if (exe.exists()) {
                                val version = dir.name.removePrefix("Blender").trim()
                                paths.add(BlenderInstallation("Blender $version (System)", exe.toString(), version))
                            }
                        }
                }
            }
        }
        return paths
    }

    private fun scanMac(): List<BlenderInstallation> {
        val installations = mutableListOf<BlenderInstallation>()
        val searchPaths = listOf("/Applications", System.getProperty("user.home") + "/Applications")
        
        searchPaths.forEach { base ->
            val basePath = Path.of(base)
            if (basePath.exists() && basePath.isDirectory()) {
                Files.list(basePath).use { stream ->
                    stream.filter { it.isDirectory() && it.name == "Blender.app" }
                        .forEach { app ->
                            val exe = app.resolve("Contents/MacOS/Blender")
                            if (exe.exists()) {
                                // On Mac, version is harder to get from folder name, we'd need to check Info.plist
                                // For now, just call it "Blender (System)" or try to find version via CLI
                                installations.add(BlenderInstallation("Blender (System: $base)", exe.toString(), "Unknown"))
                            }
                        }
                }
            }
        }
        return installations
    }

    private fun scanLinux(): List<BlenderInstallation> {
        val installations = mutableListOf<BlenderInstallation>()
        
        // Common binaries in PATH
        val commonBinaries = listOf("/usr/bin/blender", "/usr/local/bin/blender", System.getProperty("user.home") + "/bin/blender")
        commonBinaries.forEach { pathStr ->
            val path = Path.of(pathStr)
            if (path.exists() && Files.isExecutable(path)) {
                installations.add(BlenderInstallation("Blender (System: $pathStr)", path.toString(), "Unknown"))
            }
        }
        
        // Check /opt
        val opt = Path.of("/opt")
        if (opt.exists() && opt.isDirectory()) {
            Files.list(opt).use { stream ->
                stream.filter { it.isDirectory() && it.name.lowercase().contains("blender") }
                    .forEach { dir ->
                        val exe = dir.resolve("blender")
                        if (exe.exists() && Files.isExecutable(exe)) {
                            installations.add(BlenderInstallation("Blender (${dir.name})", exe.toString(), "Unknown"))
                        }
                    }
            }
        }
        
        return installations
    }
}
