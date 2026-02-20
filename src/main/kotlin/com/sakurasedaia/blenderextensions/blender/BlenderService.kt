package com.sakurasedaia.blenderextensions.blender

import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessListener
import com.intellij.execution.process.ProcessEvent
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.io.path.exists
import kotlin.io.path.name

@Service(Service.Level.PROJECT)
class BlenderService(private val project: Project) {
    private val logger = BlenderLogger.getInstance(project)
    private val downloader = BlenderDownloader.getInstance(project)
    private val linker = BlenderLinker.getInstance(project)
    private val launcher = BlenderLauncher.getInstance(project)
    private val scriptGenerator = BlenderScriptGenerator.getInstance()
    private val communicationService = BlenderCommunicationService.getInstance(project)

    private var processHandler: OSProcessHandler? = null
    private val isRunning = AtomicBoolean(false)
    private var currentExtensionName: String? = null

    /**
     * Main log entry point for other components (like UI).
     */
    fun log(message: String) = logger.log(message)

    fun getOrDownloadBlenderPath(version: String): String? = downloader.getOrDownloadBlenderPath(version)

    fun startBlenderProcess(
        blenderPath: String,
        addonSourceDir: String? = null,
        addonSymlinkName: String? = null,
        additionalArgs: String? = null,
        isSandboxed: Boolean = false,
        blenderCommand: String? = null
    ): OSProcessHandler? {
        if (isRunning.get()) return processHandler

        val projectPath = project.basePath ?: return null

        val handler = if (!blenderCommand.isNullOrBlank()) {
            launcher.startBlenderProcess(
                blenderPath = blenderPath,
                additionalArgs = additionalArgs,
                isSandboxed = isSandboxed,
                blenderCommand = blenderCommand
            )
        } else {
            val sourcePath = if (!addonSourceDir.isNullOrEmpty()) Path.of(addonSourceDir) else Path.of(projectPath)

            if (!sourcePath.exists()) {
                logger.log("Source directory does not exist: $sourcePath")
                return null
            }

            val symlinkName = if (!addonSymlinkName.isNullOrEmpty()) addonSymlinkName else sourcePath.name
            currentExtensionName = symlinkName

            linker.linkExtensionSource(addonSourceDir, addonSymlinkName, isSandboxed)
            val repoDir = linker.getExtensionsRepoDir(isSandboxed)

            val port = communicationService.startServer()
            val script = scriptGenerator.createStartupScript(port, repoDir, currentExtensionName)

            launcher.startBlenderProcess(
                blenderPath = blenderPath,
                scriptPath = script,
                additionalArgs = additionalArgs,
                isSandboxed = isSandboxed
            )
        }

        if (handler == null) {
            if (blenderCommand.isNullOrBlank()) {
                communicationService.stopServer()
            }
            return null
        }

        processHandler = handler
        handler.addProcessListener(object : ProcessListener {
            override fun processTerminated(event: ProcessEvent) {
                isRunning.set(false)
                communicationService.stopServer()
            }
        })

        handler.startNotify()
        isRunning.set(true)
        return handler
    }

    fun reloadExtension() {
        val extensionName = currentExtensionName ?: "unknown"
        communicationService.sendReloadCommand(extensionName)
    }

    companion object {
        fun getInstance(project: Project): BlenderService = project.getService(BlenderService::class.java)
    }
}
