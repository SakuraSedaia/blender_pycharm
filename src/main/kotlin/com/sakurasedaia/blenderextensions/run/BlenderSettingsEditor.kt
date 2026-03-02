package com.sakurasedaia.blenderextensions.run

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.ui.LabeledComponent
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import com.sakurasedaia.blenderextensions.blender.BlenderDownloader
import com.sakurasedaia.blenderextensions.blender.BlenderVersions
import com.sakurasedaia.blenderextensions.icons.BlenderIcons
import java.awt.BorderLayout
import javax.swing.DefaultComboBoxModel
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.SwingUtilities

class BlenderSettingsEditor(private val project: Project) : SettingsEditor<BlenderRunConfiguration>() {
    private val downloader = BlenderDownloader.getInstance(project)
    private val myBlenderVersionComboBox = ComboBox<String>()
    private val myBlenderPathField = TextFieldWithBrowseButton()
    private val myBlenderCommandField = JBTextField()
    private val myIsSandboxedCheckBox = JBCheckBox("Enable Sandboxed Environment")
    private val myImportUserConfigCheckBox = JBCheckBox("Import User Configuration")
    private val myAddonSymlinkNameField = JBTextField()
    private val myAddonSourceDirectoryField = TextFieldWithBrowseButton()
    private val myAdditionalArgumentsField = JBTextField()
    private val myDownloadButton = JButton("Download", BlenderIcons.Install)

    private val myBlenderCommandComponent = LabeledComponent.create(myBlenderCommandField, "Blender command ($ blender --command <command>):")
    private val myAddonSymlinkComponent = LabeledComponent.create(myAddonSymlinkNameField, "Addon symlink name:")
    private val myAddonSourceDirComponent = LabeledComponent.create(myAddonSourceDirectoryField, "Addon source directory:")
    private val myArgumentsComponent = LabeledComponent.create(myAdditionalArgumentsField, "Blender commandline arguments:")

    init {
        val versions = BlenderVersions.getAllSelectableVersions(downloader)
        myBlenderVersionComboBox.model = DefaultComboBoxModel(versions.toTypedArray())

        myBlenderVersionComboBox.addActionListener {
            updateDownloadButtonVisibility()
            val selected = myBlenderVersionComboBox.selectedItem as? String
            val isCustom = selected == "Custom/Pre-installed"
            myBlenderPathField.isEnabled = isCustom
        }
        
        myDownloadButton.addActionListener {
            val selected = myBlenderVersionComboBox.selectedItem as? String ?: return@addActionListener
            if (!downloader.isDownloaded(selected)) {
                ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Downloading Blender $selected") {
                    override fun run(indicator: ProgressIndicator) {
                        downloader.getOrDownloadBlenderPath(selected)
                        SwingUtilities.invokeLater {
                            updateDownloadButtonVisibility()
                        }
                    }
                })
            }
        }
    }

    private fun updateDownloadButtonVisibility() {
        val selected = myBlenderVersionComboBox.selectedItem as? String
        if (selected != null && selected != "Custom/Pre-installed") {
            myDownloadButton.isVisible = !downloader.isDownloaded(selected)
        } else {
            myDownloadButton.isVisible = false
        }
    }

    override fun resetEditorFrom(s: BlenderRunConfiguration) {
        val options = s.getOptions()
        myBlenderVersionComboBox.selectedItem = options.blenderVersion ?: "5.0"
        updateDownloadButtonVisibility()
        myIsSandboxedCheckBox.isSelected = options.isSandboxed
        myImportUserConfigCheckBox.isSelected = options.importUserConfig
        myBlenderPathField.text = options.blenderExecutablePath ?: ""
        myBlenderPathField.isEnabled = myBlenderVersionComboBox.selectedItem == "Custom/Pre-installed"
        myBlenderCommandField.text = options.blenderCommand ?: ""
        myAddonSymlinkNameField.text = options.addonSymlinkName ?: ""
        myAddonSourceDirectoryField.text = options.addonSourceDirectory ?: ""
        myAdditionalArgumentsField.text = options.additionalArguments ?: ""

        val factory = s.factory
        val isTesting = factory is BlenderStartBlenderConfigurationFactory
        val isCommand = factory is BlenderCommandConfigurationFactory
        val isBuildOrValidate = factory is BlenderBuildConfigurationFactory || factory is BlenderValidateConfigurationFactory

        myIsSandboxedCheckBox.isVisible = isTesting
        myImportUserConfigCheckBox.isVisible = isTesting
        myAddonSymlinkComponent.isVisible = isTesting
        myAddonSourceDirComponent.isVisible = isTesting
        myArgumentsComponent.isVisible = isTesting
        
        myBlenderCommandComponent.isVisible = isCommand || isBuildOrValidate
        myBlenderCommandField.isEnabled = isCommand
    }

    override fun applyEditorTo(s: BlenderRunConfiguration) {
        val options = s.getOptions()
        options.blenderVersion = myBlenderVersionComboBox.selectedItem as? String
        options.isSandboxed = myIsSandboxedCheckBox.isSelected
        options.importUserConfig = myImportUserConfigCheckBox.isSelected
        options.blenderExecutablePath = myBlenderPathField.text
        options.blenderCommand = myBlenderCommandField.text
        options.addonSymlinkName = myAddonSymlinkNameField.text
        options.addonSourceDirectory = myAddonSourceDirectoryField.text
        options.additionalArguments = myAdditionalArgumentsField.text
    }

    override fun createEditor(): JComponent {
        myBlenderPathField.addBrowseFolderListener(
            com.intellij.openapi.ui.TextBrowseFolderListener(
                FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor(),
                project
            )
        )

        myAddonSourceDirectoryField.addBrowseFolderListener(
            com.intellij.openapi.ui.TextBrowseFolderListener(
                FileChooserDescriptorFactory.createSingleFolderDescriptor(),
                project
            )
        )

        val versionPanel = JPanel(BorderLayout())
        versionPanel.add(myBlenderVersionComboBox, BorderLayout.CENTER)
        versionPanel.add(myDownloadButton, BorderLayout.EAST)

        return FormBuilder.createFormBuilder()
            .addLabeledComponent("Blender version:", versionPanel)
            .addLabeledComponent("Manual Blender path:", myBlenderPathField)
            .addComponent(myBlenderCommandComponent)
            .addComponent(myIsSandboxedCheckBox)
            .addComponent(myImportUserConfigCheckBox)
            .addComponent(myAddonSymlinkComponent)
            .addComponent(myAddonSourceDirComponent)
            .addComponent(myArgumentsComponent)
            .addComponentFillVertically(JPanel(), 0)
            .panel
    }
}
