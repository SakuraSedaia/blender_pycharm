package com.sakurasedaia.blenderextensions.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.sakurasedaia.blenderextensions.blender.BlenderService

class ReloadExtensionAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        BlenderService.getInstance(project).reloadExtension()
    }
}
