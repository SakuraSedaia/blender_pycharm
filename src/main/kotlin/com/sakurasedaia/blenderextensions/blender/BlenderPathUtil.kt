package com.sakurasedaia.blenderextensions.blender

import com.intellij.openapi.util.SystemInfo
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.exists
import kotlin.io.path.name

object BlenderPathUtil {
    private val userHome: String = System.getProperty("user.home")

    fun getSystemBlenderConfigDir(version: String): Path? {
        return when {
            SystemInfo.isWindows -> {
                val appData = System.getenv("APPDATA")
                if (appData != null) Paths.get(appData, "Blender Foundation", "Blender", version, "config") else null
            }
            SystemInfo.isMac -> {
                Paths.get(userHome, "Library", "Application Support", "Blender", version, "config")
            }
            SystemInfo.isLinux -> {
                Paths.get(userHome, ".config", "blender", version, "config")
            }
            else -> null
        }
    }

    fun getBlenderRootConfigDir(): Path? {
        return when {
            SystemInfo.isWindows -> {
                val appData = System.getenv("APPDATA")
                if (appData != null) Paths.get(appData, "Blender Foundation", "Blender") else null
            }
            SystemInfo.isMac -> {
                Paths.get(userHome, "Library", "Application Support", "Blender")
            }
            SystemInfo.isLinux -> {
                Paths.get(userHome, ".config", "blender")
            }
            else -> null
        }
    }

    fun getBlenderExecutableName(): String {
        return when {
            SystemInfo.isWindows -> "blender.exe"
            SystemInfo.isMac -> "Blender.app/Contents/MacOS/Blender"
            else -> "blender"
        }
    }

    fun findPythonExecutable(blenderExePath: Path): Path? {
        val blenderDir = if (SystemInfo.isMac) {
            // blenderExePath is .../Blender.app/Contents/MacOS/Blender
            blenderExePath.parent?.parent?.resolve("Resources")
        } else {
            blenderExePath.parent
        } ?: return null

        if (!blenderDir.exists()) return null

        // Blender's python is usually in a version-named folder, e.g., 4.2/python
        try {
            Files.list(blenderDir).use { stream ->
                val versionDir = stream.filter { 
                    it.name.all { c -> c.isDigit() || c == '.' } && Files.isDirectory(it)
                }.findFirst().orElse(null) ?: return null

                val pythonBinDir = versionDir.resolve("python").resolve("bin")
                val pythonExe = if (SystemInfo.isWindows) pythonBinDir.resolve("python.exe") else pythonBinDir.resolve("python3")
                
                if (pythonExe.exists()) return pythonExe
                
                // Fallback for some linux distributions or older versions
                val pythonExeFallback = if (SystemInfo.isWindows) versionDir.resolve("python").resolve("python.exe") else versionDir.resolve("python").resolve("bin").resolve("python")
                if (pythonExeFallback.exists()) return pythonExeFallback
            }
        } catch (e: Exception) {
            return null
        }
        return null
    }
}
