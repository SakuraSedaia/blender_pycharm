package com.sakurasedaia.blenderextensions.settings

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import com.intellij.util.ui.FormBuilder
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JPanel

class BlenderConfigurable(private val project: Project) : Configurable {
    private var myAutoReloadCheckbox = JCheckBox("Auto-reload extension on save")

    override fun getDisplayName(): String = "Blender Extension Integration"

    override fun createComponent(): JComponent {
        return FormBuilder.createFormBuilder()
            .addComponent(myAutoReloadCheckbox)
            .addComponentFillVertically(JPanel(), 0)
            .panel
    }

    override fun isModified(): Boolean {
        val settings = BlenderSettings.getInstance(project).state
        return myAutoReloadCheckbox.isSelected != settings.autoReload
    }

    override fun apply() {
        val settings = BlenderSettings.getInstance(project).state
        settings.autoReload = myAutoReloadCheckbox.isSelected
    }

    override fun reset() {
        val settings = BlenderSettings.getInstance(project).state
        myAutoReloadCheckbox.isSelected = settings.autoReload
    }
}
