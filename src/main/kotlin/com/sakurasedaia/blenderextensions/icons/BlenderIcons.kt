package com.sakurasedaia.blenderextensions.icons

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
    /* TODO: Implement the following Icons from the standard Jetbrains to reference in the code:
    *   - AllIcons.General.Refresh
    *   - AllIcons.General.Install
    * */
    
    
}
