package com.sakurasedaia.blenderextensions.actions

import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptor
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptorFactory
import com.sakurasedaia.blenderextensions.icons.BlenderIcons

class BlenderTemplateGroupFactory : FileTemplateGroupDescriptorFactory {
    override fun getFileTemplatesDescriptor(): FileTemplateGroupDescriptor {
        val group = FileTemplateGroupDescriptor("Blender", BlenderIcons.Blender)
        group.addTemplate(FileTemplateGroupDescriptor("Blender Add-on.py", BlenderIcons.Blender))
        group.addTemplate(FileTemplateGroupDescriptor("Blender Module.py", BlenderIcons.Blender))
        return group
    }
}
