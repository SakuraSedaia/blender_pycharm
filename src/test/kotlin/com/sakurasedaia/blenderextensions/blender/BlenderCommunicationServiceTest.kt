package com.sakurasedaia.blenderextensions.blender

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import java.net.Socket
import java.util.concurrent.TimeUnit
import java.io.BufferedReader
import java.io.InputStreamReader

class BlenderCommunicationServiceTest : BasePlatformTestCase() {

    fun testServerLifecycle() {
        val service = BlenderCommunicationService.getInstance(project)
        val port = service.startServer()
        assertTrue(port > 0)
        
        // Connect to the server
        val clientSocket = Socket("127.0.0.1", port)
        assertTrue(clientSocket.isConnected)
        
        // Wait a bit for the server thread to accept
        Thread.sleep(200)
        assertTrue(service.isConnected())
        
        service.stopServer()
        assertFalse(service.isConnected())
        assertTrue(clientSocket.isClosed || clientSocket.getInputStream().read() == -1)
        clientSocket.close()
    }

    fun testSendReloadCommand() {
        val service = BlenderCommunicationService.getInstance(project)
        val port = service.startServer()
        
        val clientSocket = Socket("127.0.0.1", port)
        val reader = BufferedReader(InputStreamReader(clientSocket.getInputStream()))
        
        // Wait for server to register connection
        Thread.sleep(200)
        
        service.sendReloadCommand("my_ext")
        
        val received = reader.readLine()
        assertEquals("{\"type\": \"reload\", \"name\": \"my_ext\"}", received)
        
        service.stopServer()
        clientSocket.close()
    }
}
