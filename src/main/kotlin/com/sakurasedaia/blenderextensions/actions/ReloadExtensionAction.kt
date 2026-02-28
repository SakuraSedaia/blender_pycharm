package com.sakurasedaia.blenderextensions.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.sakurasedaia.blenderextensions.blender.BlenderService
import com.sakurasedaia.blenderextensions.icons.BlenderIcons

class ReloadExtensionAction : AnAction("Reload Extension", "Reload Blender extension manually", BlenderIcons.Refresh) {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        BlenderService.getInstance(project).reloadExtension()
    }
}
