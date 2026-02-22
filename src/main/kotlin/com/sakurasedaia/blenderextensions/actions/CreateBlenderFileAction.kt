package com.sakurasedaia.blenderextensions.actions

import com.intellij.ide.actions.CreateFileFromTemplateAction
import com.intellij.ide.actions.CreateFileFromTemplateDialog
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory
import com.sakurasedaia.blenderextensions.icons.BlenderIcons

class CreateBlenderFileAction : CreateFileFromTemplateAction("Blender File", "Create a new Blender Python file", BlenderIcons.Blender), DumbAware {
    override fun buildDialog(project: Project, directory: PsiDirectory, builder: CreateFileFromTemplateDialog.Builder) {
        builder
            .setTitle("New Blender File")
            .addKind("Blender Add-on", BlenderIcons.Blender, "Blender Add-on")
            .addKind("Blender Module", BlenderIcons.Blender, "Blender Module")
    }

    override fun getActionName(directory: PsiDirectory?, newName: String, templateName: String?): String = "Blender File"
}
