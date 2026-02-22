package com.sakurasedaia.blenderextensions.icons

import com.intellij.ide.IconProvider
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiElement
import javax.swing.Icon

class BlenderIconProvider : IconProvider() {
    override fun getIcon(element: PsiElement, flags: Int): Icon? {
        if (element is PsiFile) {
            val name = element.name
            if (name.endsWith(".blend") || name.contains(".blend") && name.substringAfterLast(".blend").all { it.isDigit() }) {
                return BlenderIcons.BlenderColor
            }
        }
        return null
    }
}
