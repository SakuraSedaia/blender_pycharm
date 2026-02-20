# Blender Extension Development for PyCharm

Blender Extension Development integration for PyCharm. This plugin allows you to launch Blender from within PyCharm and automatically or manually reload your Blender extensions during development.

## AI Content Notice

This project was developed almost exclusively using the AI integration tools by JetBrains s.r.o. in Intellij IDEA. The AI tools were used to generate the initial code, documentation, and some of the content in this README. The final review and editing were done by a human to ensure accuracy and clarity.

## Features

- **Launch Blender**: Start a Blender instance directly from PyCharm.
- **Auto-Reload**: Automatically reload your extension in Blender whenever you save a file in PyCharm.
- **Manual Reload**: Trigger a reload manually using a keyboard shortcut or menu action.
- **Project Template**: Quickly start a new Blender Extension project from a single template that mirrors PyCharm’s Pure Python setup. It includes a “Project name” field that auto-formats to a lowercase-hyphenated folder name. After creation, the generated `README.md` provides clear instructions on configuring your Python interpreter and linting stubs using PyCharm's built-in tools. Optionally enable “Auto-load” to include an `auto_load.py` helper and autoload-ready `__init__.py`. The template also automatically creates a pre-configured Blender Run Configuration.
- **Configurable**: Easily set the path to your Blender executable and toggle auto-reload.

## Operating Instructions

### Setup

1. Open **Run/Debug Configurations** (Run > Edit Configurations...).
2. Click **+** and select **Blender**.
3. Choose a configuration template:
    - **Testing**: Launch Blender in a development environment with auto-reload.
    - **Build**: Build your extension using `blender --command extensions build`.
    - **Validate**: Validate your extension using `blender --command extensions validate`.
    - **Command**: Run a custom Blender command using `blender --command <command>`.
4. Choose a Blender version from the **Blender version** dropdown (4.2+). The plugin will download and manage it for you. Alternatively, pick **Custom/Pre-installed** and set the path manually.
5. (Testing only) Toggle **Enable Sandboxing** to isolate your development environment. When enabled, the plugin runs Blender with a project-local app template and user dirs to avoid conflicts. The plugin includes a default splash screen for sandboxed sessions. You can also provide a custom one by placing a `splash.png` file in your project root. You can disable sandboxing any time.
6. (Testing only) Set the **Addon source directory** (defaults to the project's root) and optionally a **symlink name**.
7. (Optional) In **Settings** > **Tools** > **Blender Extension Integration**, check **Auto-reload extension on save** to enable automatic reloads.

### Logging and Troubleshooting
The plugin maintains a runtime log in the project root: `blender_plugin.log`. This log contains detailed information about:
- Blender downloads and extraction status (including macOS DMG mounting).
- Symbolic link or directory junction creation.
- Startup arguments and sandboxing details.

If you encounter issues, check this log first.

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

The plugin starts a local TCP server when Blender is launched. It injects a startup Python script into Blender that:
1. **Repository Management**: Automatically configures a local extension repository named `blender_pycharm` pointing to the project's symlink.
2. **Communication**: Connects back to PyCharm and listens for structured JSON reload commands (e.g., `{"type": "reload", "name": "my_extension"}`).
3. **Robust Reload Cycle**: Executes a robust reload sequence on Blender's main thread (via `bpy.app.timers`):
    - Disables the specific extension module.
    - Purges the module and its submodules from `sys.modules`.
    - Forces a refresh of all extension repositories (`bpy.ops.extensions.repo_refresh_all()`).
    - Re-enables the extension, forcing a fresh import of your code changes.

This ensures that your code changes are always picked up, avoiding common caching issues in Blender's Python environment.

## Development

### Prerequisites

- [IntelliJ IDEA](https://www.jetbrains.com/idea/) (Community or Ultimate)
- [Blender](https://www.blender.org/) 4.2 or later (supporting extensions)

### Building and Running

- **Build**: `./gradlew build` (The built plugin will be available in `build/distributions/`)
- **Run in Sandbox**: `./gradlew runIde`
- **Tests**: `./gradlew test`

---

## Acknowledgments

This extension is **heavily** based on and inspired by [Jacques Lucke's blender_vscode](https://github.com/JacquesLucke/blender_vscode) extension for Visual Studio Code. Many of the core architectural decisions, particularly the robust hot-reloading mechanism and the extension platform integration, are derived from the excellent work done in that project.

---

For more information on contributing, see [CONTRIBUTING.md](./CONTRIBUTING.md).

---

## Disclaimer

This project is not affiliated with either Blender, the Blender Foundation, or JetBrains s.r.o, as I am not a developer or maintainer for either.