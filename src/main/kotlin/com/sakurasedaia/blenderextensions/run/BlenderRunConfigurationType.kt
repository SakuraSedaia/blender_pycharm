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
        BlenderTestingConfigurationFactory(this),
        BlenderBuildConfigurationFactory(this),
        BlenderValidateConfigurationFactory(this),
        BlenderCommandConfigurationFactory(this)
    )
}

class BlenderTestingConfigurationFactory(type: ConfigurationType) : ConfigurationFactory(type) {
    override fun createTemplateConfiguration(project: Project): RunConfiguration =
        BlenderRunConfiguration(project, this, "Testing")

    override fun getName(): String = "Testing"
    override fun getId(): String = "BlenderTestingConfigurationFactory"
    override fun getOptionsClass(): Class<out BaseState> = BlenderRunConfigurationOptions::class.java
}

class BlenderBuildConfigurationFactory(type: ConfigurationType) : ConfigurationFactory(type) {
    override fun createTemplateConfiguration(project: Project): RunConfiguration {
        val config = BlenderRunConfiguration(project, this, "Build")
        config.getOptions().blenderCommand = "extensions build"
        return config
    }

    override fun getName(): String = "Build"
    override fun getId(): String = "BlenderBuildConfigurationFactory"
    override fun getOptionsClass(): Class<out BaseState> = BlenderRunConfigurationOptions::class.java
}

class BlenderValidateConfigurationFactory(type: ConfigurationType) : ConfigurationFactory(type) {
    override fun createTemplateConfiguration(project: Project): RunConfiguration {
        val config = BlenderRunConfiguration(project, this, "Validate")
        config.getOptions().blenderCommand = "extensions validate"
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
