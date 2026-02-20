package com.sakurasedaia.blenderextensions.settings

import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project

@Service(Service.Level.PROJECT)
@State(name = "BlenderSettings", storages = [Storage("blender_settings.xml")])
class BlenderSettings : PersistentStateComponent<BlenderSettings.State> {
    data class State(
        var autoReload: Boolean = true
    )

    private var myState = State()

    override fun getState(): State = myState
    override fun loadState(state: State) {
        myState = state
    }

    companion object {
        fun getInstance(project: Project): BlenderSettings = project.getService(BlenderSettings::class.java)
    }
}
