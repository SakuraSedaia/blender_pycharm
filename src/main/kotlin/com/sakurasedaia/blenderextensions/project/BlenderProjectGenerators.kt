package com.sakurasedaia.blenderextensions.project

import com.intellij.facet.ui.ValidationResult
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.DirectoryProjectGenerator
import com.intellij.platform.ProjectGeneratorPeer
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBTextField
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.util.ui.FormBuilder
import com.sakurasedaia.blenderextensions.icons.BlenderIcons
import com.intellij.ui.DocumentAdapter
import com.intellij.util.ui.UIUtil
import javax.swing.event.DocumentEvent
import javax.swing.SwingUtilities
import com.intellij.execution.RunManager
import com.intellij.execution.configurations.ConfigurationTypeUtil
import com.sakurasedaia.blenderextensions.run.*
import java.nio.file.Files
import java.nio.file.Path
import javax.swing.Icon
import javax.swing.JPanel
import java.lang.ref.WeakReference
import kotlin.io.path.writeText

private fun formatToId(name: String, allowCapitals: Boolean = false): String {
    val replaced = name.replace(" ", "-")
    return if (allowCapitals) {
        replaced.replace(Regex("[^a-zA-Z0-9-]"), "")
    } else {
        replaced.lowercase().replace(Regex("[^a-z0-9-]"), "")
    }
}

class BlenderAddonProjectGenerator : DirectoryProjectGenerator<BlenderAddonProjectSettings> {
    private var myPeerReference = WeakReference<BlenderAddonProjectPeer>(null)

    override fun getName(): String = "Blender Extension"
    override fun getLogo(): Icon = BlenderIcons.Blender

    override fun createPeer(): ProjectGeneratorPeer<BlenderAddonProjectSettings> {
        val peer = BlenderAddonProjectPeer()
        myPeerReference = WeakReference(peer)
        return peer
    }

    override fun validate(baseDirPath: String): ValidationResult {
        myPeerReference.get()?.updateLocation(baseDirPath)
        return ValidationResult.OK
    }

    override fun generateProject(project: Project, baseDir: VirtualFile, settings: BlenderAddonProjectSettings, module: Module) {
        val projectPath = Path.of(baseDir.path)
        val projectName = settings.projectName?.takeIf { it.isNotBlank() } ?: project.name
        val authorName = settings.addonMaintainer ?: System.getProperty("user.name") ?: "Author"
        val addonId = settings.addonId?.takeIf { it.isNotBlank() } ?: formatToId(projectName)

        val srcDir = projectPath.resolve("src")
        Files.createDirectories(srcDir)

        val permissionsMap = mutableMapOf<String, String>()
        if (settings.permissionNetwork && !settings.permissionNetworkReason.isNullOrBlank()) permissionsMap["network"] = settings.permissionNetworkReason!!
        if (settings.permissionFiles && !settings.permissionFilesReason.isNullOrBlank()) permissionsMap["files"] = settings.permissionFilesReason!!
        if (settings.permissionClipboard && !settings.permissionClipboardReason.isNullOrBlank()) permissionsMap["clipboard"] = settings.permissionClipboardReason!!
        if (settings.permissionCamera && !settings.permissionCameraReason.isNullOrBlank()) permissionsMap["camera"] = settings.permissionCameraReason!!
        if (settings.permissionMicrophone && !settings.permissionMicrophoneReason.isNullOrBlank()) permissionsMap["microphone"] = settings.permissionMicrophoneReason!!

        val manifestSettings = BlenderManifestSettings(
            id = addonId,
            name = projectName,
            tagline = settings.addonTagline ?: "A Blender extension",
            maintainer = authorName,
            website = settings.addonWebsite,
            tags = settings.addonTags?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() },
            blenderVersionMin = settings.blenderVersionMin ?: "4.2.0",
            blenderVersionMax = settings.blenderVersionMax,
            platforms = settings.addonPlatforms?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() },
            permissions = if (permissionsMap.isNotEmpty()) permissionsMap else null,
            buildPathsExcludePattern = settings.buildPathsExcludePattern?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() }
        )

        srcDir.resolve("blender_manifest.toml").writeText(
            BlenderProjectTemplateGenerator.generateManifest(manifestSettings)
        )
        projectPath.resolve("LICENSE").writeText(BlenderProjectTemplateGenerator.generateLicense())
        projectPath.resolve("README.md").writeText(BlenderProjectTemplateGenerator.generateReadme())
        projectPath.resolve(".gitignore").writeText(BlenderProjectTemplateGenerator.generateGitignore())

        if (settings.agentGuidelines) {
            projectPath.resolve(".agent-guidelines.md").writeText(
                BlenderProjectTemplateGenerator.generateAgentGuidelines()
            )
        }

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

        // Automatically create Blender Run Configurations: Start Blender, Build, and Validate
        val runManager = RunManager.getInstance(project)
        val configType = ConfigurationTypeUtil.findConfigurationType(BlenderRunConfigurationType::class.java)
        
        // 1. Start Blender
        val startBlenderFactory = configType.configurationFactories.find { it is BlenderStartBlenderConfigurationFactory }
        if (startBlenderFactory != null) {
            val runSettings = runManager.createConfiguration("Start Blender", startBlenderFactory)
            val runConfig = runSettings.configuration as BlenderRunConfiguration
            val options = runConfig.getOptions()
            options.blenderVersion = "5.0"
            options.isSandboxed = true
            options.addonSourceDirectory = srcDir.toAbsolutePath().toString()
            options.addonSymlinkName = addonId
            runManager.addConfiguration(runSettings)
            runManager.selectedConfiguration = runSettings
        }

        // 2. Build
        val buildFactory = configType.configurationFactories.find { it is BlenderBuildConfigurationFactory }
        if (buildFactory != null) {
            val runSettings = runManager.createConfiguration("Build", buildFactory)
            val runConfig = runSettings.configuration as BlenderRunConfiguration
            runConfig.getOptions().blenderVersion = "5.0"
            runManager.addConfiguration(runSettings)
        }

        // 3. Validate
        val validateFactory = configType.configurationFactories.find { it is BlenderValidateConfigurationFactory }
        if (validateFactory != null) {
            val runSettings = runManager.createConfiguration("Validate", validateFactory)
            val runConfig = runSettings.configuration as BlenderRunConfiguration
            runConfig.getOptions().blenderVersion = "5.0"
            runManager.addConfiguration(runSettings)
        }

        if (settings.createGitRepo) {
            try {
                com.intellij.execution.configurations.GeneralCommandLine("git", "init")
                    .withWorkDirectory(projectPath.toFile())
                    .createProcess()
                    .waitFor()
            } catch (_: Exception) {}
        }
    }
}

data class BlenderAddonProjectSettings(
    var enableAutoLoad: Boolean = false,
    var projectName: String? = null,
    var addonId: String? = null,
    var addonTagline: String? = "A Blender extension",
    var addonMaintainer: String? = System.getProperty("user.name") ?: "Author",
    var addonWebsite: String? = null,
    var addonTags: String? = null,
    var blenderVersionMin: String? = "4.2.0",
    var blenderVersionMax: String? = null,
    var addonPlatforms: String? = null,
    var permissionNetwork: Boolean = false,
    var permissionNetworkReason: String? = null,
    var permissionFiles: Boolean = false,
    var permissionFilesReason: String? = null,
    var permissionClipboard: Boolean = false,
    var permissionClipboardReason: String? = null,
    var permissionCamera: Boolean = false,
    var permissionCameraReason: String? = null,
    var permissionMicrophone: Boolean = false,
    var permissionMicrophoneReason: String? = null,
    var buildPathsExcludePattern: String? = null,
    var createGitRepo: Boolean = false,
    val agentGuidelines: Boolean
)

private class BlenderAddonProjectPeer : ProjectGeneratorPeer<BlenderAddonProjectSettings> {
    private var projectLocation: String? = null
    private var isUpdating = false
    private var addonIdIsManual = false
    private val stateListeners = mutableListOf<com.intellij.platform.WebProjectGenerator.SettingsStateListener>()

    private fun fireStateChanged() {
        if (!isUpdating) {
            stateListeners.forEach { it.stateChanged(true) }
        }
    }

    fun updateLocation(path: String) {
        if (projectLocation == path) return
        projectLocation = path

        if (!isUpdating) {
            val nameFromPath = try { Path.of(path).fileName?.toString() } catch (_: Exception) { null }
            if (!nameFromPath.isNullOrEmpty() && projectNameField.text != nameFromPath) {
                isUpdating = true
                try {
                    projectNameField.text = nameFromPath
                    if (!addonIdIsManual) {
                        addonIdField.text = formatToId(nameFromPath, allowCapitals = false)
                    }
                } finally {
                    isUpdating = false
                }
                fireStateChanged()
            }
        }
    }

    private val autoLoadCheckbox = JBCheckBox("Add automatic module/class registration script", false)
    private val includeAgentGuidelines = JBCheckBox("Include Preset guidelines", false)
    private val createGitRepoCheckbox = JBCheckBox("Create Git repository", false)
    private val projectNameField = JBTextField()
    private val addonIdField = JBTextField()
    private val addonTaglineField = JBTextField("A Blender extension")
    private val addonMaintainerField = JBTextField(System.getProperty("user.name") ?: "Author")
    private val addonWebsiteField = JBTextField()
    private val addonTagsField = JBTextField()
    private val blenderVersionMinField = JBTextField("4.2.0")
    private val blenderVersionMaxField = JBTextField()
    private val addonPlatformsField = JBTextField()

    private val permissionNetworkCheckbox = JBCheckBox("Network access", false)
    private val permissionNetworkReasonField = JBTextField()
    private val permissionFilesCheckbox = JBCheckBox("Filesystem access", false)
    private val permissionFilesReasonField = JBTextField()
    private val permissionClipboardCheckbox = JBCheckBox("Clipboard access", false)
    private val permissionClipboardReasonField = JBTextField()
    private val permissionCameraCheckbox = JBCheckBox("Camera access", false)
    private val permissionCameraReasonField = JBTextField()
    private val permissionMicrophoneCheckbox = JBCheckBox("Microphone access", false)
    private val permissionMicrophoneReasonField = JBTextField()

    private val buildPathsExcludePatternField = JBTextField()

    private val panel: JPanel

    init {
        projectNameField.document.addDocumentListener(object : DocumentAdapter() {
            override fun textChanged(e: DocumentEvent) {
                if (isUpdating) return
                val original = projectNameField.text
                val formatted = formatToId(original, allowCapitals = true)

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
                            if (!addonIdIsManual) {
                                addonIdField.text = formatToId(formatted, allowCapitals = false)
                            }
                        } finally {
                            isUpdating = false
                        }
                        fireStateChanged()
                    }
                } else {
                    isUpdating = true
                    try {
                        updateLocationFromProjectName()
                        if (!addonIdIsManual) {
                            addonIdField.text = formatToId(formatted, allowCapitals = false)
                        }
                    } finally {
                        isUpdating = false
                    }
                    fireStateChanged()
                }
            }
        })

        addonIdField.document.addDocumentListener(object : DocumentAdapter() {
            override fun textChanged(e: DocumentEvent) {
                if (isUpdating) return
                
                val original = addonIdField.text
                val formatted = formatToId(original, allowCapitals = false)

                if (original != formatted) {
                    isUpdating = true
                    SwingUtilities.invokeLater {
                        try {
                            val caret = addonIdField.caretPosition
                            addonIdField.text = formatted
                            try {
                                addonIdField.caretPosition = Math.min(caret, formatted.length)
                            } catch (_: Exception) {}
                            addonIdIsManual = formatted.isNotBlank()
                        } finally {
                            isUpdating = false
                        }
                        fireStateChanged()
                    }
                } else {
                    addonIdIsManual = original.isNotBlank()
                    fireStateChanged()
                }
            }
        })

        // Reason fields should be disabled if checkbox is not selected
        permissionNetworkReasonField.isEnabled = false
        permissionNetworkCheckbox.addActionListener {
            permissionNetworkReasonField.isEnabled = permissionNetworkCheckbox.isSelected
            fireStateChanged()
        }
        permissionFilesReasonField.isEnabled = false
        permissionFilesCheckbox.addActionListener {
            permissionFilesReasonField.isEnabled = permissionFilesCheckbox.isSelected
            fireStateChanged()
        }
        permissionClipboardReasonField.isEnabled = false
        permissionClipboardCheckbox.addActionListener {
            permissionClipboardReasonField.isEnabled = permissionClipboardCheckbox.isSelected
            fireStateChanged()
        }
        permissionCameraReasonField.isEnabled = false
        permissionCameraCheckbox.addActionListener {
            permissionCameraReasonField.isEnabled = permissionCameraCheckbox.isSelected
            fireStateChanged()
        }
        permissionMicrophoneReasonField.isEnabled = false
        permissionMicrophoneCheckbox.addActionListener {
            permissionMicrophoneReasonField.isEnabled = permissionMicrophoneCheckbox.isSelected
            fireStateChanged()
        }

        autoLoadCheckbox.addActionListener { fireStateChanged() }
        includeAgentGuidelines.addActionListener { fireStateChanged() }
        createGitRepoCheckbox.addActionListener { fireStateChanged() }

        // Add listeners to other fields to trigger validation
        listOf(
            addonTaglineField,
            addonMaintainerField,
            addonWebsiteField,
            addonTagsField,
            blenderVersionMinField,
            blenderVersionMaxField,
            addonPlatformsField,
            buildPathsExcludePatternField
        ).forEach { field ->
            field.document.addDocumentListener(object : DocumentAdapter() {
                override fun textChanged(e: DocumentEvent) = fireStateChanged()
            })
        }

        // Enforce 64-char max for permission reasons
        fun enforceMax64(tf: JBTextField) {
            tf.document.addDocumentListener(object : DocumentAdapter() {
                override fun textChanged(e: DocumentEvent) {
                    val t = tf.text
                    if (t.length > 64) {
                        val caret = tf.caretPosition
                        tf.text = t.substring(0, 64)
                        try { tf.caretPosition = minOf(caret, 64) } catch (_: Exception) {}
                    }
                    fireStateChanged()
                }
            })
        }
        listOf(
            permissionNetworkReasonField,
            permissionFilesReasonField,
            permissionClipboardReasonField,
            permissionCameraReasonField,
            permissionMicrophoneReasonField
        ).forEach { enforceMax64(it) }

        panel = FormBuilder.createFormBuilder()
            .addComponent(autoLoadCheckbox)
            .addComponent(includeAgentGuidelines)
            .addComponent(createGitRepoCheckbox)
            .addSeparator()
            .addLabeledComponent("Project name:", projectNameField)
            .addLabeledComponent("Addon ID:", addonIdField)
            .addLabeledComponent("Tagline:", addonTaglineField)
            .addLabeledComponent("Maintainer:", addonMaintainerField)
            .addLabeledComponent("Website (Optional):", addonWebsiteField)
            .addLabeledComponent("Tags (comma separated, Optional):", addonTagsField)
            .addLabeledComponent("Min Blender Version:", blenderVersionMinField)
            .addLabeledComponent("Max Blender Version (Optional):", blenderVersionMaxField)
            .addLabeledComponent("Platforms (comma separated, Optional):", addonPlatformsField)
            .addSeparator()
            .addLabeledComponent("Permissions (Optional):", JPanel()) // Placeholder for header
            .addComponent(permissionNetworkCheckbox)
            .addLabeledComponent("  Reason (required if checked, max 64 chars):", permissionNetworkReasonField)
            .addComponent(permissionFilesCheckbox)
            .addLabeledComponent("  Reason (required if checked, max 64 chars):", permissionFilesReasonField)
            .addComponent(permissionClipboardCheckbox)
            .addLabeledComponent("  Reason (required if checked, max 64 chars):", permissionClipboardReasonField)
            .addComponent(permissionCameraCheckbox)
            .addLabeledComponent("  Reason (required if checked, max 64 chars):", permissionCameraReasonField)
            .addComponent(permissionMicrophoneCheckbox)
            .addLabeledComponent("  Reason (required if checked, max 64 chars):", permissionMicrophoneReasonField)
            .addSeparator()
            .addLabeledComponent("Build Exclude Patterns (Optional):", buildPathsExcludePatternField)
            .addComponentFillVertically(JPanel(), 0)
            .panel

        // Tooltips/Hints
        addonIdField.toolTipText = "Kebab-case, alphanumeric, 3-32 characters"
        addonPlatformsField.toolTipText = "windows-x64, macos-arm64, linux-x64, windows-arm64, macos-x64"
        blenderVersionMinField.toolTipText = "e.g. 4.2.0"
    }

    private fun updateLocationFromProjectName() {
        val name = projectNameField.text.trim()
        if (name.isEmpty() || projectLocation == null) return

        val path = try { Path.of(projectLocation!!) } catch (_: Exception) { return }
        val parent = path.parent ?: return
        val newPath = parent.resolve(name).toAbsolutePath().toString()

        if (newPath != projectLocation) {
            findLocationField()?.let {
                if (it.text != newPath) it.text = newPath
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

    @Suppress("OVERRIDE_DEPRECATION")
    override fun getComponent(): javax.swing.JComponent = panel

    @Suppress("OVERRIDE_DEPRECATION")
    override fun buildUI(settingsStep: com.intellij.ide.util.projectWizard.SettingsStep) {
        settingsStep.addSettingsComponent(component)
    }

    override fun getSettings(): BlenderAddonProjectSettings = BlenderAddonProjectSettings(
        enableAutoLoad = autoLoadCheckbox.isSelected,
        agentGuidelines = includeAgentGuidelines.isSelected,
        projectName = projectNameField.text?.trim(),
        addonId = addonIdField.text?.trim(),
        addonTagline = addonTaglineField.text?.trim(),
        addonMaintainer = addonMaintainerField.text?.trim(),
        addonWebsite = addonWebsiteField.text?.trim(),
        addonTags = addonTagsField.text?.trim(),
        blenderVersionMin = blenderVersionMinField.text?.trim(),
        blenderVersionMax = blenderVersionMaxField.text?.trim(),
        addonPlatforms = addonPlatformsField.text?.trim(),
        permissionNetwork = permissionNetworkCheckbox.isSelected,
        permissionNetworkReason = permissionNetworkReasonField.text?.trim(),
        permissionFiles = permissionFilesCheckbox.isSelected,
        permissionFilesReason = permissionFilesReasonField.text?.trim(),
        permissionClipboard = permissionClipboardCheckbox.isSelected,
        permissionClipboardReason = permissionClipboardReasonField.text?.trim(),
        permissionCamera = permissionCameraCheckbox.isSelected,
        permissionCameraReason = permissionCameraReasonField.text?.trim(),
        permissionMicrophone = permissionMicrophoneCheckbox.isSelected,
        permissionMicrophoneReason = permissionMicrophoneReasonField.text?.trim(),
        buildPathsExcludePattern = buildPathsExcludePatternField.text?.trim(),
        createGitRepo = createGitRepoCheckbox.isSelected
    )

    override fun validate(): ValidationInfo? {
        // Validate Addon ID
        val id = addonIdField.text?.trim().orEmpty()
        val idRegex = Regex("^[a-z0-9-]{3,32}$")
        if (id.isEmpty() || !idRegex.matches(id)) {
            return ValidationInfo("Addon ID must be kebab-case (lowercase letters, numbers, hyphens), 3–32 chars.", addonIdField)
        }
        // Validate Blender min version
        val minVer = blenderVersionMinField.text?.trim().orEmpty()
        val semver = Regex("^\\d+\\.\\d+\\.\\d+")
        if (!semver.containsMatchIn(minVer)) {
            return ValidationInfo("Minimum Blender version must look like 4.2.0", blenderVersionMinField)
        }
        // Validate Website URL if provided
        val website = addonWebsiteField.text?.trim().orEmpty()
        if (website.isNotEmpty()) {
            val urlRegex = Regex("^(https?://)?[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}(/.*)?$")
            if (!urlRegex.matches(website)) {
                return ValidationInfo("Please enter a valid URL (e.g. https://example.com)", addonWebsiteField)
            }
        }
        // Validate permissions reasons when enabled
        fun checkReason(checkbox: JBCheckBox, field: JBTextField, label: String): ValidationInfo? {
            if (checkbox.isSelected) {
                val reason = field.text?.trim().orEmpty()
                if (reason.isEmpty()) return ValidationInfo("Provide a reason for $label", field)
                if (reason.length > 64) return ValidationInfo("Reason must be <= 64 characters", field)
                if (reason.endsWith('.')) return ValidationInfo("Reason should not end with a period (.)", field)
            }
            return null
        }
        checkReason(permissionNetworkCheckbox, permissionNetworkReasonField, "Network")?.let { return it }
        checkReason(permissionFilesCheckbox, permissionFilesReasonField, "Files")?.let { return it }
        checkReason(permissionClipboardCheckbox, permissionClipboardReasonField, "Clipboard")?.let { return it }
        checkReason(permissionCameraCheckbox, permissionCameraReasonField, "Camera")?.let { return it }
        checkReason(permissionMicrophoneCheckbox, permissionMicrophoneReasonField, "Microphone")?.let { return it }
        // Validate platforms
        val allowedPlatforms = setOf("windows-x64", "macos-arm64", "linux-x64", "windows-arm64", "macos-x64")
        val platforms = addonPlatformsField.text?.split(',')?.map { it.trim() }?.filter { it.isNotBlank() } ?: emptyList()
        if (platforms.any { it !in allowedPlatforms }) {
            return ValidationInfo("Unsupported platform found. Allowed: ${'$'}allowedPlatforms", addonPlatformsField)
        }
        return null
    }

    override fun isBackgroundJobRunning(): Boolean = false

    @Suppress("OVERRIDE_DEPRECATION", "DEPRECATION")
    override fun addSettingsStateListener(listener: com.intellij.platform.WebProjectGenerator.SettingsStateListener) {
        stateListeners.add(listener)
    }
}
