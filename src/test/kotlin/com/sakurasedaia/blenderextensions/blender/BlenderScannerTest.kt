package com.sakurasedaia.blenderextensions.blender

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BlenderScannerTest {

    @Test
    fun testScanSystemInstallationsDoesNotCrash() {
        val installations = BlenderScanner.scanSystemInstallations()
        // We can't guarantee anything is installed on the build machine, 
        // but we can check it doesn't crash and returns a list.
        assertNotNull(installations)
    }

    @Test
    fun testTryGetVersionWithInvalidPath() {
        val version = BlenderScanner.tryGetVersion("/path/to/nonexistent/blender")
        assertNotNull(version)
        assertTrue(version == "Unknown")
    }
}
