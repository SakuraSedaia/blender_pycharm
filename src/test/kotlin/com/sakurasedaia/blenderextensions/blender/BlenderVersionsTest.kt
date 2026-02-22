package com.sakurasedaia.blenderextensions.blender

import org.junit.Assert.*
import org.junit.Test

class BlenderVersionsTest {

    @Test
    fun testSupportedVersions() {
        assertEquals(5, BlenderVersions.SUPPORTED_VERSIONS.size)
        assertTrue(BlenderVersions.SUPPORTED_VERSIONS.contains("4.2"))
        assertTrue(BlenderVersions.SUPPORTED_VERSIONS.contains("5.0"))
    }

    @Test
    fun testFallbackPatches() {
        assertEquals("18", BlenderVersions.FALLBACK_PATCHES["4.2"])
        assertEquals("1", BlenderVersions.FALLBACK_PATCHES["5.0"])
    }

    @Test
    fun testGetSupportedVersionsWithCustom() {
        val versions = BlenderVersions.getSupportedVersionsWithCustom()
        assertTrue(versions.contains("4.2"))
        assertTrue(versions.contains("5.0"))
        assertTrue(versions.contains("Custom/Pre-installed"))
        assertEquals(6, versions.size)
    }
}
