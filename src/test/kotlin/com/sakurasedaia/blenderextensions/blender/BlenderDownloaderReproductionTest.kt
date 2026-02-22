package com.sakurasedaia.blenderextensions.blender

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import java.nio.file.Path
import kotlin.io.path.createTempDirectory
import kotlin.io.path.deleteRecursively
import junit.framework.Assert.assertNull

class BlenderDownloaderReproductionTest : BasePlatformTestCase() {

    fun testFindBlenderExecutableWithNonExistentDirectory() {
        val downloader = BlenderDownloader(project)
        val nonExistentPath = createTempDirectory("blender_test").resolve("non_existent")
        
        // This should NO LONGER throw NoSuchFileException and should return null
        val method = BlenderDownloader::class.java.getDeclaredMethod("findBlenderExecutable", Path::class.java)
        method.isAccessible = true
        val result = method.invoke(downloader, nonExistentPath)
        
        assertNull("Should return null for non-existent directory", result)
    }
}
