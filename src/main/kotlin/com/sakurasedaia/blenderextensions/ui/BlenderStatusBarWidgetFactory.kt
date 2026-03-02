package com.sakurasedaia.blenderextensions.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.StatusBarWidgetFactory
import com.sakurasedaia.blenderextensions.BlenderBundle

class BlenderStatusBarWidgetFactory : StatusBarWidgetFactory {
    override fun getId(): String = "BlenderStatus"
    
    override fun getDisplayName(): String = BlenderBundle.message("settings.display.name")
    
    override fun isAvailable(project: Project): Boolean = true
    
    override fun createWidget(project: Project): StatusBarWidget = BlenderStatusBarWidget(project)
    
    override fun disposeWidget(widget: StatusBarWidget) {}
    
    override fun canBeEnabledOn(statusBar: StatusBar): Boolean = true
}
