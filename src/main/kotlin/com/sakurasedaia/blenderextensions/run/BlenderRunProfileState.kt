package com.sakurasedaia.blenderextensions.run

import com.intellij.execution.ExecutionException
import com.intellij.execution.Executor
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.filters.TextConsoleBuilderFactory
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.runners.ProgramRunner
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.sakurasedaia.blenderextensions.blender.BlenderService

class BlenderRunProfileState(
    private val project: Project,
    private val options: BlenderRunConfigurationOptions,
    private val environment: ExecutionEnvironment
) : RunProfileState {
    override fun execute(executor: Executor, runner: ProgramRunner<*>): com.intellij.execution.ExecutionResult? {
        val service = BlenderService.getInstance(project)
        service.log("--- Starting Blender Run Configuration: ${environment.runProfile.name} ---")
        
        val version = options.blenderVersion ?: "5.0"
        var detectedVersion: String? = null
        val blenderPath = when {
            version == "Custom/Pre-installed" -> {
                service.log("Using custom Blender path: ${options.blenderExecutablePath}")
                options.blenderExecutablePath
            }
            version in com.sakurasedaia.blenderextensions.blender.BlenderVersions.SUPPORTED_VERSIONS -> {
                service.log("Using managed Blender version: $version")
                detectedVersion = version
                ProgressManager.getInstance().run(object : Task.WithResult<String?, ExecutionException>(project, "Downloading Blender $version", true) {
                    override fun compute(indicator: ProgressIndicator): String? {
                        return service.getOrDownloadBlenderPath(version)
                    }
                })
            }
            else -> {
                service.log("Using system Blender installation: $version")
                // version is a path. Let's find its version for config import.
                val inst = com.sakurasedaia.blenderextensions.blender.BlenderScanner.scanSystemInstallations().find { it.path == version }
                detectedVersion = inst?.version?.takeIf { it != "Unknown" }
                version // It's a path
            }
        }

        if (blenderPath.isNullOrEmpty()) {
            throw ExecutionException("Blender executable path is not configured or version is not downloaded.")
        }

        // If version is still unknown, try a quick scan of the path itself if it's a path
        if (detectedVersion == null && (blenderPath.contains("/") || blenderPath.contains("\\"))) {
             val match = Regex("(\\d+\\.\\d+)").find(blenderPath)
             detectedVersion = match?.groupValues?.get(1)
        }
        
        val handler = service.startBlenderProcess(
            blenderPath = blenderPath,
            addonSourceDir = options.addonSourceDirectory,
            addonSymlinkName = options.addonSymlinkName,
            additionalArgs = options.additionalArguments,
            isSandboxed = options.isSandboxed,
            blenderCommand = options.blenderCommand,
            importUserConfig = options.importUserConfig,
            blenderVersion = detectedVersion
        ) ?: throw ExecutionException("Failed to start Blender. Check path in the run configuration.")
        
        val consoleBuilder = TextConsoleBuilderFactory.getInstance().createBuilder(project)
        val console = consoleBuilder.console
        console.attachToProcess(handler)
        
        return com.intellij.execution.DefaultExecutionResult(console, handler)
    }
}
