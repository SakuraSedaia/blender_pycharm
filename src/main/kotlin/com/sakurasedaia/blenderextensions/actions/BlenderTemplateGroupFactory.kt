package com.sakurasedaia.blenderextensions.actions

import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptor
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptorFactory
import com.sakurasedaia.blenderextensions.LangManager
import com.sakurasedaia.blenderextensions.icons.BlenderIcons

class BlenderTemplateGroupFactory : FileTemplateGroupDescriptorFactory {
    override fun getFileTemplatesDescriptor(): FileTemplateGroupDescriptor {
        val group = FileTemplateGroupDescriptor(LangManager.message("action.BlenderExtensions.Menu.text"), BlenderIcons.Blender)
        group.addTemplate(FileTemplateGroupDescriptor(LangManager.message("kind.blender.addon"), BlenderIcons.Blender))
        group.addTemplate(FileTemplateGroupDescriptor(LangManager.message("kind.blender.module"), BlenderIcons.Blender))
        return group
    }
}
