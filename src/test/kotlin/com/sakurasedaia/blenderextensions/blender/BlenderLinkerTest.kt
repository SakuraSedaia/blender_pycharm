package com.sakurasedaia.blenderextensions.blender

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class BlenderLinkerTest : BasePlatformTestCase() {

    fun testGetExtensionsRepoDirSandboxed() {
        val linker = BlenderLinker.getInstance(project)
        val path = linker.getExtensionsRepoDir(true)
        assertNotNull(path)
        val text = path!!.toString().replace('\\', '/')
        assertTrue(text.contains("/.blender-sandbox/extensions/blender_pycharm"))
    }
}
