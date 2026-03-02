package com.sakurasedaia.blenderextensions.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.util.Alarm
import com.sakurasedaia.blenderextensions.BlenderBundle
import com.sakurasedaia.blenderextensions.blender.BlenderCommunicationService
import com.sakurasedaia.blenderextensions.blender.BlenderService
import com.sakurasedaia.blenderextensions.icons.BlenderIcons
import javax.swing.Icon

class BlenderStatusBarWidget(private val project: Project) : StatusBarWidget, StatusBarWidget.MultipleTextValuesPresentation {
    private var statusBar: StatusBar? = null
    private val alarm = Alarm(Alarm.ThreadToUse.SWING_THREAD, this)

    override fun ID(): String = "BlenderStatus"

    override fun getPresentation(): StatusBarWidget.WidgetPresentation = this

    override fun install(statusBar: StatusBar) {
        this.statusBar = statusBar
        scheduleUpdate()
    }

    override fun dispose() {
        statusBar = null
    }

    override fun getSelectedValue(): String? {
        val service = BlenderService.getInstance(project)
        val commService = BlenderCommunicationService.getInstance(project)
        
        return when {
            commService.isConnected() -> BlenderBundle.message("blender.status.connected")
            service.isRunning() -> BlenderBundle.message("blender.status.disconnected")
            service.hasError() -> BlenderBundle.message("blender.status.error")
            else -> BlenderBundle.message("blender.status.not_running")
        }
    }

    override fun getPopupStep(): com.intellij.openapi.ui.popup.ListPopup? = null

    override fun getIcon(): Icon? {
        val service = BlenderService.getInstance(project)
        val commService = BlenderCommunicationService.getInstance(project)
        
        return when {
            commService.isConnected() -> BlenderIcons.Checkmark
            service.isRunning() -> BlenderIcons.BlenderColor
            service.hasError() -> com.intellij.icons.AllIcons.General.Error
            else -> BlenderIcons.Blender
        }
    }

    override fun getTooltipText(): String? = BlenderBundle.message("settings.display.name")

    private fun scheduleUpdate() {
        if (statusBar == null) return
        
        alarm.addRequest({
            statusBar?.updateWidget(ID())
            scheduleUpdate()
        }, 1000)
    }
}
