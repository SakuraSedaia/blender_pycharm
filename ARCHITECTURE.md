# Architecture Overview

This document describes the internal workings of the Blender Dev Tools for PyCharm. For a high-level overview, see the [Wiki](https://sakurasedaia.github.io/PycharmBlenderWiki/docs/Architecture.html).

## How it Works

The plugin starts a local TCP server when Blender is launched. It injects a startup Python script into Blender that handles communication and repository management.

### 1. Repository Management
The plugin automatically configures a local extension repository named `blender_pycharm` pointing to the project's symlink. It handles API differences between Blender 4.2+ and 5.0 to ensure compatibility across versions.

### 2. Communication
Blender connects back to PyCharm and listens for structured JSON reload commands (e.g., `{"type": "reload", "name": "my_extension"}`). The communication uses a bidirectional heartbeat and retry logic to maintain a robust connection.

### 3. Robust Reload Cycle
Reloads are executed on Blender's main thread (via `bpy.app.timers`) to avoid threading issues:
- **Disable**: The specific extension module is disabled.
- **Purge**: The module and all its submodules are purged from Python's `sys.modules` to clear the cache.
- **Refresh**: A fresh scan of all extension repositories is triggered via `bpy.ops.extensions.repo_refresh_all()`.
- **Enable**: The extension is re-enabled, forcing a fresh import of your code changes.

### 4. Sandboxing and Configuration
To isolate development settings, the plugin creates a project-local Blender user environment (`.blender-sandbox`). 
- **Isolation**: Uses a project-local app template and user directories to avoid conflicts with your main Blender installation.
- **Customization**: Supports custom splash screens (`splash.png` in project root) and can optionally import your standard Blender user configuration.

### 5. Global Version Management
The plugin automatically handles multi-version downloads (4.2+ and 5.0) and manages global installations. A dedicated tool window provides a UI for downloading, deleting, and monitoring these installations.

## Project Structure

The project follows the standard IntelliJ Platform plugin structure:

```
.
├── build.gradle.kts        # Gradle build configuration
├── src
│   ├── main
│   │   ├── kotlin          # Plugin source code
│   │   │   └── com.sakurasedaia.blenderextensions
│   │   │       ├── actions     # Keyboard shortcuts and menu actions
│   │   │       ├── blender     # Core Blender service, TCP server, and communication
│   │   │       ├── icons       # Custom icons and icon providers
│   │   │       ├── listeners   # File system listeners for auto-reload
│   │   │       ├── project     # Project template generators and manifest wizards
│   │   │       ├── run         # Specialized Run Configurations (Testing, Build, etc.)
│   │   │       ├── settings    # Persistent settings and configuration UI
│   │   │       └── ui          # Blender Management Tool window and UI components
│   │   └── resources
│   │       └── META-INF
│   │           └── plugin.xml  # Plugin manifest
```
