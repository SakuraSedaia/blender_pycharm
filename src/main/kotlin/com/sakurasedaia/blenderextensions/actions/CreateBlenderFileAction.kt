package com.sakurasedaia.blenderextensions.actions

import com.intellij.ide.actions.CreateFileFromTemplateAction
import com.intellij.ide.actions.CreateFileFromTemplateDialog
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory
import com.sakurasedaia.blenderextensions.BlenderBundle
import com.sakurasedaia.blenderextensions.icons.BlenderIcons

class CreateBlenderFileAction : CreateFileFromTemplateAction(
    BlenderBundle.messagePointer("action.CreateBlenderFileAction.text"),
    BlenderBundle.messagePointer("action.CreateBlenderFileAction.description"),
    BlenderIcons.Blender
), DumbAware {
    override fun buildDialog(project: Project, directory: PsiDirectory, builder: CreateFileFromTemplateDialog.Builder) {
        builder
            .setTitle(BlenderBundle.message("dialog.title.new.blender.file"))
            .addKind(BlenderBundle.message("kind.blender.addon"), BlenderIcons.Blender, "Blender Add-on")
            .addKind(BlenderBundle.message("kind.blender.module"), BlenderIcons.Blender, "Blender Module")
    }

    override fun getActionName(directory: PsiDirectory?, newName: String, templateName: String?): String = 
        BlenderBundle.message("action.CreateBlenderFileAction.text")
}
