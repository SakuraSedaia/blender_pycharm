package com.sakurasedaia.blenderextensions.blender

data class BlenderVersion(
    val majorMinor: String,
    val fallbackPatch: String
)

object BlenderVersions {
    val SUPPORTED_VERSIONS = listOf(
        BlenderVersion("4.2", "18"),
        BlenderVersion("4.5", "7"),
        BlenderVersion("5.0", "1")
    )
    
    fun getSupportedVersionsWithCustom(): Array<String> {
        return SUPPORTED_VERSIONS.map { it.majorMinor }.toTypedArray()
    }

    /**
     * Get a list of all selectable versions, including managed and discovered.
     * Managed versions are just their version strings (e.g. "5.0").
     * Discovered versions are their absolute paths.
     */
    fun getAllSelectableVersions(): List<String> {
        val selectable = mutableListOf<String>()
        
        // Managed versions
        selectable.addAll(SUPPORTED_VERSIONS.map { it.majorMinor })
        
        // System discovered versions
        val systemInstallations = BlenderScanner.scanSystemInstallations()
        selectable.addAll(systemInstallations.map { it.path })
        
        return selectable.distinct()
    }
}
