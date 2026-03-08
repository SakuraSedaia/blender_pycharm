package com.sakurasedaia.blenderextensions.actions

import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptor
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptorFactory
import com.sakurasedaia.blenderextensions.LangManager
import com.sakurasedaia.blenderextensions.icons.BlenderIcons

class BlenderTemplateGroupFactory : FileTemplateGroupDescriptorFactory {
    override fun getFileTemplatesDescriptor(): FileTemplateGroupDescriptor {
        val group = FileTemplateGroupDescriptor(LangManager.message("action.blender.menu.text"), BlenderIcons.Blender)
        group.addTemplate(FileTemplateGroupDescriptor(LangManager.message("blender.kind.addon"), BlenderIcons.Blender))
        group.addTemplate(FileTemplateGroupDescriptor(LangManager.message("blender.kind.module"), BlenderIcons.Blender))
        return group
    }
}
