# Blender Extensions for PyCharm

Blender Extensions integration for PyCharm. This plugin allows you to launch Blender from within PyCharm and automatically or manually reload your Blender extensions during development.

## Features

- **Launch Blender**: Start a Blender instance directly from PyCharm.
- **Auto-Reload**: Automatically reload your extension in Blender whenever you save a file in PyCharm.
- **Manual Reload**: Trigger a reload manually using a keyboard shortcut or menu action.
- **Configurable**: Easily set the path to your Blender executable and toggle auto-reload.

## Operating Instructions

### Setup

1. Open **Run/Debug Configurations** (Run > Edit Configurations...).
2. Click **+** and select **Blender**.
3. Set the **Blender path** to your Blender executable (e.g., `C:\Program Files\Blender Foundation\Blender 4.2\blender.exe`).
4. (Optional) In **Settings** > **Tools** > **Blender Extension Integration**, check **Auto-reload extension on save** to enable automatic reloads.

### Usage

- **Start Blender**: Create a new **Blender** Run Configuration (Run > Edit Configurations... > + > Blender) and run it.
- **Reload Extension**:
    - **Manual**: Go to the **Blender** menu and select **Reload Extension**, or use the shortcut `Ctrl+Alt+R`.
    - **Automatic**: If enabled in settings, simply save any file in your project (`Ctrl+S`).

## Project Structure

The project follows the standard IntelliJ Platform plugin structure:

```
.
├── build.gradle.kts        # Gradle build configuration
├── src
│   ├── main
│   │   ├── kotlin          # Plugin source code
│   │   │   └── com.sakurasedaia.blenderextensions
│   │   │       ├── run         # Run Configuration components
│   │   │       ├── blender     # Core Blender service and communication logic
│   │   │       ├── listeners   # File system listeners (Auto-reload)
│   │   │       └── settings    # Persistent settings and configuration UI
│   │   └── resources
│   │       └── META-INF
│   │           └── plugin.xml  # Plugin manifest
```

## How it Works

The plugin starts a local TCP server when Blender is launched. It injects a small Python script into Blender via the `--python` flag. This script connects back to PyCharm and waits for reload commands. When a reload is triggered, PyCharm sends a message over the socket, and the Python script executes `bpy.ops.extensions.package_reload()`.

## Development

### Prerequisites

- [IntelliJ IDEA](https://www.jetbrains.com/idea/) (Community or Ultimate)
- [Blender](https://www.blender.org/) 4.2 or later (supporting extensions)

### Building and Running

- **Build**: `./gradlew build`
- **Run in Sandbox**: `./gradlew runIde`
- **Tests**: `./gradlew test`

---
For more information on contributing, see [CONTRIBUTING.md](./CONTRIBUTING.md).