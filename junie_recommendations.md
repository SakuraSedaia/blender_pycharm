# Blender Dev Tools - Post-Audit Recommendations & Implementation Context

These recommendations are designed to improve the plugin's stability, UX, and maintainability before its initial launch.

---

## 1. Stability & Connectivity
*   **Bidirectional Communication (Heartbeat)**
    *   **Goal:** Allow PyCharm to confirm Blender is active and listening.
    *   **Context:**
        *   `BlenderScriptGenerator.kt`: Add a "ready" message to `listen_for_reload` after `s.connect`.
        *   `BlenderCommunicationService.kt`: In the `server.accept()` loop, listen for an initial message from the socket.
    *   **Implementation:** Once "ready" is received, update a project-level state that components can observe.

*   **Retry Logic for TCP Client**
    *   **Goal:** Resilience if Blender takes time to load.
    *   **Context:**
        *   `BlenderScriptGenerator.kt`: Wrap the `s.connect` in the injected Python script in a `while` loop with a `time.sleep(1)` and a max retry count.

---

## 2. UI/UX Refinements
*   **Connection Status Indicator**
    *   **Goal:** Visual feedback in the IDE.
    *   **Context:**
        *   `BlenderToolWindowContent.kt`: Add a status label (e.g., "Status: Connected/Disconnected").
        *   Use `BlenderCommunicationService.isConnected()` to update the UI via a listener or polling.

*   **Balloon Notifications for Critical Errors**
    *   **Goal:** Alert users without requiring log inspection.
    *   **Context:**
        *   `BlenderService.kt`: Use `NotificationGroupManager` to show notifications when `launcher.startBlenderProcess` returns null or `communicationService` fails.

*   **Download Trigger in Run Config**
    *   **Goal:** Pre-emptive downloads.
    *   **Context:**
        *   `BlenderSettingsEditor.kt`: If a non-downloaded version is selected in the dropdown, show an inline button that triggers `BlenderDownloader`.

---

## 3. Feature Extensions
*   **Multiple Source Folders**
    *   **Goal:** Develop interconnected extensions.
    *   **Context:**
        *   `BlenderSettings.kt`: Currently uses `blenderSourceFolders: MutableSet<String>`.
        *   `BlenderService.startBlenderProcess`: Update logic to iterate through all marked folders and create multiple symlinks in the extension repository.

*   **Automatic Python Interpreter Setup**
    *   **Goal:** Proper stubs/linting immediately.
    *   **Context:**
        *   `BlenderProjectTemplateGenerator.kt`: After generating the project, locate the `python` executable in the Blender binary (usually under `5.0/python/bin/python` or similar) and use `PythonSdkUpdater` (IntelliJ Python API) to suggest it as the project interpreter.

---

## 4. Testing & Maintenance
*   **Headless Integration Tests**
    *   **Goal:** Validate the full reload cycle.
    *   **Context:**
        *   Create `BlenderIntegrationTest.kt`. Use `GeneralCommandLine` to launch a managed Blender version with `--background` and the startup script.
        *   Verify the `ServerSocket` in `BlenderCommunicationService` accepts the connection and can send/receive a test command.

*   **Version Metadata Cache**
    *   **Goal:** Performance optimization.
    *   **Context:**
        *   `BlenderScanner.kt`: Store results of `tryGetVersion(path)` in a persistent `PropertiesComponent` or `BlenderSettings` to avoid expensive process calls on every run configuration edit.

---

## 5. Documentation
*   **Troubleshooting & Demo**
    *   **Context:**
        *   `README.md`: Add a "Troubleshooting" section for common macOS/Linux binary path issues.
        *   Add a `.github/workflows/verify.yml` that runs the linter and tests on every push.

---
**Key Files to Review:**
- `BlenderService.kt` (Process management)
- `BlenderCommunicationService.kt` (TCP logic)
- `BlenderScriptGenerator.kt` (Python injection)
- `BlenderScanner.kt` (Detection logic)
- `BlenderSettings.kt` (Persistence)
