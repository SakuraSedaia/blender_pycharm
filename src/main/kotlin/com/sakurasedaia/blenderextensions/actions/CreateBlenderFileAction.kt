package com.sakurasedaia.blenderextensions.actions

import com.intellij.ide.actions.CreateFileFromTemplateAction
import com.intellij.ide.actions.CreateFileFromTemplateDialog
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory
import com.sakurasedaia.blenderextensions.LangManager
import com.sakurasedaia.blenderextensions.icons.BlenderIcons

class CreateBlenderFileAction : CreateFileFromTemplateAction(
    LangManager.messagePointer("action.CreateBlenderFileAction.text"),
    LangManager.messagePointer("action.CreateBlenderFileAction.description"),
    BlenderIcons.Blender
), DumbAware {
    override fun buildDialog(project: Project, directory: PsiDirectory, builder: CreateFileFromTemplateDialog.Builder) {
        builder
            .setTitle(LangManager.message("dialog.title.new.blender.file"))
            .addKind(LangManager.message("kind.blender.addon"), BlenderIcons.Blender, "Blender Add-on")
            .addKind(LangManager.message("kind.blender.module"), BlenderIcons.Blender, "Blender Module")
    }

    override fun getActionName(directory: PsiDirectory?, newName: String, templateName: String?): String = 
        LangManager.message("action.CreateBlenderFileAction.text")
}
