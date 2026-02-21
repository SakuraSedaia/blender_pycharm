package com.sakurasedaia.blenderextensions.run

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.ui.LabeledComponent
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import javax.swing.JComponent
import javax.swing.JPanel

class BlenderSettingsEditor(private val project: Project) : SettingsEditor<BlenderRunConfiguration>() {
    private val myBlenderVersionComboBox = ComboBox(arrayOf("4.2", "4.3", "4.4", "4.5", "5.0", "Custom/Pre-installed"))
    private val myBlenderPathField = TextFieldWithBrowseButton()
    private val myBlenderCommandField = JBTextField()
    private val myIsSandboxedCheckBox = JBCheckBox("Enable Sandboxing")
    private val myAddonSymlinkNameField = JBTextField()
    private val myAddonSourceDirectoryField = TextFieldWithBrowseButton()
    private val myAdditionalArgumentsField = JBTextField()

    private val myBlenderCommandComponent = LabeledComponent.create(myBlenderCommandField, "Blender command ($ blender --command <command>):")
    private val myAddonSymlinkComponent = LabeledComponent.create(myAddonSymlinkNameField, "Addon symlink name:")
    private val myAddonSourceDirComponent = LabeledComponent.create(myAddonSourceDirectoryField, "Addon source directory:")
    private val myArgumentsComponent = LabeledComponent.create(myAdditionalArgumentsField, "Blender commandline arguments:")

    init {
        myBlenderVersionComboBox.addActionListener {
            val isCustom = myBlenderVersionComboBox.selectedItem == "Custom/Pre-installed"
            myBlenderPathField.isEnabled = isCustom
        }
    }

    override fun resetEditorFrom(s: BlenderRunConfiguration) {
        val options = s.getOptions()
        myBlenderVersionComboBox.selectedItem = options.blenderVersion ?: "5.0"
        myIsSandboxedCheckBox.isSelected = options.isSandboxed
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

        return FormBuilder.createFormBuilder()
            .addLabeledComponent("Blender version:", myBlenderVersionComboBox)
            .addLabeledComponent("Manual Blender path:", myBlenderPathField)
            .addComponent(myBlenderCommandComponent)
            .addComponent(myIsSandboxedCheckBox)
            .addComponent(myAddonSymlinkComponent)
            .addComponent(myAddonSourceDirComponent)
            .addComponent(myArgumentsComponent)
            .addComponentFillVertically(JPanel(), 0)
            .panel
    }
}
