package com.sakurasedaia.blenderextensions.project

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BlenderFileTemplateTest {
    @Test
    fun testFileTemplateExists() {
        val resource = javaClass.getResource("/fileTemplates/internal/Blender Add-on.py.ft")
        assertNotNull("File template should exist in resources", resource)
        val content = resource!!.readText()
        assertTrue("Template should contain bl_info", content.contains("bl_info = {"))
        assertTrue("Template should contain register", content.contains("def register():"))
        assertTrue("Template should contain unregister", content.contains("def unregister():"))
        assertTrue("Template should contain Velocity variables", content.contains("\${NAME}"))
    }
}
