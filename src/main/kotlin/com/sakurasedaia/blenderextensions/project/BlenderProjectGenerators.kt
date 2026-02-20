package com.sakurasedaia.blenderextensions.project

import com.intellij.facet.ui.ValidationResult
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.projectRoots.SdkType
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.openapi.roots.ModuleRootModificationUtil
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.DirectoryProjectGenerator
import com.intellij.platform.ProjectGeneratorPeer
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBTextField
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.util.ui.FormBuilder
import com.sakurasedaia.blenderextensions.icons.BlenderIcons
import com.intellij.ui.DocumentAdapter
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.JBUI
import javax.swing.event.DocumentEvent
import javax.swing.SwingUtilities
import java.awt.BorderLayout
import com.intellij.openapi.ui.FixedSizeButton
import com.intellij.openapi.ui.TextBrowseFolderListener
import com.intellij.ui.components.JBPanel
import com.intellij.util.ui.UIUtil
import java.nio.file.Files
import java.nio.file.Path
import javax.swing.Icon
import javax.swing.JPanel
import kotlin.io.path.absolutePathString
import kotlin.io.path.exists
import kotlin.io.path.writeText
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.OSProcessHandler

class BlenderAddonProjectGenerator : DirectoryProjectGenerator<BlenderAddonProjectSettings> {
    private var myPeer: BlenderAddonProjectPeer? = null

    override fun getName(): String = "Blender Extension"
    override fun getLogo(): Icon = BlenderIcons.Blender

    override fun createPeer(): ProjectGeneratorPeer<BlenderAddonProjectSettings> {
        val peer = BlenderAddonProjectPeer()
        myPeer = peer
        return peer
    }

    override fun validate(baseDirPath: String): ValidationResult {
        myPeer?.updateLocation(baseDirPath)
        return ValidationResult.OK
    }

    override fun generateProject(project: Project, baseDir: VirtualFile, settings: BlenderAddonProjectSettings, module: Module) {
        val projectPath = Path.of(baseDir.path)
        val chosenName = settings.projectName?.takeIf { it.isNotBlank() } ?: project.name
        val projectName = chosenName
        val authorName = System.getProperty("user.name") ?: "Author"
        // Ensure addonId follows the lowercase-hyphen rule as requested
        val addonId = projectName.lowercase().replace(" ", "-").replace(Regex("[^a-z0-9-]"), "")

        val srcDir = projectPath.resolve("src")
        Files.createDirectories(srcDir)

        srcDir.resolve("blender_manifest.toml").writeText(
            BlenderProjectTemplateGenerator.generateManifest(addonId, projectName, authorName)
        )
        projectPath.resolve("LICENSE").writeText(BlenderProjectTemplateGenerator.generateLicense())
        projectPath.resolve("README.md").writeText(BlenderProjectTemplateGenerator.generateReadme())
        projectPath.resolve(".gitignore").writeText(BlenderProjectTemplateGenerator.generateGitignore())

        if (settings.enableAutoLoad) {
            srcDir.resolve("__init__.py").writeText(
                BlenderProjectTemplateGenerator.generateAutoLoadInit(projectName, authorName)
            )
            srcDir.resolve("auto_load.py").writeText(
                BlenderProjectTemplateGenerator.getAutoLoadContent()
            )
        } else {
            srcDir.resolve("__init__.py").writeText(
                BlenderProjectTemplateGenerator.generateSimpleInit(projectName, authorName)
            )
        }

        // Python setup options similar to "Pure Python" template
        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Setting up Python interpreter", false) {
            override fun run(indicator: ProgressIndicator) {
                indicator.text = "Configuring Python interpreter..."
                indicator.fraction = 0.1

                val pythonSdkType = findPythonSdkType()

                when (settings.interpreterType) {
                    PythonInterpreterType.VENV -> {
                        val basePython = settings.baseInterpreterPath?.takeIf { it.isNotBlank() }
                            ?: findBestBasePython(pythonSdkType)
                        val venvDir = settings.venvLocation?.takeIf { it.isNotBlank() } ?: ".venv"

                        indicator.text = "Creating virtual environment ($venvDir)..."
                        indicator.fraction = 0.3
                        var venvOk = createVenv(projectPath, basePython, venvDir, settings.inheritSitePackages)

                        if (!venvOk) {
                            // Try to download and install Python via PythonSdkType (if available)
                            indicator.text = "Attempting to download Python 3.11..."
                            indicator.fraction = 0.5
                            venvOk = tryDownloadPythonAndCreateVenv(project, projectPath, venvDir, settings.inheritSitePackages)
                        }

                        indicator.text = "Configuring project SDK..."
                        indicator.fraction = 0.8
                        val pythonExe = resolveVenvPython(projectPath, venvDir)
                        if (pythonExe != null && pythonExe.exists()) {
                            setupProjectSdk(project, module, baseDir, pythonExe, pythonSdkType)
                        }
                    }
                    PythonInterpreterType.EXISTING -> {
                        val existingPath = settings.existingInterpreterPath
                        if (!existingPath.isNullOrBlank()) {
                            val exe = Path.of(existingPath)
                            if (exe.exists()) {
                                setupProjectSdk(project, module, baseDir, exe, pythonSdkType)
                            }
                        }
                    }
                }

                indicator.fraction = 1.0
                VfsUtil.markDirtyAndRefresh(false, true, true, baseDir)
            }
        })
    }

    private fun findPythonSdkType(): SdkType? = SdkType.EP_NAME.extensionList.find { it.name == "Python SDK" }

    private fun findBestBasePython(pythonSdkType: SdkType?): String? {
        if (pythonSdkType == null) return null
        val table = ProjectJdkTable.getInstance()
        val sdks = table.getSdksOfType(pythonSdkType)
            .filter { it.homePath != null && !it.homePath!!.contains("venv") && !it.homePath!!.contains(".venv") }

        // 1. Try to find a registered 3.11 SDK specifically (preferred for Blender 5.0)
        sdks.find { it.versionString?.contains("3.11") == true }?.homePath?.let { return it }

        // 2. Try any registered 3.x SDK (prefer higher version)
        sdks.sortedByDescending { it.versionString ?: "" }
            .find { it.versionString?.contains("Python 3") == true }?.homePath?.let { return it }

        // 3. Fallback to suggested homes from Python plugin
        val suggested = tryGetSuggestedPaths()
        // Prefer 3.11 in suggested paths
        suggested.find { it.contains("3.11") || it.contains("311") }?.let { return it }
        // Otherwise any suggested 3.x (usually suggested are only 3.x anyway)
        return suggested.firstOrNull()
    }

    private fun tryGetSuggestedPaths(): List<String> {
        return try {
            val pythonSdkTypeClass = Class.forName("com.jetbrains.python.sdk.PythonSdkType")
            val getInstance = pythonSdkTypeClass.getMethod("getInstance")
            val instance = getInstance.invoke(null)
            val suggest = pythonSdkTypeClass.getMethod("suggestHomePaths", java.lang.Boolean.TYPE)
            @Suppress("UNCHECKED_CAST")
            val homes = suggest.invoke(instance, false) as? Collection<String>
            homes?.toList() ?: emptyList()
        } catch (_: Throwable) {
            emptyList()
        }
    }

    private fun createVenv(projectPath: Path, basePython: String?, venvDirName: String, inheritSitePackages: Boolean): Boolean {
        val candidates = mutableListOf<List<String>>()
        val target = venvDirName
        val inheritFlag = if (inheritSitePackages) listOf("--system-site-packages") else emptyList()
        
        if (!basePython.isNullOrBlank()) {
            candidates.add(listOf(basePython, "-m", "venv") + inheritFlag + listOf(target))
        } else {
            // If no base python provided, try system-wide standard commands
            // Windows py launcher is a good indicator of standard installation
            candidates.add(listOf("py", "-3.11", "-m", "venv") + inheritFlag + listOf(target))
            candidates.add(listOf("py", "-3", "-m", "venv") + inheritFlag + listOf(target))
            // Linux/macOS standard commands
            candidates.add(listOf("python3.11", "-m", "venv") + inheritFlag + listOf(target))
            candidates.add(listOf("python3", "-m", "venv") + inheritFlag + listOf(target))
            candidates.add(listOf("python", "-m", "venv") + inheritFlag + listOf(target))
        }

        for (cmd in candidates) {
            if (execute(projectPath, cmd)) return true
        }
        return false
    }

    private fun tryDownloadPythonAndCreateVenv(project: Project, projectPath: Path, venvDirName: String, inheritSitePackages: Boolean): Boolean {
        // Best-effort: use reflection to call PythonSdkType download installer if available in this PyCharm build
        return try {
            val cls = Class.forName("com.jetbrains.python.sdk.PythonSdkDownloader")
            val method = cls.methods.firstOrNull { it.name == "download" }
            val result = if (method != null) method.invoke(null, project, "3.11") else null
            // Assume download returns home path String or Sdk; try resolve path
            val homePath = when (result) {
                is String -> result
                is Sdk -> result.homePath
                else -> null
            }
            if (!homePath.isNullOrBlank()) createVenv(projectPath, homePath, venvDirName, inheritSitePackages) else false
        } catch (_: Throwable) {
            false
        }
    }

    private fun resolveVenvPython(projectPath: Path, venvDirName: String): Path? {
        val win = System.getProperty("os.name").lowercase().contains("win")
        val candidate = if (win) projectPath.resolve(venvDirName).resolve("Scripts").resolve("python.exe")
        else projectPath.resolve(venvDirName).resolve("bin").resolve("python")
        return if (candidate.exists()) candidate else null
    }

    private fun setupProjectSdk(project: Project, module: Module, baseDir: VirtualFile, pythonExe: Path, pythonSdkType: SdkType?) {
        ApplicationManager.getApplication().invokeAndWait {
            ApplicationManager.getApplication().runWriteAction {
                val sdkType = pythonSdkType ?: return@runWriteAction
                val table = ProjectJdkTable.getInstance()
                val existing = table.allJdks.find { it.sdkType == sdkType && it.homePath == pythonExe.absolutePathString() }
                val sdk = existing ?: com.intellij.openapi.projectRoots.impl.SdkConfigurationUtil.createAndAddSDK(pythonExe.absolutePathString(), sdkType)
                if (sdk != null) {
                    ModuleRootModificationUtil.setModuleSdk(module, sdk)
                    ProjectRootManager.getInstance(project).projectSdk = sdk
                }
                // Exclude .venv from content roots to mimic Pure Python behavior
                ModuleRootModificationUtil.updateModel(module) { model ->
                    val contentEntry = model.contentEntries.find { it.file == baseDir } ?: model.addContentEntry(baseDir)
                    val venv = baseDir.toNioPath().resolve(".venv")
                    val venvVf = LocalFileSystem.getInstance().refreshAndFindFileByNioFile(venv)
                    if (venvVf != null && contentEntry.excludeFolders.none { it.file == venvVf }) {
                        contentEntry.addExcludeFolder(venvVf)
                    }
                }
            }
        }
    }

    private fun execute(workingDir: Path, command: List<String>): Boolean = try {
        val handler = OSProcessHandler(GeneralCommandLine(command).withWorkDirectory(workingDir.absolutePathString()))
        handler.startNotify()
        handler.waitFor()
        handler.exitCode == 0
    } catch (_: Throwable) {
        false
    }
}

enum class PythonInterpreterType { VENV, EXISTING }

data class BlenderAddonProjectSettings(
    var enableAutoLoad: Boolean = false,
    var projectName: String? = null,
    var interpreterType: PythonInterpreterType = PythonInterpreterType.VENV,
    var baseInterpreterPath: String? = null,
    var venvLocation: String? = ".venv",
    var inheritSitePackages: Boolean = false,
    var existingInterpreterPath: String? = null
)

private class BlenderAddonProjectPeer : ProjectGeneratorPeer<BlenderAddonProjectSettings> {
    private var projectLocation: String? = null
    private var settingsStep: com.intellij.ide.util.projectWizard.SettingsStep? = null
    private var isUpdating = false

    fun updateLocation(path: String) {
        if (projectLocation == path) return
        projectLocation = path

        if (!isUpdating) {
            val nameFromPath = try { Path.of(path).fileName?.toString() } catch (_: Exception) { null }
            if (!nameFromPath.isNullOrEmpty() && projectNameField.text != nameFromPath) {
                isUpdating = true
                projectNameField.text = nameFromPath
                isUpdating = false
            }
        }

        updateVisibility()
    }

    private val autoLoadCheckbox = JBCheckBox("Enable auto-load (adds auto_load.py and autoload __init__.py)", false)

    // Name field with auto-formatting logic
    private val projectNameField = JBTextField()

    // Python options mirroring Pure Python design
    private val interpreterTypeCombo = ComboBox(arrayOf("Project venv", "Custom environment"))
    private val baseInterpreterCombo = ComboBox<String>()
    private val baseInterpreterBrowse = FixedSizeButton()
    private val venvLocationField = JBTextField(".venv")
    private val inheritSitePkgsCheck = JBCheckBox("Inherit global site-packages", false)
    private val existingInterpreterField = TextFieldWithBrowseButton()
    private val hintLabel = JBLabel().apply {
        font = UIUtil.getLabelFont(UIUtil.FontSize.SMALL)
        foreground = UIUtil.getContextHelpForeground()
    }

    private val venvOptionsPanel: JPanel
    private val existingOptionsPanel: JPanel
    private val panel: JPanel

    init {
        // Hyphenation logic for the Name field (allows capitals)
        projectNameField.document.addDocumentListener(object : DocumentAdapter() {
            override fun textChanged(e: DocumentEvent) {
                if (isUpdating) return
                val original = projectNameField.text
                // Replace spaces with hyphens and remove non-alphanumeric (except hyphens)
                val formatted = original.replace(" ", "-").replace(Regex("[^a-zA-Z0-9-]"), "")

                if (original != formatted) {
                    isUpdating = true
                    SwingUtilities.invokeLater {
                        try {
                            val caret = projectNameField.caretPosition
                            projectNameField.text = formatted
                            try {
                                projectNameField.caretPosition = Math.min(caret, formatted.length)
                            } catch (_: Exception) {}
                            updateLocationFromProjectName()
                        } finally {
                            isUpdating = false
                        }
                    }
                } else {
                    updateLocationFromProjectName()
                }
            }
        })

        // Populate base interpreter combo
        val pythons = getSuggestedPythons()
        pythons.forEach { baseInterpreterCombo.addItem(it) }
        if (pythons.isNotEmpty()) {
            baseInterpreterCombo.selectedIndex = 0
        }
        baseInterpreterCombo.isEditable = true

        baseInterpreterBrowse.addActionListener {
            val descriptor = FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor()
            val file = com.intellij.openapi.fileChooser.FileChooser.chooseFile(descriptor, null, null)
            if (file != null) {
                baseInterpreterCombo.setSelectedItem(file.path)
            }
        }

        existingInterpreterField.addBrowseFolderListener(
            TextBrowseFolderListener(FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor())
        )

        val baseInterpPanel = JBPanel<JBPanel<*>>(BorderLayout(5, 0)).apply {
            add(baseInterpreterCombo, BorderLayout.CENTER)
            add(baseInterpreterBrowse, BorderLayout.EAST)
        }

        venvOptionsPanel = FormBuilder.createFormBuilder()
            .addLabeledComponent("Python version:", baseInterpPanel)
            .addLabeledComponent("Venv location:", venvLocationField)
            .addComponent(inheritSitePkgsCheck)
            .addComponent(hintLabel)
            .panel

        existingOptionsPanel = FormBuilder.createFormBuilder()
            .addLabeledComponent("Existing interpreter:", existingInterpreterField)
            .panel

        interpreterTypeCombo.addActionListener { updateVisibility() }
        venvLocationField.document.addDocumentListener(object : DocumentAdapter() {
            override fun textChanged(e: DocumentEvent) {
                updateVisibility()
            }
        })

        panel = FormBuilder.createFormBuilder()
            .addLabeledComponent("Project name:", projectNameField)
            .addComponent(autoLoadCheckbox)
            .addVerticalGap(10)
            .addLabeledComponent("Interpreter type:", interpreterTypeCombo)
            .addComponent(venvOptionsPanel)
            .addComponent(existingOptionsPanel)
            .addComponentFillVertically(JPanel(), 0)
            .panel

        updateVisibility()
    }

    private fun updateLocationFromProjectName() {
        val name = projectNameField.text.trim()
        if (name.isEmpty() || projectLocation == null) return

        val path = try { Path.of(projectLocation!!) } catch (_: Exception) { return }
        val parent = path.parent ?: return
        val newPath = parent.resolve(name).toAbsolutePath().toString()

        if (newPath != projectLocation) {
            val locField = findLocationField()
            if (locField != null && locField.text != newPath) {
                locField.text = newPath
            }
        }
    }

    private fun findLocationField(): TextFieldWithBrowseButton? {
        var current: java.awt.Component? = panel
        while (current != null) {
            val parent = current.parent
            if (parent is javax.swing.JComponent) {
                val fields = UIUtil.findComponentsOfType(parent, TextFieldWithBrowseButton::class.java)
                for (field in fields) {
                    if (!SwingUtilities.isDescendingFrom(field, panel)) {
                        return field
                    }
                }
            }
            current = parent
        }
        return null
    }

    private fun updateVisibility() {
        val isVenv = interpreterTypeCombo.selectedIndex == 0
        venvOptionsPanel.isVisible = isVenv
        existingOptionsPanel.isVisible = !isVenv

        if (isVenv) {
            val base = projectLocation ?: "."
            val venvRelPath = venvLocationField.text.trim().takeIf { it.isNotEmpty() } ?: ".venv"
            val venvPath = try {
                Path.of(base).resolve(venvRelPath).toAbsolutePath().toString()
            } catch (_: Exception) {
                "$base/$venvRelPath"
            }
            hintLabel.text = "Python virtual environment will be created in: $venvPath"
        }
    }

    private fun getSuggestedPythons(): List<String> {
        val sdkTypes = SdkType.EP_NAME.extensionList
        val pythonSdkType = sdkTypes.find { it.name == "Python SDK" || it.name == "PythonSDK" }
            ?: sdkTypes.find { it.javaClass.simpleName.contains("Python", ignoreCase = true) }
            ?: return emptyList()

        val table = ProjectJdkTable.getInstance()
        val sdks = table.getSdksOfType(pythonSdkType)
        val homes = sdks
            .filter { it.homePath != null && !it.homePath!!.contains("venv") && !it.homePath!!.contains(".venv") }
            .mapNotNull { it.homePath }
            .toMutableSet()

        homes.addAll(tryGetSuggestedPaths())

        if (homes.isEmpty()) {
            val os = System.getProperty("os.name").lowercase()
            if (os.contains("win")) {
                listOf("C:\\Python311\\python.exe", "C:\\Python312\\python.exe", "C:\\Program Files\\Python311\\python.exe")
                    .filter { Path.of(it).exists() }
                    .forEach { homes.add(it) }
            } else {
                listOf("/usr/bin/python3.11", "/usr/bin/python3.12", "/usr/bin/python3", "/usr/local/bin/python3")
                    .filter { Path.of(it).exists() }
                    .forEach { homes.add(it) }
            }
        }

        // Prioritize 3.11 in the sorted list if possible
        return homes.toList().sortedWith(compareByDescending<String> { it.contains("3.11") || it.contains("311") }
            .thenByDescending { it })
    }

    private fun tryGetSuggestedPaths(): List<String> {
        return try {
            val pythonSdkTypeClass = Class.forName("com.jetbrains.python.sdk.PythonSdkType")
            val getInstance = pythonSdkTypeClass.getMethod("getInstance")
            val instance = getInstance.invoke(null)
            val suggest = pythonSdkTypeClass.getMethod("suggestHomePaths", java.lang.Boolean.TYPE)
            @Suppress("UNCHECKED_CAST")
            val homes = suggest.invoke(instance, false) as? Collection<String>
            homes?.toList() ?: emptyList()
        } catch (_: Throwable) {
            emptyList()
        }
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun getComponent(): javax.swing.JComponent = panel

    @Suppress("OVERRIDE_DEPRECATION")
    override fun buildUI(settingsStep: com.intellij.ide.util.projectWizard.SettingsStep) {
        this.settingsStep = settingsStep
        settingsStep.addSettingsComponent(component)
    }

    override fun getSettings(): BlenderAddonProjectSettings = BlenderAddonProjectSettings(
        enableAutoLoad = autoLoadCheckbox.isSelected,
        projectName = projectNameField.text?.trim(),
        interpreterType = if (interpreterTypeCombo.selectedIndex == 1) PythonInterpreterType.EXISTING else PythonInterpreterType.VENV,
        baseInterpreterPath = (baseInterpreterCombo.selectedItem as? String)?.trim().takeIf { !it.isNullOrBlank() },
        venvLocation = venvLocationField.text?.trim().takeIf { !it.isNullOrBlank() } ?: ".venv",
        inheritSitePackages = inheritSitePkgsCheck.isSelected,
        existingInterpreterPath = existingInterpreterField.text?.trim().takeIf { !it.isNullOrBlank() }
    )

    override fun validate(): ValidationInfo? {
        val name = projectNameField.text?.trim() ?: ""
        // Name is essentially optional here if they use the Location field, 
        // but we formatted it for them if they used it.
        if (interpreterTypeCombo.selectedIndex == 1 && existingInterpreterField.text.isBlank()) {
            return ValidationInfo("Please select an existing interpreter or choose Virtualenv")
        }
        return null
    }

    override fun isBackgroundJobRunning(): Boolean = false

    @Suppress("OVERRIDE_DEPRECATION", "DEPRECATION")
    override fun addSettingsStateListener(listener: com.intellij.platform.WebProjectGenerator.SettingsStateListener) {}
}
