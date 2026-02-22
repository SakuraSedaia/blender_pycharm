package com.sakurasedaia.blenderextensions.blender

import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.file.Path
import java.nio.file.Files
import kotlin.io.path.readText

class BlenderScriptGeneratorTest {

    @Test
    fun testCreateStartupScriptContent() {
        val generator = BlenderScriptGenerator()
        val port = 12345
        val repoDir = Path.of("/tmp/test_repo")
        val extensionName = "my_addon"
        
        val scriptPath = generator.createStartupScript(port, repoDir, extensionName)
        val content = scriptPath.readText()
        
        // Verify key components are present in the script
        assertTrue(content.contains("import bpy"))
        assertTrue(content.contains("import socket"))
        assertTrue(content.contains("def ensure_extension_repo_exists"))
        assertTrue(content.contains("def listen_for_reload"))
        assertTrue(content.contains("def ensure_extension_enabled"))
        
        // Verify variables were correctly injected
        assertTrue(content.contains("$port"))
        assertTrue(content.contains("my_addon"))
        // repoPath is escaped in the script
        assertTrue(content.contains("test_repo"))
        
        // Cleanup
        Files.deleteIfExists(scriptPath)
    }
}
