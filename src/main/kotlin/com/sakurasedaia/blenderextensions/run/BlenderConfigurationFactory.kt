package com.sakurasedaia.blenderextensions.run

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.openapi.components.BaseState
import com.intellij.openapi.project.Project

class BlenderConfigurationFactory(type: ConfigurationType) : ConfigurationFactory(type) {
    override fun createTemplateConfiguration(project: Project): RunConfiguration =
        BlenderRunConfiguration(project, this, "Blender")

    override fun getId(): String = "BlenderConfigurationFactory"

    override fun getOptionsClass(): Class<out BaseState> = BlenderRunConfigurationOptions::class.java
}
