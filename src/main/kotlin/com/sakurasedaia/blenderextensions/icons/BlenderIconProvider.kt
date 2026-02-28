package com.sakurasedaia.blenderextensions.icons

import com.intellij.ide.IconProvider
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiDirectory
import com.sakurasedaia.blenderextensions.settings.BlenderSettings
import javax.swing.Icon

class BlenderIconProvider : IconProvider() {
    override fun getIcon(element: PsiElement, flags: Int): Icon? {
        if (element is PsiFile) {
            val name = element.name
            if (name.endsWith(".blend") || name.contains(".blend") && name.substringAfterLast(".blend").all { it.isDigit() }) {
                return BlenderIcons.BlendFile
            }
        } else if (element is PsiDirectory) {
            val virtualFile = element.virtualFile
            val project = element.project
            if (BlenderSettings.getInstance(project).isSourceFolder(virtualFile.path)) {
                return BlenderIcons.AddonSrcFolder
            }
        }
        return null
    }
}
