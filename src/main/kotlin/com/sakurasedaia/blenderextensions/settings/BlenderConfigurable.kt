package com.sakurasedaia.blenderextensions.settings

import com.sakurasedaia.blenderextensions.BlenderBundle
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.project.Project
import com.intellij.util.ui.FormBuilder
import com.intellij.ui.components.JBCheckBox
import javax.swing.JComponent
import javax.swing.JPanel

class BlenderConfigurable(private val project: Project) : SearchableConfigurable, Configurable.NoScroll {
    private var myAutoReloadCheckbox = JBCheckBox(BlenderBundle.message("settings.auto.reload.checkbox"))

    override fun getDisplayName(): String = BlenderBundle.message("settings.display.name")

    override fun getId(): String = "com.sakurasedaia.blenderextensions.settings.BlenderConfigurable"

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
