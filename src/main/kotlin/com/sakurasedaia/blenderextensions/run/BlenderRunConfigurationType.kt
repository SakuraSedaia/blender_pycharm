package com.sakurasedaia.blenderextensions.run

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.openapi.components.BaseState
import com.intellij.openapi.project.Project
import com.sakurasedaia.blenderextensions.icons.BlenderIcons
import java.nio.file.Path
import javax.swing.Icon
import kotlin.io.path.pathString

private fun getSrcPath(project: Project): String = 
    Path.of(project.basePath ?: "", "src").toAbsolutePath().pathString

class BlenderRunConfigurationType : ConfigurationType {
    override fun getDisplayName(): String = "Blender"
    override fun getConfigurationTypeDescription(): String = "Blender Run Configuration"
    override fun getIcon(): Icon = BlenderIcons.BlenderColor
    override fun getId(): String = "BLENDER_RUN_CONFIGURATION"
    override fun getConfigurationFactories(): Array<ConfigurationFactory> = arrayOf(
        BlenderStartBlenderConfigurationFactory(this),
        BlenderBuildConfigurationFactory(this),
        BlenderValidateConfigurationFactory(this),
        BlenderCommandConfigurationFactory(this)
    )
}

class BlenderStartBlenderConfigurationFactory(type: ConfigurationType) : ConfigurationFactory(type) {
    override fun createTemplateConfiguration(project: Project): RunConfiguration =
        BlenderRunConfiguration(project, this, "Start Blender")

    override fun getName(): String = "Start Blender"
    override fun getId(): String = "BlenderStartBlenderConfigurationFactory"
    override fun getOptionsClass(): Class<out BaseState> = BlenderRunConfigurationOptions::class.java
}

class BlenderBuildConfigurationFactory(type: ConfigurationType) : ConfigurationFactory(type) {
    
    override fun createTemplateConfiguration(project: Project): RunConfiguration {
        val config = BlenderRunConfiguration(project, this, "Build")
        config.getOptions().blenderCommand = "extension build --source-dir ${getSrcPath(project)}"
        return config
    }

    override fun getName(): String = "Build"
    override fun getId(): String = "BlenderBuildConfigurationFactory"
    override fun getOptionsClass(): Class<out BaseState> = BlenderRunConfigurationOptions::class.java
}

class BlenderValidateConfigurationFactory(type: ConfigurationType) : ConfigurationFactory(type) {
    
    override fun createTemplateConfiguration(project: Project): RunConfiguration {
        val config = BlenderRunConfiguration(project, this, "Validate")
        config.getOptions().blenderCommand = "extension validate ${getSrcPath(project)}"
        return config
    }

    override fun getName(): String = "Validate"
    override fun getId(): String = "BlenderValidateConfigurationFactory"
    override fun getOptionsClass(): Class<out BaseState> = BlenderRunConfigurationOptions::class.java
}

class BlenderCommandConfigurationFactory(type: ConfigurationType) : ConfigurationFactory(type) {
    override fun createTemplateConfiguration(project: Project): RunConfiguration {
        val config = BlenderRunConfiguration(project, this, "Command")
        config.getOptions().blenderCommand = ""
        return config
    }

    override fun getName(): String = "Command"
    override fun getId(): String = "BlenderCommandConfigurationFactory"
    override fun getOptionsClass(): Class<out BaseState> = BlenderRunConfigurationOptions::class.java
}
