package com.sakurasedaia.blenderextensions.icons

import com.intellij.openapi.util.IconLoader
import javax.swing.Icon

object BlenderIcons {
    @JvmField
    val Blender: Icon = IconLoader.getIcon("/icons/blender_logo_gray.svg", BlenderIcons::class.java)

    @JvmField
    val AddonSrcFolder: Icon = IconLoader.getIcon("/icons/addon_src_folder.svg", BlenderIcons::class.java)
}
