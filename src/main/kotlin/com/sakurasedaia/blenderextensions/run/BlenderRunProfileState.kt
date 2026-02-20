package com.sakurasedaia.blenderextensions.run

import com.intellij.execution.ExecutionException
import com.intellij.execution.Executor
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.filters.TextConsoleBuilderFactory
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.runners.ProgramRunner
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
        
        val blenderPath = if (options.blenderVersion == null || options.blenderVersion == "Custom/Pre-installed") {
            service.log("Using custom Blender path: ${options.blenderExecutablePath}")
            options.blenderExecutablePath
        } else {
            service.log("Using managed Blender version: ${options.blenderVersion}")
            service.getOrDownloadBlenderPath(options.blenderVersion!!)
        }

        if (blenderPath.isNullOrEmpty()) {
            throw ExecutionException("Blender executable path is not configured or version is not downloaded.")
        }
        
        val handler = service.startBlenderProcess(
            blenderPath,
            options.addonSourceDirectory,
            options.addonSymlinkName,
            options.additionalArguments,
            options.isSandboxed
        ) ?: throw ExecutionException("Failed to start Blender. Check path in the run configuration.")
        
        val consoleBuilder = TextConsoleBuilderFactory.getInstance().createBuilder(project)
        val console = consoleBuilder.console
        console.attachToProcess(handler)
        
        return com.intellij.execution.DefaultExecutionResult(console, handler)
    }
}
