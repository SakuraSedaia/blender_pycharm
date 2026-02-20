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
        BlenderCommandConfigurationFactory(this)
    )
}

class BlenderCommandConfigurationFactory(type: ConfigurationType) : ConfigurationFactory(type) {
    override fun createTemplateConfiguration(project: Project): RunConfiguration {
        val config = BlenderRunConfiguration(project, this, "Blender Command")
        config.getOptions().blenderCommand = "extension validate"
        return config
    }

    override fun getId(): String = "BlenderCommandConfigurationFactory"
    override fun getOptionsClass(): Class<out BaseState> = BlenderRunConfigurationOptions::class.java
}
