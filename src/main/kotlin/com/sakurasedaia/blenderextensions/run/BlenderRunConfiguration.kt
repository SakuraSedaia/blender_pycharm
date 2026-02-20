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
    private val myBlenderPathField = TextFieldWithBrowseButton()
    private val myAddonSymlinkNameField = com.intellij.ui.components.JBTextField()
    private val myAddonSourceDirectoryField = TextFieldWithBrowseButton()
    private val myAdditionalArgumentsField = com.intellij.ui.components.JBTextField()

    override fun resetEditorFrom(s: BlenderRunConfiguration) {
        val options = s.getOptions()
        myBlenderPathField.text = options.blenderExecutablePath ?: ""
        myAddonSymlinkNameField.text = options.addonSymlinkName ?: ""
        myAddonSourceDirectoryField.text = options.addonSourceDirectory ?: ""
        myAdditionalArgumentsField.text = options.additionalArguments ?: ""
    }

    override fun applyEditorTo(s: BlenderRunConfiguration) {
        val options = s.getOptions()
        options.blenderExecutablePath = myBlenderPathField.text
        options.addonSymlinkName = myAddonSymlinkNameField.text
        options.addonSourceDirectory = myAddonSourceDirectoryField.text
        options.additionalArguments = myAdditionalArgumentsField.text
    }

    override fun createEditor(): JComponent {
        myBlenderPathField.addBrowseFolderListener(
            "Select Blender Executable",
            null,
            project,
            FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor()
        )

        myAddonSourceDirectoryField.addBrowseFolderListener(
            "Select Addon Source Directory",
            null,
            project,
            FileChooserDescriptorFactory.createSingleFolderDescriptor()
        )

        return FormBuilder.createFormBuilder()
            .addLabeledComponent("Blender path:", myBlenderPathField)
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
        val blenderPath = options.blenderExecutablePath
        if (blenderPath.isNullOrEmpty()) {
            throw ExecutionException("Blender executable path is not configured in the run configuration.")
        }
        val handler = service.startBlenderProcess(
            blenderPath,
            options.addonSourceDirectory,
            options.addonSymlinkName,
            options.additionalArguments
        ) ?: throw ExecutionException("Failed to start Blender. Check path in the run configuration.")
        
        val consoleBuilder = com.intellij.execution.filters.TextConsoleBuilderFactory.getInstance().createBuilder(project)
        val console = consoleBuilder.console
        console.attachToProcess(handler)
        
        return com.intellij.execution.DefaultExecutionResult(console, handler)
    }
}
