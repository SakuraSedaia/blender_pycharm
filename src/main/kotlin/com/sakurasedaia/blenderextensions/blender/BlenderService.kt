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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.atomic.AtomicBoolean

@Service(Service.Level.PROJECT)
class BlenderService(private val project: Project) {
    private var processHandler: OSProcessHandler? = null
    private var serverSocket: ServerSocket? = null
    private val isRunning = AtomicBoolean(false)
    private var blenderClient: Socket? = null
    private var currentExtensionName: String? = null

    fun log(message: String) {
        val logFile = File(project.basePath, "blender_plugin.log")
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        try {
            logFile.appendText("[$timestamp] $message\n")
        } catch (_: Exception) {
        }
        println("[BlenderPlugin] $message")
    }

    fun getOrDownloadBlenderPath(version: String): String? {
        val os = System.getProperty("os.name").lowercase()
        val arch = System.getProperty("os.arch").lowercase()
        val isWindows = os.contains("win")
        val isMac = os.contains("mac")
        val isLinux = !isWindows && !isMac

        val downloadDir = File(com.intellij.openapi.application.PathManager.getSystemPath(), "blender_downloads/$version")
        if (!downloadDir.exists()) {
            downloadDir.mkdirs()
        }

        // Check if already downloaded
        val executable = findBlenderExecutable(downloadDir)
        if (executable != null) {
            log("Blender $version already exists at ${executable.absolutePath}")
            return executable.absolutePath
        }

        // If not, download it
        val downloadUrl = getDownloadUrl(version, isWindows, isMac, isLinux, arch) ?: run {
            log("Could not resolve download URL for Blender $version")
            return null
        }
        log("Downloading Blender $version from $downloadUrl")
        val downloadedFile = downloadFile(downloadUrl, downloadDir) ?: run {
            log("Failed to download Blender from $downloadUrl")
            return null
        }
        
        // Extract it
        log("Extracting ${downloadedFile.name}...")
        extractFile(downloadedFile, downloadDir)
        
        val finalExecutable = findBlenderExecutable(downloadDir)
        if (finalExecutable != null) {
            log("Blender $version successfully installed at ${finalExecutable.absolutePath}")
        } else {
            log("Failed to find Blender executable after extraction in ${downloadDir.absolutePath}")
        }
        return finalExecutable?.absolutePath
    }

    private fun findBlenderExecutable(directory: File): File? {
        val os = System.getProperty("os.name").lowercase()
        val isWindows = os.contains("win")
        val isMac = os.contains("mac")

        val executableName = if (isWindows) "blender.exe" else "blender"
        
        // Walk the directory to find the executable
        return directory.walkTopDown().find { 
            if (isMac) {
                it.name == "Blender" && it.path.contains("Blender.app/Contents/MacOS")
            } else {
                it.name == executableName && it.isFile && (isWindows || it.canExecute())
            }
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
            val html = java.net.URL(baseUrl).readText()
            val matches = regex.findAll(html).toList()
            val best = matches.maxByOrNull { it.groupValues[1].toIntOrNull() ?: -1 }?.value
            if (best != null) return baseUrl + best
        } catch (_: Exception) {
            // ignore and use fallback below
        }
        // Fallback to a safe default if online detection fails
        val fallbackPatch = when (version) {
            "4.2" -> "18"
            "4.3" -> "2"
            "4.4" -> "1"
            "4.5" -> "0"
            "5.0" -> "1"
            else -> "0"
        }
        val suffix = when {
            isWindows -> "windows-x64.zip"
            isMac -> if (arch.contains("aarch64") || arch.contains("arm64")) "macos-arm64.dmg" else "macos-x64.dmg"
            else -> "linux-x64.tar.xz"
        }
        return "$baseUrl" + "blender-$version.$fallbackPatch-$suffix"
    }

    private fun downloadFile(url: String, targetDir: File): File? {
        val fileName = url.substringAfterLast("/")
        val targetFile = File(targetDir, fileName)
        
        val indicator = com.intellij.openapi.progress.ProgressManager.getInstance().progressIndicator
        indicator?.text = "Downloading Blender $url..."
        
        try {
            java.net.URL(url).openStream().use { input ->
                targetFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            return targetFile
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun extractFile(file: File, targetDir: File) {
        val indicator = com.intellij.openapi.progress.ProgressManager.getInstance().progressIndicator
        indicator?.text = "Extracting Blender..."
        
        if (file.name.endsWith(".zip")) {
            val command = if (System.getProperty("os.name").lowercase().contains("win")) {
                listOf("powershell", "Expand-Archive", "-Path", file.absolutePath, "-DestinationPath", targetDir.absolutePath, "-Force")
            } else {
                listOf("unzip", "-o", file.absolutePath, "-d", targetDir.absolutePath)
            }
            try {
                ProcessBuilder(command).start().waitFor()
            } catch (e: Exception) {
                log("Failed to extract ZIP: ${e.message}")
            }
        } else if (file.name.endsWith(".tar.xz")) {
            val command = listOf("tar", "-xf", file.absolutePath, "-C", targetDir.absolutePath)
            try {
                ProcessBuilder(command).start().waitFor()
            } catch (e: Exception) {
                log("Failed to extract TAR.XZ: ${e.message}")
            }
        } else if (file.name.endsWith(".dmg")) {
            if (System.getProperty("os.name").lowercase().contains("mac")) {
                log("Handling macOS DMG mounting and extraction...")
                val mountPoint = File("/tmp/blender_mount_${System.currentTimeMillis()}")
                mountPoint.mkdirs()
                try {
                    // Mount DMG
                    log("Mounting ${file.absolutePath} to ${mountPoint.absolutePath}")
                    ProcessBuilder("hdiutil", "attach", file.absolutePath, "-mountpoint", mountPoint.absolutePath, "-nobrowse", "-readonly").start().waitFor()
                    
                    // Copy Blender.app
                    val appFile = mountPoint.listFiles()?.find { it.name == "Blender.app" }
                    if (appFile != null) {
                        log("Copying Blender.app to ${targetDir.absolutePath}")
                        ProcessBuilder("cp", "-R", appFile.absolutePath, targetDir.absolutePath).start().waitFor()
                    } else {
                        log("Could not find Blender.app in mounted DMG")
                    }
                } catch (e: Exception) {
                    log("Error during DMG handling: ${e.message}")
                    e.printStackTrace()
                } finally {
                    // Detach DMG
                    log("Detaching DMG from ${mountPoint.absolutePath}")
                    ProcessBuilder("hdiutil", "detach", mountPoint.absolutePath).start().waitFor()
                    mountPoint.delete()
                }
            } else {
                log("DMG extraction is only supported on macOS")
            }
        } else {
            log("Unsupported file format for extraction: ${file.name}")
        }
    }

    fun startBlender() {
        // This might be called from old code, but now we need the path.
        // For now, let's just do nothing or handle it if we have a default.
    }

    fun startBlenderProcess(
        blenderPath: String,
        addonSourceDir: String? = null,
        addonSymlinkName: String? = null,
        additionalArgs: String? = null,
        isSandboxed: Boolean = false
    ): OSProcessHandler? {
        if (isRunning.get()) return processHandler

        if (blenderPath.isEmpty() || !File(blenderPath).exists()) {
            return null
        }

        val sourceDir = if (!addonSourceDir.isNullOrEmpty()) File(addonSourceDir) else File(project.basePath ?: return null)
        if (!sourceDir.exists()) return null

        val symlinkName = if (!addonSymlinkName.isNullOrEmpty()) addonSymlinkName else sourceDir.name
        currentExtensionName = symlinkName

        linkExtensionSource(addonSourceDir, addonSymlinkName, isSandboxed)
        val repoDir = getExtensionsRepoDir(isSandboxed)
        log("Starting Blender with repo at: ${repoDir?.absolutePath}")
        val port = startServer()

        val script = createStartupScript(port, repoDir, currentExtensionName)
        log("Startup script created at: ${script.absolutePath}")
        val commandLine = GeneralCommandLine(blenderPath)
            .withParameters("--python", script.absolutePath)
        
        if (isSandboxed) {
            log("Using sandboxed mode")
            val sandboxDir = File(project.basePath, ".blender_sandbox")
            val configDir = File(sandboxDir, "config")
            val scriptsDir = File(sandboxDir, "scripts")
            
            if (!configDir.exists()) configDir.mkdirs()
            if (!scriptsDir.exists()) scriptsDir.mkdirs()
            
            // Create a simple app template to satisfy the requirement
            val templatesDir = File(scriptsDir, "startup/bl_app_templates/blender_extensions_dev")
            if (!templatesDir.exists()) templatesDir.mkdirs()
            val initFile = File(templatesDir, "__init__.py")
            if (!initFile.exists()) {
                initFile.writeText("def register():\n    pass\n\ndef unregister():\n    pass\n")
            }
            
            commandLine.withEnvironment("BLENDER_USER_CONFIG", configDir.absolutePath)
            commandLine.withEnvironment("BLENDER_USER_SCRIPTS", scriptsDir.absolutePath)
            commandLine.addParameters("--app-template", "blender_extensions_dev")
        }
        
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

    private fun getExtensionsRepoDir(isSandboxed: Boolean = false): File? {
        if (isSandboxed) {
            val sandboxDir = File(project.basePath, ".blender_sandbox")
            val extensionsDir = File(sandboxDir, "extensions")
            return File(extensionsDir, "blender_pycharm")
        }
        
        val os = System.getProperty("os.name").lowercase()
        val userHome = System.getProperty("user.home")
        val blenderConfigDir = when {
            os.contains("win") -> File(System.getenv("APPDATA"), "Blender Foundation/Blender")
            os.contains("mac") -> File(userHome, "Library/Application Support/Blender")
            else -> File(userHome, ".config/blender") // Linux
        }

        if (!blenderConfigDir.exists()) return null

        // Find the latest version directory
        val versions = blenderConfigDir.listFiles { file -> file.isDirectory && file.name.all { it.isDigit() || it == '.' } }
        val latestVersion = versions?.maxByOrNull { it.name } ?: return null

        val extensionsDir = File(latestVersion, "extensions")
        return File(extensionsDir, "blender_pycharm")
    }

    private fun linkExtensionSource(addonSourceDir: String?, addonSymlinkName: String?, isSandboxed: Boolean = false) {
        val userRepoDir = getExtensionsRepoDir(isSandboxed) ?: return
        
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
            log("Created symbolic link: ${targetLink.absolutePath} -> ${sourceDir.absolutePath}")
        } catch (e: Exception) {
            log("Failed to create symbolic link: ${e.message}")
            if (System.getProperty("os.name").lowercase().contains("win")) {
                log("Attempting to create a directory junction instead (Windows fallback)...")
                try {
                    // On Windows, junctions don't require special privileges
                    val process = ProcessBuilder("cmd", "/c", "mklink", "/J", targetLink.absolutePath, sourceDir.absolutePath).start()
                    val exitCode = process.waitFor()
                    if (exitCode == 0) {
                        log("Successfully created directory junction.")
                    } else {
                        log("mklink failed with exit code $exitCode")
                    }
                } catch (e2: Exception) {
                    log("Failed to create junction: ${e2.message}")
                }
            }
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
                    if (client != null) {
                        println("Blender connected on port $port")
                        blenderClient = client
                    }
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

    private fun createStartupScript(port: Int, repoDir: File?, extensionName: String?): File {
        val repoPath = repoDir?.absolutePath?.replace("\\", "\\\\") ?: ""
        val extName = extensionName ?: ""
        val scriptContent = """
            import bpy
            import socket
            import threading
            import os
            import traceback

            def ensure_extension_repo_exists(repo_name, repo_path):
                if bpy.app.version < (4, 2, 0):
                    return
                if not repo_path or not os.path.exists(repo_path):
                    return

                repo_path = os.path.normpath(repo_path)
                
                # Check if repo exists
                existing_repo = None
                for repo in bpy.context.preferences.extensions.repos:
                    if getattr(repo, 'module', None) == repo_name:
                        existing_repo = repo
                        break
                
                if existing_repo:
                    try:
                        existing_repo.enabled = True
                    except:
                        pass
                    current_path = getattr(existing_repo, 'directory', getattr(existing_repo, 'path', None))
                    if current_path and os.path.normpath(current_path) == repo_path:
                        print(f"Extension repo '{repo_name}' is already correctly configured.")
                        return
                    
                    print(f"Repo '{repo_name}' points to different path: {current_path}. Re-creating...")
                    try:
                        # Use the API to remove it
                        bpy.context.preferences.extensions.repos.remove(existing_repo)
                    except Exception as e:
                        print(f"Failed to remove existing repo: {e}")
                        # Attempt direct update as fallback, maybe it is NOT read-only in some versions
                        try:
                            if hasattr(existing_repo, 'directory'):
                                existing_repo.directory = repo_path
                                return
                            elif hasattr(existing_repo, 'path'):
                                existing_repo.path = repo_path
                                return
                        except Exception as e2:
                            print(f"Fallback update also failed: {e2}")

                # Add the repo
                try:
                    if hasattr(bpy.ops.preferences, 'extension_repo_add'):
                        # Blender 5.0+
                        bpy.ops.preferences.extension_repo_add(
                            name=repo_name,
                            type='LOCAL',
                            custom_directory=repo_path,
                            use_custom_directory=True
                        )
                        print(f"Added extensions repository (5.0+): {repo_name} -> {repo_path}")
                    elif hasattr(bpy.ops.extensions, 'repo_add'):
                        # Blender 4.2+
                        bpy.ops.extensions.repo_add(
                            name=repo_name,
                            type='LOCAL',
                            directory=repo_path
                        )
                        print(f"Added extensions repository (4.2+): {repo_name} -> {repo_path}")
                    else:
                        new_repo = bpy.context.preferences.extensions.repos.new(name=repo_name, module=repo_name)
                        try:
                            if hasattr(new_repo, 'directory'):
                                new_repo.directory = repo_path
                            elif hasattr(new_repo, 'path'):
                                new_repo.path = repo_path
                            new_repo.enabled = True
                        except Exception as e:
                            print(f"Failed to set path on new repo: {e}")
                        print(f"Created extensions repository (Manual): {repo_name} -> {repo_path}")
                except Exception as e:
                    print(f"Failed to create extensions repository: {e}")
                    traceback.print_exc()

            def listen_for_reload():
                import json
                import sys
                s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
                try:
                    s.connect(('127.0.0.1', $port))
                    print(f"Connected to IntelliJ for extension reloading on port $port")
                    while True:
                        data = s.recv(1024)
                        if not data:
                            break
                        try:
                            message = json.loads(data.decode().strip())
                            if message.get('type') == 'reload':
                                extension_name = message.get('name')
                                print(f"Received reload command for: {extension_name}")
                                
                                def do_reload():
                                    try:
                                        module_name = f"bl_ext.blender_pycharm.{extension_name}"
                                        
                                        # 1. Disable if enabled
                                        if module_name in bpy.context.preferences.addons:
                                            bpy.ops.preferences.addon_disable(module=module_name)
                                        
                                        # 2. Refresh repositories to pick up file changes
                                        if hasattr(bpy.ops.extensions, 'repo_refresh_all'):
                                            bpy.ops.extensions.repo_refresh_all()
                                        
                                        # 3. Purge from sys.modules to force re-import
                                        for m in list(sys.modules.keys()):
                                            if m == module_name or m.startswith(module_name + "."):
                                                del sys.modules[m]
                                        
                                        # 4. Re-enable
                                        bpy.ops.preferences.addon_enable(module=module_name)
                                        print(f"Successfully reloaded extension: {module_name}")
                                        
                                    except Exception as e:
                                        print(f"Error during reload of {extension_name}: {e}")
                                        traceback.print_exc()
                                    return None # Don't repeat the timer
                                
                                # Use timer to run on main thread (much safer in Blender)
                                if hasattr(bpy.app, 'timers'):
                                    bpy.app.timers.register(do_reload)
                                else:
                                    do_reload()
                        except Exception as e:
                            print(f"Error parsing reload message: {e}")
                except Exception as e:
                    print(f"Error in listen_for_reload: {e}")
                finally:
                    s.close()
            
            def ensure_extension_enabled(extension_name):
                if not extension_name:
                    return
                
                import bpy
                module_name = f"bl_ext.blender_pycharm.{extension_name}"
                if module_name not in bpy.context.preferences.addons:
                    print(f"Automatically enabling extension: {module_name}")
                    try:
                        if hasattr(bpy.ops.extensions, 'repo_refresh_all'):
                            bpy.ops.extensions.repo_refresh_all()
                        bpy.ops.preferences.addon_enable(module=module_name)
                    except Exception as e:
                        print(f"Failed to auto-enable {module_name}: {e}")
                return None

            ensure_extension_repo_exists("blender_pycharm", r"$repoPath")
            if hasattr(bpy.app, 'timers'):
                bpy.app.timers.register(lambda: ensure_extension_enabled("$extName"), first_interval=1.0)
            else:
                ensure_extension_enabled("$extName")
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
                val extensionName = currentExtensionName ?: "unknown"
                out.println("""{"type": "reload", "name": "$extensionName"}""")
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
