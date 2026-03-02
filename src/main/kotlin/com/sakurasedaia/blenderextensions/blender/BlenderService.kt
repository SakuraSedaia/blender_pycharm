package com.sakurasedaia.blenderextensions.blender

import com.sakurasedaia.blenderextensions.BlenderBundle
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessListener
import com.intellij.execution.process.ProcessEvent
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.sakurasedaia.blenderextensions.settings.BlenderSettings
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.io.path.exists
import kotlin.io.path.name

@Service(Service.Level.PROJECT)
class BlenderService(private val project: Project) {
    private val NOTIFICATION_GROUP = "Blender Extensions"
    private val logger = BlenderLogger.getInstance(project)
    private val downloader = BlenderDownloader.getInstance(project)
    private val linker = BlenderLinker.getInstance(project)
    private val launcher = BlenderLauncher.getInstance(project)
    private val scriptGenerator = BlenderScriptGenerator.getInstance()
    private val communicationService = BlenderCommunicationService.getInstance(project)

    private var processHandler: OSProcessHandler? = null
    private val isRunning = AtomicBoolean(false)
    private val hasError = AtomicBoolean(false)
    fun isRunning(): Boolean = isRunning.get()
    fun hasError(): Boolean = hasError.get()
    private var currentExtensionName: String? = null

    /**
     * Main log entry point for other components (like UI).
     */
    fun log(message: String) = logger.log(message)

    fun getOrDownloadBlenderPath(version: String): String? = downloader.getOrDownloadBlenderPath(version)

    fun clearSandbox() {
        val projectPath = project.basePath ?: return
        val sandboxDir = Path.of(projectPath, ".blender-sandbox")
        if (sandboxDir.exists()) {
            com.intellij.openapi.util.io.FileUtil.delete(sandboxDir.toFile())
            logger.log(BlenderBundle.message("log.service.cleared.sandbox", sandboxDir.toString()))
        }
    }

    fun startBlenderProcess(
        blenderPath: String,
        addonSourceDir: String? = null,
        addonSymlinkName: String? = null,
        additionalArgs: String? = null,
        isSandboxed: Boolean = false,
        blenderCommand: String? = null,
        importUserConfig: Boolean = false,
        blenderVersion: String? = null
    ): OSProcessHandler? {
        if (isRunning.get()) return processHandler
        hasError.set(false)

        val projectPath = project.basePath ?: return null

        val handler = if (!blenderCommand.isNullOrBlank()) {
            launcher.startBlenderProcess(
                blenderPath = blenderPath,
                additionalArgs = additionalArgs,
                isSandboxed = isSandboxed,
                blenderCommand = blenderCommand,
                importUserConfig = importUserConfig,
                blenderVersion = blenderVersion
            )
        } else {
            val sourcePath = if (!addonSourceDir.isNullOrEmpty()) {
                Path.of(addonSourceDir)
            } else {
                val markedSource = BlenderSettings.getInstance(project).getSourceFolders().firstOrNull()
                if (markedSource != null) Path.of(markedSource) else Path.of(projectPath)
            }

            if (!sourcePath.exists()) {
                logger.log(BlenderBundle.message("log.linker.source.not.found", sourcePath.toString()))
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
                isSandboxed = isSandboxed,
                importUserConfig = importUserConfig,
                blenderVersion = blenderVersion
            )
        }

        if (handler == null) {
            hasError.set(true)
            if (blenderCommand.isNullOrBlank()) {
                communicationService.stopServer()
            }
            
            NotificationGroupManager.getInstance()
                .getNotificationGroup(NOTIFICATION_GROUP)
                .createNotification(
                    BlenderBundle.message("notification.failed.start.blender.title"),
                    BlenderBundle.message("notification.failed.start.blender.message"),
                    NotificationType.ERROR
                )
                .notify(project)
                
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
        try {
            communicationService.sendReloadCommand(extensionName)
        } catch (e: Exception) {
            NotificationGroupManager.getInstance()
                .getNotificationGroup(NOTIFICATION_GROUP)
                .createNotification(
                    BlenderBundle.message("notification.reload.failed.title"),
                    BlenderBundle.message("notification.reload.failed.message", e.message ?: ""),
                    NotificationType.WARNING
                )
                .notify(project)
            logger.log(BlenderBundle.message("log.blender.failed.reload", e.message ?: ""))
        }
    }

    companion object {
        fun getInstance(project: Project): BlenderService = project.getService(BlenderService::class.java)
    }
}
