package com.sakurasedaia.blenderextensions.blender

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessAdapter
import com.intellij.execution.process.ProcessEvent
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.sakurasedaia.blenderextensions.settings.BlenderSettings
import java.io.File
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.atomic.AtomicBoolean

@Service(Service.Level.PROJECT)
class BlenderService(private val project: Project) {
    private var processHandler: OSProcessHandler? = null
    private var serverSocket: ServerSocket? = null
    private val isRunning = AtomicBoolean(false)
    private var blenderClient: Socket? = null

    fun startBlender() {
        // This might be called from old code, but now we need the path.
        // For now, let's just do nothing or handle it if we have a default.
    }

    fun startBlenderProcess(
        blenderPath: String,
        addonSourceDir: String? = null,
        addonSymlinkName: String? = null,
        additionalArgs: String? = null
    ): OSProcessHandler? {
        if (isRunning.get()) return processHandler

        if (blenderPath.isEmpty() || !File(blenderPath).exists()) {
            return null
        }

        linkExtensionSource(addonSourceDir, addonSymlinkName)
        val port = startServer()

        val script = createStartupScript(port)
        val commandLine = GeneralCommandLine(blenderPath)
            .withParameters("--python", script.absolutePath)
        
        if (!additionalArgs.isNullOrBlank()) {
            commandLine.addParameters(additionalArgs.split(" "))
        }
        
        val handler = OSProcessHandler(commandLine)
        processHandler = handler
        handler.addProcessListener(object : ProcessAdapter() {
            override fun processTerminated(event: ProcessEvent) {
                isRunning.set(false)
                stopServer()
            }
        })

        handler.startNotify()
        isRunning.set(true)
        return handler
    }

    private fun linkExtensionSource(addonSourceDir: String?, addonSymlinkName: String?) {
        val os = System.getProperty("os.name").lowercase()
        val userHome = System.getProperty("user.home")
        val blenderConfigDir = when {
            os.contains("win") -> File(System.getenv("APPDATA"), "Blender Foundation/Blender")
            os.contains("mac") -> File(userHome, "Library/Application Support/Blender")
            else -> File(userHome, ".config/blender") // Linux
        }

        if (!blenderConfigDir.exists()) return

        // Find the latest version directory
        val versions = blenderConfigDir.listFiles { file -> file.isDirectory && file.name.all { it.isDigit() || it == '.' } }
        val latestVersion = versions?.maxByOrNull { it.name } ?: return

        val extensionsDir = File(latestVersion, "extensions")
        val userRepoDir = File(extensionsDir, "blender_pycharm")
        
        if (!userRepoDir.exists()) {
            userRepoDir.mkdirs()
        }

        val sourceDir = if (!addonSourceDir.isNullOrEmpty()) File(addonSourceDir) else File(project.basePath ?: return)
        if (!sourceDir.exists()) return

        val symlinkName = if (!addonSymlinkName.isNullOrEmpty()) addonSymlinkName else sourceDir.name
        val targetLink = File(userRepoDir, symlinkName)

        if (targetLink.exists()) {
            // Check if it's already a link to the same place, if not maybe we should recreate it?
            // For now, let's just delete it if it's there to ensure it's correct.
            targetLink.delete()
        }

        try {
            java.nio.file.Files.createSymbolicLink(
                targetLink.toPath(),
                sourceDir.toPath()
            )
        } catch (e: Exception) {
            System.err.println("Failed to create symbolic link: ${e.message}")
        }
    }

    private fun startServer(): Int {
        val server = ServerSocket(0)
        serverSocket = server
        val port = server.localPort
        project.putUserData(BLENDER_PORT_KEY, port)

        Thread {
            try {
                while (!server.isClosed) {
                    val client = try {
                        server.accept()
                    } catch (e: Exception) {
                        null
                    }
                    blenderClient = client
                }
            } catch (e: Exception) {
                // Handle error
            }
        }.start()
        return port
    }

    private fun stopServer() {
        serverSocket?.close()
        blenderClient?.close()
    }

    private fun createStartupScript(port: Int): File {
        val scriptContent = """
            import bpy
            import socket
            import threading
            import os

            def ensure_extension_repo_exists(repo_name):
                if bpy.app.version < (4, 2, 0):
                    return
                for repo in bpy.context.preferences.extensions.repos:
                    if repo.module == repo_name:
                        return
                try:
                    bpy.context.preferences.extensions.repos.new(name=repo_name, module=repo_name)
                    print(f"Created new extensions repository: {repo_name}")
                except Exception as e:
                    print(f"Failed to create extensions repository: {e}")

            def listen_for_reload():
                s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
                try:
                    s.connect(('127.0.0.1', $port))
                    while True:
                        data = s.recv(1024)
                        if not data:
                            break
                        if data.decode().strip() == 'RELOAD':
                            # Try to reload the extension package
                            try:
                                bpy.ops.extensions.package_reload()
                            except:
                                pass
                except Exception as e:
                    print(f"Error in listen_for_reload: {e}")
                finally:
                    s.close()
            
            ensure_extension_repo_exists("blender_pycharm")
            threading.Thread(target=listen_for_reload, daemon=True).start()
        """.trimIndent()
        
        val tempFile = File.createTempFile("blender_start", ".py")
        tempFile.writeText(scriptContent)
        return tempFile
    }

    fun reloadExtension() {
        blenderClient?.let {
            try {
                val out = PrintWriter(it.getOutputStream(), true)
                out.println("RELOAD")
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    companion object {
        val BLENDER_PORT_KEY = Key.create<Int>("BLENDER_PORT")
        fun getInstance(project: Project): BlenderService = project.getService(BlenderService::class.java)
    }
}
