package com.sakurasedaia.blenderextensions.actions

import com.intellij.ide.actions.CreateFileFromTemplateAction
import com.intellij.ide.actions.CreateFileFromTemplateDialog
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory
import com.sakurasedaia.blenderextensions.icons.BlenderIcons

class CreateBlenderAddonAction : CreateFileFromTemplateAction("Blender Add-on", "Create a new single-script Blender add-on", BlenderIcons.Blender), DumbAware {
    override fun buildDialog(project: Project, directory: PsiDirectory, builder: CreateFileFromTemplateDialog.Builder) {
        builder
            .setTitle("New Blender Add-on")
            .addKind("Blender Add-on", BlenderIcons.Blender, "Blender Add-on")
    }

    override fun getActionName(directory: PsiDirectory?, newName: String, templateName: String?): String = "Blender Add-on"
}
