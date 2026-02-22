# Architecture

The Blender Development for PyCharm plugin is built as a set of services within the IntelliJ Platform.

## Core Services

- **`BlenderService`**: The primary project-level facade. It coordinates all other services and provides a simplified API for common operations like starting Blender or reloading extensions.
- **`BlenderDownloader`**: An application-level service responsible for downloading and managing different Blender versions from `download.blender.org`.
- **`BlenderLinker`**: Manages the connection between your project code and Blender. It creates symbolic links (or directory junctions on Windows) in Blender's extensions directory.
- **`BlenderLauncher`**: Handles the process execution of Blender, including setting up environment variables for sandboxing and configuring app templates.
- **`BlenderCommunicationService`**: A TCP server that listens for connections from the Blender instance and sends structured JSON commands (like `reload`).
- **`BlenderScriptGenerator`**: Generates the Python scripts that are injected into Blender at startup to initialize the communication and manage extensions.
- **`BlenderLogger`**: Provides consistent logging to both the IntelliJ platform log and the project-local `blender_plugin.log`.

## Hot-Reloading Protocol

The hot-reloading feature works through a TCP-based communication layer:

1. **Startup**: When Blender starts, the plugin injects a startup script that starts a background thread in Blender.
2. **Connection**: The Blender side connects to the TCP server started by `BlenderCommunicationService`.
3. **Trigger**: When you save a file or use the reload action, PyCharm sends a JSON message: `{"type": "reload", "name": "your_extension"}`.
4. **Execution**: Blender receives the message and schedules a reload task on the main thread using `bpy.app.timers`.
5. **Reload Cycle**:
   - **Disable**: The extension is temporarily disabled.
   - **Purge**: All modules related to the extension are removed from Python's `sys.modules`.
   - **Refresh**: A repository refresh is forced to ensure Blender sees any new files.
   - **Enable**: The extension is re-enabled, forcing a fresh import of the code.

## Sandboxing

The plugin uses the `BLENDER_USER_CONFIG` and `BLENDER_USER_SCRIPTS` environment variables to isolate the development environment. When sandboxing is enabled:
- Configuration is stored in `.blender-sandbox/config`.
- Scripts and extensions are stored in `.blender-sandbox/scripts`.
- An app template named `pycharm` is created to provide a clean environment and a custom splash screen.
