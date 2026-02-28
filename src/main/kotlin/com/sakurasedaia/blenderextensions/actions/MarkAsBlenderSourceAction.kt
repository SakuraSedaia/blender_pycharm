package com.sakurasedaia.blenderextensions.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.ToggleAction
import com.intellij.openapi.vfs.VirtualFile
import com.sakurasedaia.blenderextensions.settings.BlenderSettings

class MarkAsBlenderSourceAction : ToggleAction("Mark as Blender Source", "Designate this folder as a Blender extension source folder", null) {

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    override fun isSelected(e: AnActionEvent): Boolean {
        val project = e.project ?: return false
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return false
        if (!file.isDirectory) return false
        
        return BlenderSettings.getInstance(project).isSourceFolder(file.path)
    }

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        val project = e.project ?: return
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return
        if (!file.isDirectory) return
        
        val settings = BlenderSettings.getInstance(project)
        if (state) {
            settings.addSourceFolder(file.path)
        } else {
            settings.removeSourceFolder(file.path)
        }
    }

    override fun update(e: AnActionEvent) {
        super.update(e)
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE)
        e.presentation.isEnabledAndVisible = file != null && file.isDirectory
    }
}
