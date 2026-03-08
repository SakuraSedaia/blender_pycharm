package com.sakurasedaia.blenderextensions.project

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class BlenderProjectGeneratorsTest : BasePlatformTestCase() {

    @Test
    fun testFormatToIdLowercase() {
        assertEquals("my_addon", formatToId("My Addon"))
        assertEquals("my_addon_123", formatToId("My Addon 123!"))
        assertEquals("myaddon", formatToId("my-addon"))
        assertEquals("my_addon", formatToId("My   Addon"))
    }

    @Test
    fun testFormatToIdWithCapitals() {
        assertEquals("My_Addon", formatToId("My Addon", allowCapitals = true))
        assertEquals("My_Addon_123", formatToId("My Addon 123!", allowCapitals = true))
        assertEquals("MyAddon", formatToId("My-Addon", allowCapitals = true))
    }

    @Test
    fun testAddonProjectPeerValidation() {
        val peer = BlenderAddonProjectPeer()
        
        // Initial state should fail validation (Project Name is empty)
        val initialValidation = peer.validate()
        assertNotNull("Initial validation should fail (empty project name)", initialValidation)
        assertTrue(initialValidation!!.message.contains("Project name"))
        
        // Set Project Name
        peer.projectNameField.text = "My Project"
        
        // Next should fail on ID (which is "My_Project" by default, but wait, 
        // projectNameField listener updates ID field)
        com.intellij.util.ui.UIUtil.dispatchAllInvocationEvents()
        
        // Initial state after setting name should pass now because ID is auto-filled
        assertNull("Validation should pass now", peer.validate())
        
        // Invalid ID
        peer.addonIdField.text = "My Addon"
        assertNotNull("Validation should fail (spaces not allowed in ID)", peer.validate())
        
        // Invalid Blender version
        peer.addonIdField.text = "my_addon"
        peer.blenderVersionMinField.text = "4.2" // Should be 4.2.0
        val versionValidation = peer.validate()
        assertNotNull("Validation should fail (invalid version format)", versionValidation)
        assertTrue(versionValidation!!.message.contains("format x.y.z"))
        
        // Fix version
        peer.blenderVersionMinField.text = "4.2.0"
        assertNull("Validation should pass now", peer.validate())
        
        // Test permissions reason
        peer.permissionNetworkCheckbox.isSelected = true
        peer.permissionNetworkReasonField.text = ""
        val permissionValidation = peer.validate()
        assertNotNull("Validation should fail (empty reason)", permissionValidation)
        assertTrue(permissionValidation!!.message.contains("requires a reason"))
        
        peer.permissionNetworkReasonField.text = "Need internet"
        assertNull("Validation should pass with reason", peer.validate())
        
        // Reason ending with period
        peer.permissionNetworkReasonField.text = "Need internet."
        val periodValidation = peer.validate()
        assertNotNull("Validation should fail (reason ends with period)", periodValidation)
        assertTrue(periodValidation!!.message.contains("should not end with a period"))
    }

    @Test
    fun testValidationWithProjectNameAutoUpdate() {
        val peer = BlenderAddonProjectPeer()
        var stateChangedCount = 0
        peer.addSettingsStateListener {
            stateChangedCount++
        }

        // Set initial location so updateLocationFromProjectName works
        peer.updateLocation("/tmp/myproject")
        
        // Changing project name should update Addon ID and location
        peer.projectNameField.text = "New Project"
        
        // Since it uses invokeLater, we might need to wait or flush events if possible.
        // In BasePlatformTestCase, we can use UIUtil.dispatchAllInvocationEvents()
        com.intellij.util.ui.UIUtil.dispatchAllInvocationEvents()
        
        assertEquals("new_project", peer.addonIdField.text)
        assertNull(peer.validate())
        assertTrue("State changed should have been notified", stateChangedCount > 0)
    }
}
