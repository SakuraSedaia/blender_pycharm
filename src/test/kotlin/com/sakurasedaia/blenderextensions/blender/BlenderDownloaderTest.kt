package com.sakurasedaia.blenderextensions.blender

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.PosixFilePermission
import kotlin.io.path.createDirectories
import kotlin.io.path.createFile
import kotlin.io.path.name

class BlenderDownloaderTest : BasePlatformTestCase() {

    fun testGetDownloadDirectory() {
        val downloader = BlenderDownloader.getInstance(project)
        val dir = downloader.getDownloadDirectory("4.2")
        assertTrue(dir.toString().contains("blender_downloads"))
        assertTrue(dir.toString().contains("4.2"))
    }

    fun testFindBlenderExecutable() {
        val downloader = BlenderDownloader.getInstance(project)
        val tempDir = Files.createTempDirectory("blender_test")
        
        try {
            // Test with empty directory
            assertNull(downloader.invokePrivate("findBlenderExecutable", tempDir))
            
            // Create a mock executable
            val osName = System.getProperty("os.name").lowercase()
            val isWindows = osName.contains("win")
            val isMac = osName.contains("mac")
            
            val executablePath = if (isWindows) {
                tempDir.resolve("blender.exe").createFile()
            } else if (isMac) {
                val macPath = tempDir.resolve("Blender.app/Contents/MacOS")
                macPath.createDirectories()
                macPath.resolve("Blender").createFile()
            } else {
                val linuxPath = tempDir.resolve("blender").createFile()
                // Set executable permission for Linux to ensure discovery
                try {
                    Files.setPosixFilePermissions(
                        linuxPath,
                        setOf(
                            PosixFilePermission.OWNER_EXECUTE,
                            PosixFilePermission.OWNER_READ,
                            PosixFilePermission.GROUP_EXECUTE,
                            PosixFilePermission.OTHERS_EXECUTE
                        )
                    )
                } catch (_: Exception) { /* ignore on non-posix FS */ }
                linuxPath
            }
            
            val found = downloader.invokePrivate("findBlenderExecutable", tempDir) as Path?
            assertNotNull(found)
            val normalized = found!!.name.replace(".exe", "").lowercase()
            assertEquals("blender", normalized)
            
        } finally {
            tempDir.toFile().deleteRecursively()
        }
    }

    private fun Any.invokePrivate(methodName: String, vararg args: Any?): Any? {
        val method = this.javaClass.getDeclaredMethods().find { it.name == methodName }
            ?: throw NoSuchMethodException("Method $methodName not found")
        method.isAccessible = true
        return method.invoke(this, *args)
    }
}
