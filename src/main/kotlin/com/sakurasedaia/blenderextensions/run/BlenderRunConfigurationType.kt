package com.sakurasedaia.blenderextensions.run

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.openapi.components.BaseState
import com.intellij.openapi.project.Project
import com.sakurasedaia.blenderextensions.icons.BlenderIcons
import javax.swing.Icon

class BlenderRunConfigurationType : ConfigurationType {
    override fun getDisplayName(): String = "Blender"
    override fun getConfigurationTypeDescription(): String = "Blender Run Configuration"
    override fun getIcon(): Icon = BlenderIcons.Blender
    override fun getId(): String = "BLENDER_RUN_CONFIGURATION"
    override fun getConfigurationFactories(): Array<ConfigurationFactory> = arrayOf(
        BlenderConfigurationFactory(this),
        BlenderValidateConfigurationFactory(this),
        BlenderBuildConfigurationFactory(this)
    )
}

class BlenderValidateConfigurationFactory(type: ConfigurationType) : ConfigurationFactory(type) {
    override fun createTemplateConfiguration(project: Project): RunConfiguration {
        val config = BlenderRunConfiguration(project, this, "Validate Extension")
        config.getOptions().additionalArguments = "extension validate"
        return config
    }

    override fun getId(): String = "BlenderValidateConfigurationFactory"
    override fun getOptionsClass(): Class<out BaseState> = BlenderRunConfigurationOptions::class.java
}

class BlenderBuildConfigurationFactory(type: ConfigurationType) : ConfigurationFactory(type) {
    override fun createTemplateConfiguration(project: Project): RunConfiguration {
        val config = BlenderRunConfiguration(project, this, "Build Extension")
        config.getOptions().additionalArguments = "extension build"
        return config
    }

    override fun getId(): String = "BlenderBuildConfigurationFactory"
    override fun getOptionsClass(): Class<out BaseState> = BlenderRunConfigurationOptions::class.java
}
