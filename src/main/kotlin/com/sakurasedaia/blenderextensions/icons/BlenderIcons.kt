package com.sakurasedaia.blenderextensions.icons

import com.intellij.icons.AllIcons
import com.intellij.openapi.util.IconLoader
import javax.swing.Icon

object BlenderIcons {
    @JvmField
    val Blender: Icon = IconLoader.getIcon("/images/blenderGray.svg", BlenderIcons::class.java)
    
    @JvmField
    val BlenderColor: Icon = IconLoader.getIcon("/images/blender_color.svg", BlenderIcons::class.java)

    @JvmField
    val AddonSrcFolder: Icon = IconLoader.getIcon("/images/addonFolder.svg", BlenderIcons::class.java)
    
    @JvmField
    val BlendFile: Icon = IconLoader.getIcon("/images/blendFile.svg", BlenderIcons::class.java)

    @JvmField
    val Refresh: Icon = AllIcons.Actions.Refresh

    @JvmField
    val Install: Icon = AllIcons.Actions.Install

    @JvmField
    val Delete: Icon = AllIcons.Actions.GC

    @JvmField
    val Checkmark: Icon = AllIcons.Actions.Checked

    @JvmField
    val Cross: Icon = AllIcons.General.Close

    @JvmField
    val Add: Icon = AllIcons.General.Add

    @JvmField
    val System: Icon = AllIcons.Nodes.HomeFolder

    @JvmField
    val Custom: Icon = AllIcons.General.User
}
