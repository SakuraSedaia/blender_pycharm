package com.sakurasedaia.blenderextensions.blender

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.util.ExecUtil
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.sakurasedaia.blenderextensions.run.BlenderRunConfigurationOptions
import java.lang.management.ManagementFactory
import java.nio.file.Path
import kotlin.io.path.exists

@Service(Service.Level.PROJECT)
class BlenderTelemetryService(private val project: Project) {
    private val logger = BlenderLogger.getInstance(project)

    fun collectAndLogTelemetry(
        context: String = "Startup",
        options: BlenderRunConfigurationOptions? = null,
        blenderPath: String? = null,
        blenderVersion: String? = null
    ) {
        val osName = System.getProperty("os.name")
        val osVersion = System.getProperty("os.version")
        val osArch = System.getProperty("os.arch")
        val javaVersion = System.getProperty("java.version")
        
        val ramGb = try {
            val osBean = ManagementFactory.getOperatingSystemMXBean() as com.sun.management.OperatingSystemMXBean
            val totalMemory = osBean.totalPhysicalMemorySize
            totalMemory / (1024 * 1024 * 1024)
        } catch (e: Exception) {
            -1L
        }

        val telemetryData = StringBuilder()
        telemetryData.append("\n--- Offline Telemetry [$context] ---\n")
        telemetryData.append("OS: $osName ($osVersion)\n")
        telemetryData.append("Architecture: $osArch\n")
        telemetryData.append("RAM: ${if (ramGb != -1L) "$ramGb GB" else "Unknown"}\n")
        telemetryData.append("Java Version: $javaVersion\n")
        
        // Add project-specific info
        val projectPath = project.basePath
        if (projectPath != null) {
            val sandboxDir = Path.of(projectPath, ".blender-sandbox")
            telemetryData.append("Sandbox exists: ${sandboxDir.exists()}\n")
        }

        // Add Blender info if available
        if (blenderPath != null) {
            telemetryData.append("Blender Path: $blenderPath\n")
            val version = blenderVersion ?: getBlenderVersion(blenderPath)
            telemetryData.append("Blender Version: $version\n")
            
            val pythonInfo = getBlenderPythonInfo(blenderPath)
            telemetryData.append("Python Version: ${pythonInfo.first}\n")
            telemetryData.append("fake-bpy-module status: ${if (pythonInfo.second) "Installed" else "Not Found"}\n")
        }

        // Add Run Configuration settings if available
        if (options != null) {
            telemetryData.append("Run Configuration Settings:\n")
            telemetryData.append("  - Sandboxed: ${options.isSandboxed}\n")
            telemetryData.append("  - Import User Config: ${options.importUserConfig}\n")
            telemetryData.append("  - Additional Args: ${options.additionalArguments}\n")
            telemetryData.append("  - Custom Command: ${options.blenderCommand.isNullOrBlank().not()}\n")
        }
        
        telemetryData.append("--------------------------------------")

        logger.log(telemetryData.toString())
    }

    private fun getBlenderVersion(path: String): String {
        return BlenderScanner.tryGetVersion(path)
    }

    private fun getBlenderPythonInfo(blenderPath: String): Pair<String, Boolean> {
        return try {
            val script = "import sys; import importlib.util; has_fake = importlib.util.find_spec('bpy') is not null; print(f'{sys.version_info.major}.{sys.version_info.minor}.{sys.version_info.micro}|{has_fake}')"
            // Note: In Blender, 'bpy' is always present, but 'fake-bpy-module' usually provides stubs.
            // However, the user asked for 'fake-bpy-module status'.
            // Actually, if we are running INSIDE blender, 'bpy' is the real one.
            // To check for 'fake-bpy-module', we'd need to check the python environment used by PyCharm, 
            // but the request says "include data points about ... fake-bpy-module status".
            // If it means "is it available for the developer in their IDE environment", 
            // checking it from Blender doesn't make sense.
            // But let's check it from Blender's python just in case, or maybe it means "can we import it".
            
            // Re-evaluating: 'fake-bpy-module' is for IDE completion. 
            // Usually, it's installed in the project's virtualenv.
            
            val commandLine = GeneralCommandLine(blenderPath, "--background", "--python-expr", script)
            val output = ExecUtil.execAndGetOutput(commandLine)
            if (output.exitCode == 0) {
                val lastLine = output.stdoutLines.lastOrNull { it.contains("|") }
                if (lastLine != null) {
                    val parts = lastLine.split("|")
                    return Pair(parts[0], parts[1].toBoolean())
                }
            }
            Pair("Unknown", false)
        } catch (e: Exception) {
            Pair("Error: ${e.message}", false)
        }
    }

    companion object {
        fun getInstance(project: Project): BlenderTelemetryService = project.getService(BlenderTelemetryService::class.java)
    }
}
