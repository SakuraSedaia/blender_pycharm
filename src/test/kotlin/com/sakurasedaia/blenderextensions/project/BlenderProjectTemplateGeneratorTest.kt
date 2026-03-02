package com.sakurasedaia.blenderextensions.project

import org.junit.Assert.assertTrue
import org.junit.Test

class BlenderProjectTemplateGeneratorTest {

    @Test
    fun testGenerateManifestBasic() {
        val settings = BlenderManifestSettings(
            id = "test-addon",
            name = "Test Addon",
            tagline = "A test tagline",
            maintainer = "Test Author",
            blenderVersionMin = "4.2.0"
        )
        val manifest = BlenderProjectTemplateGenerator.generateManifest(settings)
        
        assertTrue(manifest.contains("id = \"test-addon\""))
        assertTrue(manifest.contains("name = \"Test Addon\""))
        assertTrue(manifest.contains("tagline = \"A test tagline\""))
        assertTrue(manifest.contains("maintainer = \"Test Author\""))
        assertTrue(manifest.contains("blender_version_min = \"4.2.0\""))
        assertTrue(manifest.contains("type = \"add-on\""))
    }

    @Test
    fun testGenerateManifestFull() {
        val settings = BlenderManifestSettings(
            id = "full-addon",
            name = "Full Addon",
            tagline = "A very full addon",
            maintainer = "Busy Author",
            website = "https://example.com",
            tags = listOf("mesh", "edit"),
            blenderVersionMin = "4.2.0",
            blenderVersionMax = "5.0.0",
            platforms = listOf("windows-x64", "linux-x64"),
            permissions = mapOf("network" to "Check for updates", "files" to "Read logs"),
            buildPathsExcludePattern = listOf("*.txt", "tests/")
        )
        val manifest = BlenderProjectTemplateGenerator.generateManifest(settings)
        
        assertTrue(manifest.contains("website = \"https://example.com\""))
        assertTrue(manifest.contains("tags = [\"mesh\", \"edit\"]"))
        assertTrue(manifest.contains("blender_version_max = \"5.0.0\""))
        assertTrue(manifest.contains("platforms = [\"windows-x64\", \"linux-x64\"]"))
        assertTrue(manifest.contains("[permissions]"))
        assertTrue(manifest.contains("network = \"Check for updates\""))
        assertTrue(manifest.contains("files = \"Read logs\""))
        assertTrue(manifest.contains("paths_exclude_pattern = [\"*.txt\", \"tests/\"]"))
    }

    @Test
    fun testGenerateReadme() {
        val readme = BlenderProjectTemplateGenerator.generateReadme("Name", "Author", "A Cool Tagline")
        assertTrue(readme.contains("# Blender Extension"))
        assertTrue(readme.contains("## Setup Instructions"))
    }

    @Test
    fun testGenerateSimpleInit() {
        val init = BlenderProjectTemplateGenerator.generateSimpleInit("Test Name", "Test Author")
        assertTrue(init.contains("\"name\": \"Test Name\""))
        assertTrue(init.contains("\"author\": \"Test Author\""))
        assertTrue(init.contains("def register():"))
        assertTrue(init.contains("def unregister():"))
    }

    @Test
    fun testGenerateAutoLoadInit() {
        val init = BlenderProjectTemplateGenerator.generateAutoLoadInit("Auto Name", "Auto Author")
        assertTrue(init.contains("\"name\": \"Auto Name\""))
        assertTrue(init.contains("from . import auto_load"))
        assertTrue(init.contains("auto_load.init()"))
    }

    @Test
    fun testGenerateLicense() {
        val license = BlenderProjectTemplateGenerator.generateLicense()
        assertTrue(license.contains("GNU GENERAL PUBLIC LICENSE"))
        assertTrue(license.contains("Version 3, 29 June 2007"))
        assertTrue(license.contains("Copyright © 2007 Free Software Foundation, Inc."))
        // Check if it's the full version, which should be quite long
        assertTrue(license.length > 30000)
    }
}
