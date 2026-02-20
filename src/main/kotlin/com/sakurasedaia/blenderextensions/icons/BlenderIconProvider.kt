package com.sakurasedaia.blenderextensions.icons

import com.intellij.ide.IconProvider
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import javax.swing.Icon

class BlenderIconProvider : IconProvider() {
    override fun getIcon(element: PsiElement, flags: Int): Icon? {
        if (element is PsiDirectory) {
            val virtualFile = element.virtualFile
            if (virtualFile.findChild("blender_manifest.toml") != null) {
                return BlenderIcons.AddonSrcFolder
            }
        }
        return null
    }
}
