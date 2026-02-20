package com.sakurasedaia.blenderextensions.listeners

import com.intellij.openapi.fileEditor.FileDocumentManagerListener
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.ProjectManager
import com.sakurasedaia.blenderextensions.blender.BlenderService
import com.sakurasedaia.blenderextensions.settings.BlenderSettings

class BlenderFileSaveListener : FileDocumentManagerListener {
    override fun beforeDocumentSaving(document: Document) {
        val projects = ProjectManager.getInstance().openProjects
        for (project in projects) {
            val settings = BlenderSettings.getInstance(project).state
            if (settings.autoReload) {
                BlenderService.getInstance(project).reloadExtension()
            }
        }
    }
}
