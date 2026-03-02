package com.sakurasedaia.blenderextensions.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.sakurasedaia.blenderextensions.BlenderBundle
import com.sakurasedaia.blenderextensions.blender.BlenderService
import com.sakurasedaia.blenderextensions.icons.BlenderIcons

class ReloadExtensionAction : AnAction(
    BlenderBundle.messagePointer("action.ReloadExtensionAction.text"),
    BlenderBundle.messagePointer("action.ReloadExtensionAction.description"),
    BlenderIcons.Refresh
) {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        BlenderService.getInstance(project).reloadExtension()
    }
}
