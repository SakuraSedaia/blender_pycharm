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
        val blenderDir = getBlenderInternalDir(blenderExePath) ?: return null
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

    fun getPythonLibraryPaths(pythonExePath: Path): List<Path> {
        val pythonBinDir = pythonExePath.parent
        val pythonDir = pythonBinDir.parent // .../python/
        
        val libPaths = mutableListOf<Path>()
        
        if (SystemInfo.isWindows) {
            libPaths.add(pythonDir.resolve("Lib"))
            libPaths.add(pythonDir.resolve("Lib").resolve("site-packages"))
        } else {
            // On Linux/Mac, it's usually .../python/lib/python3.x
            val libDir = pythonDir.resolve("lib")
            if (libDir.exists()) {
                try {
                    Files.list(libDir).use { stream ->
                        stream.filter { it.name.startsWith("python") && Files.isDirectory(it) }
                            .forEach { pyLib ->
                                libPaths.add(pyLib)
                                libPaths.add(pyLib.resolve("site-packages"))
                            }
                    }
                } catch (_: Exception) {}
            }
        }
        
        // Also look for Blender's own scripts/modules if they are relative to pythonExe
        // Structure: 
        // blender/
        //   5.0/
        //     python/
        //     scripts/
        //       modules/
        val versionDir = pythonDir.parent
        if (versionDir != null) {
            val modulesDir = versionDir.resolve("scripts").resolve("modules")
            if (modulesDir.exists()) {
                libPaths.add(modulesDir)
            }
            val addonModulesDir = versionDir.resolve("scripts").resolve("addons").resolve("modules")
            if (addonModulesDir.exists()) {
                libPaths.add(addonModulesDir)
            }
        }

        return libPaths.filter { it.exists() }
    }

    private fun getBlenderInternalDir(blenderExePath: Path): Path? {
        return if (SystemInfo.isMac) {
            // blenderExePath is .../Blender.app/Contents/MacOS/Blender
            blenderExePath.parent?.parent?.resolve("Resources")
        } else {
            blenderExePath.parent
        }
    }
}
