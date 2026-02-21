package com.sakurasedaia.blenderextensions.blender

object BlenderVersions {
    val SUPPORTED_VERSIONS = listOf("4.2", "4.3", "4.4", "4.5", "5.0")
    
    val FALLBACK_PATCHES = mapOf(
        "4.2" to "18",
        "4.3" to "2",
        "4.4" to "1",
        "4.5" to "0",
        "5.0" to "1"
    )
    
    fun getSupportedVersionsWithCustom(): Array<String> {
        return (SUPPORTED_VERSIONS + "Custom/Pre-installed").toTypedArray()
    }
}
