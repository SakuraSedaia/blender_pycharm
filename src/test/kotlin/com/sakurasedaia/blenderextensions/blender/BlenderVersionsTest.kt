package com.sakurasedaia.blenderextensions.blender

import org.junit.Assert.*
import org.junit.Test

class BlenderVersionsTest {

    @Test
    fun testSupportedVersions() {
        assertEquals(3, BlenderVersions.SUPPORTED_VERSIONS.size)
        assertTrue(BlenderVersions.SUPPORTED_VERSIONS.any { it.majorMinor == "4.2" })
        assertTrue(BlenderVersions.SUPPORTED_VERSIONS.any { it.majorMinor == "5.0" })
    }

    @Test
    fun testFallbackPatches() {
        assertEquals("18", BlenderVersions.SUPPORTED_VERSIONS.find { it.majorMinor == "4.2" }?.fallbackPatch)
        assertEquals("1", BlenderVersions.SUPPORTED_VERSIONS.find { it.majorMinor == "5.0" }?.fallbackPatch)
    }

    @Test
    fun testGetSupportedVersions() {
        val versions = BlenderVersions.getSupportedVersionsWithCustom()
        assertTrue(versions.contains("4.2"))
        assertTrue(versions.contains("5.0"))
        assertEquals(3, versions.size)
    }

    @Test
    fun testGetAllSelectableVersions() {
        val versions = BlenderVersions.getAllSelectableVersions()
        assertTrue(versions.contains("4.2"))
        assertTrue(versions.contains("5.0"))
    }
}
