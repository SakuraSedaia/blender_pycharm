package com.sakurasedaia.blenderextensions.actions

import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptor
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptorFactory
import com.sakurasedaia.blenderextensions.BlenderBundle
import com.sakurasedaia.blenderextensions.icons.BlenderIcons

class BlenderTemplateGroupFactory : FileTemplateGroupDescriptorFactory {
    override fun getFileTemplatesDescriptor(): FileTemplateGroupDescriptor {
        val group = FileTemplateGroupDescriptor(BlenderBundle.message("action.BlenderExtensions.Menu.text"), BlenderIcons.Blender)
        group.addTemplate(FileTemplateGroupDescriptor(BlenderBundle.message("kind.blender.addon"), BlenderIcons.Blender))
        group.addTemplate(FileTemplateGroupDescriptor(BlenderBundle.message("kind.blender.module"), BlenderIcons.Blender))
        return group
    }
}
