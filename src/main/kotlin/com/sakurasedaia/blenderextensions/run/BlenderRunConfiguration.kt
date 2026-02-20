package com.sakurasedaia.blenderextensions.run

import com.intellij.execution.ExecutionException
import com.intellij.execution.Executor
import com.intellij.execution.configurations.*
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.util.ui.FormBuilder
import com.sakurasedaia.blenderextensions.blender.BlenderService
import javax.swing.JComponent
import javax.swing.JPanel

class BlenderRunConfiguration(project: Project, factory: ConfigurationFactory, name: String) :
    RunConfigurationBase<BlenderRunConfigurationOptions>(project, factory, name) {

    public override fun getOptions(): BlenderRunConfigurationOptions {
        return super.getOptions() as BlenderRunConfigurationOptions
    }

    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> {
        return BlenderSettingsEditor(project)
    }

    override fun getState(executor: Executor, environment: ExecutionEnvironment): RunProfileState? {
        return BlenderRunProfileState(project, getOptions(), environment)
    }
}

class BlenderRunConfigurationOptions : RunConfigurationOptions() {
    private val blenderExecutablePathProperty = string("").provideDelegate(this, "blenderExecutablePath")
    var blenderExecutablePath: String?
        get() = blenderExecutablePathProperty.getValue(this)
        set(value) = blenderExecutablePathProperty.setValue(this, value)

    private val blenderVersionProperty = string("Custom/Pre-installed").provideDelegate(this, "blenderVersion")
    var blenderVersion: String?
        get() = blenderVersionProperty.getValue(this)
        set(value) = blenderVersionProperty.setValue(this, value)

    private val isSandboxedProperty = property(true).provideDelegate(this, "isSandboxed")
    var isSandboxed: Boolean
        get() = isSandboxedProperty.getValue(this)
        set(value) = isSandboxedProperty.setValue(this, value)

    private val addonSymlinkNameProperty = string("").provideDelegate(this, "addonSymlinkName")
    var addonSymlinkName: String?
        get() = addonSymlinkNameProperty.getValue(this)
        set(value) = addonSymlinkNameProperty.setValue(this, value)

    private val addonSourceDirectoryProperty = string("").provideDelegate(this, "addonSourceDirectory")
    var addonSourceDirectory: String?
        get() = addonSourceDirectoryProperty.getValue(this)
        set(value) = addonSourceDirectoryProperty.setValue(this, value)

    private val additionalArgumentsProperty = string("").provideDelegate(this, "additionalArguments")
    var additionalArguments: String?
        get() = additionalArgumentsProperty.getValue(this)
        set(value) = additionalArgumentsProperty.setValue(this, value)
}

class BlenderSettingsEditor(private val project: Project) : SettingsEditor<BlenderRunConfiguration>() {
    private val myBlenderVersionComboBox = com.intellij.openapi.ui.ComboBox<String>(arrayOf("4.2", "4.3", "4.4", "4.5", "5.0", "Custom/Pre-installed"))
    private val myIsSandboxedCheckBox = com.intellij.ui.components.JBCheckBox("Enable Sandboxing")
    private val myBlenderPathField = TextFieldWithBrowseButton()
    private val myAddonSymlinkNameField = com.intellij.ui.components.JBTextField()
    private val myAddonSourceDirectoryField = TextFieldWithBrowseButton()
    private val myAdditionalArgumentsField = com.intellij.ui.components.JBTextField()

    init {
        myBlenderVersionComboBox.addActionListener {
            val isCustom = myBlenderVersionComboBox.selectedItem == "Custom/Pre-installed"
            myBlenderPathField.isEnabled = isCustom
        }
    }

    override fun resetEditorFrom(s: BlenderRunConfiguration) {
        val options = s.getOptions()
        myBlenderVersionComboBox.selectedItem = options.blenderVersion ?: "Custom/Pre-installed"
        myIsSandboxedCheckBox.isSelected = options.isSandboxed
        myBlenderPathField.text = options.blenderExecutablePath ?: ""
        myBlenderPathField.isEnabled = myBlenderVersionComboBox.selectedItem == "Custom/Pre-installed"
        myAddonSymlinkNameField.text = options.addonSymlinkName ?: ""
        myAddonSourceDirectoryField.text = options.addonSourceDirectory ?: ""
        myAdditionalArgumentsField.text = options.additionalArguments ?: ""
    }

    override fun applyEditorTo(s: BlenderRunConfiguration) {
        val options = s.getOptions()
        options.blenderVersion = myBlenderVersionComboBox.selectedItem as? String
        options.isSandboxed = myIsSandboxedCheckBox.isSelected
        options.blenderExecutablePath = myBlenderPathField.text
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
            .addComponent(myIsSandboxedCheckBox)
            .addLabeledComponent("Manual Blender path:", myBlenderPathField)
            .addLabeledComponent("Addon symlink name:", myAddonSymlinkNameField)
            .addLabeledComponent("Addon source directory:", myAddonSourceDirectoryField)
            .addLabeledComponent("Blender commandline arguments:", myAdditionalArgumentsField)
            .addComponentFillVertically(JPanel(), 0)
            .panel
    }
}

class BlenderRunProfileState(
    private val project: Project,
    private val options: BlenderRunConfigurationOptions,
    private val environment: ExecutionEnvironment
) : RunProfileState {
    override fun execute(executor: Executor, runner: com.intellij.execution.runners.ProgramRunner<*>): com.intellij.execution.ExecutionResult? {
        val service = BlenderService.getInstance(project)
        service.log("--- Starting Blender Run Configuration: ${environment.runProfile.name} ---")
        
        val blenderPath = if (options.blenderVersion == null || options.blenderVersion == "Custom/Pre-installed") {
            service.log("Using custom Blender path: ${options.blenderExecutablePath}")
            options.blenderExecutablePath
        } else {
            // Get path for the specific version
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
        
        val consoleBuilder = com.intellij.execution.filters.TextConsoleBuilderFactory.getInstance().createBuilder(project)
        val console = consoleBuilder.console
        console.attachToProcess(handler)
        
        return com.intellij.execution.DefaultExecutionResult(console, handler)
    }
}
