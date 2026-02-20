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
import java.nio.file.Files
import java.nio.file.Path
import javax.swing.Icon
import javax.swing.JPanel
import java.lang.ref.WeakReference
import kotlin.io.path.writeText

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
        val chosenName = settings.projectName?.takeIf { it.isNotBlank() } ?: project.name
        val projectName = chosenName
        val authorName = System.getProperty("user.name") ?: "Author"
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
    }
}

data class BlenderAddonProjectSettings(
    var enableAutoLoad: Boolean = false,
    var projectName: String? = null
)

private class BlenderAddonProjectPeer : ProjectGeneratorPeer<BlenderAddonProjectSettings> {
    private var projectLocation: String? = null
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
    }

    private val autoLoadCheckbox = JBCheckBox("Enable auto-load (adds auto_load.py and autoload __init__.py)", false)
    private val projectNameField = JBTextField()
    private val panel: JPanel

    init {
        projectNameField.document.addDocumentListener(object : DocumentAdapter() {
            override fun textChanged(e: DocumentEvent) {
                if (isUpdating) return
                val original = projectNameField.text
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

        panel = FormBuilder.createFormBuilder()
            .addLabeledComponent("Project name:", projectNameField)
            .addComponent(autoLoadCheckbox)
            .addComponentFillVertically(JPanel(), 0)
            .panel
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

    @Suppress("OVERRIDE_DEPRECATION")
    override fun getComponent(): javax.swing.JComponent = panel

    @Suppress("OVERRIDE_DEPRECATION")
    override fun buildUI(settingsStep: com.intellij.ide.util.projectWizard.SettingsStep) {
        settingsStep.addSettingsComponent(component)
    }

    override fun getSettings(): BlenderAddonProjectSettings = BlenderAddonProjectSettings(
        enableAutoLoad = autoLoadCheckbox.isSelected,
        projectName = projectNameField.text?.trim()
    )

    override fun validate(): ValidationInfo? = null

    override fun isBackgroundJobRunning(): Boolean = false

    @Suppress("OVERRIDE_DEPRECATION", "DEPRECATION")
    override fun addSettingsStateListener(listener: com.intellij.platform.WebProjectGenerator.SettingsStateListener) {}
}
